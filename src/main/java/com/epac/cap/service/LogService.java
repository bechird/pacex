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
import com.epac.cap.handler.JobHandler;
import com.epac.cap.handler.LogHandler;
import com.epac.cap.handler.LookupHandler;
import com.epac.cap.handler.RollHandler;
import com.epac.cap.model.Log;
import com.epac.cap.repository.LoadTagSearchBean;
import com.epac.cap.repository.LogSearchBean;
import com.epac.cap.utils.LogUtils;
import com.epac.cap.utils.PaginatedResult;
import com.epac.cap.validator.InputResponseError;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@Path("/logs")
public class LogService extends AbstractService{

	@Autowired
	private LogHandler logHandler;
	
	@Autowired
	private JobHandler jobHandler;
	
	@Autowired
	private RollHandler rollHandler;
	
	@Autowired
	private LookupHandler lookupHandler;
	
	private static Logger logger = Logger.getLogger(LogService.class);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLogs(){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Log> logs = new ArrayList<Log>();
		try {
			logs = logHandler.readAll();
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of log records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(logs).build();
	}

	@POST
	@Path("/paginated")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPaginatedRolls(String data){
		
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		PaginatedResult paginatedResult = new PaginatedResult();
		List<Log> pageList = new ArrayList<Log>();
		
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

			LogSearchBean searchBean = new LogSearchBean();
			searchBean.setResultOffset(start);
			searchBean.setMaxResults(pageLength);

			
			//sorting
			int sortingColumn = aoData.get(2).get("value").get(0).get("column").asInt();
			
			String sortingColumnName = aoData.get(1).get("value").get(sortingColumn).get("data").asText();
			
			String sortingDirection = aoData.get(2).get("value").get(0).get("dir").asText();
			
			OrderBy orderBy = new OrderBy();			
			if(sortingDirection.equals("asc"))orderBy.setDirection(OrderBy.ASC);
			if(sortingDirection.equals("desc"))orderBy.setDirection(OrderBy.DESC);
			
			if(sortingColumnName.equals("logId"))orderBy.setName("logId");
			if(sortingColumnName.equals("machineId"))orderBy.setName("machineId");
			if(sortingColumnName.equals("currentJobId"))orderBy.setName("currentJobId");
			if(sortingColumnName.equals("event"))orderBy.setName("event");
			if(sortingColumnName.equals("logCause.name"))orderBy.setName("lc.name");
			if(sortingColumnName.equals("logResult.name"))orderBy.setName("lr.name");
			if(sortingColumnName.equals("startTime"))orderBy.setName("startTime");
			if(sortingColumnName.equals("finishTime"))orderBy.setName("finishTime");
			
			if(sortingColumnName.equals("rollId"))orderBy.setName("rollId");	
			if(sortingColumnName.equals("rollLength"))orderBy.setName("rollLength");	
			if(sortingColumnName.equals("counterFeet"))orderBy.setName("counterFeet");	
			
			searchBean.getOrderByList().clear();
			searchBean.addOrderBy(orderBy);
			//-----------------------------------------------------------------------------------------------------

			//filtering
			boolean filtering = false;
			
			String filter = aoData.get(1).get("value").get(0).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setSearchLogIdPart(filter);
					filtering = true;
				} catch (Exception e) {}
			}

