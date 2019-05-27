package com.epac.cap.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
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

import com.epac.cap.model.DefaultStation;
import com.epac.cap.model.Job;
import com.epac.cap.model.Log;
import com.epac.cap.model.LookupItem;
import com.epac.cap.model.Machine;
import com.epac.cap.model.Order;
import com.epac.cap.model.PNLTemplateLine;
import com.epac.cap.model.PaperType;
import com.epac.cap.model.PaperTypeMedia;
import com.epac.cap.model.Preference;
import com.epac.cap.model.Part;
import com.epac.cap.model.PartCategory;
import com.epac.cap.model.Roll;
import com.epac.cap.model.Station;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.repository.LogSearchBean;
import com.epac.cap.repository.LookupSearchBean;
import com.epac.cap.repository.MachineSearchBean;
import com.epac.cap.repository.OrderSearchBean;
import com.epac.cap.repository.PartSearchBean;
import com.epac.cap.repository.RollSearchBean;
import com.epac.cap.repository.StationSearchBean;
import com.epac.cap.common.PersistenceException;
import com.epac.cap.functionel.Imposition;
import com.epac.cap.functionel.PNLInfo;
import com.epac.cap.functionel.WorkflowEngine;
import com.epac.cap.handler.DefaultStationHandler;
import com.epac.cap.handler.JobHandler;
import com.epac.cap.handler.LogHandler;
import com.epac.cap.handler.LookupHandler;
import com.epac.cap.handler.MachineHandler;
import com.epac.cap.handler.OrderHandler;
import com.epac.cap.handler.PartHandler;
import com.epac.cap.handler.PreferenceByIdComparator;
import com.epac.cap.handler.RollHandler;
import com.epac.cap.handler.StationHandler;
import com.epac.cap.validator.InputResponseError;

@Controller
@Path("/lookups")
public class LookupService<L extends LookupItem> extends AbstractService{
	
	@Autowired
	private LookupHandler lookupHandler;
	@Autowired
	private PartHandler partHandler;
	@Autowired
	private StationHandler stationHandler;
	@Autowired
	private DefaultStationHandler defaultStationHandler;
	@Autowired
	private JobHandler jobHandler;
	@Autowired
	private LogHandler logHandler;
	@Autowired
	private MachineHandler machineHandler;
	@Autowired
	private OrderHandler orderHandler;
	@Autowired
	private RollHandler rollHandler;
	@Autowired
	private WorkflowEngine workflowEngine;
	@Autowired
	private Imposition imposition;
	
	@GET
	@Path("/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getItems(@PathParam("type") String type) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<L> lookups =  new ArrayList<L>();
		try{
			lookups = lookupHandler.readAll((Class<L>) Class.forName("com.epac.cap.model."+type));
			if("Preference".equals(type)){
				Collections.sort(lookups, new PreferenceByIdComparator());
			}
		}catch (ClassNotFoundException c){
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of records: Class Not Found!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of lookups records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
			//return ImpositionResponse.serverError().build();
		}
		return Response.ok(lookups).build();
	}
	
