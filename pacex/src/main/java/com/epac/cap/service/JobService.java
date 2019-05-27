package com.epac.cap.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.OrderBy;
import com.epac.cap.common.PersistenceException;
import com.epac.cap.handler.JobHandler;
import com.epac.cap.handler.JobSchedulingComparator;
import com.epac.cap.handler.LookupHandler;
import com.epac.cap.handler.MachineHandler;
import com.epac.cap.handler.PartHandler;
import com.epac.cap.handler.StationHandler;
import com.epac.cap.model.Job;
import com.epac.cap.model.JobPrevious;
import com.epac.cap.model.JobStatus;
import com.epac.cap.model.Lamination;
import com.epac.cap.model.Log;
import com.epac.cap.model.Machine;
import com.epac.cap.model.Order;
import com.epac.cap.model.Part;
import com.epac.cap.model.Station;
import com.epac.cap.model.StationCategory;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.repository.MachineSearchBean;
import com.epac.cap.repository.OrderDAO;
import com.epac.cap.repository.PartDAO;
import com.epac.cap.repository.StationDAO;
import com.epac.cap.utils.LogUtils;
import com.epac.cap.utils.PaginatedResult;
import com.epac.cap.validator.InputResponseError;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@Path("/jobs")
public class JobService extends AbstractService{

	@Autowired
	private JobHandler jobHandler;
	
	@Autowired
	private PartHandler partHandler;
	
	@Autowired
	private StationHandler stationHandler;
	
	@Autowired
	private MachineHandler machineHandler;
	
	@Autowired
	private LookupHandler lookupHandler;
	
	@Autowired
	private OrderDAO orderDAO;
	
	@Autowired
	private PartDAO partDAO;
	
	@Autowired
	private StationDAO stationDAO;
	
