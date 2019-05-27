package com.epac.cap.handler;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.Job;
import com.epac.cap.model.Roll;
import com.epac.cap.model.Station;
import com.epac.cap.repository.RollDAO;
import com.epac.cap.repository.StationDAO;
import com.epac.cap.repository.StationSearchBean;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Interacts with Station data.  Uses StationDAO for entity persistence.
 * @author walid
 *
 */
@Service
public class StationHandler {
	
	@Autowired
	private StationDAO stationDAO;
	
	@Autowired
	private RollDAO rollDAO;
	
	@Autowired
	private JobHandler jobHandler;

	private static Logger logger = Logger.getLogger(StationHandler.class);
	
	/***
	 * No arg constructor.  This is the preferred constructor.
	 */
	public StationHandler(){  }
  
	/** 
	 * Calls the corresponding create method on the StationDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public void create(Station bean) throws PersistenceException {
		try {
			getStationDAO().create(bean);		   
		} catch (Exception ex) {
			logger.error("Error occurred creating a Station : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Calls the corresponding update method on the StationDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void moveUp(Integer itemId, String stationId, String level) throws PersistenceException {
		try {
		   Station station = stationDAO.read(stationId);
		   if(station != null){
			    List<Job> jobsList = jobHandler.getStationJobs(stationId);
				TreeSet<Job> jobsListTS = new TreeSet<Job>(new JobsByMachineOrderingComparator());
				jobsListTS.addAll(jobsList);
				
			    if(Station.inputTypes.Roll.toString().equalsIgnoreCase(station.getInputType())){
			    	TreeSet<Roll> rollsListTS = new TreeSet<Roll>(new RollsByMachineOrderingComparator());
			    	rollsListTS.addAll(jobHandler.getStationRolls(jobsList));
			    	if("one".equals(level)){
			    		Roll prevRoll = null;
			    		for(Roll roll : rollsListTS){
			    			if(roll.getRollId().equals(itemId)){
			    				//switch the machine ordering between our roll and its previous roll
			    				if(prevRoll != null){
			    					Integer rollSwitchInt = roll.getMachineOrdering();
			    					roll.setMachineOrdering(prevRoll.getMachineOrdering()); 
			    					rollDAO.update(roll);
			    					prevRoll.setMachineOrdering(rollSwitchInt); 
			    					rollDAO.update(prevRoll);
			    				}
			    			}
			    			prevRoll = roll;
			    		}
			    	}else if("top".equals(level)){
			    		int positionIndex = 0;
			    		Roll prevRoll = null;
			    		Integer orderingOfTargetEntry = null;
			    		Integer orderingOfSelectedEntry = null;
			    		for(Roll roll : rollsListTS){
			    			if(positionIndex >= 3){
			    				if(positionIndex == 3){
			    					orderingOfTargetEntry = roll.getMachineOrdering();
			    				}else{
			    					if(roll.getRollId().equals(itemId)){
			    						orderingOfSelectedEntry = roll.getMachineOrdering();
			    						roll.setMachineOrdering(orderingOfTargetEntry); rollDAO.update(roll);
			    						prevRoll.setMachineOrdering(orderingOfSelectedEntry); rollDAO.update(prevRoll);
			    						break;
			    					}
			    					prevRoll.setMachineOrdering(roll.getMachineOrdering()); rollDAO.update(prevRoll);
			    				}
			    			}
			    			positionIndex ++;
			    			prevRoll = roll;
			    		}
			    	}
			    }else{
			    	if("one".equals(level)){
				    	Job prevJob = null;
				    	for(Job job : jobsListTS){
			    			if(job.getJobId().equals(itemId)){
			    				//switch the machine ordering between our job and its previous job
			    				if(prevJob != null){
			    					Integer jobSwitchInt = job.getMachineOrdering();
			    					job.setMachineOrdering(prevJob.getMachineOrdering()); 
			    					jobHandler.update(job);
			    					prevJob.setMachineOrdering(jobSwitchInt); 
			    					jobHandler.update(prevJob);
			    				}
			    			}
			    			prevJob = job;
			    		}
				    }else if("top".equals(level)){
				    	int positionIndex = 0;
			    		Job prevJob = null;
			    		Integer orderingOfTargetEntry = null;
			    		Integer orderingOfSelectedEntry = null;
			    		for(Job job : jobsListTS){
			    			if(positionIndex >= 3){
			    				if(positionIndex == 3){
			    					orderingOfTargetEntry = job.getMachineOrdering();
			    				}else{
			    					if(job.getJobId().equals(itemId)){
			    						orderingOfSelectedEntry = job.getMachineOrdering();
			    						job.setMachineOrdering(orderingOfTargetEntry); jobHandler.update(job);
			    						prevJob.setMachineOrdering(orderingOfSelectedEntry); jobHandler.update(prevJob);
			    						break;
			    					}
			    					prevJob.setMachineOrdering(job.getMachineOrdering()); jobHandler.update(prevJob);
			    				}
			    			}
			    			positionIndex ++;
			    			prevJob = job;
			    		}
				    }
			    }
		   }
		} catch (Exception ex) {
			logger.error("Error occurred while changing the order of some jobs on the overview dashboard section: " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Used to move up the jobs/rolls on the station so they get produced first
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(Station bean) throws PersistenceException {
		try {
			getStationDAO().update(bean);		   
		} catch (Exception ex) {
			logger.error("Error occurred updating a Station : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
		
	/** 
	 * Calls the corresponding delete method on the StationDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public boolean delete(Station bean) throws PersistenceException {
		try {
			return getStationDAO().delete(bean);
		} catch (Exception ex) {
			logger.error("Error occurred deleting a Station with id '" + (bean == null ? null : bean.getStationId()) + "' : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}

	/** 
	 * A convenience method which calls readAll(StationSearchBean) with a
	 * null search bean. 
	 *
	 * @see #readAll(StationSearchBean)
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Station> readAll() throws PersistenceException{
		return this.readAll(null);    
	}
	
	/** 
	 * Calls the corresponding readAll method on the StationDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Station> readAll(StationSearchBean searchBean) throws PersistenceException{
		try{
			return getStationDAO().readAll(searchBean);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a list of Stations : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	
	/** 
	 * Calls the corresponding read method on the StationDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Station read(String stationId) throws PersistenceException{
		try{
			return getStationDAO().read(stationId);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a Station with id '" + stationId + "' : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Calls the corresponding fetchStationsMenu method on the StationDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<String[]> readStationsForMenu() throws PersistenceException{
		try{
			return getStationDAO().fetchStationsMenu();
		} catch (Exception ex) {
			logger.error("Error occurred retrieving stations For menu" + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	     	 
	/** 
	 * Calls the corresponding readStationName method on the StationDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public String readStationName(String stationId) throws PersistenceException{
		try{
			return getStationDAO().readStationName(stationId);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving station's name" + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/**
	 * @return the StationDAO
	 */
	public StationDAO getStationDAO() {
		return stationDAO;
	}

	/**
	 * @param dao the StationDAO to set
	 */
	public void setStationDAO(StationDAO dao) {
		this.stationDAO = dao;
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

	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Object[]> getNameOfSations() {
		
		try{
			return getStationDAO().getNameOfSations();
		} catch (Exception ex) {
			logger.error("Error occurred retrieving stations name" + ex.getMessage(),ex);
		}
		return new ArrayList<Object[]>();
	}
	public List<Station> fetchStation(){
		return stationDAO.fetchStation();
	}
 
}