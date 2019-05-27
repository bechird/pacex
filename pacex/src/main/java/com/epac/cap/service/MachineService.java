package com.epac.cap.service;

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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.functionel.PrintingTimeCalculator;
import com.epac.cap.handler.JobHandler;
import com.epac.cap.handler.LogHandler;
import com.epac.cap.handler.LookupHandler;
import com.epac.cap.handler.MachineHandler;
import com.epac.cap.handler.OrderHandler;
import com.epac.cap.handler.RollHandler;
import com.epac.cap.handler.StationHandler;
import com.epac.cap.model.CoverSection;
import com.epac.cap.model.Job;
import com.epac.cap.model.JobStatus;
import com.epac.cap.model.JobType;
import com.epac.cap.model.Machine;
import com.epac.cap.model.MachineStatus;
import com.epac.cap.model.Order;
import com.epac.cap.model.Part;
import com.epac.cap.model.Priority;
import com.epac.cap.model.Roll;
import com.epac.cap.model.StationCategory;
import com.epac.cap.model.WFSLocation;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.repository.MachineSearchBean;
import com.epac.cap.repository.RollSearchBean;
import com.epac.cap.sse.beans.Event;
import com.epac.cap.sse.beans.EventTarget;
import com.epac.cap.validator.InputResponseError;


@Controller
@Path("/machines")
public class MachineService extends AbstractService{
	
	@Autowired
	private MachineHandler machineHandler;
	
	@Autowired
	private StationHandler stationHandler;
	
	@Autowired
	private RollHandler rollHandler;
	
	@Autowired
	private JobHandler jobHandler;
	
	@Autowired
	private OrderHandler orderHandler;
	
	@Autowired
	private LogHandler logHandler;
	
	@Autowired
	private LookupHandler lookupHandler;
	
	@Autowired
	private NotificationService notificationService;
	
	@Autowired
	private PrintingTimeCalculator printingTimeCalculator;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMachines(){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Machine> machines = new ArrayList<Machine>();
		try {
			machines = machineHandler.readAll();
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of machine records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
			//return ImpositionResponse.serverError().build();
		}
		return Response.ok(machines).build();
	}
	
