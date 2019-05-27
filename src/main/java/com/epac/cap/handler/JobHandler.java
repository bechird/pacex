package com.epac.cap.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.DateUtil;
import com.epac.cap.common.OrderBy;
import com.epac.cap.common.PersistenceException;
import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.functionel.PrintingTimeCalculator;
import com.epac.cap.functionel.WorkflowEngine;
import com.epac.cap.model.DefaultStation;
import com.epac.cap.model.Job;
import com.epac.cap.model.JobPrevious;
import com.epac.cap.model.JobStatus;
import com.epac.cap.model.JobType;
import com.epac.cap.model.LoadTag;
import com.epac.cap.model.Log;
import com.epac.cap.model.Machine;
import com.epac.cap.model.MachineStatus;
import com.epac.cap.model.Order;
import com.epac.cap.model.OrderPart;
import com.epac.cap.model.PaperType;
import com.epac.cap.model.Part;
import com.epac.cap.model.Preference;
import com.epac.cap.model.Priority;
import com.epac.cap.model.Roll;
import com.epac.cap.model.RollStatus;
import com.epac.cap.model.RollType;
import com.epac.cap.model.Station;
import com.epac.cap.model.StationCategory;
import com.epac.cap.model.SubPart;
import com.epac.cap.repository.JobDAO;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.repository.LogDAO;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.repository.MachineDAO;
import com.epac.cap.repository.MachineSearchBean;
import com.epac.cap.repository.OrderDAO;
import com.epac.cap.repository.PartDAO;
import com.epac.cap.repository.RollDAO;
import com.epac.cap.repository.StationDAO;
import com.epac.cap.service.NotificationService;
import com.epac.cap.sse.beans.EventTarget;

/**
 * Interacts with Job data.  Uses JobDAO for entity persistence.
 * @author walid
 *
 */
@Service
public class JobHandler {
	
	@Autowired
	private JobDAO jobDAO;
	
	@Autowired
	private OrderDAO orderDAO;

	@Autowired
	private PartDAO partDAO;
	
	@Autowired
	private StationDAO stationDAO;
	
	@Autowired
	private MachineDAO machineDAO;
	
	@Autowired
	private LookupDAO lookupDAO;
	
	@Autowired
	private RollDAO rollDAO;
	
	@Autowired
	private LogDAO logDAO;
	
	@Autowired
	private RollHandler rollHandler;
	
	@Autowired
	private OrderHandler orderHandler;
	
	@Autowired
	private MachineHandler machineHandler;
	
	@Autowired
	private PrintingTimeCalculator printingTimeCalculator;
	
	@Autowired
	private NotificationService notificationService;
	
	@Autowired
	private WorkflowEngine workflowEngine;
	
	private static Logger logger = Logger.getLogger(JobHandler.class);
	public static final String DEFAULT_ = "DEFAULT";
	
	/***
	 * No arg constructor.  This is the preferred constructor.
	 */
	public JobHandler(){
		/*TopicMatcher matcher = Topics.only("cap/event/order/accepted");// .or(Topics.topics("app/events/swing/fields/**"));
		NDispatcher.getDispatcher().subscribe(matcher, List.class, new Subscriber<List>() {
			public void onEvent(Event<List> event) throws Exception {
				System.out.println("Received: " + event.toString() + "subscribing for order accepted");
				createJobs((Order)event.getSource().get(0), (Part)event.getSource().get(1), (Part)event.getSource().get(2), (String)event.getSource().get(3));
			}
		});*/
	}
  
