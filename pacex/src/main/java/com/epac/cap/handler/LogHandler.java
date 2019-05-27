package com.epac.cap.handler;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.BatchStatus;
import com.epac.cap.model.CoverBatch;
import com.epac.cap.model.CoverBatchJob;
import com.epac.cap.model.CoverSection;
import com.epac.cap.model.JobStatus;
import com.epac.cap.model.Log;
import com.epac.cap.model.LogCause;
import com.epac.cap.model.LogResult;
import com.epac.cap.model.Machine;
import com.epac.cap.model.MachineStatus;
import com.epac.cap.model.Roll;
import com.epac.cap.model.SectionStatus;
import com.epac.cap.model.Station;
import com.epac.cap.model.StationCategory;
import com.epac.cap.repository.CoverBatchDAO;
import com.epac.cap.repository.CoverSectionDAO;
import com.epac.cap.repository.LogDAO;
import com.epac.cap.repository.LogSearchBean;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.repository.MachineDAO;
import com.epac.cap.repository.RollDAO;
import com.epac.cap.repository.StationDAO;

import jp.co.fujifilm.xmf.oc.Printer;

/**
 * Interacts with Log data.  Uses LogDAO for entity persistence.
 * @author walid
 *
 */
@Service
public class LogHandler {
	
	@Autowired
	private JobHandler jobHandler;
	
	@Autowired
	private RollHandler rollHandler;
	
	@Autowired
	private LogDAO logDAO;
	
	@Autowired
	private RollDAO rollDAO;
	
	@Autowired
	private StationDAO stationDAO;
	
	@Autowired
	private LookupDAO lookupDAO;
	
	@Autowired
	private MachineDAO machineDAO;
	
	@Autowired
	private CoverSectionDAO coverSectionDAO;
	
	@Autowired
	private CoverBatchDAO coverBatchDAO;
	
	@Autowired
	private PrintersHandler printersHandler;

	private static Logger logger = Logger.getLogger(LogHandler.class);
	
	/***
	 * No arg constructor.  This is the preferred constructor.
	 */
	public LogHandler(){  }
  
