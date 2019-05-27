package com.epac.cap.service;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.handler.PackageHandler;
import com.epac.cap.model.Package;
import com.epac.cap.validator.InputResponseError;

@Controller
@Path("/packages")
public class PackageService {
	@Autowired
	PackageHandler packageHandler;
	
	@POST
	@Path("/add")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addPackageBook(Package package_){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			packageHandler.create(package_);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of job records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok().build();
	}
	@GET
	@Path("/packageByPcb/{pcbId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchPackageByPcbId(@PathParam("pcbId") Long pcbId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Package package_ = null;
		try {
			 package_ = packageHandler.fetchPackageByPcbId(pcbId);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of job records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(package_).build();
	}

}
