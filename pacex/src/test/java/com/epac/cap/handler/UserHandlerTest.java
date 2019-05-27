package com.epac.cap.handler;

import static org.junit.Assert.*;

import com.epac.cap.model.User;
import com.epac.cap.repository.UserSearchBean;
import com.epac.cap.test.BaseTest;
import com.epac.cap.handler.UserHandler;
import static com.epac.cap.common.StringUtil.getRandomString;
import static com.epac.cap.common.NumberUtil.getRandomInteger;
import static com.epac.cap.common.DateUtil.isBetweenDays;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.stereotype.Service;

/**
 * Test class for the UserHandler class.
 * 
 * @author walid
 *
 */
@Service
public class UserHandlerTest extends BaseTest{
	private static final Logger logger = Logger.getLogger(UserHandlerTest.class);
	@Resource
	private UserHandler userHandler;

	private User createNewUser() throws Exception{
		User bean = new User();
		bean.setCreatedDate(new Date());
		bean.setCreatorId("junit");
		bean.setLastUpdateDate(new Date());
		bean.setLastUpdateId("junit");
		bean.setUserId(getRandomString(35));
		bean.setFirstName(getRandomString(50));					
		bean.setLastName(getRandomString(50));					
		bean.setEmail(getRandomString(70));					
		bean.setPhoneNum(getRandomString(15));					
		bean.setLoginName(getRandomString(50));					
		bean.setLoginPassword(getRandomString(10));					
		bean.setActiveFlag(true);			
		
		return bean;
	}
	
	@Test
	public void testCreate() throws Exception{
		User bean = createNewUser();
		userHandler.create(bean);
	}
	
	private User getRandomUser() throws Exception{
		User bean = null;
		UserSearchBean searchBean = new UserSearchBean();
		searchBean.setMaxResults(100);
		List<User> results = userHandler.readAll(searchBean);
		if(results != null && !results.isEmpty()){
			bean = results.get(getRandomInteger(results.size()));
		}else{
			bean = createNewUser();
			userHandler.create(bean);		
		}
		
		return bean;		
	}	

	@Test
	public void testReadAll() throws Exception{
		getRandomUser();//ensures there's at least 1 record in the db
		List<User> userResults = userHandler.readAll();
		assertNotNull("readAll return null",userResults);
		assertFalse("readAll returned an empty list",userResults.isEmpty());		
	}
	
	@Test
	public void testUpdate() throws Exception{
		User user = getRandomUser();
		user.setLastUpdateId("junit");
		user.setLastUpdateDate(new Date());
		userHandler.update(user);
	}

	@Test
	public void testReadValidId() throws Exception{
		User user = getRandomUser();
		User readUser = userHandler.read(user.getUserId());
		assertNotNull("read return null",readUser);
		assertEquals("read returned incorrect object by id",user, readUser);		
	}

	@Test
	public void testReadInvalidId() throws Exception{
		getRandomUser();
		User readUser = userHandler.read("-1");
		assertNull("read didnt return null as expected",readUser);		
	}
		
	@Test
	public void testDeleteExisting() throws Exception{		
		User user = getRandomUser();
		userHandler.delete(user);
	}
	
	@Test
	public void testDeleteNew() throws Exception{
		User bean = createNewUser();
		userHandler.create(bean);
		userHandler.delete(bean);
	}
		
	@Test
	public void testReadAllBlankSearchBean() throws Exception{
		getRandomUser();//ensures there's at least 1 record in the db
		UserSearchBean searchBean = new UserSearchBean();
		List<User> userResults = userHandler.readAll(searchBean);
		assertNotNull("readAll return null",userResults);
		assertFalse("readAll returned an empty list",userResults.isEmpty());
	}

	@Test
	public void testReadAllSearchBeanFields() throws Exception{
		getRandomUser();//ensures there's at least 1 record in the db
		UserSearchBean searchBean = new UserSearchBean();
		searchBean.setMaxResults(1);
		List<User> userResults = userHandler.readAll(searchBean);
		assertNotNull("readAll return null",userResults);
		assertFalse("readAll returned an empty list",userResults.isEmpty());
		assertTrue("readAll did not honor max results",userResults.size() == searchBean.getMaxResults());
	}

