package com.epac.cap.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
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

import com.epac.cap.common.OrderBy;
import com.epac.cap.common.PersistenceException;
import com.epac.cap.handler.JobHandler;
import com.epac.cap.handler.JobsByMachineOrderingComparator;
import com.epac.cap.handler.MachineHandler;
import com.epac.cap.handler.PartHandler;
import com.epac.cap.handler.RollHandler;
import com.epac.cap.handler.RollsByMachineOrderingComparator;
import com.epac.cap.handler.StationHandler;
import com.epac.cap.model.Job;
import com.epac.cap.model.Roll;
import com.epac.cap.model.Station;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.repository.StationSearchBean;
import com.epac.cap.validator.InputResponseError;

@Controller
@Path("/stations")
public class StationService extends AbstractService{
	
	@Autowired
	private StationHandler stationHandler;
	
	@Autowired
	private MachineHandler machineHandler;
	
	@Autowired
	private JobHandler jobHandler;
	
	@Autowired
	private RollHandler rollHandler;
	
	@Autowired
	private PartHandler partHandler;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStations(){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Station> stations = new ArrayList<Station>();
		try {
			StationSearchBean ssb = new StationSearchBean();
			ssb.setOrderByList(Arrays.asList(new OrderBy("productionOrdering"), new OrderBy("stationId", "desc")));
			stations = stationHandler.readAll(ssb);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of station records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
			//return ImpositionResponse.serverError().build();
		}
		return Response.ok(stations).build();
	}
	
	@GET
	@Path("/quick")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStationsQuickMode(){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Station> stations = new ArrayList<Station>();
		//try {
			StationSearchBean ssb = new StationSearchBean();
			ssb.setOrderByList(Arrays.asList(new OrderBy("productionOrdering"), new OrderBy("stationId", "desc")));
			ssb.setListing(true);
			//stations = stationHandler.readAll(ssb);
			stations = stationHandler.fetchStation();
		/*} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of station records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
			//return ImpositionResponse.serverError().build();
		}*/
		return Response.ok(stations).build();
	}
	
	@GET
	@Path("/menuList")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStationsForMenu(){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<String[]> stations = new ArrayList<String[]>();
		try {
			stations = stationHandler.readStationsForMenu();
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of station records for menu!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
			//return ImpositionResponse.serverError().build();
		}
		return Response.ok(stations).build();
	}
	
	/**
	 * Used for the overview schedule board page to build the list of stations with their scheduled/unscheduled hours, and available running jobs
	 * @return
	 */
	@GET
	@Path("/overview")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStationsForOverview(){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Station> stations = new ArrayList<Station>();
		try {
			StationSearchBean ssb = new StationSearchBean();
			ssb.setOrderBy("productionOrdering");
			stations = stationHandler.readAll(ssb);
			for(Station s : stations){
				s.setScheduledHours(jobHandler.calculateStationHours(s.getStationId(), "S"));
				s.setUnscheduledHours(jobHandler.calculateStationHours(s.getStationId(), "U"));
				List<Job> tmpList = jobHandler.getStationJobs(s.getStationId());
				TreeSet<Job> ts = new TreeSet<Job>(new JobsByMachineOrderingComparator());
				ts.addAll(tmpList);
				s.setJobs(ts);

				
				if(Station.inputTypes.Roll.toString().equalsIgnoreCase(s.getInputType())){
					TreeSet<Roll> ts2 = new TreeSet<Roll>(new RollsByMachineOrderingComparator());
					ts2.addAll(jobHandler.getStationRolls(tmpList));
					s.setRolls(ts2);
				}
				
				for(Roll roll : s.getRolls()) {
					roll.getAlljobs().clear();
				}
				
				/*List<Float[]> stationPercentages = jobHandler.getJobsPercentagesByStatus(s.getStationId());
				List<String[]> percentages = new ArrayList<String[]>();
				for(Float[] iter : stationPercentages){
					percentages.add(new String[] {  "{'height': '" + iter[0] + "%'}",
													"" + iter[1],
													"{'height': '" + iter[2] + "%'}",
													"" + iter[3]});
					percentages.add(new String[] {   ""+iter[0],
							"" + iter[1],
							"" + iter[2] ,
							"" + iter[3]});
				}
				s.setJobsPercentages(percentages);*/
				
				// currently for today and tomorrow; and for sched/unsched; and for 4C and 1C
				s.setJobsPercentages(jobHandler.getJobsPercentagesByStatus(s.getStationId(), "1", "A", "A"));
				
				//remove machines
				s.getMachines().clear();
				s.getPfMachineTypes().clear();
				
			}
			
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of station records for the overview schedule board page!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
			//return ImpositionResponse.serverError().build();
		}
		return Response.ok(stations).build();
	}
	
