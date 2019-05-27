package com.epac.cap.service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.epac.cap.handler.ProductionReportsHandler;
import com.epac.cap.handler.ProductionReportsHandler.ProdReportLine;
import com.epac.cap.validator.InputResponseError;

@Controller
@Path("/reports")
public class ProductionReportsService {
	
	@Autowired
	private ProductionReportsHandler productionReportsHandler;
	
	
	private static Logger log = Logger.getLogger(ProductionReportsService.class);
	private Date startDateTime, endDateTime;
		
	@POST
	@Path("/day")
	@Produces(MediaType.APPLICATION_JSON)
	public Response reportByDay(Map<String, String> json){
		
				
		String dayPart = json.get("dayPart");
		String dayString = json.get("day");
		
		
		

		Date date = null;
	    try {
			date = new Date( Long.parseLong(dayString)	);
			log.debug(date);
		} catch (Exception e) { e.printStackTrace(); }
		
	    if(date == null) {
	    		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
			constraintViolationsMessages.put("errors", "An error occurred while converting date!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
	    }
		
	    if(dayPart.equals("all")) {
	    		startDateTime  = setTime(date, 0, 0, 0);
	    		endDateTime  = setTime(date, 23, 59, 59);
	    }

	    if(dayPart.equals("morning")) {
    			startDateTime  = setTime(date, 6, 0, 0);
    			endDateTime  = setTime(date, 11, 59, 59);
	    }	    

	    if(dayPart.equals("afternoon")) {
			startDateTime  = setTime(date, 12, 0, 0);
			endDateTime  = setTime(date, 17, 59, 59);
	    }

	    if(dayPart.equals("night")) {
			startDateTime  = setTime(date, 18, 0, 0);
			endDateTime  = setTime(date, 5, 59, 59);
	    }
	    
	    log.debug("startDateTime:" + startDateTime);
	    log.debug("endDateTime:" + endDateTime);
	    
	    
	    List<ProdReportLine> report  = productionReportsHandler.makeReport(startDateTime, endDateTime);
	    
	    return Response.ok(report).build();
	}
	
	
	
	@POST
	@Path("/range")
	@Produces(MediaType.APPLICATION_JSON)
	public Response reportbyRange(Map<String, String> json){
		
		String startDateString = json.get("startDate");
		String endDateString = json.get("endDate");
		
		startDateTime = null;
		endDateTime = null;
		
	    try {
	    		startDateTime = new Date( Long.parseLong(startDateString) );
			log.debug(startDateTime);
			
			endDateTime = new Date( Long.parseLong(endDateString) );
			log.debug(endDateTime);
		} catch (Exception e) { e.printStackTrace(); }
		
	    
	    if(startDateTime == null || endDateTime == null) {
	    		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
			constraintViolationsMessages.put("errors", "An error occurred while converting date!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
	    }
		
	    log.debug("startDateTime:" + startDateTime);
	    log.debug("endDateTime:" + endDateTime);
	    
	    List<ProdReportLine> report  = productionReportsHandler.makeReport(startDateTime, endDateTime);
	    
	    return Response.ok(report).build();
	}
	
	
	private Date setTime(Date date, int hour, int minute, int second) {    
        Calendar cal = Calendar.getInstance();  
        cal.setTime(date);  
        cal.set(Calendar.HOUR_OF_DAY, hour);  
        cal.set(Calendar.MINUTE, minute);  
        cal.set(Calendar.SECOND, second);  
        cal.set(Calendar.MILLISECOND, 0);  
        return cal.getTime(); 
    }
	

}