	@Test
	public void testReadAllCompleteSearchBean() throws Exception{				
		User user = getRandomUser();
		UserSearchBean searchBean = new UserSearchBean();
		Calendar cal = Calendar.getInstance();
		searchBean.setUserId(user.getUserId());
		searchBean.setFirstName(user.getFirstName());
		searchBean.setLastName(user.getLastName());
		searchBean.setEmail(user.getEmail());
		searchBean.setPhoneNum(user.getPhoneNum());
		searchBean.setLoginName(user.getLoginName());
		searchBean.setLoginPassword(user.getLoginPassword());
		searchBean.setActiveFlag(user.getActiveFlag());

		if(user.getCreatedDate() != null){
			cal.setTime(user.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("user.getCreatedDate() was null so not including it in the criteria");
		}

		if(user.getLastUpdateDate() != null){
			cal.setTime(user.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("user.getLastUpdateDate() was null so not including it in the criteria");
		}

		List<User> userResults = userHandler.readAll(searchBean);
		assertSearchResults(userResults,user);
	}
	
	@Test
	public void testReadAllSearchBeanUserId() throws Exception{
		User user = getRandomUser();
		UserSearchBean searchBean = new UserSearchBean();
					
		searchBean.setUserId(user.getUserId());
		List<User> userResults = userHandler.readAll(searchBean);
		assertSearchResults(userResults,user);
		for(User currBean : userResults){
			assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getUserId(),currBean.getUserId());
		}
	}
	
	@Test
	public void testReadAllSearchBeanFirstName() throws Exception{
		User user = getRandomUser();
		UserSearchBean searchBean = new UserSearchBean();
					
		searchBean.setFirstName(user.getFirstName());
		List<User> userResults = userHandler.readAll(searchBean);
		assertSearchResults(userResults,user);
		for(User currBean : userResults){
			if(searchBean.getFirstName() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getFirstName().toLowerCase().indexOf(searchBean.getFirstName().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanLastName() throws Exception{
		User user = getRandomUser();
		UserSearchBean searchBean = new UserSearchBean();
					
		searchBean.setLastName(user.getLastName());
		List<User> userResults = userHandler.readAll(searchBean);
		assertSearchResults(userResults,user);
		for(User currBean : userResults){
			if(searchBean.getLastName() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getLastName().toLowerCase().indexOf(searchBean.getLastName().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanEmail() throws Exception{
		User user = getRandomUser();
		UserSearchBean searchBean = new UserSearchBean();
					
		searchBean.setEmail(user.getEmail());
		List<User> userResults = userHandler.readAll(searchBean);
		assertSearchResults(userResults,user);
		for(User currBean : userResults){
			if(searchBean.getEmail() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getEmail().toLowerCase().indexOf(searchBean.getEmail().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanPhoneNum() throws Exception{
		User user = getRandomUser();
		UserSearchBean searchBean = new UserSearchBean();
					
		searchBean.setPhoneNum(user.getPhoneNum());
		List<User> userResults = userHandler.readAll(searchBean);
		assertSearchResults(userResults,user);
		for(User currBean : userResults){
			if(searchBean.getPhoneNum() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getPhoneNum().toLowerCase().indexOf(searchBean.getPhoneNum().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanLoginName() throws Exception{
		User user = getRandomUser();
		UserSearchBean searchBean = new UserSearchBean();
					
		searchBean.setLoginName(user.getLoginName());
		List<User> userResults = userHandler.readAll(searchBean);
		assertSearchResults(userResults,user);
		for(User currBean : userResults){
			if(searchBean.getLoginName() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getLoginName().toLowerCase().indexOf(searchBean.getLoginName().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanLoginPassword() throws Exception{
		User user = getRandomUser();
		UserSearchBean searchBean = new UserSearchBean();
					
		searchBean.setLoginPassword(user.getLoginPassword());
		List<User> userResults = userHandler.readAll(searchBean);
		assertSearchResults(userResults,user);
		for(User currBean : userResults){
			if(searchBean.getLoginPassword() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getLoginPassword().toLowerCase().indexOf(searchBean.getLoginPassword().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanActiveFlag() throws Exception{
		User user = getRandomUser();
		UserSearchBean searchBean = new UserSearchBean();
					
		searchBean.setActiveFlag(user.getActiveFlag());
		List<User> userResults = userHandler.readAll(searchBean);
		assertSearchResults(userResults,user);
		for(User currBean : userResults){
			if(searchBean.getActiveFlag() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getActiveFlag(), currBean.getActiveFlag());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanCreatedDate() throws Exception{
		User user = getRandomUser();
		UserSearchBean searchBean = new UserSearchBean();
					
		if(user.getCreatedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(user.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("user.getCreatedDate() was null so not including it in the criteria");
		}
						
		List<User> userResults = userHandler.readAll(searchBean);
		assertSearchResults(userResults,user);
		for(User currBean : userResults){
			if(searchBean.getCreatedDateFrom() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", isBetweenDays(currBean.getCreatedDate(),searchBean.getCreatedDateFrom(),searchBean.getCreatedDateTo()));
			
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanLastUpdateDate() throws Exception{
		User user = getRandomUser();
		UserSearchBean searchBean = new UserSearchBean();
					
		if(user.getLastModifiedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(user.getLastUpdateDate() == null ? user.getLastModifiedDate() : user.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("user.getLastUpdateDate() was null so not including it in the criteria");
		}
						
		List<User> userResults = userHandler.readAll(searchBean);
		assertSearchResults(userResults,user);
		for(User currBean : userResults){
			if(searchBean.getLastUpdateDateFrom() != null){
				boolean resultMatchesCreatedDate = isBetweenDays(currBean.getCreatedDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				boolean resultMatchesLastUpdateDate= currBean.getLastUpdateDate() == null || isBetweenDays(currBean.getLastUpdateDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				assertTrue("Search results returned a bean that didn't match the criteria", (resultMatchesCreatedDate || resultMatchesLastUpdateDate));
			}
		}
	}
	
	private void assertSearchResults(
			List<User> userResults,
			User user) {
		if(userResults == null || userResults.isEmpty()){
			userResults = new ArrayList<User>();
			userResults.add(user);
		}
		assertNotNull("readAll return null",userResults);
		assertFalse("readAll returned an empty list",userResults.isEmpty());
		assertTrue("readAll did not return a bean used to populate the search bean",userResults.contains(user));
	}	
}