			filter = aoData.get(1).get("value").get(1).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
					searchBean.setSearchMachineIdPart(filter);
					filtering = true;
			}

			filter = aoData.get(1).get("value").get(2).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setSearchJobIdPart(filter);
					filtering = true;
				} catch (Exception e) {}
			}
			
			filter = aoData.get(1).get("value").get(3).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
					searchBean.setEvent(filter);
					filtering = true;
			}

			filter = aoData.get(1).get("value").get(4).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
					searchBean.setSearchCausePart(filter);
					filtering = true;
			}

			filter = aoData.get(1).get("value").get(5).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
					searchBean.setSearchResultPart(filter);
					filtering = true;
			}

			filter = aoData.get(1).get("value").get(6).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {					
					SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
				    Date date = parser.parse(filter); 
					searchBean.setStartDateExact(date);
					filtering = true;
				} catch (Exception e) {}
			}

			
			filter = aoData.get(1).get("value").get(7).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {					
					SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
				    Date date = parser.parse(filter); 
					searchBean.setFinishDateExact(date);
					filtering = true;
				} catch (Exception e) {}
			}
			
			filter = aoData.get(1).get("value").get(8).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setSearchRollIdPart(filter);
					filtering = true;
				} catch (Exception e) {}
			}

			filter = aoData.get(1).get("value").get(9).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setSearchRollLengthPart(filter);
					filtering = true;
				} catch (Exception e) {}
			}

			filter = aoData.get(1).get("value").get(10).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setSearchCounterFeetPart(filter);
					filtering = true;
				} catch (Exception e) {}
			}
			//-----------------------------------------------------------------------------------------------------
			
			
			//search word
			String wordToSearch = aoData.get(5).get("value").get("value").asText();
			
			if(wordToSearch != null && !wordToSearch.isEmpty()) {
				
				//logger.debug("wordToSearch:" + wordToSearch);
				pageList = logHandler.fullSearch(wordToSearch, null, null);
				count = pageList.size();				
				pageList = logHandler.fullSearch(wordToSearch, pageLength, start);
				
				
			}else if(filtering){
				
				searchBean.setResultOffset(null);
				searchBean.setMaxResults(null);
				pageList = logHandler.readAll(searchBean);
				count = pageList.size();
				
				searchBean.setResultOffset(start);
				searchBean.setMaxResults(pageLength);
				pageList = logHandler.readAll(searchBean);
				
			}else {
				count = logHandler.getCount();				
				pageList = logHandler.readAll(searchBean);
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
	public Response getLog(@PathParam("id") Integer id){
		Log log = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			log = logHandler.read(id);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the log record with id: " + id);
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(log).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response add(Log log){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			
			if(constraintViolationsMessages.isEmpty()){
				doAddLogging(log);
				
				logHandler.create(log);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while adding the log record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;	
	}
	
	@POST
	@Path("/fromProd")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addFromProd(Log log){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			if(constraintViolationsMessages.isEmpty()){
				doAddLogging(log);
				logHandler.createFromProd(log);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while adding the log record from the production dashboard!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;	
	}
	
	@POST
	@Path("/handleInterruption")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response handleInterruption(Log log){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		String executingUserId = this.getExecutingUserId();
		try {
			if(constraintViolationsMessages.isEmpty()){
				doAddLogging(log);
				logHandler.handleInterruption(log, executingUserId);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			LogUtils.error("An error occurred while handling the interruption from the production dashboard!",e); 
			constraintViolationsMessages.put("errors", "An error occurred while handling the interruption from the production dashboard!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;	
	}
	
	/*@POST
	@Path("/completedJobs")
	@Consumes(MediaType.APPLICATION_JSON)
	public ImpositionResponse setCompletedJobsFromProd(List<String> completedJobs){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		ImpositionResponse response = null;
		String executingUserId = this.getExecutingUserId();
		try {
			if(constraintViolationsMessages.isEmpty()){
				logHandler.setCompletedJobsFromProd(completedJobs, executingUserId);
				response = ImpositionResponse.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while setting the completed jobs from the production dashboard!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;	
	}
	
	@POST
	@Path("/allCompletedJobs")
	@Consumes(MediaType.APPLICATION_JSON)
	public ImpositionResponse setAllCompletedJobsFromProd(List<String> completedJobs){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		ImpositionResponse response = null;
		String executingUserId = this.getExecutingUserId();
		try {
			if(constraintViolationsMessages.isEmpty()){
				logHandler.setAllCompletedJobsFromProd(completedJobs, executingUserId);
				response = ImpositionResponse.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while setting the completed jobs from the production dashboard!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;	
	}*/
	
	@Path("{id}")
	@DELETE
	@Transactional
	public Response delete(@PathParam("id") Integer id){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response res = null;
        try{
        	Log log = logHandler.read(id);
        	if(log != null){
        		//see if ok to delete the log 
        		//if(roll.getLogs().isEmpty() && roll.getJobs().isEmpty()){
	        		logHandler.delete(log);
	            	res = Response.status(200).build();
        		//}else{
        		//	constraintViolationsMessages.put("errors", "Cannot delete the roll as it has jobs or logs assigned to it!");
            	//	InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
    			//	res = inputResponseError.createResponse();
        		//}
        	}else{
        		return Response.status(404).entity("Log with id : " + id + " not present in the database").build();
        	}
        }catch (PersistenceException e) {
        	constraintViolationsMessages.put("errors", "An error occurred while deleting the log record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return res;
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(Log log){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			
			if(constraintViolationsMessages.isEmpty()){
				doEditLogging(log);
				
				logHandler.update(log);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while updating the log record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}

	/**
	 * @return the logHandler
	 */
	public LogHandler getLogHandler() {
		return logHandler;
	}

	/**
	 * @param logHandler the logHandler to set
	 */
	public void setLogHandler(LogHandler logHandler) {
		this.logHandler = logHandler;
	}

	/**
	 * @return the jobHandler
	 */
	public JobHandler getJobHandler() {
		return jobHandler;
	}

	/**
	 * @param jobHandler the jobHandler to set
	 */
	public void setJobHandler(JobHandler jobHandler) {
		this.jobHandler = jobHandler;
	}

	/**
	 * @return the rollHandler
	 */
	public RollHandler getRollHandler() {
		return rollHandler;
	}

	/**
	 * @param rollHandler the rollHandler to set
	 */
	public void setRollHandler(RollHandler rollHandler) {
		this.rollHandler = rollHandler;
	}

	/**
	 * @return the lookupHandler
	 */
	public LookupHandler getLookupHandler() {
		return lookupHandler;
	}

	/**
	 * @param lookupHandler the lookupHandler to set
	 */
	public void setLookupHandler(LookupHandler lookupHandler) {
		this.lookupHandler = lookupHandler;
	}


}
