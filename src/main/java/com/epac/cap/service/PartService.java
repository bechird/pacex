/**
 * 
 */
package com.epac.cap.service;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.OrderBy;
import com.epac.cap.common.PersistenceException;
import com.epac.cap.handler.JobHandler;
import com.epac.cap.handler.LookupHandler;
import com.epac.cap.handler.OrderHandler;
import com.epac.cap.handler.PartHandler;
import com.epac.cap.handler.PartsSearchComparator;
import com.epac.cap.handler.WFSDataSupportHandler;
import com.epac.cap.handler.WFSLocationHandler;
import com.epac.cap.handler.WFSWorkflowHandler;
import com.epac.cap.model.Job;
import com.epac.cap.model.JobStatus;
import com.epac.cap.model.Order;
import com.epac.cap.model.Part;
import com.epac.cap.model.PartCritiria;
import com.epac.cap.model.SubPart;
import com.epac.cap.model.WFSDataSupport;
import com.epac.cap.model.WFSLocation;
import com.epac.cap.model.WFSPartWorkflow;
import com.epac.cap.model.WFSProductionStatus;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.repository.OrderSearchBean;
import com.epac.cap.repository.PartDAO;
import com.epac.cap.repository.PartSearchBean;
import com.epac.cap.utils.PaginatedResult;
import com.epac.cap.validator.InputResponseError;
import com.epac.cap.utils.LogUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;

@Controller
@Path("/parts")
public class PartService extends AbstractService {

	@Autowired
	private PartHandler partHandler;
	
	@Autowired
	private PartDAO partDAO;

	@Autowired
	private OrderHandler orderHandler;

	@Autowired
	private JobHandler jobHandler;

	@Autowired
	private LookupHandler lookupHandler;

	@Autowired
	private WFSDataSupportHandler dataSupportHandler;

	@Autowired
	private WFSLocationHandler locationHandler;

	@Autowired
	private WFSWorkflowHandler wfsWorkflowHandler;
	
	private static Logger logger = Logger.getLogger(PartService.class);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getParts() {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Part> parts = new ArrayList<Part>();
		try {
			parts = partHandler.readAll();
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of part records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(parts).build();
	}
	
	@POST
	@Path("/paginated")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPaginatedRolls(String data){
		
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		PaginatedResult paginatedResult = new PaginatedResult();
		List<Part> pageList = new ArrayList<Part>();
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			
			
			JsonNode json = mapper.readTree(data);
			
			JsonNode aoData = json.get("aoData");
			
			int draw = aoData.get(0).get("value").asInt();
			int start = aoData.get(3).get("value").asInt();
			int pageLength = aoData.get(4).get("value").asInt();
			
			//logger.debug("get page");
			//logger.debug("draw:" + draw);
			//logger.debug("start:" + start);
			//logger.debug("pageLength:" + pageLength);
			
			Integer count = 0;

			PartSearchBean searchBean = new PartSearchBean();
			searchBean.setResultOffset(start);
			searchBean.setMaxResults(pageLength);
			searchBean.setListing(true);

			
			//sorting
			int sortingColumn = aoData.get(2).get("value").get(0).get("column").asInt();
			
			String sortingColumnName = aoData.get(1).get("value").get(sortingColumn).get("data").asText();
			
			String sortingDirection = aoData.get(2).get("value").get(0).get("dir").asText();
			
			OrderBy orderBy = new OrderBy();			
			if(sortingDirection.equals("asc"))orderBy.setDirection(OrderBy.ASC);
			if(sortingDirection.equals("desc"))orderBy.setDirection(OrderBy.DESC);
			
			if(sortingColumnName.equals("partNum"))orderBy.setName("partNum");
			if(sortingColumnName.equals("isbn"))orderBy.setName("isbn");
			if(sortingColumnName.equals("title"))orderBy.setName("title");
			if(sortingColumnName.equals("category.name"))orderBy.setName("category.id");
			if(sortingColumnName.equals("colors"))orderBy.setName("colors");
			if(sortingColumnName.equals("size"))orderBy.setName("width");
			if(sortingColumnName.equals("bindingType.name"))orderBy.setName("bindingType.id");
			if(sortingColumnName.equals("paperType.name"))orderBy.setName("paperType.id");
			if(sortingColumnName.equals("lamination.name"))orderBy.setName("lamination.id");
			
			searchBean.getOrderByList().clear();
			searchBean.addOrderBy(orderBy);
			//-----------------------------------------------------------------------------------------------------

			//filtering
			boolean filtering = false;
			
			String filter = aoData.get(1).get("value").get(0).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setPartNumLike(filter);
					filtering = true;
				} catch (Exception e) {}
			}
			