	/** 
	 * Calls the corresponding create method on the LogDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public void create(Log bean) throws PersistenceException {
		try {
			prepareBeans(bean);
			getLogDAO().create(bean);		   
		} catch (Exception ex) {
			bean.setLogId(null);
			logger.error("Error occurred creating a Log : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Calls the corresponding update method on the LogDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(Log bean) throws PersistenceException {
		try {
			prepareBeans(bean);
			getLogDAO().update(bean);		   
		} catch (Exception ex) {
			logger.error("Error occurred updating a Log : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}
	}
	
	private void prepareBeans(Log log){
		/*if(log.getMachine() != null && !StringUtils.isEmpty(log.getMachine().getMachineId())){
			log.setMachine(machineDAO.read(log.getMachine().getMachineId()));
		}else{
			log.setMachine(null);
		}*/
		/*if(log.getRoll() != null && log.getRoll().getRollId() != null){
			log.setRoll(rollDAO.read(log.getRoll().getRollId()));
		}else{
			log.setRoll(null);
		}*/
		/*if(log.getCurrentJob() != null && log.getCurrentJob().getJobId() != null){
			log.setCurrentJob(jobDAO.read(log.getCurrentJob().getJobId()));
		}else{
			log.setCurrentJob(null);
		}*/
		if(log.getRollId() != null && log.getRollId() <= 0){
			log.setRollId(null);
		}
		if(log.getLogResult() != null && !StringUtils.isEmpty(log.getLogResult().getId())){
			log.setLogResult(lookupDAO.read(log.getLogResult().getId(), LogResult.class));
		}else{
			log.setLogResult(null);
		}
		if(log.getLogCause() != null && !StringUtils.isEmpty(log.getLogCause().getId())){
			log.setLogCause(lookupDAO.read(log.getLogCause().getId(), LogCause.class));
		}else{
			log.setLogCause(null);
		}
	}
	
	/** 
	 * Called from the production dash-board to handle the interruption when the machine is running
	 * Will add a new log entry and then update the jobs/rolls/machine status depending on which station we are in...
	 * Note: In case of Stopping jobs: Existence of a left over roll means most probably the jobs are stopped completely, 
	 * else there is chance of continuing these jobs.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public void handleInterruption(Log bean, String executingUser) throws PersistenceException {
		try {
			Station station = null;
			Machine machine = machineDAO.read(bean.getMachineId());
			if(machine != null){
				station = stationDAO.read(machine.getStationId());
			}
			bean.setCreatedDate(new Date());
			bean.setCreatorId(executingUser);
			createFromProd(bean);
			if(Log.LogEvent.STOP.toString().equals(bean.getEvent()) && !station.getInputType().equals(Station.inputTypes.Batch.getName())){
				jobHandler.setCompletedJobsFromProd(bean, executingUser);
			}else if(Log.LogEvent.COMPLETE.toString().equals(bean.getEvent()) && !station.getInputType().equals(Station.inputTypes.Batch.getName())){
				jobHandler.setAllCompletedJobsFromProd(bean, executingUser);
			}
			//update the machine status
			if(machine != null){
				if(LogCause.causes.ISSUE.toString().equals(bean.getLogCause().getId())){
					machine.setStatus(lookupDAO.read(MachineStatus.statuses.OUTSERVICE.toString(), MachineStatus.class));
				}else if(LogCause.causes.ONOFF.toString().equals(bean.getLogCause().getId())){
					machine.setStatus(lookupDAO.read(MachineStatus.statuses.OFF.toString(), MachineStatus.class));
			    }else if(LogCause.causes.SERVICE.toString().equals(bean.getLogCause().getId())){
					machine.setStatus(lookupDAO.read(MachineStatus.statuses.SERVICE.toString(), MachineStatus.class));
			    }else{
					machine.setStatus(lookupDAO.read(MachineStatus.statuses.ON.toString(), MachineStatus.class));
				}
				// Complete jobs for COVERPRESS section on prod
				if (StationCategory.Categories.COVERPRESS.toString().equals(machine.getStationId())
						&& station.getInputType().equals(Station.inputTypes.Batch.getName())
						&& Log.LogEvent.COMPLETE.toString().equals(bean.getEvent())) {
					CoverSection section = coverSectionDAO.read(bean.getSectionId());
					for (CoverBatchJob cvjob : section.getJobs()) {
						if ((cvjob.getQuantity() + cvjob.getJob().getQuantityProduced()) == cvjob.getJob()
								.getQuantityNeeded()) {
							cvjob.getJob().setJobStatus(
									lookupDAO.read(JobStatus.JobStatuses.COMPLETE.toString(), JobStatus.class));
							jobHandler.activateNextStationJobs(cvjob.getJob(), new Roll(), 0, executingUser);
						} else {
							cvjob.getJob().setJobStatus(
									lookupDAO.read(JobStatus.JobStatuses.COMPLETE_PARTIAL.toString(), JobStatus.class));
						}
						cvjob.getJob().setQuantityProduced(cvjob.getJob().getQuantityProduced() + cvjob.getQuantity());
					}
					section.setStatus(lookupDAO.read(SectionStatus.statuses.PRINTED.toString(), SectionStatus.class));
					coverSectionDAO.update(section);
					// complete batch when all sections are completed otherwise
					// partially complete
					CoverBatch batch = coverBatchDAO.read(section.getBatchId());
					boolean notComplete = false;
					for (CoverSection sect : batch.getSections()) {
						if (!sect.getStatus().getId().equals(SectionStatus.statuses.PRINTED.toString())) {
							if (section.getCoverSectionId() != sect.getCoverSectionId()) {
								notComplete = true;
								break;
							} else {
								continue;
							}
						}
					}
					if (notComplete) {
						batch.setStatus(
								lookupDAO.read(BatchStatus.statuses.COMPLETE_PARTIAL.toString(), BatchStatus.class));
					} else {
						batch.setStatus(lookupDAO.read(BatchStatus.statuses.COMPLETE.toString(), BatchStatus.class));
					}
					coverBatchDAO.update(batch);
				}
				// reset current job on complete/stop
				if(Log.LogEvent.STOP.toString().equals(bean.getEvent()) || Log.LogEvent.COMPLETE.toString().equals(bean.getEvent())){
					machine.setCurrentJob(null);
				}
				// If Epac mode Press, send corresponding stop/hold request to the printer
				Printer printer = printersHandler.getPrinter(machine.getMachineId());
				
				if(StationCategory.Categories.PRESS.toString().equals(machine.getStationId()) 
						&& printer != null && printer.isEpacMode()){
					if(Log.LogEvent.STOP.toString().equals(bean.getEvent())){
						printer.stop();
					}else if(Log.LogEvent.PAUSE.toString().equals(bean.getEvent()) || Log.LogEvent.SERVICE.toString().equals(bean.getEvent())){
						// stop either way, when resumed will calculate jobs
						printer.stop();
					}
				}
				
				machineDAO.update(machine);
			}
		} catch (Exception ex) {
			bean.setLogId(null);
			logger.error("Error occurred while handling the interruption from production dashboard: " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Calls the corresponding create method on the LogDAO. Called from the production dash-board
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void createFromProd(Log bean) throws PersistenceException {
		try {
			// update the finish time of the last log entry for the machine
			Machine machine = machineDAO.read(bean.getMachineId());
			if(machine != null && machine.getLogs().size() > 0){
				Log prevLog = machine.getLogs().iterator().next();
				prevLog.setFinishTime(new Date());
				getLogDAO().update(prevLog);
			}
			Roll childRoll = null;
			if(machine != null && StationCategory.Categories.PRESS.toString().equals(machine.getStationId())){
				 childRoll = rollHandler.getLeftOverRoll(bean.getRollId());
			}
	    	if(Log.LogEvent.COMPLETE.toString().equals(bean.getEvent())){
	    		if(machine != null && StationCategory.Categories.PRESS.toString().equals(machine.getStationId())){
	    			bean.setLogResult(lookupDAO.read(LogResult.results.ROLL_PRODUCED.toString(), LogResult.class));
	    		}else{
	    			bean.setLogResult(lookupDAO.read(LogResult.results.TASKENDED.toString(), LogResult.class));
	    		}
	    		bean.setLogCause(lookupDAO.read(LogCause.causes.JOBSCOMPLETE.toString(), LogCause.class));
	    	}
	    	if(Log.LogEvent.STOP.toString().equals(bean.getEvent())){
	    		if(LogCause.causes.ISSUE.toString().equals(bean.getLogCause().getId())){
	    			bean.setLogResult(lookupDAO.read(LogResult.results.REPAIR.toString(), LogResult.class));
	    		}else if(LogCause.causes.SERVICE.toString().equals(bean.getLogCause().getId())){
	    			bean.setLogResult(lookupDAO.read(LogResult.results.SERVICE.toString(), LogResult.class));
	    		}else{
	    			if(machine != null && StationCategory.Categories.PRESS.toString().equals(machine.getStationId())){
		    			bean.setLogResult(lookupDAO.read(LogResult.results.ROLL_PRODUCED.toString(), LogResult.class));
		    		}else{
		    			bean.setLogResult(lookupDAO.read(LogResult.results.TASKENDED.toString(), LogResult.class));
		    		}
	    		}
	    	}
			if (machine != null && StationCategory.Categories.COVERPRESS.toString().equals(machine.getStationId())
					&& machine.getCoverSectionOnProd() != null) {
				bean.setSectionId(machine.getCoverSectionOnProd().getCoverSectionId());
			}
	    	if(Log.LogEvent.PAUSE.toString().equals(bean.getEvent())){
	    		if(LogCause.causes.ISSUE.toString().equals(bean.getLogCause().getId())){
    				bean.setLogResult(lookupDAO.read(LogResult.results.REPAIR.toString(), LogResult.class));
    			}
    			if(LogCause.causes.SERVICE.toString().equals(bean.getLogCause().getId())){
    				bean.setLogResult(lookupDAO.read(LogResult.results.SERVICE.toString(), LogResult.class));
    			}
	    	}
	    	bean.setStartTime(new Date());
			prepareBeans(bean);
			
			//if there is a left over roll created and no log entry with that left over roll, then should add a log entry for it
			if(childRoll != null){
				LogSearchBean lsb = new LogSearchBean();
				lsb.setRollId(childRoll.getRollId());
				lsb.setResult(LogResult.results.LEFTOVERROLL.toString());
				lsb.setMachineId(bean.getMachineId());
				List<Log> result = readAll(lsb);
				if(result.isEmpty()){
					Log leftoverRollLog = new Log();
					leftoverRollLog.setMachineId(bean.getMachineId());
					leftoverRollLog.setRollId(childRoll.getRollId());
					leftoverRollLog.setEvent(bean.getEvent());
					leftoverRollLog.setLogResult(lookupDAO.read(LogResult.results.LEFTOVERROLL.toString(), LogResult.class));
					leftoverRollLog.setLogCause(bean.getLogCause());
					leftoverRollLog.setCurrentJobId(bean.getCurrentJobId());
					leftoverRollLog.setRollLength(childRoll.getLength());
					leftoverRollLog.setStartTime(new Date());
					leftoverRollLog.setFinishTime(new Date());
					leftoverRollLog.setCounterFeet(bean.getCounterFeet());
					leftoverRollLog.setCreatedDate(new Date());
					leftoverRollLog.setCreatorId(bean.getCreatorId());
					prepareBeans(leftoverRollLog);
					getLogDAO().create(leftoverRollLog);
				}
			}
			//now add the new log entry
			getLogDAO().create(bean);
		} catch (Exception ex) {
			bean.setLogId(null);
			logger.error("Error occurred creating a Log record from production dashboard: " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Calls the corresponding delete method on the LogDAO.
	 *
	 */
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public boolean delete(Log bean) throws PersistenceException {
		try {
			return getLogDAO().delete(bean);
		} catch (Exception ex) {
			logger.error("Error occurred deleting a Log with id '" + (bean == null ? null : bean.getLogId()) + "' : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}

	/** 
	 * A convenience method which calls readAll(LogSearchBean) with a
	 * null search bean. 
	 *
	 * @see #readAll(LogSearchBean)
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Log> readAll() throws PersistenceException{
		return this.readAll(null);    
	}
	
	/** 
	 * Calls the corresponding readAll method on the LogDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Log> readAll(LogSearchBean searchBean) throws PersistenceException{
		try{
			return getLogDAO().readAll(searchBean);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a list of Logs : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Calls the corresponding read method on the LogDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Log read(Integer logId) throws PersistenceException{
		try{
			return getLogDAO().read(logId);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a Log with id '" + logId + "' : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}

	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Integer getCount() {
		return logDAO.getCount();
	}
	
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Log> fullSearch(String word, Integer maxResult, Integer offset) {	
		return logDAO.fullSearch(word, maxResult, offset);		
	}
	
	/**
	 * @return the LogDAO
	 */
	public LogDAO getLogDAO() {
		return logDAO;
	}

	/**
	 * @param dao the LogDAO to set
	 */
	public void setLogDAO(LogDAO dao) {
		this.logDAO = dao;
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
	 
	public MachineDAO getMachineDAO() {
		return machineDAO;
	}*/

	/**
	 * @param machineDAO the machineDAO to set
	 
	public void setMachineDAO(MachineDAO machineDAO) {
		this.machineDAO = machineDAO;
	}*/

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

	
 
}