	@GET
	@Path("/quick")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMachinesQuick(){
		
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Machine> machines = new ArrayList<Machine>();
		MachineSearchBean msb = new MachineSearchBean();
		//try {
			msb.setListing(true);
		//machines = machineHandler.readAll(msb);
		machines = machineHandler.fetchMachines();
	/*} catch (PersistenceException e) {
		constraintViolationsMessages.put("errors", "An error occurred while reading the list of machine records!");
		InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
		return inputResponseError.createResponse();
		//return ImpositionResponse.serverError().build();
	}*/
		return Response.ok(machines).build();
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMachine(@PathParam("id") String id){
		Machine machine = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			machine = machineHandler.read(id);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of machine records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(machine).build();
	}
	
	/*@GET
	@Path("/machineCount/{stationId}/{color}/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMachineWorkingCount(@PathParam("stationId") String stationId, @PathParam("color") String color){
		int machineCount = 1;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			MachineSearchBean msb = new MachineSearchBean();
			msb.setStationId(stationId);
			msb.setStatusDiff(MachineStatus.statuses.OUTSERVICE.toString());
			if(!color.equals("NA")){
				msb.setType(color);
			}
			List<Machine> machines = machineHandler.readAll(msb);
			if(machines != null && !machines.isEmpty()){
				machineCount = machines.size();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of machine records count!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(machineCount).build();
	}*/
	@GET
	@Path("/machineCount/{stationId}/{color}/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMachineWorkingCount(@PathParam("stationId") String stationId, @PathParam("color") String color){
		Integer machineCount = 1;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			machineCount = machineHandler.readCount(stationId, color);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of machine records count!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(machineCount).build();
	}
	
	@GET
	@Path("/printerSpeed/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDefaultPrinterSpeed(){
		Float result = (float) 0;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			result = machineHandler.getDefaultMachineSpeed(StationCategory.Categories.PRESS.toString());
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the printer speed value!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(result).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response add(Machine machine){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			//check for duplication by id or name
			MachineSearchBean msb = new MachineSearchBean();
			msb.setMachineId(machine.getMachineId());
			List<Machine> foundResult = machineHandler.readAll(msb);
			if(!foundResult.isEmpty()){
				constraintViolationsMessages.put("errors", "A duplicate by the id is detected!");
			}
			foundResult.clear();
			msb.setMachineId(null);
			msb.setNameExact(machine.getName());
			foundResult = machineHandler.readAll(msb);
			if(!foundResult.isEmpty()){
				constraintViolationsMessages.put("errors", "A duplicate by the name is detected!");
			}
			
			if(constraintViolationsMessages.isEmpty()){
				doAddLogging(machine);
				if(StringUtils.isEmpty(machine.getStatus().getId())){
					//nullify the status otherwise hibernate will ask to persist the empty transient status bean
					machine.setStatus(null);
				}
				machineHandler.create(machine);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while adding the machine record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;	
	}
	
	@POST
	@Path("/toAssign")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response assignToMachine(String[] selectedRollsForAssignment){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<String> rollsNotAssigned = new ArrayList<String>();
		Response response = null;
		try {
			String executingUserId = this.getExecutingUserId();// TODO make sure this works
			if(constraintViolationsMessages.isEmpty()){
				if(selectedRollsForAssignment != null && selectedRollsForAssignment.length > 0){
					rollsNotAssigned = machineHandler.assignToMachine(selectedRollsForAssignment, executingUserId);
				}
				response = Response.ok(rollsNotAssigned).build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while assigning rolls to the machine!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}
	
	@GET
	@Path("/rasterLocation/{pressJobId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getRasterLocation(@PathParam("jobId") Integer pressJobId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			if(constraintViolationsMessages.isEmpty()){
				Job job = jobHandler.read(pressJobId);
				WFSLocation loc = null;
				if(job != null){
					if(job.getRollId() != null){
						Roll roll = rollHandler.read(job.getRollId());
						String sheetSize = printingTimeCalculator.getBestSheetHeight(roll, job,
								machineHandler.getBsValue(), machineHandler.getUseOptimizedSheetAlgo());
						loc = machineHandler.getRasterLocation(job, "EM", sheetSize);
					}else{
						
					}
				}
				response = Response.ok(loc).build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while retrieving the raster files location!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}
	
	@POST
	@Path("/sectionsToAssign")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response assignBatchesToMachine(String[] selectedBatchesForAssignment){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			List<String> sectionsNotAssigned = new ArrayList<String>();
			String executingUserId = this.getExecutingUserId();
			if(constraintViolationsMessages.isEmpty()){
				if(selectedBatchesForAssignment != null && selectedBatchesForAssignment.length > 0){
					sectionsNotAssigned = machineHandler.assignSectionsToMachine(selectedBatchesForAssignment, executingUserId);
				}
				response = Response.ok(sectionsNotAssigned).build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while assigning batches to the machine!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}

	@POST
	@Path("/jobsToAssign")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response assignJobsToMachine(String[] selectedJobsForAssignment){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			String executingUserId = this.getExecutingUserId();// TODO make sure this works
			if(constraintViolationsMessages.isEmpty()){
				if(selectedJobsForAssignment != null && selectedJobsForAssignment.length > 0){
					machineHandler.assignJobsToMachine(selectedJobsForAssignment, executingUserId);
				}
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while assigning jobs to the machine!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}
	
	@POST
	@Path("/startResume/{selectedModeOption}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response startResumeMachine(String machineId, @PathParam("selectedModeOption") String selectedModeOption){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			if(constraintViolationsMessages.isEmpty()){
				String result = machineHandler.startResumeMachine(machineId, selectedModeOption, this.getExecutingUserId());
				if(result == null){
					response = Response.ok().build();
				}else{
					constraintViolationsMessages.put("errors", result);
					InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
					response = inputResponseError.createResponse();
				}
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while starting/resuming the machine!");
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
        	Machine machine = machineHandler.read(id);
        	if(machine != null){
        		//see if ok to delete the machine
        		JobSearchBean jsb = new JobSearchBean();
        		jsb.setMachineId(id);
        		RollSearchBean rsb = new RollSearchBean();
        		rsb.setMachineId(id);
        		if(machine.getCurrentJob() == null && jobHandler.readAll(jsb).isEmpty() && machine.getLogs().isEmpty() && rollHandler.readAll(rsb).isEmpty()){
	        		machineHandler.delete(machine);
	            	res = Response.status(200).build();
        		}else{
        			constraintViolationsMessages.put("errors", "Cannot delete the machine as it has jobs, rolls or logs associated to it!");
            		InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
    				res = inputResponseError.createResponse();
        		}
        	}else{
        		return Response.status(404).entity("Machine with the id : " + id + " not present in the database").build();
        	}
        }catch (PersistenceException e) {
        	constraintViolationsMessages.put("errors", "An error occurred while deleting the machine record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return res;
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(Machine machine){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			//check duplication by name
			MachineSearchBean msb = new MachineSearchBean();
			msb.setNameExact(machine.getName());
			msb.setMachineIdDiff(machine.getMachineId());
			List<Machine> foundResult = machineHandler.readAll(msb);
			if(!foundResult.isEmpty()){
				constraintViolationsMessages.put("errors", "A duplicate by the name is detected!");
			}
			if(constraintViolationsMessages.isEmpty()){
				doEditLogging(machine);
				if(StringUtils.isEmpty(machine.getStatus().getId())){
					//nullify the status otherwise hibernate will ask to persist the empty transient status bean
					machine.setStatus(null);
				}
				machineHandler.update(machine);
				Map<String, Object> ssePayload=new HashMap<String, Object>();
				ssePayload.put("machineId", machine.getMachineId());
				ssePayload.put("stationId", machine.getStationId());
				Event event=new Event(EventTarget.MachineStatus, false, null, ssePayload);
				notificationService.broadcast(event);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while updating the machine record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
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
	 * @return the orderHandler
	 */
	public OrderHandler getOrderHandler() {
		return orderHandler;
	}

	/**
	 * @param orderHandler the orderHandler to set
	 */
	public void setOrderHandler(OrderHandler orderHandler) {
		this.orderHandler = orderHandler;
	}

	
	
	@GET
	@Path("/currentJob/{orderId}/{machineId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateCurrentJobOfMachine(@PathParam("orderId")int orderId,@PathParam("machineId")String machineId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			JobSearchBean searchbean = new JobSearchBean();
			searchbean.setStationId("SHIPPING");
			searchbean.setOrderId(orderId);
			List<Job> jobs = jobHandler.readAll(searchbean);
			Job job = null;
			if(jobs.size() > 0){
			 job = jobs.get(0);
			 }else {
				job = new Job();
				job.setCreatedDate(new Date());
				job.setCreatorId(this.getExecutingUserId());
				job.setMachineOrdering(1);
				job.setProductionOrdering(10);
				job.setSplitLevel(0);
				job.setJobPriority(lookupHandler.read(Priority.Priorities.NORMAL.getName(), Priority.class));
				job.setJobType(lookupHandler.read(JobType.JobTypes.BINDING.getName(), JobType.class));
				job.setStationId("SHIPPING");
				Order order = orderHandler.read(orderId);
				Part part = order.getOrderPart().getPart();
				job.setOrderId(orderId);
				job.setPartNum(part.getPartNum());
				
				job.setDueDate(order.getDueDate());
				job.setPartColor(part.getColors());
				job.setPartPaperId(part.getPaperType().getId());
				job.setProductionMode(order.getProductionMode());
				job.setPartTitle(part.getTitle());
				job.setPartIsbn(part.getIsbn());
				job.setPartCategory(part.getCategory().getId());
				
				job.setMachineId(machineId);
				job.setJobStatus(lookupHandler.read("New", JobStatus.class));
				jobHandler.create(job);
			 }
			
			Machine machine = machineHandler.read(machineId);
			machine.setCurrentJob(job);
			machineHandler.update(machine);
			
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while assigning jobs to the machine!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}
	@GET
	@Path("/SHIPPING")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchALLMachineShipping(){

		List<Machine> machines = machineHandler.fetchMachines();

		return Response.ok(machines).build();

	}


}
