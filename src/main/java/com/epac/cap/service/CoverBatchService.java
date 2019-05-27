package com.epac.cap.service;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.epac.cap.common.EpacException;
import com.epac.cap.common.PersistenceException;
import com.epac.cap.handler.CoverBatchHandler;
import com.epac.cap.model.CoverBatch;
import com.epac.cap.model.Station;
import com.epac.cap.model.StationCategory;
import com.epac.cap.repository.CoverBatchDAO;
import com.epac.cap.validator.InputResponseError;

@Controller
@Path("/batches")
public class CoverBatchService extends AbstractService{
	
	@Autowired
	CoverBatchHandler coverBatchHandler;
	
	@Autowired
	CoverBatchDAO coverBatchDAO;
	
	@GET
	@Path("/id/{batchId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBatchById(@PathParam("batchId") Integer batchId){
		CoverBatch batch =null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			batch = coverBatchDAO.read(batchId);
		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the section by Id!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(batch).build();
	}
	
	@GET
	@Path("/recreate/{batchId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response reCreateBatch(@PathParam("batchId") Integer batchId){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			String executingUserId = this.getExecutingUserId();
				boolean success = coverBatchHandler.reCreateBatch(batchId, executingUserId);
				response = Response.ok(success).build();
		}
		catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the section by Id!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return response;
	}
	
}
