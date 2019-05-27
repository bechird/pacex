package com.epac.cap.handler;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.OrderBy;
import com.epac.cap.common.PersistenceException;
import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.config.NDispatcher;
import com.epac.cap.functionel.Rip;
import com.epac.cap.functionel.WorkflowEngine;
import com.epac.cap.model.Customer;
import com.epac.cap.model.Job;
import com.epac.cap.model.Order;
import com.epac.cap.model.OrderPart;
import com.epac.cap.model.Package;
import com.epac.cap.model.PackageBook;
import com.epac.cap.model.Pallette;
import com.epac.cap.model.PalletteBook;
import com.epac.cap.model.Part;
import com.epac.cap.model.Preference;
import com.epac.cap.model.WFSDataSupport;
import com.epac.cap.model.WFSLocation;
import com.epac.cap.model.WFSPartWorkflow;
import com.epac.cap.model.WFSProductionStatus;
import com.epac.cap.model.WFSProgress;
import com.epac.cap.om.EsprintToPaceX;
import com.epac.cap.repository.CustomerSearchBean;
import com.epac.cap.repository.JobDAO;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.repository.LookupSearchBean;
import com.epac.cap.repository.OrderDAO;
import com.epac.cap.repository.OrderSearchBean;
import com.epac.cap.repository.PackageBookDao;
import com.epac.cap.repository.PartDAO;
import com.epac.cap.repository.StationDAO;
import com.epac.cap.repository.WFSDataSupportDAO;
import com.epac.cap.service.NotificationService;
import com.epac.cap.sse.beans.Event;
import com.epac.cap.sse.beans.EventTarget;
import com.epac.cap.utils.LogUtils;
import com.epac.om.api.book.Book;
import com.google.common.io.Files;
import com.mycila.event.api.topic.Topics;

/**
 * Interacts with Order data. Uses OrderDAO for entity persistence.
 * 
 * @author walid
 *
 */
@Service
public class OrderHandler {

	@Autowired
	private OrderDAO orderDAO;

	@Autowired
	private PartDAO partDAO;
	
	@Autowired
	private WFSDataSupportDAO dsDAO;

	@Autowired
	private PartHandler partHandler;

	@Autowired
	private StationDAO stationDAO;

	@Autowired
	private JobDAO jobDAO;

	@Autowired
	private LookupDAO lookupDAO;

	@Autowired
	private CustomerHandler customerHandler;

	@Autowired
	private JobHandler jobHandler;

	@Autowired
	private PalletteHandler palletteHandler;
	@Autowired
	private PackageBookHandler packageBookHandler;
	
	@Autowired
	private WFSDataSupportHandler wfsDataSupportHandler;
	
	@Autowired
	private WFSWorkflowHandler wfsWorkflowHandler;
	
	@Autowired
	private WorkflowEngine workflowEngine;
	
	@Autowired
	private NotificationService notificationService;
	
	@Autowired
	private EsprintToPaceX esprintToPaceX;
	
	@Autowired
	PackageBookDao packageBookDao;
	private static Logger logger = Logger.getLogger(OrderHandler.class);

	/**
	 * No arg constructor. This is the preferred constructor.
	 */
	public OrderHandler() {

	}

