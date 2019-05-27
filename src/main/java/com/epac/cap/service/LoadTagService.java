package com.epac.cap.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.OrderBy;
import com.epac.cap.common.PersistenceException;
import com.epac.cap.handler.LoadTagHandler;
import com.epac.cap.model.LoadTag;
import com.epac.cap.repository.LoadTagSearchBean;
import com.epac.cap.utils.PaginatedResult;
import com.epac.cap.validator.InputResponseError;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@Path("/loadTags")
public class LoadTagService extends AbstractService{

	@Autowired
	private LoadTagHandler loadTagHandler;
	
	private static Logger logger = Logger.getLogger(LoadTagService.class);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLoadTags(){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<LoadTag> loadTags = new ArrayList<LoadTag>();
		try {
			loadTags = loadTagHandler.readAll();
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of LoadTag records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(loadTags).build();
	}

	@POST
	@Path("/paginated")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPaginatedRolls(String data){
		
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		PaginatedResult paginatedResult = new PaginatedResult();
		List<LoadTag> pageList = new ArrayList<LoadTag>();
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			
			
			JsonNode json = mapper.readTree(data);
			
			JsonNode aoData = json.get("aoData");
			
			int draw = aoData.get(0).get("value").asInt();
			int start = aoData.get(3).get("value").asInt();
			int pageLength = aoData.get(4).get("value").asInt();
			
			//logger.debug("get page");
			//logger.debug("draw:" + draw);
			//logger.debug("start:" + start);
			//logger.debug("pageLength:" + pageLength);
			
			Integer count = 0;

			LoadTagSearchBean searchBean = new LoadTagSearchBean();
			searchBean.setResultOffset(start);
			searchBean.setMaxResults(pageLength);

			
			//sorting
			int sortingColumn = aoData.get(2).get("value").get(0).get("column").asInt();
			
			String sortingColumnName = aoData.get(1).get("value").get(sortingColumn).get("data").asText();
			
			String sortingDirection = aoData.get(2).get("value").get(0).get("dir").asText();
			
			OrderBy orderBy = new OrderBy();			
			if(sortingDirection.equals("asc"))orderBy.setDirection(OrderBy.ASC);
			if(sortingDirection.equals("desc"))orderBy.setDirection(OrderBy.DESC);
			
			if(sortingColumnName.equals("loadTagId"))orderBy.setName("loadTagId");
			if(sortingColumnName.equals("jobId"))orderBy.setName("jobId");
			if(sortingColumnName.equals("quantity"))orderBy.setName("quantity");
			if(sortingColumnName.equals("startTime"))orderBy.setName("startTime");
			if(sortingColumnName.equals("finishTime"))orderBy.setName("finishTime");
			if(sortingColumnName.equals("waste"))orderBy.setName("waste");
			if(sortingColumnName.equals("cartNum"))orderBy.setName("cartNum");
			if(sortingColumnName.equals("usedFlag"))orderBy.setName("usedFlag");			
			
			searchBean.getOrderByList().clear();
			searchBean.addOrderBy(orderBy);
			//-----------------------------------------------------------------------------------------------------

			//filtering
			boolean filtering = false;
			String filter = aoData.get(1).get("value").get(0).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setSearchLoadtagIdPart(filter);
					filtering = true;
				} catch (Exception e) {}
			}

