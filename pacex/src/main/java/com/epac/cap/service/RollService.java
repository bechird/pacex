package com.epac.cap.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.EpacException;
import com.epac.cap.common.OrderBy;
import com.epac.cap.common.PersistenceException;
import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.functionel.PrintingTimeCalculator;
import com.epac.cap.handler.CoverBatchHandler;
import com.epac.cap.handler.JobHandler;
import com.epac.cap.handler.JobsByRollOrderingComparator;
import com.epac.cap.handler.LogHandler;
import com.epac.cap.handler.LookupHandler;
import com.epac.cap.handler.MachineHandler;
import com.epac.cap.handler.PartHandler;
import com.epac.cap.handler.RollByUtilizationComparator;
import com.epac.cap.handler.RollHandler;
import com.epac.cap.handler.RollMachineAssignmentComparator;
import com.epac.cap.handler.StationHandler;
import com.epac.cap.model.Job;
import com.epac.cap.model.MachineType;
import com.epac.cap.model.Part;
import com.epac.cap.model.Preference;
import com.epac.cap.model.Roll;
import com.epac.cap.model.RollStatus;
import com.epac.cap.model.RollType;
import com.epac.cap.model.Station;
import com.epac.cap.model.StationCategory;
import com.epac.cap.model.WFSDataSupport;
import com.epac.cap.model.WFSLocation;
import com.epac.cap.repository.LogSearchBean;
import com.epac.cap.repository.RollSearchBean;
import com.epac.cap.utils.Format;
import com.epac.cap.utils.LogUtils;
import com.epac.cap.utils.PaginatedResult;
import com.epac.cap.validator.InputResponseError;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@Path("/rolls")
public class RollService extends AbstractService{

	@Autowired
	private RollHandler rollHandler;
	
	@Autowired
	private LogHandler logHandler;
	
	@Autowired
	private CoverBatchHandler batchHandler;
	
	@Autowired
	private StationHandler stationHandler;
	
	@Autowired
	private JobHandler jobHandler;
	
	@Autowired
	private PartHandler partHandler;
	
	@Autowired
	private MachineHandler machineHandler;
	
	@Autowired
	private LookupHandler lookupHandler;
	
	@Autowired
	private PrintingTimeCalculator printingTimeCalculator;
	
	private static Logger logger = Logger.getLogger(RollService.class);
	
	public final static  String UNIT_US = "US";
	public final static  String UNIT_FR = "FR";
	
