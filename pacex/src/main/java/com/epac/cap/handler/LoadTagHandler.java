package com.epac.cap.handler;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.Job;
import com.epac.cap.model.JobStatus;
import com.epac.cap.model.LoadTag;
import com.epac.cap.model.Machine;
import com.epac.cap.model.MachineStatus;
import com.epac.cap.model.Order;
import com.epac.cap.model.Roll;
import com.epac.cap.model.Station;
import com.epac.cap.model.StationCategory;
import com.epac.cap.repository.JobDAO;
import com.epac.cap.repository.LoadTagDAO;
import com.epac.cap.repository.LoadTagSearchBean;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.repository.MachineDAO;
import com.epac.cap.repository.OrderDAO;
import com.epac.cap.repository.RollDAO;
import com.epac.cap.repository.StationDAO;
import com.epac.cap.service.NotificationService;
import com.epac.cap.sse.beans.Event;
import com.epac.cap.sse.beans.EventTarget;

/**
 * Interacts with Load tag data.  Uses LoadTagDAO for entity persistence.
 * @author walid
 *
 */
@Service
public class LoadTagHandler {
	
	@Autowired
	private LoadTagDAO loadTagDAO;
	
	@Autowired
	private JobDAO jobDAO;
	
	@Autowired
	private MachineDAO machineDAO;
	
	@Autowired
	private StationDAO stationDAO;
	
	@Autowired
	private RollDAO rollDAO;
	
	@Autowired
	private OrderDAO orderDAO;
	
	@Autowired
	private LogHandler logHandler;
	
	@Autowired
	private JobHandler jobHandler;
	
	@Autowired
	private LookupDAO lookupDAO;
	
	@Autowired
	private NotificationService notificationService;

	private static Logger logger = Logger.getLogger(LoadTagHandler.class);
	
	/***
	 * No arg constructor.  This is the preferred constructor.
	 */
	public LoadTagHandler(){  }
  
