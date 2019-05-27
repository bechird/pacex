package com.epac.cap.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.handler.UserHandler;
import com.epac.cap.model.User;
import com.epac.cap.repository.UserSearchBean;
import com.epac.cap.security.UserRepositoryUserDetails;
import com.epac.cap.validator.InputResponseError;

@Controller
@Path("/users")
public class UserService extends AbstractService{
	
	@Autowired
	private UserHandler userHandler;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	//@PreAuthorize("hasRole('ROLE_ADMIN')")  TODO work on authorization, not working, and calling the rest url is possible even not logged in at all!!!
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response getUsers(){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<User> users = new ArrayList<User>();
		try {
			users = userHandler.readAll();
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of user records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
			//return ImpositionResponse.serverError().build();
		}
		return Response.ok(users).build();
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(@PathParam("id") String id){
		User user = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			user = userHandler.read(id);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of user records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(user).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_ADMIN"})
	public Response add(User user){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			//check for duplication by email or login name; cannot check by user id (and it is redundant as checking the email is the same)
			//because the user id has not been generated yet
			UserSearchBean usb = new UserSearchBean();
			usb.setEmailExact(user.getEmail());
			List<User> foundResult = userHandler.readAll(usb);
			if(!foundResult.isEmpty()){
				constraintViolationsMessages.put("errors", "A duplicate by the email is detected!");
			}
			usb.setEmailExact(null);
			usb.setLoginNameExact(user.getLoginName());
			foundResult.clear();
			foundResult = userHandler.readAll(usb);
			if(!foundResult.isEmpty()){
				constraintViolationsMessages.put("errors", "A duplicate by the login name is detected!");
			}
			if(constraintViolationsMessages.isEmpty()){
				doAddLogging(user);
				userHandler.create(user);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while adding the user record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;	
	}
	
	@Path("{id}")
	@DELETE
	@Transactional
	@Secured({"ROLE_ADMIN"})
	public Response delete(@PathParam("id") String id){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response res = null;
        try{
        	User user = userHandler.read(id);
        	if(user != null){
        		userHandler.delete(user);
            	res = Response.status(200).build();
        	}else{
        		return Response.status(404).entity("User with the id : " + id + " not present in the database").build();
        	}
        } catch (PersistenceException e) {
        	constraintViolationsMessages.put("errors", "An error occurred while deleting the user record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return res;
	}
	
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response update(User user){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			//check duplication by email and login name
			UserSearchBean usb = new UserSearchBean();
			usb.setEmailExact(user.getEmail());
			usb.setUserIdDiff(user.getUserId());
			List<User> foundResult = userHandler.readAll(usb);
			if(!foundResult.isEmpty()){
				constraintViolationsMessages.put("errors", "A duplicate by the email is detected!");
			}
			usb.setEmailExact(null);
			usb.setLoginNameExact(user.getLoginName());
			foundResult.clear();
			foundResult = userHandler.readAll(usb);
			if(!foundResult.isEmpty()){
				constraintViolationsMessages.put("errors", "A duplicate by the login name is detected!");
			}
			
			if(constraintViolationsMessages.isEmpty()){
				doEditLogging(user);
				userHandler.update(user);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while updating the user record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}

	/**
	 * @return the userHandler
	 */
	public UserHandler getUserHandler() {
		return userHandler;
	}

	/**
	 * @param userHandler the userHandler to set
	 */
	public void setUserHandler(UserHandler userHandler) {
		this.userHandler = userHandler;
	}
	
}
