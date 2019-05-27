package com.epac.cap.service;

import java.awt.Desktop;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.epac.cap.common.OrderBy;
import com.epac.cap.common.PersistenceException;
import com.epac.cap.handler.CoverSectionHandler;
import com.epac.cap.model.CoverSection;
import com.epac.cap.model.Log;
import com.epac.cap.model.PackageBook;
import com.epac.cap.model.Pallette;
import com.epac.cap.model.PalletteBook;
import com.epac.cap.model.RollStatus;
import com.epac.cap.model.Pallette.PalletteStatus;
import com.epac.cap.repository.CoverSectionDAO;
import com.epac.cap.repository.CoverSectionSearchBean;
import com.epac.cap.repository.PalletteSearchBean;
import com.epac.cap.utils.AoData;
import com.epac.cap.utils.AoDataParser;
import com.epac.cap.utils.LogUtils;
import com.epac.cap.utils.PaginatedResult;
import com.epac.cap.validator.InputResponseError;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Controller
@Path("/sections")
public class CoverSectionService extends AbstractService {


	@Autowired
	private CoverSectionHandler coverSectionHandler;

	@Autowired
	private CoverSectionDAO coverSectionDAO;

	@GET
	@Path("/allNew")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNewSections() {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<CoverSection> sections = new ArrayList<CoverSection>();
		try {
			/*CoverSectionSearchBean csb = new CoverSectionSearchBean();
			csb.setStatus(RollStatus.statuses.NEW.toString());
			sections = coverSectionHandler.readAll(csb);*/
			sections = coverSectionHandler.findNewSection();
		} catch (Exception e) {
			constraintViolationsMessages.put("errors",
					"An error occurred while reading the list of new cover sections records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(sections).build();
	}

	@GET
	@Path("/id/{sectionId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSectionById(@PathParam("sectionId") Integer sectionId) {
		CoverSection section = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			section = coverSectionDAO.read(sectionId);
		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the section by Id!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(section).build();
	}

	@GET
	@Path("/name/{sectionName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSectionByName(@PathParam("sectionName") String sectionName) {
		CoverSectionSearchBean section = new CoverSectionSearchBean();
		List<CoverSection> secList = new ArrayList<CoverSection>();
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			section.setCoverSectionName(sectionName);
			secList = coverSectionHandler.readAll(section);
		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the section by name!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(secList.iterator().next()).build();
	}

	@GET
	@Path("/rerun/{sectionId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response recreateSection(@PathParam("sectionId") Integer sectionId) {
		Response response = null;
		List<String> messages = new ArrayList<String>();
		try {
				if (sectionId != null) {
					messages = coverSectionHandler.recreateSection(sectionId);
					LogUtils.debug("received messages while re creation of the section: ["+messages+"]");
				}
				response = Response.ok(messages).build();
			
		} catch (Exception e) {
			LogUtils.error("An error occurred while re creation of the section!", e);
		}
		return response;
	}

	@GET
	@Path("/unassign/{sectionId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response unassignBatch(@PathParam("sectionId") Integer sectionId) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			String executingUserId = this.getExecutingUserId();// TODO make sure
																// this works
			if (constraintViolationsMessages.isEmpty()) {
				if (sectionId != null) {
					coverSectionHandler.unassignSection(sectionId, executingUserId);
				}
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors",
					"An error occurred while un-assigning a section from the machine!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return response;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSections(){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<CoverSection> sections = new ArrayList<CoverSection>();
		try {
			sections = coverSectionHandler.readAll();
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of sections records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(sections).build();
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(CoverSection section){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			
			if(constraintViolationsMessages.isEmpty()){
				doEditLogging(section);
				
				coverSectionHandler.update(section);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while updating the section record:"+section.getCoverSectionId()+"!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}
	
	@GET
	@Path("/document/{sectionId}")
	@Produces("application/pdf")
	public javax.ws.rs.core.Response readFile(@PathParam("sectionId") Integer sectionId) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		File file = null;
		try {
			CoverSection section = coverSectionDAO.read(sectionId);
			if (section != null && !section.getPath().isEmpty()) {
				file = new File(section.getPath());
			} else {
				return Response.status(404).entity("File Location not present in the system").build();
			}

			if (file != null && file.exists()) {
				ResponseBuilder response = Response.ok((Object) file);
				if (file.isFile()) {
					response.type("application/pdf");
					response.header("Content-Disposition", "attachment; filename=" + file.getName());
					return response.build();
				} else if (file.isDirectory()) {
					response.header("Content-Disposition", "directory");
					Desktop desktop = Desktop.getDesktop();
					desktop.open(file);
				}
				return null;
			}
			return Response.status(404).entity("File Location not present in the system").build();
		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "Error while downloading the file. Please try again !!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
	}
	
	
	
	@POST
	@Path("/paginated")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllCoversPaginated(String data){
		
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		PaginatedResult paginatedResult = new PaginatedResult();		
		CoverSectionSearchBean searchBean = new CoverSectionSearchBean();		
		List<CoverSection> sections = new ArrayList<CoverSection>();
		List<Object> pageList = new ArrayList<Object>();
		
		
		ObjectMapper mapper = new ObjectMapper();
		AoData aoDataObject = AoDataParser.parse(data);
		try {

			
			int draw = aoDataObject.getAjaxRequestId();
			int start = aoDataObject.getStartIndex();
			int pageLength = aoDataObject.getPageLength();
			
			//logger.debug("get page");
			//logger.debug("draw:" + draw);
			//logger.debug("start:" + start);
			//logger.debug("pageLength:" + pageLength);
			
			Integer count = 0;		
			

			//sorting
			String sortingColumnName = aoDataObject.getSortingColumnName();
			
			OrderBy orderBy = new OrderBy();			
			orderBy.setDirection(aoDataObject.getOrderBy());

			
			if(sortingColumnName.equals("coverSectionId"))orderBy.setName("coverSectionId");
			if(sortingColumnName.equals("coverSectionName"))orderBy.setName("coverSectionName");
			if(sortingColumnName.equals("status.name"))orderBy.setName("status.name");
			if(sortingColumnName.equals("laminationType.id"))orderBy.setName("laminationType.id");
			if(sortingColumnName.equals("quantity"))orderBy.setName("quantity");
			if(sortingColumnName.equals("priority"))orderBy.setName("priority");
			if(sortingColumnName.equals("dueDate"))orderBy.setName("dueDate");
			
			searchBean.getOrderByList().clear();
			searchBean.addOrderBy(orderBy);
			//-----------------------------------------------------------------------------------------------------
			
			if(aoDataObject.isColumnsFiltering() && !aoDataObject.isGeneralFiltering()) {
				//-----------------------------------------------------------------------------------------------------
				
			
				//filtering
				for(String columnNameToFilter  : aoDataObject.getColumnsFilters().keySet()) {
					String filterValue = aoDataObject.getColumnsFilters().get(columnNameToFilter);
					
					if(columnNameToFilter.equals("coverSectionId")) {
						try {	searchBean.setCoverSectionId(Integer.parseInt(filterValue));} catch (Exception e) {}
					}
					
					if(columnNameToFilter.equals("coverSectionName")) {
						try {	searchBean.setCoverSectionName(filterValue);} catch (Exception e) {}
					}
					
					if(columnNameToFilter.equals("status.name")) {
						try {	searchBean.setStatusName(filterValue);} catch (Exception e) {}
					}
					
					if(columnNameToFilter.equals("laminationType.id")) {
						try {	searchBean.setLaminationTypeId(filterValue);} catch (Exception e) {}
					}
					
					if(columnNameToFilter.equals("quantity")) {
						try {	searchBean.setQuantity(Integer.parseInt(filterValue));} catch (Exception e) {}
					}

					if(columnNameToFilter.equals("priority")) {
						try {	searchBean.setPriority(filterValue);} catch (Exception e) {}
					}
					
					if(columnNameToFilter.equals("dueDate")) {
						try {					
							SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
						    Date date = parser.parse(filterValue); 
							searchBean.setDueDate(date);
						} catch (Exception e) {}
					}
				}
				
				searchBean.setResultOffset(null);
				searchBean.setMaxResults(null);
				sections = coverSectionHandler.readAll(searchBean);
				count = sections.size();
				
				searchBean.setResultOffset(start);
				searchBean.setMaxResults(pageLength);
				sections = coverSectionHandler.readAll(searchBean);
		

				//-----------------------------------------------------------------------------------------------------
			}else if(aoDataObject.isGeneralFiltering()) {
				
				sections = coverSectionHandler.fullSearch(aoDataObject.getGeneralFilter(), null, null);
				count = sections.size();				
				sections = coverSectionHandler.fullSearch(aoDataObject.getGeneralFilter(), pageLength, start);
				
			}else {				
				
				count = coverSectionHandler.getCount();
				
				searchBean.setResultOffset(start);
				searchBean.setMaxResults(pageLength);
				sections = coverSectionHandler.readAll(searchBean);
			}
			
			
			
			
			//-----------------------------------------------------------------------------------------------------
			for(CoverSection section : sections) {
				section.getJobs().clear();
			}
			pageList.addAll(sections);
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
	




}
