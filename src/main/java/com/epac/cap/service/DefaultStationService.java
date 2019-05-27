package com.epac.cap.service;

import java.util.ArrayList;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.handler.DefaultStationHandler;
import com.epac.cap.model.DefaultStation;
import com.epac.cap.repository.StationSearchBean;
import com.epac.cap.validator.InputResponseError;

@Controller
@Path("/defaultStations")
public class DefaultStationService extends AbstractService{
	
	@Autowired
	private DefaultStationHandler defaultStationHandler;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDefaultStations(){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<DefaultStation> defaultStations = new ArrayList<DefaultStation>();
		try {
			defaultStations = defaultStationHandler.readAll();
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of default station records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
			//return ImpositionResponse.serverError().build();
		}
		return Response.ok(defaultStations).build();
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDefaultStation(@PathParam("id") String id){
		DefaultStation defaultStation = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			defaultStation = defaultStationHandler.read(id);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of station records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(defaultStation).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response add(DefaultStation defaultStation){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			//check for duplication 
			StationSearchBean ssb = new StationSearchBean();
			ssb.setPartCategoryId(defaultStation.getId().getCategoryId());
			ssb.setPartCritiriaId(defaultStation.getId().getCritiriaId());
			ssb.setBindingTypeId(defaultStation.getId().getBindingTypeId());
			ssb.setStationCategoryId(defaultStation.getId().getStationCategoryId());
			List<DefaultStation> foundResult = defaultStationHandler.readAll(ssb);
			if(!foundResult.isEmpty()){
				constraintViolationsMessages.put("errors", "A duplicate default station is detected!");
			}
			
			if(constraintViolationsMessages.isEmpty()){
				doAddLogging(defaultStation);
				defaultStationHandler.create(defaultStation);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while adding the default station record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;	
	}
	
	@Path("{id}")
	@DELETE
	@Transactional
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response delete(@PathParam("id") String id){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response res = null;
        try{
        	//Check if ok to delete the default station
        	DefaultStation defaultStation = defaultStationHandler.read(id);
        	if(defaultStation != null){
	        	//if(station.getJobs().isEmpty() && station.getMachines().isEmpty() && station.getDefaultStations().isEmpty()){
	        		defaultStationHandler.delete(defaultStation);
	        		res = Response.status(200).build();
	        	//}else{
	        	//	constraintViolationsMessages.put("errors", "Cannot delete the station as it is being used!");
	        	//	InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				//	res = inputResponseError.createResponse();
	        	//}
        	}else{
        		return Response.status(404).entity("Default Station with the id : " + id + " not present in the database").build();
	        }
        }catch (PersistenceException e) {
        	constraintViolationsMessages.put("errors", "An error occurred while deleting the default station record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return res;
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response update(DefaultStation defaultStation){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			
			if(constraintViolationsMessages.isEmpty()){
				doEditLogging(defaultStation);
				
				defaultStationHandler.update(defaultStation);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while updating the default station record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}

	/**
	 * @return the defaultStationHandler
	 */
	public DefaultStationHandler getDefaultStationHandler() {
		return defaultStationHandler;
	}

	/**
	 * @param defaultStationHandler the defaultStationHandler to set
	 */
	public void setDefaultStationHandler(DefaultStationHandler defaultStationHandler) {
		this.defaultStationHandler = defaultStationHandler;
	}
	
}