	private String bsValue;
	private String useOptimizedSheetAlgo;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRolls(){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Roll> rolls = new ArrayList<Roll>();
		try {
			rolls = rollHandler.readAll();
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of roll records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(rolls).build();
	}
	
	@GET
	@Path("/quick")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRollsQuick(){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Roll> rolls = new ArrayList<Roll>();
		try {
			RollSearchBean rsb = new RollSearchBean();
			rsb.setListing(true);
			rolls = rollHandler.readAll(rsb);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of roll records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(rolls).build();
	}

	@GET
	@Path("/idsList")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJobsIdsList(){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Integer> ids = new ArrayList<Integer>();
		try {
			ids = rollHandler.getIdsList();			
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
	public Response getPaginatedRolls(String data){
		
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		PaginatedResult paginatedResult = new PaginatedResult();
		List<Roll> rolls = new ArrayList<Roll>();
		
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			
			JsonNode json = mapper.readTree(data);
			
			JsonNode aoData = json.get("aoData");
			
			int draw = aoData.get(0).get("value").asInt();
			int start = aoData.get(3).get("value").asInt();
			int pageLength = aoData.get(4).get("value").asInt();
			
			//logger.debug("get rolls");
			//logger.debug("draw:" + draw);
			//.debug("start:" + start);
			//logger.debug("pageLength:" + pageLength);
			
			Integer count = 0;

			RollSearchBean rsb = new RollSearchBean();
			rsb.setResultOffset(start);
			rsb.setMaxResults(pageLength);
			rsb.setListing(true);

			
			//sorting
			int sortingColumn = 0;
			String sortingColumnName = "rollId";
			String sortingDirection = "desc";
			if(aoData.get(2) != null && aoData.get(2).get("value") != null && aoData.get(2).get("value").size() > 0){
				sortingColumn = aoData.get(2).get("value").get(0).get("column").asInt();
				sortingColumnName = aoData.get(1).get("value").get(sortingColumn).get("data").asText();
				sortingDirection = aoData.get(2).get("value").get(0).get("dir").asText();
			}
			OrderBy orderBy = new OrderBy();			
			if(sortingDirection.equals("asc"))orderBy.setDirection(OrderBy.ASC);
			if(sortingDirection.equals("desc"))orderBy.setDirection(OrderBy.DESC);
			
			if(sortingColumnName.equals("rollId"))orderBy.setName("rollId");
			if(sortingColumnName.equals("rollType.name"))orderBy.setName("rollType.id");
			if(sortingColumnName.equals("status.name"))orderBy.setName("status.id");
			if(sortingColumnName.equals("length"))orderBy.setName("length");
			if(sortingColumnName.equals("machineId"))orderBy.setName("machineId");
			if(sortingColumnName.equals("paperType.name"))orderBy.setName("paperType.id");
			if(sortingColumnName.equals("hours"))orderBy.setName("hours");
			if(sortingColumnName.equals("utilization"))orderBy.setName("utilization");
			if(sortingColumnName.equals("createdDate") || sortingColumnName.equals("creationDate"))orderBy.setName("createdDate");
			
			rsb.getOrderByList().clear();
			rsb.addOrderBy(orderBy);
			//-----------------------------------------------------------------------------------------------------

			//filtering
			boolean filtering = false;
			String filter = aoData.get(1).get("value").get(0).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					rsb.setSearchRollIdPart(filter);
					filtering = true;
				} catch (Exception e) {}
			}

			filter = aoData.get(1).get("value").get(1).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				rsb.setSearchRollType(filter);
				filtering = true;
			}
			
			filter = aoData.get(1).get("value").get(2).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					String dateFormat = "MMM dd yyyy";
					Preference dfPref = lookupHandler.read("DATE_FORMAT", Preference.class);
					if(dfPref != null){
						dateFormat = dfPref.getName();
					}
					SimpleDateFormat parser = new SimpleDateFormat(dateFormat);
				    Date date = parser.parse(filter); 
				    rsb.setCreationDateExact(date);
					filtering = true;
				} catch (Exception e) {}
			}

