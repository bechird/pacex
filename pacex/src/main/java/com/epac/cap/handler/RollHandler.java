package com.epac.cap.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.EpacException;
import com.epac.cap.common.PersistenceException;
import com.epac.cap.config.NDispatcher;
import com.epac.cap.functionel.PrintingTimeCalculator;
import com.epac.cap.model.Job;
import com.epac.cap.model.JobStatus;
import com.epac.cap.model.JobType;
import com.epac.cap.model.MachineType;
import com.epac.cap.model.Order;
import com.epac.cap.model.PaperType;
import com.epac.cap.model.PaperTypeMedia;
import com.epac.cap.model.Part;
import com.epac.cap.model.Preference;
import com.epac.cap.model.Roll;
import com.epac.cap.model.RollStatus;
import com.epac.cap.model.RollType;
import com.epac.cap.model.StationCategory;
import com.epac.cap.model.WFSDataSupport;
import com.epac.cap.model.WFSPartWorkflow;
import com.epac.cap.model.WFSProductionStatus;
import com.epac.cap.repository.JobDAO;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.repository.MachineDAO;
import com.epac.cap.repository.OrderDAO;
import com.epac.cap.repository.PartDAO;
import com.epac.cap.repository.RollDAO;
import com.epac.cap.repository.RollSearchBean;
import com.epac.cap.repository.StationDAO;
import com.epac.cap.service.NotificationService;
import com.epac.cap.sse.beans.Event;
import com.epac.cap.sse.beans.EventTarget;
import com.epac.cap.utils.LogUtils;
import com.mycila.event.api.topic.Topics;

/**
 * Interacts with Roll data.  Uses RollDAO for entity persistence.
 * @author walid
 *
 */
@Service
public class RollHandler {
	
	@Autowired
	private JobHandler jobHandler;
	
	@Autowired
	private MachineHandler machineHandler;
	
	@Autowired
	private RollDAO rollDAO;
	
	@Autowired
	private OrderDAO orderDAO;
	
	@Autowired
	private PartDAO partDAO;
	
	@Autowired
	private MachineDAO machineDAO;
	
	@Autowired
	private StationDAO stationDAO;
	
	@Autowired
	private JobDAO jobDAO;
	
	@Autowired
	private LookupDAO lookupDAO;
	
	@Autowired
	private WFSDataSupportHandler wfsDataSupportHandler;
	
	@Autowired
	private WFSWorkflowHandler wfsWorkflowHandler;
	
	@Autowired
	private PrintingTimeCalculator printingTimeCalculator;
	
	@Autowired
	private NotificationService notificationService;
	
	private static ExecutorService executor = Executors.newCachedThreadPool();

	private static Logger logger = Logger.getLogger(RollHandler.class);
	
	/***
	 * No arg constructor.  This is the preferred constructor.
	 */
	public RollHandler(){  }
  