	/**
	 * Used to move up the jobs/rolls on the station so they get produced first
	 */
	@GET
	@Path("/overview/{itemId}/{stationId}/{level}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response moveUp(@PathParam("itemId") Integer itemId, @PathParam("stationId") String stationId,  @PathParam("level") String level){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			stationHandler.moveUp(itemId, stationId, level);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while changing the order of some jobs on the overview dashboard section.");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}
	
	@GET
	@Path("/load/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loadStation(@PathParam("id") String id){
		Station station = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			station = stationHandler.loadStation(id);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of station records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(station).build();
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStation(@PathParam("id") String id){
		Station station = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			station = stationHandler.read(id);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of station records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(station).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response add(@FormParam("stationId") String stationId, @FormParam("parentStationId") String parentStationId, @FormParam("stationCategoryId") String stationCategoryId,
			@FormParam("name") String name, @FormParam("description") String description, @FormParam("productionOrdering") Integer productionOrdering,
			@FormParam("inputType") String inputType, @FormParam("activeFlag") boolean activeFlag){
		
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		
		Station station = new Station();
		station.setStationId(stationId);
		station.setParentStationId(parentStationId);
		station.setStationCategoryId(stationCategoryId);
		station.setName(name);
		station.setDescription(description);
		station.setProductionOrdering(productionOrdering);
		station.setInputType(inputType);
		station.setActiveFlag(activeFlag);
		try {
			//check for duplication by id or name
			StationSearchBean ssb = new StationSearchBean();
			ssb.setStationId(stationId);
			List<Station> foundResult = stationHandler.readAll(ssb);
			if(!foundResult.isEmpty()){
				constraintViolationsMessages.put("errors", "A duplicate by the id is detected!");
			}
			foundResult.clear();
			ssb.setStationId(null);
			ssb.setNameExact(name);
			foundResult = stationHandler.readAll(ssb);
			if(!foundResult.isEmpty()){
				constraintViolationsMessages.put("errors", "A duplicate by the name is detected!");
			}
			
			if(constraintViolationsMessages.isEmpty()){
				doAddLogging(station);
				stationHandler.create(station);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while adding the station record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;	
	}
	
	public Set<Job> getStationJobs(Station station) throws PersistenceException{
		Set<Job> result = new HashSet<Job>();
		JobSearchBean jsb = new JobSearchBean();
		jsb.setStationId(station.getStationId());
		result.addAll(jobHandler.readAll(jsb));
		return result;
	}
	
	@Path("{id}")
	@DELETE
	@Transactional
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response delete(@PathParam("id") String id){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response res = null;
        try{
        	//Check if ok to delete the station
        	Station station = stationHandler.read(id);
        	if(station != null){
	        	//if(station.getJobs().isEmpty() && station.getMachines().isEmpty() && station.getDefaultStations().isEmpty()){
	        	if(getStationJobs(station).isEmpty() && station.getMachines().isEmpty()){
	        		stationHandler.delete(station);
	        		res = Response.status(200).build();
	        	}else{
	        		constraintViolationsMessages.put("errors", "Cannot delete the station as it is being related to machine or job records!");
	        		InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
					res = inputResponseError.createResponse();
	        	}
        	}else{
        		return Response.status(404).entity("Station with the id : " + id + " not present in the database").build();
	        }
        }catch (PersistenceException e) {
        	constraintViolationsMessages.put("errors", "An error occurred while deleting the station record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return res;
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response update(@FormParam("stationId") String stationId, @FormParam("parentStationId") String parentStationId, @FormParam("stationCategoryId") String stationCategoryId,
			@FormParam("name") String name, @FormParam("description") String description, @FormParam("productionOrdering") Integer productionOrdering,
			@FormParam("inputType") String inputType, @FormParam("activeFlag") boolean activeFlag){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			//check for duplication by name
			StationSearchBean ssb = new StationSearchBean();
			ssb.setNameExact(name);
			ssb.setStationIdDiff(stationId);
			List<Station> foundResult = stationHandler.readAll(ssb);
			if(!foundResult.isEmpty()){
				constraintViolationsMessages.put("errors", "A duplicate by the name is detected!");
			}
			if(constraintViolationsMessages.isEmpty()){
				Station station = stationHandler.read(stationId);
				if(station != null){
					station.setName(name);
					station.setDescription(description);
					station.setProductionOrdering(productionOrdering);
					station.setInputType(inputType);
					station.setActiveFlag(activeFlag);
					station.setParentStationId(parentStationId);
					station.setStationCategoryId(stationCategoryId);
					doEditLogging(station);
					stationHandler.update(station);
					response = Response.ok().build();
				}else{
					return Response.status(404).entity("Station with the id : " + stationId + " not present in the database").build();
				}
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e1) {
			constraintViolationsMessages.put("errors", "An error occurred while updating the station record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}

	/**
	 * @return the stationHandler
	 */
	public StationHandler getStationHandler() {
		return stationHandler;
	}

	/**
	 * @param stationHandler the stationHandler to set
	 */
	public void setStationHandler(StationHandler stationHandler) {
		this.stationHandler = stationHandler;
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
	 * @return the machineHandler
	 */
	public MachineHandler getMachineHandler() {
		return machineHandler;
	}

	/**
	 * @param machineHandler the machineHandler to set
	 */
	public void setMachineHandler(MachineHandler machineHandler) {
		this.machineHandler = machineHandler;
	}

	public RollHandler getRollHandler() {
		return rollHandler;
	}

	public void setRollHandler(RollHandler rollHandler) {
		this.rollHandler = rollHandler;
	}

	public PartHandler getPartHandler() {
		return partHandler;
	}

	public void setPartHandler(PartHandler partHandler) {
		this.partHandler = partHandler;
	}
	
}
