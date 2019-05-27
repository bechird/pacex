package com.epac.cap.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import com.epac.cap.common.OrderBy;
import com.epac.cap.common.PersistenceException;
import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.handler.BonLivraisonHandler;
import com.epac.cap.handler.CustomerHandler;
import com.epac.cap.handler.JobHandler;
import com.epac.cap.handler.OrderHandler;
import com.epac.cap.handler.PalletteHandler;
import com.epac.cap.handler.PartHandler;
import com.epac.cap.handler.WFSDataSupportsByIdOrderingComparator;
import com.epac.cap.model.BonLivraison;
import com.epac.cap.model.BonLivraison.blStatus;
import com.epac.cap.model.Job;
import com.epac.cap.model.JobStatus;
import com.epac.cap.model.LoadTag;
import com.epac.cap.model.Order;
import com.epac.cap.model.OrderBl;
import com.epac.cap.model.OrderPart;
import com.epac.cap.model.Package;
import com.epac.cap.model.PackageBook;
import com.epac.cap.model.Pallette;
import com.epac.cap.model.PalletteBook;
import com.epac.cap.model.Part;
import com.epac.cap.model.WFSDataSupport;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.repository.LoadTagSearchBean;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.repository.OrderSearchBean;
import com.epac.cap.utils.LogUtils;
import com.epac.cap.utils.PaginatedResult;
import com.epac.cap.validator.InputResponseError;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.html.WebColors;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.AcroFields.FieldPosition;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImage;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;
import com.itextpdf.text.pdf.PdfStamper;

@Controller
@Path("/orders")
public class OrderService extends AbstractService {

	private static Logger logger = Logger.getLogger(OrderService.class);

	private static final String ALLICATION_JSON_UTF8 = "application/json; charset=utf-8";

	@Autowired
	private OrderHandler orderHandler;

	@Autowired
	private JobHandler jobHandler;

	@Autowired
	private CustomerHandler customerHandler;

	@Autowired
	private LookupDAO lookupDAO;

	@Autowired
	private PartHandler partHandler;

	private ExecutorService executor;

	@Autowired
	private BonLivraisonHandler bonLivraisonHandler;
	@Autowired
	private PalletteHandler palletteHandler;

	public OrderService() {
		// start();
	}

	public void start() {
		LogUtils.debug("StaticThreadService.start()");
		Runnable task = new Runnable() {

			@Override
			public void run() {

				while (partHandler == null || customerHandler == null || lookupDAO == null || orderHandler == null)
					try {
						Thread.sleep(5000);
						LogUtils.debug("Waiting for injection customerHandler, partHandler, lookupDAO");
					} catch (Exception e) {
					}

				LogUtils.debug("customerHandler ready, running SFTPWatchDog daemon ");

				// OrderXMLParser orderXMLParser = new OrderXMLParser();
				// orderXMLParser.parseXML( customerHandler, partHandler, lookupDAO,
				// orderHandler);

			}
		};
		LogUtils.debug("Staring the Metadata Files Wtchdog to new Thread");
		executor = Executors.newSingleThreadExecutor();
		executor.execute(task);
	}

	/*
	 * public static void shutdown() throws Exception{
	 * LogUtils.debug("StaticThreadService.shutdown()"); SFTPWatchDog.shutdown();
	 * executor.shutdown();
	 * 
	 * if(!executor.awaitTermination(1, TimeUnit.SECONDS)){
	 * LogUtils.debug("Can't stop, Killing it..."); executor.shutdownNow(); }
	 * 
	 * }
	 */

	@GET
	// @Path("/overview")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrders() {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Order> orders = new ArrayList<Order>();
		try {
			orders = orderHandler.readAll();
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of order records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(orders).build();
	}
	