			filter = aoData.get(1).get("value").get(3).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				rsb.setSearchStatus(filter);
				filtering = true;
			}
			
			filter = aoData.get(1).get("value").get(4).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					rsb.setSearchLength(filter);
					filtering = true;
				} catch (Exception e) {}
			}
			
			filter = aoData.get(1).get("value").get(5).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				rsb.setSearchMachineId(filter);
				filtering = true;
			}
			
			filter = aoData.get(1).get("value").get(6).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				rsb.setSearchPaperType(filter);
				filtering = true;
			}
			
			filter = aoData.get(1).get("value").get(7).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					rsb.setSearchHours(filter);
					filtering = true;
				} catch (Exception e) {}
			}
			
			filter = aoData.get(1).get("value").get(8).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					rsb.setSearchUtilization(filter);
					filtering = true;
				} catch (Exception e) {}
			}
			//-----------------------------------------------------------------------------------------------------
			
			
			//search word
			String wordToSearch = aoData.get(5).get("value").get("value").asText();
			
			if(wordToSearch != null && !wordToSearch.isEmpty()) {
				
				//logger.debug("wordToSearch:" + wordToSearch);
				rolls = rollHandler.fullSearch(wordToSearch, null, null);
				count = rolls.size();				
				rolls = rollHandler.fullSearch(wordToSearch, pageLength, start);
				
				
			}else if(filtering){
				
				rsb.setResultOffset(null);
				rsb.setMaxResults(null);
				rolls = rollHandler.readAll(rsb);
				count = rolls.size();
				
				rsb.setResultOffset(start);
				rsb.setMaxResults(pageLength);
				rolls = rollHandler.readAll(rsb);
				
			}else {
				count = rollHandler.getCount();				
				rolls = rollHandler.readAll(rsb);
			}
			
			//-----------------------------------------------------------------------------------------------------

			//logger.debug("rolls:" + rolls.size());
			//logger.debug("rolls count:" + count);
			
			paginatedResult.setDraw(draw);
			paginatedResult.setRecordsFiltered(count);
			paginatedResult.setRecordsTotal(count);
			paginatedResult.setData(rolls);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of roll records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		
		return Response.ok(paginatedResult).build();
	}
	
	@GET
	@Path("/available/{color}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAvailableRolls(@PathParam("color") String color){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Roll> rolls = null;
		try {
			rolls = rollHandler.getAvailableRolls(color, this.getExecutingUserId());
			// try to calculate the utilization/hours for each available roll (without persisting the data):
			if(!rolls.isEmpty()){
				if(Part.PartColors._4C.getName().equals(color) || Part.PartColors._1C.getName().equals(color)){
					for(Roll aRoll : rolls){
						if(aRoll.getUtilization() == null){
							aRoll.setUtilization(0);
						}
						if(aRoll.getLength() != null && aRoll.getLength() > 0){
							Float availableHoursOnRoll = rollHandler.getAvailableHoursOnRoll(aRoll.getLength());
							List<Job> jobs = jobHandler.getAvailableJobsForScheduling(color, aRoll.getPaperType().getId());
							if(jobs != null && !jobs.isEmpty()){
								for(Job job : jobs){
									//re-calculate job hours based on roll width
									Part pr = partHandler.read(job.getPartNum());
									job.setHours(jobHandler.calculateJobHoursAndLength(pr, job.getQuantityNeeded(), aRoll.getWidth())[0]);
									if(availableHoursOnRoll - job.getHours() >= 0 && aRoll.getPaperType().equals(job.getPartPaperId())){
										availableHoursOnRoll -= job.getHours();
										aRoll.setHours(aRoll.getHours() + job.getHours());
									}
								}
								aRoll.setUtilization((int) ((aRoll.getHours() * machineHandler.getDefaultMachineSpeed(StationCategory.Categories.PRESS.toString()) * 100) / (float) aRoll.getLength()));
							}
						}
					}
				}
			}
			Collections.sort(rolls, new RollByUtilizationComparator());
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of available roll records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(rolls).build();
	}
	
	/**
	 * Provides a list of job ids suggested to be assigned to the roll (in param) for production
	 * Conditions include roll hours bigger or equal to suggested jobs; and both have same paper type 
	 * @param color, paperType, rollId
	 * @return List<Integer>
	 */
	@GET
	@Path("/proposeJobs/{color}/{paperType}/{rollWidth}/{rollId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response proposeJobs(@PathParam("color") String color, @PathParam("paperType") String paperType, 
			@PathParam("rollWidth") Float rollWidth, @PathParam("rollId") Integer rollId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Float[]> proposedJobs = new ArrayList<Float[]>();
		Roll roll = null;
		try {
			if(rollId <= 0){
				roll = rollHandler.buildNewRoll(paperType, rollWidth, this.getExecutingUserId());
			}else{
				roll = rollHandler.read(rollId);
			}
			if(roll != null){
				Float availableHoursOnRoll = rollHandler.getAvailableHoursOnRoll(roll.getLength());
				List<Job> jobs = jobHandler.getAvailableJobsForScheduling(color, paperType);
				//float impositionType = 0;
				if(jobs != null && !jobs.isEmpty()){
					for(Job job : jobs){
						// re-calculate job hours based on roll width, 
						// and verify jobs on the roll are imposed with same imposition schema
						Part pr = partHandler.read(job.getPartNum());
						Float[] jobHoursLengthAndImp = jobHandler.calculateJobHoursAndLength(pr, job.getQuantityNeeded(), roll.getWidth());
						job.setHours(jobHoursLengthAndImp[0]);
						//if(impositionType == 0){
						//	impositionType = jobHoursLengthAndImp[2];
						//}
						if(availableHoursOnRoll - job.getHours() >= 0 && roll.getPaperType().getId().equals(job.getPartPaperId())){
							//&& impositionType == jobHoursLengthAndImp[2]){
							proposedJobs.add(new Float[] {Float.parseFloat(job.getJobId().toString()), job.getHours()});
							availableHoursOnRoll -= job.getHours();
						}
					}
				}
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while retrieving the list of jobs to propose for the roll production!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(proposedJobs).build();
	}
	
	@GET
	@Path("/scheduled/{stationId}/{selectedMachineType}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScheduledRolls(@PathParam("stationId") String stationId, @PathParam("selectedMachineType") String selectedMachineType){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Roll> rolls = new ArrayList<Roll>();
		try {
			RollSearchBean rsb = new RollSearchBean();
			rsb.setStatusIn(new String[] {RollStatus.statuses.SCHEDULED.toString()});
			if(StationCategory.Categories.PRESS.toString().equals(stationId)){	
				rsb.setTypeIn(new String[] {RollType.types.NEW.toString(), RollType.types.LEFTOVER.toString()});
				if(selectedMachineType != null && selectedMachineType.contains(MachineType.types._1C.getName())){
					for(Roll iter : rollHandler.readAll(rsb)){
						if(iter.getColors() != null && selectedMachineType.contains(iter.getColors())){
							rolls.add(iter);
						}
					}
				}else{
					rolls = rollHandler.readAll(rsb);
				}
				//rolls = rollHandler.fetchRoll();
				LogUtils.debug("rolls:"+rolls.size());
			}else if(StationCategory.Categories.PLOWFOLDER.toString().equals(stationId)){
				rsb.setTypeIn(new String[] {RollType.types.PRODUCED.toString()});
				if(selectedMachineType != null && !selectedMachineType.isEmpty() && !selectedMachineType.equals(MachineType.types._ALL.getName())){
					for(Roll iter : rollHandler.readAll(rsb)){
						if(selectedMachineType.equals(iter.getMachineTypeId())){
							rolls.add(iter);
						}
					}
				}else{
					rolls = rollHandler.readAll(rsb);
				}
			}
			
			Collections.sort(rolls, new RollMachineAssignmentComparator());
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of scheduled roll records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(rolls).build();
	}
	
	@GET
	@Path("/unassign/{rollId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response unassignRoll(@PathParam("rollId") Integer rollId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			String executingUserId = this.getExecutingUserId();// TODO make sure this works
			if(constraintViolationsMessages.isEmpty()){
				if(rollId != null){
					rollHandler.unassignRoll(rollId, executingUserId);
				}
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while un-assigning a roll from the machine!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRoll(@PathParam("id") Integer id){
		Roll roll = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			roll = rollHandler.read(id);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the roll record with id: " + id);
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(roll).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response add(Roll roll){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			
			if(constraintViolationsMessages.isEmpty()){
				doAddLogging(roll);
				
				rollHandler.create(roll);
				response = Response.ok(roll.getRollId()).build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while adding the roll record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;	
	}
	
	@POST
	@Path("/checkTrimCut/{rollWidth}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response checkTrimCutSizes(int[] selectedJobsForProduction, @PathParam("rollWidth") Float rollWidth){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		List<String> result = new ArrayList<String>();
		try {
			if(constraintViolationsMessages.isEmpty()){
				Preference unitValue = lookupHandler.read("UNITSYSTEM", Preference.class);
				if(unitValue == null){
					unitValue = new Preference();
					unitValue.setName(UNIT_US);
				}
				Roll tmpRoll = new Roll(-1);
				SortedSet<Job> tmpJobs = new TreeSet<Job>(new JobsByRollOrderingComparator());
				for(int i = 1; i< selectedJobsForProduction.length; i++){
					tmpJobs.add(jobHandler.read(selectedJobsForProduction[i]));
				}
				tmpRoll.setAlljobs(tmpJobs);
				tmpRoll.setRollType(lookupHandler.read(RollType.types.NEW.toString(), RollType.class));
				
				float bestSheetHeightS1  = Float.parseFloat(System.getProperty(ConfigurationConstants.SHEET_S1_HEIGHT, "0.0"));	
				float bestSheetHeightS2  = Float.parseFloat(System.getProperty(ConfigurationConstants.SHEET_S2_HEIGHT, "0.0"));			
				float bestSheetHeightS3  = Float.parseFloat(System.getProperty(ConfigurationConstants.SHEET_S3_HEIGHT, "0.0"));
				
				float coverSheetSizeM  = Float.parseFloat(System.getProperty(ConfigurationConstants.COVER_M_SHEET_HEIGHT, "0.0"));	
				float coverSheetSizeL  = Float.parseFloat(System.getProperty(ConfigurationConstants.COVER_L_SHEET_HEIGHT, "0.0"));			
				float coverSheetSizeXL  = Float.parseFloat(System.getProperty(ConfigurationConstants.COVER_XL_SHEET_HEIGHT, "0.0"));
				float coverSheetSize = 0;
				
				float trimCutSecuRetreat  = Float.parseFloat(System.getProperty(ConfigurationConstants.TRIM_CUT_SECUR_RETREAT, "0.0"));
				float trimCutSecuRetreatValue = UNIT_US.equals(unitValue.getName()) ? Format.inch2mm(trimCutSecuRetreat) : trimCutSecuRetreat;
				
				String bestSheet;
				float bestSheetHeight = 0;
				for(Job job : tmpJobs){
					bestSheet = printingTimeCalculator.getBestSheetHeight(tmpRoll, job, this.getBsValue(), this.getUseOptimizedSheetAlgo());
					if(StringUtils.isBlank(bestSheet) || "S0".equals(bestSheet)){
						continue;
					}
					if(ConfigurationConstants.SHEET_S1_HEIGHT.contains(bestSheet)){
						bestSheetHeight = bestSheetHeightS1;
					}else if(ConfigurationConstants.SHEET_S2_HEIGHT.contains(bestSheet)){
						bestSheetHeight = bestSheetHeightS2;
					}else if(ConfigurationConstants.SHEET_S3_HEIGHT.contains(bestSheet)){
						bestSheetHeight = bestSheetHeightS3;
					}
					bestSheetHeight = UNIT_US.equals(unitValue.getName()) ? Format.inch2mm(bestSheetHeight) : bestSheetHeight;
					if(bestSheetHeight == 0){ //In case the properties file doesn't have the sheet sizes
						bestSheetHeight = 950;
					}
					Part part = partHandler.read(job.getPartNum().endsWith("T") ? (job.getPartNum().substring(0, job.getPartNum().length() - 1).concat("C")) : job.getPartNum());
					 float signatureHeight = (UNIT_US.equals(unitValue.getName()) ? Format.inch2mm(part.getLength()) : part.getLength()) + 26;
					 int nbSigPerSheet = (int) Math.floor(bestSheetHeight / signatureHeight);
					 float realSignatureHeight = bestSheetHeight / nbSigPerSheet;
					 // now look for the cover sheet size used
					 for(WFSDataSupport ds: part.getDataSupportsOnProd()){
						 if(ds.getName().equals(WFSDataSupport.NAME_IMPOSE) && ds.getDsType().equalsIgnoreCase(WFSDataSupport.TYPE_COVER)){
							 WFSLocation wfdloc = ds.getLocationdByType("Destination");
							 if(wfdloc != null && !StringUtils.isBlank(wfdloc.getPath())){
								 String coverSheet = wfdloc.getFileName();
								 coverSheet = coverSheet.substring(0, coverSheet.indexOf("_"));
								 if("M".equals(coverSheet)){
									 coverSheetSize = coverSheetSizeM;
								 }else if("L".equals(coverSheet)){
									 coverSheetSize = coverSheetSizeL;
								 }else if("XL".equals(coverSheet)){
									 coverSheetSize = coverSheetSizeXL;
								 }
								 coverSheetSize = UNIT_US.equals(unitValue.getName()) ? Format.inch2mm(coverSheetSize) : coverSheetSize;
								 if(coverSheetSize == 0){ //In case the properties files doesn't have the cover sheet sizes
									 coverSheetSize = 275;
								 }
								 if(realSignatureHeight  > (coverSheetSize - trimCutSecuRetreatValue)){
									 result.add(job.getJobId()+":"+(realSignatureHeight - coverSheetSize + trimCutSecuRetreatValue));
								 }
							 } 
						 }
					 }
				}
				response = Response.ok(result).build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while checking the trim cut size!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			response = Response.ok(result).build();
		}
		return response;
	}
	
	@POST
	@Path("/toProduce/{pfMachineType}/{modeOption}/{rollWidth}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response produceRoll(int[] selectedJobsForProduction, @PathParam("pfMachineType") String pfMachineType,
			@PathParam("modeOption") String modeOption, @PathParam("rollWidth") Float rollWidth){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		Integer rollId = 0;
		try {
			String executingUserId = this.getExecutingUserId();// TODO make sure this works
			if(constraintViolationsMessages.isEmpty()){
				if(selectedJobsForProduction != null && selectedJobsForProduction.length > 0){
					rollId = rollHandler.produceRoll(selectedJobsForProduction, rollWidth, pfMachineType, modeOption, executingUserId);
					//if coverpress input type is batch create batch and sections
					Station station = stationHandler.read(StationCategory.Categories.COVERPRESS.toString());
					Integer recreate = null;	
					if(station.getInputType().equals(Station.inputTypes.Batch.getName()))
					batchHandler.createBatch(rollId, executingUserId, recreate);
				}
//				if(rollId == 0){
//					constraintViolationsMessages.put("errors", "Some Jobs could not be assigned to the roll; one of the reasons may be that the"
//							+ " imposition is different in some of the jobs while all should be the same...");
//					InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
//					response = inputResponseError.createResponse();
//				}else{
					response = Response.ok(rollId).build();
				//}
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while producing the roll!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			//return inputResponseError.createResponse();
			rollId = 0;
			response = Response.ok(rollId).build();
		}
		catch (EpacException e) {
			constraintViolationsMessages.put("errors", "An error occurred while producing the roll: Jobs may have different impositions!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			//return inputResponseError.createResponse();
			rollId = 0;
			response = Response.ok(rollId).build();
		}
		return response;
	}
	
	@GET
	@Path("/leftover/{parentRollId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLeftOverRoll(@PathParam("parentRollId") Integer parentRollId){
		Roll roll = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			roll = rollHandler.getLeftOverRoll(parentRollId);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while retrieving the left over roll for parent roll id: " + parentRollId);
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(roll).build();
	}
	
	@GET
	@Path("/produced/{parentRollId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProducedRoll(@PathParam("parentRollId") Integer parentRollId){
		Roll roll = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			roll = rollHandler.getProducedRoll(parentRollId);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while retrieving the produced roll for parent roll id: " + parentRollId);
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(roll).build();
	}
	
	@Path("{id}")
	@DELETE
	@Transactional
	@Secured({ "ROLE_PM", "ROLE_ADMIN", "ROLE_LOP"})
	public Response delete(@PathParam("id") Integer id){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response res = null;
        try{
        	Roll roll = rollHandler.read(id);
        	if(roll != null){
        		//see if ok to delete the roll 
        		LogSearchBean lsb = new LogSearchBean();
        		lsb.setRollId(id);
        		if(logHandler.readAll(lsb).isEmpty() && roll.getJobs().isEmpty()){
	        		rollHandler.delete(roll);
	            	res = Response.status(200).build();
        		}else{
        			constraintViolationsMessages.put("errors", "Cannot delete the roll as it has jobs or logs assigned to it!");
            		InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
    				res = inputResponseError.createResponse();
        		}
        	}else{
        		return Response.status(404).entity("Roll with id : " + id + " not present in the database").build();
        	}
        }catch (PersistenceException e) {
        	constraintViolationsMessages.put("errors", "An error occurred while deleting the roll record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return res;
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(Roll roll){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			
			if(constraintViolationsMessages.isEmpty()){
				doEditLogging(roll);
				
				rollHandler.update(roll);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while updating the roll record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
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
	 * @return the bsValue
	 * @throws PersistenceException 
	 */
	public String getBsValue() throws PersistenceException {
		if(bsValue == null){
			Preference bsValuePref = lookupHandler.read("BESTSHEETUSE", Preference.class);
			if(bsValuePref != null){
				bsValue = bsValuePref.getName();
			}else{
				bsValue = "true";
			}
		}
		return bsValue;
	}

	/**
	 * @param bsValue the bsValue to set
	 */
	public void setBsValue(String bsValue) {
		this.bsValue = bsValue;
	}

	/**
	 * @return the useOptimizedSheetAlgo
	 * @throws PersistenceException 
	 */
	public String getUseOptimizedSheetAlgo() throws PersistenceException {
		if(useOptimizedSheetAlgo == null){
			Preference bsValuePref = lookupHandler.read("USEOPTIMIZEDSHEETALGO", Preference.class);
			if(bsValuePref != null){
				useOptimizedSheetAlgo = bsValuePref.getName();
			}else{
				useOptimizedSheetAlgo = "true";
			}
		}
		return useOptimizedSheetAlgo;
	}

	/**
	 * @param useOptimizedSheetAlgo the useOptimizedSheetAlgo to set
	 */
	public void setUseOptimizedSheetAlgo(String useOptimizedSheetAlgo) {
		this.useOptimizedSheetAlgo = useOptimizedSheetAlgo;
	}
}
