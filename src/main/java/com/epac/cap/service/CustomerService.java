package com.epac.cap.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.handler.CustomerHandler;
import com.epac.cap.handler.OrderHandler;
import com.epac.cap.model.Customer;
import com.epac.cap.model.Order;
import com.epac.cap.repository.CustomerSearchBean;
import com.epac.cap.repository.OrderSearchBean;
import com.epac.cap.validator.InputResponseError;

@Controller
@Path("/customers")
public class CustomerService extends AbstractService{
	@Autowired
	private CustomerHandler customerHandler;
	
	@Autowired
	private OrderHandler orderHandler;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomers(){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Customer> customers = new ArrayList<Customer>();
		try {
			customers = customerHandler.readAll();
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of customer records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(customers).build();
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomer(@PathParam("id") Integer id){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Customer customer = null;
		try {
			customer = customerHandler.read(id);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the customer record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(customer).build();
	}
	
	@GET
	@Path("/email/{email}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomerByEmail(@PathParam("email") String email){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Customer customer = null;
		try {
			CustomerSearchBean csb = new CustomerSearchBean();
			csb.setEmailExact(email);
			List<Customer> customers = customerHandler.readAll(csb);
			if(!customers.isEmpty()){
				customer = customers.get(0);
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the customer record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(customer).build();
	}
	
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response add(Customer customer){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			//check duplication by email
			CustomerSearchBean csb = new CustomerSearchBean();
			csb.setEmailExact(customer.getEmail());
			List<Customer> foundResult = customerHandler.readAll(csb);
			if(!foundResult.isEmpty()){
				constraintViolationsMessages.put("errors", "A duplicate by the email is detected!");
			}
			
			if(constraintViolationsMessages.isEmpty()){
				doAddLogging(customer);
				customerHandler.create(customer);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while adding the customer record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;	
	}
	
	@Path("{id}")
	@DELETE
	@Transactional
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response delete(@PathParam("id") Integer id){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response res = null;
        try{
        	//check customer is not used
        	Customer customer = customerHandler.read(id);
        	if(customer == null){
        		return Response.status(404).entity("Customer with the id : " + id + " not present in the database").build();
        	}
        	List<Order> foundResult = new ArrayList<Order>();
        	OrderSearchBean osb = new OrderSearchBean();
        	osb.setCustomerId(id);
        	foundResult = orderHandler.readAll(osb);
        	if(foundResult.isEmpty()){
        		customerHandler.delete(customer);
        		res = Response.status(200).build();
        	}else{
        		constraintViolationsMessages.put("errors", "Cannot delete customer as it has orders associated to it!");
        		InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				res = inputResponseError.createResponse();
        	}
        }catch (PersistenceException e) {
        	constraintViolationsMessages.put("errors", "An error occurred while deleting the customer record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return res;
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response update(Customer customer){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			//check duplication by email
			CustomerSearchBean csb = new CustomerSearchBean();
			csb.setEmailExact(customer.getEmail());
			csb.setCustomerIdDiff(customer.getCustomerId());
			List<Customer> foundResult = customerHandler.readAll(csb);
			if(!foundResult.isEmpty()){
				constraintViolationsMessages.put("errors", "A duplicate by the email is detected!");
			}
			if(constraintViolationsMessages.isEmpty()){
				doEditLogging(customer);
				customerHandler.update(customer);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while updating the customer record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}

	/**
	 * @return the customerHandler
	 */
	public CustomerHandler getCustomerHandler() {
		return customerHandler;
	}

	/**
	 * @param customerHandler the customerHandler to set
	 */
	public void setCustomerHandler(CustomerHandler customerHandler) {
		this.customerHandler = customerHandler;
	}

	/**
	 * @return the orderHandler
	 */
	public OrderHandler getOrderHandler() {
		return orderHandler;
	}

	/**
	 * @param orderHandler the orderHandler to set
	 */
	public void setOrderHandler(OrderHandler orderHandler) {
		this.orderHandler = orderHandler;
	}
	
}