	/** 
	 * Calls the corresponding create method on the RollDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public void create(Roll bean) throws PersistenceException {
		try {
			prepareBeans(bean);
			getRollDAO().create(bean);
			Event event=new Event(EventTarget.Roll, false, null, bean.getRollId());
			notificationService.broadcast(event);
		} catch (Exception ex) {
			bean.setRollId(null);
			Event event=new Event(EventTarget.Roll, true, ex.getLocalizedMessage(), bean.getRollId());
			notificationService.broadcast(event);
			logger.error("Error occurred creating a Roll : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Calls the corresponding update method on the RollDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void updateCopyStatus(String copStatus,Integer id){
		rollDAO.updateCopyStatus(copStatus,id);
	}
	
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(Roll bean) throws PersistenceException {
		try {
			prepareBeans(bean);
			
			/*if(bean.getMachineId() == null){
				Roll originalRoll = this.read(bean.getRollId());
				if(originalRoll.getMachineId() != null && !originalRoll.getMachineId().isEmpty()){
					Machine machine = machineDAO.read(originalRoll.getMachineId());
					machine.getRolls().remove(bean);
					machineDAO.update(machine);
				}
			}*/
			getRollDAO().update(bean);		   
		} catch (Exception ex) {
			logger.error("Error occurred updating a Roll : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	private void prepareBeans(Roll roll){
		if(roll.getMachineId() != null && StringUtils.isEmpty(roll.getMachineId())){
			roll.setMachineId(null);
		}
		if(roll.getRollType() != null && !StringUtils.isEmpty(roll.getRollType().getId())){
			roll.setRollType(lookupDAO.read(roll.getRollType().getId(), RollType.class));
		}else{
			roll.setRollType(null);
		}
		if(roll.getPaperType() != null && !StringUtils.isEmpty(roll.getPaperType().getId())){
			roll.setPaperType(lookupDAO.read(roll.getPaperType().getId(), PaperType.class));
		}else{
			roll.setPaperType(null);
		}
		if(roll.getStatus() != null && !StringUtils.isEmpty(roll.getStatus().getId())){
			roll.setStatus(lookupDAO.read(roll.getStatus().getId(), RollStatus.class));
		}else{
			roll.setStatus(null);
		}
	}
	
	/** 
	 * Finds the rolls available for printing on them;
	 * Include the rolls that are left over and mounted on machines (their status is then 'Assigned' but have no jobs on them)
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Roll> getAvailableRolls(String color, String executingUserId) throws PersistenceException{
		try{
			List<Roll> rolls = new ArrayList<Roll>();
			RollSearchBean rsb = new RollSearchBean();
			String[] statuses = {RollStatus.statuses.AVAILABLE.toString(),RollStatus.statuses.NEW.toString(), RollStatus.statuses.ASSIGNED.toString()};
			String[] types = {RollType.types.LEFTOVER.toString(), RollType.types.NEW.toString()};
			
			rsb.setStatusIn(statuses);
			rsb.setTypeIn(types);
			List<Job> jobs = jobHandler.getAvailableJobsForScheduling(color, "ALL");
			Map<String, Float> paperTypesOnJobs = new HashMap<String, Float>();
			if(!jobs.isEmpty()){
				for(Job aJob : jobs){
					if(aJob.getPartPaperId() != null){
						paperTypesOnJobs.put(aJob.getPartPaperId(), 
							(paperTypesOnJobs.get(aJob.getPartPaperId()) != null ?
							paperTypesOnJobs.get(aJob.getPartPaperId()) : (float)0) + aJob.getHours());
					}
				}
			}
			for(Roll roll : readAll(rsb)){
				if(paperTypesOnJobs.keySet().contains(roll.getPaperType().getId())){
					if(Part.PartColors._ALL.getName().equals(color) || roll.getColors() == null){
						if(!RollStatus.statuses.ASSIGNED.toString().equals(roll.getStatus().getId()) || roll.getJobs().isEmpty()){
							rolls.add(roll);
						}
					}else if(Part.PartColors._4C.getName().equals(color) && Part.PartColors._4C.getName().equals(roll.getColors())){
						if(!RollStatus.statuses.ASSIGNED.toString().equals(roll.getStatus().getId()) || roll.getJobs().isEmpty()){
							rolls.add(roll);
						}
					}else if(Part.PartColors._1C.getName().equals(color) && Part.PartColors._1C.getName().equals(roll.getColors())){
						if(!RollStatus.statuses.ASSIGNED.toString().equals(roll.getStatus().getId()) || roll.getJobs().isEmpty()){
							rolls.add(roll);
						}
					}
				}
			}
			// See if we should add an option for a new roll:
			// If there are jobs but no roll at all then provide a new roll.
			// If total hours of jobs bigger than total hours of available time on rolls, then provide a new roll; actually we will provide one anyway
			// If no roll with same paper type as jobs, then provide new roll with that paper type 
			// We can as well add new roll if there are jobs and color option is 'All'
			if(jobs.size() > 0){
				if(rolls.size() == 0 || Part.PartColors._ALL.getName().equals(color)){
					// For each paper type on the jobs create a new roll with that paper type in case operator prefers a new roll
					// and take account of each paper type media type
					for(String pt : paperTypesOnJobs.keySet()){
						PaperType paperTypeBean = lookupDAO.read(pt, PaperType.class);
						if(paperTypeBean != null){
							for(PaperTypeMedia iter : paperTypeBean.getMedias()){
								rolls.add(buildNewRoll(pt, iter.getRollWidth(), executingUserId));
							}
						}
					}
				}else{
					//Add new roll when a job paper type is not contained in one of the rolls or that the total job hours for a certain
					//paper type is bigger than the available hours on the rolls having that same paper type; actually we are providing one anyway
					/*Map<String, Float> paperTypesOnRolls = new HashMap<String, Float>();
					for(Roll aRoll : rolls){
						paperTypesOnRolls.put(aRoll.getPaperType().getId(), 
								(paperTypesOnRolls.get(aRoll.getPaperType().getId()) != null ?
										paperTypesOnRolls.get(aRoll.getPaperType().getId()) : (float)0) + this.getAvailableHoursOnRoll(aRoll.getLength()));
					}*/
					for(String paperTypeOnJob: paperTypesOnJobs.keySet()){
						//if(!paperTypesOnRolls.keySet().contains(paperTypeOnJob) ||
							//	paperTypesOnJobs.get(paperTypeOnJob) > paperTypesOnRolls.get(paperTypeOnJob)){
						PaperType paperTypeBean = lookupDAO.read(paperTypeOnJob, PaperType.class);
						if(paperTypeBean != null){
							for(PaperTypeMedia iter : paperTypeBean.getMedias()){
								rolls.add(buildNewRoll(paperTypeOnJob, iter.getRollWidth(), executingUserId));
							}
						}
						//}
					}
				}
			}
			 Collections.sort(rolls, new RollSchedulingComparator());
			return rolls;
		} catch (Exception ex) {
			logger.error("Error occurred while reading the list of available roll records! " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}
	}
	
	public Roll buildNewRoll(String paperType, Float rollWidth, String executingUserId) throws NumberFormatException, PersistenceException{
		Roll roll = new Roll();
		PaperType paperTypeBean = lookupDAO.read(paperType, PaperType.class);
		if(paperTypeBean != null){
			if(rollWidth != null){
				for(PaperTypeMedia iter : paperTypeBean.getMedias()){
					if(iter.getRollWidth() == rollWidth){
						roll.setLength(iter.getRollLength());
						roll.setWidth(iter.getRollWidth());
						break;
					}
				}
			}else{
				PaperTypeMedia ptm = paperTypeBean.getMedias().iterator().next();
				roll.setLength(ptm.getRollLength());
				roll.setWidth(ptm.getRollWidth());
			}
		}else{
			roll.setLength(Integer.parseInt(lookupDAO.read("ROLLINITLENGTH", Preference.class).getName()));
			roll.setWidth(Float.parseFloat(lookupDAO.read("ROLLWIDTH", Preference.class).getName()));
		}
		roll.setPaperType(paperTypeBean);
		roll.setRollType(lookupDAO.read(RollType.types.NEW.toString(), RollType.class));
		//status 'New': used to distinguish that this is a transient roll that later when produced will first need to be persisted/created
		roll.setStatus(lookupDAO.read(RollStatus.statuses.NEW.toString(), RollStatus.class));
		roll.setCreatedDate(new Date());
		roll.setCreatorId(executingUserId);
		return roll;
	}
	
	public Float getAvailableHoursOnRoll(Integer rollLength) throws PersistenceException{
		Float result = null;
		result = rollLength / machineHandler.getDefaultMachineSpeed(StationCategory.Categories.PRESS.toString());
		return result;
	}
	
	/** 
	 * Producing Roll; assigning the roll to the jobs and updating the statuses.
	 * If roll is full and cannot hold all jobs, split the last job (min qty of the job on roll has to be at least 1)
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public Integer produceRoll(int[] selectedJobsForProduction, Float rollWidth, String pfMachineType, String modeOption, String executingUserId) throws PersistenceException, EpacException {
		Integer rollId = 0;
		Roll roll = null;
		try {
			boolean jobAssignedToRoll = false;
			if(selectedJobsForProduction != null && selectedJobsForProduction.length > 1){
				// If station is plow folder, and some jobs have different imposition types, then do not produce the roll
				float impositionType = 0;
				boolean differentImpositionsExist = false;
				Set<Part> theParts = new HashSet<Part>();
				for(int i=1; i< selectedJobsForProduction.length; i++){
					Job job = jobDAO.read(selectedJobsForProduction[i]);
					if(job != null){
						// re-calculate job hours based on roll width, 
						Part pr = partDAO.read(job.getPartNum());
						Float[] jobHoursLengthAndImp = jobHandler.calculateJobHoursAndLength(pr, job.getQuantityNeeded(), rollWidth);
						job.setHours(jobHoursLengthAndImp[0]);
						jobDAO.update(job);
						
						if(impositionType == 0){
							impositionType = jobHoursLengthAndImp[2];
						}
						if(impositionType != jobHoursLengthAndImp[2]){
							differentImpositionsExist = true;
						}
					}
				}
				if(MachineType.types.PLOWFOLDER.toString().equals(pfMachineType) && differentImpositionsExist){
					return 0;
				}
				rollId = selectedJobsForProduction[0];
				if(rollId <= 0){
					Job j = jobDAO.read(selectedJobsForProduction[1]);
					// roll paper type should be the same as any of the job's paper types
					roll = this.buildNewRoll(j.getPartPaperId() != null ? j.getPartPaperId() : "", rollWidth, executingUserId);
					create(roll);
					rollId = roll.getRollId();
				}else{
					roll = getRollDAO().read(rollId);
					//set created date to this date where roll is being generated and jobs assigned to it rather than original creation date (case of left over roll)
					if(roll != null){
						roll.setCreatedDate(new Date());
					}
				}
				Float availableHoursOnRoll = (float) 0;
				if(roll != null){
					availableHoursOnRoll = getAvailableHoursOnRoll(roll.getLength());
				}
				// Now assign jobs to the roll and update their status
				for(int i=1; i< selectedJobsForProduction.length; i++){
					Job job = jobDAO.read(selectedJobsForProduction[i]);
					if(job != null && availableHoursOnRoll > 0){
						Part pr = partDAO.read(job.getPartNum());
						if(availableHoursOnRoll - job.getHours() < 0){//job needs to be split first (respect the order of the below code lines as it matters)
							//Integer newQuantity = (int) Math.ceil(((job.getHours() - availableHoursOnRoll) * 60000) / (float)job.getPart().getPagesCount());
							Integer newQuantity = printingTimeCalculator.getJobQuantity(job.getHours() - availableHoursOnRoll, pr);
							availableHoursOnRoll -= job.getHours();
							if(job.getQuantityNeeded() != null && newQuantity < job.getQuantityNeeded()){
								if(JobType.JobTypes.PRINTING_POPLINE.toString().contains(pfMachineType)){
									if(JobType.JobTypes.PRINTING_3UP.toString().equals(job.getJobType().getId())){
										int modulo = (job.getQuantityNeeded() - newQuantity) % 3;
										if(modulo != 0){
											newQuantity = newQuantity + 3 - modulo;
										}
									}
									if(JobType.JobTypes.PRINTING_2UP.toString().equals(job.getJobType().getId())){
										int modulo = (job.getQuantityNeeded() - newQuantity) % 2;
										if(modulo != 0){
											newQuantity = newQuantity + 2 - modulo;
										}
									}
								}
								if(newQuantity < job.getQuantityNeeded()){
									jobHandler.splitJobs(job, newQuantity, (float) 0, "A", executingUserId);
								}else{// the job will have 0 qty after split which should not happen, so continue and do nothing for this job
									continue;
								}
							}else{// the job will have 0 qty after split which should not happen, so continue and do nothing for this job
								continue;
							}
						}else{
							availableHoursOnRoll -= job.getHours();
						}
						job.setRollId(roll != null ? roll.getRollId() : null);
						roll.getAlljobs().add(job);
						jobAssignedToRoll = true;
						if(roll != null && RollStatus.statuses.ASSIGNED.toString().equals(roll.getStatus().getId())){
							job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.ASSIGNED.toString(), JobStatus.class));
						}else{
							job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.SCHEDULED.toString(), JobStatus.class));
						}
						job.setRollOrdering(i);
						if(roll != null){
							if(!RollStatus.statuses.ASSIGNED.toString().equals(roll.getStatus().getId())){
								roll.setStatus(lookupDAO.read(RollStatus.statuses.SCHEDULED.toString(), RollStatus.class));
							}
							roll.setHours(roll.getHours() + job.getHours());
						}

						theParts.add(pr);
						
						// check the value of the system preference COVERPRESSJOBACTIVATION to see if cover job needs to be activated as well
						Preference coverjobactivationPref = lookupDAO.read("COVERPRESSJOBACTIVATION", Preference.class);
						Boolean coverjobactivationFlag = false;
						if(coverjobactivationPref != null && "true".equals(coverjobactivationPref.getName())){
							coverjobactivationFlag = true;
						}
						JobSearchBean jsb = new JobSearchBean();
						jsb.setOrderId(job.getOrderId());
						jsb.setPartFamily((job.getPartNum().endsWith("T") || job.getPartNum().endsWith("C")) ? (job.getPartNum().substring(0, job.getPartNum().length() - 1)) : job.getPartNum());
						jsb.setSplitLevel(job.getSplitLevel());
						jsb.setStationId(StationCategory.Categories.COVERPRESS.toString());
						for(Job j : jobDAO.readAll(jsb)){
							if(coverjobactivationFlag && JobStatus.JobStatuses.NEW.toString().equals(j.getJobStatus().getId())){
								j.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.SCHEDULED.toString(), JobStatus.class));
								jobDAO.update(j);
							}
						}
						
						//update the pdf job type based on value from pfMachineType
						JobSearchBean jobSearchBean = new JobSearchBean();
						jobSearchBean.setOrderId(job.getOrderId());
						jsb.setPartFamily((job.getPartNum().endsWith("T") || job.getPartNum().endsWith("C")) ? (job.getPartNum().substring(0, job.getPartNum().length() - 1)) : job.getPartNum());
						jobSearchBean.setSplitLevel(job.getSplitLevel());
						jobSearchBean.setStationId(StationCategory.Categories.PLOWFOLDER.toString());
						for(Job j : jobDAO.readAll(jobSearchBean)){
							if(JobType.JobTypes.PRINTING_POPLINE.toString().contains(pfMachineType)){
								j.setJobType(lookupDAO.read(JobType.JobTypes.PRINTING_POPLINE.toString(), JobType.class));
								// now check the quantities of press and pf jobs to make sure they are full for pop lines
								if(j.getQuantityNeeded() != null){
									if(JobType.JobTypes.PRINTING_3UP.toString().equals(j.getJobType().getId())){
										int modulo = j.getQuantityNeeded() % 3;
										if(modulo != 0){
											j.setQuantityNeeded(j.getQuantityNeeded() + 3 - modulo);
										}
									}
									if(JobType.JobTypes.PRINTING_2UP.toString().equals(j.getJobType().getId())){
										int modulo = j.getQuantityNeeded() % 2;
										if(modulo != 0){
											j.setQuantityNeeded(j.getQuantityNeeded() + 2 - modulo);
										}
									}
								}
								jobDAO.update(j);
							} else
							if(JobType.JobTypes.PRINTING_FLYFOLDER.toString().contains(pfMachineType)){
								j.setJobType(lookupDAO.read(JobType.JobTypes.PRINTING_FLYFOLDER.toString(), JobType.class));
								jobDAO.update(j);
							} else {
								j.setJobType(lookupDAO.read(JobType.JobTypes.PRINTING_PLOWFOLDER.toString(), JobType.class));
								jobDAO.update(j);
							}
							if(roll != null && roll.getRollTag() == null){
								roll.setRollTag(pfMachineType);
							}
						}
						if(JobType.JobTypes.PRINTING_POPLINE.toString().contains(pfMachineType)){
							if(job.getQuantityNeeded() != null){
								if(JobType.JobTypes.PRINTING_3UP.toString().equals(job.getJobType().getId())){
									int modulo = job.getQuantityNeeded() % 3;
									if(modulo != 0){
										job.setQuantityNeeded(job.getQuantityNeeded() + 3 - modulo);
									}
								}
								if(JobType.JobTypes.PRINTING_2UP.toString().equals(job.getJobType().getId())){
									int modulo = job.getQuantityNeeded() % 2;
									if(modulo != 0){
										job.setQuantityNeeded(job.getQuantityNeeded() + 2 - modulo);
									}
								}
							}
						}
						getJobDAO().update(job);
						//set the order status from accepted to 'on prod' and also set its production mode
						if(job.getOrderId() != null){
							Order or = orderDAO.read(job.getOrderId());
							or.setProductionMode(modeOption);
							if(Order.OrderStatus.ACCEPTED.toString().equals(or.getStatus())){
								or.setStatus(Order.OrderStatus.ONPROD.toString());
								//orderDAO.update(job.getOrder());
								/*
								String[] updateOrderStatus = {job.getOrder().getOrderId()+"", job.getOrder().getStatus()};
								OrderHandler.broadcastStatus(updateOrderStatus);
								*/
								Map<String, Object> ssePayload=new HashMap<String, Object>();
								ssePayload.put("orderId", job.getOrderId());
								ssePayload.put("status", or.getStatus());
								Event event=new Event(EventTarget.OrderStatus, false, null, ssePayload);
								notificationService.broadcast(event);
							}
							orderDAO.update(or);
							// TODO also update the jobs prodmode
						}
					}
				}
				
				//Check if we need to Redo the Workflow 
				// TODO what about the case of Esprint orders
				for(Part p : theParts){
					//if(!Order.OrderSources.ESPRINT.getName().equals(job.getOrder().getSource())){
						WFSPartWorkflow oldPartWorkflow = p.getPartWorkFlowOnProd();
						//String oldWorkflow = (oldPartWorkflow != null && oldPartWorkflow.getWorkflow() != null && 
						//		oldPartWorkflow.getWorkflow().getName().toLowerCase().contains("popline"))? "POPLINE" : "PLOWFOLDER";
						if (oldPartWorkflow == null || (!rollWidth.equals(oldPartWorkflow.getRollWidth()))) {
							// set the older workflow along with its data supports to obsolete
							//WFSPartWorkflow oldPartWorkflow = job.getPart().getPartWorkFlowOnProd();
							if (oldPartWorkflow != null) {
								oldPartWorkflow.setWfStatus(WFSProductionStatus.statuses.OBSOLETE.getName());
								oldPartWorkflow.setLastUpdateDate(new Date());
								oldPartWorkflow.setLastUpdateId(p.getCreatorId());
								wfsWorkflowHandler.updatePartWorkflow(oldPartWorkflow);
								for (WFSDataSupport dsIter : p.getDataSupportsOnProd()) {
									if (!dsIter.getName().equalsIgnoreCase(WFSDataSupport.NAME_DOWNLOAD)) {
										dsIter.setProductionStatus(
												lookupDAO.read(WFSProductionStatus.statuses.OBSOLETE.getName(),
														WFSProductionStatus.class));
										dsIter.setLastUpdateDate(new Date());
										dsIter.setLastUpdateId(p.getCreatorId());
										wfsDataSupportHandler.update(dsIter);
									}
								}
							}
							/*if (JobType.JobTypes.PRINTING_POPLINE.toString().contains(pfMachineType)) {
								List<Object> parameters = new ArrayList<Object>();
								parameters.add(job.getPart());
								parameters.add(rollWidth);
								NDispatcher.getDispatcher().publish(Topics.topic("cap/events/part/popline"),
										parameters);
							} else {*/
								List<Object> parameters = new ArrayList<Object>();
								parameters.add(p);
								parameters.add(rollWidth);
								NDispatcher.getDispatcher().publish(Topics.topic("cap/events/part/scheduled"),
										parameters);
							//}
						} else if (!p.isPartWorkFlowOnProdReady()) {
							List<Object> parameters = new ArrayList<Object>();
							parameters.add(p);
							parameters.add(rollWidth);
							NDispatcher.getDispatcher().publish(Topics.topic("cap/events/part/scheduled"),
									parameters);
						}
					//}else{
						
						
					//}
				}
				
				if(roll != null && jobAssignedToRoll){
					/*if(!RollStatus.statuses.ASSIGNED.toString().equals(roll.getStatus().getId())){
						roll.setStatus(lookupDAO.read(RollStatus.statuses.SCHEDULED.toString(), RollStatus.class));
					}else{
						//roll.setMachineOrdering(0);
					}*/
					
					//  update the Utilization
					roll.setUtilization(Math.round((roll.getHours() * machineHandler.getDefaultMachineSpeed(StationCategory.Categories.PRESS.toString()) * 100) / (float)roll.getLength()));
					getRollDAO().update(roll);
					
					//update best sheets and wastes
					Preference lengthWhenSheetChangedValue = lookupDAO.read("LENGTHSHEETCHANGED", Preference.class);
					Float lengthWhenSheetChanged = (float) 10000; // in mm, usually 10m
					if(lengthWhenSheetChangedValue != null ){
						lengthWhenSheetChanged = Float.parseFloat(lengthWhenSheetChangedValue.getName());
					}
					Float waste = (float) 0;
					Set<String> existingSheets = new HashSet<String>();
					for(Job j : roll.getJobs()){
						Part pr = partDAO.read(j.getPartNum());
						String sheetSize = printingTimeCalculator.getBestSheetHeight(roll, j,
								machineHandler.getBsValue(), machineHandler.getUseOptimizedSheetAlgo());
						j.setBestSheetUsed(sheetSize);
						getJobDAO().update(j);
						
						if(!"S0".equals(sheetSize)){
							if(pr.getBestSheetWaste() != null){
								waste += pr.getBestSheetWaste() * j.getQuantityNeeded();
							}
							existingSheets.add(sheetSize);
						}else{
							existingSheets.add(sheetSize+pr.getLength());
						}
					}
					waste = waste + ((existingSheets.size() - 1) * lengthWhenSheetChanged);// account for 10m sheet change between jobs
					roll.setCalculatedWaste(waste);
					getRollDAO().update(roll);
				}
				//update the scheduled/unscheduled hours on the press station
				/*if(station != null){
					station.setScheduledHours(station.getScheduledHours() + totalHours);
					station.setUnscheduledHours(station.getUnscheduledHours() - totalHours);
					stationDAO.update(station);
				}*/	
			}
			if(jobAssignedToRoll){
				// if the roll is on a machine (assigned to it), then copy the raster files to the printer server
				Roll tmpRoll = roll;
				if(RollStatus.statuses.ASSIGNED.toString().equals(roll.getStatus().getId()) && roll.getMachineId() != null && !roll.getMachineId().isEmpty()){
					Runnable task = new Runnable() {
						@Override
						public void run() {
								try {
									machineHandler.copyRasterFilesToPrinter(tmpRoll);
								} catch (IOException e) {
									logger.error("Error occurred while copying rasterFiles to printer : " + e.getMessage(),e); 
									//throw new PersistenceException(e);
								} catch (Exception e) {
									logger.error("Error occurred while copying rasterFiles to printer : " + e.getMessage(),e); 
									//throw new PersistenceException(e);
								}
						}
					};
					executor.execute(task);
				}
				// broadcast roll status changed
				Map<String, Object> ssePayload=new HashMap<String, Object>();
				ssePayload.put("RollId", roll.getRollId());
				ssePayload.put("status", roll.getStatus().getId());
				Event event=new Event(EventTarget.RollStatus, false, null, ssePayload);
				notificationService.broadcast(event);
				
				return rollId;
			}
			return 0;
		} catch (Exception ex) {
			
			Event event=new Event(EventTarget.RollStatus, true, ex.getLocalizedMessage(), rollId);
			notificationService.broadcast(event);
			
			logger.error("Error occurred producing Roll : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Un-Assigning a roll from the machines/printers;
	 */
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void unassignRoll(Integer rollId, String executingUserId) throws PersistenceException {
		try {
			if(rollId != null){
				Roll roll = rollDAO.read(rollId);
				if(roll != null){
					roll.setMachineId(null);
					if(roll.getJobs().isEmpty()){
						roll.setStatus(lookupDAO.read(RollStatus.statuses.AVAILABLE.toString(), RollStatus.class));
					}else{
						roll.setStatus(lookupDAO.read(RollStatus.statuses.SCHEDULED.toString(), RollStatus.class));
						for(Job j : roll.getJobs()){
							j.setMachineId(null);
							jobHandler.update(j);
						}
					}
					
					roll.setMachineOrdering(0);
					getRollDAO().update(roll);
					// broadcast roll status changed
					Map<String, Object> ssePayload=new HashMap<String, Object>();
					ssePayload.put("RollId", roll.getRollId());
					ssePayload.put("status", roll.getStatus().getId());
					Event event=new Event(EventTarget.RollStatus, false, null, ssePayload);
					notificationService.broadcast(event);
				}
			}
		} catch (Exception ex) {
			logger.error("Error occurred un-assigning the roll from the machine : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
		
	/** 
	 * Calls the corresponding delete method on the RollDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public boolean delete(Roll bean) throws PersistenceException {
		try {
			boolean result = getRollDAO().delete(bean);
			if(result){
				Event event=new Event(EventTarget.Roll, false, null, bean.getRollId());
				notificationService.broadcast(event);
			}
			return result;
		} catch (Exception ex) {
			logger.error("Error occurred deleting a Roll with id '" + (bean == null ? null : bean.getRollId()) + "' : " + ex.getMessage(),ex);
			Event event=new Event(EventTarget.Roll, true, ex.getLocalizedMessage(), bean.getRollId());
			notificationService.broadcast(event);
			throw new PersistenceException(ex);
		}
	}

	/** 
	 * A convenience method which calls readAll(RollSearchBean) with a
	 * null search bean. 
	 *
	 * @see #readAll(RollSearchBean)
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Roll> readAll() throws PersistenceException{
		return this.readAll(null);    
	}
	
	/** 
	 * Calls the corresponding readAll method on the RollDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Roll> readAll(RollSearchBean searchBean) throws PersistenceException{
		try{
			return getRollDAO().readAll(searchBean);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a list of Rolls : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Roll> fetchRoll(){
		return getRollDAO().fetchRoll();
	}
	/** 
	 * Calls the corresponding read method on the RollDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Roll read(Integer rollId) throws PersistenceException{
		try{
			return getRollDAO().read(rollId);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a Roll with id '" + rollId + "' : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/**
	 * A method that checks if the roll has a child roll of type leftover
	 */
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Roll getLeftOverRoll(Integer rollId) throws PersistenceException {
		Roll result = null;
		RollSearchBean rsb = new RollSearchBean();
		rsb.setParentRollId(rollId);
		rsb.setRollType(RollType.types.LEFTOVER.toString());
		List<Roll> rolls = rollDAO.readAll(rsb);
		if(!rolls.isEmpty() && rollId != null){
			result = rolls.get(0);
		}
		return result;
	}
	 
	/**
	 * A method that checks if the roll with rollId has a produced roll
	 */
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Roll getProducedRoll(Integer rollId) throws PersistenceException {
		Roll result = null;
		RollSearchBean rsb = new RollSearchBean();
		rsb.setRollId(rollId);
		rsb.setRollType(RollType.types.PRODUCED.toString());
		List<Roll> rolls = rollDAO.readAll(rsb);
		if(!rolls.isEmpty() && rollId != null){
			result = rolls.get(0);
		}
		return result;
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Integer getCount() {
		return rollDAO.getCount();
	}
	
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Roll> fullSearch(String word, Integer maxResult, Integer offset) {	
		return rollDAO.fullSearch(word, maxResult, offset);		
	}
	
	/**
	 * @return the RollDAO
	 */
	public RollDAO getRollDAO() {
		return rollDAO;
	}

	/**
	 * @param dao the RollDAO to set
	 */
	public void setRollDAO(RollDAO dao) {
		this.rollDAO = dao;
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
	 * @return the jobDAO
	 */
	public JobDAO getJobDAO() {
		return jobDAO;
	}

	/**
	 * @param jobDAO the jobDAO to set
	 */
	public void setJobDAO(JobDAO jobDAO) {
		this.jobDAO = jobDAO;
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

	public NotificationService getNotificationService() {
		return notificationService;
	}

	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Roll getOnProdRollByMachine(String machineId) {
		RollSearchBean search = new RollSearchBean();
		search.setMachineId(machineId);
		search.setStatus(RollStatus.statuses.ONPROD.getName() );
		Roll roll = null;
		try {
			List<Roll> rolls = readAll(search);
			if(rolls.size() > 0)
				roll = rolls.get(0);
		} catch (PersistenceException e) {
			LogUtils.error("Error occurred while getting onProd rolls for machine: "+machineId);
		}
		return roll;
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Integer> getIdsList() {		
		return rollDAO.getIdsList();
	}

 
}