	/**
	 * Calls the corresponding create method on the OrderDAO.
	 *
	 */
	@Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.REQUIRED)
	public void create(Order bean) throws PersistenceException {
		try {
		
			// get the customer bean
			CustomerSearchBean csb = new CustomerSearchBean();
			if (bean.getCustomer() != null) {
				csb.setEmailExact(bean.getCustomer().getEmail());
				csb.setFirstName(bean.getCustomer().getFirstName());
				csb.setLastName(bean.getCustomer().getLastName());
			}
			List<Customer> customers = customerHandler.readAll(csb);
			if (!customers.isEmpty()) {
				bean.setCustomer(customers.get(0));
			} else {
				bean.setCustomer(null);
			}
			bean.setRecievedDate(new Date());
			if ("System_Auto".equals(bean.getCreatorId())) {
				bean.setSource(Order.OrderSources.AUTO.toString());
			} else if ("System_Esprint".equals(bean.getCreatorId())) {
				bean.setSource(Order.OrderSources.ESPRINT.toString());
			} else if ("System_Pacer".equals(bean.getCreatorId())) {
				bean.setSource(Order.OrderSources.PACER.toString());
			} else {
				bean.setSource(Order.OrderSources.MANUAL.toString());
			}
			if (bean.getCreatedDate() == null) {
				bean.setCreatedDate(new Date());
			}
			Set<OrderPart> tmpOPs = bean.getOrderParts();
			bean.setOrderParts(new HashSet<OrderPart>(0));

			if (!tmpOPs.isEmpty()) {
				// refresh the part bean of this order; case of order created
				// manually from the pacex form & case of order coming esprint
				// (may be also from xml)
				// see if the order has more than one op and so add all

				// OrderPart op = tmpOPs.iterator().next();
				for (OrderPart op : tmpOPs) {
					if (op.getQuantityMax() == null) {
						op.setQuantityMax(op.getQuantity() + jobHandler.getOvers(op.getQuantity()));
					}
					if (op.getQuantityMin() == null) {
						op.setQuantityMin(op.getQuantity() - jobHandler.getUnders(op.getQuantity()));
					}
					// TODO work on the printing hours as well
					// op.setPrintingHours(calculatePrintingHours(op.getQuantityMax(),
					// part.getPagesCount()));
					Part p = null;
					if(op.getPart().getPartNum() != null && !op.getPart().getPartNum().isEmpty()){
						p = partDAO.read(op.getPart().getPartNum());
					}
					if (p != null) {
						op.setPart(p);
					} else {
						String partNumber = partHandler.generatePartNb();
						op.getPart().setPartNum(partNumber);
					}

					op.setPrintingHours(
							jobHandler.calculateJobHoursAndLength(op.getPart(), op.getQuantityMax(), (float) 0.0)[0]);
					bean.getOrderParts().add(op);
				}
			}

			Set<OrderPart> parts = bean.getOrderParts();
			if (parts != null) {
				for (OrderPart orderPart : parts) {
					Part part = orderPart.getPart();
					if (part == null) {
						continue;
					}

					Set<WFSPartWorkflow> workflows = part.getWorkflows();
					if (workflows != null)
						for (WFSPartWorkflow workflow : workflows) {
							workflow.setPartNum(part.getPartNum());
							SortedSet<WFSProgress> progresses = workflow.getProgresses();
							if (progresses != null)
								for (WFSProgress progress : progresses) {
									progress.setPartWorkflow(workflow);
								}

						}
					SortedSet<WFSDataSupport> dataSupports = part.getDataSupports();
					if (dataSupports != null)
						for (WFSDataSupport dataSupport : dataSupports) {
							dataSupport.setPartNumb(part.getPartNum());

							// reset dataSupport into dataLocation beacause with
							// Json, dataLocation does not have proper
							// dataSupport

							Set<WFSLocation> locations = dataSupport.getLocations();
							dataSupport.setLocations(new HashSet<WFSLocation>());
							for (WFSLocation location : locations) {
								dataSupport.addLocation(location);
							}
						}
					try {
						if ("System_Esprint".equals(bean.getCreatorId()) || "System_Auto".equals(bean.getCreatorId())
								|| "System_Pacer".equals(bean.getCreatorId())) {
							partHandler.create(part);
						}
					} catch (Exception e) {
						LogUtils.debug("Failed creating sub-parts for part " + part.getPartNum());
					}

				}
			}

			getOrderDAO().create(bean);
			//getOrderDAO().update(bean);
			//Order order = read(bean.getOrderId());
			//JSONObject jsonOrder = new JSONObject(order);

			Event event=new Event(EventTarget.Order, false, null, bean.getOrderId());
			notificationService.broadcast(event);

			// if order status shows order not processing, and the part has
			// delete afterwards requested, then
			// deactivate the part
			/*if (Order.OrderStatus.COMPLETE.getName().equals(bean.getStatus())
					|| Order.OrderStatus.CANCELLED.getName().equals(bean.getStatus())
					|| Order.OrderStatus.REJECTED.getName().equals(bean.getStatus())) {
				if (Boolean.TRUE.equals(bean.getOrderPart().getPart().getSoftDelete())) {
					bean.getOrderPart().getPart().setActiveFlag(false);
					partDAO.update(bean.getOrderPart().getPart());
				}
			}*/

		} catch (Exception ex) {
			bean.setOrderId(null);

			Event event=new Event(EventTarget.Order, true, ex.getLocalizedMessage(), bean.getOrderNum());
			notificationService.broadcast(event);

			logger.error("Error occurred creating an Order : " + ex.getMessage(), ex);
			throw new PersistenceException(ex);
		}
	}
	
	/**
	 * Calls the corresponding order part update method on the OrderDAO.
	 */
	@Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.SUPPORTS)
	public int getErrorStatusCount() throws PersistenceException {
		try {
			return getOrderDAO().getErrorStatusCount();
		} catch (Exception ex) {
			logger.error("Error occurred getting how many erroneous orders are present : " + ex.getMessage(), ex);
			throw new PersistenceException(ex);
		}
	}

	/**
	 * Calls the corresponding order part update method on the OrderDAO.
	 */
	@Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.REQUIRED)
	public void updateOrderPart(OrderPart bean) throws PersistenceException {
		try {
			getOrderDAO().updateOrderPart(bean);
		} catch (Exception ex) {
			logger.error("Error occurred updating the Order part : " + ex.getMessage(), ex);
			throw new PersistenceException(ex);
		}
	}
	
	/**
	 * Calls the corresponding update method on the OrderDAO.
	 */
	@Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.REQUIRED)
	public void update(Order bean) throws PersistenceException {
		try {
			// update the customer bean in case it has changed
			CustomerSearchBean csb = new CustomerSearchBean();
			csb.setEmailExact(bean.getCustomer() != null ? bean.getCustomer().getEmail() : null);
			List<Customer> customers = customerHandler.readAll(csb);
			if (!customers.isEmpty()) {
				bean.setCustomer(customers.get(0));
			}

			Set<OrderPart> tmpOPs = bean.getOrderParts();
			bean.setOrderParts(new HashSet<OrderPart>(0));

			if (!tmpOPs.isEmpty()) {
				// refresh the part bean of this order; case of order created
				// manually from the pacex form & case of order coming esprint
				// (may be also from xml)
				// see if the order has more than one op and so add all

				// OrderPart op = tmpOPs.iterator().next();
				for (OrderPart op : tmpOPs) {
					if (op.getQuantityMax() == null) {
						op.setQuantityMax(op.getQuantity() + jobHandler.getOvers(op.getQuantity()));
					}
					if (op.getQuantityMin() == null) {
						op.setQuantityMin(op.getQuantity() - jobHandler.getUnders(op.getQuantity()));
					}
					// TODO work on the printing hours as well
					// op.setPrintingHours(calculatePrintingHours(op.getQuantityMax(),
					// part.getPagesCount()));
					Part p = null;
					if (op.getPart().getPartNum() != null && !op.getPart().getPartNum().isEmpty()) {
						p = partDAO.read(op.getPart().getPartNum());
					}
					if (p != null) {
						op.setPart(p);
					} else {
						String partNumber = partHandler.generatePartNb();
						op.getPart().setPartNum(partNumber);
					}

					op.setPrintingHours(
							jobHandler.calculateJobHoursAndLength(op.getPart(), op.getQuantityMax(), (float) 0.0)[0]);
					bean.getOrderParts().add(op);
				}
			}

			Set<OrderPart> parts = bean.getOrderParts();
			if (parts != null) {
				for (OrderPart orderPart : parts) {
					Part part = orderPart.getPart();
					if (part == null) {
						continue;
					}

					Set<WFSPartWorkflow> workflows = part.getWorkflows();
					if (workflows != null)
						for (WFSPartWorkflow workflow : workflows) {
							workflow.setPartNum(part.getPartNum());
							SortedSet<WFSProgress> progresses = workflow.getProgresses();
							if (progresses != null)
								for (WFSProgress progress : progresses) {
									progress.setPartWorkflow(workflow);
								}

						}
					SortedSet<WFSDataSupport> dataSupports = part.getDataSupports();
					if (dataSupports != null){
						for (WFSDataSupport dataSupport : dataSupports) {
							dataSupport.setPartNumb(part.getPartNum());

							// reset dataSupport into dataLocation beacause with
							// Json, dataLocation does not have proper
							// dataSupport

							Set<WFSLocation> locations = dataSupport.getLocations();
							dataSupport.setLocations(new HashSet<WFSLocation>());
							for (WFSLocation location : locations) {
								dataSupport.addLocation(location);
							}
						}
					}
				}
			}

			Order originalOrder = read(bean.getOrderId());
			if ((originalOrder != null) && (!originalOrder.getStatus().equalsIgnoreCase(bean.getStatus()))) {
				Map<String, Object> ssePayload = new HashMap<String, Object>();
				ssePayload.put("orderId", originalOrder.getOrderId());
				ssePayload.put("status", originalOrder.getStatus());
				Event event = new Event(EventTarget.OrderStatus, false, null, ssePayload);
				notificationService.broadcast(event);
				if(bean.getNotes() == null){
					bean.setNotes("");
				}
				//if Esprint order was erroneous and now is not then check if rasters are fine, else we need confirmation to allow status change from error to pending
				if(Order.OrderSources.ESPRINT.getName().equals(bean.getSource()) && 
						Order.OrderStatus.ERROR.getName().equals(originalOrder.getStatus()) && Order.OrderStatus.PENDING.getName().equals(bean.getStatus())){
					//loop through the data-supports and make sure all rasters do exist:
					boolean checkRasterSM = true;
					boolean checkRasterEM = true;
					Preference p = lookupDAO.read("CHECKRASTERSM", Preference.class);
					if(p != null && "false".equals(p.getName())){
						checkRasterSM = false;
					}
					p = lookupDAO.read("CHECKRASTEREM", Preference.class);
					if(p != null && "false".equals(p.getName())){
						checkRasterEM = false;
					}
					boolean allRastersAreFine = true;
					for(OrderPart op : bean.getOrderParts()){
						Set<WFSDataSupport> imposedDss = new HashSet<WFSDataSupport>();
						Set<WFSDataSupport> rasterDss = new HashSet<WFSDataSupport>();
						Part opPart = partDAO.read(op.getPart().getPartNum().endsWith("T") ? op.getPart().getPartNum() : op.getPart().getPartNum() + "T");
						for(WFSDataSupport dss : opPart.getDataSupportsOnProd()){
							if(dss.getName().equals(WFSDataSupport.NAME_IMPOSE) &&
									dss.getDescription().equals("Text") && dss.getDsType().contains(".PDF")){
								if((dss.getDsType().contains("SM") && checkRasterSM) || (dss.getDsType().contains("EM") && checkRasterEM)){
									imposedDss.add(dss);
								}
							}
							if(dss.getName().equals(WFSDataSupport.NAME_RASTER)){
								if((dss.getDsType().contains("SM") && checkRasterSM) || (dss.getDsType().contains("EM") && checkRasterEM)){
									rasterDss.add(dss);
								}
							}
						}
						if(rasterDss.isEmpty() || rasterDss.size() < imposedDss.size()){
							bean.setStatus(Order.OrderStatus.ERROR.getName());
							bean.setNotes(bean.getNotes().concat("Status change failed: Raster files are not all available for part " + op.getPart().getPartNum()) + ". Check rasterisation process and re-run workflow if needed.");
						}else{
							for(WFSDataSupport rs : rasterDss){
								WFSLocation loc = rs.getLocationdByType("Destination");
								if(loc != null && rs.getDsType().contains("EM")){
									File f = new File(loc.getPath());
									if(!f.exists() || !f.isDirectory() || f.list() == null || f.list().length < Rip.rasterEMExtensions.length){
										bean.setNotes(bean.getNotes().concat("Status change failed. " + rs.getDsType() + " Raster files are not all available for part " + op.getPart().getPartNum()) + ". Check rasterisation process and re-run workflow if needed.");
										bean.setStatus(com.epac.cap.model.Order.OrderStatus.ERROR.getName());
									}
								}
								if(loc != null && rs.getDsType().contains("SM")){
									File rasterFolder = new File(loc.getPath());
									File[] rasterFiles = rasterFolder.getParentFile().listFiles(new FileFilter() {
										@Override
										public boolean accept(File file) {
											if(file.getName().contains(Files.getNameWithoutExtension(loc.getPath())))
												return true;
											return false;
										}
									});
									if(rasterFiles == null || rasterFiles.length < Rip.rasterSMExtensions.length){
										bean.setNotes(bean.getNotes().concat("Status change failed. " + rs.getDsType() + " Raster files are not all available for part " + op.getPart().getPartNum()) + ". Check rasterisation process and re-run workflow if needed.");
										bean.setStatus(com.epac.cap.model.Order.OrderStatus.ERROR.getName());
									}
								}
							}
						}
					}
				}
			}

			getOrderDAO().update(bean);

			// update the jobs in case they exist
			JobSearchBean jsb = new JobSearchBean();
			jsb.setOrderId(bean.getOrderId());
			List<Job> orderJobs = jobDAO.readAll(jsb);
			for (Job j : orderJobs) {
				if (!bean.getDueDate().equals(j.getDueDate()) || (bean.getProductionMode() != null
						&& !bean.getProductionMode().equals(j.getProductionMode()))) {
					j.setDueDate(bean.getDueDate());
					j.setProductionMode(bean.getProductionMode());
					jobDAO.update(j);
				}
			}

			// if order status shows order not processing, and the part has
			// delete afterwards requested, then
			// deactivate the part
			for (OrderPart ordp : bean.getOrderParts()) {
				if (Order.OrderStatus.COMPLETE.getName().equals(bean.getStatus())
						|| Order.OrderStatus.CANCELLED.getName().equals(bean.getStatus())
						|| Order.OrderStatus.REJECTED.getName().equals(bean.getStatus())) {
					if (ordp != null && Boolean.TRUE.equals(ordp.getPart().getSoftDelete())) {
						ordp.getPart().setActiveFlag(false);
						partDAO.update(ordp.getPart());
					}
				}
			}

		} catch (Exception ex) {

			Event event = new Event(EventTarget.OrderStatus, true, ex.getLocalizedMessage(), bean.getOrderNum());
			notificationService.broadcast(event);

			logger.error("Error occurred updating a Order : " + ex.getMessage(), ex);
			throw new PersistenceException(ex);
		}
	}

	/**
	 * Accepting Orders; includes generating the necessary jobs.
	 */
	@Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.REQUIRED)
	public void acceptOrders(int[] orderIds, String executingUserId) throws PersistenceException {
		try {
			// for each order, update the status to accepted and create the necessary jobs
			for (Integer i : orderIds) {
				Order order = orderDAO.read(i);
				if(order != null && !Order.OrderStatus.ACCEPTED.getName().equals(order.getStatus())){
					order.setStatus(Order.OrderStatus.ACCEPTED.getName());
					order.setLastUpdateDate(new Date());
					order.setLastUpdateId(executingUserId);
					orderDAO.update(order);
					//String[] updateOrderStatus = {order.getOrderId()+"", order.getStatus()};
					//broadcastStatus(updateOrderStatus);

					// now create the jobs using the default stations
					for(OrderPart op : order.getOrderParts()){
						Part topPart = op.getPart();
						//List<Object> objects = null;
						for(String childId : topPart.getChildren()){
							/*Part part = partDAO.read(childId);
							objects = new ArrayList<Object>();
							objects.add(order);
							objects.add(topPart);
							objects.add(part);
							objects.add(executingUserId);
							NDispatcher.getDispatcher().publish(Topics.topic("cap/event/order/accepted"), objects);*/
							jobHandler.createJobs(order, topPart, partDAO.read(childId), executingUserId);
						}
						jobHandler.createJobs(order, topPart, topPart, executingUserId);
						/*objects = new ArrayList<Object>();
						objects.add(order);
						objects.add(topPart);
						objects.add(topPart);
						objects.add(executingUserId);
						NDispatcher.getDispatcher().publish(Topics.topic("cap/event/order/accepted"), objects);*/
					}
					
					Map<String, Object> ssePayload=new HashMap<String, Object>();
					ssePayload.put("orderId", order.getOrderId());
					ssePayload.put("status", order.getStatus());
					Event event=new Event(EventTarget.OrderStatus, false, null, ssePayload);
					notificationService.broadcast(event);
				}
			}
			// now run the workflows
			Set<Part> theParts = new HashSet<Part>();
			for (Integer i : orderIds) {
				Order order = orderDAO.read(i);
				if(order != null && !Order.OrderSources.ESPRINT.getName().equals(order.getSource())){
					for(OrderPart op : order.getOrderParts()){
						theParts.add(op.getPart());
					}
				}
			}
			for(Part pa : theParts){
				for(String childId : pa.getChildren()){
					Part childPart = partDAO.read(childId);
					if(childPart != null && !Part.PartsCategory.DUSTJACKET.toString().equals(childPart.getCategory().getId()) &&
							!Part.PartsCategory.ENDSHEET.toString().equals(childPart.getCategory().getId())){
						WFSDataSupport ds = childPart.getDataSupportOnProdByName(WFSDataSupport.NAME_DOWNLOAD);
						if(ds != null){
							NDispatcher.getDispatcher().publish(Topics.topic("cap/events/part/accepted"), childPart);
						}
					}
				}
				if(pa.getChildren().isEmpty()){
					WFSDataSupport ds = pa.getDataSupportOnProdByName(WFSDataSupport.NAME_DOWNLOAD);
					if(ds != null){
						NDispatcher.getDispatcher().publish(Topics.topic("cap/events/part/accepted"), pa);
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error occurred accepting Orders : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}

	/** 
	 * Calls the corresponding delete method on the OrderDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public boolean delete(Order bean) throws PersistenceException {
		try {
			boolean result = getOrderDAO().delete(bean);
			if(result){
				Event event=new Event(EventTarget.Order, false, null, bean.getOrderId());
				notificationService.broadcast(event);
			}
			return result;
		} catch (Exception ex) {
			logger.error("Error occurred deleting a Order with id '" + (bean == null ? null : bean.getOrderId())
					+ "' : " + ex.getMessage(), ex);
			Event event=new Event(EventTarget.OrderStatus, true, ex.getLocalizedMessage(), bean.getOrderNum());
			notificationService.broadcast(event);
			throw new PersistenceException(ex);
		}
	}

	/**
	 * A convenience method which calls readAll(OrderSearchBean) with a null
	 * search bean.
	 *
	 * @see #readAll(OrderSearchBean)
	 */
	@Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
	public List<Order> readAll() throws PersistenceException {
		return this.readAll(null);
	}

	/**
	 * Calls the corresponding readAll method on the OrderDAO.
	 */
	@Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
	public List<Order> readAll(OrderSearchBean searchBean) throws PersistenceException {
		try {
			return getOrderDAO().readAll(searchBean);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a list of Orders : " + ex.getMessage(), ex);
			throw new PersistenceException(ex);
		}
	}

	/**
	 * Calls the corresponding read method on the OrderDAO.
	 */
	@Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
	public Order read(Integer orderId) throws PersistenceException {
		try {
			return getOrderDAO().read(orderId);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a Order with id '" + orderId + "' : " + ex.getMessage(), ex);
			throw new PersistenceException(ex);
		}
	}
	@Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
	public Order readByOrderNum(String orderNum) throws PersistenceException {
		try {
			return getOrderDAO().readByOrderNum(orderNum);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a Order with orderNum '" + orderNum + "' : " + ex.getMessage(), ex);
			throw new PersistenceException(ex);
		}
	}


	/**
	 * @return the OrderDAO
	 */
	public OrderDAO getOrderDAO() {
		return orderDAO;
	}

	/**
	 * @param dao
	 *            the OrderDAO to set
	 */
	public void setOrderDAO(OrderDAO dao) {
		this.orderDAO = dao;
	}

	/**
	 * @return the customerHandler
	 */
	public CustomerHandler getCustomerHandler() {
		return customerHandler;
	}

	/**
	 * @param customerHandler
	 *            the customerHandler to set
	 */
	public void setCustomerHandler(CustomerHandler customerHandler) {
		this.customerHandler = customerHandler;
	}

	/**
	 * @return the partDAO
	 */
	public PartDAO getPartDAO() {
		return partDAO;
	}

	/**
	 * @param partDAO
	 *            the partDAO to set
	 */
	public void setPartDAO(PartDAO partDAO) {
		this.partDAO = partDAO;
	}

	/**
	 * @return the stationDAO
	 */
	public StationDAO getStationDAO() {
		return stationDAO;
	}

	/**
	 * @param stationDAO
	 *            the stationDAO to set
	 */
	public void setStationDAO(StationDAO stationDAO) {
		this.stationDAO = stationDAO;
	}

	/**
	 * @return the lookupDAO
	 */
	public LookupDAO getLookupDAO() {
		return lookupDAO;
	}

	/**
	 * @param lookupDAO
	 *            the lookupDAO to set
	 */
	public void setLookupDAO(LookupDAO lookupDAO) {
		this.lookupDAO = lookupDAO;
	}

	/**
	 * @return the jobDAO
	 */
	public JobDAO getJobDAO() {
		return jobDAO;
	}

	/**
	 * @param jobDAO
	 *            the jobDAO to set
	 */
	public void setJobDAO(JobDAO jobDAO) {
		this.jobDAO = jobDAO;
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

	public NotificationService getNotificationService() {
		return notificationService;
	}

	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
	public List<Order> fetchOrderInFInishing(){
		return orderDAO.fetchOrderFinishing();
	}
	@Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
	public List<Order> fetchOrderFinishingOfInterforum(String interforumSiren){
		return orderDAO.fetchOrderFinishingOfInterforum(interforumSiren);

	}
	public  int calcQtyBookInOrder(Pallette pallette,Order order){
		int totalBook = 0;

		List<PalletteBook> palletteBooks = pallette.getBooks();

		for(   com.epac.cap.model.Package orderPackage :order.getOrderPackages()){
			for(Package pack: orderPackage.getPackages()){
				for(PackageBook pcbs :pack.getPcbs()){
					for(PalletteBook palBook : palletteBooks){
						PackageBook pckBook = palBook.getPackageBook();
						if(pckBook.getPackagePartId().equals(pcbs.getPackagePartId())){
							int qtyInPcb = pckBook.getDepthQty()*pckBook.getHeightQty()*pckBook.getWidthQty();
							totalBook+= qtyInPcb*palBook.getQuantity();
						}
					}
				}

			}
		}

		return totalBook;
	}
	public  Map<String,Object> fetchOrderByPAllette(long palletteId){
		Pallette pallette;
		Map<String,Object> result = new HashMap<>();
		try {
			pallette = palletteHandler.read(palletteId);

			List<Order> orders = new ArrayList<>();
			List<Integer> qtyBookorders = new ArrayList<>();
			if(pallette != null){
				List<PalletteBook> palletteBooks = pallette.getBooks();
				for(PalletteBook palletB: palletteBooks){
					PackageBook pcb =  palletB.getPackageBook();
					long pckBookId = pcb.getPackagePartId();
					Integer orderId = packageBookHandler.fetchOrder(pckBookId);
					if(orderId != null){
						Order order = read(orderId);
						int qty = calcQtyBookInOrder(pallette,order);
						if(orders.indexOf(order) == -1){
							orders.add(order);
							qtyBookorders.add(qty);

						}
					}
				}
			}
			result.put("orders", orders);
			result.put("books", qtyBookorders);
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public List<Order> fetchOrderInPallet(){
		return orderDAO.fetchOrderInPROD();
	}
	
	public List<Order> fetchOrderInPRODPaginate(Integer pageLength, Integer offset, OrderBy orderBy, String searchClause){
		return orderDAO.fetchOrderInPRODPaginate(pageLength, offset, orderBy, searchClause);
	}
	
	public Set<Pallette> fetchPalletByOrder(Order order){
		Set<Pallette> result = new HashSet<>();
		
			Set<Package> pakages = order.getOrderPackages();
			for(Package package_ : pakages){
				Set<Package> pakagesChild = package_.getPackages();
				for(Package packChild : pakagesChild){
					Set<PackageBook> pcbs = packChild.getPcbs();
					for(PackageBook pcb : pcbs){
						List<Pallette> pallettes = packageBookDao.fetchPalletteByPcb(pcb.getPackagePartId());
						result.addAll(pallettes);
					}
			}
		}
		
		return result;
	}
	
	
	@Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.REQUIRED)
	public void rerunWorkFlow(String partNum, Order order, String source){
		try {
			Part part = partHandler.read(partNum);
			
			if(part == null){
				return;
			}
		
		if(Order.OrderSources.ESPRINT.toString().equals(source)){
			Part textBean = partHandler.getSubPart(part, Part.PartsCategory.TEXT.getName());
			Part coverBean = partHandler.getSubPart(part, Part.PartsCategory.COVER.getName());
			
			String filename = textBean.getFileName();
			
			if(filename == null)
				filename = coverBean.getFileName();
			
			if(filename == null){
				LogUtils.error("File name is null, can't get Esprint bookId for part "+part.getPartNum());
				return;
			}
			String bookId = filename.substring(0, filename.indexOf('.'));
			LogUtils.info("Update files for bookId: "+bookId);
			Map<String, Map<String, Object>> files = esprintToPaceX.getBook(bookId, null);
			
			SortedSet<WFSDataSupport> dataSupports = esprintToPaceX.bookSupportToDataSupport(files, part, order, bookId);
			LogUtils.info("Esprint book has ("+dataSupports.size()+") DataSupport entries");
			if(dataSupports.isEmpty())
				return;
			
			
			SortedSet<WFSDataSupport> textDs = textBean.getDataSupports();
			SortedSet<WFSDataSupport> coverDs= coverBean.getDataSupports();
		
			WFSProductionStatus obsoltete = lookupDAO.read(WFSProductionStatus.statuses.OBSOLETE.getName(), WFSProductionStatus.class);
			
			for (WFSDataSupport ds : textDs) {
				ds.setProductionStatus(obsoltete);
				dsDAO.update(ds);
			}
			
			for (WFSDataSupport ds : coverDs) {
				ds.setProductionStatus(obsoltete);
				dsDAO.update(ds);
			}
				
			for(WFSDataSupport ds: dataSupports){
				ds.setPartNumb("Text".equals(ds.getDescription())? textBean.getPartNum():coverBean.getPartNum());
				dsDAO.save(ds);
			}

			partDAO.update(textBean);
			partDAO.update(coverBean);
			
		}else if (part.getSubParts().isEmpty()) {
			if (part.getPartWorkFlowOnProd() != null) {

				// set the older workflow along with its data supports to obsolete
				//WFSPartWorkflow oldPartWorkflow = part.getPartWorkFlowOnProd();
				//if (oldPartWorkflow != null){
					//oldPartWorkflow.setWfStatus(WFSProductionStatus.statuses.OBSOLETE.getName());
					//wfsWorkflowHandler.updatePartWorkflow(oldPartWorkflow);
					for(WFSPartWorkflow pw : part.getWorkflows()){
						if(WFSProductionStatus.statuses.ONPROD.getName().equals(pw.getWfStatus())){
							pw.setWfStatus(WFSProductionStatus.statuses.OBSOLETE.getName());
							pw.setLastUpdateDate(new Date());
							pw.setLastUpdateId(part.getCreatorId());
							wfsWorkflowHandler.updatePartWorkflow(pw);
						}
					}
					for(WFSDataSupport dsIter : part.getDataSupports()){
						if (!dsIter.getName().equalsIgnoreCase("Download") && dsIter.getProductionStatus() != null &&
								!WFSProductionStatus.statuses.OBSOLETE.getName().equals(dsIter.getProductionStatus().getId())){
							dsIter.setProductionStatus(lookupDAO.read(WFSProductionStatus.statuses.OBSOLETE.getName(),WFSProductionStatus.class ));
							dsIter.setLastUpdateDate(new Date());
							dsIter.setLastUpdateId(part.getCreatorId());
							wfsDataSupportHandler.update(dsIter);
						}
					}
					partDAO.update(part);
				//}
				NDispatcher.getDispatcher().publish(Topics.topic("cap/events/part/accepted"), part);

			}
		} else{
			Part textBean = partHandler.getSubPart(part, Part.PartsCategory.TEXT.getName());
			if(textBean != null){
				//WFSPartWorkflow oldPartWorkflow = textBean.getPartWorkFlowOnProd();
				//if (oldPartWorkflow != null){
					//oldPartWorkflow.setWfStatus(WFSProductionStatus.statuses.OBSOLETE.getName());
					//wfsWorkflowHandler.updatePartWorkflow(oldPartWorkflow);
					for(WFSPartWorkflow pw : textBean.getWorkflows()){
						if(WFSProductionStatus.statuses.ONPROD.getName().equals(pw.getWfStatus())){
							pw.setWfStatus(WFSProductionStatus.statuses.OBSOLETE.getName());
							pw.setLastUpdateDate(new Date());
							pw.setLastUpdateId(textBean.getCreatorId());
							wfsWorkflowHandler.updatePartWorkflow(pw);
						}
					}
					for(WFSDataSupport dsIter : textBean.getDataSupports()){
						if (!dsIter.getName().equalsIgnoreCase("Download") && dsIter.getProductionStatus() != null &&
								!WFSProductionStatus.statuses.OBSOLETE.getName().equals(dsIter.getProductionStatus().getId())){
							dsIter.setProductionStatus(lookupDAO.read(WFSProductionStatus.statuses.OBSOLETE.getName(),WFSProductionStatus.class ));
							dsIter.setLastUpdateDate(new Date());
							dsIter.setLastUpdateId(textBean.getCreatorId());
							wfsDataSupportHandler.update(dsIter);
						}
					}
					partDAO.update(textBean);
				//}
				NDispatcher.getDispatcher().publish(Topics.topic("cap/events/part/accepted"), textBean);
			}
			Part coverBean = partHandler.getSubPart(part, Part.PartsCategory.COVER.getName());
			
			if(coverBean != null){
				//WFSPartWorkflow oldPartWorkflow = coverBean.getPartWorkFlowOnProd();
				//if (oldPartWorkflow != null){
					//oldPartWorkflow.setWfStatus(WFSProductionStatus.statuses.OBSOLETE.getName());
					//wfsWorkflowHandler.updatePartWorkflow(oldPartWorkflow);
					for(WFSPartWorkflow pw : coverBean.getWorkflows()){
						if(WFSProductionStatus.statuses.ONPROD.getName().equals(pw.getWfStatus())){
							pw.setWfStatus(WFSProductionStatus.statuses.OBSOLETE.getName());
							pw.setLastUpdateDate(new Date());
							pw.setLastUpdateId(coverBean.getCreatorId());
							wfsWorkflowHandler.updatePartWorkflow(pw);
						}
					}
					for(WFSDataSupport dsIter : coverBean.getDataSupports()){
						if (!dsIter.getName().equalsIgnoreCase("Download") && dsIter.getProductionStatus() != null &&
								!WFSProductionStatus.statuses.OBSOLETE.getName().equals(dsIter.getProductionStatus().getId())){
							dsIter.setProductionStatus(lookupDAO.read(WFSProductionStatus.statuses.OBSOLETE.getName(),WFSProductionStatus.class ));
							dsIter.setLastUpdateDate(new Date());
							dsIter.setLastUpdateId(coverBean.getCreatorId());
							wfsDataSupportHandler.update(dsIter);
						}
					}
					partDAO.update(coverBean);
				//}
				NDispatcher.getDispatcher().publish(Topics.topic("cap/events/part/accepted"), coverBean);
			}
		}
		} catch (PersistenceException e) {
			LogUtils.error("", e);
		}
	
}


	@Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.REQUIRED)
	public void rerunAllWorkFlows(Integer orderId) {
		try {
			Order order = orderDAO.read(orderId);
			LogUtils.debug(" Start Rerun All workflows for Order [" + orderId + "]");
			order.getOrderParts()
					.forEach((OrderPart op) -> rerunWorkFlow(op.getPart().getPartNum(), order, order.getSource()));

		} catch (Exception e) {
			LogUtils.error("Rerun All workflows for Order [" + orderId + "]: ", e);
		}

	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Integer getCount() {
		return orderDAO.getCount();
	}
	
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Order> fullSearch(String word, Integer maxResult, Integer offset) {	
		return orderDAO.fullSearch(word, maxResult, offset);		
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Order> searchIsbnAndQuantity(String isbn, String quantity, OrderSearchBean searchBean) {	
		return orderDAO.searchIsbnAndQuantity(isbn, quantity, searchBean);		
	}
	

}