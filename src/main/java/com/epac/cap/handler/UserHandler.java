package com.epac.cap.handler;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.User;
import com.epac.cap.model.UserRole;
import com.epac.cap.repository.UserDAO;
import com.epac.cap.repository.UserSearchBean;

import java.util.List;

/**
 * Interacts with User data.  Uses UserDAO for entity persistence.
 *
 * @author walid
 *
 */
@Service
public class UserHandler{
	
	@Autowired
	private UserDAO userDAO;

	private static Logger logger = Logger.getLogger(UserHandler.class);
	
	public static final int USER_ID_MAX_LENGTH = 35;
	public static final String DEFAULT_PASSWORD_DISPLAY = "******";
	
	/***
	 * No arg constructor.  This is the preferred constructor.
	 */
	public UserHandler(){  }
  
	/** 
	 * Calls the corresponding create method on the UserDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public void create(User bean) throws PersistenceException {
		try {
			//generate the user id
			generateUserId(bean);
			
			if(!bean.getRolesOrigin().isEmpty()){
				for(String r : bean.getRolesOrigin()){
					UserRole ur = new UserRole(bean.getUserId(), r);
					bean.getUserRoles().add(ur);
				}
			}
			//encrypt the password
			String encryptedPassword = encryptPassword(bean.getLoginPassword());
			bean.setLoginPassword(encryptedPassword);
			
			getUserDAO().create(bean);		   
		} catch (Exception ex) {
			logger.error("Error occurred creating a User : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	/**
	 * A method that generates the user id that can be used to uniquely identify a user.
	 * The generation is based on using the user email to get a new unique string that can be used as the id.
	 */
	public void generateUserId(User bean){
		String userId = null;
		 if (!StringUtils.isEmpty(bean.getEmail())){
			 userId = bean.getEmail().trim();
			 userId = userId.replaceAll("[@\\.\\-\\+]", "");
			 if(userId.length() > USER_ID_MAX_LENGTH){
				 userId = userId.substring(0, USER_ID_MAX_LENGTH);
			 }
			 bean.setUserId(userId);
		 }
	}
	
	/**
	 * A method used to encrypt the password
	 */
	public String encryptPassword(String originalPassword){
		BCryptPasswordEncoder encoder = new  BCryptPasswordEncoder(11);
		String encryptedPassword = "";
		if(!StringUtils.isEmpty(originalPassword)){
			encryptedPassword = encoder.encode(originalPassword);
		}
		return encryptedPassword;
	}
	
	/** 
	 * Calls the corresponding update method on the UserDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(User bean) throws PersistenceException {
		try {
			if(!bean.getRolesOrigin().isEmpty()){
				bean.getUserRoles().clear();
				for(String r : bean.getRolesOrigin()){
					UserRole ur = new UserRole(bean.getUserId(), r);
					bean.getUserRoles().add(ur);
				}
			}else{
				bean.getUserRoles().clear();
			}
			//encrypt the password
			if(!DEFAULT_PASSWORD_DISPLAY.equals(bean.getLoginPassword())){
				String encryptedPassword = encryptPassword(bean.getLoginPassword());
				bean.setLoginPassword(encryptedPassword);
			}else{//restore original login password as it was not changed
				User originalUserBean = getUserDAO().read(bean.getUserId());
				if(originalUserBean != null){
					bean.setLoginPassword(originalUserBean.getLoginPassword());
				}
			}
			getUserDAO().update(bean);		   
		} catch (Exception ex) {
			logger.error("Error occurred updating a User : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
		
	/** 
	 * Calls the corresponding delete method on the UserDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public boolean delete(User bean) throws PersistenceException {
		try {
			return getUserDAO().delete(bean);
		} catch (Exception ex) {
			logger.error("Error occurred deleting a User with id '" + (bean == null ? null : bean.getUserId()) + "' : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}

	/** 
	 * A convenience method which calls readAll(UserSearchBean) with a
	 * null search bean. 
	 *
	 * @see #readAll(UserSearchBean)
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<User> readAll() throws PersistenceException{
		return this.readAll(null);    
	}
	
	/** 
	 * Calls the corresponding readAll method on the UserDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<User> readAll(UserSearchBean searchBean) throws PersistenceException{
		try{
			return getUserDAO().readAll(searchBean);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a list of Users : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Calls the corresponding read method on the UserDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public User read(String userId) throws PersistenceException{
		try{
			return getUserDAO().read(userId);
			
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a User with id '" + userId + "' : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}

	/** 
	 * Calls the corresponding read method on the UserDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public User loadUserByEmail(String email) throws PersistenceException{
		try{
			return getUserDAO().findByEmail(email);		
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a User with email '" + email + "' : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Calls the corresponding read method on the UserDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public User loadUserByUserName(String userName) throws PersistenceException{
		try{
			return getUserDAO().findByUserName(userName);		
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a User with Login name '" + userName + "' : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/**
	 * @return the UserDAO
	 */
	public UserDAO getUserDAO() {
		return userDAO;
	}

	/**
	 * @param dao the UserDAO to set
	 */
	public void setUserDAO(UserDAO dao) {
		this.userDAO = dao;
	}
 
}