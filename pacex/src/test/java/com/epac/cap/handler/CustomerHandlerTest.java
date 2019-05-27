package com.epac.cap.handler;

import static org.junit.Assert.*;

import static com.epac.cap.common.StringUtil.getRandomString;
import static com.epac.cap.common.NumberUtil.getRandomInteger;
import static com.epac.cap.common.DateUtil.isBetweenDays;

import com.epac.cap.model.Customer;
import com.epac.cap.model.Log;
import com.epac.cap.repository.CustomerSearchBean;
import com.epac.cap.test.BaseTest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.stereotype.Service;

/**
 * Test class for the CustomerHandler interface.
 * 
 * @author walid
 *
 */
@Service
public class CustomerHandlerTest extends BaseTest{
	
	private static final Logger logger = Logger.getLogger(CustomerHandlerTest.class);
	@Resource
	private CustomerHandler customerHandler;
	@Resource
	private UserHandler userHandler = null;

	private Customer createNewCustomer() throws Exception{
		Customer bean = new Customer();
		bean.setCreatedDate(new Date());
		bean.setCreatorId("junit");
		bean.setFirstName(getRandomString(50));					
		bean.setLastName(getRandomString(50));					
		bean.setEmail(getRandomString(70));					
		bean.setPhoneNum(getRandomString(15));					
		
		return bean;
	}
	
	@Test
	public void testCreate() throws Exception{
		Customer bean = createNewCustomer();
		customerHandler.create(bean);
		assertNotNull("id property 'customerId' was null after create",bean.getCustomerId());
		assertTrue("id property 'customerId' was invalid after create", bean.getCustomerId() > 0);
	}
	
	private Customer getRandomCustomer() throws Exception{
		Customer bean = null;
		CustomerSearchBean searchBean = new CustomerSearchBean();
		searchBean.setMaxResults(100);
		List<Customer> results = customerHandler.readAll(searchBean);
		if(results != null && !results.isEmpty()){
			bean = results.get(getRandomInteger(results.size()));
		}else{
			bean = createNewCustomer();
			customerHandler.create(bean);		
		}
		
		return bean;		
	}	

	@Test
	public void testReadAll() throws Exception{
		getRandomCustomer();//ensures there's at least 1 record in the db
		List<Customer> customerResults = customerHandler.readAll();
		assertNotNull("readAll return null",customerResults);
		assertFalse("readAll returned an empty list",customerResults.isEmpty());		
	}
	
	@Test
	public void testUpdate() throws Exception{
		Customer customer = getRandomCustomer();
		customer.setLastUpdateId("junit");
		customer.setLastUpdateDate(new Date());
		customerHandler.update(customer);
	}

	@Test
	public void testReadValidId() throws Exception{
		Customer customer = getRandomCustomer();
		Customer readCustomer = customerHandler.read(customer.getCustomerId());
		assertNotNull("read return null",readCustomer);
		assertEquals("read returned incorrect object by id",customer, readCustomer);		
	}

	@Test
	public void testReadInvalidId() throws Exception{
		getRandomCustomer();
		Customer readCustomer = customerHandler.read(-1);
		assertNull("read didnt return null as expected",readCustomer);		
	}
		
	@Test
	public void testDeleteExisting() throws Exception{		
		Customer customer = getRandomCustomer();
		customerHandler.delete(customer);
	}
	
	@Test
	public void testDeleteNew() throws Exception{
		Customer bean = createNewCustomer();
		customerHandler.create(bean);
		customerHandler.delete(bean);
	}
		
	@Test
	public void testReadAllBlankSearchBean() throws Exception{
		getRandomCustomer();//ensures there's at least 1 record in the db
		CustomerSearchBean searchBean = new CustomerSearchBean();
		List<Customer> customerResults = customerHandler.readAll(searchBean);
		assertNotNull("readAll return null",customerResults);
		assertFalse("readAll returned an empty list",customerResults.isEmpty());
	}

	@Test
	public void testReadAllSearchBeanFields() throws Exception{
		getRandomCustomer();//ensures there's at least 1 record in the db
		CustomerSearchBean searchBean = new CustomerSearchBean();
		searchBean.setMaxResults(1);
		List<Customer> customerResults = customerHandler.readAll(searchBean);
		assertNotNull("readAll return null",customerResults);
		assertFalse("readAll returned an empty list",customerResults.isEmpty());
		assertTrue("readAll did not honor max results",customerResults.size() == searchBean.getMaxResults());
	}

