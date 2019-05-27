package com.epac.cap.handler;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.Customer;
import com.epac.cap.repository.CustomerDAO;
import com.epac.cap.repository.CustomerSearchBean;

import java.util.List;

/**
 * Interacts with Customer data.  Uses CustomerDAO for entity persistence.
 * @author walid
 *
 */
@Service
public class CustomerHandler {
	@Autowired
	private CustomerDAO customerDAO;

	private static Logger logger = Logger.getLogger(CustomerHandler.class);
	
	/***
	 * No arg constructor.  This is the preferred constructor.
	 */
	public CustomerHandler(){  }
  
	/** 
	 * Calls the corresponding create method on the CustomerDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public void create(Customer bean) throws PersistenceException {
		try {
			getCustomerDAO().create(bean);		   
		} catch (Exception ex) {
			bean.setCustomerId(null);
			logger.error("Error occurred creating a Customer : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Calls the corresponding update method on the CustomerDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(Customer bean) throws PersistenceException {
		try {
			getCustomerDAO().update(bean);		   
		} catch (Exception ex) {
			logger.error("Error occurred updating a Customer : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
		
	/** 
	 * Calls the corresponding delete method on the CustomerDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public boolean delete(Customer bean) throws PersistenceException {
		try {
			return getCustomerDAO().delete(bean);
		} catch (Exception ex) {
			logger.error("Error occurred deleting a Customer with id '" + (bean == null ? null : bean.getCustomerId()) + "' : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}

	/** 
	 * A convenience method which calls readAll(CustomerSearchBean) with a
	 * null search bean. 
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Customer> readAll() throws PersistenceException{
		return this.readAll(null);    
	}
	
	/** 
	 * Calls the corresponding readAll method on the CustomerDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Customer> readAll(CustomerSearchBean searchBean) throws PersistenceException{
		try{
			return getCustomerDAO().readAll(searchBean);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a list of Customers : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Calls the corresponding read method on the CustomerDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Customer read(Integer customerId) throws PersistenceException{
		try{
			return getCustomerDAO().read(customerId);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a Customer with id '" + customerId + "' : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Calls the corresponding read method on the CustomerDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Customer readByEmail(String email) throws PersistenceException{
		try{
			CustomerSearchBean csb = new CustomerSearchBean();
			csb.setEmail(email);
			List<Customer> result = customerDAO.readAll(csb);
			if(!result.isEmpty()){
				return result.get(0);
			} else return null;
			
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a Customer with email '" + email + "' : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	         	
	/**
	 * @return the CustomerDAO
	 */
	public CustomerDAO getCustomerDAO() {
		return customerDAO;
	}

	/**
	 * @param dao the CustomerDAO to set
	 */
	public void setCustomerDAO(CustomerDAO dao) {
		this.customerDAO = dao;
	}
 
}