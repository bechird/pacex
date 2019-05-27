package com.epac.cap.handler;

import static org.junit.Assert.*;

import com.epac.cap.model.Machine;
import com.epac.cap.model.Role;
import static com.epac.cap.common.StringUtil.getRandomString;
import static com.epac.cap.common.NumberUtil.getRandomInteger;
import static com.epac.cap.common.DateUtil.isBetweenDays;
import com.epac.cap.repository.RoleSearchBean;
import com.epac.cap.test.BaseTest;
import com.epac.cap.handler.UserHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.stereotype.Service;

/**
 * Test class for the RoleHandler class.
 * 
 * @author walid
 *
 */
@Service
public class RoleHandlerTest extends BaseTest{
	private static final Logger logger = Logger.getLogger(RoleHandlerTest.class);
	@Resource
	private RoleHandler roleHandler;
	@Resource
	private UserHandler userHandler = null;

	private Role createNewRole() throws Exception{
		Role bean = new Role();
		bean.setCreatedDate(new Date());
		bean.setCreatorId("junit");
		bean.setLastUpdateDate(new Date());
		bean.setLastUpdateId("junit");
		bean.setRoleId(getRandomString(50));
		bean.setRoleName(getRandomString(100));					
		bean.setRoleDescription(getRandomString(10));					
		
		return bean;
	}
	
	@Test
	public void testCreate() throws Exception{
		Role bean = createNewRole();
		roleHandler.create(bean);
	}
	
	private Role getRandomRole() throws Exception{
		Role bean = null;
		RoleSearchBean searchBean = new RoleSearchBean();
		searchBean.setMaxResults(100);
		List<Role> results = roleHandler.readAll(searchBean);
		if(results != null && !results.isEmpty()){
			bean = results.get(getRandomInteger(results.size()));
		}else{
			bean = createNewRole();
			roleHandler.create(bean);		
		}
		
		return bean;		
	}	

	@Test
	public void testReadAll() throws Exception{
		getRandomRole();//ensures there's at least 1 record in the db
		List<Role> roleResults = roleHandler.readAll();
		assertNotNull("readAll return null",roleResults);
		assertFalse("readAll returned an empty list",roleResults.isEmpty());		
	}
	
	@Test
	public void testUpdate() throws Exception{
		Role role = getRandomRole();
		role.setLastUpdateId("junit");
		role.setLastUpdateDate(new Date());
		roleHandler.update(role);
	}

	@Test
	public void testReadValidId() throws Exception{
		Role role = getRandomRole();
		Role readRole = roleHandler.read(role.getRoleId());
		assertNotNull("read return null",readRole);
		assertEquals("read returned incorrect object by id",role, readRole);		
	}

	@Test
	public void testReadInvalidId() throws Exception{
		getRandomRole();
		Role readRole = roleHandler.read("-1");
		assertNull("read didnt return null as expected",readRole);		
	}
		
	@Test
	public void testDeleteExisting() throws Exception{		
		Role role = getRandomRole();
		roleHandler.delete(role);
	}
	
	@Test
	public void testDeleteNew() throws Exception{
		Role bean = createNewRole();
		roleHandler.create(bean);
		roleHandler.delete(bean);
	}
		
	@Test
	public void testReadAllBlankSearchBean() throws Exception{
		getRandomRole();//ensures there's at least 1 record in the db
		RoleSearchBean searchBean = new RoleSearchBean();
		List<Role> roleResults = roleHandler.readAll(searchBean);
		assertNotNull("readAll return null",roleResults);
		assertFalse("readAll returned an empty list",roleResults.isEmpty());
	}

	@Test
	public void testReadAllSearchBeanFields() throws Exception{
		getRandomRole();//ensures there's at least 1 record in the db
		RoleSearchBean searchBean = new RoleSearchBean();
		searchBean.setMaxResults(1);
		List<Role> roleResults = roleHandler.readAll(searchBean);
		assertNotNull("readAll return null",roleResults);
		assertFalse("readAll returned an empty list",roleResults.isEmpty());
		assertTrue("readAll did not honor max results",roleResults.size() == searchBean.getMaxResults());
	}

