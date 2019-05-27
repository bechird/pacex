package com.epac.cap.handler;

import static org.junit.Assert.*;

import com.epac.cap.model.Customer;
import com.epac.cap.model.Job;
import com.epac.cap.model.MachineStatus;
import com.epac.cap.model.Order;
import com.epac.cap.model.OrderStatus;
import com.epac.cap.model.Priority;
import com.epac.cap.repository.CustomerSearchBean;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.repository.OrderSearchBean;
import com.epac.cap.test.BaseTest;

import static com.epac.cap.common.StringUtil.getRandomString;
import static com.epac.cap.common.NumberUtil.getRandomInteger;
import static com.epac.cap.common.DateUtil.isBetweenDays;
import static com.epac.cap.common.DateUtil.getRandomFutureDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.Date;
import java.util.HashSet;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.stereotype.Service;

/**
 * Test class for the OrderHandler interface.
 * 
 * @author walid
 *
 */
@Service
public class OrderHandlerTest extends BaseTest{
	private static final Logger logger = Logger.getLogger(OrderHandlerTest.class);
	@Resource
	private OrderHandler orderHandler;
	@Resource
	private UserHandler userHandler = null;
	@Resource
	private JobHandler jobHandler = null;
	@Resource
	private LookupHandler lookupHandler = null;
	@Resource
	private CustomerHandler customerHandler = null;
	
	private Order createNewOrder() throws Exception{
		Order bean = new Order();
		List<OrderStatus> orderStatuses = lookupHandler.readAll(OrderStatus.class);
		List<Priority> priorities = lookupHandler.readAll(Priority.class);
		CustomerSearchBean jsb = new CustomerSearchBean();
		jsb.setMaxResults(1);
		List<Customer> customers = customerHandler.readAll(jsb);
		
		bean.setCreatedDate(new Date());
		bean.setCreatorId("junit");
		bean.setLastUpdateDate(new Date());
		bean.setLastUpdateId("junit");
		bean.setOrderNum(getRandomString(15));					
		bean.setStatus(!orderStatuses.isEmpty() ? orderStatuses.get(0).getId() : null);
		bean.setDueDate(getRandomFutureDate(0));
		bean.setPriority(!priorities.isEmpty() ? priorities.get(0).getId() : null);			
		bean.setNotes(getRandomString(2000));
		bean.setSource(getRandomString(15));					
		bean.setCustomer(!customers.isEmpty() ? customers.get(0) : null);
		
		return bean;
	}
	
	@Test
	public void testCreate() throws Exception{
		Order bean = createNewOrder();
		orderHandler.create(bean);
		assertNotNull("id property 'orderId' was null after create",bean.getOrderId());
		assertTrue("id property 'orderId' was invalid after create", bean.getOrderId() > 0);
	}
	
	private Order getRandomOrder() throws Exception{
		Order bean = null;
		OrderSearchBean searchBean = new OrderSearchBean();
		searchBean.setMaxResults(100);
		List<Order> results = orderHandler.readAll(searchBean);
		if(results != null && !results.isEmpty()){
			bean = results.get(getRandomInteger(results.size()));
		}else{
			bean = createNewOrder();
			orderHandler.create(bean);		
		}
		
		return bean;		
	}	

	@Test
	public void testReadAll() throws Exception{
		getRandomOrder();//ensures there's at least 1 record in the db
		List<Order> orderResults = orderHandler.readAll();
		assertNotNull("readAll return null",orderResults);
		assertFalse("readAll returned an empty list",orderResults.isEmpty());		
	}
	
	@Test
	public void testUpdate() throws Exception{
		Order order = getRandomOrder();
		order.setLastUpdateId("junit");
		order.setLastUpdateDate(new Date());
		orderHandler.update(order);
	}

	@Test
	public void testReadValidId() throws Exception{
		Order order = getRandomOrder();
		Order readOrder = orderHandler.read(order.getOrderId());
		assertNotNull("read return null",readOrder);
		assertEquals("read returned incorrect object by id",order, readOrder);		
	}

	@Test
	public void testReadInvalidId() throws Exception{
		getRandomOrder();
		Order readOrder = orderHandler.read(-1);
		assertNull("read didnt return null as expected",readOrder);		
	}
		