			filter = aoData.get(1).get("value").get(1).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setIsbn(filter);
					filtering = true;
				} catch (Exception e) {}
			}		

			filter = aoData.get(1).get("value").get(2).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setTitle(filter);
					filtering = true;
				} catch (Exception e) {}
			}
			
			filter = aoData.get(1).get("value").get(3).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setCategoryId(filter);
					filtering = true;
				} catch (Exception e) {}
			}
			
			filter = aoData.get(1).get("value").get(4).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setColors(filter);
					filtering = true;
				} catch (Exception e) {}
			}
			
			filter = aoData.get(1).get("value").get(6).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setBindingTypeId(filter);
					filtering = true;
				} catch (Exception e) {}
			}
			
			filter = aoData.get(1).get("value").get(7).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setPaperType(filter);
					filtering = true;
				} catch (Exception e) {}
			}
			
			filter = aoData.get(1).get("value").get(8).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setLamination(filter);
					filtering = true;
				} catch (Exception e) {}
			}
			//-----------------------------------------------------------------------------------------------------
			
			
			//search word
			String wordToSearch = aoData.get(5).get("value").get("value").asText();
			
			if(wordToSearch != null && !wordToSearch.isEmpty()) {
				
				//logger.debug("wordToSearch:" + wordToSearch);
				pageList = partHandler.fullSearch(wordToSearch, null, null);
				count = pageList.size();				
				pageList = partHandler.fullSearch(wordToSearch, pageLength, start);
				
				
			}else if(filtering){
				
				searchBean.setResultOffset(null);
				searchBean.setMaxResults(null);
				pageList = partHandler.readAll(searchBean);
				count = pageList.size();
				
				searchBean.setResultOffset(start);
				searchBean.setMaxResults(pageLength);
				pageList = partHandler.readAll(searchBean);
				
			}else {
				count = partHandler.getCount();				
				pageList = partHandler.readAll(searchBean);
			}
			
			//-----------------------------------------------------------------------------------------------------

			//logger.debug("list:" + pageList.size());
			//logger.debug("global count:" + count);
			
			paginatedResult.setDraw(draw);
			paginatedResult.setRecordsFiltered(count);
			paginatedResult.setRecordsTotal(count);
			paginatedResult.setData(pageList);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of roll records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages) ;
			return inputResponseError.createResponse();
		}
		return Response.ok(paginatedResult).build();
	}
	
	@GET
	@Path("/distinctIsbn")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDistinctIsbn() {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Part> isbns = new ArrayList<Part>();
		try {
			isbns = partHandler.readDistinctIsbn();
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of part records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(isbns).build();
	}

	@POST
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchParts(PartSearchBean partSearchBean) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Part> parts = new ArrayList<Part>();
		try {
			if (StringUtils.isBlank(partSearchBean.getIsbn())) {
				partSearchBean.setIsbn(null);
			}
			if (StringUtils.isBlank(partSearchBean.getPartNumLike())) {
				partSearchBean.setPartNumLike(null);
			}
			if (partSearchBean.getVersion() != null && partSearchBean.getVersion() == 0) {
				partSearchBean.setVersion(null);
			}
			partSearchBean.setActiveFlag(true);
			partSearchBean.setHasNoParent(true);
			parts = partHandler.readAll(partSearchBean);
			Collections.sort(parts, new PartsSearchComparator());
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of part records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(parts).build();
	}

	@GET
	@Path("/generateNewPartNum")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getNewPartNum() {
		String result = "";
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {

			result = partHandler.generatePartNb();

		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while generating new part number!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		} catch (NumberFormatException nfe) {
			constraintViolationsMessages.put("errors", "An error occurred while generating new part number!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(result).build();
	}

	@GET
	@Path("/parentParts/add")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getIsbnPartsForAdd() {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Part> parts = new ArrayList<Part>();
		try {
			PartSearchBean psb = new PartSearchBean();
			// psb.setNonNullIsbns(true);
			psb.setHasNoParent(true);
			psb.setActiveFlag(true);
			parts = partHandler.readAll(psb);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of part records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(parts).build();
	}

	@GET
	@Path("/parentParts/edit")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getIsbnPartsForEdit() {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Part> parts = new ArrayList<Part>();
		try {
			PartSearchBean psb = new PartSearchBean();
			// psb.setNonNullIsbns(true);
			psb.setHasNoParent(true);
			// psb.setActiveFlag(true);
			parts = partHandler.readAll(psb);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of part records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(parts).build();
	}

	@GET
	@Path("/{partNum}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPart(@PathParam("partNum") String partNum) {
		Part part = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			part = partHandler.read(partNum);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of part records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(part).build();
	}

	@GET
	@Path("/producible/{partNum}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getIsPartProducible(@PathParam("partNum") String partNum) {
		Boolean result = false;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			OrderSearchBean osb = new OrderSearchBean();
			Set<String> theParts = new HashSet<String>();
			theParts.add(partNum);
			osb.setPartNumbers(theParts);
			List<Order> theOrders = orderHandler.readAll(osb);
			for (Order r : theOrders) {
				if (Order.OrderStatus.COMPLETE.getName().equals(r.getStatus())
						|| Order.OrderStatus.ONPROD.getName().equals(r.getStatus())) {
					result = true;
					break;
				}
			}

		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors",
					"An error occurred while checking if the part is on prod or has been produced");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(result).build();
	}

	@GET
	@Path("/isbn/{isbn}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPartByISBN(@PathParam("isbn") String isbn) {
		List<Part> parts = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			PartSearchBean partSearchBean = new PartSearchBean();
			partSearchBean.setIsbnExact(isbn);
			parts = partHandler.readAll(partSearchBean);
			if (!parts.isEmpty())
				return Response.ok(parts.get(0).getPartNum()).build();
			else
				return Response.status(404).build();
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of part records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_PM", "ROLE_ADMIN", "ROLE_LOP" })
	public Response add(Part part) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			// check for duplication by partNum if exists (may not exist if the
			// part comes from the XML file), otherwise check with the rest of
			// the part fields
			Part dbPart = null;
			PartSearchBean psb = new PartSearchBean();
			List<Part> foundResult = new ArrayList<Part>();
			if (part.getPartNum() != null && !part.getPartNum().isEmpty()) {
				psb.setPartNum(part.getPartNum());
				foundResult = partHandler.readAll(psb);
				if (!foundResult.isEmpty()) {
					constraintViolationsMessages.put("errors", "A duplicate by the Part Number is detected!");
				}
			} else {// case of a part coming from the XML parsed file.
				psb.setBindingTypeId(part.getBindingType() != null ? part.getBindingType().getId() : null);
				psb.setColors(part.getColors());
				psb.setIsbnExact(part.getIsbn());
				psb.setLamination(part.getLamination() != null ? part.getLamination().getId() : null);
				psb.setHasNoParent(true);
				psb.setPagesCount(part.getPagesCount());
				psb.setPaperType(part.getPaperType() != null ? part.getPaperType().getId() : null);
				psb.setThickness(part.getThickness());
				psb.setTitle(part.getTitle());
				foundResult = partHandler.readAll(psb);
				boolean exist = false;
				for (Part aPart : foundResult) {
					if (part.equalsSpecs(aPart)) {
						constraintViolationsMessages.put("errors", "A duplicate by the Part Specs is detected!");
						exist = true;
						dbPart = aPart;
						break;
					}
				}
				String partNb;
				if (!exist) {
					partNb = partHandler.generatePartNb();
					part.setPartNum(partNb);
				}
			}
			if (constraintViolationsMessages.isEmpty()) {
				if (part.getCreatorId() == null) {// ||
													// !part.getCreatorId().equalsIgnoreCase("system_auto")
					doAddLogging(part);
				} else {
					part.setCreatedDate(new Date());
				}
				partHandler.create(part);
				response = Response.ok(part.getPartNum()).build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
				response = (dbPart == null) ? inputResponseError.createResponse()
						: Response.ok(dbPart.getPartNum()).build();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while adding the part record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return response;
	}

	@POST
	@Path("/saveDS")
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_PM", "ROLE_ADMIN", "ROLE_LOP" })
	public Response addDataSupport(WFSDataSupport dataSupport) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			dataSupportHandler.save(dataSupport);
			response = Response.ok(dataSupport.getDataSupportId()).build();
		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while adding the datasupport record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return response;
	}

	@POST
	@Path("/saveDL")
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_PM", "ROLE_ADMIN", "ROLE_LOP" })
	public Response addDataLocation(WFSLocation location) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			locationHandler.save(location);
			response = Response.ok(location.getLocationId()).build();
		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while adding the datasupport record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return response;
	}

	public Set<Job> getPartJobs(Part part) throws PersistenceException {
		Set<Job> result = new HashSet<Job>();
		JobSearchBean jsb = new JobSearchBean();
		jsb.setPartNum(part.getPartNum());
		result.addAll(jobHandler.readAll(jsb));
		return result;
	}

	public boolean hasActiveJobs(Part part) throws PersistenceException {
		boolean result = false;
		for (Job j : getPartJobs(part)) {
			if (!JobStatus.JobStatuses.CANCELLED.getName().equals(j.getJobStatus().getId())
					&& !JobStatus.JobStatuses.COMPLETE.getName().equals(j.getJobStatus().getId())
					&& !JobStatus.JobStatuses.COMPLETE_PARTIAL.getName().equals(j.getJobStatus().getId())) {
				result = true;
				break;
			}
		}
		return result;
	}

	@GET
	@Path("/test/{partNum}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTestPart(@PathParam("partNum") String partNum) {
		Part part = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			part = partHandler.read(partNum);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of part records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(part).build();
	}

	@Path("{partNum}")
	@DELETE
	@Transactional
	@Secured({ "ROLE_PM", "ROLE_ADMIN" })
	public Response delete(@PathParam("partNum") String partNum) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response res = null;
		try {
			// Check if ok to delete the part:
			// if the part is not related to an order, we can do hard delete
			// (delete the pdfs as well)
			// if the part is related to an order, the order must not be active
			// (and so no active jobs), and we just do soft delete
			// child part is related to its parent, deleting the parent causes
			// deleting the children
			Part part = partHandler.read(partNum);
			if (part != null) {
				if (hasActiveJobs(part)) {
					constraintViolationsMessages.put("errors",
							"Cannot delete the part as it has active jobs related to it!");
				}
				if (!getPartJobs(part).isEmpty()) {
					constraintViolationsMessages.put("errors", "Cannot delete the part as it has jobs related to it!");
				}
				// TODO make sure also part has no datasupports or workflows

				if (constraintViolationsMessages.isEmpty()) {
					partHandler.delete(part);
					res = Response.status(200).build();
				} else {
					InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
					res = inputResponseError.createResponse();
				}
			} else {
				return Response.status(404).entity("Part with the id : " + partNum + " not present in the database")
						.build();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while deleting the part record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return res;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_PM", "ROLE_ADMIN", "ROLE_LOP" })
	public Response update(Part part) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			// read the original Part bean before modification
			Part originalPart = partHandler.read(part.getPartNum());

			if (originalPart != null) {
				if (!getPartJobs(originalPart).isEmpty()) {
					if (originalPart.getBindingType()!= null && part.getBindingType() != null){
						if (!originalPart.getBindingType().getId().equals(part.getBindingType().getId())
								|| !originalPart.getCritirias().equals(part.getCritirias())) {
							constraintViolationsMessages.put("errors",
									"Cannot update some of the part data as it is being processed and has jobs associated to it!");
						}
					}
				}
			} else {
				return Response.status(404)
						.entity("Part with the id : " + part.getPartNum() + " not present in the database").build();
			}

			// TODO what if ISBN has been changed?

			if (constraintViolationsMessages.isEmpty()) {
				doEditLogging(part);
				if (part.getBindingType() != null && StringUtils.isEmpty(part.getBindingType().getId())) {
					// nullify the status otherwise hibernate will ask to
					// persist the empty transient status bean
					part.setBindingType(null);
				}
				if (part.getPaperType() != null && StringUtils.isEmpty(part.getPaperType().getId())) {
					// nullify the status otherwise hibernate will ask to
					// persist the empty transient status bean
					part.setPaperType(null);
				}
				if (part.getLamination() != null && StringUtils.isEmpty(part.getLamination().getId())) {
					// nullify the status otherwise hibernate will ask to
					// persist the empty transient status bean
					part.setLamination(null);
				}
				partHandler.update(part);
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e1) {
			constraintViolationsMessages.put("errors", "An error occurred while updating the part record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return response;
	}

	@POST
	@Path("/upload/{addEdit}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Secured({ "ROLE_PM", "ROLE_ADMIN", "ROLE_LOP" })
	@Transactional
	public javax.ws.rs.core.Response upload(@FormDataParam("fileName") String fileName,
			@FormDataParam("partNum") String partNum, @FormDataParam("isbn") String isbn,
			@FormDataParam("file") InputStream fileInputStream, @FormDataParam("partCategory") String partCategory,
			@PathParam("addEdit") String addEdit) {
		OutputStream out = null;
		// buffer size used for writing
	    final int BUFFER_SIZE = 31457;
		javax.ws.rs.core.Response result = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			int read = 0;
			byte[] bytes = new byte[BUFFER_SIZE];
			// String fileId = "" + new Date().getTime();
			// //System.currentTimeMillis();

			// check for duplication by partNum if addEdit is adding not editing
			// of the part
			LogUtils.debug("Check file with isbn [" +isbn+ "] to be uploaded...");
			if ("add".equals(addEdit)) {
				PartSearchBean psb = new PartSearchBean();
				psb.setPartNum(partNum);
				List<Part> foundResult = partHandler.readAll(psb);
				if (!foundResult.isEmpty()) {
					LogUtils.debug("A duplicate by the Part Number is detected! the file with isbn [" +isbn+ "] cannot be uploaded");
					return Response.status(412).entity("A duplicate by the Part Number is detected!").build();
				}
			}

			File repository = partHandler.generateFilePath(isbn, partNum, partCategory, "Original");
			File tmpFile = null;
			if (repository != null && repository.exists()) {
				// TODO: Check if this works fine on Prod
				tmpFile = new File(repository,
						Files.getNameWithoutExtension(partHandler.generateFileName(fileName)) + ".pdf");// File.createTempFile(Files.getNameWithoutExtension(partHandler.generateFileName(fileName)),
																										// ".pdf",
																										// repository);
				SimpleDateFormat formatter= new SimpleDateFormat("HH:mm:ss");

				LogUtils.debug("Start uploading file with isbn [" +isbn+ "] at "+formatter.format(new Date()));
				out = new FileOutputStream(tmpFile);
				while ((read = fileInputStream.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}
				out.flush();
				LogUtils.debug("File with isbn [" +isbn+ "] is uploaded successfully at "+formatter.format(new Date()));
				Part part = partHandler.read(partNum);
				Map<String,Object> checkResult = partHandler.checkPdf(tmpFile, part, partCategory);
				
				String res = partHandler.resolveFilenameWithoutExtension(tmpFile.getName());
				
				Boolean  Trim = (Boolean)checkResult.get("Trim");
				if(!Trim && !Part.PartsCategory.DUSTJACKET.getName().equalsIgnoreCase(partCategory) && !Part.PartsCategory.ENDSHEET.getName().equalsIgnoreCase(partCategory)){
					constraintViolationsMessages.put("errors",
							"No TrimBox");
					InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
					LogUtils.debug("No TrimBox found for the pdf uploaded");
					result = inputResponseError.createResponse();
				}else{
				Map<String,Object> response = new HashMap<>();
				response.put("FileName", res);
				response.put("NumberOfPage", checkResult.get("NumberOfPage"));
				response.put("Spine", checkResult.get("Spine"));

				result = javax.ws.rs.core.Response.ok(response).build();
				// Add a dataSupport and Location for the new File uploaded
				WFSDataSupport wfsDataSupport = new WFSDataSupport();
				WFSLocation wfsDataSrcLocation = new WFSLocation();
				LogUtils.debug("Add a dataSupport and Location for the new File uploaded");

				wfsDataSrcLocation.setLocationType("Destination");
				wfsDataSrcLocation.setPath(tmpFile != null ? tmpFile.getAbsolutePath() : "");

				wfsDataSupport.setName("Download");
				wfsDataSupport.setDescription(partCategory);
				
				
				if (Part.PartsCategory.TEXT.getName().equalsIgnoreCase(partCategory) && !partNum.endsWith("T")) {
					partNum += "T";
				} else if (Part.PartsCategory.COVER.getName().equalsIgnoreCase(partCategory) && !partNum.endsWith("C")) {
					partNum += "C";
				} else if (Part.PartsCategory.DUSTJACKET.getName().equalsIgnoreCase(partCategory) && !partNum.endsWith("J")){
					partNum += "J";
				} else if (Part.PartsCategory.ENDSHEET.getName().equalsIgnoreCase(partCategory) && !partNum.endsWith("E")){
					partNum += "E";
				}

				wfsDataSupport.setPartNumb(partNum);
				wfsDataSupport.setCreatedDate(new Date());
				wfsDataSupport.setCreatorId(this.getExecutingUserId());
				wfsDataSupport.setProductionStatus(
						lookupHandler.read(WFSProductionStatus.statuses.ONPROD.getName(), WFSProductionStatus.class));
				// Set the older DataSupport which is onProd to Obsolete
				Part currentPart = partHandler.read(partNum);
				if (currentPart != null) {
					WFSDataSupport oldDS = currentPart.getDataSupportOnProdByName("Download");
					if (oldDS != null) {
						LogUtils.debug("Set the older DataSupport which is onProd to Obsolete");
						oldDS.setProductionStatus(lookupHandler.read(WFSProductionStatus.statuses.OBSOLETE.getName(),WFSProductionStatus.class));
						oldDS.setLastUpdateDate(new Date());
						oldDS.setLastUpdateId(currentPart.getCreatorId());
						dataSupportHandler.update(oldDS);
						
					}
				}else{// case of before there was no cover but now we're uploading cover; so update the part to be able to add the cover
					if (Part.PartsCategory.COVER.getName().equalsIgnoreCase(partCategory)){
						/*Part coverPart = new Part();
						coverPart.setPartNum(partNum);
						coverPart.setSoftDelete(part.getSoftDelete());
						coverPart.setActiveFlag(part.getActiveFlag());
						coverPart.setThickness(part.getThickness());
						coverPart.setCoverColor(part.getCoverColor());
						coverPart.setPaperType(part.getPaperType());
						coverPart.setLength(part.getLength());
						coverPart.setWidth(part.getWidth());
						coverPart.setLamination(part.getLamination());
						coverPart.setCategory(lookupHandler.read(Part.PartsCategory.COVER.getName(), PartCategory.class));
						coverPart.setIsbn(part.getIsbn());
						coverPart.setTitle(part.getTitle());
						coverPart.setCreatedDate(new Date());
						coverPart.setCreatorId(this.getExecutingUserId());
						
						partDAO.create(coverPart);
						//SubPart subPart2 = new SubPart(bean.getPartNum(), coverPart.getPartNum());
						//bean.getSubParts().add(subPart2);*/
						if(!part.getPartCritirias().isEmpty()){
							for(PartCritiria pc : part.getPartCritirias()){
								if(pc.getId().getCritiriaId().equals(PartHandler.SELFCOVER_C)){
									part.getPartCritirias().remove(pc);
									break;
								}
							}
						}
						partHandler.update(part);
					}
				}

				wfsDataSrcLocation.setCreatedDate(new Date());
				wfsDataSrcLocation.setCreatorId(this.getExecutingUserId());
				
				wfsDataSupport.addLocation(wfsDataSrcLocation);
				
				//currentPart.addDataSupports(wfsDataSupport);
				//partHandler.getPartDAO().update(currentPart);
				dataSupportHandler.save(wfsDataSupport);
				}
			} else {
				LogUtils.debug("File Location not present in the system");
				result = Response.status(404).entity("File Location not present in the system").build();
			}
			return result;
		} catch (PersistenceException p) {
			LogUtils.debug("Error in File Location while uploading file. Please check the destination location !!");
			constraintViolationsMessages.put("errors",
					"Error in File Location while uploading file. Please check the destination location !!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		} catch (IOException e) {
			LogUtils.debug("Error while uploading file. Please try again !!");
			constraintViolationsMessages.put("errors", "Error while uploading file. Please try again !!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					LogUtils.debug("Error while uploading file. Resources could not be closed !!");
					constraintViolationsMessages.put("errors",
							"Error while uploading file. Resources could not be closed !!");
					InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
					return inputResponseError.createResponse();
				}
			}
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					LogUtils.debug("Error while uploading file. Resources could not be closed !!");
					constraintViolationsMessages.put("errors",
							"Error while uploading file. Resources could not be closed !!");
					InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
					return inputResponseError.createResponse();
				}
			}
		}
	}

	@GET
	@Path("/{partCategory}/{partNum}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPartByCategory(@PathParam("partNum") String partNum,
			@PathParam("partCategory") String partCategory) {
		Part part = null;
		Part concernedPart = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			part = partHandler.read(partNum);
			if (part != null && partCategory != null) {
				if ("Text".equals(part.getCategory().getName()) || "Cover".equals(part.getCategory().getName()) ||
						"DustJacket".equals(part.getCategory().getName()) || "EndSheet".equals(part.getCategory().getName())) {
					if (partCategory.equals(part.getCategory().getName())) {
						concernedPart = part;
					}
				}
				if ("Book".equals(part.getCategory().getName())) {
					for (SubPart sp : part.getSubParts()) {
						Part child = partHandler.read(sp.getId().getSubPartNum());
						if (child != null && partCategory.equals((child.getCategory().getName()))) {
							concernedPart = child;
							break;
						}
					}
				}
				// if(concernedPart != null){

				// }
			} else {
				return Response.status(404).entity("Part with the id : " + partNum + " not present in the database")
						.build();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of part records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(concernedPart).build();
	}

	@GET
	@Path("/doc/{partCategory}/{partNum}")
	@Produces("application/pdf")
	// @Produces(MediaType.APPLICATION_OCTET_STREAM)
	public javax.ws.rs.core.Response readUploadedFile(@PathParam("partNum") String partNum,
			@PathParam("partCategory") String partCategory) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			Part part = partHandler.read(partNum);
			File file = null;
			// FileInputStream fileInputStream = null;
			if (part != null && partNum != null && partCategory != null) {
				// based on part category, find the correct part
				Part concernedPart = null;
				if ("Text".equals(part.getCategory().getName()) || "Cover".equals(part.getCategory().getName()) ||
						"DustJacket".equals(part.getCategory().getName()) || "EndSheet".equals(part.getCategory().getName())) {
					if (partCategory.equals(part.getCategory().getName())) {
						concernedPart = part;
					}
				}
				if ("Book".equals(part.getCategory().getName())) {
					for (SubPart sp : part.getSubParts()) {
						Part child = partHandler.read(sp.getId().getSubPartNum());
						if (child != null && partCategory.equals((child.getCategory().getName()))) {
							concernedPart = child;
							break;
						}
					}
				}
				if (concernedPart != null) {
					file = new File(concernedPart.getFilePath() + File.separator + concernedPart.getFileName());
				} else {
					return Response.status(404).entity("File Location not present in the system").build();
				}

				if (file != null && file.exists()) {
					// fileInputStream = new FileInputStream(file);
					ResponseBuilder response = Response.ok((Object) file);
					response.type("application/pdf");
					response.header("Content-Disposition",
							"attachment; filename=" + getOriginalFileName(concernedPart.getFileName()));
					return response.build();
				} // else{
					// return ImpositionResponse.status(404).entity("File Location not
					// present in the system").build();
					// }
				return javax.ws.rs.core.Response.ok().build();
			} else {
				return Response.status(404).entity("File Location not present in the system").build();
			}
		} catch (PersistenceException p) {
			constraintViolationsMessages.put("errors",
					"Error in File Location while downloading file. Please check the destination location !!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "Error while downloading the file. Please try again !!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		} finally {
			// fileInputStream.
		}
	}

	@GET
	@Path("/document/{dataSupportId}")
	@Produces("application/pdf")
	public javax.ws.rs.core.Response readFile(@PathParam("dataSupportId") Integer dataSupportId) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		File file = null;
		try {
			WFSDataSupport dataSupport = dataSupportHandler.getSupport(dataSupportId);
			if (dataSupport != null && !dataSupport.getLocations().isEmpty()) {
				file = new File(dataSupport.getLocations().iterator().next().getPath());
			} else {
				return Response.status(404).entity("File Location not present in the system").build();
			}

			if (file != null && file.exists()) {
				// fileInputStream = new FileInputStream(file);
				ResponseBuilder response = Response.ok((Object) file);
				if (file.isFile()) {
					// response = ImpositionResponse.ok((Object) file);
					response.type("application/pdf");
					response.header("Content-Disposition", "attachment; filename=" + file.getName());
					return response.build();
				} else if (file.isDirectory()) {
					// response = ImpositionResponse.ok((Object) file);
					// response.type("");
					// response.header("Content-Disposition", "attachment;
					// filename=" + file.getName());
					response.header("Content-Disposition", "directory");
					Desktop desktop = Desktop.getDesktop();
					desktop.open(file);
				}
				return null;
			}
			return Response.status(404).entity("File Location not present in the system").build();
		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "Error while downloading the file. Please try again !!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
	}

	/*
	 * @Path("/doc/{partCategory}/{partNum}")
	 * 
	 * @DELETE
	 * 
	 * @Transactional
	 * 
	 * @Secured({ "ROLE_PM", "ROLE_ADMIN", "ROLE_LOP"}) public ImpositionResponse
	 * removeFile(@PathParam("partCategory") String
	 * partCategory, @PathParam("partNum") String partNum ){ Map<String, String>
	 * constraintViolationsMessages = new HashMap<String, String>(); File file =
	 * null; try{ Part part = partHandler.read(partNum); if(part != null){ file
	 * = new File(part.getFilePath()+File.separator+part.getFileName()); if(file
	 * != null && file.exists()){ file.delete(); } return
	 * javax.ws.rs.core.Response.ok( ).build(); }else{ return
	 * ImpositionResponse.status(404).entity("Part with the id : " + partNum +
	 * " not present in the database").build(); } }catch (PersistenceException
	 * e) { constraintViolationsMessages.put("errors",
	 * "An error occurred while deleting the part record!"); InputResponseError
	 * inputResponseError = new InputResponseError(constraintViolationsMessages)
	 * ; return inputResponseError.createResponse(); } }
	 */

	@Path("/doc/{partCategory}/{partNum}")
	@DELETE
	@Transactional
	@Secured({ "ROLE_PM", "ROLE_ADMIN", "ROLE_LOP" })
	public Response removeFile(@PathParam("partCategory") String partCategory, @PathParam("partNum") String partNum) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			Part part = partHandler.read(partNum);
			if (part != null) {
				// set the older workflow along with its data supports to obsolete
				WFSPartWorkflow oldPartWorkflow = part.getPartWorkFlowOnProd();
				if (oldPartWorkflow != null){
					oldPartWorkflow.setWfStatus(WFSProductionStatus.statuses.OBSOLETE.getName());
					oldPartWorkflow.setLastUpdateId(part.getCreatorId());
					oldPartWorkflow.setLastUpdateDate(new Date());
					wfsWorkflowHandler.updatePartWorkflow(oldPartWorkflow);
					for(WFSDataSupport dsIter : part.getDataSupports()){
						if (dsIter.getProductionStatus() != null &&
								!WFSProductionStatus.statuses.OBSOLETE.getName().equals(dsIter.getProductionStatus().getId())){
							dsIter.setProductionStatus(lookupHandler.read(WFSProductionStatus.statuses.OBSOLETE.getName(),WFSProductionStatus.class ));
							dsIter.setLastUpdateDate(new Date());
							dsIter.setLastUpdateId(part.getCreatorId());
							dataSupportHandler.update(dsIter);
						}
					}
				}
				return javax.ws.rs.core.Response.ok().build();
			} else {
				return Response.status(404).entity("Part with the id : " + partNum + " not present in the database")
						.build();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while deleting the part record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
	}

	/**
	 * @return the partHandler
	 */
	public PartHandler getPartHandler() {
		return partHandler;
	}

	/**
	 * @param partHandler
	 *            the partHandler to set
	 */
	public void setPartHandler(PartHandler partHandler) {
		this.partHandler = partHandler;
	}

	/**
	 * @return the partDAO
	 */
	public PartDAO getPartDAO() {
		return partDAO;
	}
	/**
	 * @param partDAO the partDAO to set
	 */
	public void setPartDAO(PartDAO partDAO) {
		this.partDAO = partDAO;
	}
	/**
	 * @return the lookupHandler
	 */
	public LookupHandler getLookupHandler() {
		return lookupHandler;
	}

	/**
	 * @param lookupHandler
	 *            the lookupHandler to set
	 */
	public void setLookupHandler(LookupHandler lookupHandler) {
		this.lookupHandler = lookupHandler;
	}

	/**
	 * @return the orderHandler
	 */
	public OrderHandler getOrderHandler() {
		return orderHandler;
	}

	/**
	 * @return the jobHandler
	 */
	public JobHandler getJobHandler() {
		return jobHandler;
	}

	/**
	 * @param jobHandler
	 *            the jobHandler to set
	 */
	public void setJobHandler(JobHandler jobHandler) {
		this.jobHandler = jobHandler;
	}

	/**
	 * @param orderHandler
	 *            the orderHandler to set
	 */
	public void setOrderHandler(OrderHandler orderHandler) {
		this.orderHandler = orderHandler;
	}

	/**
	 * A method that returns the uploaded file name without the timestamp
	 * portion
	 */
	public String getOriginalFileName(String fileName) {
		String originalName = fileName.substring(0, fileName.lastIndexOf("_"));
		// String timestampName = fileName.substring(originalName.length()+1,
		// fileName.lastIndexOf("."));
		String extension = fileName.substring(fileName.lastIndexOf("."));
		return originalName + extension;

	}

	@GET
	@Path("byIsbn/{isbn}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPartByIsbn(@PathParam("isbn") String isbn) {
		Part part = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {

			part = partHandler.readByIsbn(isbn);
			return Response.ok(part).build();

		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the part!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}

	}
}