	@Test
	public void testReadAllCompleteSearchBean() throws Exception{				
		Role role = getRandomRole();
		RoleSearchBean searchBean = new RoleSearchBean();
		Calendar cal = Calendar.getInstance();
		searchBean.setRoleId(role.getRoleId());
		searchBean.setRoleName(role.getRoleName());
		searchBean.setRoleDescription(role.getRoleDescription());

		if(role.getCreatedDate() != null){
			cal.setTime(role.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("role.getCreatedDate() was null so not including it in the criteria");
		}

		if(role.getLastUpdateDate() != null){
			cal.setTime(role.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("role.getLastUpdateDate() was null so not including it in the criteria");
		}

		List<Role> roleResults = roleHandler.readAll(searchBean);
		assertSearchResults(roleResults,role);
	}
	
	@Test
	public void testReadAllSearchBeanRoleId() throws Exception{
		Role role = getRandomRole();
		RoleSearchBean searchBean = new RoleSearchBean();
					
		searchBean.setRoleId(role.getRoleId());
		List<Role> roleResults = roleHandler.readAll(searchBean);
		assertSearchResults(roleResults,role);
		for(Role currBean : roleResults){
			assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getRoleId(),currBean.getRoleId());
		}
	}
	
	@Test
	public void testReadAllSearchBeanRoleName() throws Exception{
		Role role = getRandomRole();
		RoleSearchBean searchBean = new RoleSearchBean();
					
		searchBean.setRoleName(role.getRoleName());
		List<Role> roleResults = roleHandler.readAll(searchBean);
		assertSearchResults(roleResults,role);
		for(Role currBean : roleResults){
			if(searchBean.getRoleName() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getRoleName().toLowerCase().indexOf(searchBean.getRoleName().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanRoleDescription() throws Exception{
		Role role = getRandomRole();
		RoleSearchBean searchBean = new RoleSearchBean();
					
		searchBean.setRoleDescription(role.getRoleDescription());
		List<Role> roleResults = roleHandler.readAll(searchBean);
		assertSearchResults(roleResults,role);
		for(Role currBean : roleResults){
			if(searchBean.getRoleDescription() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getRoleDescription().toLowerCase().indexOf(searchBean.getRoleDescription().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanCreatedDate() throws Exception{
		Role role = getRandomRole();
		RoleSearchBean searchBean = new RoleSearchBean();
					
		if(role.getCreatedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(role.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("role.getCreatedDate() was null so not including it in the criteria");
		}
						
		List<Role> roleResults = roleHandler.readAll(searchBean);
		assertSearchResults(roleResults,role);
		for(Role currBean : roleResults){
			if(searchBean.getCreatedDateFrom() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", isBetweenDays(currBean.getCreatedDate(),searchBean.getCreatedDateFrom(),searchBean.getCreatedDateTo()));
			
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanLastUpdateDate() throws Exception{
		Role role = getRandomRole();
		RoleSearchBean searchBean = new RoleSearchBean();
					
		if(role.getLastModifiedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(role.getLastUpdateDate() == null ? role.getLastModifiedDate() : role.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("role.getLastUpdateDate() was null so not including it in the criteria");
		}
						
		List<Role> roleResults = roleHandler.readAll(searchBean);
		assertSearchResults(roleResults,role);
		for(Role currBean : roleResults){
			if(searchBean.getLastUpdateDateFrom() != null){
				boolean resultMatchesCreatedDate = isBetweenDays(currBean.getCreatedDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				boolean resultMatchesLastUpdateDate= currBean.getLastUpdateDate() == null || isBetweenDays(currBean.getLastUpdateDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				assertTrue("Search results returned a bean that didn't match the criteria", (resultMatchesCreatedDate || resultMatchesLastUpdateDate));
			}
		}
	}
	
	private void assertSearchResults(
			List<Role> roleResults,
			Role role) {
		if(roleResults == null || roleResults.isEmpty()){
			roleResults = new ArrayList<Role>();
			roleResults.add(role);
		}
		assertNotNull("readAll return null",roleResults);
		assertFalse("readAll returned an empty list",roleResults.isEmpty());
		assertTrue("readAll did not return a bean used to populate the search bean",roleResults.contains(role));
	}	
}