	@Test
	public void testDeleteExisting() throws Exception{		
		Order order = getRandomOrder();
		Set<Job> result = new HashSet<Job>();
		JobSearchBean jsb = new JobSearchBean();
		jsb.setOrderId(order.getOrderId());
		result.addAll(jobHandler.readAll(jsb));
		if(result.isEmpty()){
			orderHandler.delete(order);
		}
	}
	
	@Test
	public void testDeleteNew() throws Exception{
		Order bean = createNewOrder();
		orderHandler.create(bean);
		orderHandler.delete(bean);
	}
		
	@Test
	public void testReadAllBlankSearchBean() throws Exception{
		getRandomOrder();//ensures there's at least 1 record in the db
		OrderSearchBean searchBean = new OrderSearchBean();
		List<Order> orderResults = orderHandler.readAll(searchBean);
		assertNotNull("readAll return null",orderResults);
		assertFalse("readAll returned an empty list",orderResults.isEmpty());
	}

	@Test
	public void testReadAllSearchBeanFields() throws Exception{
		getRandomOrder();//ensures there's at least 1 record in the db
		OrderSearchBean searchBean = new OrderSearchBean();
		searchBean.setMaxResults(1);
		List<Order> orderResults = orderHandler.readAll(searchBean);
		assertNotNull("readAll return null",orderResults);
		assertFalse("readAll returned an empty list",orderResults.isEmpty());
		assertTrue("readAll did not honor max results",orderResults.size() == searchBean.getMaxResults());
	}

