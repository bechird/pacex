package com.epac.cap.handler;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.DefaultStation;
import com.epac.cap.model.DefaultStationId;
import com.epac.cap.model.PartCategory;
import com.epac.cap.model.Station;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.repository.LookupSearchBean;
import com.epac.cap.repository.StationDAO;
import com.epac.cap.repository.StationSearchBean;

import java.util.List;

/**
 * Interacts with Station data.  Uses StationDAO for entity persistence.
 * @author walid
 *
 */
@Service
public class DefaultStationHandler {
	
	@Autowired
	private StationDAO stationDAO;
	
	@Autowired
	private LookupDAO lookupDAO;

	private static Logger logger = Logger.getLogger(DefaultStationHandler.class);
	
	/***
	 * No arg constructor.  This is the preferred constructor.
	 */
	public DefaultStationHandler(){  }
  
	/** 
	 * Calls the corresponding create method on the StationDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public void create(DefaultStation bean) throws PersistenceException {
		try {
			getStationDAO().createDefault(bean);
		} catch (Exception ex) {
			logger.error("Error occurred creating a Default Station : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Calls the corresponding update method on the StationDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(DefaultStation bean) throws PersistenceException {
		try {
			getStationDAO().updateDefault(bean);
		} catch (Exception ex) {
			logger.error("Error occurred updating a Default Station : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
		
	/** 
	 * Calls the corresponding delete method on the StationDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public boolean delete(DefaultStation bean) throws PersistenceException {
		try {
			return getStationDAO().deleteDefault(bean);
		} catch (Exception ex) {
			logger.error("Error occurred deleting a Default Station with id '" + (bean == null ? null : bean.getId()) + "' : " + ex.getMessage(),ex); 
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
	public List<DefaultStation> readAll() throws PersistenceException{
		return this.readAll(null);
	}
	
	/** 
	 * Calls the corresponding readAll method on the StationDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<DefaultStation> readAll(StationSearchBean searchBean) throws PersistenceException{
		try{
			return getStationDAO().readAllDefault(searchBean);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a list of Default Stations : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Calls the corresponding read method on the StationDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public DefaultStation read(DefaultStationId id) throws PersistenceException{
		try{
			return getStationDAO().readDefault(id);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a Default Station with id '" + id + "' : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Calls the corresponding read method on the StationDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public DefaultStation read(String id) throws PersistenceException{
		try{
			String[] splittedId = id.split("_");
			DefaultStationId dsid = new DefaultStationId(splittedId[0], splittedId[1], splittedId[2], splittedId[3]);
			return getStationDAO().readDefault(dsid);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a Default Station with id '" + id + "' : " + ex.getMessage(),ex);
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
 
	
}