	@GET
	@Path("/errorStatusCount")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getErrorStatusCount() {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		int errorStatusCount = 0;
		try {
			errorStatusCount = orderHandler.getErrorStatusCount();
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while retrieving how many erroneous orders we have!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(errorStatusCount).build();
	}
	
	@POST
	@Path("/paginated")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPaginated(String data){
		
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		PaginatedResult paginatedResult = new PaginatedResult();
		List<Order> pageList = new ArrayList<Order>();
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			
			
			JsonNode json = mapper.readTree(data);
			
			JsonNode aoData = json.get("aoData");
			
			int draw = aoData.get(0).get("value").asInt();
			int start = aoData.get(3).get("value").asInt();
			int pageLength = aoData.get(4).get("value").asInt();
			
			//logger.debug("get page");
			//.debug("draw:" + draw);
			//logger.debug("start:" + start);
			//logger.debug("pageLength:" + pageLength);
			
			Integer count = 0;

			OrderSearchBean searchBean = new OrderSearchBean();
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
			
			if(sortingColumnName.equals("orderId"))orderBy.setName("orderId");
			if(sortingColumnName.equals("orderNum"))orderBy.setName("orderNum");
			if(sortingColumnName.equals("customer.email"))orderBy.setName("c.email");
			if(sortingColumnName.equals("customer.fullName"))orderBy.setName("c.firstName");
			//if(sortingColumnName.equals("orderPart.part.isbn"))orderBy.setName("orderPart.part.isbn");
			if(sortingColumnName.equals("source"))orderBy.setName("source");
			if(sortingColumnName.equals("recievedDate"))orderBy.setName("recievedDate");
			if(sortingColumnName.equals("dueDate"))orderBy.setName("dueDate");			
			//if(sortingColumnName.equals("orderPart.quantity"))orderBy.setName("orderPart.quantity");	
			if(sortingColumnName.equals("priority"))orderBy.setName("priority");	
			if(sortingColumnName.equals("status"))orderBy.setName("status");	
			
			searchBean.getOrderByList().clear();
			searchBean.addOrderBy(orderBy);
			//-----------------------------------------------------------------------------------------------------

			//filtering
			boolean filtering = false;
			
			String filter = aoData.get(1).get("value").get(0).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {				
					searchBean.setSearchOrderIdPart(filter);
					filtering = true;
				} catch (Exception e) {}
			}

			filter = aoData.get(1).get("value").get(1).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setOrderNum(filter);
					filtering = true;
				} catch (Exception e) {}
			}

			filter = aoData.get(1).get("value").get(2).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {					
					searchBean.setEmail(filter);
					filtering = true;
				} catch (Exception e) {}
			}

			filter = aoData.get(1).get("value").get(3).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setFullName(filter);
					filtering = true;
				} catch (Exception e) {}
			}			

			filter = aoData.get(1).get("value").get(4).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setSearchIsbn(filter);
				} catch (Exception e) {}
			}
			
			filter = aoData.get(1).get("value").get(5).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setSource(filter);
					filtering = true;
				} catch (Exception e) {}
			}

			filter = aoData.get(1).get("value").get(6).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {					
					SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
				    Date date = parser.parse(filter); 
					searchBean.setReceivedDateExact(date);
					filtering = true;
				} catch (Exception e) {}
			}

			
			filter = aoData.get(1).get("value").get(7).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {					
					SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
				    Date date = parser.parse(filter); 
					searchBean.setDueDateExact(date);
					filtering = true;
				} catch (Exception e) {}
			}

			filter = aoData.get(1).get("value").get(8).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setSearchQuantity(filter);
				} catch (Exception e) {}
			}
			
			filter = aoData.get(1).get("value").get(9).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setPriorityLevel(filter);
					filtering = true;
				} catch (Exception e) {}
			}
			
			filter = aoData.get(1).get("value").get(10).get("search").get("value").asText();
			if(filter != null && !filter.isEmpty()) {
				try {
					searchBean.setStatus(filter);
					filtering = true;
				} catch (Exception e) {}
			}
			

			//-----------------------------------------------------------------------------------------------------
			//search word
			String wordToSearch = aoData.get(5).get("value").get("value").asText();
			
			if(wordToSearch != null && !wordToSearch.isEmpty()) {
				
				//logger.debug("wordToSearch:" + wordToSearch);
				pageList = orderHandler.fullSearch(wordToSearch, null, null);
				count = pageList.size();				
				pageList = orderHandler.fullSearch(wordToSearch, pageLength, start);
				
				
			}else if(filtering){
				
				searchBean.setResultOffset(null);
				searchBean.setMaxResults(null);
				pageList = orderHandler.readAll(searchBean);
				count = pageList.size();
				
				searchBean.setResultOffset(start);
				searchBean.setMaxResults(pageLength);
				pageList = orderHandler.readAll(searchBean);
				
			}else {
				
				if(StringUtils.isBlank(searchBean.getSearchIsbn()) && StringUtils.isBlank(searchBean.getSearchQuantity())){
					count = orderHandler.getCount();				
					pageList = orderHandler.readAll(searchBean);
				}else {
					pageList = orderHandler.searchIsbnAndQuantity(searchBean.getSearchIsbn(), searchBean.getSearchQuantity(), searchBean);
					count = pageList.size();
				}
				
				
				
				
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
	@Path("/acceptance")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrdersForAcceptance() {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		List<Order> orders = new ArrayList<Order>();
		try {
			OrderSearchBean osb = new OrderSearchBean();
			osb.setStatus(Order.OrderStatus.PENDING.getName());
			orders = orderHandler.readAll(osb);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of order records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(orders).build();
	}

	@POST
	@Path("/toAccept")
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_PM", "ROLE_ADMIN", "ROLE_LOP" })
	public Response acceptOrders(int[] orderIds) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			String executingUserId = this.getExecutingUserId();// TODO make sure this works
			if (constraintViolationsMessages.isEmpty()) {
				if (orderIds != null && orderIds.length > 0) {
					orderHandler.acceptOrders(orderIds, executingUserId);
				}
				response = Response.ok().build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while accepting the orders!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return response;
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrder(@PathParam("id") Integer id) {
		Order order = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			order = orderHandler.read(id);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of order records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(order).build();
	}

	@GET
	@Path("/byNum/{orderNum}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrder(@PathParam("orderNum") String orderNum) {
		Order order = null;

		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			order = orderHandler.readByOrderNum(orderNum);
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of order records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(order).build();
	}

	@GET
	@Path("/overview/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrderOverview(@PathParam("id") Integer id) {
		Order order = null;
		// List<WFSDataSupport> dataSupports = new ArrayList<WFSDataSupport>();
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			order = orderHandler.read(id);

			order.getOrderParts().forEach((orderPart) -> {
				Part part = orderPart.getPart();
				Set<WFSDataSupport> dataSupports = new HashSet<>();
				if (part.getSubParts() != null)
					part.getSubParts().forEach((subPart) -> {
						try {
							Part sub = partHandler.read(subPart.getId().getSubPartNum());
							Set<WFSDataSupport> dataSupportsOnProd = sub.getDataSupportsOnProd();
							if (dataSupportsOnProd != null)
								dataSupportsOnProd.forEach((dataSupport) -> {
									dataSupports.add(dataSupport);
								});
						} catch (PersistenceException e) {
							e.printStackTrace();
						}
					});

				// FIXME: what's the logic behind this?
				if (dataSupports != null) {
					SortedSet<WFSDataSupport> tmpDS = new TreeSet<WFSDataSupport>(
							new WFSDataSupportsByIdOrderingComparator());
					tmpDS.addAll(dataSupports);
					if (!part.getDataSupportsOnProd().isEmpty()) {
						tmpDS.addAll(part.getDataSupportsOnProd());
						part.setDataSupportsOnProd(tmpDS);
					} else
						part.setDataSupportsOnProd(tmpDS);

					orderPart.setPart(part);
				}
				// part.setDataSupports(nes Collections.sort(dataSupports, new
				// WFSDataSupportsByIdOrderingComparator()));

			});
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of order records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}

		return Response.ok(order).build();
	}
	
	@GET
	@Path("/orderPartDs/{partNum}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrderPartDs(@PathParam("partNum") String partNum) {
		Part part = null;
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		try {
			part = partHandler.read(partNum);
			Set<WFSDataSupport> dataSupports = new HashSet<>();
				if (part.getSubParts() != null)
					part.getSubParts().forEach((subPart) -> {
						try {
							Part sub = partHandler.read(subPart.getId().getSubPartNum());
							Set<WFSDataSupport> dataSupportsOnProd = sub.getDataSupportsOnProd();
							if (dataSupportsOnProd != null)
								dataSupportsOnProd.forEach((dataSupport) -> {
									dataSupports.add(dataSupport);
								});
						} catch (PersistenceException e) {
							e.printStackTrace();
						}
					});

				// FIXME: what's the logic behind this?
				if (dataSupports != null) {
					SortedSet<WFSDataSupport> tmpDS = new TreeSet<WFSDataSupport>(
							new WFSDataSupportsByIdOrderingComparator());
					tmpDS.addAll(dataSupports);
					if (!part.getDataSupportsOnProd().isEmpty()) {
						tmpDS.addAll(part.getDataSupportsOnProd());
						part.setDataSupportsOnProd(tmpDS);
					} else
						part.setDataSupportsOnProd(tmpDS);

				}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while reading the list of order records!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}

		return Response.ok(part).build();
	}

	@POST
	@Consumes(ALLICATION_JSON_UTF8)
	@Secured({ "ROLE_PM", "ROLE_ADMIN", "ROLE_LOP" })
	public Response add(Order order) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			// check for duplication by PO#
			OrderSearchBean osb = new OrderSearchBean();
			osb.setOrderNumExact(order.getOrderNum());
			List<Order> foundResult = orderHandler.readAll(osb);
			if (!foundResult.isEmpty()) {
				constraintViolationsMessages.put("errors", "PO number '" + order.getOrderNum() + "' already exists!");
			}

			if (constraintViolationsMessages.isEmpty()) {
				if (order.getCreatorId() == null) {
					doAddLogging(order);
				} else {// case of creator id set at the XML parser
					order.setCreatedDate(new Date());
				}
				orderHandler.create(order);
				response = Response.ok(order.getOrderId()).build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while adding the order record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return response;
	}

	@Path("{id}")
	@DELETE
	@Transactional
	@Secured({ "ROLE_PM", "ROLE_ADMIN" })
	public Response delete(@PathParam("id") Integer id) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response res = null;
		try {
			// Check if ok to delete the order
			Order order = orderHandler.read(id);
			if (order != null) {
				if (getOrderJobs(order).isEmpty()) {
					orderHandler.delete(order);

					res = Response.status(200).build();
				} else {
					constraintViolationsMessages.put("errors",
							"Cannot delete the order as it has jobs associated to it!");
					InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
					res = inputResponseError.createResponse();
				}
			} else {
				return Response.status(404).entity("Order with the id : " + id + " not present in the database")
						.build();
			}
		} catch (PersistenceException e) {
			constraintViolationsMessages.put("errors", "An error occurred while deleting the order record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return res;
	}

	public Set<Job> getOrderJobs(Order order) throws PersistenceException {
		Set<Job> result = new HashSet<Job>();
		JobSearchBean jsb = new JobSearchBean();
		jsb.setOrderId(order.getOrderId());
		result.addAll(jobHandler.readAll(jsb));
		return result;
	}

	public boolean hasActiveJobs(Order order) throws PersistenceException {
		boolean result = false;
		for (Job j : getOrderJobs(order)) {
			if (!JobStatus.JobStatuses.CANCELLED.getName().equals(j.getJobStatus().getId())
					&& !JobStatus.JobStatuses.COMPLETE.getName().equals(j.getJobStatus().getId())
					&& !JobStatus.JobStatuses.COMPLETE_PARTIAL.getName().equals(j.getJobStatus().getId())) {
				result = true;
				break;
			}
		}
		return result;
	}

	@PUT
	@Path("/orderPart")
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_PM", "ROLE_ADMIN", "ROLE_LOP" })
	public Response update(OrderPart orderPart) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			orderHandler.updateOrderPart(orderPart);
			response = Response.ok(orderPart.getId()).build();
		} catch (PersistenceException e1) {
			constraintViolationsMessages.put("errors",
					"An error occurred while updating the order part:" + orderPart.getId());
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return response;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Secured({ "ROLE_PM", "ROLE_ADMIN", "ROLE_LOP" })
	public Response update(Order order) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Response response = null;
		try {
			// check for duplication by PO#
			OrderSearchBean osb = new OrderSearchBean();
			osb.setOrderNumExact(order.getOrderNum());
			osb.setOrderIdDiff(order.getOrderId());
			List<Order> foundResult = orderHandler.readAll(osb);
			if (!foundResult.isEmpty()) {
				constraintViolationsMessages.put("errors", "A duplicate by the Order Number is detected!");
			}

			// see if ok to update the order status:
			// if(Order.OrderStatus.COMPLETE.getName().equals(order.getStatus()) &&
			// order.hasActiveJobs()){
			if (Order.OrderStatus.COMPLETE.getName().equals(order.getStatus()) && hasActiveJobs(order)) {
				constraintViolationsMessages.put("errors",
						"Cannot update order status to complete as there are currently active jobs for this order!");
			}
			if (Order.OrderStatus.CANCELLED.getName().equals(order.getStatus()) && hasActiveJobs(order)) {
				constraintViolationsMessages.put("errors",
						"Cannot update order status to cancelled as there are currently active jobs for this order!");
			}
			if (Order.OrderStatus.REJECTED.getName().equals(order.getStatus())) {
				if (!getOrderJobs(order).isEmpty()) {
					constraintViolationsMessages.put("errors",
							"Cannot update order status to rejected as there are currently jobs for this order!");
				}
			}
			if (Order.OrderStatus.TOEPAC.getName().equals(order.getStatus()) && hasActiveJobs(order)) {
				constraintViolationsMessages.put("errors",
						"Cannot update order status to 'Send to Epac' as there are currently active jobs for this order!");
			}

			if (constraintViolationsMessages.isEmpty()) {
				doEditLogging(order);
				orderHandler.update(order);
				response = Response.ok(order.getOrderId()).build();
			} else {
				InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
				response = inputResponseError.createResponse();
			}
		} catch (PersistenceException e1) {
			constraintViolationsMessages.put("errors", "An error occurred while updating the order record!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return response;
	}

	/**
	 * @return the orderHandler
	 */
	public OrderHandler getOrderHandler() {
		return orderHandler;
	}

	/**
	 * @param orderHandler
	 *            the orderHandler to set
	 */
	public void setOrderHandler(OrderHandler orderHandler) {
		this.orderHandler = orderHandler;
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

	@GET
	@Path("/finishing")
	@Produces(MediaType.APPLICATION_JSON)
	public Response ordersComplete() {
		List<Order> orders = orderHandler.fetchOrderInFInishing();
		return Response.ok(orders).build();
	}

	@GET
	@Path("/completeStatus/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateStatus(@PathParam("id") int id) {
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		Job job = null;
		Order order = null;
		try {
			order = orderHandler.read(id);
			JobSearchBean searchbean = new JobSearchBean();
			searchbean.setStationId("SHIPPING");
			searchbean.setOrderId(order.getOrderId());
			List<Job> jobs = jobHandler.readAll(searchbean);
			job = jobs.get(0);
			job.setJobStatus(lookupDAO.read("DELIVERED", JobStatus.class));
			jobHandler.update(job);
			order.setStatus("DELIVERED");
			Date date = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			order.setCompleteDate(cal.getTime());
			orderHandler.update(order);
		} catch (Exception e) {
			constraintViolationsMessages.put("errors", "An error occurred while complete Order!");
			InputResponseError inputResponseError = new InputResponseError(constraintViolationsMessages);
			return inputResponseError.createResponse();
		}
		return Response.ok(order).build();
	}

	private ByteArrayInputStream createBl(List<Order> orders, List<Long> palletteIds, List<Integer> quantities) {

		File bonLivraisonFile;
		try {
			bonLivraisonFile = File.createTempFile("bonLivraison_Info", ".pdf");

			FileInputStream file = new FileInputStream(new File(System.getProperty(ConfigurationConstants.BLIVRAISON)));
			if (orders.size() > 1)
				file = new FileInputStream(new File(System.getProperty(ConfigurationConstants.BLIVRAISONINT)));
			PdfReader reader = new PdfReader(file);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfStamper stamper = new PdfStamper(reader, baos);
			List<ByteArrayOutputStream> listContentPdf = new ArrayList<>();

			listContentPdf.add(baos);

			BonLivraison bl = new BonLivraison();
			int numero = bonLivraisonHandler.getMaxCount();
			int newNumero = numero + 1;
			bl.setNum(newNumero);
			Date date = new Date();
			bl.setCreationDate(date);
			bl.setStatus(blStatus.NEW);
			bonLivraisonHandler.save(bl);
			AcroFields form = stamper.getAcroFields();
			form.setField("n_bdl", bl.getNumBL());
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			String d = format.format(date);
			form.setField("date", d);
			List<Order> fusionedOrder = new ArrayList<>();
			List<Integer> fusionedQuantities = new ArrayList<>();
			int j = 0;
			for (Order order : orders) {
				int indexFusionedOrder = fusionedOrder.indexOf(order);
				int indexOrder = orders.indexOf(order);
				if (indexFusionedOrder < 0) {
					fusionedOrder.add(order);
					int qtyy = quantities.get(indexOrder);
					fusionedQuantities.add(qtyy);
				} else {
					int qty = fusionedQuantities.get(indexFusionedOrder) + quantities.get(j);
					fusionedQuantities.set(indexFusionedOrder, qty);
				}
				j++;
			}

			int i = 1;
			Integer totalBookBl = 0;
			for (Order order : fusionedOrder) {

				Set<Package> orderPackages = order.getOrderPackages();
				if (orders.size() == 1)
					form.setField("Text1", orderPackages.iterator().next().getLabel());

				form.setField("pos" + i, String.valueOf(i));
				form.setField("titre" + i, order.getOrderPartOrigin().getPart().getTitle());
				form.setField("ean" + i, order.getOrderPartOrigin().getPart().getIsbn());
				form.setField("fsc" + i, order.getOrderPartOrigin().getPart().getPublisher());
				form.setField("comm" + i, order.getOrderNum());
				bl.setDestination(orderPackages.iterator().next().getDestination());
				Integer totalBookOrder = fusionedQuantities.get(i - 1);
				totalBookBl += totalBookOrder;
				OrderBl oderBl = new OrderBl();
				oderBl.setQty(totalBookOrder);
				oderBl.setBonLivraison(bl);
				bonLivraisonHandler.createOrderBl(oderBl);

				order.getOrder_Bl().add(oderBl);
				orderHandler.update(order);
				form.setField("qty" + i, totalBookOrder.toString());
				i++;
			}
			bl.setQty(totalBookBl);
			bonLivraisonHandler.update(bl);
			for (Long id : palletteIds) {
				Pallette pallette = palletteHandler.read(id);
				pallette.setBlNumber(bl.getNum());
				palletteHandler.update(pallette);
			}
			// String[] description = bl.getDestination().split("/");
			form.setField("adresse", bl.getDestination());
			stamper.setFormFlattening(true);
			stamper.close();
			reader.close();
			Document document = new Document();

			PdfCopy copy = new PdfCopy(document, new FileOutputStream(bonLivraisonFile));
			document.open();
			for (ByteArrayOutputStream br : listContentPdf) {
				reader = new PdfReader(br.toByteArray());
				// document.open();
				copy.addDocument(reader);
				reader.close();
			}
			copy.freeReader(reader);
			document.close();

			HttpHeaders headers = new HttpHeaders();
			headers.put("Content-Type", Arrays.asList("application/pdf"));
			headers.put("Content-Disposition", Arrays.asList("attachment;filename=BL" + bl.getNum()));
			byte[] out = FileUtils.readFileToByteArray(bonLivraisonFile);
			headers.put("Content-Length", Arrays.asList(String.valueOf(out.length)));
			ByteArrayInputStream input = new ByteArrayInputStream(out);
			return input;
		} catch (IOException | DocumentException | PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private ByteArrayInputStream printBl(BonLivraison bl, List<Order> orders, List<Integer> quantities) {

		File bonLivraisonFile;
		try {
			bonLivraisonFile = File.createTempFile("bonLivraison_Info", ".pdf");

			FileInputStream file = new FileInputStream(new File(System.getProperty(ConfigurationConstants.BLIVRAISON)));
			if (orders.size() > 1)
				file = new FileInputStream(new File(System.getProperty(ConfigurationConstants.BLIVRAISONINT)));
			PdfReader reader = new PdfReader(file);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfStamper stamper = new PdfStamper(reader, baos);
			List<ByteArrayOutputStream> listContentPdf = new ArrayList<>();

			listContentPdf.add(baos);

			AcroFields form = stamper.getAcroFields();
			form.setField("n_bdl", bl.getNumBL());
			Date date = bl.getCreationDate();
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			String d = format.format(date);
			form.setField("date", d);

			int i = 1;

			for (Order order : orders) {

				Set<Package> orderPackages = order.getOrderPackages();
				if (orders.size() == 1)
					form.setField("Text1", orderPackages.iterator().next().getLabel());
				form.setField("pos" + i, String.valueOf(i));
				form.setField("titre" + i, order.getOrderPart().getPart().getTitle());
				form.setField("ean" + i, order.getOrderPart().getPart().getIsbn());
				form.setField("fsc" + i, order.getOrderPartOrigin().getPart().getPublisher());
				form.setField("comm" + i, order.getOrderNum());

				bl.setDestination(orderPackages.iterator().next().getDestination());
				Integer totalBookOrder = quantities.get(i - 1);
				form.setField("qty" + i, totalBookOrder.toString());
				i++;
			}

			form.setField("adresse", bl.getDestination());
			stamper.setFormFlattening(true);
			stamper.close();
			reader.close();
			Document document = new Document();

			PdfCopy copy = new PdfCopy(document, new FileOutputStream(bonLivraisonFile));
			document.open();
			for (ByteArrayOutputStream br : listContentPdf) {
				reader = new PdfReader(br.toByteArray());
				// document.open();
				copy.addDocument(reader);
				reader.close();
			}
			copy.freeReader(reader);
			document.close();

			HttpHeaders headers = new HttpHeaders();
			headers.put("Content-Type", Arrays.asList("application/pdf"));
			headers.put("Content-Disposition", Arrays.asList("attachment;filename=BL" + bl.getNum()));
			byte[] out = FileUtils.readFileToByteArray(bonLivraisonFile);
			headers.put("Content-Length", Arrays.asList(String.valueOf(out.length)));
			ByteArrayInputStream input = new ByteArrayInputStream(out);
			return input;
		} catch (IOException | DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@POST
	@Path("/Bl")
	@Consumes({ "application/json" })
	public Response createBLivraison(Map<String, Object> maps) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			List<Order> orders = mapper.convertValue(maps.get("orders"), new TypeReference<List<Order>>() {
			});
			List<Integer> quantities = mapper.convertValue(maps.get("quantities"), new TypeReference<List<Integer>>() {
			});
			List<Long> palletteIds = mapper.convertValue(maps.get("palletteIds"), new TypeReference<List<Long>>() {
			});
			ByteArrayInputStream input = createBl(orders, palletteIds, quantities);

			return Response.ok(input).build();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	@GET
	@Path("/Bl/{blId}")
	@Consumes({ "application/json" })
	public Response printBLivraison(@PathParam("blId") long blId) {
		
		ByteArrayInputStream input = null;
		Map<Order, Integer> ordersAndQuantitesMap = new HashMap<Order, Integer>();
		
		try {
			List<Order> orders = bonLivraisonHandler.fetchOrdersByBl(blId);
			BonLivraison bl = bonLivraisonHandler.fetchBl(blId);

			List<Integer> quantities = new ArrayList<>();

			for (Order order : orders) {
				Set<OrderBl> bls = order.getOrder_Bl();
				for (OrderBl ordebl : bls) {
					if (ordebl.getBonLivraison().getId().equals(blId)) {
						quantities.add(ordebl.getQty());
					}
				}
			}
			
			
			for(int i=0;i<orders.size();i++) {
				ordersAndQuantitesMap.put(orders.get(i), quantities.get(i));
			}
					
			if (ordersAndQuantitesMap.size() == 1) {				
				input = createBlMonoOrderV2(bl, ordersAndQuantitesMap);
			}
			
			if (ordersAndQuantitesMap.size() > 1) {
				input = createBlMultiOrdersV2(bl, ordersAndQuantitesMap);
			}
			
			return Response.ok(input).build();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;		
		
	}

	@GET
	@Path("/qtyInPallete/{orderNum}/")
	public Response qtyInPallete(@PathParam("orderNum") String orderNum) {

		Order order;
		try {

			order = orderHandler.readByOrderNum(orderNum);

			if (order != null) {
				int totalBookOrder = 0;
				Set<Package> orderPackages = order.getOrderPackages();
				for (Package pkgs : orderPackages) {
					for (Package pkg : pkgs.getPackages()) {
						Set<PackageBook> pckBooks = pkg.getPcbs();
						for (PackageBook pcb : pckBooks) {
							int qtyInPcb = pcb.getDepthQty() * pcb.getHeightQty() * pcb.getWidthQty();
							int qty = 0;
							List<PalletteBook> pllettesBook = palletteHandler
									.fetchPalletteBookByPcbId(pcb.getPackagePartId());
							for (PalletteBook pb : pllettesBook) {
								qty += pb.getQuantity();
							}
							totalBookOrder += qtyInPcb * qty;

						}
					}
				}
				return Response.ok(totalBookOrder).build();
			}
		} catch (Exception e) {

		}
		return Response.ok().build();
	}

	@GET
	@Path("/workflow/{partNum}/{orderId}/{source}")
	public Response rerunWorkFlow(@PathParam("partNum") String partNum, @PathParam("orderId") Integer orderId, @PathParam("source") String source) {
		try {
			orderHandler.rerunWorkFlow(partNum, orderHandler.read(orderId), source);
		} catch (Exception e) {
			LogUtils.error("Error occured when re-running workflow for part: " + partNum, e);
		}

		return Response.ok().build();
	}
	
	@GET
	@Path("/workflows/{orderId}")
	public Response rerunAllWorkFlows(@PathParam("orderId") Integer orderId) {
		try {

			orderHandler.rerunAllWorkFlows(orderId);
		} catch (Exception e) {
			LogUtils.error("Rerun All workflows service error for Order [" + orderId + "]: ", e);
		}

		return Response.ok().build();
	}

	@GET
	@Path("/inPallets/")
	public Response fetchOrderInPallet() {
		List<Order> result = orderHandler.fetchOrderInPallet();
		return Response.ok(result).build();
	}
	
	
	@POST
	@Path("/inPallets/paginated")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPaginated2(String data){
		
		Map<String, String> constraintViolationsMessages = new HashMap<String, String>();
		PaginatedResult paginatedResult = new PaginatedResult();
		List<Order> pageList = new ArrayList<Order>();
		
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
			
			
			//sorting
			int sortingColumn = aoData.get(2).get("value").get(0).get("column").asInt();
			
			String sortingColumnName = aoData.get(1).get("value").get(sortingColumn).get("data").asText();
			
			String sortingDirection = aoData.get(2).get("value").get(0).get("dir").asText();
			
			OrderBy orderBy = new OrderBy();			
			if(sortingDirection.equals("asc"))orderBy.setDirection(OrderBy.ASC);
			if(sortingDirection.equals("desc"))orderBy.setDirection(OrderBy.DESC);
			
			if(sortingColumnName.equals("orderId"))orderBy.setName("Order_Id");
			if(sortingColumnName.equals("orderNum"))orderBy.setName("Order_Num");
			if(sortingColumnName.equals("status"))orderBy.setName("Status");
			
			
			//search word
			String wordToSearch = aoData.get(5).get("value").get("value").asText();
			String searchClause = null;
			if(wordToSearch != null && !wordToSearch.isEmpty()) {
				
					searchClause = " and (" +
								   " Order_Id LIKE '" + wordToSearch + "%'"+
								   " or Order_Num LIKE '" + wordToSearch + "%'"+
							       ")";				
								
			}
			
			

			//-----------------------------------------------------------------------------------------------------
						
			pageList = orderHandler.fetchOrderInPRODPaginate(pageLength, start, orderBy, searchClause);
			Integer count = pageList.size();	
			
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
	@Path("/pallets/{orderId}")
	public Response fetchPalletteByOrder(@PathParam("orderId") Integer orderId) {
		Set<Pallette> pallettes = null;
		try {
			Order order = orderHandler.read(orderId);
			pallettes = orderHandler.fetchPalletByOrder(order);
		} catch (PersistenceException e) {
			LogUtils.error("Error occured when fetch pallette of " + orderId, e);
		}
		return Response.ok(pallettes).build();
	}

	@POST
	@Path("/downloadTodaySelectedBl")
	@Consumes({ "application/json" })
	public Response downloadTodaySelectedBl(String json) {

		//System.out.println(json);
		ObjectMapper mapper = new ObjectMapper();

		ByteArrayInputStream input = null;
		List<Pallette> selectedPallettes = new ArrayList<Pallette>();

		try {
			JsonNode jsonNode = mapper.readTree(json);

			JsonNode selectedPallettesJson = jsonNode.get("selectedPallettes");

			for (JsonNode palJson : selectedPallettesJson) {
				boolean palSelected = palJson.get("selected").asBoolean();
				if (palSelected) {
					long palletteId = palJson.get("id").asLong();
					Pallette pallette = palletteHandler.read(palletteId);
					selectedPallettes.add(pallette);
				}
			}

			if (selectedPallettes.size() > 0) {

				String adresse = selectedPallettes.get(0).getDestination();

				BonLivraison bl = new BonLivraison();
				int numero = bonLivraisonHandler.getMaxCount();
				int newNumero = numero + 1;
				bl.setNum(newNumero);
				bl.setCreationDate(new Date());
				bl.setStatus(blStatus.NEW);
				bl.setDestination(adresse);
				bonLivraisonHandler.save(bl);

				int totalBookBl = 0;

				Map<Order, Integer> ordersAndQuantitesMap = new HashMap<Order, Integer>();

				for (Pallette pallette : selectedPallettes) {
					pallette.setBlNumber(bl.getNum());
					palletteHandler.update(pallette);

					// get orders and quantities
					Map<String, Object> ordersAndTheirBooksQty = orderHandler.fetchOrderByPAllette(pallette.getId());
					List<Order> orders = (List<Order>) ordersAndTheirBooksQty.get("orders");
					List<Integer> qtyBookOrders = (List<Integer>) ordersAndTheirBooksQty.get("books");

					// orders fusion
					for (int i = 0; i < orders.size(); i++) {
						Order order = orders.get(i);
						int totalBookOrder = qtyBookOrders.get(i);

						boolean found = false;
						for (Order mapOrder : ordersAndQuantitesMap.keySet()) {
							if (mapOrder.getOrderId().intValue() == order.getOrderId().intValue()) {
								int newQty = ordersAndQuantitesMap.get(mapOrder) + totalBookOrder;
								ordersAndQuantitesMap.put(mapOrder, newQty);
								found = true;
								break;
							}
						}

						if (!found) {
							ordersAndQuantitesMap.put(order, totalBookOrder);
						}

					}

				}

				// create order bl
				for (Order mapOrder : ordersAndQuantitesMap.keySet()) {

					int totalBooksOrder = ordersAndQuantitesMap.get(mapOrder);
					totalBookBl += totalBooksOrder;

					OrderBl oderBl = new OrderBl();
					oderBl.setQty(totalBooksOrder);
					oderBl.setBonLivraison(bl);
					bonLivraisonHandler.createOrderBl(oderBl);

					mapOrder.getOrder_Bl().add(oderBl);
					orderHandler.update(mapOrder);
				}

				bl.setQty(totalBookBl);
				bonLivraisonHandler.update(bl);
				
				
				// to delete -----------------------------------------------
				/*
					Order or = null;
					for (Order mapOrder : ordersAndQuantitesMap.keySet()) {
						or = mapOrder;	
					}
				
					for (int i = 1; i < 100; i++) {
						Order o = or;
						o.setOrderId(688 + i);
						ordersAndQuantitesMap.put(o, 200);
					}
				*/	
				// ---------------------------------------------------------
				
				
				if (ordersAndQuantitesMap.size() == 1) {
					input = createBlMonoOrderV2(bl, ordersAndQuantitesMap);
				} 
				if (ordersAndQuantitesMap.size() > 1) {
					input = createBlMultiOrdersV2(bl, ordersAndQuantitesMap);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return Response.ok(input).build();
	}

	private ByteArrayInputStream createBlMultiOrdersV2(BonLivraison bl,	Map<Order, Integer> ordersAndQuantitesMap) {

		try {
			File outputPdfFile = File.createTempFile("bonLivraison_Info", ".pdf");
			File intermediate = File.createTempFile("intermediate", ".pdf");

			File sourcePdfFile = new File(System.getProperty(ConfigurationConstants.BLIVRAISONINT));

			// copy
			FileUtils.copyFile(sourcePdfFile, intermediate);

			// ---------------------------------------------------------------------------------

			// copy arial to temp folder
			File arialFontSource = ResourceUtils.getFile("classpath:ArialUnicode.ttf");
			File arialFontDest = new File(System.getProperty("java.io.tmpdir") + File.separator + "ArialUnicode.ttf");
			FileUtils.copyFile(arialFontSource, arialFontDest);
			
			
			//CalibriBold			
			File calibriBoldFontSource = ResourceUtils.getFile("classpath:CalibriBold.ttf");
			File calibriBoldFontDest = new File(System.getProperty("java.io.tmpdir") + File.separator + "ArialUnicode.ttf");
			FileUtils.copyFile(calibriBoldFontSource, calibriBoldFontDest);

			int numberOfLines = 18;
			int currentPageNumber = 1;
			int rowsCounter = 0;
			
			int totalPages = (int) (ordersAndQuantitesMap.size() / 18);
			if((ordersAndQuantitesMap.size() % 18) > 0 ) totalPages = totalPages + 1;


			// define fonts
			BaseFont arial = BaseFont.createFont(arialFontDest.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			Font arialFont = new Font(arial, 9);

			
			BaseFont calibriBold = BaseFont.createFont(calibriBoldFontDest.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			Font cb9 = new Font(calibriBold, 9);
			cb9.setColor(new BaseColor(103, 111, 121));
			Font cb11 = new Font(calibriBold, 11);
			cb11.setColor(new BaseColor(103, 111, 121));
			
			// new version
			// -------------------------------------------------------------------------------------------
			// -------------------------------------------------------------------------------------------

			// create initial table
			PdfPTable table = createTable(cb9, cb11);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Document doc = new Document();
			PdfSmartCopy copy = new PdfSmartCopy(doc, baos);
			copy.setMergeFields();
			doc.open();
			
			String conditions = "";
			Set<String> conditionsSet = new HashSet<String>();
			for (Order order : ordersAndQuantitesMap.keySet()) {
				Set<Package> orderPackages = order.getOrderPackages();
				String orderCondition = orderPackages.iterator().next().getLabel();
				if(orderCondition != null) {
					conditionsSet.add(orderCondition);
				}					
			}
			
			for (String orderCondition : conditionsSet) {
				orderCondition = orderCondition.trim();
				if(orderCondition != null && !orderCondition.isEmpty()) {
					conditions += "- " + orderCondition + "\n";
				}
			}

			int i = 1;
			for (Order order : ordersAndQuantitesMap.keySet()) {

				int totalBooksOrder = ordersAndQuantitesMap.get(order);
				

				table.addCell(createCell(String.valueOf(i), arialFont));
				table.addCell(createCell(order.getOrderPart().getPart().getTitle(), arialFont));
				table.addCell(createCell(order.getOrderPart().getPart().getIsbn(), arialFont));
				table.addCell(createCell(order.getOrderNum(), arialFont));
				table.addCell(createCell("", arialFont));
				table.addCell(createCell(String.valueOf(totalBooksOrder), arialFont));

				i++;
				rowsCounter++;

				if (rowsCounter == numberOfLines) {

					// change form fields names
					PdfReader sourceReader = new PdfReader(intermediate.getAbsolutePath());
					ByteArrayOutputStream instanceBaos = new ByteArrayOutputStream();
					PdfStamper sourceStamper = new PdfStamper(sourceReader, instanceBaos);

					AcroFields sourceForm = sourceStamper.getAcroFields();

					String cpn = String.valueOf(currentPageNumber);

					sourceForm.renameField("n_bdl", "n_bdl" + cpn);
					sourceForm.renameField("date", "date" + cpn);
					sourceForm.renameField("adresse", "adresse" + cpn);
					sourceForm.renameField("page", "page" + cpn);
					sourceForm.renameField("pos", "pos" + cpn);
					sourceForm.renameField("barcode", "barcode" + cpn);
					sourceForm.renameField("conditions", "conditions" + cpn);
					

					sourceForm.setField("n_bdl" + cpn, bl.getNumBL());
					SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
					String d = format.format(bl.getCreationDate());
					sourceForm.setField("date" + cpn, d);
					sourceForm.setField("adresse" + cpn, bl.getDestination());
					sourceForm.setField("page" + cpn, cpn + "/" + String.valueOf(totalPages));
					sourceForm.setField("barcode" + cpn, "*" +  bl.getNumBL().replace("#", "") + "*");
					sourceForm.setField("conditions" + cpn, conditions);

					// initial position
					List<FieldPosition> positions = sourceForm.getFieldPositions("pos" + cpn);
					Rectangle rect = positions.get(0).position;
					float x0 = rect.getLeft();
					float y0 = rect.getBottom();

					// add infos and table
					PdfContentByte canvas = sourceStamper.getOverContent(1);
					table.completeRow();
					table.writeSelectedRows(0, -1, x0, y0, canvas);

					table = createTable(cb9, cb11);
					currentPageNumber++;
					rowsCounter = 0;

					sourceStamper.setFormFlattening(true);
					sourceStamper.close();
					sourceReader.close();
					copy.addDocument(new PdfReader(instanceBaos.toByteArray()));

				}
			}

			if (table.getRows().size() > 1) {
				
				// change form fields names
				PdfReader sourceReader = new PdfReader(intermediate.getAbsolutePath());
				ByteArrayOutputStream instanceBaos = new ByteArrayOutputStream();
				PdfStamper sourceStamper = new PdfStamper(sourceReader, instanceBaos);

				AcroFields sourceForm = sourceStamper.getAcroFields();

				String cpn = String.valueOf(currentPageNumber);

				sourceForm.renameField("n_bdl", "n_bdl" + cpn);
				sourceForm.renameField("date", "date" + cpn);
				sourceForm.renameField("adresse", "adresse" + cpn);
				sourceForm.renameField("page", "page" + cpn);
				sourceForm.renameField("pos", "pos" + cpn);
				sourceForm.renameField("barcode", "barcode" + cpn);
				sourceForm.renameField("conditions", "conditions" + cpn);
				

				sourceForm.setField("n_bdl" + cpn, bl.getNumBL());
				SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
				String d = format.format(bl.getCreationDate());
				sourceForm.setField("date" + cpn, d);
				sourceForm.setField("adresse" + cpn, bl.getDestination());
				sourceForm.setField("page" + cpn, cpn + "/" + String.valueOf(totalPages));
				sourceForm.setField("barcode" + cpn, "*" +  bl.getNumBL().replace("#", "") + "*");
				sourceForm.setField("conditions" + cpn, conditions);

				// initial position
				List<FieldPosition> positions = sourceForm.getFieldPositions("pos" + cpn);
				Rectangle rect = positions.get(0).position;
				float x0 = rect.getLeft();
				float y0 = rect.getBottom();

				// add infos and table
				PdfContentByte canvas = sourceStamper.getOverContent(1);
				table.completeRow();
				table.writeSelectedRows(0, -1, x0, y0, canvas);

				table = createTable(cb9, cb11);
				currentPageNumber++;
				rowsCounter = 0;

				sourceStamper.setFormFlattening(true);
				sourceStamper.close();
				sourceReader.close();
				copy.addDocument(new PdfReader(instanceBaos.toByteArray()));
				
			}

			// close copy
			doc.close();

			// write final pdf
			Document finalDocument = new Document();
			PdfCopy simpleCopy = new PdfCopy(finalDocument, new FileOutputStream(outputPdfFile));
			finalDocument.open();
			PdfReader finalReader = new PdfReader(baos.toByteArray());
			simpleCopy.addDocument(finalReader);
			finalReader.close();
			simpleCopy.freeReader(finalReader);
			finalDocument.close();

			byte[] out = FileUtils.readFileToByteArray(outputPdfFile);

			ByteArrayInputStream input = new ByteArrayInputStream(out);
			return input;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private ByteArrayInputStream createBlMonoOrderV2(BonLivraison bl, Map<Order, Integer> ordersAndQuantitesMap) {

		try {
			File outputPdfFile = File.createTempFile("bonLivraison_Info", ".pdf");

			File sourcePdfFile = new File(System.getProperty(ConfigurationConstants.BLIVRAISON));

			// copy
			FileUtils.copyFile(sourcePdfFile, outputPdfFile);
			// ---------------------------------------------------------------------------------

			// draw the data table
			FileInputStream output = new FileInputStream(outputPdfFile);
			PdfReader reader = new PdfReader(output);
			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputPdfFile));

			// set form fields text
			AcroFields form = stamper.getAcroFields();
			form.setField("n_bdl", bl.getNumBL());
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			String d = format.format(bl.getCreationDate());
			form.setField("date", d);
			form.setField("adresse", bl.getDestination());
			form.setField("barcode", "*" +  bl.getNumBL().replace("#", "") + "*");

			for (Order order : ordersAndQuantitesMap.keySet()) {

				int totalBooksOrder = ordersAndQuantitesMap.get(order);

				Set<Package> orderPackages = order.getOrderPackages();
				form.setField("Text1", orderPackages.iterator().next().getLabel());
				form.setField("pos1", "1");
				form.setField("titre1", order.getOrderPart().getPart().getTitle());
				form.setField("ean1", order.getOrderPart().getPart().getIsbn());
				form.setField("comm1", order.getOrderNum());
				form.setField("qty1", String.valueOf(totalBooksOrder));

			}

			stamper.setFormFlattening(true);
			stamper.close();
			reader.close();

			byte[] out = FileUtils.readFileToByteArray(outputPdfFile);

			ByteArrayInputStream input = new ByteArrayInputStream(out);
			return input;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private PdfPCell createCell(String text, Font font) {
		PdfPCell cell = new PdfPCell(new Paragraph(text, font));
		
		cell.setPaddingTop(8);
		cell.setPaddingBottom(8);
		
		cell.setBorderColorTop(new BaseColor(220, 223, 227));
		
		cell.setBorderWidthBottom(0);
		cell.setBorderWidthLeft(0);
		cell.setBorderWidthRight(0);
		
		cell.setBorderWidthTop(2);
		
		
		return cell;
	}

	private PdfPTable createTable(Font font, Font font2) {

		PdfPTable table = new PdfPTable(6);
		table.setHeaderRows(1);
		try {
			table.setTotalWidth(new float[] { 30, 255, 80, 80, 40, 40 });
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		table.setLockedWidth(true);

		// headers
		table.addCell(createCell("Pos", font2));
		table.addCell(createCell("Titre", font2));
		table.addCell(createCell("Ean", font2));
		table.addCell(createCell("N° commande", font2));
		table.addCell(createCell("N° fsc", font2));
		table.addCell(createCell("Qty", font2));

		return table;

	}

}