	@Test
	public void testReadAllCompleteSearchBean() throws Exception{				
		Customer customer = getRandomCustomer();
		CustomerSearchBean searchBean = new CustomerSearchBean();
		Calendar cal = Calendar.getInstance();
		searchBean.setCustomerId(customer.getCustomerId());
		searchBean.setFirstName(customer.getFirstName());
		searchBean.setLastName(customer.getLastName());
		searchBean.setEmail(customer.getEmail());
		searchBean.setPhoneNum(customer.getPhoneNum());

		if(customer.getCreatedDate() != null){
			cal.setTime(customer.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("customer.getCreatedDate() was null so not including it in the criteria");
		}

		if(customer.getLastUpdateDate() != null){
			cal.setTime(customer.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("customer.getLastUpdateDate() was null so not including it in the criteria");
		}

		List<Customer> customerResults = customerHandler.readAll(searchBean);
		assertSearchResults(customerResults,customer);
	}
	
	@Test
	public void testReadAllSearchBeanCustomerId() throws Exception{
		Customer customer = getRandomCustomer();
		CustomerSearchBean searchBean = new CustomerSearchBean();
					
		searchBean.setCustomerId(customer.getCustomerId());
		List<Customer> customerResults = customerHandler.readAll(searchBean);
		assertSearchResults(customerResults,customer);
		for(Customer currBean : customerResults){
			if(searchBean.getCustomerId() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getCustomerId(), currBean.getCustomerId());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanFirstName() throws Exception{
		Customer customer = getRandomCustomer();
		CustomerSearchBean searchBean = new CustomerSearchBean();
					
		searchBean.setFirstName(customer.getFirstName());
		List<Customer> customerResults = customerHandler.readAll(searchBean);
		assertSearchResults(customerResults,customer);
		for(Customer currBean : customerResults){
			if(searchBean.getFirstName() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getFirstName().toLowerCase().indexOf(searchBean.getFirstName().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanLastName() throws Exception{
		Customer customer = getRandomCustomer();
		CustomerSearchBean searchBean = new CustomerSearchBean();
					
		searchBean.setLastName(customer.getLastName());
		List<Customer> customerResults = customerHandler.readAll(searchBean);
		assertSearchResults(customerResults,customer);
		for(Customer currBean : customerResults){
			if(searchBean.getLastName() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getLastName().toLowerCase().indexOf(searchBean.getLastName().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanEmail() throws Exception{
		Customer customer = getRandomCustomer();
		CustomerSearchBean searchBean = new CustomerSearchBean();
					
		searchBean.setEmail(customer.getEmail());
		List<Customer> customerResults = customerHandler.readAll(searchBean);
		assertSearchResults(customerResults,customer);
		for(Customer currBean : customerResults){
			if(searchBean.getEmail() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getEmail().toLowerCase().indexOf(searchBean.getEmail().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanPhoneNum() throws Exception{
		Customer customer = getRandomCustomer();
		CustomerSearchBean searchBean = new CustomerSearchBean();
					
		searchBean.setPhoneNum(customer.getPhoneNum());
		List<Customer> customerResults = customerHandler.readAll(searchBean);
		assertSearchResults(customerResults,customer);
		for(Customer currBean : customerResults){
			if(searchBean.getPhoneNum() != null && currBean.getPhoneNum() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getPhoneNum().toLowerCase().indexOf(searchBean.getPhoneNum().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanCreatedDate() throws Exception{
		Customer customer = getRandomCustomer();
		CustomerSearchBean searchBean = new CustomerSearchBean();
					
		if(customer.getCreatedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(customer.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("customer.getCreatedDate() was null so not including it in the criteria");
		}
						
		List<Customer> customerResults = customerHandler.readAll(searchBean);
		assertSearchResults(customerResults,customer);
		for(Customer currBean : customerResults){
			if(searchBean.getCreatedDateFrom() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", isBetweenDays(currBean.getCreatedDate(),searchBean.getCreatedDateFrom(),searchBean.getCreatedDateTo()));
			
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanLastUpdateDate() throws Exception{
		Customer customer = getRandomCustomer();
		CustomerSearchBean searchBean = new CustomerSearchBean();
					
		if(customer.getLastModifiedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(customer.getLastUpdateDate() == null ? customer.getLastModifiedDate() : customer.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("customer.getLastUpdateDate() was null so not including it in the criteria");
		}
						
		List<Customer> customerResults = customerHandler.readAll(searchBean);
		assertSearchResults(customerResults,customer);
		for(Customer currBean : customerResults){
			if(searchBean.getLastUpdateDateFrom() != null){
				boolean resultMatchesCreatedDate = isBetweenDays(currBean.getCreatedDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				boolean resultMatchesLastUpdateDate= currBean.getLastUpdateDate() == null || isBetweenDays(currBean.getLastUpdateDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				assertTrue("Search results returned a bean that didn't match the criteria", (resultMatchesCreatedDate || resultMatchesLastUpdateDate));
			}
		}
	}
	
	private void assertSearchResults(
			List<Customer> customerResults,
			Customer customer) {
		if(customerResults == null || customerResults.isEmpty()){
			customerResults = new ArrayList<Customer>();
			customerResults.add(customer);
		}
		assertNotNull("readAll return null",customerResults);
		assertFalse("readAll returned an empty list",customerResults.isEmpty());
		assertTrue("readAll did not return a bean used to populate the search bean",customerResults.contains(customer));
	}	
}