	@GET
	@Path("/prefGroups/{clientId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPrefGroups( @PathParam("clientId") String clientId) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<String> result = null;
		try{
			Set<String> prefGroups;
			if(clientId == null || clientId.isEmpty() || "undefined".equals(clientId)){
				prefGroups = new HashSet<String>(lookupHandler.readPrefGroups(null));
			}else{
				prefGroups = new HashSet<String>(lookupHandler.readPrefGroups(clientId));
			}
			if(!prefGroups.isEmpty()){
				result = new ArrayList<String>(prefGroups);
				Collections.sort(result);
			}
		}catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of lookups records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
			//return ImpositionResponse.serverError().build();
		}
		return Response.ok(result).build();
	}
	
	@GET
	@Path("/prefSubjects/list/{groupId}/{clientId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPrefSubjects(@PathParam("groupId") String groupId, @PathParam("clientId") String clientId) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<String> result = null;
		try{
			Set<String> prefSubjects;
			if(clientId == null || clientId.isEmpty() || "undefined".equals(clientId)){
				prefSubjects = new HashSet<String>(lookupHandler.readPrefSubjects(groupId, null));
			}else{
				prefSubjects = new HashSet<String>(lookupHandler.readPrefSubjects(groupId, clientId));
			}
			if(!prefSubjects.isEmpty()){
				result = new ArrayList<String>(prefSubjects);
				Collections.sort(result);
			}
		}catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of lookups records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
			//return ImpositionResponse.serverError().build();
		}
		return Response.ok(result).build();
	}
	
	@GET
	@Path("/prefSubjects/items/{prefSubject}/{clientId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPrefsBySubject(@PathParam("prefSubject") String prefSubject, @PathParam("clientId") String clientId) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Preference> result =  new ArrayList<Preference>();
		try{
			List<Preference> lookups = lookupHandler.readAll(Preference.class);
			for(Preference p : lookups){
				if(prefSubject.equals(p.getPrefSubject())){
					if(!"Naming_Convention".equals(prefSubject)){// exception: naming conventions should show for anyone
						if(clientId == null || clientId.isEmpty() || "undefined".equals(clientId)){
							if(p.getClientId() == null || p.getClientId().isEmpty() || "undefined".equals(p.getClientId())){
								result.add(p);
							}
						}else if(clientId.equals(p.getClientId())){
							result.add(p);
						}
					}else{
						result.add(p);
					}
				}
			}
			Collections.sort(result, new PreferenceByIdComparator());
		}catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of lookups records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
			//return ImpositionResponse.serverError().build();
		}
		return Response.ok(result).build();
	}
	
	@GET
	@Path("/{type}/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getItem(@PathParam("type") String type, @PathParam("id") String id) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		LookupItem lookupItem = null;
		try {
			lookupItem = lookupHandler.read(id, (Class<LookupItem>) Class.forName("com.epac.cap.model."+type));
		}catch (ClassNotFoundException c){
			constraintViolationsMessages.put("errors", "An error occurred while reading the record: Class Not Found!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the lookup record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
			//return ImpositionResponse.serverError().build();
		}
		return Response.ok(lookupItem).build();
	}
	
	@POST
	@Path("/{type}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	//@Produces(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response add(@FormParam("id") String id, @FormParam("name") String name, @FormParam("paperTypeId") String paperTypeId, 
			@FormParam("rollWidth") Float rollWidth, @FormParam("rollLength") Integer rollLength, 
			@FormParam("shortName") String shortName, @FormParam("thickness") Float thickness, @FormParam("weight") Float weight, @FormParam("dropFolder") String dropFolder, 
			@FormParam("groupingValue") String groupingValue, @FormParam("description") String description, @FormParam("prefSubject") String prefSubject, 
			@FormParam("partNum") String partNum,  @FormParam("clientId") String clientId, @PathParam("type") String type) {
		
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			// If PNL preference, see if the user is entering the same preference more than once
			if("Preference".equals(type) && "PNL".equals(prefSubject)){
				LookupSearchBean lsbean =  new LookupSearchBean();
				lsbean.setIdPrefix(id);
				lsbean.setPrefSubject("PNL");
				lsbean.setPartNum(partNum != null && !partNum.isEmpty() ? partNum : null);
				if(partNum == null || partNum.isEmpty()){
					lsbean.setClientId(clientId != null && !clientId.isEmpty() && !"undefined".equals(clientId) ? clientId : null);
				}
				List<Preference> foundPrefResult = lookupHandler.readAll(null, lsbean, Preference.class);
				if(!foundPrefResult.isEmpty()){
					if((partNum == null || partNum.isEmpty()) && (clientId == null || clientId.isEmpty() || "undefined".equals(clientId))){
						for(Preference p : foundPrefResult){
							if((p.getPartNum() == null || p.getPartNum().isEmpty()) && (p.getClientId() == null || p.getClientId().isEmpty() || "undefined".equals(p.getClientId()))){
								constraintViolationsMessages.put("errors", "A PNL Preference duplication is detected!");
								break;
							}
						}
					}else{
						constraintViolationsMessages.put("errors", "A PNL Preference duplication is detected!");
					}
				}
			}
			//check for duplication by id (if not PNL preference)
			LookupSearchBean lsb =  new LookupSearchBean();
			lsb.setId(id);
			List<L> foundResult = lookupHandler.readAll(lsb, (Class<L>) Class.forName("com.epac.cap.model."+type));
			if(!foundResult.isEmpty()){
				if("Preference".equals(type) && "PNL".equals(prefSubject)){
					lsb.setId(null);
					lsb.setIdPrefix(id);
					List<Preference> foundResults = lookupHandler.readAll(null, lsb, Preference.class);
					id = id.concat("_"+(foundResults.size() + 3));
				}else{
					constraintViolationsMessages.put("errors", "A duplicate by the id is detected!");
				}
			}
			
			if(constraintViolationsMessages.isEmpty()){
				Object item2 = Class.forName("com.epac.cap.model."+type).newInstance();
				((L)item2).setId(id);
				((L)item2).setName(name);
				if("PaperType".equals(type)){
					((PaperType)item2).setShortName(shortName);
					((PaperType)item2).setThickness(thickness);
					((PaperType)item2).setWeight(weight);
					((PaperType)item2).setDropFolder(dropFolder);
				}
				if("PaperTypeMedia".equals(type)){
					((PaperTypeMedia)item2).setPaperTypeId(paperTypeId);
					((PaperTypeMedia)item2).setRollLength(rollLength);
					((PaperTypeMedia)item2).setRollWidth(rollWidth);
				}
				if("Preference".equals(type)){
					((Preference)item2).setGroupingValue(groupingValue != null && groupingValue != "" ? groupingValue : lookupHandler.getGroupBySubject(prefSubject));
					((Preference)item2).setPrefSubject(prefSubject);
					((Preference)item2).setPartNum(partNum == null || partNum.isEmpty() ? null : partNum);
					((Preference)item2).setClientId(clientId);
				}
				((L)item2).setDescription(description); 
				((L)item2).setCreatorId(this.getExecutingUserId()); 
				((L)item2).setCreatedDate(new Date());
				lookupHandler.create( (LookupItem) item2);
				response = Response.ok(((LookupItem)item2).getId()).build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
			//ClassNotFoundException nf, InstantiationException ie, IllegalAccessException iae
		}catch (PersistenceException p){
			constraintViolationsMessages.put("errors", "An error occurred while adding the lookup record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while adding the lookup record: Class Not Found!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;	
	}
	
	@PUT
	@Path("/{type}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response update(@FormParam("id") String id, @FormParam("name") String name, @FormParam("description") String description,
			@FormParam("shortName") String shortName, @FormParam("thickness") Float thickness, @FormParam("weight") Float weight, @FormParam("dropFolder") String dropFolder, 
			@FormParam("groupingValue") String groupingValue, @FormParam("paperTypeId") String paperTypeId, @FormParam("rollWidth") Float rollWidth, @FormParam("rollLength") Integer rollLength,
			@FormParam("partNum") String partNum,  @FormParam("clientId") String clientId, @FormParam("prefSubject") String prefSubject, @PathParam("type") String type) {
		
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			/*check duplication by name
			LookupSearchBean lsb =  new LookupSearchBean();
			lsb.setNameExact(name);
			lsb.setIdDiff(id);
			List<L> foundResult = lookupHandler.readAll(lsb, (Class<L>) Class.forName("com.epac.cap.model."+type));
			if(!foundResult.isEmpty() && !"Preference".equals(type)){
				constraintViolationsMessages.put("errors", "A duplicate by the name is detected!");
			}*/
			if(constraintViolationsMessages.isEmpty()){
				LookupItem item2 = lookupHandler.read(id, (Class<LookupItem>) Class.forName("com.epac.cap.model."+type));
				item2.setName(name);
				if("PaperType".equals(type)){
					((PaperType)item2).setShortName(shortName);
					((PaperType)item2).setThickness(thickness);
					((PaperType)item2).setWeight(weight);
					((PaperType)item2).setDropFolder(dropFolder);
				}
				if("PaperTypeMedia".equals(type)){
					((PaperTypeMedia)item2).setPaperTypeId(paperTypeId);
					((PaperTypeMedia)item2).setRollLength(rollLength);
					((PaperTypeMedia)item2).setRollWidth(rollWidth);
				}
				if("Preference".equals(type)){
					//((Preference)item2).setGroupingValue(groupingValue);
					((Preference)item2).setPrefSubject(prefSubject);
					((Preference)item2).setPartNum(partNum == null || partNum.isEmpty() ? null : partNum);
					((Preference)item2).setClientId(clientId);
				}
				item2.setDescription(description);
				item2.setLastUpdateDate(new Date());
				item2.setLastUpdateId(this.getExecutingUserId());
				lookupHandler.update(item2);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
			//ClassNotFoundException nf, InstantiationException ie, IllegalAccessException iae
		}catch (PersistenceException p){
			constraintViolationsMessages.put("errors", "An error occurred while updating the lookup record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while updating the lookup record: Class Not Found!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
		
	}
	
	@Path("/{type}/{id}")
	@DELETE
	@Transactional
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response delete(@PathParam("type") String type, @PathParam("id") String id) {
		Response res = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
        try{
        	LookupItem lookupItem = lookupHandler.read(id, (Class<LookupItem>) Class.forName("com.epac.cap.model."+type));
        	if(lookupItem != null){
        		//see if ok to delete the item
        		boolean allowDelete = true;
        		if("BindingType".equals(type)){
        			List<Part> result = new ArrayList<Part>();
        			List<DefaultStation> result2 = new ArrayList<DefaultStation>();
        			PartSearchBean psb = new PartSearchBean();
        			psb.setBindingTypeId(id);
        			result = partHandler.readAll(psb);
        			if(!result.isEmpty()){
        				allowDelete = false;
        			}
        			StationSearchBean ssb = new StationSearchBean();
        			ssb.setBindingTypeId(id);
        			result2 = defaultStationHandler.readAll(ssb);
        			if(!result2.isEmpty()){
        				allowDelete = false;
        			}
        		}
        		if("StationCategory".equals(type)){
        			List<Station> result = new ArrayList<Station>();
        			List<DefaultStation> result2 = new ArrayList<DefaultStation>();
        			StationSearchBean ssb = new StationSearchBean();
        			ssb.setStationCategoryId(id);
        			result = stationHandler.readAll(ssb);
        			if(!result.isEmpty()){
        				allowDelete = false;
        			}
        			result2 = defaultStationHandler.readAll(ssb);
        			if(!result2.isEmpty()){
        				allowDelete = false;
        			}
        		}
        		if("Critiria".equals(type)){
        			List<Part> result = new ArrayList<Part>();
        			List<DefaultStation> result2 = new ArrayList<DefaultStation>();
        			PartSearchBean psb = new PartSearchBean();
        			psb.setCritiriaId(id);
        			result = partHandler.readAll(psb);
        			if(!result.isEmpty()){
        				allowDelete = false;
        			}
        			StationSearchBean ssb = new StationSearchBean();
        			ssb.setPartCritiriaId(id);
        			result2 = defaultStationHandler.readAll(ssb);
        			if(!result2.isEmpty()){
        				allowDelete = false;
        			}
        		}
        		if("JobStatus".equals(type) ){
        			List<Job> result = new ArrayList<Job>();
        			JobSearchBean jsb = new JobSearchBean();
        			jsb.setStatus(id);
        			result = jobHandler.readAll(jsb);
        			if(!result.isEmpty()){
        				allowDelete = false;
        			}
        		}
        		if("JobType".equals(type) ){
        			List<Job> result = new ArrayList<Job>();
        			JobSearchBean jsb = new JobSearchBean();
        			jsb.setJobType(id);
        			result = jobHandler.readAll(jsb);
        			if(!result.isEmpty()){
        				allowDelete = false;
        			}
        		}
        		if("Lamination".equals(type)){
        			List<Part> result = new ArrayList<Part>();
        			PartSearchBean psb = new PartSearchBean();
        			psb.setLamination(id);
        			result = partHandler.readAll(psb);
        			if(!result.isEmpty()){
        				allowDelete = false;
        			}
        		}
        		if("LogCause".equals(type) ){
        			List<Log> result = new ArrayList<Log>();
        			LogSearchBean lsb = new LogSearchBean();
        			lsb.setCause(id);
        			result = logHandler.readAll(lsb);
        			if(!result.isEmpty()){
        				allowDelete = false;
        			}
        		}
        		if("LogResult".equals(type)){
        			List<Log> result = new ArrayList<Log>();
        			LogSearchBean lsb = new LogSearchBean();
        			lsb.setResult(id);
        			result = logHandler.readAll(lsb);
        			if(!result.isEmpty()){
        				allowDelete = false;
        			}
        		}
        		if("MachineStatus".equals(type) ){
        			List<Machine> result = new ArrayList<Machine>();
        			MachineSearchBean msb = new MachineSearchBean();
        			msb.setStatus(id);
        			result = machineHandler.readAll(msb);
        			if(!result.isEmpty()){
        				allowDelete = false;
        			}
        		}
        		if("MachineType".equals(type) ){
        			List<Machine> result = new ArrayList<Machine>();
        			MachineSearchBean msb = new MachineSearchBean();
        			msb.setType(id);
        			result = machineHandler.readAll(msb);
        			if(!result.isEmpty()){
        				allowDelete = false;
        			}
        		}
        		if("OrderStatus".equals(type) ){
        			List<Order> result = new ArrayList<Order>();
        			OrderSearchBean osb = new OrderSearchBean();
        			osb.setStatus(id);
        			result = orderHandler.readAll(osb);
        			if(!result.isEmpty()){
        				allowDelete = false;
        			}
        		}
        		if("PaperType".equals(type) ){
        			List<Part> result = new ArrayList<Part>();
        			PartSearchBean psb = new PartSearchBean();
        			psb.setPaperType(id);
        			result = partHandler.readAll(psb);
        			if(!result.isEmpty()){
        				allowDelete = false;
        			}
        			if(!((PaperType)lookupItem).getMedias().isEmpty()){
        				allowDelete = false;
        			}
        		}
        		if("PartCategory".equals(type)){
        			List<Part> result = new ArrayList<Part>();
        			PartSearchBean psb = new PartSearchBean();
        			psb.setCategoryId(id);
        			result = partHandler.readAll(psb);
        			if(!result.isEmpty() || !((PartCategory)lookupItem).getDefaultStations().isEmpty()){
        				allowDelete = false;
        			}
        			StationSearchBean ssb = new StationSearchBean();
        			ssb.setPartCategoryId(id);
        			List<DefaultStation> result2 = new ArrayList<DefaultStation>();
        			result2 = defaultStationHandler.readAll(ssb);
        			if(!result2.isEmpty()){
        				allowDelete = false;
        			}
        		}
        		if("Priority".equals(type)){
        			List<Order> result = new ArrayList<Order>();
        			OrderSearchBean osb = new OrderSearchBean();
        			osb.setPriorityLevel(id);
        			result = orderHandler.readAll(osb);
        			if(!result.isEmpty()){
        				allowDelete = false;
        			}
        			List<Job> result2 = new ArrayList<Job>();
        			JobSearchBean jsb = new JobSearchBean();
        			jsb.setJobPriority(id);
        			result2 = jobHandler.readAll(jsb);
        			if(!result2.isEmpty()){
        				allowDelete = false;
        			}
        		}
        		if("RollStatus".equals(type)){
        			List<Roll> result = new ArrayList<Roll>();
        			RollSearchBean rsb = new RollSearchBean();
        			rsb.setStatus(id);
        			result = rollHandler.readAll(rsb);
        			if(!result.isEmpty()){
        				allowDelete = false;
        			}
        		}
        		if("RollType".equals(type) ){
        			List<Roll> result = new ArrayList<Roll>();
        			RollSearchBean rsb = new RollSearchBean();
        			rsb.setRollType(id);
        			result = rollHandler.readAll(rsb);
        			if(!result.isEmpty()){
        				allowDelete = false;
        			}
        		}
        		if("Client".equals(type) ){
        			List<Order> result = new ArrayList<Order>();
        			OrderSearchBean psb = new OrderSearchBean();
        			psb.setClientId(id);
        			result = orderHandler.readAll(psb);
        			if(!result.isEmpty()){
        				allowDelete = false;
        			}
        		}
        		
        		if(allowDelete){
        			lookupHandler.delete(id, (Class<L>) Class.forName("com.epac.cap.model."+type));
        			res = Response.status(200).build();
        		}else{
        			constraintViolationsMessages.put("errors", "Cannot delete the lookup item as it is being used!");
            		InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
    				res = inputResponseError.createResponse();
        		}
        	}else{
        		return Response.status(404).entity("Item with the id : " + id + " not present in the database").build();
        	}
        }catch (ClassNotFoundException c){
			constraintViolationsMessages.put("errors", "An error occurred while reading the record: Class Not Found!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}catch (PersistenceException e) {
        	constraintViolationsMessages.put("errors", "An error occurred while deleting the lookup record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return res;
	}
	
	@GET
	@Path("/templateConventions")
	@Produces(MediaType.APPLICATION_JSON)
	public Response templateConventions() {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Map<String, String> result = new HashMap<String, String>();
		try {
			result = workflowEngine.getPNLTemplateNamingConventions();
		}catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of template naming conventions records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
			//return ImpositionResponse.serverError().build();
		}
		return Response.ok(result).build();
	}
	
	@GET
	@Path("/{type}/lines/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTemplateLineById(@PathParam("type") String type, @PathParam("id") Integer id) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		PNLTemplateLine tmpLine = null;
		try {
			tmpLine = lookupHandler.readTmpLine(id);
		}catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the template line record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
			//return ImpositionResponse.serverError().build();
		}
		return Response.ok(tmpLine).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	@Path("/{type}/lines")
	public Response addTemplateLine(PNLTemplateLine tmpLine){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			if(constraintViolationsMessages.isEmpty()){
				lookupHandler.createTmpLine(tmpLine);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while adding the template line record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;	
	}
	
	@Path("/{type}/lines/{id}")
	@DELETE
	@Transactional
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response deleteTmpLine(@PathParam("type") String type, @PathParam("id") Integer id){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response res = null;
        try{
        	//Check if ok to delete the template line
        	PNLTemplateLine tmpLine = lookupHandler.readTmpLine(id);
        	if(tmpLine != null){
        		lookupHandler.deleteTmpLine(id);
	        	res = Response.status(200).build();
        	}else{
        		return Response.status(404).entity("Template Line with the id : " + id + " not present in the database").build();
	        }
        }catch (PersistenceException e) {
        	constraintViolationsMessages.put("errors", "An error occurred while deleting the template line record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return res;
	}
	
	@Path("/{type}/lines")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response updateTmpLine(PNLTemplateLine tmpLine){
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			if(constraintViolationsMessages.isEmpty()){
				lookupHandler.updateTmpLine(tmpLine);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while updating the template line record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}
	
	@POST
	@Path("/pnlTrial/{partNum}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response pnlTrial(@PathParam("partNum") String partNum, Float[] widthHeight) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			if(constraintViolationsMessages.isEmpty()){
				PNLInfo result = null;
				Part part = partHandler.read(partNum);
				if(part != null){
					if(widthHeight != null && widthHeight.length == 2 && widthHeight[0] != null && widthHeight[0] > 0 && widthHeight[1] != null && widthHeight[1] > 0){
						result = workflowEngine.resolvePNLInfo(part, widthHeight[0], widthHeight[1], false, null);
					}else{
						result = workflowEngine.resolvePNLInfo(part, null, null, false, null);
					}
					if(result == null){
						constraintViolationsMessages.put("errors", "An error occurred while trying the pnl info! some PNL data seems to be missing");
						InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
						response = inputResponseError.createResponse();
					}else{
						response = Response.ok(result.toString()).build();
					}
				}
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while trying the pnl info!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}	
	
	@GET
	@Path("/pnl/preview/resolve/{templateId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response resolvePNLForPreview(@PathParam("templateId") String templateId) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			if(constraintViolationsMessages.isEmpty()){
				response = Response.ok(workflowEngine.resolvePNLInfo(null, null, null, true, templateId)).build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while trying the pnl info!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}
	
	@POST
	@Path("/pnl/preview/generate/")
	//@Produces("application/pdf")
	@Secured({ "ROLE_PM", "ROLE_ADMIN"})
	public Response pnlPreviewGenerate(PNLInfo pnlInfo) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			if(constraintViolationsMessages.isEmpty()){
				//response = Response.ok(workflowEngine.pnlPreviewGenerate(pnlInfo)).build();
				//imposition.generatePnlPreview(pnlInfo);
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
				response = inputResponseError.createResponse();
			}
		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while generating the PNL for preview!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return response;
	}
	
	@GET
	@Path("/cache")
	//@Produces(MediaType.APPLICATION_JSON)
	public Response clearCache() {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			//lookupHandler..read(id, (Class<LookupItem>) Class.forName("com.epac.cap.model."+type));
			lookupHandler.clearCache();
		}catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the lookup record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
			//return ImpositionResponse.serverError().build();
		}
		return Response.ok().build();
	}
	
	/**
	 * @return the lookupHandler
	 */
	public LookupHandler getLookupHandler() {
		return lookupHandler;
	}

	/**
	 * @param lookupHandler the lookupHandler to set
	 */
	public void setLookupHandler(LookupHandler lookupHandler) {
		this.lookupHandler = lookupHandler;
	}

	/**
	 * @return the partHandler
	 */
	public PartHandler getPartHandler() {
		return partHandler;
	}

	/**
	 * @param partHandler the partHandler to set
	 */
	public void setPartHandler(PartHandler partHandler) {
		this.partHandler = partHandler;
	}

	/**
	 * @return the stationHandler
	 */
	public StationHandler getStationHandler() {
		return stationHandler;
	}

	/**
	 * @param stationHandler the stationHandler to set
	 */
	public void setStationHandler(StationHandler stationHandler) {
		this.stationHandler = stationHandler;
	}

	/**
	 * @return the jobHandler
	 */
	public JobHandler getJobHandler() {
		return jobHandler;
	}

	/**
	 * @param jobHandler the jobHandler to set
	 */
	public void setJobHandler(JobHandler jobHandler) {
		this.jobHandler = jobHandler;
	}

	/**
	 * @return the logHandler
	 */
	public LogHandler getLogHandler() {
		return logHandler;
	}

	/**
	 * @param logHandler the logHandler to set
	 */
	public void setLogHandler(LogHandler logHandler) {
		this.logHandler = logHandler;
	}

	/**
	 * @return the machineHandler
	 */
	public MachineHandler getMachineHandler() {
		return machineHandler;
	}

	/**
	 * @param machineHandler the machineHandler to set
	 */
	public void setMachineHandler(MachineHandler machineHandler) {
		this.machineHandler = machineHandler;
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

	/**
	 * @return the rollHandler
	 */
	public RollHandler getRollHandler() {
		return rollHandler;
	}

	/**
	 * @param rollHandler the rollHandler to set
	 */
	public void setRollHandler(RollHandler rollHandler) {
		this.rollHandler = rollHandler;
	}

	/**
	 * @return the defaultStationHandler
	 */
	public DefaultStationHandler getDefaultStationHandler() {
		return defaultStationHandler;
	}

	/**
	 * @param defaultStationHandler the defaultStationHandler to set
	 */
	public void setDefaultStationHandler(DefaultStationHandler defaultStationHandler) {
		this.defaultStationHandler = defaultStationHandler;
	}

	/**
	 * @return the workflowEngine
	 */
	public WorkflowEngine getWorkflowEngine() {
		return workflowEngine;
	}

	/**
	 * @param workflowEngine the workflowEngine to set
	 */
	public void setWorkflowEngine(WorkflowEngine workflowEngine) {
		this.workflowEngine = workflowEngine;
	}
	
}
