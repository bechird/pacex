package com.epac.cap.handler;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.Role;
import com.epac.cap.repository.RoleDAO;
import com.epac.cap.repository.RoleSearchBean;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Interacts with Role data.  Uses RoleDAO for entity persistence.
 *
 * @see com.epac.cap.dao.RoleDAO
 * @author walid
 *
 */
@Service
public class RoleHandler {
	
	@Autowired
	private RoleDAO roleDAO;

	private static Logger logger = Logger.getLogger(RoleHandler.class);
	
	/***
	 * No arg constructor.  This is the preferred constructor.
	 */
	public RoleHandler(){  }
  
	/** 
	 * Calls the corresponding create method on the RoleDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public void create(Role bean) throws PersistenceException {
		try {
			getRoleDAO().create(bean);		   
		} catch (Exception ex) {
			logger.error("Error occurred creating a Role : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Calls the corresponding update method on the RoleDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(Role bean) throws PersistenceException {
		try {
			getRoleDAO().update(bean);		   
		} catch (Exception ex) {
			logger.error("Error occurred updating a Role : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
		
	/** 
	 * Calls the corresponding delete method on the RoleDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public boolean delete(Role bean) throws PersistenceException {
		try {
			return getRoleDAO().delete(bean);
		} catch (Exception ex) {
			logger.error("Error occurred deleting a Role with id '" + (bean == null ? null : bean.getRoleId()) + "' : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}

	/** 
	 * A convenience method which calls readAll(RoleSearchBean) with a
	 * null search bean. 
	 *
	 * @see #readAll(RoleSearchBean)
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Role> readAll() throws PersistenceException{
		return this.readAll(null);    
	}
	
	/** 
	 * Calls the corresponding readAll method on the RoleDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Role> readAll(RoleSearchBean searchBean) throws PersistenceException{
		try{
			return getRoleDAO().readAll(searchBean);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a list of Roles : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	
	/** 
	 * Calls the corresponding read method on the RoleDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Role read(String roleId) throws PersistenceException{
		try{
			return getRoleDAO().read(roleId);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a Role with id '" + roleId + "' : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	         	
	/**
	 * @return the RoleDAO
	 */
	public RoleDAO getRoleDAO() {
		return roleDAO;
	}

	/**
	 * @param dao the RoleDAO to set
	 */
	public void setRoleDAO(RoleDAO dao) {
		this.roleDAO = dao;
	}
 
}