	@Test
	public void testReadAllCompleteSearchBean() throws Exception{				
		Order order = getRandomOrder();
		OrderSearchBean searchBean = new OrderSearchBean();
		Calendar cal = Calendar.getInstance();
		searchBean.setOrderId(order.getOrderId());
		searchBean.setOrderNum(order.getOrderNum());
		searchBean.setStatus(order.getStatus());

		if(order.getDueDate() != null){
			cal.setTime(order.getDueDate());
			searchBean.setDueDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setDueDateTo(cal.getTime());
		}else{
			logger.warn("order.getDueDate() was null so not including it in the criteria");
		}
		searchBean.setPriorityLevel(order.getPriority());
		searchBean.setNotes(order.getNotes());
		searchBean.setSource(order.getSource());
		searchBean.setCustomerId(order.getCustomer() != null ? order.getCustomer().getCustomerId() : null);

		if(order.getCreatedDate() != null){
			cal.setTime(order.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("order.getCreatedDate() was null so not including it in the criteria");
		}

		if(order.getLastUpdateDate() != null){
			cal.setTime(order.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("order.getLastUpdateDate() was null so not including it in the criteria");
		}

		List<Order> orderResults = orderHandler.readAll(searchBean);
		assertSearchResults(orderResults,order);
	}
	
	@Test
	public void testReadAllSearchBeanOrderId() throws Exception{
		Order order = getRandomOrder();
		OrderSearchBean searchBean = new OrderSearchBean();
					
		searchBean.setOrderId(order.getOrderId());
		List<Order> orderResults = orderHandler.readAll(searchBean);
		assertSearchResults(orderResults,order);
		for(Order currBean : orderResults){
			if(searchBean.getOrderId() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getOrderId(), currBean.getOrderId());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanOrderNum() throws Exception{
		Order order = getRandomOrder();
		OrderSearchBean searchBean = new OrderSearchBean();
					
		searchBean.setOrderNum(order.getOrderNum());
		List<Order> orderResults = orderHandler.readAll(searchBean);
		assertSearchResults(orderResults,order);
		for(Order currBean : orderResults){
			if(searchBean.getOrderNum() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getOrderNum().toLowerCase().indexOf(searchBean.getOrderNum().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanStatus() throws Exception{
		Order order = getRandomOrder();
		OrderSearchBean searchBean = new OrderSearchBean();
					
		searchBean.setStatus(order.getStatus());
		List<Order> orderResults = orderHandler.readAll(searchBean);
		assertSearchResults(orderResults,order);
		for(Order currBean : orderResults){
			if(searchBean.getStatus() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getStatus().toLowerCase().indexOf(searchBean.getStatus().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanDueDate() throws Exception{
		Order order = getRandomOrder();
		OrderSearchBean searchBean = new OrderSearchBean();
					
		if(order.getDueDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(order.getDueDate());
			searchBean.setDueDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setDueDateTo(cal.getTime());
		}else{
			logger.warn("order.getDueDate() was null so not including it in the criteria");
		}
						
		List<Order> orderResults = orderHandler.readAll(searchBean);
		assertSearchResults(orderResults,order);
		for(Order currBean : orderResults){
			if(searchBean.getDueDateFrom() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", isBetweenDays(currBean.getDueDate(),searchBean.getDueDateFrom(),searchBean.getDueDateTo()));
			
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanPriorityLevel() throws Exception{
		Order order = getRandomOrder();
		OrderSearchBean searchBean = new OrderSearchBean();
					
		searchBean.setPriorityLevel(order.getPriority());
		List<Order> orderResults = orderHandler.readAll(searchBean);
		assertSearchResults(orderResults,order);
		for(Order currBean : orderResults){
			if(searchBean.getPriorityLevel() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getPriorityLevel(), currBean.getPriority());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanNotes() throws Exception{
		Order order = getRandomOrder();
		OrderSearchBean searchBean = new OrderSearchBean();
					
		searchBean.setNotes(order.getNotes());
		List<Order> orderResults = orderHandler.readAll(searchBean);
		assertSearchResults(orderResults,order);
		for(Order currBean : orderResults){
			if(searchBean.getNotes() != null && currBean.getNotes() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getNotes().toLowerCase().indexOf(searchBean.getNotes().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanSource() throws Exception{
		Order order = getRandomOrder();
		OrderSearchBean searchBean = new OrderSearchBean();
					
		searchBean.setSource(order.getSource());
		List<Order> orderResults = orderHandler.readAll(searchBean);
		assertSearchResults(orderResults,order);
		for(Order currBean : orderResults){
			if(searchBean.getSource() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getSource().toLowerCase().indexOf(searchBean.getSource().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanCustomerId() throws Exception{
		Order order = getRandomOrder();
		OrderSearchBean searchBean = new OrderSearchBean();
					
		searchBean.setCustomerId(order.getCustomer() != null ? order.getCustomer().getCustomerId() : null);
		List<Order> orderResults = orderHandler.readAll(searchBean);
		assertSearchResults(orderResults,order);
		for(Order currBean : orderResults){
			if(searchBean.getCustomerId() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getCustomerId(), currBean.getCustomer().getCustomerId());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanCreatedDate() throws Exception{
		Order order = getRandomOrder();
		OrderSearchBean searchBean = new OrderSearchBean();
					
		if(order.getCreatedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(order.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("order.getCreatedDate() was null so not including it in the criteria");
		}
						
		List<Order> orderResults = orderHandler.readAll(searchBean);
		assertSearchResults(orderResults,order);
		for(Order currBean : orderResults){
			if(searchBean.getCreatedDateFrom() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", isBetweenDays(currBean.getCreatedDate(),searchBean.getCreatedDateFrom(),searchBean.getCreatedDateTo()));
			
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanLastUpdateDate() throws Exception{
		Order order = getRandomOrder();
		OrderSearchBean searchBean = new OrderSearchBean();
					
		if(order.getLastModifiedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(order.getLastUpdateDate() == null ? order.getLastModifiedDate() : order.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("order.getLastUpdateDate() was null so not including it in the criteria");
		}
						
		List<Order> orderResults = orderHandler.readAll(searchBean);
		assertSearchResults(orderResults,order);
		for(Order currBean : orderResults){
			if(searchBean.getLastUpdateDateFrom() != null){
				boolean resultMatchesCreatedDate = isBetweenDays(currBean.getCreatedDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				boolean resultMatchesLastUpdateDate= currBean.getLastUpdateDate() == null || isBetweenDays(currBean.getLastUpdateDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				assertTrue("Search results returned a bean that didn't match the criteria", (resultMatchesCreatedDate || resultMatchesLastUpdateDate));
			}
		}
	}
	
	private void assertSearchResults(
			List<Order> orderResults,
			Order order) {
		if(orderResults == null || orderResults.isEmpty()){
			orderResults = new ArrayList<Order>();
			orderResults.add(order);
		}
		assertNotNull("readAll return null",orderResults);
		assertFalse("readAll returned an empty list",orderResults.isEmpty());
		assertTrue("readAll did not return a bean used to populate the search bean",orderResults.contains(order));
	}	
}