	/** 
	 * Calls the corresponding create method on the JobDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public void create(Job bean) throws PersistenceException {
		try {
			prepareBeans(bean);
			getJobDAO().create(bean);
			com.epac.cap.sse.beans.Event event = new com.epac.cap.sse.beans.Event(EventTarget.Job, false, null, bean.getJobId());
			notificationService.broadcast(event);
		} catch (Exception ex) {
			bean.setJobId(null);
			com.epac.cap.sse.beans.Event event=new com.epac.cap.sse.beans.Event(EventTarget.Job, true, ex.getLocalizedMessage(), bean.getJobId());
			notificationService.broadcast(event);
			logger.error("Error occurred creating a Job : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Calls the corresponding update method on the JobDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(Job bean) throws PersistenceException {
		try {
			prepareBeans(bean);
			getJobDAO().update(bean);		   
		} catch (Exception ex) {
			logger.error("Error occurred updating a Job : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	private void prepareBeans(Job bean){
		/*if(bean.getOrder() == null || bean.getOrder().getOrderId() == null){
			//nullify the status otherwise hibernate will ask to persist the empty transient status bean
			bean.setOrder(null);
		}else{
			bean.setOrder(orderDAO.read(bean.getOrder().getOrderId()));
		}
		if(bean.getPart() == null || StringUtils.isEmpty(bean.getPart().getPartNum())){
			bean.setPart(null);
		}else{
			bean.setPart(partDAO.read(bean.getPart().getPartNum()));
		}*/
		/*if(bean.getStation() == null || StringUtils.isEmpty(bean.getStation().getStationId())){
			bean.setStation(null);
		}else{
			bean.setStation(stationDAO.read(bean.getStation().getStationId()));
		}*/
		/*if(bean.getMachine() == null || StringUtils.isEmpty(bean.getMachine().getMachineId())){
			bean.setMachine(null);
		}else{
			bean.setMachine(machineDAO.read(bean.getMachine().getMachineId()));
		}*/
		if(bean.getMachineId() != null && StringUtils.isEmpty(bean.getMachineId())){
			bean.setMachineId(null);
		}
		if(bean.getJobStatus() == null || StringUtils.isEmpty(bean.getJobStatus().getId())){
			bean.setJobStatus(null);
		}else{
			bean.setJobStatus(lookupDAO.read(bean.getJobStatus().getId(), JobStatus.class));
		}
		if(bean.getJobPriority() == null || StringUtils.isEmpty(bean.getJobPriority().getId())){
			bean.setJobPriority(null);
		}else{
			bean.setJobPriority(lookupDAO.read(bean.getJobPriority().getId(), Priority.class));
		}
		if(bean.getBinderyPriority() == null || StringUtils.isEmpty(bean.getBinderyPriority().getId())){
			bean.setBinderyPriority(null);
		}else{
			bean.setBinderyPriority(lookupDAO.read(bean.getBinderyPriority().getId(), Priority.class));
		}
		if(bean.getRollId() != null && bean.getRollId() <= 0){
			bean.setRollId(null);
		}
		/*if(bean.getRoll() == null || bean.getRoll().getRollId() == null){
			bean.setRoll(null);
		}else{
			bean.setRoll(rollDAO.read(bean.getRoll().getRollId()));
		}*/
		if(bean.getJobType() == null || StringUtils.isEmpty(bean.getJobType().getId())){
			bean.setJobType(null);
		}else{
			bean.setJobType(lookupDAO.read(bean.getJobType().getId(), JobType.class));
		}
	}
		
	/** 
	 * Calls the corresponding delete method on the JobDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public boolean delete(Job bean) throws PersistenceException {
		try {
			boolean result = getJobDAO().delete(bean);
			if(result){
				com.epac.cap.sse.beans.Event event = new com.epac.cap.sse.beans.Event(EventTarget.Job, false, null, bean.getJobId());
				notificationService.broadcast(event);
			}
			return result;
		} catch (Exception ex) {
			logger.error("Error occurred deleting a Job with id '" + (bean == null ? null : bean.getJobId()) + "' : " + ex.getMessage(),ex);
			com.epac.cap.sse.beans.Event event=new com.epac.cap.sse.beans.Event(EventTarget.Job, true, ex.getLocalizedMessage(), bean.getJobId());
			notificationService.broadcast(event);
			throw new PersistenceException(ex);
		}
	}

	/** 
	 * A convenience method which calls readAll(JobSearchBean) with a
	 * null search bean. 
	 *
	 * @see #readAll(JobSearchBean)
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Job> readAll() throws PersistenceException{
		return this.readAll(null);    
	}
	
	/** 
	 * Calls the corresponding readAll method on the JobDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Job> readAll(JobSearchBean searchBean) throws PersistenceException{
		try{
			return getJobDAO().readAll(searchBean);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a list of Jobs : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Retrives an order jobs list.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Set<Job> readOrderJobs(Integer orderId) throws PersistenceException{
		try{
			SortedSet<Job> jobs = new TreeSet<Job>(new JobsByProductionOrderingComparator()); 
			JobSearchBean jsb = new JobSearchBean();
			jsb.setOrderId(orderId);
			jobs.addAll(this.readAll(jsb));
			return jobs;
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a list of order Jobs : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Calls the corresponding read method on the JobDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Job read(Integer jobId) throws PersistenceException{
		try{
			return getJobDAO().read(jobId);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a Job with id '" + jobId + "' : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Returns the Overs value of the original quantity.
	 */
	public Integer getOvers(Integer originalQuantity) throws PersistenceException{
		Float overs = (float) 0;
		Float oversAdditif = (float) 0;
		Preference oversPref = lookupDAO.read("OVERS", Preference.class);
		if(oversPref != null){
			try{
				overs = Float.parseFloat(oversPref.getName());
			}catch(NumberFormatException nfe){
				overs = (float) 0;
			} 
		}
		oversPref = lookupDAO.read("OVERSADDITIF", Preference.class);
		if(oversPref != null){
			try{
				oversAdditif = Float.parseFloat(oversPref.getName());
			}catch(NumberFormatException nfe){
				oversAdditif = (float) 0;
			} 
		}
		return (int) (Math.floor(originalQuantity * overs / (float)100) + oversAdditif);
	}
	
	/** 
	 * Returns the Unders value of the original quantity.
	 */
	public Integer getUnders(Integer originalQuantity) throws PersistenceException{
		Float unders = (float) 0;
		Float undersAdditif = (float) 0;
		Preference undersPref = lookupDAO.read("UNDERS", Preference.class);
		if(undersPref != null){
			try{
				unders = Float.parseFloat(undersPref.getName());
			}catch(NumberFormatException nfe){
				unders = (float) 0;
			} 
		}
		undersPref = lookupDAO.read("UNDERSADDITIF", Preference.class);
		if(undersPref != null){
			try{
				undersAdditif = Float.parseFloat(undersPref.getName());
			}catch(NumberFormatException nfe){
				undersAdditif = (float) 0;
			} 
		}
		return (int) (Math.floor(originalQuantity * unders / (float)100) + undersAdditif);
	}
	
	/**
	 * Create the necessary jobs for part using the default stations for the corresponding part category.
	 */
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void createJobs(Order order, Part topPart, Part part, String executingUserId) throws PersistenceException {
		try {
			Map<String,Integer> requiredStations = new HashMap<String,Integer>();
			for(DefaultStation ds : part.getCategory().getDefaultStations()){
				if(DEFAULT_.equals(ds.getId().getCritiriaId()) && DEFAULT_.equals(ds.getId().getBindingTypeId())){
					//special case of lamination station
					if(StationCategory.Categories.LAMINATION.toString().equals(ds.getId().getStationCategoryId())){
						if(part.getLamination() != null && !"no".equalsIgnoreCase(part.getLamination().getId())){
							requiredStations.put(ds.getId().getStationCategoryId(), ds.getProductionOrdering());
						}
					}else{
						requiredStations.put(ds.getId().getStationCategoryId(), ds.getProductionOrdering());
					}
				}else if(DEFAULT_.equals(ds.getId().getCritiriaId()) && !DEFAULT_.equals(ds.getId().getBindingTypeId())){
					if(topPart.getBindingType() != null && ds.getId().getBindingTypeId().equals(topPart.getBindingType().getId())){
						requiredStations.put(ds.getId().getStationCategoryId(), ds.getProductionOrdering());
					}
				}else if (!DEFAULT_.equals(ds.getId().getCritiriaId()) && DEFAULT_.equals(ds.getId().getBindingTypeId())){
					if(topPart.partCritiriaFor(ds.getId().getCritiriaId())){
						requiredStations.put(ds.getId().getStationCategoryId(), ds.getProductionOrdering());
					}
				}else{
					if(topPart.getBindingType() != null && ds.getId().getBindingTypeId().equals(topPart.getBindingType().getId()) &&
							topPart.partCritiriaFor(ds.getId().getCritiriaId())){
						requiredStations.put(ds.getId().getStationCategoryId(), ds.getProductionOrdering());
					}
				}
			}
			
			// order the requiredStations by overall production ordering (for top and sub parts)...
			for(String stationCategoryId : requiredStations.keySet()){
				//Make sure the station exists:
				Station theStation = stationDAO.read(stationCategoryId);
				if(theStation != null && theStation.getActiveFlag()){
					//create the jobs
					Job job = new Job();
					job.setOrderId(order.getOrderId());
					job.setPartNum(part.getPartNum());
					//job.setQuantityNeeded(order.getOrderPart().getQuantity() + getOvers(order.getOrderPart().getQuantity()));
					//job.setQuantityNeeded(order.getOrderPart().getQuantity());
					job.setQuantityNeeded(order.getOrderPartByPartNum(topPart.getPartNum()).getQuantityMax());
					job.setSplitLevel(0);
					//get the station
					//StationSearchBean ssb = new StationSearchBean();
					//ssb.setStationCategoryId(stationCategoryId);
					//List<Station> stations = stationDAO.readAll(ssb);
					//if(!stations.isEmpty()){
					//	job.setStationId(stations.get(0).getStationId());
					//}
					job.setStationId(stationCategoryId);
					job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.NEW.toString(), JobStatus.class));
					job.setJobPriority(lookupDAO.read(order.getPriority(), Priority.class));
					job.setProductionOrdering(requiredStations.get(stationCategoryId));
					
					job.setDueDate(order.getDueDate());
					job.setPartColor(topPart.getColors());
					job.setPartPaperId(topPart.getPaperType().getId());
					job.setProductionMode(order.getProductionMode());
					job.setPartTitle(topPart.getTitle());
					job.setPartIsbn(topPart.getIsbn());
					job.setPartCategory(part.getCategory().getId());
					
					if(StationCategory.Categories.PRESS.toString().equals(stationCategoryId)){
						// TODO see why this generates npe
						OrderPart op = order.getOrderPartByPartNum(topPart.getPartNum());
						job.setHours((op != null && op.getPrintingHours() != null) ? op.getPrintingHours() : 0);
						//add hours to the unscheduled total for the press station
						/*if(!stations.isEmpty()){
							if(stations.get(0).getUnscheduledHours() == null){
								stations.get(0).setUnscheduledHours((float) 0);
							}
							stations.get(0).setUnscheduledHours(stations.get(0).getUnscheduledHours() + job.getHours());
							stationDAO.update(stations.get(0));
						}*/
					}else if(StationCategory.Categories.PLOWFOLDER.toString().equals(stationCategoryId)){
						job.setHours(calculateJobHoursAndLength(part, job.getQuantityNeeded(), (float) 0.0)[1] / machineHandler.getDefaultMachineSpeed(stationCategoryId));
					}else{
						OrderPart op = order.getOrderPartByPartNum(topPart.getPartNum());
						job.setHours((op != null ? op.getQuantityMax() : 0) / machineHandler.getDefaultMachineSpeed(stationCategoryId));
					}
					if(Part.PartsCategory.BOOK.toString().equals(part.getCategory().getId())){
						job.setJobType(lookupDAO.read(JobType.JobTypes.BINDING.toString(), JobType.class));
					}else{
						if(StationCategory.Categories.PRESS.toString().equals(stationCategoryId)){
							Float impSchema = calculateJobHoursAndLength(part, job.getQuantityNeeded(), (float) 0.0)[2];
							if(impSchema == 2){
								job.setJobType(lookupDAO.read(JobType.JobTypes.PRINTING_2UP.toString(), JobType.class));
							}else if(impSchema == 3){
								job.setJobType(lookupDAO.read(JobType.JobTypes.PRINTING_3UP.toString(), JobType.class));
							}else if(impSchema == 4){
								job.setJobType(lookupDAO.read(JobType.JobTypes.PRINTING_4UP.toString(), JobType.class));
							}
						}else{
							job.setJobType(lookupDAO.read(JobType.JobTypes.PRINTING.toString(), JobType.class));
						}
					}
					job.setBinderyPriority(lookupDAO.read(Priority.Priorities.NORMAL.toString(), Priority.class));
					job.setCreatedDate(new Date());
					job.setCreatorId(executingUserId);
					jobDAO.create(job);
				}
			}
			com.epac.cap.sse.beans.Event event = new com.epac.cap.sse.beans.Event(EventTarget.Job, false, null, 0);
			notificationService.broadcast(event);
		} catch (Exception ex) {
			logger.error("Error occurred creating jobs: " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Returns the jobs running on the station.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Job> getStationJobs(String stationId) throws PersistenceException{
		try{
			JobSearchBean jsb = new JobSearchBean();
			//jobs on a station (shown on the overview schedule board page) are the jobs which are not cancelled nor complete,
			//and related to the selected station
			List<String> statusesNotIn = Arrays.asList(JobStatus.JobStatuses.CANCELLED.toString(), JobStatus.JobStatuses.COMPLETE.toString());
			jsb.setStatusesNotIn(statusesNotIn);
			jsb.setStationId(stationId);
			return getJobDAO().readAll(jsb);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving the list of jobs on the station " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Returns the rolls running on the station based on the list of jobs on this same station.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Set<Roll> getStationRolls(List<Job> jobs) throws PersistenceException{
		try{
			Set<Roll> result = new HashSet<Roll>();
			for(Job job : jobs){
				if(job.getRollId() != null){
					result.add(rollDAO.read(job.getRollId()));
				}
			}
			return result;

		} catch (Exception ex) {
			logger.error("Error occurred retrieving the list of rolls from the list of jobs on the station " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Used on the scheduling page to retrieve the jobs to schedule and assign to rolls.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Job> getAvailableJobsForScheduling(String color, String paperType) throws PersistenceException{
		try{
			List<Job> result = null;
			JobSearchBean jsb = new JobSearchBean();
			//jobs available for roll scheduling are the 'new' jobs with type 'printing' and related to 'text' parts; have no roll assigned yet,
			//and assigned to the PRESS station
			//jsb.setJobType(JobType.JobTypes.PRINTING.toString());
			jsb.setStatus(JobStatus.JobStatuses.NEW.toString());
			jsb.setPartCategory(Part.PartsCategory.TEXT.toString());
			jsb.setStationId(StationCategory.Categories.PRESS.toString());
			jsb.setRollIdNull(true);
			if(Part.PartColors._1C.getName().equals(color)){
				jsb.setPartColors(color);
			}
			if(!"ALL".equals(paperType)){
				jsb.setPartPapertype(paperType);
			}
			result = jobDAO.readAll(jsb);
			
			List<PaperType> pt = lookupDAO.readAll(null, null, PaperType.class);
			// see which jobs need to run on more than 18 inch roll, and display this info on the front end on scheduling page
			float defaultWidth = Float.parseFloat(System.getProperty(ConfigurationConstants.DEFAULT_PAPER_WIDTH));
			for(Job j : result){
				Part pr = partDAO.read(j.getPartNum());
				float width = workflowEngine.checkRollWidthCompatibility(pr, defaultWidth);
				if(width > defaultWidth){
					//using the job name field temporarily to send this info
					j.setJobName((j.getJobName() != null ? j.getJobName() : "") + "_W" + width);
				}
				// needed for display on front end
				j.setOrderPartsCount(jobDAO.orderPartsCount(j.getOrderId()));
				if(!pr.getTopParts().isEmpty()){
					SubPart parent = pr.getTopParts().iterator().next();
					pr = partDAO.read(parent.getId().getTopPartNum());
					j.setBindingTypeId(pr.getBindingType().getId());
				}
				for(PaperType p : pt){
					if(p.getId().equals(j.getPartPaperId())){
						j.setPartPaperShortName(p.getShortName());
						break;
					}
				}
				if(pr != null && pr.getSpotVarnish()){
					j.setPartColor(j.getPartColor() + "/<font title = 'Spot Varnish' color='red'><b>SV<b/></font>");
				}
			}
			
			Collections.sort(result, new JobSchedulingComparator());
			
			return result;
		} catch (Exception ex) {
			logger.error("Error occurred retrieving the list of jobs available for schduling and assigning to rolls." + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Un-Assigning a job from the machine;
	 */
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void unassignJob(Integer jobId, String executingUserId) throws PersistenceException {
		try {
			if(jobId != null){
				Job job = jobDAO.read(jobId);
				if(job != null){
					job.setMachineId(null);
					job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.SCHEDULED.toString(), JobStatus.class));
					job.setMachineOrdering(0);
					getJobDAO().update(job);
				}
			}
		} catch (Exception ex) {
			logger.error("Error occurred un-assigning the roll from the machine : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	/**
	 * Updates the job statuses to complete for the selected jobs and sets the quantity produced;
	 * Also a new produced roll should be created and the associated plow folder jobs need to be assigned to it
	 * 
	 * 
	 * 
	 * TODO Try to update the last log entry roll id; it should be set to the produced roll when there is a produced roll or to the leftover roll
	 * when there is leftover roll
	 */
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void setCompletedJobsFromProd(Log bean, String executingUserId) throws PersistenceException {
		try {
			handleCompletedJobs(bean, executingUserId);
		} catch (Exception ex) {
			logger.error("Error occurred while setting the completed jobs from the production dashboard: " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}
	}
	
	/**
	 * Updates the job statuses to complete for all the jobs and sets the quantity produced to the order quantity;
	 * Also a new produced roll should be created and the associated plow folder jobs need to be assigned to it...
	 * Also set the roll status to exhausted, and remove it from the machine
	 * Also set the machine status to 'ON'
	 * 
	 * 
	 * 
	 * 
	 * 
	 * TODO Try to update the last log entry roll id; it should be set to the produced roll when there is a produced roll or to the leftover roll
	 * when there is leftover roll
	 */
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void setAllCompletedJobsFromProd(Log bean, String executingUserId) throws PersistenceException {
		try {
			List<String> completedJobQtys = bean.getCompletedJobQtys();
			Roll theRoll = null;
			if(completedJobQtys != null && completedJobQtys.size() > 0){
				for(int i = 0; i < completedJobQtys.size(); i++){
					Integer jobId = Integer.parseInt(completedJobQtys.get(i).substring(0, completedJobQtys.get(i).indexOf("_")));
					Job job = this.read(jobId);
					Integer qty = (job != null && job.getQuantityNeeded() != null) ? job.getQuantityNeeded() : 0;
					completedJobQtys.set(i, jobId + "_" + qty + "_0");
					if(theRoll == null && job != null && job.getRollId() != null){
						theRoll = rollDAO.read(job.getRollId());
					}
				}
			}else if(bean.getRollId() != null){//somehow the roll has no jobs on it...
				theRoll = rollDAO.read(bean.getRollId());
			}
			handleCompletedJobs(bean, executingUserId);
			// when all jobs are complete; set the roll (if applies) status to exhausted, and remove it from the machine
			if(theRoll != null && RollType.types.PRODUCED.getName().equals(theRoll.getStatus().getId())){
				/*Machine machine = machineDAO.read(theRoll.getMachineId());
				if(machine != null){
					machine.setStatus(lookupDAO.read(MachineStatus.statuses.ON.toString(), MachineStatus.class));
					machineDAO.update(machine);
				}*/
				theRoll.setStatus(lookupDAO.read(RollStatus.statuses.EXHAUSTED.toString(), RollStatus.class));
				//theRoll.setMachineId(null); TODO do we nullify it or keep it for history?
				theRoll.setLastUpdateDate(new Date());
				theRoll.setLastUpdateId(executingUserId);
				rollDAO.update(theRoll);
			}
		} catch (Exception ex) {
			logger.error("Error occurred while setting all the completed jobs from the production dashboard: " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}
	}
	
	/**
	 * Updates the job statuses to complete for the selected jobs and sets the quantity produced;
	 * Also for the press station, a new produced roll should be created and the associated plow folder jobs need to be assigned to it
	 * Also handles the non completed jobs in case jobs were stopped for some reason...
	 */
	public void handleCompletedJobs(Log bean, String executingUserId) throws PersistenceException {
		try {
			List<String> completedJobQtys = bean.getCompletedJobQtys();
			if(completedJobQtys != null && completedJobQtys.size() > 0){
				// For the Press station, a produced roll need to be created, and plow folder jobs associated to each printing job will need to be retrieved 
				// and assigned to the produced roll...
				Roll currentRoll = null;
				Job aJob = this.read(Integer.parseInt(completedJobQtys.get(0).substring(0, completedJobQtys.get(0).indexOf("_"))));
				if(aJob != null && aJob.getRollId() != null){
					currentRoll = rollDAO.read(aJob.getRollId());
				}
				Machine machine = machineDAO.read(bean.getMachineId());
				boolean producedRollCreated = false;
				///Roll producedRoll = new Roll();
				Integer rollOrderingDescendingCounter = completedJobQtys.size();
				Integer qtyRequested = 0;
				for(String iter : completedJobQtys){
					Integer jobIdentifier = Integer.parseInt(iter.substring(0, iter.indexOf("_")));
					Job job = this.read(jobIdentifier);
					//Integer qtyRequested = (job != null) ? (job.getQuantityNeeded() - getOvers(job.getOrder().getOrderPart().getQuantity()) - getUnders(job.getOrder().getOrderPart().getQuantity())) : 0;
					if(job != null){
						qtyRequested = job.getQuantityNeeded();
					}else{
						qtyRequested = 0;
					}
					Integer qtyProduced =  0;
					try{
						qtyProduced = Integer.parseInt(iter.substring(iter.indexOf("_") + 1, iter.lastIndexOf("_")));
					}catch(IndexOutOfBoundsException iobe){
						qtyProduced =  0;
					}catch(NumberFormatException nfe){
						qtyProduced =  0;
					}
					Integer qtyMissing = 0;
					try{
						qtyMissing = Integer.parseInt(iter.substring(iter.lastIndexOf("_") + 1));
					}catch(IndexOutOfBoundsException iobe){
						qtyMissing = 0;
					}catch(NumberFormatException nfe){
						qtyMissing = 0;
					}
					//a job can be totally incomplete
					//or if the produced quantity is less than the expected, then split the job and set the first as complete, the other one needs to be produced
					//else the job is complete
					if(job != null){
						//Roll childRoll = null;
						//if(machine != null && StationCategory.Categories.PRESS.toString().equals(machine.getStationId())){
							// childRoll = rollHandler.getLeftOverRoll(job.getRollId());
						//}
						
						job.setQuantityUnaccountedfor(qtyMissing);
						
						if(qtyProduced > 0){
							if(StationCategory.Categories.PRESS.toString().equals(machine.getStationId()) && !producedRollCreated){
								//producedRoll.setParentRollId(currentRoll != null ? currentRoll.getRollId() : null);
								currentRoll.setRollType(lookupDAO.read(RollType.types.PRODUCED.toString(), RollType.class));
								//producedRoll.setWidth(currentRoll != null ? currentRoll.getWidth() : null);
								//producedRoll.setPaperType(currentRoll != null ? currentRoll.getPaperType() : null);
								currentRoll.setHours(0);
								currentRoll.setMachineId(null);
								currentRoll.setMachineOrdering(null);
								currentRoll.setStatus(lookupDAO.read(RollStatus.statuses.SCHEDULED.toString(), RollStatus.class));
								currentRoll.setLastUpdateDate(new Date());
								currentRoll.setLastUpdateId(executingUserId);
								//producedRoll.setCreatedDate(new Date());
								//producedRoll.setCreatorId(executingUserId);
								rollDAO.update(currentRoll);
								producedRollCreated = true;
								bean.setRollId(currentRoll.getRollId());
								bean.setRollLength(currentRoll.getLength());
								logDAO.update(bean);
							}
							// when job produced is cover press job, and hunkeler job started, activate finishing job if required
							if(StationCategory.Categories.COVERPRESS.toString().equals(machine.getStationId()) &&
									!job.getPartNum().endsWith("J") && !job.getPartNum().endsWith("E")){
								Preference finishingPreference = lookupDAO.read("ACTIVATEBINDERFORSTANLY", Preference.class);
								if(finishingPreference != null && "true".equals(finishingPreference.getName())){
									JobSearchBean jobSearchBean = new JobSearchBean();
									jobSearchBean.setOrderId(job.getOrderId());
									jobSearchBean.setPartFamily((job.getPartNum().endsWith("T") || job.getPartNum().endsWith("C") || job.getPartNum().endsWith("J") || job.getPartNum().endsWith("E")) ? (job.getPartNum().substring(0, job.getPartNum().length() - 1)) : job.getPartNum());
									jobSearchBean.setStationId(StationCategory.Categories.PLOWFOLDER.toString());
									jobSearchBean.setSplitLevel(job.getSplitLevel());
									List<Job> correspondingJobs = jobDAO.readAll(jobSearchBean);
									if(!correspondingJobs.isEmpty()){
										Roll hunkelerRoll = rollDAO.read(correspondingJobs.get(0).getRollId());
										if(hunkelerRoll != null && (RollStatus.statuses.ONPROD.toString().equals(hunkelerRoll.getStatus().getId()) ||
												RollStatus.statuses.EXHAUSTED.toString().equals(hunkelerRoll.getStatus().getId()))){
											jobSearchBean.setStationId(StationCategory.Categories.BINDER.toString());
											jobSearchBean.setStatusesIn(Arrays.asList(JobStatus.JobStatuses.NEW.toString()));
											correspondingJobs = jobDAO.readAll(jobSearchBean);
											if(!correspondingJobs.isEmpty()){
												correspondingJobs.get(0).setJobStatus(lookupDAO.read(JobStatus.JobStatuses.SCHEDULED.toString(), JobStatus.class));
												correspondingJobs.get(0).setLastUpdateDate(new Date());
												correspondingJobs.get(0).setLastUpdateId(executingUserId);
												jobDAO.update(correspondingJobs.get(0));
											}
										}
									}
								}
							}
							if(qtyProduced >= qtyRequested){	//job was fully produced (complete)
								job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.COMPLETE.toString(), JobStatus.class));
								job.setQuantityProduced(qtyProduced);
								/*// Set the order status to Complete if all other order jobs were complete
								JobSearchBean jsb = new JobSearchBean();
								jsb.setOrderId(job.getOrder().getOrderId());
								jsb.setStatusesNotIn(Arrays.asList(JobStatus.JobStatuses.COMPLETE.toString(), JobStatus.JobStatuses.CANCELLED.toString()));
								List<Job> resultJobs = jobDAO.readAll(jsb);
								if(resultJobs.size() == 0){
									job.getOrder().setStatus(Order.OrderStatus.COMPLETE.toString());
									orderDAO.update(job.getOrder());
									// TODO may need to send an email notification
								}*/
								
								//if station is Press, then set the quantity produced, if not then it is set when adding loads
								//if(StationCategory.Categories.PRESS.toString().equals(machine.getStationId())){
								//	job.setQuantityProduced(job.getQuantityProduced() + qtyProduced);
								//}
								
								// Now activate the next station(s) jobs if exist
								
								//if(!isOnFinalStation(job, job.getOrder().getOrderPart().getPart().getCategory().getId())){
									rollOrderingDescendingCounter = activateNextStationJobs(job, currentRoll, rollOrderingDescendingCounter, executingUserId);
								//}
							}else{// job was partially produced, 
								// then split it and update the statuses of both jobs as necessary.
								//if(!StationCategory.Categories.PRESS.toString().equals(machine.getStationId()) || childRoll != null){
								//job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.COMPLETE.toString(), JobStatus.class));
								//}
								job.setQuantityProduced(qtyProduced);
								if(StationCategory.Categories.PRESS.toString().equals(machine.getStationId()) || 
										StationCategory.Categories.COVERPRESS.toString().equals(machine.getStationId())){
									job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.COMPLETE_PARTIAL.toString(), JobStatus.class));
									
									
									// Now activate the next station(s) jobs:
									rollOrderingDescendingCounter = activateNextStationJobs(job, currentRoll, rollOrderingDescendingCounter, executingUserId);
								}else{
									if(qtyRequested - qtyProduced - job.getTotalWaste() > 0 && qtyMissing == 0){
										job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.SCHEDULED.toString(), JobStatus.class));
										job.setMachineId(null);
										job.setMachineOrdering(null);
										
									}else{
										job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.COMPLETE_PARTIAL.toString(), JobStatus.class));
										
										
										// Now activate the next station(s) jobs:
										rollOrderingDescendingCounter = activateNextStationJobs(job, currentRoll, rollOrderingDescendingCounter, executingUserId);
									}
								}
								
								/*
								//if(!StationCategory.Categories.PRESS.toString().equals(machine.getStationId()) || childRoll != null){
								this.splitJobs(job, (int) Math.ceil((float)job.getQuantityNeeded() - job.getQuantityProduced()), job.getTotalWaste(), 
										StationCategory.Categories.PRESS.toString().equals(machine.getStationId()) ? "A" : "S", executingUserId);
								//}*/
								
							}
							
							//job.setQuantityProduced(job.getQuantityProduced() + qtyProduced);
							
							job.setLastUpdateDate(new Date());
							job.setLastUpdateId(executingUserId);
							this.update(job);
							// Now activate the next station(s) jobs:
							//rollOrderingDescendingCounter = activateNextStationJobs(job, producedRoll, rollOrderingDescendingCounter, executingUserId);
							
							//for stations that use Rolls, update the machine current job to follow the flow of jobs running on the machine...
							if(currentRoll != null && currentRoll.getMachineId() != null){
								Machine currentMachine = machineDAO.read(currentRoll.getMachineId());
								if(currentMachine != null){
									currentMachine.setCurrentJob(job);
									if(JobStatus.JobStatuses.SCHEDULED.toString().equals(job.getJobStatus().getId())){
										job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.RUNNING.toString(), JobStatus.class));
									}
									machineDAO.update(currentMachine);
								}
							}
						}else{ //job was not produced, it should go back to the list for production
							// NO MORE RESPECTED: if 'Press' station then leftover roll should first be created, if not then it stays on the original roll
							// if(!StationCategory.Categories.PRESS.toString().equals(machine.getStationId()) || childRoll != null){
								//the job goes back to ready to be scheduled by affecting it to another roll
								if(StationCategory.Categories.PRESS.toString().equals(job.getStationId())){
									job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.NEW.toString(), JobStatus.class));
									job.setFileSentFlag(false);
									job.setRollId(null);
									job.setRollOrdering(null);
									job.setQuantityProduced(0);
									job.setMachineId(null);
									job.setMachineOrdering(null);
									
								}else{
									if(qtyRequested - qtyProduced - job.getTotalWaste() > 0 && qtyMissing == 0){
										job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.SCHEDULED.toString(), JobStatus.class));
										job.setQuantityProduced(0);
										job.setMachineId(null);
										job.setMachineOrdering(null);
									}else{ // totally wasted job
										job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.COMPLETE_PARTIAL.toString(), JobStatus.class));
										//this.splitJobs(job, 0, job.getTotalWaste(), "S", executingUserId);
										/*// crate a new job to compensate the totally wasted job
										Job newJob = new Job();
										newJob.setOrder(job.getOrder());
										newJob.setPart(job.getPart());
										newJob.setStationId(job.getStationId());
										newJob.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.SCHEDULED.toString(), JobStatus.class));
										newJob.setJobPriority(job.getJobPriority());
										newJob.setBinderyPriority(job.getBinderyPriority());
										newJob.setProductionOrdering(job.getProductionOrdering());
										newJob.setHours(job.getHours());
										newJob.setJobType(job.getJobType());
										newJob.setQuantityNeeded(job.getQuantityNeeded());
										newJob.setSplitLevel(job.getSplitLevel() + 1);
										newJob.setCreatedDate(new Date());
										newJob.setCreatorId(executingUserId);
										jobDAO.create(newJob);*/
										job.setQuantityProduced(0);
										
										// Now activate the next station(s) jobs:
										rollOrderingDescendingCounter = activateNextStationJobs(job, currentRoll, rollOrderingDescendingCounter, executingUserId);
									}
								}
								
								job.setLastUpdateDate(new Date());
								job.setLastUpdateId(executingUserId);
								this.update(job);
								if(currentRoll != null && JobStatus.JobStatuses.COMPLETE_PARTIAL.toString().equals(job.getJobStatus().getId())){
									currentRoll.setHours(currentRoll.getHours() - job.getHours());
									currentRoll.setUtilization(Math.round((currentRoll.getHours() * machineHandler.getDefaultMachineSpeed(job.getStationId()) * 100) / currentRoll.getLength()));
									currentRoll.setLastUpdateDate(new Date());
									currentRoll.setLastUpdateId(executingUserId);
									rollDAO.update(currentRoll);
								}
							// }
						}
						// now if this is the final station check for job compensation for the order; if all complete, order is complete
						if(isOnFinalStation(job, null)){
							checkToCompensateOrderJobs(job, executingUserId);
						}

						//for the plow folder, the current roll becomes exhausted when all jobs completed; and becomes scheduled if not
						if(currentRoll != null && StationCategory.Categories.PLOWFOLDER.toString().equals(machine.getStationId())){
							if(Log.LogEvent.COMPLETE.toString().equals(bean.getEvent())){
								if(!RollStatus.statuses.EXHAUSTED.toString().equals(currentRoll.getStatus().getId())){
									currentRoll.setStatus(lookupDAO.read(RollStatus.statuses.EXHAUSTED.toString(), RollStatus.class));
									currentRoll.setLastUpdateDate(new Date());
									currentRoll.setLastUpdateId(executingUserId);
									rollDAO.update(currentRoll);
								}
							}
							if(Log.LogEvent.STOP.toString().equals(bean.getEvent())){
								if(currentRoll.getAllJobsComplete()){// all jobs on roll are complete or partially complete
									currentRoll.setStatus(lookupDAO.read(RollStatus.statuses.EXHAUSTED.toString(), RollStatus.class));
									currentRoll.setLastUpdateDate(new Date());
									currentRoll.setLastUpdateId(executingUserId);
									rollDAO.update(currentRoll);
								}else{
									if(!RollStatus.statuses.SCHEDULED.toString().equals(currentRoll.getStatus().getId())){
										currentRoll.setStatus(lookupDAO.read(RollStatus.statuses.SCHEDULED.toString(), RollStatus.class));
										currentRoll.setMachineId(null);
										currentRoll.setMachineOrdering(null);
										currentRoll.setLastUpdateDate(new Date());
										currentRoll.setLastUpdateId(executingUserId);
										rollDAO.update(currentRoll);
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error occurred while setting the completed jobs from the production dashboard: " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}
	}
	
	/**
	 * tells whether this job is on the last station related to the job's part category specified
	 * @param job, partCategory
	 * @return boolean
	 * @throws PersistenceException 
	 */
	//@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public boolean isOnFinalStation(Job job, String partCategory) throws PersistenceException{
		boolean result = true;
		Preference lastStationPreference = lookupDAO.read("LASTSTATION", Preference.class);
		if(lastStationPreference != null){
			if(lastStationPreference.getName().equals(job.getStationId())){
				return result;
			}
		}
		JobSearchBean jsb = new JobSearchBean();
		jsb.setOrderId(job.getOrderId());
		jsb.setPartFamily((job.getPartNum().endsWith("T") || job.getPartNum().endsWith("C") || job.getPartNum().endsWith("J") || job.getPartNum().endsWith("E")) ? (job.getPartNum().substring(0, job.getPartNum().length() - 1)) : job.getPartNum());
		jsb.setPartCategory(partCategory);
		jsb.setSplitLevel(0);
		for(Job j : this.readAll(jsb)){
			if(j.getProductionOrdering() > job.getProductionOrdering()){
				result = false;
				break;
			}
		}
		return result;
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Job getJobByStation(Job theJob, String stationId) throws PersistenceException{
		Job result = null;
		JobSearchBean jsb = new JobSearchBean();
		jsb.setOrderId(theJob.getOrderId());
		jsb.setPartFamily((theJob.getPartNum().endsWith("T") || theJob.getPartNum().endsWith("C") || theJob.getPartNum().endsWith("J") || theJob.getPartNum().endsWith("E")) ? (theJob.getPartNum().substring(0, theJob.getPartNum().length() - 1)) : theJob.getPartNum());
		jsb.setStationId(stationId);
		jsb.setSplitLevel(theJob.getSplitLevel());
		List<Job> theJobs = this.readAll(jsb);
		if(theJobs != null && !theJobs.isEmpty()){
			result = theJobs.get(0);
		}
		return result;
	}
	
	/**
	 * tells whether this job is on the first station of the order related to the job's part category specified
	 * @param job, partCategory
	 * @return boolean
	 * @throws PersistenceException 
	 */
	//@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public boolean isOnFirstStation(Job job, String partCategory) throws PersistenceException{
		Part pr = partDAO.read(job.getPartNum());
		boolean result = partCategory != null && partCategory.equals(pr.getCategory().getId());
		JobSearchBean jsb = new JobSearchBean();
		jsb.setOrderId(job.getOrderId());
		jsb.setPartFamily((job.getPartNum().endsWith("T") || job.getPartNum().endsWith("C") || job.getPartNum().endsWith("J") || job.getPartNum().endsWith("E")) ? (job.getPartNum().substring(0, job.getPartNum().length() - 1)) : job.getPartNum());
		jsb.setPartCategory(partCategory);
		jsb.setSplitLevel(0);
		for(Job j : this.readAll(jsb)){
			if(j.getProductionOrdering() < job.getProductionOrdering()){
				result = false;
				break;
			}
		}
		return result;
	}
	
	/**
	 * 
	 * Calculates produced quantity by Part
	 * @param orderId 
	 * @return Float 
	 * @throws PersistenceException
	 */
	@Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
	public float getOrderProducedQuantityByPart(Integer orderId, String partNum) throws PersistenceException {
		float result = 0;
		JobSearchBean jsb = new JobSearchBean();
		jsb.setOrderId(orderId);
		jsb.setPartNum(partNum);
		List<Job> orderJobs = readAll(jsb);
		Part part = partDAO.read(partNum);
		for (Job j : orderJobs) {
			if (this.isOnFinalStation(j, part.getCategory().getId())) {
				result += j.getQuantityProduced();
			}
		}
		return result;
	}

	/**
	 * Calculates produced quantity for this order 
	 * @param orderId 
	 * @return Float 
	 * @throws PersistenceException
	 */
	@Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
	public float getOrderProducedQuantity(Integer orderId) throws PersistenceException {
		Order order = orderDAO.read(orderId);
		Float totalproduced = (float) 0;
		for (OrderPart op : order.getOrderParts()) {
			totalproduced += getOrderProducedQuantityByPart(orderId, op.getPart().getPartNum());
		}
		return totalproduced;
	}
	
	/**
	 * A method that checks to see if compensation jobs need to be created to satisfy the order.
	 * If the order is already satisfied, then it is set to complete
	 * @param job, executingUserId
	 * @throws PersistenceException 
	 */
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void checkToCompensateOrderJobs(Job job, String executingUserId) throws PersistenceException{
		// first make sure all jobs from last station are complete or complete partially
		boolean checkToCompensate = true;
		JobSearchBean jsb = new JobSearchBean();
		jsb.setOrderId(job.getOrderId());
		jsb.setPartFamily((job.getPartNum().endsWith("T") || job.getPartNum().endsWith("C") || job.getPartNum().endsWith("J") || job.getPartNum().endsWith("E")) ? (job.getPartNum().substring(0, job.getPartNum().length() - 1)) : job.getPartNum());
		jsb.setStationId(job.getStationId());
		List<Job> lastStationJobs = this.readAll(jsb);
		Float lastStationQtyProduced = (float) 0;
		Integer qtyNeededForCompensation = 0;
		Integer textQtyInStock = 0;
		Integer coverQtyInStock = 0;
		Integer maxSpliLevel = job.getSplitLevel();
		for(Job j : lastStationJobs){
			if(j.getSplitLevel() > maxSpliLevel){
				maxSpliLevel = j.getSplitLevel();
			}
			if(!JobStatus.JobStatuses.COMPLETE.toString().equals(j.getJobStatus().getId()) && 
			   !JobStatus.JobStatuses.COMPLETE_PARTIAL.toString().equals(j.getJobStatus().getId()) &&
			   !JobStatus.JobStatuses.CANCELLED.toString().equals(j.getJobStatus().getId())){
				checkToCompensate = false;
			}
			lastStationQtyProduced += j.getQuantityProduced();
		}
		if(checkToCompensate){
			// check on the order qty minimum satisfaction
			Order or = orderDAO.read(job.getOrderId());
			OrderPart op = or.getOrderPartByPartNum(job.getPartNum());
			if(op != null && lastStationQtyProduced < op.getQuantityMin()){
				qtyNeededForCompensation = op.getQuantityMax() - (int) Math.floor(lastStationQtyProduced);
				//see now how much already available in stock for text and cover
				for(String s : op.getPart().getChildren()){
					if(s.endsWith("T")){
						textQtyInStock = calculateQtyInStock(job, Part.PartsCategory.TEXT.toString(), true);
					}else if(s.endsWith("C")){
						coverQtyInStock = calculateQtyInStock(job, Part.PartsCategory.COVER.toString(), true);
					}
				}
				// go ahead and create the compensation jobs:
				jsb = new JobSearchBean();
				jsb.setOrderId(job.getOrderId());
				jsb.setPartFamily((job.getPartNum().endsWith("T") || job.getPartNum().endsWith("C") || job.getPartNum().endsWith("J") || job.getPartNum().endsWith("E")) ? (job.getPartNum().substring(0, job.getPartNum().length() - 1)) : job.getPartNum());
				jsb.setSplitLevel(0);
				//jsb.setPartNum(job.getPart().getPartNum());
				Set<Job> originalJobs = new HashSet<Job>(this.readAll(jsb));
				for(Job b : originalJobs){
				  if(!StationCategory.Categories.SHIPPING.toString().equals(b.getStationId())){
					Part pr = partDAO.read(b.getPartNum());
					if((Part.PartsCategory.TEXT.toString().equals(pr.getCategory().getId()) && (qtyNeededForCompensation - textQtyInStock) > 0) ||
					   (Part.PartsCategory.COVER.toString().equals(pr.getCategory().getId()) && (qtyNeededForCompensation - coverQtyInStock) > 0) ||
					   (Part.PartsCategory.BOOK.toString().equals(pr.getCategory().getId()) && qtyNeededForCompensation > 0)){
						Job newJob = new Job();
						newJob.setOrderId(b.getOrderId());
						newJob.setPartNum(b.getPartNum());
						if(Part.PartsCategory.TEXT.toString().equals(pr.getCategory().getId())){
							newJob.setQuantityNeeded(qtyNeededForCompensation - textQtyInStock);
						}else if(Part.PartsCategory.COVER.toString().equals(pr.getCategory().getId())){
							newJob.setQuantityNeeded(qtyNeededForCompensation - coverQtyInStock);
						}else{
							newJob.setQuantityNeeded(qtyNeededForCompensation);
						}
						newJob.setSplitLevel(maxSpliLevel + 1);
						newJob.setStationId(b.getStationId());
						newJob.setJobName("Auto_Re_Order"); // this is to distinguish these jobs and know they are compensation jobs
						newJob.setDueDate(b.getDueDate());
						newJob.setPartColor(b.getPartColor());
						newJob.setPartPaperId(b.getPartPaperId());
						newJob.setProductionMode(b.getProductionMode());
						newJob.setPartTitle(b.getPartTitle());
						newJob.setPartIsbn(b.getPartIsbn());
						newJob.setPartCategory(b.getPartCategory());
						
						// as this is a compensation job list, set the cover press job to scheduled in case there is
						// no compensation job for press to produce, in such case there is no action to set the
						// cover press job to scheduled automatically so we'd better set that manually in this stage.
						// Note: this seems to be causing side effect issues so we're canceling it for now
						//if(StationCategory.Categories.COVERPRESS.toString().equals(b.getStationId())){
						//	newJob.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.SCHEDULED.toString(), JobStatus.class));
						//}else{
							newJob.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.NEW.toString(), JobStatus.class));
						//}
						
						newJob.setJobPriority(b.getJobPriority());
						newJob.setProductionOrdering(b.getProductionOrdering());
						if(StationCategory.Categories.PRESS.toString().equals(b.getStationId())){
							//newJob.setHours(orderHandler.calculatePrintingHours(qtyNeededForCompensation, b.getPart().getPagesCount()));
							Float[] jobCalc = calculateJobHoursAndLength(pr, newJob.getQuantityNeeded(), (float) 0.0); 
							newJob.setHours(jobCalc[0]);
							if(jobCalc[2] == 2){
								newJob.setJobType(lookupDAO.read(JobType.JobTypes.PRINTING_2UP.toString(), JobType.class));
							}else if(jobCalc[2] == 3){
								newJob.setJobType(lookupDAO.read(JobType.JobTypes.PRINTING_3UP.toString(), JobType.class));
							}else if(jobCalc[2] == 4){
								newJob.setJobType(lookupDAO.read(JobType.JobTypes.PRINTING_4UP.toString(), JobType.class));
							}
						}else if(StationCategory.Categories.PLOWFOLDER.toString().equals(b.getStationId())){
							newJob.setJobType(lookupDAO.read(JobType.JobTypes.PRINTING.toString(), JobType.class));
							newJob.setHours(calculateJobHoursAndLength(pr, newJob.getQuantityNeeded(), (float) 0.0)[1] / machineHandler.getDefaultMachineSpeed(b.getStationId()));
						}else{
							newJob.setHours(newJob.getQuantityNeeded() / machineHandler.getDefaultMachineSpeed(b.getStationId()));
						}
						if(Part.PartsCategory.BOOK.toString().equals(pr.getCategory().getId())){
							newJob.setJobType(lookupDAO.read(JobType.JobTypes.BINDING.toString(), JobType.class));
						}else if(Part.PartsCategory.COVER.toString().equals(pr.getCategory().getId())){
							newJob.setJobType(lookupDAO.read(JobType.JobTypes.PRINTING.toString(), JobType.class));
						}
						newJob.setBinderyPriority(b.getBinderyPriority());
						newJob.setCreatedDate(new Date());
						newJob.setCreatorId(executingUserId);
						jobDAO.create(newJob);
					}
				  }
				}
				
			}else{//order satisfied, set it to complete
				boolean allcomplete = true;
				jsb = new JobSearchBean();
				jsb.setOrderId(job.getOrderId());
				for(Job j : readAll(jsb)){
					if(!JobStatus.JobStatuses.COMPLETE.toString().equals(j.getJobStatus().getId()) && 
							   !JobStatus.JobStatuses.COMPLETE_PARTIAL.toString().equals(j.getJobStatus().getId()) &&
							   !JobStatus.JobStatuses.CANCELLED.toString().equals(j.getJobStatus().getId())){
						allcomplete = false;
						break;
					}
				}
				if(allcomplete){
					or = orderDAO.read(job.getOrderId());
					or.setStatus(Order.OrderStatus.COMPLETE.toString());
					orderDAO.update(or);
					/*
					String[] updateOrderStatus = {job.getOrder().getOrderId()+"", job.getOrder().getStatus()};
					OrderHandler.broadcastStatus(updateOrderStatus);
					*/
					
					Map<String, Object> ssePayload=new HashMap<String, Object>();
					ssePayload.put("orderId", job.getOrderId());
					ssePayload.put("status", or.getStatus());
					com.epac.cap.sse.beans.Event event=new com.epac.cap.sse.beans.Event(EventTarget.OrderStatus, false, null, ssePayload);
					notificationService.broadcast(event);
				}
				// TODO may need to send an email notification
			}
		}
	}
	
	/**
	 * Calculates how much in stock available (produced in the previous round) from that part category for this order
	 * @param job, 
	 * @param includeCurrentBranch, if true adds the qty in stock from the job's branch of jobs 
	 * @return Integer
	 * @throws PersistenceException
	 */
	//@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Integer calculateQtyInStock(Job job, String partCategory, boolean includeCurrentBranch) throws PersistenceException{
		Integer result = 0;
		//if the job is not a binding job, return 0
		Part pr = partDAO.read(job.getPartNum());
		if(!pr.getTopParts().isEmpty()){
			return 0;
		}
		// calculate total produced qty from last station the text/cover went through
		Float totalQtyProducedFromLastStation = (float) 0;
		Integer totalQtyUsedFromBinderStation = 0;
		JobSearchBean jsb = new JobSearchBean();
		jsb.setOrderId(job.getOrderId());
		jsb.setPartFamily((job.getPartNum().endsWith("T") || job.getPartNum().endsWith("C") || job.getPartNum().endsWith("J") || job.getPartNum().endsWith("E")) ? (job.getPartNum().substring(0, job.getPartNum().length() - 1)) : job.getPartNum());
		jsb.setPartCategory(partCategory);
		for(Job j : this.readAll(jsb)){
			if(this.isOnFinalStation(j, partCategory)){
				if(includeCurrentBranch || !job.getSplitLevel().equals(j.getSplitLevel())){
					totalQtyProducedFromLastStation += j.getQuantityProduced();
				}
			}
		}
		// calculate qty used on first station 
		jsb = new JobSearchBean();
		jsb.setOrderId(job.getOrderId());
		jsb.setPartFamily((job.getPartNum().endsWith("T") || job.getPartNum().endsWith("C") || job.getPartNum().endsWith("J") || job.getPartNum().endsWith("E")) ? (job.getPartNum().substring(0, job.getPartNum().length() - 1)) : job.getPartNum());
		jsb.setPartCategory(pr.getCategory().getId());
		for(Job j : this.readAll(jsb)){
			if(this.isOnFirstStation(j, pr.getCategory().getId())){
				if(includeCurrentBranch || !job.getSplitLevel().equals(j.getSplitLevel())){
					totalQtyUsedFromBinderStation = totalQtyUsedFromBinderStation + j.getQuantityNeeded() + (int) Math.ceil(j.getTotalWaste());  // TODO may be use j.getQuantityProduced() instead?
				}
			}
		}
		result = (int) Math.floor(totalQtyProducedFromLastStation - totalQtyUsedFromBinderStation);
		return result > 0 ? result : 0;
		//return result;
	}
	
	/**
	 * Splits the jobs of same family as the original job and give them new quantity
	 * Updates the original job quantities to the difference between original qty and new qty
	 * If there is waste then recreate the jobs needed to compensate the waste.
	 * Sets the split level of the new jobs to be the max of all job levels of the order plus one
	 * cascadeFlag: 
	 * 'A' for splitting All jobs (case of the Press job); 
	 * 'S' for Subsequent jobs of Same part; 
	 * 'O' for only this job.
	 */
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void splitJobs(Job job, Integer newQuantity, Float waste, String cascadeFlag, String executingUserId) throws PersistenceException{
		try {
			Integer maxLevel = job.getSplitLevel();
			Integer wasteQty = (int) Math.ceil(waste);
			// If Press or CoverPress the waste is not taken into account as what matters is the qty produced and the rest has to be re-produced
			if(StationCategory.Categories.COVERPRESS.toString().equals(job.getStationId()) || StationCategory.Categories.PRESS.toString().equals(job.getStationId())){
				wasteQty = 0;
			}
			JobSearchBean jsb = new JobSearchBean();
			jsb.setOrderId(job.getOrderId());
			jsb.setPartFamily((job.getPartNum().endsWith("T") || job.getPartNum().endsWith("C") || job.getPartNum().endsWith("J") || job.getPartNum().endsWith("E")) ? (job.getPartNum().substring(0, job.getPartNum().length() - 1)) : job.getPartNum());
			for(Job theJob : readAll(jsb)){
				if(theJob.getSplitLevel() > maxLevel){
					maxLevel = theJob.getSplitLevel();
				}
			}
			jsb.setSplitLevel(job.getSplitLevel());
			//List<Job> originalJobs = readAll(jsb);
			Set<Job> originalJobs = new HashSet<Job>(this.readAll(jsb));
			for(Job theJob : originalJobs){
			  if(!StationCategory.Categories.SHIPPING.toString().equals(theJob.getStationId())){
				Part pr = partDAO.read(theJob.getPartNum());
				if("A".equals(cascadeFlag) || 
				  ("S".equals(cascadeFlag) && theJob.getPartNum().equals(job.getPartNum()) && theJob.getProductionOrdering() >= job.getProductionOrdering()) ||
				  ("O".equals(cascadeFlag) && theJob.getJobId().equals(job.getJobId()))){
					if(newQuantity - wasteQty > 0){
						Job newJob = new Job();
						newJob.setOrderId(theJob.getOrderId());
						newJob.setPartNum(theJob.getPartNum());
						newJob.setStationId(theJob.getStationId());
						
						newJob.setJobPriority(theJob.getJobPriority());
						newJob.setBinderyPriority(theJob.getBinderyPriority());
						newJob.setProductionOrdering(theJob.getProductionOrdering());
						newJob.setHours(theJob.getHours() * (newQuantity - wasteQty) / (float)theJob.getQuantityNeeded());
						newJob.setJobType(theJob.getJobType());
						newJob.setQuantityNeeded(newQuantity - wasteQty);
						
						newJob.setDueDate(theJob.getDueDate());
						newJob.setPartColor(theJob.getPartColor());
						newJob.setPartPaperId(theJob.getPartPaperId());
						newJob.setProductionMode(theJob.getProductionMode());
						newJob.setPartTitle(theJob.getPartTitle());
						newJob.setPartIsbn(theJob.getPartIsbn());
						newJob.setPartCategory(theJob.getPartCategory());
						
						if(StationCategory.Categories.PRESS.toString().equals(job.getStationId())){
							newJob.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.NEW.toString(), JobStatus.class));
						}else if(StationCategory.Categories.PLOWFOLDER.toString().equals(job.getStationId())) {
						    if(StationCategory.Categories.PLOWFOLDER.toString().equals(theJob.getStationId())){
								newJob.setRollId(theJob.getRollId());
								newJob.setRollOrdering(theJob.getRollOrdering());
								newJob.setMachineId(theJob.getMachineId());
								newJob.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.SCHEDULED.toString(), JobStatus.class));
						    }
						}else if(job.getStationId().equals(theJob.getStationId())){
							newJob.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.SCHEDULED.toString(), JobStatus.class));
						}else{
							newJob.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.NEW.toString(), JobStatus.class));
						}
						if("O".equals(cascadeFlag)){// if this is a one level split meaning splitting only this job then give same split level as this job
							newJob.setSplitLevel(job.getSplitLevel());
						}else{// otherwise give a new split level for the group of newly created jobs
							newJob.setSplitLevel(maxLevel + 1);
						}
						newJob.setCreatedDate(new Date());
						newJob.setCreatorId(executingUserId);
						jobDAO.create(newJob);
					}
					//else{
						//theJob.setQuantityNeeded(theJob.getQuantityNeeded() - newQuantity - wasteQty);
					//}
					
					if(newQuantity == 0){
						theJob.setQuantityNeeded(theJob.getQuantityNeeded() - wasteQty);
					}else{
						theJob.setQuantityNeeded(theJob.getQuantityNeeded() - newQuantity);
					}
					
					//theJob.setHours(theJob.getHours() - newJob.getHours());
					//theJob.setHours(theJob.getHours() - (theJob.getHours() * newQuantity / theJob.getQuantityNeeded()));
					if(StationCategory.Categories.PRESS.toString().equals(theJob.getStationId())){
						//theJob.setHours((float)(theJob.getQuantityNeeded() * job.getPart().getPagesCount()) / (float)60000);
						//theJob.setHours(orderHandler.calculatePrintingHours(theJob.getQuantityNeeded(), job.getPart().getPagesCount()));
						//NDispatcher.getDispatcher().publish(Topics.topic("cap/events/PrintingTimeCalculation"), Arrays.asList(job.getPart(), theJob.getQuantityNeeded(), 0.0));
						
						//PrintingTimeCalculator p = new PrintingTimeCalculator();
						//p.handler(Arrays.asList(job.getPart(), theJob.getQuantityNeeded(), 0.0));
						//theJob.setHours(PrintingTimeCalculator.timePerJob);
						
						theJob.setHours(calculateJobHoursAndLength(pr, theJob.getQuantityNeeded(), (float) 0.0)[0]);
						// TODO figure out how for the other station jobs
					}else if(StationCategory.Categories.PLOWFOLDER.toString().equals(theJob.getStationId())){
						theJob.setHours(calculateJobHoursAndLength(pr, theJob.getQuantityNeeded(), (float) 0.0)[1] / machineHandler.getDefaultMachineSpeed(theJob.getStationId()));
					}else{
						theJob.setHours(theJob.getQuantityNeeded() / machineHandler.getDefaultMachineSpeed(theJob.getStationId()));
					}
					
					theJob.setLastUpdateDate(new Date());
					theJob.setLastUpdateId(executingUserId);
					jobDAO.update(theJob);
				}
			  }
			}
			// If there is waste, recreate all jobs (for the part or if it is a top part for both parent and children)
			// with the qty wasted to compensate this waste
			// Note: if the station is the Press or CoverPress then ignore the waste as it is being considered when specifying the qty produced.
			if(wasteQty > 0){
				jsb.setSplitLevel(0);
				//originalJobs = readAll(jsb);
				originalJobs = new HashSet<Job>(this.readAll(jsb));
				for(Job theJob : originalJobs){
				  if(!StationCategory.Categories.SHIPPING.toString().equals(theJob.getStationId())){
					Part pr = partDAO.read(job.getPartNum());
					if(pr.getTopParts().isEmpty() ||
							theJob.getPartNum().equals(job.getPartNum())){
						Job newJob = new Job();
						newJob.setOrderId(theJob.getOrderId());
						newJob.setPartNum(theJob.getPartNum());
						newJob.setStationId(theJob.getStationId());
						
						newJob.setDueDate(theJob.getDueDate());
						newJob.setPartColor(theJob.getPartColor());
						newJob.setPartPaperId(theJob.getPartPaperId());
						newJob.setProductionMode(theJob.getProductionMode());
						newJob.setPartTitle(theJob.getPartTitle());
						newJob.setPartIsbn(theJob.getPartIsbn());
						newJob.setPartCategory(theJob.getPartCategory());
						
						if(!pr.getTopParts().isEmpty() && StationCategory.Categories.COVERPRESS.toString().equals(theJob.getStationId())){
							newJob.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.SCHEDULED.toString(), JobStatus.class));
						}else{
							newJob.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.NEW.toString(), JobStatus.class));
						}
						newJob.setJobPriority(theJob.getJobPriority());
						newJob.setBinderyPriority(theJob.getBinderyPriority());
						newJob.setProductionOrdering(theJob.getProductionOrdering());
						newJob.setHours(theJob.getHours() * wasteQty / (float)theJob.getQuantityNeeded());
						newJob.setJobType(theJob.getJobType());
						newJob.setQuantityNeeded(wasteQty);
						newJob.setSplitLevel(maxLevel + 2);
						newJob.setCreatedDate(new Date());
						newJob.setCreatorId(executingUserId);
						jobDAO.create(newJob);
					}
				  }
				}
			}
		} catch (Exception e) {
			logger.error("Error occurred splitting job " + job.getJobId() + e.getMessage(),e);
			throw new PersistenceException(e);
		}
	}
	
	/**
	 * Calculates time to take for this job to finish
	 * @param job
	 * @return Float
	 * @throws PersistenceException 
	 */
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Float[] calculateJobHoursAndLength(Part part, Integer qty, float paperWidth) throws PersistenceException{
		//Elamine: below code replaced by this line, It is not ALLOWED to access component attributes statically
		
		Float[] result = printingTimeCalculator.calculateJobHoursAndLength(Arrays.asList(part, qty, paperWidth));
		/*
		printingTimeCalculator.handle(Arrays.asList(part, qty, paperWidth));
		Float[] result = new Float[] {PrintingTimeCalculator.timePerJob,
									  PrintingTimeCalculator.paperPerJob,
									  (float) PrintingTimeCalculator.impositionScheme};
									  */
		return result;
	}
	
	/**
	 * Finds the next jobs to run for production after this current job 
	 * @param job
	 * @return List<Job>
	 * @throws PersistenceException 
	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Job> findNextStationJobs(Job job, boolean onlyNew) throws PersistenceException{
		Integer maxProductionOrdering = 0;
		for(DefaultStation ds : stationDAO.readAllDefault(null)){
			if(maxProductionOrdering < ds.getProductionOrdering()){
				maxProductionOrdering = ds.getProductionOrdering();
			}
		}
		List<Job> nextJobs = new ArrayList<Job>();
		JobSearchBean jobSearchBean = new JobSearchBean();
		jobSearchBean.setOrderId(job.getOrder() != null ? job.getOrder().getOrderId() : null);
		jobSearchBean.setSplitLevel(job.getSplitLevel());
		jobSearchBean.setProductionOrdering(job.getProductionOrdering());
		if(onlyNew){
			jobSearchBean.setStatus(JobStatus.JobStatuses.NEW.toString());
		}
		do{
			jobSearchBean.setProductionOrdering(jobSearchBean.getProductionOrdering() + 1);
			nextJobs = this.readAll(jobSearchBean);
		}while(jobSearchBean.getProductionOrdering() <= maxProductionOrdering && nextJobs.isEmpty());
		// make sure any retrieved job has its previous job status for same sub part as 'complete'
		List<Job> result = new ArrayList<Job>();
		for(Job iterJob : nextJobs){
			jobSearchBean.setProductionOrdering(iterJob.getProductionOrdering() - 1);
			jobSearchBean.setPartNum(iterJob.getPart().getPartNum());
			jobSearchBean.setStatus(null);
			List<Job> iterResult = this.readAll(jobSearchBean);
			if(iterResult.isEmpty() || !onlyNew || JobStatus.JobStatuses.COMPLETE.toString().equals(iterResult.get(0).getJobStatus().getId())){
				result.add(iterJob);
			}
		}
		return result;
	}*/
	
	/**
	 * Finds the next jobs to run for production after this current job; this is a more correct version than the other one...
	 * @param job
	 * @return List<Job>
	 * @throws PersistenceException 
	 */
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Job> findNextJobs(Job job, boolean onlyNew) throws PersistenceException{
		Integer maxProductionOrdering = 0;
		for(DefaultStation ds : stationDAO.readAllDefault(null)){
			if(maxProductionOrdering < ds.getProductionOrdering()){
				maxProductionOrdering = ds.getProductionOrdering();
			}
		}
		Set<Job> nextJobs = new HashSet<Job>();
		List<Job> tmpJobs = new ArrayList<Job>();
		JobSearchBean jobSearchBean = new JobSearchBean();
		jobSearchBean.setOrderId(job.getOrderId());
		jobSearchBean.setPartFamily((job.getPartNum().endsWith("T") || job.getPartNum().endsWith("C") || job.getPartNum().endsWith("J") || job.getPartNum().endsWith("E")) ? (job.getPartNum().substring(0, job.getPartNum().length() - 1)) : job.getPartNum());
		jobSearchBean.setSplitLevel(job.getSplitLevel());
		jobSearchBean.setProductionOrdering(job.getProductionOrdering());
		//if(onlyNew){
			//jobSearchBean.setStatus(JobStatus.JobStatuses.NEW.toString());
		//}
		Part pr = partDAO.read(job.getPartNum());
		// Special case for end sheet printing job: next station is the end sheet station.
		if(Part.PartsCategory.ENDSHEET.toString().equals(pr.getCategory().getId())){
			jobSearchBean.setProductionOrdering(null);
			for(Job j : this.readAll(jobSearchBean)){
				if(j.getStationId().equals(StationCategory.Categories.ENDSHEET.toString())){
					return Arrays.asList(j);
				}
			}
		}
		do{
			jobSearchBean.setProductionOrdering(jobSearchBean.getProductionOrdering() + 1);
			tmpJobs = this.readAll(jobSearchBean);
			nextJobs.addAll(tmpJobs);
			if(!StationCategory.Categories.PRESS.toString().equals(job.getStationId()) && pr != null && !pr.getTopParts().isEmpty()){
				for(Job jb : tmpJobs){
					Part p = partDAO.read(jb.getPartNum());
					if(!p.getTopParts().isEmpty() && !p.getPartNum().equals(job.getPartNum())){
						nextJobs.remove(jb);
					}
				}
			}
		}while(jobSearchBean.getProductionOrdering() <= maxProductionOrdering && nextJobs.isEmpty());
		
		List<Job> result = new ArrayList<Job>();
		for(Job iterJob : nextJobs){
			pr = partDAO.read(iterJob.getPartNum());
			if(StationCategory.Categories.PRESS.toString().equals(job.getStationId())){
				result.add(iterJob);
			}else if(job.getPartNum() != null && iterJob.getPartNum() != null && job.getPartNum().equals(iterJob.getPartNum())){
				result.add(iterJob);
			}else if(iterJob.getPartNum() != null && pr.getTopParts().isEmpty()){
				result.add(iterJob);
			}
		}
		return result;
	}
	
	/**
	 * Activates the next jobs to run for production after this current job 
	 * @param job, producedRoll, executingUserId
	 * @throws PersistenceException 
	 */
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public Integer activateNextStationJobs(Job job, Roll producedRoll, Integer rollOrderingDescendingCounter, String executingUserId) throws PersistenceException{
		//first save the job as it was changed before calling this method; this is so we can get the refreshed status and produced quantities
		jobDAO.update(job);
		
		// only activate next station jobs if all order jobs of this job's station are complete or partial complete
		boolean okToActivate = true;
		Float qtyNeededForTheJobToActivate = (float) 0;
		JobSearchBean jobSearchBean = new JobSearchBean();
		jobSearchBean.setOrderId(job.getOrderId());
		jobSearchBean.setPartFamily((job.getPartNum().endsWith("T") || job.getPartNum().endsWith("C") || job.getPartNum().endsWith("J") || job.getPartNum().endsWith("E")) ? (job.getPartNum().substring(0, job.getPartNum().length() - 1)) : job.getPartNum());
		jobSearchBean.setStationId(job.getStationId());
		jobSearchBean.setSplitLevel(job.getSplitLevel());// only for the jobs that are from same group as this one
		for(Job j : this.readAll(jobSearchBean)){
			if(!JobStatus.JobStatuses.COMPLETE.toString().equals(j.getJobStatus().getId()) &&
			   !JobStatus.JobStatuses.COMPLETE_PARTIAL.toString().equals(j.getJobStatus().getId()) &&
			   !JobStatus.JobStatuses.CANCELLED.toString().equals(j.getJobStatus().getId())){
				okToActivate = false;
			}
			qtyNeededForTheJobToActivate += j.getQuantityProduced();
		}
		
		if(okToActivate){
			for(Job nextJob : findNextJobs(job, true)){
				Station nextStation = stationDAO.read(nextJob.getStationId());
				if(nextStation != null && StationCategory.Categories.SHIPPING.toString().equals(nextStation.getStationId())){
					break;
				}
				// if next job already activated, skip it
				if(!JobStatus.JobStatuses.NEW.toString().equals(nextJob.getJobStatus().getId())){
					continue;
				}
				// If current job is the Press job, then find the associated plow folder job and assign it to the producedRoll
				// also special case: when there is end sheet that needs to be activated along with the hunkeler job, activate it as well
				if(nextStation != null && StationCategory.Categories.PLOWFOLDER.toString().equals(nextStation.getStationCategoryId())){
					nextJob.setRollId(producedRoll.getRollId());
					//set the roll ordering 'descending' (and increment it for the other jobs):
					nextJob.setRollOrdering(rollOrderingDescendingCounter);
					rollOrderingDescendingCounter--;
					/*for(Job iterJob : producedRoll.getJobs()){
						if(iterJob.getRollOrdering() != null){
							iterJob.setRollOrdering(iterJob.getRollOrdering() + 1);
						}else{
							iterJob.setRollOrdering(0);
						}
					}*/
					if(producedRoll != null && producedRoll.getRollId() != null){
						producedRoll.setUtilization(100);
						// TODO length of this roll ?
						// TODO try figure the length using the counter feet...
						// TODO, the hours, should be the sum of all the job hours
						producedRoll.setHours(producedRoll.getHours() + nextJob.getHours());
						producedRoll.setLength(Math.round(producedRoll.getHours() * machineHandler.getDefaultMachineSpeed(StationCategory.Categories.PLOWFOLDER.toString())));
						
						rollDAO.update(producedRoll);
					}
					Preference activateEndSheetWithHunkelerPreference = lookupDAO.read("ACTIVATENDSHEETWITHUNKELER", Preference.class);
					if(activateEndSheetWithHunkelerPreference != null && "true".equals(activateEndSheetWithHunkelerPreference.getName())){
						Job endSheetJob = getJobByStation(nextJob, StationCategory.Categories.ENDSHEET.toString());
						if(endSheetJob != null && JobStatus.JobStatuses.NEW.toString().equals(endSheetJob.getJobStatus().getId())){
							if(endSheetJob.getQuantityNeeded() > qtyNeededForTheJobToActivate){
								endSheetJob.setQuantityNeeded((int)Math.floor(qtyNeededForTheJobToActivate));
							}
							endSheetJob.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.SCHEDULED.toString(), JobStatus.class));
							endSheetJob.setLastUpdateDate(new Date());
							endSheetJob.setLastUpdateId(executingUserId);
							this.update(endSheetJob);
						}
					}
				}
				//special case: if no binder job but only drill job, make sure the drill job gets activated through the 'Text' job not the 'Cover' job
				/*Preference finishingActivationByTextPreference = lookupDAO.read("ACTIVATEFINISHINGBYTEXT", Preference.class);
				if(finishingActivationByTextPreference != null && "true".equals(finishingActivationByTextPreference.getName())){
					if(Part.PartsCategory.BOOK.toString().equals(nextJob.getPart().getCategory().getId()) 
					   && nextStation != null && !StationCategory.Categories.BINDER.toString().equals(nextStation.getStationCategoryId())
					   && Part.PartsCategory.COVER.toString().equals(job.getPart().getCategory().getId())){
						continue;
					}
				}*/
				
				// if next job to activate is the binder, make sure text and cover are both ready before activating the binder job
				// but do this only if ACTIVATEBINDERFORSTANLY is not true
				Preference finishingPreference = lookupDAO.read("ACTIVATEBINDERFORSTANLY", Preference.class);
				boolean finishingActivationEarly = false;
				if(finishingPreference != null && "true".equals(finishingPreference.getName())){
					finishingActivationEarly = true;
				}
				if(nextStation != null && this.isOnFirstStation(nextJob, Part.PartsCategory.BOOK.toString()) &&
						!finishingActivationEarly){
					    JobPrevious prevBinderJobs = findPrevJobData(nextJob.getJobId());
					    if(prevBinderJobs.getCoverStatus() != null && !JobStatus.JobStatuses.COMPLETE.toString().equalsIgnoreCase(prevBinderJobs.getCoverStatus()) &&
								   !JobStatus.JobStatuses.COMPLETE_PARTIAL.toString().equalsIgnoreCase(prevBinderJobs.getCoverStatus()) &&
								   !JobStatus.JobStatuses.CANCELLED.toString().equalsIgnoreCase(prevBinderJobs.getCoverStatus())){
							continue;
						}
						if(prevBinderJobs.getTextStatus() != null && !JobStatus.JobStatuses.COMPLETE.toString().equalsIgnoreCase(prevBinderJobs.getTextStatus()) &&
								   !JobStatus.JobStatuses.COMPLETE_PARTIAL.toString().equalsIgnoreCase(prevBinderJobs.getTextStatus()) &&
								   !JobStatus.JobStatuses.CANCELLED.toString().equalsIgnoreCase(prevBinderJobs.getTextStatus())){
							continue;
						}
				}else{
					//nextJob.setQuantityNeeded(Math.round(job.getQuantityProduced())); TODO is this ok to do? looks like no; case of binder job qty changed which should not...
					if(nextJob.getQuantityNeeded() > qtyNeededForTheJobToActivate){
						nextJob.setQuantityNeeded((int)Math.floor(qtyNeededForTheJobToActivate));
					}
					// TODO what about the hours
				}
				nextJob.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.SCHEDULED.toString(), JobStatus.class));
				nextJob.setLastUpdateDate(new Date());
				nextJob.setLastUpdateId(executingUserId);
				this.update(nextJob);
				//producedRoll.setHours(producedRoll.getHours() + nextJob.getHours());
			}
		}
		return rollOrderingDescendingCounter;
	}
	
	/**
	 * Gather the data needed for a binder job (or other job types that get product from previous station)
	 * to display the status of each previous's job part data.
	 */
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public JobPrevious findPrevJobData(Integer currentJobId) throws PersistenceException{
		Job currentJob = null;
		boolean isEndSheetPrinted = false;
		JobPrevious result = new JobPrevious();
		currentJob = read(currentJobId);
		if(currentJob != null){
			// calculate the quantities in stock: we need to account for case of the prev job doesn't exist but there is qty in stock that should be represented in the result ....
			// special case: end sheet job might use text and end sheets, so take this into consideration
			Part pr = partDAO.read(currentJob.getPartNum());
			List<Job> rootJobs = null;
			if(StationCategory.Categories.ENDSHEET.toString().equals(currentJob.getStationId())){
				JobSearchBean jsb = new JobSearchBean();
				jsb.setOrderId(currentJob.getOrderId());
				jsb.setPartFamily(pr.getPartNum().substring(0, pr.getPartNum().length()-1));
				rootJobs = readAll(jsb);
				for(Job j :rootJobs ){
					if(Part.PartsCategory.ENDSHEET.toString().equals(j.getPartCategory())){
						isEndSheetPrinted = true;
						break;
					}
				}
			}
			Integer qtyInStockText = calculateQtyInStock(currentJob, Part.PartsCategory.TEXT.toString(), false);
			Integer qtyInStockCover = calculateQtyInStock(currentJob, isEndSheetPrinted ? Part.PartsCategory.ENDSHEET.toString() : Part.PartsCategory.COVER.toString(), false);
			
			result.setMachineId(currentJob.getMachineId());
			rootJobs = null;
			List<Job> rootJobsToInclude = new ArrayList<Job>();
			List<String> parts = new ArrayList<String>();
			boolean isBindingStage = isEndSheetPrinted || this.isOnFirstStation(currentJob, Part.PartsCategory.BOOK.toString());
			
			if(isEndSheetPrinted){
				parts.add(currentJob.getPartNum());
				parts.add(currentJob.getPartNum().endsWith("T") ? currentJob.getPartNum().substring(0, currentJob.getPartNum().length() - 1).concat("E") : currentJob.getPartNum().concat("E"));
			}else if(isBindingStage){
				for(String s : pr.getChildren()){
					if(!s.endsWith("E") && !s.endsWith("J")){
						parts.add(s);
					}
				}
			}else{
				parts.add(currentJob.getPartNum());
			}
			result.setBindingStage(isBindingStage);
			for(String iterPart : parts){
				Part part1 = partDAO.read(iterPart);
				Float totalQty = (float) 0;
				if(part1 != null){
					JobSearchBean jsb = new JobSearchBean();
					jsb.setOrderId(currentJob.getOrderId());
					jsb.setPartNum(part1.getPartNum());
					jsb.setSplitLevel(currentJob.getSplitLevel());// include the previous cover/text jobs from same group as this binder job
					jsb.setOrderByList(Arrays.asList(new OrderBy("productionOrdering", "desc"), new OrderBy("splitLevel", "asc")));
					rootJobs = readAll(jsb);
					Integer prodOrderingToAccountFor = null;
					if(isBindingStage && !isEndSheetPrinted){
						// prod order for the jobs that will move to binder station
						prodOrderingToAccountFor = !rootJobs.isEmpty() ? rootJobs.get(0).getProductionOrdering() : 0;
					}else{
						for(Job iterJob : rootJobs){
							if(iterJob.getProductionOrdering() < currentJob.getProductionOrdering()){
								prodOrderingToAccountFor = iterJob.getProductionOrdering();
								break;
							}
						}
					}
					for(Job iterJob : rootJobs){
						//if(iterJob.getSplitLevel() >= binderJob.getSplitLevel() && totalQty < binderJob.getQuantityNeeded()){
						if(totalQty < currentJob.getQuantityNeeded() && iterJob.getProductionOrdering() == prodOrderingToAccountFor){
							//totalQty += iterJob.getQuantityNeeded();
							totalQty += iterJob.getQuantityProduced();
							rootJobsToInclude.add(iterJob);
							//break; // as usually it is one job that should be accounted for 
						}
					}
					totalQty = (float) 0;
					if(!rootJobsToInclude.isEmpty()){
						String stationName = stationDAO.readStationName(rootJobsToInclude.get(0).getStationId());
						if(Part.PartsCategory.COVER.toString().equals(part1.getCategory().getId()) || Part.PartsCategory.ENDSHEET.toString().equals(part1.getCategory().getId())){
							result.setCoverPrevStation(stationName);
							result.setCoverStatus(rootJobsToInclude.get(rootJobsToInclude.size() - 1).getJobStatus().getId().toLowerCase());
							for(Job iterJob : rootJobsToInclude){
								for(LoadTag iterLT : iterJob.getLoadTags()){
									result.getCoverLocation().add(iterLT.getCartNum());
									result.getCoverLoadtags().add(iterLT);
								}
								result.setCoverQuantityReceived(result.getCoverQuantityReceived() + iterJob.getQuantityProduced());
							}
							// add any qty in stock when this is the binder job
							if(isBindingStage){
								result.setCoverQuantityReceived(result.getCoverQuantityReceived() + qtyInStockCover);
							}
						}else if(Part.PartsCategory.TEXT.toString().equals(part1.getCategory().getId())){
							result.setTextPrevStation(stationName);
							result.setTextStatus(rootJobsToInclude.get(rootJobsToInclude.size() - 1).getJobStatus().getId().toLowerCase());
							for(Job iterJob : rootJobsToInclude){
								for(LoadTag iterLT : iterJob.getLoadTags()){
									result.getTextLocation().add(iterLT.getCartNum());
									result.getTextLoadtags().add(iterLT);
								}
								result.setTextQuantityReceived(result.getTextQuantityReceived() + iterJob.getQuantityProduced());
							}
							// add any qty in stock when this is the binder job
							if(isBindingStage){
								result.setTextQuantityReceived(result.getTextQuantityReceived() + qtyInStockText);
							}
						}else{
							result.setBookPrevStation(stationName);
							result.setBookStatus(rootJobsToInclude.get(rootJobsToInclude.size() - 1).getJobStatus().getId());
							for(Job iterJob : rootJobsToInclude){
								for(LoadTag iterLT : iterJob.getLoadTags()){
									result.getBookLocation().add(iterLT.getCartNum());
									result.getBookLoadtags().add(iterLT);
								}
								result.setBookQuantityReceived(result.getBookQuantityReceived() + iterJob.getQuantityProduced());
							}
							// add any qty in stock when this is the binder job; although we won't have a Book part category in this case !!!
							//if(StationCategory.Categories.BINDER.toString().equals(currentJob.getStationId())){
							//	result.setBookQuantityReceived(result.getBookQuantityReceived() + calculateQtyInStock(currentJob, Part.PartsCategory.TEXT.toString(), false));
							//}
						}
					}else {
						if(Part.PartsCategory.TEXT.toString().equals(part1.getCategory().getId()) && qtyInStockText > 0){
							result.setTextQuantityReceived(qtyInStockText);
						}else if((Part.PartsCategory.COVER.toString().equals(part1.getCategory().getId()) || Part.PartsCategory.ENDSHEET.toString().equals(part1.getCategory().getId()))
								&& qtyInStockCover > 0){
							result.setCoverQuantityReceived(qtyInStockCover);
						}
					}
					rootJobsToInclude.clear();
				}
			}
			// update the binder job qty needed to be the min between the cover and text qtys produced if those jobs were already complete
			if(isBindingStage){
				if(currentJob.getQuantityNeeded() > result.getCoverQuantityReceived() || currentJob.getQuantityNeeded() > result.getTextQuantityReceived()){
					if((JobStatus.JobStatuses.COMPLETE.toString().equalsIgnoreCase(result.getTextStatus()) ||
							   JobStatus.JobStatuses.COMPLETE_PARTIAL.toString().equalsIgnoreCase(result.getTextStatus()) ||
							   JobStatus.JobStatuses.CANCELLED.toString().equalsIgnoreCase(result.getTextStatus())) && 
							(JobStatus.JobStatuses.COMPLETE.toString().equalsIgnoreCase(result.getCoverStatus()) ||
									   JobStatus.JobStatuses.COMPLETE_PARTIAL.toString().equalsIgnoreCase(result.getCoverStatus()) ||
									   JobStatus.JobStatuses.CANCELLED.toString().equalsIgnoreCase(result.getCoverStatus()))){
						currentJob.setQuantityNeeded((int) Math.min(result.getCoverQuantityReceived(), result.getTextQuantityReceived()));
						currentJob.setLastUpdateDate(new Date());
						//binderJob.setLastUpdateId(binderJob.getLastUpdateId());
						jobDAO.update(currentJob);
					}
				}
			}
			result.setCurrentJobQtyNeeded(currentJob.getQuantityNeeded());
		}
		return result;
	}
	       
	/** 
	 * Calculates the Unscheduled/Scheduled Hours for Press jobs
	 * hoursType: 'S' for Scheduled; 'U' for Unscheduled; 'A' for both
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Float calculatePressHours(String colors, String theDay, String hoursType) throws PersistenceException{
		try{
			Float result = (float) 0;
			Pattern pattern = Pattern.compile("T(\\d+)");
			Matcher matcher = pattern.matcher(theDay);
			
			List<Job> jobs = new ArrayList<Job>();
			JobSearchBean jsb = new JobSearchBean();
			//jsb.setJobType(JobType.JobTypes.PRINTING.toString());
			if("S".equals(hoursType)){
				jsb.setStatus(JobStatus.JobStatuses.SCHEDULED.toString());
			}else if("U".equals(hoursType)){
				jsb.setStatus(JobStatus.JobStatuses.NEW.toString());
			}else if("A".equals(hoursType)){
				jsb.setStatusesIn(Arrays.asList(JobStatus.JobStatuses.NEW.toString(), JobStatus.JobStatuses.SCHEDULED.toString()));
			}
			jsb.setPartCategory(Part.PartsCategory.TEXT.toString());
			jsb.setStationId(StationCategory.Categories.PRESS.toString());
			if(!"A".equals(colors)){
				jsb.setPartColors(colors);
			}
			jobs = readAll(jsb);
			for(Job job : jobs){
				if("A".equals(theDay)){
					result +=job.getHours();
				}else if(matcher.matches()){
					int stp = Integer.parseInt(theDay.substring(1));
					Date date = DateUtil.addDaysToDate(new Date(), stp);
					if(job.getDueDate() != null && DateUtil.isSameDay(date, job.getDueDate())){
						result += job.getHours();
					}
				}
			}
			return result;
		} catch (Exception ex) {
			logger.error("Error occurred calculating the UnscheduledHours for Press jobs" + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Calculates the length that's supposed to be on the left over roll once the original roll is complete/stopped,
	 * based on the roll jobs completion status.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Integer calculateLeftOverRollLength(Log log) throws PersistenceException{
		Integer result = 0;
		try{
			Integer cumulJobLengths = 0;
			Float qtyProduced = (float) 0;
			Roll theRoll = null;
			List<String> completedJobQtys = log.getCompletedJobQtys();
			if(completedJobQtys != null && completedJobQtys.size() > 0){
				for(int i = 0; i < completedJobQtys.size(); i++){
					Integer jobId = Integer.parseInt(completedJobQtys.get(i).substring(0, completedJobQtys.get(i).indexOf("_")));
					Job job = this.read(jobId);
					Part pr = partDAO.read(job.getPartNum());
					if(theRoll == null && job != null && job.getRollId() != null){
						theRoll = rollDAO.read(job.getRollId());
					}
					if(Log.LogEvent.COMPLETE.toString().equals(log.getEvent())){
						cumulJobLengths = (int) (cumulJobLengths + calculateJobHoursAndLength(pr, 
								job.getQuantityNeeded(), (float) (theRoll != null ? theRoll.getWidth() : 0.0))[1]);
					}else{
						try{
							qtyProduced = Float.parseFloat(completedJobQtys.get(i).substring(completedJobQtys.get(i).indexOf("_") + 1));
						}catch(IndexOutOfBoundsException iobe){
							qtyProduced = (float) 0;
						}catch(NumberFormatException nfe){
							qtyProduced = (float) 0;
						}
						cumulJobLengths = (int) (cumulJobLengths + calculateJobHoursAndLength(pr,
								(int) Math.ceil(qtyProduced), (float) (theRoll != null ? theRoll.getWidth() : 0.0))[1]);
					}
				}
			}
			if(theRoll != null){
				result = theRoll.getLength() - cumulJobLengths;
			}
			return result;
		} catch (Exception ex) {
			logger.error("Error occurred calculating the Left Over Roll Length" + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Calculates the press station hours for the days needed; for the hours type selected ('S' for scheduled, 'U' for unscheduled, and 'A' for both).
	 * First day will hold hours from the past if exist
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Float> getPressStationHours(String colors, String dayNeeded, String cumulFlag, String hoursType) throws PersistenceException{
		try{
			List<Float> result = null;
			Integer daysNeeded = 0;
			try{
				daysNeeded = Integer.parseInt(dayNeeded);
			}catch(NumberFormatException nfe){
				daysNeeded = 0;
			}
			result = new ArrayList<Float>(daysNeeded+1);
			List<Job> jobs = new ArrayList<Job>();
			JobSearchBean jsb = new JobSearchBean();
			//jsb.setJobType(JobType.JobTypes.PRINTING.toString());
			if("S".equals(hoursType)){
				jsb.setStatus(JobStatus.JobStatuses.SCHEDULED.toString());
			}else if("U".equals(hoursType)){
				jsb.setStatus(JobStatus.JobStatuses.NEW.toString());
			}else if("A".equals(hoursType)){
				jsb.setStatusesIn(Arrays.asList(JobStatus.JobStatuses.NEW.toString(), JobStatus.JobStatuses.SCHEDULED.toString()));
			}
			jsb.setPartCategory(Part.PartsCategory.TEXT.toString());
			jsb.setStationId(StationCategory.Categories.PRESS.toString());
			if(!"A".equals(colors)){
				jsb.setPartColors(colors);
			}
			jobs = readAll(jsb);
			for(int i = 0; i <= daysNeeded; i++){
				if("true".equals(cumulFlag)){
					try{
						result.add(result.get(i-1));
					}catch(IndexOutOfBoundsException iobe){
						result.add((float) 0);
					}
				}else{
					result.add((float) 0);
				}
				for(Job job : jobs){
			    	Date date = DateUtil.addDaysToDate(new Date(), i);
					if(job.getDueDate() != null && 
						(DateUtil.isSameDay(date, job.getDueDate()) || (i == 0 && DateUtil.isAfterDay(date, job.getDueDate())))){
						result.set(i, result.get(i) + job.getHours());
					}
				}
			}
			return result;
		} catch (Exception ex) {
			logger.error("Error occurred calculating the unscheduled hours for the days needed." + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/**
	 * Calculates the specified station Hours.
	 * HoursType could be 'S' for scheduled, 'U' for unscheduled, or 'A for both
	 */
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Float calculateStationHours(String stationId, String hoursType) throws PersistenceException{
		try{
			Float result = (float) 0;
			List<Job> jobs = new ArrayList<Job>();
			JobSearchBean jsb = new JobSearchBean();
			//jsb.setJobType(JobType.JobTypes.PRINTING.toString());
			if("S".equals(hoursType)){
				jsb.setStatus(JobStatus.JobStatuses.SCHEDULED.toString());
			}else if("U".equals(hoursType)){
				jsb.setStatus(JobStatus.JobStatuses.NEW.toString());
			}else if("A".equals(hoursType)){
				jsb.setStatusesIn(Arrays.asList(JobStatus.JobStatuses.NEW.toString(), JobStatus.JobStatuses.SCHEDULED.toString()));
			}
			jsb.setStationId(stationId);
			jobs = getJobDAO().readAll(jsb);
			for(Job job : jobs){
				result += job.getHours();
			}
			return result;
		} catch (Exception ex) {
			logger.error("Error occurred calculating the station work Hours" + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/**
	 * Calculates the work hours for the days needed for the station specified
	 * The hours type could be 'S' for scheduled, 'U' for unscheduled, or 'A' for both
	 * First day will hold hours from the past if exist
	 */
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Float> getStationHours(String stationId, String dayNeeded, String hoursType) throws PersistenceException{
		try{
			List<Float> result = null;
			Integer daysNeeded = 0;
			try{
				daysNeeded = Integer.parseInt(dayNeeded);
			}catch(NumberFormatException nfe){
				daysNeeded = 0;
			}
			result = new ArrayList<Float>(daysNeeded+1);
			List<Job> jobs = new ArrayList<Job>();
			JobSearchBean jsb = new JobSearchBean();
			if("S".equals(hoursType)){
				jsb.setStatus(JobStatus.JobStatuses.SCHEDULED.toString());
			}else if("U".equals(hoursType)){
				jsb.setStatus(JobStatus.JobStatuses.NEW.toString());
			}else if("A".equals(hoursType)){
				jsb.setStatusesIn(Arrays.asList(JobStatus.JobStatuses.NEW.toString(), JobStatus.JobStatuses.SCHEDULED.toString()));
			}
			jsb.setStationId(stationId);
			jobs =  getJobDAO().readAll(jsb);
			for(int i = 0; i <= daysNeeded; i++){
				result.add((float) 0);
				for(Job job : jobs){
			    	Date date = DateUtil.addDaysToDate(new Date(), i);
					if(job.getDueDate() != null && 
							(DateUtil.isSameDay(date, job.getDueDate()) || (i == 0 && DateUtil.isAfterDay(date, job.getDueDate())))){
						result.set(i, result.get(i) + job.getHours());
					}
				}
			}
			return result;
		} catch (Exception ex) {
			logger.error("Error occurred calculating the station unscheduled hours for the days needed." + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Calculates the late hours of the station
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Float getLateWorkHours(String stationId) throws PersistenceException{
		try{
			Float result = (float) 0;
			List<Job> jobs = new ArrayList<Job>();
			JobSearchBean jsb = new JobSearchBean();
			jsb.setStatusesIn(Arrays.asList(JobStatus.JobStatuses.NEW.toString(), JobStatus.JobStatuses.SCHEDULED.toString()));
			jsb.setStationId(stationId);
			jobs = readAll(jsb);
			for(Job job : jobs){
		    	if(job.getDueDate() != null && DateUtil.isBeforeDay(job.getDueDate(), new Date())){
					result += job.getHours();
				}
			}
			return result;
		} catch (Exception ex) {
			logger.error("Error occurred calculating the late work hours for the " + stationId + " station. " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Calculates the work hours of the station due after today/tomorrow
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Float getWorkHoursDueAfterToday(String stationId) throws PersistenceException{
		try{
			Float result = (float) 0;
			List<Job> jobs = new ArrayList<Job>();
			JobSearchBean jsb = new JobSearchBean();
			jsb.setStatusesIn(Arrays.asList(JobStatus.JobStatuses.NEW.toString(), JobStatus.JobStatuses.SCHEDULED.toString()));
			jsb.setStationId(stationId);
			jobs = readAll(jsb);
			Date tomorrowDate = DateUtil.addDaysToDate(new Date(), 1);
			for(Job job : jobs){
		    	if(job.getDueDate() != null && DateUtil.isAfterDay(job.getDueDate(), tomorrowDate)){
					result += job.getHours();
				}
			}
			return result;
		} catch (Exception ex) {
			logger.error("Error occurred calculating the work hours due after today/tomorrow for the " + stationId + " station. " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Calculates the percentages representing the work shift hours during the day
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Float[]> getDayShifts(Date date) throws PersistenceException{
		List<Float[]> result = new ArrayList<Float[]>();
		try{
			Calendar dateCal = Calendar.getInstance();
			dateCal.setTime(date);
			Preference shiftPreference = lookupDAO.read("SHIFT", Preference.class);
			if(shiftPreference != null && shiftPreference.getName() != null && !shiftPreference.getName().isEmpty()){
				List<String> splittedStr = new ArrayList<String>(Arrays.asList(shiftPreference.getName().split(";")));
				if(!splittedStr.isEmpty() ){
					//if today is weekend and it is a non work day, then return 100 % no work
					String lastPortion = splittedStr.get(splittedStr.size() - 1);
					if((dateCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || dateCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)){
						if(lastPortion.endsWith(":nw")){
							result.add(new Float[] {(float) 0, (float)1});
							result.add(new Float[] {(float) 100, (float)0});
							return result;
						}
					}
					//if shift reg exp doesn't start with beginning of day meaning 0 or ends with 24h, add missing portions to allow calculation to be correct
					String tmp = null;
					if(!splittedStr.get(0).startsWith("0-") && !splittedStr.get(0).startsWith("00-")){
						//tmp = splittedStr.get(0).substring(0, splittedStr.get(0).indexOf("-"));
						splittedStr.add(0, "0-0");
						//result.add((float) 0);
					}
					tmp = lastPortion.substring(lastPortion.indexOf("-") + 1, lastPortion.indexOf(":"));
					if(!"24".equals(tmp)){
						splittedStr.remove(splittedStr.size() - 1);
						splittedStr.add(splittedStr.size() ,   lastPortion.substring(0, lastPortion.indexOf(":")) );
						splittedStr.add(splittedStr.size() , "24-24");
					}
					List<Float> portionHours = new ArrayList<Float>();
					for(String portion : splittedStr){
						List<String> splittedStr2 = Arrays.asList(portion.split("-"));
						if(splittedStr2.size() == 2 && isNumeric(splittedStr2.get(0)) && isNumeric(splittedStr2.get(1))){
							portionHours.add(Float.parseFloat(splittedStr2.get(0)));
							portionHours.add(Float.parseFloat(splittedStr2.get(1)));
						}//last portion has the weekend string, so it is a special case
						else if(splittedStr2.size() == 2 && isNumeric(splittedStr2.get(0)) && !isNumeric(splittedStr2.get(1))
								&& splittedStr2.get(1).contains(":")){
							portionHours.add(Float.parseFloat(splittedStr2.get(0)));
							portionHours.add(Float.parseFloat(splittedStr2.get(1).substring(0, splittedStr2.get(1).indexOf(":"))));
						}
					}
					for(int i = 0; i < portionHours.size() - 1; i++){
						result.add(new Float[] {(portionHours.get(i+1) - portionHours.get(i)) * 100 / (float)24, i % 2 == 0 ? (float)1 : (float)0});
					}
				}
			}
			return result;
		}catch (Exception ex) {
			logger.error("An error occurred while calculating the percentages representing the work shift hours during the day; please contact the Administrator." + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Calculates the percentages representing the work shift hours during the day + percentages of jobs by status
	 * The days are today and tomorrow
	 * For the 'Due that day', calculate hours due that day and subtract from remaining free time of that day
	 * For 'Late Work', it is the work time due before today which will span on the remaining free hours of today and tomorrow (or whatever nb days in the param).
	 * For 'Due After Today', calculate work time due after tomorrow which should span on remaining free time of tomorrow's day.
	 * Mapping of percentages to job/work status:
	 * 1 for Free
	 * 0 for Non Work Hours
	 * 2 Due today
	 * 3 Late work
	 * 4 Due after today/tomorrow
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<List<Float[]>> getJobsPercentagesByStatus(String stationId, String dayNeeded, String hoursType, String colors) throws PersistenceException{
		List<List<Float[]>> result = new ArrayList<List<Float[]>>();
		List<Float[]> tmpShifts = new ArrayList<Float[]>();
		Calendar dateCal = Calendar.getInstance();
		dateCal.setTime(new Date());
		try{
			Integer daysNeeded = 0;
			try{
				daysNeeded = Integer.parseInt(dayNeeded);
			}catch(NumberFormatException nfe){
				daysNeeded = 0;
			}
			for(int i = 0; i <= daysNeeded; i++){
				dateCal.add(Calendar.DAY_OF_MONTH, i);
				tmpShifts = getDayShifts(dateCal.getTime());
				result.add(tmpShifts);
			}
			//Now calculate and add the 'Due That Day' hours; for today and tomorrow (or whatever nb days in the param)
			List<Float> dueThatDayResult = new ArrayList<Float>();
			if(StationCategory.Categories.PRESS.toString().equals(stationId)){
				dueThatDayResult = this.getPressStationHours(colors, dayNeeded, "false", hoursType);
			}else{
				dueThatDayResult = this.getStationHours(stationId, dayNeeded, hoursType);
			}
			//convert the values to percentages: make sure to divide the hours by nb machines
			for(int i = 0; i < dueThatDayResult.size(); i++){
				dueThatDayResult.set(i, dueThatDayResult.get(i) * 100 / (float)(24 * this.getNbMachines(stationId, null)));
			}
			// subtract the due today values from the free hours available
			for(int iterDays = 0; iterDays < result.size(); iterDays++){
				for(int iterPercentages = 0; iterPercentages < result.get(iterDays).size(); iterPercentages++){
					if(dueThatDayResult.get(iterDays) > 0){
						if(result.get(iterDays).get(iterPercentages)[1] == 1){//The Free Time
							if(dueThatDayResult.get(iterDays) >= result.get(iterDays).get(iterPercentages)[0]){
								result.get(iterDays).get(iterPercentages)[1] = (float) 2;//free time becomes due today
								dueThatDayResult.set(iterDays, dueThatDayResult.get(iterDays) - result.get(iterDays).get(iterPercentages)[0]) ;
							}else{
								result.get(iterDays).add(iterPercentages, new Float[] {dueThatDayResult.get(iterDays), (float) 2});
								//result.get(iterDays).get(iterPercentages)[0] -= dueThatDayResult.get(iterDays);
								result.get(iterDays).set(iterPercentages + 1,new Float[] {result.get(iterDays).get(iterPercentages + 1)[0] - dueThatDayResult.get(iterDays), result.get(iterDays).get(iterPercentages + 1)[1]});
								dueThatDayResult.set(iterDays, (float) 0);
							}
						}
					}else{
						break;
					}
				}
			}
			
			// Now calculate the late work hours and include in the chart of values
			Float lateHours = this.getLateWorkHours(stationId);
			//convert the value to percentage
			lateHours = lateHours * 100 / (float)(24 * this.getNbMachines(stationId, null));
			// subtract the late hours value from the free hours available in today's and tomorrow's days (or whatever nb days in the param)
			for(int iterDays = 0; iterDays < result.size(); iterDays++){
				for(int iterPercentages = 0; iterPercentages < result.get(iterDays).size(); iterPercentages++){
					if(lateHours > 0){
						if(result.get(iterDays).get(iterPercentages)[1] == 1){//The Free Time
							if(lateHours >= result.get(iterDays).get(iterPercentages)[0]){
								result.get(iterDays).get(iterPercentages)[1] = (float) 3;//free time becomes late work
								//dueThatDayResult.set(iterDays, dueThatDayResult.get(iterDays) - result.get(iterDays).get(iterPercentages)[0]) ;
								lateHours = lateHours - result.get(iterDays).get(iterPercentages)[0];
							}else{
								result.get(iterDays).add(iterPercentages, new Float[] {lateHours, (float) 3});
								//result.get(iterDays).set(iterPercentages + 1,new Float[] {result.get(iterDays).get(iterPercentages + 1)[0] - dueThatDayResult.get(iterDays), result.get(iterDays).get(iterPercentages + 1)[1]});
								result.get(iterDays).set(iterPercentages + 1,new Float[] {result.get(iterDays).get(iterPercentages + 1)[0] - lateHours, result.get(iterDays).get(iterPercentages + 1)[1]});
								lateHours = (float) 0;
							}
						}
					}else{
						break;
					}
				}
			}
			
			// Now calculate the due after tomorrow work hours and include in the chart of values
			Float dueAfterTomorrowHours = this.getWorkHoursDueAfterToday(stationId);
			//convert the value to percentage
			dueAfterTomorrowHours = dueAfterTomorrowHours * 100 / (float)(24 * this.getNbMachines(stationId, null));
			// subtract the due after tomorrow hours value from the free hours available in tomorrow's day
			if(result.size() > 1){
				for(int iterPercentages = 0; iterPercentages < result.get(1).size(); iterPercentages++){
					if(dueAfterTomorrowHours > 0){
						if(result.get(1).get(iterPercentages)[1] == 1){//The Free Time
							if(dueAfterTomorrowHours >= result.get(1).get(iterPercentages)[0]){
								result.get(1).get(iterPercentages)[1] = (float) 4;//free time becomes due after tomorrow time
								//dueThatDayResult.set(iterDays, dueThatDayResult.get(iterDays) - result.get(iterDays).get(iterPercentages)[0]) ;
								dueAfterTomorrowHours = dueAfterTomorrowHours - result.get(1).get(iterPercentages)[0];
							}else{
								result.get(1).add(iterPercentages, new Float[] {dueAfterTomorrowHours, (float) 4});
								//result.get(iterDays).set(iterPercentages + 1,new Float[] {result.get(iterDays).get(iterPercentages + 1)[0] - dueThatDayResult.get(iterDays), result.get(iterDays).get(iterPercentages + 1)[1]});
								result.get(1).set(iterPercentages + 1,new Float[] {result.get(1).get(iterPercentages + 1)[0] - dueAfterTomorrowHours, result.get(1).get(iterPercentages + 1)[1]});
								dueAfterTomorrowHours = (float) 0;
							}
						}
					}else{
						break;
					}
				}
			}
			return result;
		}catch (Exception ex) {
			logger.error("An error occurred while calculating the percentages representing the work shift hours during the day + percentages of jobs by status; please contact the Administrator." + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Calculates the capacity hours for the days specified
	 */
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Float> getCapacityHours(String colors, String dayNeeded, String cumulFlag) throws PersistenceException{
		try{
			List<Float> result = null;
			Integer daysNeeded = 0;
			Calendar dateCal = Calendar.getInstance();
			dateCal.setTime(new Date());
			try{
				daysNeeded = Integer.parseInt(dayNeeded);
			}catch(NumberFormatException nfe){
				daysNeeded = 0;
			}
			int nbMachines = getNbMachines(StationCategory.Categories.PRESS.toString(), colors);
			result = new ArrayList<Float>(daysNeeded+1);
			Float nbHoursPerDay = (float) 0;
			Preference shiftPreference = lookupDAO.read("SHIFT", Preference.class);
			if(shiftPreference != null && shiftPreference.getName() != null && !shiftPreference.getName().isEmpty()){
				List<String> splittedStr = Arrays.asList(shiftPreference.getName().split(";"));
				List<String> splittedStr3 = null;
				for(String iter : splittedStr){
					List<String> splittedStr2 = Arrays.asList(iter.split("-"));
					if(splittedStr2.size() == 2 && isNumeric(splittedStr2.get(0)) && isNumeric(splittedStr2.get(1))){
						nbHoursPerDay += (Float.parseFloat(splittedStr2.get(1)) - Float.parseFloat(splittedStr2.get(0)));
					}else if(splittedStr2.size() == 2 && isNumeric(splittedStr2.get(0)) ){
					 splittedStr3 = Arrays.asList(splittedStr2.get(1).split(":"));
						if(splittedStr3.size() == 2 && isNumeric(splittedStr3.get(0))){
							nbHoursPerDay += (Float.parseFloat(splittedStr3.get(0)) - Float.parseFloat(splittedStr2.get(0)));
						}
					}
				}
				//nbHoursPerDay = calculateDayWorkHours(shiftPreference.getName());
				for(int i = 0; i <= daysNeeded; i++){
					if(splittedStr3 != null && splittedStr3.get(1) != null && "nw".equals(splittedStr3.get(1)) &&
							(dateCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || dateCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)){
						try{
							if("true".equals(cumulFlag)){
								result.add(result.get(result.size() - 1));
							}else{
								result.add((float) 0);
							}
						}catch(IndexOutOfBoundsException iobe){
							result.add((float) 0);
						}
					}else{
						try{
							if("true".equals(cumulFlag)){
								result.add(result.get(result.size() - 1) + (nbHoursPerDay * nbMachines));
							}else{
								result.add(nbHoursPerDay * nbMachines);
							}
						}catch(IndexOutOfBoundsException iobe){
							result.add(nbHoursPerDay * nbMachines);
						}
					}
					dateCal.add(Calendar.DAY_OF_MONTH, 1);
				}
			}
			return result;
		} catch (Exception ex) {
			logger.error("An error occurred while calculating the Capacity Hours for the needed days; please contact the Administrator." + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}
	}
	
	private boolean isNumeric(String str){  
		  try {  
		    @SuppressWarnings("unused")
			Float d = Float.parseFloat(str);  
		  }  
		  catch(NumberFormatException nfe)  {  
		    return false;  
		  }  
		  return true;  
	}
	
	/**
	 * retrieves the number of active machines
	 * @return integer: number of machines
	 */
	private int getNbMachines(String stationId, String colors){
		int nbMachines = 1;
		MachineSearchBean msb = new MachineSearchBean();
		msb.setStationId(stationId);
		msb.setStatusDiff(MachineStatus.statuses.OUTSERVICE.toString());
		if(colors != null){
			msb.setType(colors);
		}
		List<Machine> machines = machineDAO.readAll(msb);
		if(machines != null && !machines.isEmpty()){
			nbMachines = machines.size();
		}
		return nbMachines;
	}
	
	@SuppressWarnings("unused")
	private Float calculateDayWorkHours(String str) 
	{  
		Float result = (float) 0;
		List<String> splittedStr = Arrays.asList(str.split(";"));
		for(String iter : splittedStr){
			List<String> splittedStr3 = null;
			List<String> splittedStr2 = Arrays.asList(iter.split("-"));
			if(splittedStr2.size() == 2 && isNumeric(splittedStr2.get(0)) && isNumeric(splittedStr2.get(1))){
				result += (Float.parseFloat(splittedStr2.get(1)) - Float.parseFloat(splittedStr2.get(0)));
			}else if(splittedStr2.size() == 2 && isNumeric(splittedStr2.get(0)) ){
			 splittedStr3 = Arrays.asList(splittedStr2.get(1).split(":"));
				if(splittedStr3.size() == 2 && isNumeric(splittedStr3.get(0))){
					result += (Float.parseFloat(splittedStr3.get(0)) - Float.parseFloat(splittedStr2.get(0)));
				}
			}
		}
		return result;
	}
	
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Integer getCount() {
		return jobDAO.getCount();
	}
	
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Job> fullSearch(String word, Integer maxResult, Integer offset) {	
		return jobDAO.fullSearch(word, maxResult, offset);		
	}

	/**
	 * @return the JobDAO
	 */
	public JobDAO getJobDAO() {
		return jobDAO;
	}

	/**
	 * @param dao the JobDAO to set
	 */
	public void setJobDAO(JobDAO dao) {
		this.jobDAO = dao;
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
	 * @return the partDAO
	 */
	public PartDAO getPartDAO() {
		return partDAO;
	}

	/**
	 * @param partDAO the partDAO to set
	 */
	public void setPartDAO(PartDAO partDAO) {
		this.partDAO = partDAO;
	}

	/**
	 * @return the stationDAO
	 */
	public StationDAO getStationDAO() {
		return stationDAO;
	}

	/**
	 * @param stationDAO the stationDAO to set
	 */
	public void setStationDAO(StationDAO stationDAO) {
		this.stationDAO = stationDAO;
	}

	/**
	 * @return the machineDAO
	 */
	public MachineDAO getMachineDAO() {
		return machineDAO;
	}

	/**
	 * @param machineDAO the machineDAO to set
	 */
	public void setMachineDAO(MachineDAO machineDAO) {
		this.machineDAO = machineDAO;
	}

	/**
	 * @return the lookupDAO
	 */
	public LookupDAO getLookupDAO() {
		return lookupDAO;
	}

	/**
	 * @param lookupDAO the lookupDAO to set
	 */
	public void setLookupDAO(LookupDAO lookupDAO) {
		this.lookupDAO = lookupDAO;
	}

	/**
	 * @return the rollDAO
	 */
	public RollDAO getRollDAO() {
		return rollDAO;
	}

	/**
	 * @param rollDAO the rollDAO to set
	 */
	public void setRollDAO(RollDAO rollDAO) {
		this.rollDAO = rollDAO;
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
	 * @return the logDAO
	 */
	public LogDAO getLogDAO() {
		return logDAO;
	}

	/**
	 * @param logDAO the logDAO to set
	 */
	public void setLogDAO(LogDAO logDAO) {
		this.logDAO = logDAO;
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
	 * @return the printingTimeCalculator
	 */
	public PrintingTimeCalculator getPrintingTimeCalculator() {
		return printingTimeCalculator;
	}

	/**
	 * @param printingTimeCalculator the printingTimeCalculator to set
	 */
	public void setPrintingTimeCalculator(PrintingTimeCalculator printingTimeCalculator) {
		this.printingTimeCalculator = printingTimeCalculator;
	}

	/*private static SseBroadcaster broadcaster = new SseBroadcaster();

	public void register(EventOutput eventOutput) {
		broadcaster.add(eventOutput);
	}

	public static void broadcast(Job job) {

		OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
		OutboundEvent event = eventBuilder.name("message").mediaType(MediaType.APPLICATION_JSON_TYPE)
				.data(Job.class, job).build();

		broadcaster.broadcast(event);
	}*/
	
   public List<Order> fetchJobPackaging(String isbn){
	   return jobDAO.fetchJobPackaging(isbn);
  }

	
   public NotificationService getNotificationService() {
		return notificationService;
	}
	
	public 
	void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	/**
	 * @return the workflowEngine
	 */
	public WorkflowEngine getWorkflowEngine() {
		return workflowEngine;
	}

	/**
	 * @param workflowEngine the workflowEngine to set
	 */
	public void setWorkflowEngine(WorkflowEngine workflowEngine) {
		this.workflowEngine = workflowEngine;
	}
   
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Integer> getIdsList() {		
		return jobDAO.getIdsList();
	}
   
}