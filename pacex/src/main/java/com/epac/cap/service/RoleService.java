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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.handler.RoleHandler;
import com.epac.cap.model.Role;
import com.epac.cap.repository.RoleSearchBean;
import com.epac.cap.validator.InputResponseError;

@Controller
@Path("/roles")
public class RoleService extends AbstractService{
	
	@Autowired
	private RoleHandler roleHandler;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRoles(){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Role> roles = new ArrayList<Role>();
		try {
			roles = roleHandler.readAll();
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of user records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
			//return ImpositionResponse.serverError().build();
		}
		return Response.ok(roles).build();
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRole(@PathParam("id") String id){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Role role = null;
		try {
			role = roleHandler.read(id);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the role record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(role).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_ADMIN"})
	public Response add(Role role){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			//check duplication by id or name
			RoleSearchBean rsb = new RoleSearchBean();
			rsb.setRoleId(role.getRoleId());
			List<Role> foundResult = roleHandler.readAll(rsb);
			if(!foundResult.isEmpty()){
				constraintViolationsMessages.put("errors", "A duplicate by the id is detected!");
			}
			rsb.setRoleId(null);
			rsb.setRoleNameExact(role.getRoleName());
			foundResult.clear();
			foundResult = roleHandler.readAll(rsb);
			if(!foundResult.isEmpty()){
				constraintViolationsMessages.put("errors", "A duplicate by the name is detected!");
			}
			if(constraintViolationsMessages.isEmpty()){
				doAddLogging(role);
				roleHandler.create(role);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while adding the role record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;	
	}
	
	@Path("{id}")
	@DELETE
	@Transactional
	@Secured({ "ROLE_ADMIN"})
	public Response delete(@PathParam("id") String id){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response res = null;
        try{
        	//check role is not used
        	Role role = roleHandler.read(id);
        	if(role == null){
        		return Response.status(404).entity("Role with the id : " + id + " not present in the database").build();
        	}
        	if(role.getUserRoles().isEmpty()){
        		roleHandler.delete(role);
        		res = Response.status(200).build();
        	}else{
        		constraintViolationsMessages.put("errors", "Cannot delete role as it is being used for some user records!");
        		InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				res = inputResponseError.createResponse();
        	}
        }catch (PersistenceException e) {
        	constraintViolationsMessages.put("errors", "An error occurred while deleting the role record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return res;
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_ADMIN"})
	public Response update(Role role){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			//check duplication by name
			RoleSearchBean rsb = new RoleSearchBean();
			rsb.setRoleNameExact(role.getRoleName());
			rsb.setRoleIdDiff(role.getRoleId());
			List<Role> foundResult = roleHandler.readAll(rsb);
			if(!foundResult.isEmpty()){
				constraintViolationsMessages.put("errors", "A duplicate by the name is detected!");
			}
			if(constraintViolationsMessages.isEmpty()){
				doEditLogging(role);
				roleHandler.update(role);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while updating the role record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}

	/**
	 * @return the roleHandler
	 */
	public RoleHandler getRoleHandler() {
		return roleHandler;
	}

	/**
	 * @param roleHandler the roleHandler to set
	 */
	public void setRoleHandler(RoleHandler roleHandler) {
		this.roleHandler = roleHandler;
	}
	
}