	/** 
	 * Calls the corresponding create method on the LoadTagDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public void create(LoadTag bean) throws PersistenceException {
		try {
			// Set the machine Id
			if(bean.getJobId() != null){
				Job j = jobDAO.read(bean.getJobId());
				//if(j != null && j.getMachineId() != null && !j.getMachineId().isEmpty()){
					bean.setMachineId(j.getMachineId());
					//update the job produced quantity
					j.setQuantityProduced(j.getQuantityProduced() + bean.getQuantity());
					j.setLastUpdateDate(new Date());
					j.setLastUpdateId(bean.getCreatorId());
					jobDAO.update(j);
				//}
			}
			getLoadTagDAO().create(bean);		   
		} catch (Exception ex) {
			bean.setLoadTagId(null);
			logger.error("Error occurred creating a LoadTag : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Calls the corresponding create method on the LoadTagDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void createFromProd(LoadTag bean) throws PersistenceException {
		try {
			// Set the machine Id
			if(bean.getJobId() != null){
				Job jb = jobDAO.read(bean.getJobId());
				if(jb != null && jb.getMachineId() != null && !jb.getMachineId().isEmpty()){
					bean.setMachineId(jb.getMachineId());
				}
			}
			getLoadTagDAO().create(bean);
			//now update the produced quantity of the job and if the job produced quantity is satisfied then set the job as complete
			//and move the current job of the machine to the next job in the queue if it exists
			Job theJob = jobDAO.read(bean.getJobId());
			if(theJob != null){
				theJob.setQuantityProduced(theJob.getQuantityProduced() + bean.getQuantity());
				//Integer qtyRequested = theJob.getQuantityNeeded() - jobHandler.getOvers(theJob.getOrder().getOrderPart().getQuantity()) - jobHandler.getUnders(theJob.getOrder().getOrderPart().getQuantity());
				//if(theJob.getQuantityProduced() >= qtyRequested){ //job is complete
				if(theJob.getQuantityProduced() + theJob.getTotalWaste() + bean.getWaste() >= (float)theJob.getQuantityNeeded()){//job is complete/partial_complete
					Roll theRoll = null;
					Machine theMachine = null;
					if(theJob.getRollId() != null){
						theRoll = rollDAO.read(theJob.getRollId());
						if(theRoll != null){
							theMachine = machineDAO.read(theRoll.getMachineId());
						}
					}else{
						theMachine = machineDAO.read(theJob.getMachineId());
					}
					if(theMachine != null){
						// if station is plow folder or cover press, set status to complete and set next job as current
						// if this is the plow folder and job is the last one on the roll, keep it as current, it will become complete manually through the interruption screen
						// for the other stations, the job should become complete when operator sets it manually through the interruption screen
						if((StationCategory.Categories.PLOWFOLDER.toString().equals(theMachine.getStationId()) && !theRoll.getJobs().isEmpty() && !theJob.getJobId().equals(theRoll.getJobs().last().getJobId())) ||
								StationCategory.Categories.COVERPRESS.toString().equals(theMachine.getStationId())){
							if(theJob.getQuantityProduced() >= (float)theJob.getQuantityNeeded()){ // fully complete
								theJob.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.COMPLETE.toString(), JobStatus.class));
							}else{ // partial complete (there is waster)
								theJob.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.COMPLETE_PARTIAL.toString(), JobStatus.class));
							}
							// If cover press, set the order status to Complete if all other order jobs were complete
							if(StationCategory.Categories.COVERPRESS.toString().equals(theMachine.getStationId()) &&
									jobHandler.isOnFinalStation(theJob, null)){
								/*JobSearchBean jsb = new JobSearchBean();
								jsb.setOrderId(theJob.getOrder().getOrderId());
								jsb.setStatusesNotIn(Arrays.asList(JobStatus.JobStatuses.COMPLETE.toString(), JobStatus.JobStatuses.CANCELLED.toString()));
								List<Job> resultJobs = jobDAO.readAll(jsb);
								if(resultJobs.size() == 0){
									theJob.getOrder().setStatus(Order.OrderStatus.COMPLETE.toString());
									orderDAO.update(theJob.getOrder());
									OrderHandler.broadcastStatus(theJob.getOrder().getStatus());
								}*/
								jobHandler.checkToCompensateOrderJobs(theJob, bean.getCreatorId());
							}
						}
						Station theStation = stationDAO.read(theMachine.getStationId());
						if(theStation != null){
							if(Station.inputTypes.Roll.toString().equals(theStation.getInputType())){//case of the plow folder
								//if there are more jobs on the roll then activate the next one
								Job nextJobOnRoll = null;
								if(theRoll != null && theRoll.getJobs().size() > 1){
									for(Job j : theRoll.getJobs()){
										if(!j.getJobId().equals(theJob.getJobId()) && j.getRollOrdering() != null && theJob.getRollOrdering() != null &&
												j.getRollOrdering() >= theJob.getRollOrdering() && 
												!JobStatus.JobStatuses.COMPLETE.toString().equals(j.getJobStatus().getId()) &&
												!JobStatus.JobStatuses.COMPLETE_PARTIAL.toString().equals(j.getJobStatus().getId())){
											nextJobOnRoll = j;
											break;
										}
									}
									if(nextJobOnRoll != null){
										if(!JobStatus.JobStatuses.COMPLETE.toString().equals(nextJobOnRoll.getJobStatus().getId()) &&
												!JobStatus.JobStatuses.COMPLETE_PARTIAL.toString().equals(nextJobOnRoll.getJobStatus().getId())){
											theMachine.setCurrentJob(nextJobOnRoll);
											nextJobOnRoll.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.RUNNING.toString(), JobStatus.class));
											machineDAO.update(theMachine);
										}
									}else{// else move to the next roll assigned to the machine
										if(theMachine.getAssignedRolls().size() > 0){
											Roll nextRoll = theMachine.getAssignedRolls().iterator().next();
											nextJobOnRoll = (nextRoll != null && !nextRoll.getJobs().isEmpty()) ? nextRoll.getJobs().iterator().next() : null;
											theMachine.setCurrentJob(nextJobOnRoll);
											nextJobOnRoll.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.RUNNING.toString(), JobStatus.class));
											machineDAO.update(theMachine);
										}
									}
									// Also activate the next station job if currently we are on the hunkeler
									jobHandler.activateNextStationJobs(theJob, null, 0, bean.getCreatorId());
								}
							}else{
								// If cover press then move to the next job automatically
								// Also activate the next station job
								if(StationCategory.Categories.COVERPRESS.toString().equals(theMachine.getStationId())){
									if(theMachine.getRunningAndAssignedJobs().size() > 0){
										for(Job jb : theMachine.getRunningAndAssignedJobs()){
											if(!theMachine.getCurrentJob().getJobId().equals(jb.getJobId())){
												theMachine.setCurrentJob(jb);
												jb.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.RUNNING.toString(), JobStatus.class));
												break;
											}
										}
										if(theMachine.getRunningAndAssignedJobs().size() == 1){ // last job, set machine to 'ON'
											theMachine.setStatus(lookupDAO.read(MachineStatus.statuses.ON.toString(), MachineStatus.class));
										}
									}else{// no more assigned jobs, then set the machine status to 'On' rather than 'Running'
										theMachine.setStatus(lookupDAO.read(MachineStatus.statuses.ON.toString(), MachineStatus.class));
									}
									machineDAO.update(theMachine);
									// activate next station job
									jobHandler.activateNextStationJobs(theJob, null, 0, bean.getCreatorId());
								}
							}
						}
					}
				}else{
					theJob.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.RUNNING.toString(), JobStatus.class));
				}
				theJob.setLastUpdateDate(new Date());
				theJob.setLastUpdateId(bean.getCreatorId());
				jobDAO.update(theJob);
			}
		} catch (Exception ex) {
			bean.setLoadTagId(null);
			logger.error("Error occurred creating a LoadTag from production dashboard: " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Calls the corresponding update method on the LoadTagDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void updateFromProd(LoadTag bean) throws PersistenceException {
		try {
			//now update the produced quantity of the job and if the job produced quantity is satisfied then set the job as complete
			//otherwise if job already complete but new quantity makes it incomplete then change it to incomplete.
			// first get the older quantity
			LoadTag originalBean = loadTagDAO.read(bean.getLoadTagId());
			int originalQuantity = 0;
			if(originalBean != null){
				originalQuantity = originalBean.getQuantity();
			}
			getLoadTagDAO().update(bean);
			Job theJob = jobDAO.read(bean.getJobId());
			if(theJob != null){
				theJob.setQuantityProduced(theJob.getQuantityProduced() - originalQuantity + bean.getQuantity());
				//Integer qtyRequested = theJob.getQuantityNeeded() - jobHandler.getOvers(theJob.getOrder().getOrderPart().getQuantity()) - jobHandler.getUnders(theJob.getOrder().getOrderPart().getQuantity());
				//if(theJob.getQuantityProduced() >= qtyRequested){//job is complete, set status to complete if not already set
				if(theJob.getQuantityProduced() + theJob.getTotalWaste() + bean.getWaste() - originalBean.getWaste() >= (float)theJob.getQuantityNeeded()){//job is complete/partial_complete, set status to complete if not already set
					Roll theRoll = null;
					Machine theMachine = null;
					if(theJob.getRollId() != null){
						theRoll = rollDAO.read(theJob.getRollId());
						if(theRoll != null){
							theMachine = machineDAO.read(theRoll.getMachineId());
						}
					}else{
						theMachine = machineDAO.read(theJob.getMachineId());
					}
					if(theMachine != null){
						if((StationCategory.Categories.PLOWFOLDER.toString().equals(theMachine.getStationId()) && !theRoll.getJobs().isEmpty() && !theJob.getJobId().equals(theRoll.getJobs().last().getJobId())) ||
								StationCategory.Categories.COVERPRESS.toString().equals(theMachine.getStationId())){
							if(!JobStatus.JobStatuses.COMPLETE.toString().equals(theJob.getJobStatus().getId()) && 
									!JobStatus.JobStatuses.COMPLETE_PARTIAL.toString().equals(theJob.getJobStatus().getId())){
								if(theJob.getQuantityProduced() >= (float)theJob.getQuantityNeeded()){ // fully complete
									theJob.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.COMPLETE.toString(), JobStatus.class));
								}else{ // partial complete (there is waster)
									theJob.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.COMPLETE_PARTIAL.toString(), JobStatus.class));
								}
								// If cover press, set the order status to Complete if all other order jobs were complete
								if(StationCategory.Categories.COVERPRESS.toString().equals(theMachine.getStationId()) &&
										jobHandler.isOnFinalStation(theJob, null)){
									/*JobSearchBean jsb = new JobSearchBean();
									jsb.setOrderId(theJob.getOrder().getOrderId());
									jsb.setStatusesNotIn(Arrays.asList(JobStatus.JobStatuses.COMPLETE.toString(), JobStatus.JobStatuses.CANCELLED.toString()));
									List<Job> resultJobs = jobDAO.readAll(jsb);
									if(resultJobs.size() == 0){
										theJob.getOrder().setStatus(Order.OrderStatus.COMPLETE.toString());
										orderDAO.update(theJob.getOrder());
										OrderHandler.broadcastStatus(theJob.getOrder().getStatus());
									}*/
									jobHandler.checkToCompensateOrderJobs(theJob, bean.getCreatorId());
								}
							}
						}
						Station theStation = stationDAO.read(theMachine.getStationId());
						if(theStation != null){
							if(Station.inputTypes.Roll.toString().equals(theStation.getInputType())){//case of the plow folder
								//if there are more jobs on the roll then activate the next one
								Job nextJobOnRoll = null;
								if(theRoll != null && theRoll.getJobs().size() > 1){
									for(Job j : theRoll.getJobs()){
										if(!j.getJobId().equals(theJob.getJobId()) && j.getRollOrdering() != null && theJob.getRollOrdering() != null &&
												j.getRollOrdering() >= theJob.getRollOrdering() &&
												!JobStatus.JobStatuses.COMPLETE.toString().equals(j.getJobStatus().getId()) &&
												!JobStatus.JobStatuses.COMPLETE_PARTIAL.toString().equals(j.getJobStatus().getId())){
											nextJobOnRoll = j;
											break;
										}
									}
									if(nextJobOnRoll != null){
										if(!JobStatus.JobStatuses.COMPLETE.toString().equals(nextJobOnRoll.getJobStatus().getId()) &&
												!JobStatus.JobStatuses.COMPLETE_PARTIAL.toString().equals(nextJobOnRoll.getJobStatus().getId())){
											theMachine.setCurrentJob(nextJobOnRoll);
											nextJobOnRoll.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.RUNNING.toString(), JobStatus.class));
											machineDAO.update(theMachine);
										}
									}else{// else move to the next roll assigned to the machine
										if(theMachine.getAssignedRolls().size() > 0){
											Roll nextRoll = theMachine.getAssignedRolls().iterator().next();
											nextJobOnRoll = (nextRoll != null && !nextRoll.getJobs().isEmpty()) ? nextRoll.getJobs().iterator().next() : null;
											theMachine.setCurrentJob(nextJobOnRoll);
											nextJobOnRoll.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.RUNNING.toString(), JobStatus.class));
											machineDAO.update(theMachine);
										}
									}
									// Also activate the next station job  if currently we are on the hunkeler
									jobHandler.activateNextStationJobs(theJob, null, 0, bean.getCreatorId());
								}
							}else{
								// If cover press then move to the next job automatically
								// Also activate the next station job
								if(StationCategory.Categories.COVERPRESS.toString().equals(theMachine.getStationId())){
									if(theMachine.getRunningAndAssignedJobs().size() > 0){
										for(Job jb : theMachine.getRunningAndAssignedJobs()){
											if(!theMachine.getCurrentJob().getJobId().equals(jb.getJobId())){
												theMachine.setCurrentJob(jb);
												jb.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.RUNNING.toString(), JobStatus.class));
												break;
											}
										}
										if(theMachine.getRunningAndAssignedJobs().size() == 1){ // last job, set machine to 'ON'
											theMachine.setStatus(lookupDAO.read(MachineStatus.statuses.ON.toString(), MachineStatus.class));
										}
									}else{// no more assigned jobs, then set the machine status to 'On' rather than 'Running'
										theMachine.setStatus(lookupDAO.read(MachineStatus.statuses.ON.toString(), MachineStatus.class));
									}
									machineDAO.update(theMachine);
									// activate next station job
									jobHandler.activateNextStationJobs(theJob, null, 0, bean.getCreatorId());
								}
							}
						}
					}
				}else{//if the job is already complete then set it to incomplete
					if(JobStatus.JobStatuses.COMPLETE.toString().equals(theJob.getJobStatus().getId()) ||
							JobStatus.JobStatuses.COMPLETE_PARTIAL.toString().equals(theJob.getJobStatus().getId())){
						theJob.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.RUNNING.toString(), JobStatus.class));
						// If order complete, set the order status to OnProd
						Order or = orderDAO.read(theJob.getOrderId());
						if(Order.OrderStatus.COMPLETE.toString().equals(or.getStatus())){
							or.setStatus(Order.OrderStatus.ONPROD.toString());
							orderDAO.update(or);
							
							/*
							String[] updateOrderStatus = {theJob.getOrder().getOrderId()+"", theJob.getOrder().getStatus()};
							OrderHandler.broadcastStatus(updateOrderStatus);
							*/
							
							Map<String, Object> ssePayload=new HashMap<String, Object>();
							ssePayload.put("orderId", theJob.getOrderId());
							ssePayload.put("status", or.getStatus());
							Event event=new Event(EventTarget.OrderStatus, false, null, ssePayload);
							notificationService.broadcast(event);
							
						}
					}
				}
				theJob.setLastUpdateDate(new Date());
				theJob.setLastUpdateId(bean.getCreatorId());
				jobDAO.update(theJob);
			}
		} catch (Exception ex) {
			Event event=new Event(EventTarget.OrderStatus, true, ex.getLocalizedMessage(), bean.getLoadTagId());
			notificationService.broadcast(event);
			
			logger.error("Error occurred updating a LoadTag from production dashboard: " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Calls the corresponding update method on the LoadTagDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(LoadTag bean) throws PersistenceException {
		try {
			LoadTag originalBean = loadTagDAO.read(bean.getLoadTagId());
			int originalQuantity = 0;
			if(originalBean != null){
				originalQuantity = originalBean.getQuantity();
				Job theJob = jobDAO.read(bean.getJobId());
				if(theJob != null){
					theJob.setQuantityProduced(theJob.getQuantityProduced() - originalQuantity + bean.getQuantity());
					theJob.setLastUpdateDate(new Date());
					theJob.setLastUpdateId(bean.getCreatorId());
					jobDAO.update(theJob);
				}
			}
			getLoadTagDAO().update(bean);		   
		} catch (Exception ex) {
			logger.error("Error occurred updating a LoadTag : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
		
	/** 
	 * Calls the corresponding delete method on the LoadTagDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public boolean delete(LoadTag bean) throws PersistenceException {
		try {
			if(bean.getJobId() != null){
				Job theJob = jobDAO.read(bean.getJobId());
				if(theJob != null){
					theJob.setQuantityProduced(theJob.getQuantityProduced() - bean.getQuantity());
					theJob.setLastUpdateDate(new Date());
					theJob.setLastUpdateId(bean.getCreatorId());
					jobDAO.update(theJob);
				}
			}
			return getLoadTagDAO().delete(bean);
		} catch (Exception ex) {
			logger.error("Error occurred deleting a LoadTag with id '" + (bean == null ? null : bean.getLoadTagId()) + "' : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}

	/** 
	 * A convenience method which calls readAll(LoadTagSearchBean) with a
	 * null search bean. 
	 *
	 * @see #readAll(LoadTagSearchBean)
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<LoadTag> readAll() throws PersistenceException{
		return this.readAll(null);    
	}
	
	/** 
	 * Calls the corresponding readAll method on the LoadTagDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<LoadTag> readAll(LoadTagSearchBean searchBean) throws PersistenceException{
		try{
			return getLoadTagDAO().readAll(searchBean);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a list of LoadTags : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Calls the corresponding read method on the LoadTagDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public LoadTag read(Integer loadTagId) throws PersistenceException{
		try{
			return getLoadTagDAO().read(loadTagId);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a LoadTag with id '" + loadTagId + "' : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}

	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Integer getCount() {
		return loadTagDAO.getCount();
	}
	
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<LoadTag> fullSearch(String word, Integer maxResult, Integer offset) {	
		return loadTagDAO.fullSearch(word, maxResult, offset);		
	}
	/**
	 * @return the loadTagDAO
	 */
	public LoadTagDAO getLoadTagDAO() {
		return loadTagDAO;
	}

	/**
	 * @param loadTagDAO the loadTagDAO to set
	 */
	public void setLoadTagDAO(LoadTagDAO loadTagDAO) {
		this.loadTagDAO = loadTagDAO;
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

	public NotificationService getNotificationService() {
		return notificationService;
	}

	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}   
 
	
}