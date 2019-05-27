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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.OrderBy;
import com.epac.cap.common.PersistenceException;
import com.epac.cap.functionel.PrintingTimeCalculator;
import com.epac.cap.handler.JobHandler;
import com.epac.cap.handler.JobsByMachineOrderingComparator;
import com.epac.cap.handler.MachineHandler;
import com.epac.cap.handler.PartHandler;
import com.epac.cap.handler.PrintersHandler;
import com.epac.cap.handler.RollHandler;
import com.epac.cap.handler.RollsByMachineOrderingComparator;
import com.epac.cap.handler.StationHandler;
import com.epac.cap.model.Job;
import com.epac.cap.model.JobStatus;
import com.epac.cap.model.Lamination;
import com.epac.cap.model.Machine;
import com.epac.cap.model.Part;
import com.epac.cap.model.PartCritiria;
import com.epac.cap.model.Roll;
import com.epac.cap.model.Station;
import com.epac.cap.model.StationCategory;
import com.epac.cap.model.WFSDataSupport;
import com.epac.cap.model.WFSLocation;
import com.epac.cap.model.WFSPartWorkflow;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.repository.PartDAO;
import com.epac.cap.repository.StationSearchBean;
import com.epac.cap.validator.InputResponseError;
import com.epac.cap.utils.LogUtils;