			filter = aoData.get(1).get("value").get(1).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setSearchJobIdPart(filter);
					filtering = true;
				} catch (Exception e) {}
			}

			filter = aoData.get(1).get("value").get(2).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setSearchQuantityPart(filter);
					filtering = true;
				} catch (Exception e) {}
			}

			filter = aoData.get(1).get("value").get(3).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {					
					SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
				    Date date = parser.parse(filter); 
					searchBean.setStartDateExact(date);
					filtering = true;
				} catch (Exception e) {}
			}

			
			filter = aoData.get(1).get("value").get(4).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {					
					SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
				    Date date = parser.parse(filter); 
					searchBean.setFinishDateExact(date);
					filtering = true;
				} catch (Exception e) {}
			}
			
			filter = aoData.get(1).get("value").get(5).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setSearchWastePart(filter);
					filtering = true;
				} catch (Exception e) {}
			}
			
			filter = aoData.get(1).get("value").get(6).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				searchBean.setCartNum(filter);
				filtering = true;
			}
			

			//-----------------------------------------------------------------------------------------------------
			
			
			//search word
			String wordToSearch = aoData.get(5).get("value").get("value").asText();
			
			if(wordToSearch != null && !wordToSearch.isEmpty()) {
				
				//logger.debug("wordToSearch:" + wordToSearch);
				pageList = loadTagHandler.fullSearch(wordToSearch, null, null);
				count = pageList.size();				
				pageList = loadTagHandler.fullSearch(wordToSearch, pageLength, start);
				
				
			}else if(filtering){
				
				searchBean.setResultOffset(null);
				searchBean.setMaxResults(null);
				pageList = loadTagHandler.readAll(searchBean);
				count = pageList.size();
				
				searchBean.setResultOffset(start);
				searchBean.setMaxResults(pageLength);
				pageList = loadTagHandler.readAll(searchBean);
				
			}else {
				count = loadTagHandler.getCount();				
				pageList = loadTagHandler.readAll(searchBean);
			}
			
			//-----------------------------------------------------------------------------------------------------

			//logger.debug("list:" + pageList.size());
			//logger.debug("global count:" + count);
			
			paginatedResult.setDraw(draw);
			paginatedResult.setRecordsFiltered(count);
			paginatedResult.setRecordsTotal(count);
			paginatedResult.setData(pageList);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of roll records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(paginatedResult).build();
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLoadTag(@PathParam("id") Integer id){
		LoadTag loadTag = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			loadTag = loadTagHandler.read(id);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the LoadTag record with id: " + id);
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(loadTag).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response add(LoadTag loadTag){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			
			if(constraintViolationsMessages.isEmpty()){
				doAddLogging(loadTag);
				
				loadTagHandler.create(loadTag);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while adding the LoadTag record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;	
	}
	
	@POST
	@Path("/fromProd")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addFromProd(LoadTag loadTag){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			if(constraintViolationsMessages.isEmpty()){
				doAddLogging(loadTag);
				loadTagHandler.createFromProd(loadTag);
				response = Response.ok(loadTag).build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while adding the LoadTag record from Production Dashboard!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;	
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(LoadTag loadTag){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			
			if(constraintViolationsMessages.isEmpty()){
				doEditLogging(loadTag);
				
				loadTagHandler.update(loadTag);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while updating the LoadTag record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}
	
	@PUT
	@Path("/fromProd")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateFromProd(LoadTag loadTag){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			if(constraintViolationsMessages.isEmpty()){
				doEditLogging(loadTag);
				loadTagHandler.updateFromProd(loadTag);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while updating the LoadTag record from Production dashboard!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}
	
	@Path("{id}")
	@DELETE
	@Transactional
	public Response delete(@PathParam("id") Integer id){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response res = null;
        try{
        	LoadTag loadTag = loadTagHandler.read(id);
        	if(loadTag != null){
        		loadTagHandler.delete(loadTag);
	            res = Response.status(200).build();
        		
        	}else{
        		return Response.status(404).entity("LoadTag with id : " + id + " not present in the database").build();
        	}
        }catch (PersistenceException e) {
        	constraintViolationsMessages.put("errors", "An error occurred while deleting the LoadTag record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return res;
	}

	/**
	 * @return the loadTagHandler
	 */
	public LoadTagHandler getLoadTagHandler() {
		return loadTagHandler;
	}

	/**
	 * @param loadTagHandler the loadTagHandler to set
	 */
	public void setLoadTagHandler(LoadTagHandler loadTagHandler) {
		this.loadTagHandler = loadTagHandler;
	}


}