	private static Logger logger = Logger.getLogger(JobService.class);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJobs(){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Job> jobs = new ArrayList<Job>();
		try {
			jobs = jobHandler.readAll();
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of job records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(jobs).build();
	}

	@GET
	@Path("/idsList")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJobsIdsList(){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Integer> ids = new ArrayList<Integer>();
		try {
			ids = jobHandler.getIdsList();
			logger.debug("jobs ids:" + ids.size());
			for(Integer id : ids) {
				if(id == null)logger.debug("there is null");
			}
		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of job records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(ids).build();
	}
	
	@POST
	@Path("/paginated")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPaginatedJobs(String data){
		
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		PaginatedResult paginatedResult = new PaginatedResult();
		List<Job> pageList = new ArrayList<Job>();
		
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

			JobSearchBean searchBean = new JobSearchBean();
			searchBean.setResultOffset(start);
			searchBean.setMaxResults(pageLength);
			searchBean.setListing(true);
			
			//Need to show the station name:
			List<Object[]> stationsName = stationHandler.getNameOfSations();
			logger.debug("list:" + stationsName.get(0)[0].toString());
			//sorting
			int sortingColumn = aoData.get(2).get("value").get(0).get("column").asInt();
			
			String sortingColumnName = aoData.get(1).get("value").get(sortingColumn).get("data").asText();
			
			String sortingDirection = aoData.get(2).get("value").get(0).get("dir").asText();
			
			OrderBy orderBy = new OrderBy();			
			if(sortingDirection.equals("asc"))orderBy.setDirection(OrderBy.ASC);
			if(sortingDirection.equals("desc"))orderBy.setDirection(OrderBy.DESC);
			
			if(sortingColumnName.equals("jobId"))orderBy.setName("jobId");
			if(sortingColumnName.equals("orderId"))orderBy.setName("orderId");
			if(sortingColumnName.equals("partNum"))orderBy.setName("partNum");
			if(sortingColumnName.equals("rollId"))orderBy.setName("rollId");
			if(sortingColumnName.equals("jobStatus.id"))orderBy.setName("jobStatus.id");
			if(sortingColumnName.equals("stationId"))orderBy.setName("stationId");
			if(sortingColumnName.equals("splitLevel"))orderBy.setName("splitLevel");
			if(sortingColumnName.equals("hours"))orderBy.setName("hours");			
			if(sortingColumnName.equals("quantityNeeded"))orderBy.setName("quantityNeeded");
			if(sortingColumnName.equals("quantityProduced"))orderBy.setName("quantityProduced");
			
			
			searchBean.getOrderByList().clear();
			searchBean.addOrderBy(orderBy);
			//-----------------------------------------------------------------------------------------------------

			//filtering
			boolean filtering = false;
			
			String filter = aoData.get(1).get("value").get(0).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setSearchJobIdPart(filter);
					filtering = true;
				} catch (Exception e) {}
			}

			filter = aoData.get(1).get("value").get(1).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setSearchOrderId(filter);
					filtering = true;
				} catch (Exception e) {}
			}

			filter = aoData.get(1).get("value").get(2).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				searchBean.setSearchPartNum(filter);
				filtering = true;
			}
			
			filter = aoData.get(1).get("value").get(3).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setSearchRollId(filter);
					filtering = true;
				} catch (Exception e) {}
			}
			
			filter = aoData.get(1).get("value").get(4).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				searchBean.setSearchStatus(filter);
				filtering = true;
			}
			
			filter = aoData.get(1).get("value").get(5).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				
				searchBean.setSearchStationId("0");				
				for(Object[] s : stationsName){
					if(s[0].toString().trim().toLowerCase().indexOf(filter.trim().toLowerCase())  > -1){
						searchBean.setSearchStationId(s[1].toString());
						break;
					}
				}
				
				
				filtering = true;
			}
			
			filter = aoData.get(1).get("value").get(6).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setSearchSplitLevel(filter);
					filtering = true;
				} catch (Exception e) {}
			}
			
			filter = aoData.get(1).get("value").get(7).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setSearchHours(filter);
					filtering = true;
				} catch (Exception e) {}
			}

			filter = aoData.get(1).get("value").get(8).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setSearchQuantityNeeded(filter);
					filtering = true;
				} catch (Exception e) {}
			}
			
			filter = aoData.get(1).get("value").get(9).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setSearchQuantityProduced(filter);
					filtering = true;
				} catch (Exception e) {}
			}
			//-----------------------------------------------------------------------------------------------------
			
			
			//search word
			String wordToSearch = aoData.get(5).get("value").get("value").asText();
			
			if(wordToSearch != null && !wordToSearch.isEmpty()) {
				
				//logger.debug("wordToSearch:" + wordToSearch);
				pageList = jobHandler.fullSearch(wordToSearch, null, null);
				count = pageList.size();				
				pageList = jobHandler.fullSearch(wordToSearch, pageLength, start);
				
				
			}else if(filtering){
				
				searchBean.setResultOffset(null);
				searchBean.setMaxResults(null);
				pageList = jobHandler.readAll(searchBean);
				count = pageList.size();
				
				searchBean.setResultOffset(start);
				searchBean.setMaxResults(pageLength);
				pageList = jobHandler.readAll(searchBean);
				
			}else {
				count = jobHandler.getCount();				
				pageList = jobHandler.readAll(searchBean);
			}
			
			//-----------------------------------------------------------------------------------------------------

			//logger.debug("list:" + pageList.size());
			//logger.debug("global count:" + count);
			
			
			for(Job j : pageList){
				for(Object[] s : stationsName){
					if(s[1].toString().equals(j.getStationId())){
						j.setStationId(s[0].toString());
						break;
					}
				}
			}
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
	@Path("/orderJobs/{orderId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrderJobs(@PathParam("orderId") Integer orderId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Set<Job> jobs = null;
		try {
			jobs = jobHandler.readOrderJobs(orderId);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the order jobs records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(jobs).build();
	}
	
	
	/**
	 * Used on the scheduling page to retrieve the jobs to schedule and assign to rolls
	 * @return
	 */
	@GET
	@Path("/availableForScheduling/{color}/{paperType}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAvailableJobsForScheduling(@PathParam("color") String color, @PathParam("paperType") String paperType){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Job> jobs = new ArrayList<Job>();
		try {
			jobs = jobHandler.getAvailableJobsForScheduling(color, paperType);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of job records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(jobs).build();
	}
	
	/**
	 * Used on the overview scheduling board page to retrieve the jobs running on each station
	 * @return
	 */
	@GET
	@Path("/stationJobs/{stationId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStationJobs(@PathParam("stationId") String stationId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Job> jobs = new ArrayList<Job>();
		try {
			jobs = jobHandler.getStationJobs(stationId);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of job records for the station " + stationId);
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(jobs).build();
	}
	
	@GET
	@Path("/scheduled/{stationId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScheduledJobs(@PathParam("stationId") String stationId) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Job> jobs = new ArrayList<Job>();
		try {

			if (!Station.inputTypes.Batch.toString().equals(getStationDAO().fetchInputType(stationId))) {
				JobSearchBean jsb = new JobSearchBean();
				jsb.setStationId(stationId);
				jsb.setStatus(JobStatus.JobStatuses.SCHEDULED.toString());

				jobs = jobHandler.readAll(jsb);
				// needed for cover press: add the lamination
				if(StationCategory.Categories.COVERPRESS.toString().equals(stationId) || StationCategory.Categories.LAMINATION.toString().equals(stationId)){
					for(Job j : jobs){
						String laminationId = partDAO.findLamination(j.getPartNum());
						Part pr = partDAO.read(j.getPartNum().endsWith("C") ? j.getPartNum().replace("C", "") : j.getPartNum());
						if(pr != null && pr.getSpotVarnish()){
							j.setPartLamination(new Lamination(laminationId + "/<font title = 'Spot Varnish' color='red'><b>SV<b/></font>"));
						}else{
							j.setPartLamination(new Lamination(laminationId));
						}
					}
				}
				if(!StationCategory.Categories.COVERPRESS.toString().equals(stationId)){
					for(Job j : jobs){
						j.setPrevJobData(jobHandler.findPrevJobData(j.getJobId()));
					}
				}
				Collections.sort(jobs, new JobSchedulingComparator());
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors",
					"An error occurred while reading the list of scheduled job records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(jobs).build();
	}
	
	@GET
	@Path("/coverManagement")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCoverManagementobs(){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Job> jobs = new ArrayList<Job>();
		try {
			JobSearchBean jsb = new JobSearchBean();
			jsb.setStationId(StationCategory.Categories.COVERPRESS.toString());
			jsb.setStatusesIn(Arrays.asList(JobStatus.JobStatuses.SCHEDULED.toString(),JobStatus.JobStatuses.RUNNING.toString(),
					JobStatus.JobStatuses.ASSIGNED.toString()));
			
			jobs = jobHandler.readAll(jsb);
			Collections.sort(jobs, new JobSchedulingComparator());
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of scheduled job records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(jobs).build();
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJob(@PathParam("id") Integer id){
		Job job = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			job = jobHandler.read(id);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the job record with id: " + id);
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(job).build();
	}
	
	@GET
	@Path("/withLam/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJobWithLam(@PathParam("id") Integer id){
		Job job = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			job = jobHandler.read(id);
			if(job != null){
				String laminationId = partDAO.findLamination(job.getPartNum());
				job.setPartLamination(new Lamination(laminationId));
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the job record with id: " + id);
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(job).build();
	}
	
	@GET
	@Path("/calculateJobHoursAndLength/{partNum}/{qty}/{rollWidth}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response calculateJobHoursAndLength(@PathParam("partNum") String partNum, @PathParam("qty") Integer qty,
			@PathParam("rollWidth") Float rollWidth){
		Part part = null;
		Float[] hoursAndlength;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			part = partHandler.read(partNum);
			hoursAndlength = jobHandler.calculateJobHoursAndLength(part, qty, rollWidth);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while calculating hours and length for part: " + partNum);
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(hoursAndlength).build();
	}
	
	/**
	 * @param colors ('4C', '1C' or 'A' for all), day (a string that can have values: 'T0' for today; 'Ti' where i in 1..n, or 'A' for all days)
	 * hoursType could be 'S' for scheduled, 'U' for unscheduled or 'A' for both
	 * @return
	 */
	@GET
	@Path("/calculatePressHours/{colors}/{day}/{hoursType}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response calculatePressHours(@PathParam("colors") String colors, @PathParam("day") String theDay, @PathParam("hoursType") String hoursType){
		Float result = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			result = jobHandler.calculatePressHours(colors, theDay, hoursType);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while calculating Printing Hours");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(result).build();
	}
	
	/**
	 * @param colors ('4C', '1C', 'A' for all), daysNeeded (an integer that tells the days we want to get the unscheduled hours calculated for)
	 * @param cumulFlag, tells whether we should cumulate the calculated hours or not
	 * @param hoursType tells what hours to include: 'S' for scheduled, 'U' for unscheduled, and 'A' for both
	 * @return an array of floats representing the unscheduled hours for the days needed
	 */
	@GET
	@Path("/getPressStationHours/{colors}/{dayNeeded}/{cumulFlag}/{hoursType}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPressStationHours(@PathParam("colors") String colors, @PathParam("dayNeeded") String dayNeeded, @PathParam("cumulFlag") String cumulFlag, @PathParam("hoursType") String hoursType){
		List<Float> result = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			result = jobHandler.getPressStationHours(colors, dayNeeded, cumulFlag, hoursType);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while calculating Press Station Work Hours for the days and for the hours type needed.");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(result).build();
	}
	
	@GET
	@Path("/calculateStationHours/{stationId}/{hoursType}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response calculateStationHours(@PathParam("stationId") String stationId, @PathParam("hoursType") String hoursType){
		Float result = (float) 0;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			result = jobHandler.calculateStationHours(stationId, hoursType);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while calculating work hours for station " + stationId);
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(result).build();
	}
	
	@GET
	@Path("/getStationHours/{stationId}/{dayNeeded}/{hoursType}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStationHours(@PathParam("stationId") String stationId, @PathParam("dayNeeded") String dayNeeded, @PathParam("hoursType") String hoursType){
		List<Float> result = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			result = jobHandler.getStationHours(stationId, dayNeeded, hoursType);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while calculating work Hours for the first " + dayNeeded + " day(s) for station " + stationId);
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(result).build();
	}
	
	/**
	 * @param daysNeeded (an integer that tells the days we want to get the capacity hours calculated for)
	 * @return an array of floats representing the capacity hours for the days needed; capacity should be the total work hours for the day
	 * Also it depends on the number of machines on the station, so multiply by the number of machines which is retrieved using the colors to know
	 * whether it is the 4C or 1C and look for how many machines in each type
	 */
	@GET
	@Path("/getCapacityHours/{colors}/{dayNeeded}/{cumulFlag}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCapacityHours(@PathParam("colors") String colors, @PathParam("dayNeeded") String dayNeeded, @PathParam("cumulFlag") String cumulFlag){
		List<Float> result = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			result = jobHandler.getCapacityHours(colors, dayNeeded, cumulFlag);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while calculating the Capacity Hours for the days needed.");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(result).build();
	}
	
	/**
	 * @return an array of percentages representing the work shift hours during the day
	 * The first value in the array represents work portion time, the next is break time, the next is work, next is break, ... and so on until end of the array.
	 */
	@GET
	@Path("/getDayShifts")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDayShifts(){
		List<Float[]> result = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try{
			result = jobHandler.getDayShifts(new Date());
		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while calculating the percentages representing the work shift hours during the day");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(result).build();
	}
	
	/**
	 * @return an array of percentages representing the work shift hours during the day + percentages of jobs by status
	 * The result is a map of the percentage work and the type of job by status 
	 * The values are for the current day and the next day; so the first two values are for today, the other two are for tomorrow
	 * Used to draw the vertical bar charts next to each station frame on the overview dash-board screen
	 */
	@GET
	@Path("/getJobsPercentages/{stationId}/{dayNeeded}/{hoursType}/{colors}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJobsPercentagesByStatus(@PathParam("stationId") String stationId, @PathParam("dayNeeded") String dayNeeded, @PathParam("hoursType") String hoursType, @PathParam("colors") String colors){
		List<List<Float[]>> result = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try{
			result = jobHandler.getJobsPercentagesByStatus(stationId, dayNeeded, hoursType, colors);
		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while calculating the percentages representing the work shift hours during the day + percentages of jobs by status.");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(result).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response add(Job job){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			
			if(constraintViolationsMessages.isEmpty()){
				doAddLogging(job);
				jobHandler.create(job);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while adding the job record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;	
	}
	
	@Path("{id}")
	@DELETE
	@Transactional
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response delete(@PathParam("id") Integer id){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response res = null;
        try{
        	Job job = jobHandler.read(id);
        	if(job != null){
        		//see if ok to delete the job (when no load tags exist for the job)
        		if(job.getLoadTags().isEmpty()){
	        		jobHandler.delete(job);
	            	res = Response.status(200).build();
        		}else{
        			constraintViolationsMessages.put("errors", "Cannot delete the job as it has load tags assigned to it!");
            		InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
    				res = inputResponseError.createResponse();
        		}
        	}else{
        		return Response.status(404).entity("Job with id : " + id + " not present in the database").build();
        	}
        }catch (PersistenceException e) {
        	constraintViolationsMessages.put("errors", "An error occurred while deleting the job record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return res;
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(Job job){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			
			if(constraintViolationsMessages.isEmpty()){
				doEditLogging(job);
				jobHandler.update(job);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while updating the job record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}
	
	@GET
	@Path("/splitJobs/{jobId}/{newQuantity}/{cascadeFlag}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response splitJobs(@PathParam("jobId") Integer jobId, @PathParam("newQuantity") Integer newQuantity, @PathParam("cascadeFlag") String cascadeFlag){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			Job job = this.jobHandler.read(jobId);
			if(job != null){
				this.jobHandler.splitJobs(job, newQuantity, (float) 0, cascadeFlag,  this.getExecutingUserId());
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while splitting jobs for jobId " + jobId);
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok().build();
	}
	
	@GET
	@Path("/unassign/{jobId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response unassignJob(@PathParam("jobId") Integer jobId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			String executingUserId = this.getExecutingUserId();// TODO make sure this works
			if(constraintViolationsMessages.isEmpty()){
				if(jobId != null){
					jobHandler.unassignJob(jobId, executingUserId);
				}
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while un-assigning a job from the machine!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}
	
	@GET
	@Path("/findNext/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findNextStationJob(@PathParam("id") Integer id){
		Job job = null;
		Job jobResult = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			job = jobHandler.read(id);
			if(job != null){
				List<Job> jobs = jobHandler.findNextJobs(job, false);
				if(!jobs.isEmpty()){
					jobResult = jobs.get(0);
					jobResult.setStationId(stationHandler.readStationName(jobs.get(0).getStationId()));//we need to display the station name
				}
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the job record with id: " + id);
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(jobResult).build();
	}

	@GET
	@Path("/findPrevJobData/{jobId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findPrevJobData(@PathParam("jobId") Integer currentJobId){
		JobPrevious result = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			result = jobHandler.findPrevJobData(currentJobId);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while gathering the data about cover and text for the binder station job with id: " + currentJobId);
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(result).build();
	}
	
	@GET
	@Path("/orderProducedQuantity/{orderId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrderProducedQuantity(@PathParam("orderId") Integer orderId){
		Float result = (float) 0;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			result = jobHandler.getOrderProducedQuantity(orderId);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while calculating produced quantity for order with id: " + orderId);
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(result).build();
	}
	
	@GET
	@Path("/partProducedQuantity/{orderId}/{partNum}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPartProducedQuantity(@PathParam("orderId") Integer orderId,
			@PathParam("partNum") String partNum) {
		Float result = (float) 0;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			result = jobHandler.getOrderProducedQuantityByPart(orderId, partNum);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors",
					"An error occurred while calculating produced quantity for order with id: " + orderId
							+ " and part Number:" + partNum);
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(result).build();
	}
	
	@POST
	@Path("/lorl")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response calculateLeftOverRollLength(Log log){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		Integer rollLength = null;
		try {
			if(constraintViolationsMessages.isEmpty()){
				rollLength = jobHandler.calculateLeftOverRollLength(log);
				response = Response.ok(rollLength).build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while calculating the left over roll length!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;	
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
	 * @return the partHandler
	 */
	public PartHandler getPartHandler() {
		return partHandler;
	}

	/**
	 * @param partHandler the partHandler to set
	 */
	public void setPartHandler(PartHandler partHandler) {
		this.partHandler = partHandler;
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
	 * @return the orderDAO
	 */
	public OrderDAO getOrderDAO() {
		return orderDAO;
	}

	/**
	 * @param orderDAO the orderDAO to set
	 */
	public void setOrderDAO(OrderDAO orderDAO) {
		this.orderDAO = orderDAO;
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
	@GET
	@Path("/packaging/{isbn}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrderOfPackagingByISBN(@PathParam("isbn")String isbn){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Order> orders = new ArrayList<Order>();
		try {
			orders = jobHandler.fetchJobPackaging(isbn);
		} catch (Exception e) {
			LogUtils.error("Error occurred while reading the list of job of packaging for ["+isbn+"]", e);
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of job of packaging!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(orders).build();
	}
	
	@GET
	@Path("/Shippingjob/{orderId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJobByOrderId(@PathParam("orderId")int orderId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Job job = null;
		try {
			JobSearchBean searchbean = new JobSearchBean();
			searchbean.setStationId("SHIPPING");
			searchbean.setOrderId(orderId);
			List<Job> jobs = jobHandler.readAll(searchbean);
			job = jobs.get(0);
		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of job of packaging!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(job).build();
	}
	@GET
	@Path("/completeJob/{jobId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response completeOrder(@PathParam("jobId")int jobId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			//Job job = jobHandler.read(jobId);
			//job.setJobStatus(lookupHandler.read(JobStatus.JobStatuses.COMPLETE.toString(), JobStatus.class));
			//jobHandler.update(job);
			MachineSearchBean bean = new MachineSearchBean();
			bean.setCurrentJobId(jobId);
			List<Machine> machines = machineHandler.readAll(bean);
			if(machines.size() > 0) {
				for(Machine machine : machines){
				
				machine.setCurrentJob(null);
				machineHandler.update(machine);
				}
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}

	public StationDAO getStationDAO() {
		return stationDAO;
	}

	public void setStationDAO(StationDAO stationDAO) {
		this.stationDAO = stationDAO;
	}
	

	
	
}