import jp.co.fujifilm.xmf.oc.EventListener;
import jp.co.fujifilm.xmf.oc.Printer;
import jp.co.fujifilm.xmf.oc.model.Event;
import jp.co.fujifilm.xmf.oc.model.Event.EventType;
import jp.co.fujifilm.xmf.oc.model.printing.PrintingJob;

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
	
	@Autowired
	private PartDAO partDAO;
	
	@Autowired
	private LookupDAO lookupDAO;
	
	@Autowired
	private PrintersHandler printers;
	
	@Autowired
	private PrintingTimeCalculator printingTimeCalculator;
	
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
			station = stationHandler.read(id);
			// For the press station, prepare printers for the EPAC mode functioning
			// first see which ones work for Emf mode, in which case disregard those printers
			// then ping each printer to check for epac mode
			// finally initiate sse connection on each one to watch epac mode status for each printer
			if(StationCategory.Categories.PRESS.toString().equals(id) && station != null){
				for(Machine m : station.getMachines()){
					if(Boolean.TRUE.equals(m.getIsEmfMode())){
						m.setIsEpacModePrintingActive(Boolean.FALSE);
					}else{
						try{
							Printer printer = printers.getPrinter(m.getMachineId());
							if(StringUtils.isBlank(m.getIpAddress())){
								m.setIsEpacModePrintingActive(Boolean.FALSE);
								if(printer != null){
									printers.shutdownPrinter(m.getMachineId());
								}
								continue;
							}else{
								if(printer == null){
									m.setIsEpacModePrintingActive(Boolean.TRUE);
								}else{
									m.setIsEpacModePrintingActive(printer.isEpacMode());
								}
							}
							//we need the raster names to be able to localize the jobs running and update their printing status on the front end (in the loadstation method on front end)
							Roll rop = m.getRollOnProd();
							if(rop != null){
								for(Job job : rop.getJobs()){
									Part pr = partHandler.read(job.getPartNum());
									List<String> rasters = new ArrayList<String>();
									if(pr != null){
										for(WFSDataSupport ds : pr.getDataSupportsOnProd()){
											if(ds.getName().equals(WFSDataSupport.NAME_RASTER)){
												WFSLocation location = ds.getLocationdByType(WFSLocation.DESTINATION);
												if(location != null){
													rasters.add(location.getFileName());
												}
											}
										}
									}
									job.setRasterNames(rasters);
								}
							}
							if(printer == null){
								printer = new Printer("http://".concat(m.getIpAddress().concat(m.getNetPort() != null ? ":".concat(Integer.toString(m.getNetPort())): "")), m.getMachineId());
								printer.addListener(new EventListener() {
									@Override
									public void handleEvent(Event event) {
										
										
										LogUtils.debug("Event received from OutputController ["+m.getMachineId()+"]: "+event.getType());
										if(event.getType() == EventType.PRINTING) {
											Roll roll = null;
											try{
												roll = rollHandler.getOnProdRollByMachine(m.getMachineId());
											}catch(Exception e){
												LogUtils.error("Error occured while reading machine instance from DB: "+m.getMachineId());
											}
											
											if(roll == null){
												LogUtils.debug("No roll on prod on the machine ["+m.getMachineId()+"], event ignored");
												return;
											}
										
											Set<Job> jobs = roll.getJobs();
								
											List list = (List) event.getObject();
											for (Object object : list) {
												
												if(object instanceof PrintingJob) {
													PrintingJob printingJob = (PrintingJob)object;
													for (Job job : jobs) {
														if(JobStatus.JobStatuses.COMPLETE.getName().equals(job.getJobStatus().getId())){
															continue;
														}
														
														String jobName = job.getJobName();
														if(StringUtils.isBlank(jobName)){
															WFSLocation location = null;
															try {
																location = machineHandler.getRasterLocation(job, "EM", printingTimeCalculator.getBestSheetHeight(roll));
																if(location != null){
																	jobName = String.valueOf(job.getJobId()).concat(location.getFileName());
																}
															} catch (PersistenceException e1) {
																LogUtils.error("Error occured while trying to get job name from hunkeler station");
															}
														}
														if(StringUtils.isBlank(jobName) || !jobName.equals(printingJob.getId())){
															continue;
														}
														
														Double produced = 0.0;
														if(printingJob.getPrintedSheetCount() != null){
															produced = Math.floor(printingJob.getPrintedSheetCount()/printingJob.getJob().getNumSheets());
														}
																
														// update produced quantity
														job.setQuantityProduced(produced.intValue());
														
														if("COMPLETED".equals(printingJob.getStatus())) {
															LogUtils.debug(m.getMachineId()+": Update job #"+job.getJobId()+" produced="+produced+" and status=COMPLETE");
															// set status to complete
															job.setQuantityProduced(job.getQuantityNeeded());
															job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.COMPLETE.getName(), JobStatus.class));
														}else if ("FAILED".equals(printingJob.getStatus()) && job.getQuantityProduced() > 0) {
															LogUtils.debug(m.getMachineId()+": Update job #"+job.getJobId()+" produced="+produced+" and status=FAILED");
															// set status to partial complete
															job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.COMPLETE_PARTIAL.getName(), JobStatus.class));
														}else if ("RUNNING".equals(printingJob.getStatus())) {
															LogUtils.debug(m.getMachineId()+": Update job #"+job.getJobId()+" produced="+produced+" and status=RUNNING");
															// set status to running
															job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.RUNNING.getName(), JobStatus.class));
														}
														try {
															jobHandler.update(job);
														} catch (PersistenceException e) {
															LogUtils.error("Error occured while updating status for job #"+job.getJobId());
														}
														
														LogUtils.debug(m.getMachineId()+": Job "+job.getJobId()+" updated: [status = "+job.getJobStatus().getName()+", produced: "+job.getQuantityProduced()+"]");
														
													}
	
												}else {
													LogUtils.error(m.getMachineId()+": Event object is not a PrintingJob instance, ignored");
												}
											}
										}
									}
								});
								printers.addPrinter(m.getMachineId(), printer);
							}
						}catch(Exception e){
							LogUtils.error("", e);
							m.setIsEpacModePrintingActive(Boolean.FALSE);
							machineHandler.update(m);
						}
					}
				}
				
			}
			// spine and head/tail: needed for display on the front end when case bound station
			if(StationCategory.Categories.CASEBOUND.toString().equals(id) && station != null){
				for(Machine m : station.getMachines()){
					Job job = m.getJobOnProd();
					if(job != null){
						Part pr = partHandler.read(job.getPartNum());
						if(pr != null){
							job.setSpineType(pr.getSpineType());
							job.setHeadTailBands(pr.getHeadTailBands());
						}
					}
				}
			}
			// wire color: needed for display on the front end when coil bind station
			if(StationCategory.Categories.PLASTICOIL.toString().equals(id) && station != null){
				for(Machine m : station.getMachines()){
					Job job = m.getJobOnProd();
					if(job != null){
						Part pr = partHandler.read(job.getPartNum());
						if(pr != null){
							job.setWireColor(pr.getWireColor());
						}
					}
				}
			}
			// pages count: needed for display on the front end for cover press station
			if(StationCategory.Categories.COVERPRESS.toString().equals(id) && station != null && !Station.inputTypes.Batch.toString().equalsIgnoreCase(station.getInputType())){
				for(Machine m : station.getMachines()){
					for(Job job : m.getRunningAndAssignedJobs()){
							Part pr = partHandler.read(job.getPartNum());
							if(pr != null){
								job.setPartPagesCount(pr.getPagesCount());
							}
					}
				}
			}
			if(StationCategory.Categories.PLOWFOLDER.toString().equals(id) && station != null){
				for(Machine m : station.getMachines()){
					Roll rop = m.getRollOnProd();
					if(rop != null){
						for(Job job : rop.getJobs()){
							Part pr = partHandler.read(job.getPartNum());
							if(pr != null){
								job.setPartPagesCount(pr.getPagesCount());
							}
						}
					}
				}
			}
			// prepare the cart numbers to show on the stations
			if(station != null && !StationCategory.Categories.PRESS.toString().equals(id) && 
					!StationCategory.Categories.PLOWFOLDER.toString().equals(id) && !StationCategory.Categories.COVERPRESS.toString().equals(id)){
				for(Machine m : station.getMachines()){
					for(Job job : m.getAssignedJobs()){
							job.setPrevJobData(jobHandler.findPrevJobData(job.getJobId()));
					}
				}
			}
			//set the lamination when cover or lam station
			if(station != null && (StationCategory.Categories.COVERPRESS.toString().equals(id) || StationCategory.Categories.LAMINATION.toString().equals(id))){
				for(Machine m : station.getMachines()){
					for(Job job : m.getRunningAndAssignedJobs()){
						String laminationId = partDAO.findLamination(job.getPartNum());
						Part pr = partDAO.read(job.getPartNum().endsWith("C") ? job.getPartNum().replace("C", "") : job.getPartNum());
						if(pr != null && pr.getSpotVarnish()){
							job.setPartLamination(new Lamination(laminationId + "/SV"));
						}else{
							job.setPartLamination(new Lamination(laminationId));
						}
					}
				}
			}
			
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
