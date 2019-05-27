package com.epac.cap.validator;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

public class InputResponseError {
	
	private static final String ERROR = "error";
	private static final String ERRORS = "errors";
	
	private final Map<String, String> constraintViolationsMessages;
	
	public InputResponseError(Map<String, String> constraintViolationsMessages){
		this.constraintViolationsMessages = constraintViolationsMessages;
	}
	
	public Response createResponse(){
		Map<String,Object> result = new HashMap<String,Object>();
		result.put(ERROR, true);
		result.put(ERRORS, constraintViolationsMessages);
		return Response.status(412).entity(result).build();
	}
}
