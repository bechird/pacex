package com.epac.cap.service;

import java.util.HashMap;
import java.util.List;
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
import com.epac.cap.handler.OrderHandler;
import com.epac.cap.handler.PackageBookHandler;
import com.epac.cap.model.Order;
import com.epac.cap.model.PackageBook;
import com.epac.cap.model.PackageBook.statusPcb;
import com.epac.cap.validator.InputResponseError;

@Controller
@Path("/pcbs")
public class PackageBookService extends AbstractService {
	
	@Autowired
	PackageBookHandler packageBookHandler;
	@Autowired
	OrderHandler orderHandler;
	
	
	@POST
	@Path("/update/{qty}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updatePackageBook(@PathParam("qty") int qty, PackageBook pBook){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			int deli = pBook.getDelivered();
			pBook.setDelivered(deli+qty);
			packageBookHandler.update(pBook);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of job records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok().build();
	}
	
	@GET
	@Path("/updateStatus/{packageBookId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updatePackageBook(@PathParam("packageBookId") long packageBookId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			PackageBook pckBook = packageBookHandler.read(packageBookId);
			pckBook.setStatus(statusPcb.COMPLETE);
			packageBookHandler.update(pckBook);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of job records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
			
		}
		return Response.ok().build();
	}
	
	@POST
	@Path("/add")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addPackageBook(PackageBook packageBook){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			packageBookHandler.create(packageBook);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of job records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok().build();
	}
	
	@GET
	@Path("/order/{pcbId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetcOrderByPcb(@PathParam("pcbId") long pcbId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Order order = null;
		try {
			Integer orderId = packageBookHandler.fetchOrder(pcbId);
			 order = orderHandler.read(orderId);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of job records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(order).build();
	}
	
}
