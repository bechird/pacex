package com.epac.cap.functionel;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.epac.cap.common.OrderBy;
import com.epac.cap.common.PersistenceException;
import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.config.NDispatcher;
import com.epac.cap.handler.PartHandler;
import com.epac.cap.handler.WFSActionHandler;
import com.epac.cap.handler.WFSDataSupportHandler;
import com.epac.cap.handler.WFSLocationHandler;
import com.epac.cap.handler.WFSProgressHandler;
import com.epac.cap.handler.WFSWorkflowHandler;
import com.epac.cap.model.Order;
import com.epac.cap.model.Order.OrderStatus;
import com.epac.cap.model.PNLTemplate;
import com.epac.cap.model.PNLTemplateLine;
import com.epac.cap.model.PaperTypeMedia;
import com.epac.cap.model.Part;
import com.epac.cap.model.Part.BindingTypes;
import com.epac.cap.model.Part.PartsCategory;
import com.epac.cap.model.Preference;
import com.epac.cap.model.SubPart;
import com.epac.cap.model.WFSAction;
import com.epac.cap.model.WFSDataSupport;
import com.epac.cap.model.WFSLocation;
import com.epac.cap.model.WFSPartWorkflow;
import com.epac.cap.model.WFSProductionStatus;
import com.epac.cap.model.WFSProgress;
import com.epac.cap.model.WFSSequence;
import com.epac.cap.model.WFSStatus.ProgressStatus;
import com.epac.cap.model.WFSWorkflow;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.repository.LookupSearchBean;
import com.epac.cap.handler.OrderHandler;
import com.epac.cap.repository.OrderSearchBean;
import com.epac.cap.utils.Format;
import com.epac.cap.utils.LogUtils;
import com.google.common.io.Files;
import com.mycila.event.api.Event;
import com.mycila.event.api.Subscriber;
import com.mycila.event.api.topic.TopicMatcher;
import com.mycila.event.api.topic.Topics;

@SuppressWarnings({"rawtypes", "unchecked"})
@Controller
public class WorkflowEngine implements IWorkflow {

	
	private static ExecutorService executor;

	@Autowired
	private WFSProgressHandler wfsProgressHandler;

	@Autowired
	private PartHandler partHandler;

	@Autowired
	private WFSLocationHandler wfsLocationHandler;

	@Autowired
	private WFSDataSupportHandler wfsDataSupportHandler;

	@Autowired
	private WFSWorkflowHandler wfsWorkflowHandler;
	
	@Autowired
	private WFSActionHandler wfsActionHandler;
	
	@Autowired
	private LookupDAO lookupDAO;
	
	@Autowired
	private OrderHandler orderHandler;
	
	@Autowired
	private Imposition imposition;
	
	public static final String UNIT_US = "US";
	public static final String UNIT_FR = "FR";
	
	public static final String PNL_INCLUDE                            = "PNL_INCLUDE";
	public static final String PNL_EXCLUDED                           = "PNL_EXCLUDED";
	public static final String PNL_TMPL_ID							  = "PNL_TMPL_ID";
	public static final String PNL_LOCATION 						  = "PNL_LOCATION";
	public static final String PNL_LANGUAGE                           = "PNL_LANGUAGE";
	public static final String PNL_PAGE_NUMBER						  = "PNL_PAGE_NUMBER";
	public static final String PNL_PRINTING_NUMBER					  = "PNL_PRINTING_NUMBER";
	public static final String PNL_HORIZONTAL_MARGIN				  = "PNL_HORIZONTAL_MARGIN";
	public static final String PNL_VERTICAL_MARGIN					  = "PNL_VERTICAL_MARGIN";
	public static final String PNL_LINE_SPACING					      = "PNL_LINE_SPACING";
	public static final String PNL_FONT_TYPE					      = "PNL_FONT_TYPE";
	public static final String PNL_FONT_SIZE					      = "PNL_FONT_SIZE";
	public static final String PNL_FONT_BOLD					      = "PNL_FONT_BOLD";
	public static final String PNL_FONT_ITALIC					      = "PNL_FONT_ITALIC";
	public static final String PNL_TMPL_PRINTING_NUMBER				  = "com.epac.cap.pnl.template.number";
	public static final String PNL_DATE					      		  = "com.epac.cap.pnl.template.date";
	public static final String PNL_MONTH					      	  = "com.epac.cap.pnl.template.month";
	public static final String PNL_YEAR					      		  = "com.epac.cap.pnl.template.year";
	public static final String PNL_DATE_FORMAT					      = "PNL_DATE_FORMAT";
	public static final String _EN					      		 	  = "_EN";
	public static final String _FR					      		 	  = "_FR";
	public static final String _ES					      		 	  = "_ES";
	public static final String _CA					      		 	  = "_CA";
	public static final String _MA					      		 	  = "_MA";
	public static final String DATE_FMT					      		  = "dd MMMM, yyyy";
	public static final String MONTH_FMT					      	  = "MMMM";
	public static final String YEAR_FMT					      		  = "yyyy";
	
	private Preference unitValue;

	public WorkflowEngine() {
		executor = Executors.newCachedThreadPool();
		subscribe();
		try {
			StartWorkflow();
		} catch (PersistenceException e1) {
			
			e1.printStackTrace();
		}
	}
	
	public void StartWorkflow() throws PersistenceException {
		
		LogUtils.start();

		TopicMatcher downloadMatcher = Topics.only("cap/events/download/done");
		NDispatcher.getDispatcher().subscribe(downloadMatcher, String.class, new Subscriber<String>() {
			public void onEvent(Event<String> event) throws Exception {
				//Runnable task = new Runnable() {
					//@Override
					//public void run() {
						try {
							
							//Part currentPart = partHandler.read(event.getSource());
							LogUtils.debug("Received: " + event.toString() + " download method is done");
							
						} catch (Exception e) {
							
							e.printStackTrace();
						}
					//}
				//};
				//executor.execute(task);
			}
		});

		TopicMatcher copyMatcher = Topics.only("cap/events/copy/done");
		NDispatcher.getDispatcher().subscribe(copyMatcher, String.class, new Subscriber<String>() {
			public void onEvent(Event<String> event) throws Exception {
				// Runnable task = new Runnable() {
				// @Override
				// public void run() {
				try {
					LogUtils.debug("Received: " + event.toString() + " copy method is done");

					Part currentPart = partHandler.read(event.getSource());
					if (currentPart != null) {
						WFSPartWorkflow partWorkflowOnProd = currentPart.getPartWorkFlowOnProd();
						if (partWorkflowOnProd != null) {
							WFSProgress actualProgress = partWorkflowOnProd.getProgressByActionName("copy");
							actualProgress.setStatus(ProgressStatus.DONE.getName());
							wfsProgressHandler.update(actualProgress);
							WFSAction nextAction = getNextAction(partWorkflowOnProd);
							if (nextAction != null)
								fireAction(nextAction, partHandler.read(event.getSource()));
							else {
								partWorkflowOnProd.setIsReady(true);
								wfsWorkflowHandler.updatePartWorkflow(partWorkflowOnProd);
							}

						}
					}
				} catch (Exception e) {
					
					e.printStackTrace();
				}
				// }
				// };
				// executor.execute(task);
				// task.run();
			}
		});

		TopicMatcher moveMatcher = Topics.only("cap/events/move/done");
		NDispatcher.getDispatcher().subscribe(moveMatcher, String.class, new Subscriber<String>() {
			public void onEvent(Event<String> event) throws Exception {
//				Runnable task = new Runnable() {
//					@Override
//					public void run() {
						try {
							LogUtils.debug("Received: " + event.toString() + " move method is done");
							Part currentPart = partHandler.read(event.getSource());
							if(currentPart != null){
								WFSPartWorkflow partWorkflowOnProd = currentPart.getPartWorkFlowOnProd();
								if(partWorkflowOnProd != null){
									WFSProgress actualProgress = partWorkflowOnProd.getProgressByActionName("move");
									actualProgress.setStatus(ProgressStatus.DONE.getName());
									wfsProgressHandler.update(actualProgress);
									WFSAction nextAction = getNextAction(partWorkflowOnProd);
									if (nextAction != null)
										fireAction(nextAction, partHandler.read(event.getSource()));
									else {
										partWorkflowOnProd.setIsReady(true);
										wfsWorkflowHandler.updatePartWorkflow(partWorkflowOnProd);
									}
								}
							}
						} catch (Exception e) {
							
							e.printStackTrace();
						}
//					}
//				};
//				executor.execute(task);
			}
		});
		
		TopicMatcher imposeMatcher = Topics.only("cap/events/imposition/done");
		NDispatcher.getDispatcher().subscribe(imposeMatcher, Part.class, new Subscriber<Part>() {
			public void onEvent(Event<Part> event) throws Exception {
						try {
							
						
							LogUtils.info("Received: " + event.toString() + " imposition method is done for part "+event.getSource().getPartNum());
							Part currentPart = event.getSource();
							if(currentPart != null){
								WFSPartWorkflow partWorkflowOnProd = currentPart.getPartWorkFlowOnProd();
								if(partWorkflowOnProd != null){
									WFSProgress actualProgress = partWorkflowOnProd.getProgressByActionName("impose");
									if (!actualProgress.getStatus().toString().equalsIgnoreCase(ProgressStatus.DONE.toString())){
										actualProgress.setStatus(ProgressStatus.DONE.getName());
										wfsProgressHandler.update(actualProgress);
										
										WFSAction nextAction = getNextAction(partWorkflowOnProd);
										if (nextAction != null)
											fireAction(nextAction, currentPart);
										else {
											partWorkflowOnProd.setIsReady(true);
											wfsWorkflowHandler.updatePartWorkflow(partWorkflowOnProd);
										}
									}
								}
							}
						} catch (Exception e) {
							LogUtils.error("Error processing imposition/done event", e);
						}
			}
		});
		
		TopicMatcher imposeFailMatcher = Topics.only("cap/events/imposition/fail");
		NDispatcher.getDispatcher().subscribe(imposeFailMatcher, Part.class, new Subscriber<Part>() {
			public void onEvent(Event<Part> event) throws Exception {
						try {
							LogUtils.info("Received: " + event.toString() + " imposition method failed ");
							Part currentPart = event.getSource();
							if(currentPart != null){
								WFSPartWorkflow partWorkflowOnProd = currentPart.getPartWorkFlowOnProd();
								if(partWorkflowOnProd != null){
									WFSProgress actualProgress = partWorkflowOnProd.getProgressByActionName("impose");
									if (actualProgress != null && !actualProgress.getStatus().toString().equalsIgnoreCase(ProgressStatus.ERROR.toString())){
										actualProgress.setStatus(ProgressStatus.ERROR.getName());
										wfsProgressHandler.update(actualProgress);
									}
								}
							}
						} catch (Exception e) {
							LogUtils.error("Error processing imposition/fail event", e);
						}
			}
		});
	
		TopicMatcher ripMatcher = Topics.only("cap/events/ripping/done");
		NDispatcher.getDispatcher().subscribe(ripMatcher, List.class, new Subscriber<List<String>>() {
			public void onEvent(Event<List<String>> event) throws Exception {
						try {
							
							List<String> params = event.getSource();
							
							String partNum = params.get(0);
							String pattern = params.get(1);
							String raster  = params.get(2);
							
							LogUtils.info("Received: " + event.toString() + " ripping method is done");
							Part currentPart = partHandler.read(partNum);
							if(currentPart != null){
								Set<WFSDataSupport> dataSupports = currentPart.getDataSupportsOnProd();
								for(WFSDataSupport ds: dataSupports){
									if(pattern.equals(ds.getDsType()) && ds.getName().equals(WFSDataSupport.NAME_RASTER)){
										WFSLocation location = ds.getLocationdByType(WFSLocation.DESTINATION);
										if(location != null && (location.getPath() == null || location.getPath().isEmpty())){
											location.setLastUpdateDate(new Date());
											location.setFileName(Files.getNameWithoutExtension(raster));
											location.setPath(raster);
											wfsLocationHandler.update(location);
											break;
										}
									}
								}
								
								WFSPartWorkflow partWorkflowOnProd = currentPart.getPartWorkFlowOnProd();
								if(partWorkflowOnProd != null){
									WFSProgress actualProgress = partWorkflowOnProd.getProgressByActionName("rip");
									actualProgress.setStatus(ProgressStatus.DONE.getName());
									wfsProgressHandler.update(actualProgress);
								
									WFSAction nextAction = getNextAction(partWorkflowOnProd);
									if (nextAction != null)
										fireAction(nextAction, currentPart);
									else {
										partWorkflowOnProd.setIsReady(true);
										wfsWorkflowHandler.updatePartWorkflow(partWorkflowOnProd);
									}
								}
							}
						} catch (Exception e) {
							LogUtils.error("Error occured while processing ripping/done event", e);
						}
			}
		});
		
		TopicMatcher ripErrorMatcher = Topics.only("cap/events/ripping/error");
		NDispatcher.getDispatcher().subscribe(ripErrorMatcher, String.class, new Subscriber<String>() {
			public void onEvent(Event<String> event) throws Exception {
						try {
							LogUtils.info("Received: " + event.toString() + " ripping method is erroneous");
							Part currentPart = partHandler.read(event.getSource());
							if(currentPart != null){
								WFSPartWorkflow partWorkflowOnProd = currentPart.getPartWorkFlowOnProd();
								if(partWorkflowOnProd != null){
									WFSProgress actualProgress = partWorkflowOnProd.getProgressByActionName("rip");
									actualProgress.setStatus(ProgressStatus.ERROR.getName());
									wfsProgressHandler.update(actualProgress);
									
								}
							}
						} catch (Exception e) {
							LogUtils.error("Error occured while processing ripping/done event", e);
						}
			}
		});
		
		LogUtils.end();

	}

	@Override
	public boolean handler(Object parameter) {
		LogUtils.start();
		
		//boolean isAutomatic = false;
		boolean isPopline = false;
		
		
		List<Object> parameters = (List<Object>) parameter;
		Part part = (Part) parameters.get(0);
		if(((String)parameters.get(1)).equalsIgnoreCase("Popline")){
			isPopline = true;
		}
			
		//if (part.getCategory().getId().equalsIgnoreCase(Part.PartsCategory.TEXT.toString()))
		//	NDispatcher.getDispatcher().publish(Topics.topic("cap/events/ChooseWidth"), part.getWidth());

		try {
			Part persistedPart = partHandler.read(part.getPartNum());

			WFSAction wsfAction = null;
			if((persistedPart.getPartWorkFlowOnProd() == null) || (!persistedPart.isPartWorkFlowOnProdReady())){
				if (persistedPart.getPartWorkFlowOnProd() == null) {
					// Set the Corresponding Workflow 
					WFSWorkflow wfsWorkflow = null;
					if(isPopline && (persistedPart.getCategory().getId().equalsIgnoreCase(PartsCategory.TEXT.getName()))){
						wfsWorkflow = wfsWorkflowHandler.getWorkflow(2);
					} else if(persistedPart.getCategory().getId().equalsIgnoreCase(PartsCategory.TEXT.getName())){
						wfsWorkflow = wfsWorkflowHandler.getWorkflow(1);
					} else {
						wfsWorkflow = wfsWorkflowHandler.getWorkflow(3);
					}
					WFSPartWorkflow wfsPartWorkflow = new WFSPartWorkflow();
					wfsPartWorkflow.setWorkflow(wfsWorkflow);
					wfsPartWorkflow.setPartNum(part.getPartNum());
					wfsPartWorkflow.setWfStatus(WFSProductionStatus.statuses.ONPROD.getName());
					wfsPartWorkflow.setCreatedDate(new Date());
					wfsPartWorkflow.setCreatorId(part.getCreatorId());
					wfsPartWorkflow.setIsReady(false);
					if (isPopline) 
						wfsPartWorkflow.setRollWidth((Float)parameters.get(2));
					else if (parameters.size() == 3) 
						wfsPartWorkflow.setRollWidth((Float)parameters.get(2));
					else wfsPartWorkflow.setRollWidth(checkRollWidthCompatibility(part, Float.parseFloat(System.getProperty(ConfigurationConstants.DEFAULT_PAPER_WIDTH))));
					
					// set the older workflow along with its data supports to obsolete
					WFSPartWorkflow oldPartWorkflow = persistedPart.getPartWorkFlowOnProd();
					if (oldPartWorkflow != null){
						oldPartWorkflow.setWfStatus(WFSProductionStatus.statuses.OBSOLETE.getName());
						oldPartWorkflow.setLastUpdateId(part.getCreatorId());
						oldPartWorkflow.setLastUpdateDate(new Date());
						wfsWorkflowHandler.updatePartWorkflow(oldPartWorkflow);
						for(WFSDataSupport dsIter : persistedPart.getDataSupports()){
							if (!dsIter.getName().equalsIgnoreCase("Download") && dsIter.getProductionStatus() != null &&
									!WFSProductionStatus.statuses.OBSOLETE.getName().equals(dsIter.getProductionStatus().getId())){
								dsIter.setProductionStatus(lookupDAO.read(WFSProductionStatus.statuses.OBSOLETE.getName(),WFSProductionStatus.class ));
								dsIter.setLastUpdateDate(new Date());
								dsIter.setLastUpdateId(part.getCreatorId());
								wfsDataSupportHandler.update(dsIter);
							}
						}
					}
					
					
					wfsWorkflowHandler.savePartWorkflow(wfsPartWorkflow);
					part.getWorkflows().add(wfsPartWorkflow);
					
					Set<WFSSequence> wfsSequences = wfsWorkflow.getSequences();
					//Here create the progresses
					wfsSequences.forEach((sequence)->{
						WFSProgress progress = new WFSProgress();
						progress.setCreatedDate(new Date());
						progress.setCreatorId(part.getCreatorId());
						progress.setStatus(ProgressStatus.PENDING.getName());
						progress.setPartWorkflow(wfsPartWorkflow);
						progress.setSequence(sequence);
						
						wfsProgressHandler.save(progress);
						wfsPartWorkflow.addProgress(progress);
					});
					
					partHandler.update(part); 
					wsfAction = getNextAction(wfsPartWorkflow);
				}
				//if (persistedPart.getPartWorkFlowOnProd() != null && !persistedPart.isPartWorkFlowOnProdReady())
				//	wsfAction = getNextAction(persistedPart.getPartWorkFlowOnProd());
				
				if (wsfAction != null )
					fireAction(wsfAction, part);
			}

		} catch (PersistenceException e) {
			LogUtils.error("Error occured", e);
		}
		
		LogUtils.end();
		return true;
	}
	
	public void handleInitialWorkflow(Object parameter){
		
		LogUtils.start();
		String filePath = "";
		boolean isAutomatic = false;
		Part part = new Part();
		
		List<Object> parameters = (List<Object>) parameter;
		if (parameters.size() == 2) {
			part = (Part) parameters.get(0);
			filePath = (String) parameters.get(1);
		} else part = (Part) parameters.get(0);
		
		try {
			Part persistedPart = partHandler.read(part.getPartNum());
			if(persistedPart.getCreatorId().equalsIgnoreCase("system_auto"))
				isAutomatic = true;
			
			//partHandler.update(persistedPart);
			part = partHandler.read(part.getPartNum());
			
			if (isAutomatic){
				WFSAction wsfAction = wfsActionHandler.getAction(1);
				part.setFilePath(filePath);
				fireAction(wsfAction, part);
			}
			
		} catch (PersistenceException e) {
			
			e.printStackTrace();
		}
		LogUtils.end();
	}

	@Override
	public boolean TryFire() {
	
		return false;
	}

	@Override
	public void subscribe() {
		
		//Subscriber over the part creation Event
		//TODO: add the event when the part is updated
		TopicMatcher partDoneMatcher = Topics.only("cap/events/part/done");
		NDispatcher.getDispatcher().subscribe(partDoneMatcher, List.class, new Subscriber<List>() {
			public void onEvent(Event<List> event) throws Exception {
				LogUtils.debug("Received: " + event.toString() + " calling the download Workflow ");
				try {
					handleInitialWorkflow(event.getSource());
				} catch (Exception e) {
					LogUtils.error("Error occured while processing part creation event", e);
				}
			}
		});
		
		TopicMatcher partAcceptedMatcher = Topics.only("cap/events/part/accepted");
		NDispatcher.getDispatcher().subscribe(partAcceptedMatcher, Part.class, new Subscriber<Part>() {
			public void onEvent(Event<Part> event) throws Exception {
				LogUtils.info("Received: " + event.toString() + " calling the accepted part Workflow ");
				Runnable task = new Runnable() {
					@Override
					public void run() {
						try {
							List<Object> parameters = new ArrayList<Object>();
							parameters.add(event.getSource());
							parameters.add("PlowFolder");
							handler(parameters);
						} catch (Exception e) {
							LogUtils.error("Error occured while processing part acceptence event", e);
						}
					}
				};
				executor.execute(task);
			}
		});
		
		TopicMatcher partScheduledMatcher = Topics.only("cap/events/part/scheduled");
		NDispatcher.getDispatcher().subscribe(partScheduledMatcher, List.class, new Subscriber<List>() {
			public void onEvent(Event<List> event) throws Exception {
				LogUtils.info("Received: " + event.toString() + " calling the scheduled part Workflow ");
				Runnable task = new Runnable() {
					@Override
					public void run() {
						try {
							List<Object> parameters = new ArrayList<Object>();
							parameters.add(event.getSource().get(0));
							parameters.add("PlowFolder");
							parameters.add(event.getSource().get(1));
							handler(parameters);
						} catch (Exception e) {
							LogUtils.error("Error occured while processing part scheduling event", e);
						}
					}
				};
				executor.execute(task);
			}
		});
		
		TopicMatcher poplineMatcher = Topics.only("cap/events/part/popline");
		NDispatcher.getDispatcher().subscribe(poplineMatcher, List.class, new Subscriber<List>() {
			public void onEvent( Event<List> event) throws Exception {
				LogUtils.debug("Received: " + event.toString() + " calling the popline imposition Workflow ");
				Runnable task = new Runnable() {
					@Override
					public void run() {
						try {
							List<Object> parameters = new ArrayList<Object>();
							parameters.add(event.getSource().get(0)); // Part
							parameters.add("Popline");
							parameters.add(event.getSource().get(1)); // RollWidth
							handler(parameters);
						} catch (Exception e) {
							
							e.printStackTrace();
						}
					}
				};
				executor.execute(task);
			}
		});
		
	}

	@Override
	public void unsubscribe() {
	}

	private void fireAction(WFSAction wfsAction, Part part) throws PersistenceException {
		String actionName = wfsAction.getName().toLowerCase();
		
		
		LogUtils.info("Action fired: "+actionName + " for Part "+part.getPartNum());
		String parentPartNumb = (part.getTopParts().isEmpty()) ? part.getPartNum() : part.getPartNum().substring(0, part.getPartNum().length()-1);
		
		if(this.getUnitValue() == null){
			this.setUnitValue(getLookupDAO().read("UNITSYSTEM", Preference.class));
			if(this.getUnitValue() == null){
				this.setUnitValue(new Preference());
				this.getUnitValue().setName(UNIT_US);
			}
		}
		switch (actionName) {
		case "download": {
			List<Object> downParameters = new ArrayList<Object>();

			// WFSDataSupportSearchBean sb = new WFSDataSupportSearchBean();
			// sb.setPartNum(part.getPartNum());
			// sb.setDsName("Download");
			// sb.setProductionStatusId(WFSProductionStatus.statuses.ONPROD.toString());
			// List<WFSDataSupport> partDSs = wfsDataSupportHandler.readAll(sb);

			// if(!partDSs.isEmpty()){
			downParameters.add(part.getFilePath());
			downParameters.add(
					partHandler.generatePath(part.getIsbn(), parentPartNumb, part.getCategory().getName(), "Original")
							+ Files.getNameWithoutExtension(part.getFilePath()) + "_" + new Date().getTime() + ".pdf");
			// }else{
			// downParameters.add("");
			// downParameters.add("");
			// }


			//Add a dataSupport and Location for the new File uploaded
			WFSDataSupport wfsDataSupport = new WFSDataSupport();
			WFSLocation wfsDataSrcLocation = new WFSLocation();
			
			
			
			wfsDataSupport.setName(WFSDataSupport.NAME_DOWNLOAD);
			wfsDataSupport.setDescription(part.getCategory().getName());
			wfsDataSupport.setPartNumb(part.getPartNum());
			wfsDataSupport.setCreatedDate(new Date());
			wfsDataSupport.setCreatorId(part.getCreatorId());
			wfsDataSupport.setProductionStatus(lookupDAO.read(WFSProductionStatus.statuses.ONPROD.getName(),WFSProductionStatus.class ));

			wfsDataSrcLocation.setLocationType(WFSLocation.DESTINATION);
			wfsDataSrcLocation.setPath((String) downParameters.get(1));
			wfsDataSrcLocation.setCreatedDate(new Date());
			wfsDataSrcLocation.setCreatorId(part.getCreatorId());
			
			wfsDataSupport.addLocation(wfsDataSrcLocation);
			wfsDataSupportHandler.save(wfsDataSupport);

			downParameters.add(part.getPartNum());
			
			NDispatcher.getDispatcher().publish(Topics.topic("cap/events/download"), downParameters);
		}
			break;
		case "copy": {
			List<Object> parameters = new ArrayList<Object>();
			String originalFilePath = part.getDataSupportOnProdByName(WFSDataSupport.NAME_DOWNLOAD).getLocationdByType(WFSLocation.DESTINATION)
					.getPath();
			LogUtils.debug("Original File is " + originalFilePath);

			WFSDataSupport oldDataSupport = part.getDataSupportOnProdByName("Copy");

			if (oldDataSupport != null) {
				oldDataSupport.setProductionStatus(lookupDAO.read(WFSProductionStatus.statuses.OBSOLETE.getName(),WFSProductionStatus.class ));
				oldDataSupport.setLastUpdateDate(new Date());
				oldDataSupport.setLastUpdateId(part.getCreatorId());
				wfsDataSupportHandler.update(oldDataSupport);
			}

				parameters.add(originalFilePath);
				LogUtils.debug("Source File is " + parameters.get(0));
				parameters.add(
						partHandler.generatePath(part.getIsbn(), parentPartNumb, part.getCategory().getName(), "Copy")
								+ Files.getNameWithoutExtension(originalFilePath) + ".pdf");
				LogUtils.debug("Destination File is " + parameters.get(1));

				WFSDataSupport copiedSupport = new WFSDataSupport();
				WFSLocation copiedDataLocation = new WFSLocation();
				copiedSupport.setCreatedDate(new Date());
				copiedSupport.setCreatorId(part.getCreatorId());
				copiedSupport.setDescription(part.getCategory().getName());
				copiedSupport.setName("Copy");
				copiedSupport.setPartNumb(part.getPartNum());
				copiedSupport.setProductionStatus(
						lookupDAO.read(WFSProductionStatus.statuses.ONPROD.getName(), WFSProductionStatus.class));
				
		
				copiedDataLocation.setCreatedDate(new Date());
				copiedDataLocation.setCreatorId(part.getCreatorId());
				copiedDataLocation.setPath((String) parameters.get(1));
				copiedDataLocation.setLocationType("Destination");
				
				copiedSupport.addLocation(copiedDataLocation);
				wfsDataSupportHandler.save(copiedSupport);

				WFSProgress actualProgress = part.getPartWorkFlowOnProd().getProgressByActionName("copy");
				actualProgress.setDataSupport(copiedSupport);
				wfsProgressHandler.update(actualProgress);

				copiedSupport.setProgressId(actualProgress.getProgressId());
				wfsDataSupportHandler.update(copiedSupport);

				part.getDataSupports().add(copiedSupport);
				partHandler.update(part);

				parameters.add(part.getPartNum());
				NDispatcher.getDispatcher().publish(Topics.topic("cap/events/copy"), parameters);
			//}
		}
			break;
		case "impose": {
			
			if(!Part.PartsCategory.TEXT.getName().equals(part.getCategory().getId()))
				return;
			
			Set<SubPart> children = part.getSubParts();
			// try to get parent part so we impose cover & text; but for cover, only if not Loose and not Case Bind
			if(children == null || children.isEmpty()){
				if(!part.getTopParts().isEmpty()){
					SubPart parent = part.getTopParts().iterator().next();
					part = partHandler.read(parent.getId().getTopPartNum());
					children = part.getSubParts();
				}
			}
			
			List<Part> childParts = new ArrayList<>();
			for(SubPart sub: children){
				childParts.add(partHandler.read(sub.getId().getSubPartNum()));
			}
			// at least add the text part itself
			if(childParts.isEmpty()){
				childParts.add(part);
			}
			
			String textPath = null;
			String coverPath = null;
			
			Part textPart = null;
			Part coverPart= null;
			float  rollWidth = 0;
			for(Part child: childParts){
				WFSDataSupport ds = child.getDataSupportOnProdByName("Download");
				if(ds != null){
					String path = ds.getLocationdByType("Destination").getPath();
					if(Part.PartsCategory.TEXT.getName().equals(child.getCategory().getId())){
						textPath = path;
						textPart = child;
						rollWidth = textPart.getPartWorkFlowOnProd().getRollWidth();
						
						rollWidth = checkRollWidthCompatibility(part, rollWidth);
					}else if(Part.PartsCategory.COVER.getName().equals(child.getCategory().getId())){
						coverPath = path;
						coverPart = child;
					}
				}
			}
			
			String output = partHandler.getImposedDirectory(part.getIsbn(), parentPartNumb);
			
			ImpositionRequest impositionRequest = new ImpositionRequest();
			
			impositionRequest.setText(textPath);
			impositionRequest.setCover(coverPath);
			impositionRequest.setTextOutput(output + File.separator + textPart.getCategory().getName());
			impositionRequest.setCoverOutput(coverPart != null ? (output  + File.separator + coverPart.getCategory().getName()) : "");
			impositionRequest.setBookId(textPart.getPartNum().concat("R").concat(String.valueOf(rollWidth).replace(".", "")));
			impositionRequest.setBarcode(part.getIsbn());
			impositionRequest.setBookWidth(UNIT_US.equals(unitValue.getName()) ? Format.inch2mm(part.getWidth()): part.getWidth());
			impositionRequest.setBookHeight(UNIT_US.equals(unitValue.getName()) ? Format.inch2mm(part.getLength()): part.getLength());
			impositionRequest.setBookThickness(UNIT_US.equals(unitValue.getName()) ? Format.inch2mm(part.getThickness()): part.getThickness());
			if(part.getPaperType() != null)impositionRequest.setPaperThickness((UNIT_US.equals(unitValue.getName()) ? Format.inch2mm(part.getPaperType().getThickness()): part.getPaperType().getThickness()) * 1000);
			impositionRequest.setRollWidth(UNIT_US.equals(unitValue.getName()) ? Format.inch2mm(rollWidth) : rollWidth);
			
			if(part.getBindingType().getId().equals(BindingTypes.LOOSELEAF.toString())){
				List<String> hunkelerLines = new ArrayList<>();
				hunkelerLines.add("PL");
				impositionRequest.setHunkelerLines(hunkelerLines);
			}else{
				List<String> hunkelerLines = new ArrayList<>();
				hunkelerLines.add("FF");
				hunkelerLines.add("PF");
				hunkelerLines.add("PB");
				impositionRequest.setHunkelerLines(hunkelerLines);
			}
			// Add the PNL if needed:
			PNLInfo pnlInfo = this.resolvePNLInfo(part, null, null, false, null);
			if(!pnlInfo.getPnlLines().isEmpty()){
				impositionRequest.setPnlInformation(pnlInfo);
			}
			ImpositionResponse response = imposition.submit(impositionRequest);
			
			if(response.hasError()){
				NDispatcher.getDispatcher().publish(Topics.topic("cap/events/imposition/fail"), textPart);
				if(coverPart != null){
					NDispatcher.getDispatcher().publish(Topics.topic("cap/events/imposition/fail"), coverPart);
				}
				return;
			}
			
			String [] types = {
					ImpositionResponse.FFSM_PDF ,
					ImpositionResponse.FFEM_PDF ,
					ImpositionResponse.PFSM_PDF ,
					ImpositionResponse.PFEM_PDF ,
					ImpositionResponse.PLSM_PDF ,
					ImpositionResponse.PLEM_PDF ,
					ImpositionResponse.PBSM_PDF ,
					ImpositionResponse.PBEM_PDF ,
					
					ImpositionResponse.FFSM_JDF ,
					ImpositionResponse.FFEM_JDF ,
					ImpositionResponse.PFSM_JDF ,
					ImpositionResponse.PFEM_JDF ,
					ImpositionResponse.PLSM_JDF ,
					ImpositionResponse.PLEM_JDF ,
					ImpositionResponse.PBSM_JDF ,
					ImpositionResponse.PBEM_JDF 
			};
			
			for (int i = 0; i < types.length; i++) {
				
				String file = response.get(types[i]);
				if(file == null)
					continue;
				String[] listOfFiles = file.split(";");
				for(String path : listOfFiles){
					if(!path.isEmpty()){
						WFSDataSupport ds = new WFSDataSupport();
						ds.setDsType(types[i]);
						ds.setPartNumb(textPart.getPartNum());
						ds.setCreatedDate(new Date());
						ds.setCreatorId(textPart.getCreatorId());
						ds.setDescription(Part.PartsCategory.TEXT.getName());
						ds.setProductionStatus(lookupDAO.read(WFSProductionStatus.statuses.ONPROD.getName(), WFSProductionStatus.class));
						ds.setName(WFSDataSupport.NAME_IMPOSE);
		
						WFSLocation location = new WFSLocation();
						location.setCreatedDate(new Date());
						location.setCreatorId(textPart.getCreatorId());
						location.setPath(path);
						location.setFileName(FilenameUtils.getName(path));
						location.setLocationType(WFSLocation.DESTINATION);
		
						ds.addLocation(location);
						wfsDataSupportHandler.save(ds);
						textPart.addDataSupports(ds);
					}
				}
			}
			
			partHandler.update(textPart);
			
			if(coverPart != null){
				String file = response.get(ImpositionResponse.COVER);
				WFSDataSupport ds = new WFSDataSupport();
				ds.setDsType(ImpositionResponse.COVER);
				ds.setPartNumb(coverPart.getPartNum());
				ds.setCreatedDate(new Date());
				ds.setCreatorId(coverPart.getCreatorId());
				ds.setDescription(Part.PartsCategory.COVER.getName());
				ds.setProductionStatus(lookupDAO.read(WFSProductionStatus.statuses.ONPROD.getName(), WFSProductionStatus.class));
				ds.setName(WFSDataSupport.NAME_IMPOSE);

				WFSLocation location = new WFSLocation();
				location.setCreatedDate(new Date());
				location.setCreatorId(textPart.getCreatorId());
				location.setPath(file);
				location.setFileName(FilenameUtils.getName(file));
				location.setLocationType(WFSLocation.DESTINATION);

				ds.addLocation(location);
			
				wfsDataSupportHandler.save(ds);
			
				coverPart.addDataSupports(ds);
				partHandler.update(coverPart);
			}
			
			partHandler.update(textPart);
			
			NDispatcher.getDispatcher().publish(Topics.topic("cap/events/imposition/done"), textPart);
			//NDispatcher.getDispatcher().publish(Topics.topic("cap/events/imposition/done"), coverPart);
			
		}
		
			break;
			
		case "rip": {
			String [] types = {
					"FFSM" ,
					"FFEM" ,
					"PFSM" ,
					"PFEM" ,
					"PLSM" ,
					"PLEM" ,
					"PBSM" ,
					"PBEM" };
			WFSDataSupport oldDataSupport = part.getDataSupportOnProdByName(WFSDataSupport.NAME_RASTER);
			int nbTimes = 1;
			if (oldDataSupport == null) {
				for (int i = 0; i < types.length; i++) {
					boolean typeUsed = false;
					for(WFSDataSupport ds : part.getDataSupportsOnProd()){
						if(ds.getDsType() != null && ds.getDsType().contains(types[i]) && WFSDataSupport.NAME_IMPOSE.equals(ds.getName())){
							typeUsed = true;
							break;
						}
					}
					if(typeUsed){
						if(types[i].contains("SM") || types[i].contains("PBEM") || types[i].contains("PLEM")){
							nbTimes = 1;
						}else{
							nbTimes = 4;
						}
						for(int j = 0; j < nbTimes; j++){
							WFSDataSupport rippedSupport = new WFSDataSupport();
							WFSLocation rasterDataLocation = new WFSLocation();
							
							rippedSupport.setDsType(types[i]);
							rippedSupport.setPartNumb(part.getPartNum());
							rippedSupport.setCreatedDate(new Date());
							rippedSupport.setCreatorId(part.getCreatorId());
							rippedSupport.setDescription(part.getCategory().getName());
							rippedSupport.setProductionStatus(lookupDAO.read(WFSProductionStatus.statuses.ONPROD.getName(), WFSProductionStatus.class));
							rippedSupport.setName(WFSDataSupport.NAME_RASTER);
							
							rasterDataLocation.setCreatedDate(new Date());
							rasterDataLocation.setCreatorId(part.getCreatorId());
							//rasterDataLocation.setPath();
							//rasterDataLocation.setFileName();
							rasterDataLocation.setLocationType(WFSLocation.DESTINATION);
							
							rippedSupport.addLocation(rasterDataLocation);
							wfsDataSupportHandler.save(rippedSupport);
							part.addDataSupports(rippedSupport);
						}
					}
				}
				partHandler.update(part);
				NDispatcher.getDispatcher().publish(Topics.topic("cap/events/rip"), part);
			}
		}
			break;
		case "move": {
			List<Object> parameters = new ArrayList<Object>();
			
			String originalFilePath = part.getDataSupportOnProdByName("Download").getLocationdByType("Destination").getPath();

			parameters.add(partHandler.generatePath(part.getIsbn(), parentPartNumb, part.getCategory().getName(), "Original") 
						+ Files.getNameWithoutExtension(originalFilePath)+".pdf");
			parameters.add(partHandler.generatePath(part.getIsbn(), parentPartNumb, part.getCategory().getName(), "Move") 
						+ Files.getNameWithoutExtension(originalFilePath)+".pdf");
			
			WFSDataSupport movedSupport = new WFSDataSupport();
			WFSLocation movedDataLocation = new WFSLocation();
			movedSupport.setCreatedDate(new Date());
			movedSupport.setCreatorId(part.getCreatorId());
			movedSupport.setDescription(part.getCategory().getName());
			movedSupport.setName("Move");
			movedSupport.setPartNumb(part.getPartNum());
			movedSupport.setProductionStatus(lookupDAO.read(WFSProductionStatus.statuses.ONPROD.getName(),WFSProductionStatus.class ));
			
			
			movedDataLocation.setCreatedDate(new Date());
			movedDataLocation.setCreatorId(part.getCreatorId());
			movedDataLocation.setPath((String) parameters.get(1));
			movedDataLocation.setLocationType("Destination");
			movedSupport.addLocation(movedDataLocation);
			
			wfsDataSupportHandler.save(movedSupport);
			
			WFSProgress actualProgress = part.getPartWorkFlowOnProd().getProgressByActionName("move");
			actualProgress.setDataSupport(movedSupport);
			wfsProgressHandler.update(actualProgress);
			
			movedSupport.setProgressId(actualProgress.getProgressId());
			wfsDataSupportHandler.update(movedSupport);
			
			part.getDataSupports().add(movedSupport);
			partHandler.update(part);
			
			parameters.add(part.getPartNum());
			NDispatcher.getDispatcher().publish(Topics.topic("cap/events/move"), parameters);
		} break;

		default:
			break;
		}
	}

	public float checkRollWidthCompatibility(Part part, float rollWidth) {
		float originalRollWidth = rollWidth;
		float result = rollWidth;
		float twoUpMargins;
		
		Preference unitValue = lookupDAO.read("UNITSYSTEM", Preference.class);
		if(unitValue == null){
			unitValue = new Preference();
			unitValue.setName(UNIT_US);
		}
		
		if(UNIT_US.equals(unitValue.getName())){
			rollWidth = Format.inch2mm(rollWidth);
		}
		
		Preference lookupValue = lookupDAO.read("TWOUPMARGINS"	, Preference.class);
		if(UNIT_US.equals(unitValue.getName())){
			twoUpMargins 	= (lookupValue != null) ? Format.inch2mm(Float.parseFloat(lookupValue.getName())) : 24;
		}else{
			twoUpMargins 	= (lookupValue != null) ? Float.parseFloat(lookupValue.getName()) : 24;
		}
		
		float partWidth = part.getWidth();
		if(UNIT_US.equals(unitValue.getName())){
			partWidth = Format.inch2mm(partWidth);
		}
		
		float minRollWidth = 2 * partWidth + twoUpMargins;
		
		if(rollWidth < minRollWidth){
			for(PaperTypeMedia ptm : part.getPaperType().getMedias()){
				float mediaWidth;
				if(UNIT_US.equals(unitValue.getName())){
					mediaWidth=Format.inch2mm(ptm.getRollWidth());
				}else{
					mediaWidth=ptm.getRollWidth();
				}
				if(mediaWidth > rollWidth){
					result = ptm.getRollWidth();
					if(UNIT_US.equals(unitValue.getName())){
						rollWidth = Format.inch2mm(ptm.getRollWidth());
					}else{
						rollWidth = ptm.getRollWidth();
					}
					break;
				}
			}
			if(rollWidth < minRollWidth){
				LogUtils.debug("Issue: unable to find paper width that can fit for this part: " + part.getPartNum());
				part.setNotes("Issue: unable to find paper width that can fit for this part: " + part.getPartNum());
			}
		}
		if(result != originalRollWidth){
			LogUtils.debug("Imposition paper width that should be used for part " + part.getPartNum() + " is: " + result);
		}
		return result;
	}

	private WFSAction getNextAction(WFSPartWorkflow partWorkflow) {
		WFSAction action = null;
		if(partWorkflow == null){
			return action;
		}
		//TODO when have multi EM sheets, does this method need review...
		
		Set<WFSProgress> tempProgresses = partWorkflow.getProgresses();
		//tempProgresses.sort(Comparator.comparing(WFSSequence::getRanking));
		for (WFSProgress wfsProgress : tempProgresses) {
			if (wfsProgress.getStatus().equalsIgnoreCase(ProgressStatus.PENDING.getName())) {
				action = wfsProgress.getSequence().getWfAction();
				break;
			}
		}

		return action;
	}
	
	/**
	 * This method looks for the value of the preference and considers the settings level overriding:
	 * So if the preference is related to an order/part it means it has the highest priority and is returned, 
	 * next is the client, and finally the global preference....
	 * It also takes into account the language and chooses the corresponding preference for that specific language if exists, else uses the default value
	 * @return the preference value if found, null otherwise
	 * @throws PersistenceException 
	 */
	private String findPreferenceByLevel(String preference, Part part, String pnlLanguage) throws PersistenceException{
		String result = null;
		String clientId = null;
		// find all preferences related to this prefix preference order by creation date desc
		if(preference != null){
			String idWithLang = pnlLanguage != null ? preference.concat(pnlLanguage) : preference;
			String pref_EN = preference.concat(_EN), pref_FR = preference.concat(_FR), pref_ES = preference.concat(_ES), pref_MA = preference.concat(_MA), pref_CA = preference.concat(_CA);
			LookupSearchBean lsb = new LookupSearchBean();
			lsb.setIdPrefix(preference);
			lsb.setPrefSubject("PNL");
			lsb.setOrderByList(Arrays.asList(new OrderBy("id", "asc")));
			List<Preference> foundPrefs = lookupDAO.readAll(null, lsb, Preference.class);
			if(!foundPrefs.isEmpty()){
				// find the client for this part
				if(part != null){
					OrderSearchBean osb = new OrderSearchBean();
					osb.setPartNumbers(new HashSet( Arrays.asList(part.getPartNum())));
					List<Order> concernedOrders = orderHandler.readAll(osb);
					for(Order o : concernedOrders){
						if(!(OrderStatus.COMPLETE.getName().equals(o.getStatus()) || OrderStatus.DELIVERED.getName().equals(o.getStatus()) || OrderStatus.ERROR.getName().equals(o.getStatus()))){
							clientId = o.getClientId();
							break;
						}
					}
					if(clientId == null && !concernedOrders.isEmpty()){
						clientId = concernedOrders.get(0).getClientId();
					}
				}
				if(part != null){// look for match by the part
					if(pnlLanguage != null){
						for(Preference p : foundPrefs){
							if(part.getPartNum().equals(p.getPartNum()) && p.getId().startsWith(idWithLang)){
								result = p.getName();
								break;
							}
						}
						if(result == null){
							for(Preference p : foundPrefs){
								if(part.getPartNum().equals(p.getPartNum())){//also it should not be for another language
									if(!p.getId().startsWith(pref_EN) && !p.getId().startsWith(pref_FR) && !p.getId().startsWith(pref_CA) && !p.getId().startsWith(pref_MA) && !p.getId().startsWith(pref_ES)){
										result = p.getName();
										break;
									}
								}
							}
						}
					}else{
						for(Preference p : foundPrefs){
							if(part.getPartNum().equals(p.getPartNum())){
								result = p.getName();
								break;
							}
						}
					}
				}
				if(result == null && clientId != null){// look for match by the client
					if(pnlLanguage != null){
						for(Preference p : foundPrefs){
							if(clientId.equals(p.getClientId()) && p.getId().startsWith(idWithLang) 
									&& (p.getPartNum() == null || p.getPartNum().isEmpty())){
								result = p.getName();
								break;
							}
						}
						if(result == null){
							for(Preference p : foundPrefs){
								if(clientId.equals(p.getClientId()) && (p.getPartNum() == null || p.getPartNum().isEmpty())){
									if(!p.getId().startsWith(pref_EN) && !p.getId().startsWith(pref_FR) && !p.getId().startsWith(pref_CA) && !p.getId().startsWith(pref_MA) && !p.getId().startsWith(pref_ES)){
										result = p.getName();
										break;
									}
								}
							}
						}
					}else{
						for(Preference p : foundPrefs){
							if(clientId.equals(p.getClientId()) && (p.getPartNum() == null || p.getPartNum().isEmpty())){
								result = p.getName();
								break;
							}
						}
					}
				}
				if(result == null){ //look for match by the facility
					if(pnlLanguage != null){
						for(Preference p : foundPrefs){
							if(p.getId().startsWith(idWithLang) && (p.getPartNum() == null || p.getPartNum().isEmpty()) &&
									(p.getClientId() == null || p.getClientId().isEmpty() || "undefined".equals(p.getClientId()))){
								result = p.getName();
								break;
							}
						}
						if(result == null){
							for(Preference p : foundPrefs){
								if( (p.getPartNum() == null || p.getPartNum().isEmpty()) &&
										(p.getClientId() == null || p.getClientId().isEmpty() || "undefined".equals(p.getClientId()))){
									if(!p.getId().startsWith(pref_EN) && !p.getId().startsWith(pref_FR) && !p.getId().startsWith(pref_CA) && !p.getId().startsWith(pref_MA) && !p.getId().startsWith(pref_ES)){
										result = p.getName();
										break;
									}
								}
							}
						}
					}else{
						for(Preference p : foundPrefs){
							if( (p.getPartNum() == null || p.getPartNum().isEmpty()) &&
									(p.getClientId() == null || p.getClientId().isEmpty() || "undefined".equals(p.getClientId()))){
								result = p.getName();
								break;
							}
						}
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * This method gets the PNL Template and resolves the variables within the line text of it, and the styling as well;
	 * then it finds the rest of the PNL info and constructs the PNL Information bean that gets sent to the Imposer Service
	 * It is also used for previewing the PNL: in such case it will be a partial resolution: some of the
	 * variables might not get resolved so just return some static values just for the preview
	 * @throws PersistenceException
	 */
	public PNLInfo resolvePNLInfo(Part part, Float pageWidth, Float pageHeight, Boolean preview, String templateIdForPreview) throws PersistenceException{
		if(this.getUnitValue() == null){
			this.setUnitValue(getLookupDAO().read("UNITSYSTEM", Preference.class));
			if(this.getUnitValue() == null){
				this.setUnitValue(new Preference());
				this.getUnitValue().setName(UNIT_US);
			}
		}
		PNLInfo pnlInformation = new PNLInfo();
		pnlInformation.setNotes("");
		PNLTemplate pnlTemplate = null;
		Preference pnlIncludeValue = lookupDAO.read(PNL_INCLUDE, Preference.class);
		if(preview || (pnlIncludeValue != null && "true".equalsIgnoreCase(pnlIncludeValue.getName()))){
			// See if PNL is not needed (no PNL will be added, since correct PNL info is already in the book that does not need to be updated.):
			String pnlNotNeededValue = findPreferenceByLevel(getPreferenceNamingConvention(PNL_EXCLUDED), part, null);
			if(pnlNotNeededValue == null || "false".equalsIgnoreCase(pnlNotNeededValue)){
				// find the PNL template to use:
				String pnlTemplateIdValue = templateIdForPreview;
				if(preview == null || !preview){
					pnlTemplateIdValue = findPreferenceByLevel(getPreferenceNamingConvention(PNL_TMPL_ID), part, null);
				}
				if(pnlTemplateIdValue != null){
					pnlTemplate = lookupDAO.read(pnlTemplateIdValue, PNLTemplate.class);
					if(pnlTemplate != null){
						// we continue in processing the PNL
					}else{
						LogUtils.debug("PNL will not be included because PNL Template is not found");
						pnlInformation.setNotes(pnlInformation.getNotes().concat("PNL will not be included because PNL Template is not found; "));
						return pnlInformation;
					}
				}else{
					LogUtils.debug("PNL will not be included because PNL Template is not defined");
					pnlInformation.setNotes(pnlInformation.getNotes().concat("PNL will not be included because PNL Template is not defined; "));
					return pnlInformation;
				}
			}else{
				LogUtils.debug("PNL will not be included because it is not needed");
				pnlInformation.setNotes(pnlInformation.getNotes().concat("PNL will not be included because it is not needed; "));
				return pnlInformation;
			}
		}else{
			LogUtils.debug("PNL will not be included because it is not required");
			pnlInformation.setNotes(pnlInformation.getNotes().concat("PNL will not be included because it is not required; "));
			return pnlInformation;
		}
		
		// See if we need to take into account the language differences or not
		String pnlLanguage = findPreferenceByLevel(getPreferenceNamingConvention(PNL_LANGUAGE), part, null);
		// Set the page width and height in case it is a preview
		if(preview){
			if(UNIT_US.equals(unitValue.getName())){
				pnlInformation.setPageWidth(pageWidth != null ? Format.inch2mm(pageWidth) : null);
				pnlInformation.setPageHeight(pageHeight != null ? Format.inch2mm(pageHeight) : null);
			}else{
				pnlInformation.setPageWidth(pageWidth != null ? pageWidth : null);
				pnlInformation.setPageHeight(pageHeight != null ? pageHeight : null);
			}
		}
		// find the PNL page number
		String pnlPageNumberValue = findPreferenceByLevel(getPreferenceNamingConvention(PNL_PAGE_NUMBER), part, null);
		Integer pnlPageNumber = 1;
		if(pnlPageNumberValue != null){
			pnlPageNumber = Integer.parseInt(pnlPageNumberValue);
		}else{
			LogUtils.debug("PNL does not have page number specified where to inject it.");
			pnlInformation.setNotes(pnlInformation.getNotes().concat("PNL does not have page number specified where to inject it; "));
			return pnlInformation;
		}
		pnlInformation.setPageNumber(pnlPageNumber);
		
		// find the PNL pnlHorizontalMargin
		String pnlHorizontalMarginValue = findPreferenceByLevel(getPreferenceNamingConvention(PNL_HORIZONTAL_MARGIN), part, null);
		Float pnlHorizontalMargin = (float)10;
		if(pnlHorizontalMarginValue != null){
			if(UNIT_US.equals(unitValue.getName())){
				pnlHorizontalMargin = Format.inch2mm(Float.parseFloat(pnlHorizontalMarginValue));
			}else{
				pnlHorizontalMargin = Float.parseFloat(pnlHorizontalMarginValue);
			}
		}else{
			LogUtils.debug("PNL Horizontal Margin is not defined");
			pnlInformation.setNotes(pnlInformation.getNotes().concat("PNL Horizontal Margin is not defined; "));
			//return null;
		}
		pnlInformation.sethMargin(pnlHorizontalMargin);
		
		// find the PNL pnlVerticalMargin
		String pnlVerticalMarginValue = findPreferenceByLevel(getPreferenceNamingConvention(PNL_VERTICAL_MARGIN), part, null);
		Float pnlVerticalMargin = (float)10;
		if(pnlVerticalMarginValue != null){
			if(UNIT_US.equals(unitValue.getName())){
				pnlVerticalMargin = Format.inch2mm(Float.parseFloat(pnlVerticalMarginValue));
			}else{
				pnlVerticalMargin = Float.parseFloat(pnlVerticalMarginValue);
			}
		}else{
			LogUtils.debug("PNL Vertical Margin is not defined");
			pnlInformation.setNotes(pnlInformation.getNotes().concat("PNL Vertical Margin is not defined; "));
			//return null;
		}
		pnlInformation.setvMargin(pnlVerticalMargin);
		
		// find the PNL line spacing
		String pnlLineSpacingValue = findPreferenceByLevel(getPreferenceNamingConvention(PNL_LINE_SPACING), part, null);
		Float pnlLineSpacing = (float)0;
		if(pnlLineSpacingValue != null){
			if(UNIT_US.equals(unitValue.getName())){
				pnlLineSpacing = Format.inch2mm(Float.parseFloat(pnlLineSpacingValue));
			}else{
				pnlLineSpacing = Float.parseFloat(pnlLineSpacingValue);
			}
		}else{
			LogUtils.debug("PNL Line Spacing is not defined");
			pnlInformation.setNotes(pnlInformation.getNotes().concat("NL Line Spacing is not defined; "));
			pnlLineSpacing = (float)0;
		}
		pnlInformation.setLineSpacing(pnlLineSpacing);
		
		// Now resolve the PNL Content by replacing all variables in the text with the real values from the preferences database
		// Also need to resolve the styling in case it is being overridden
		LocalDate localDate=LocalDate.now();
		Locale spanishLocale=new Locale("es", "ES");
		Locale malayLocale=new Locale("ms", "MY");
		String pnlDateFormat = findPreferenceByLevel(getPreferenceNamingConvention(PNL_DATE_FORMAT), part, null);
		String dateInEnglish=localDate.format(DateTimeFormatter.ofPattern(pnlDateFormat != null ? pnlDateFormat : DATE_FMT,Locale.ENGLISH));
		String dateInFrench=localDate.format(DateTimeFormatter.ofPattern(pnlDateFormat != null ? pnlDateFormat : DATE_FMT,Locale.FRENCH));
		String dateInSpanish=localDate.format(DateTimeFormatter.ofPattern(pnlDateFormat != null ? pnlDateFormat : DATE_FMT,spanishLocale));
		String dateInCanadian=localDate.format(DateTimeFormatter.ofPattern(pnlDateFormat != null ? pnlDateFormat : DATE_FMT,Locale.CANADA));
		String dateInMalaysian=localDate.format(DateTimeFormatter.ofPattern(pnlDateFormat != null ? pnlDateFormat : DATE_FMT,malayLocale));
		String monthInEnglish=localDate.format(DateTimeFormatter.ofPattern(MONTH_FMT,Locale.ENGLISH));
		String monthInFrench=localDate.format(DateTimeFormatter.ofPattern(MONTH_FMT,Locale.FRENCH));
		String monthInSpanish=localDate.format(DateTimeFormatter.ofPattern(MONTH_FMT,spanishLocale));
		String monthInCanadian=localDate.format(DateTimeFormatter.ofPattern(MONTH_FMT,Locale.CANADA));
		String monthInMalaysian=localDate.format(DateTimeFormatter.ofPattern(MONTH_FMT,malayLocale));
		String yearInEnglish=localDate.format(DateTimeFormatter.ofPattern(YEAR_FMT,Locale.ENGLISH));
		String yearInFrench=localDate.format(DateTimeFormatter.ofPattern(YEAR_FMT,Locale.FRENCH));
		String yearInSpanish=localDate.format(DateTimeFormatter.ofPattern(YEAR_FMT,spanishLocale));
		String yearInCanadian=localDate.format(DateTimeFormatter.ofPattern(YEAR_FMT,Locale.CANADA));
		String yearInMalaysian=localDate.format(DateTimeFormatter.ofPattern(YEAR_FMT,malayLocale));
		
		for(PNLTemplateLine ptl : pnlTemplate.getTemplateLines()){
			StringBuilder sb = new StringBuilder();
			boolean isVar = false;
			StringBuilder sbVar = new StringBuilder();
			String varValue = null;
			for(char c : ptl.getLineText().toCharArray()){
				if(c == '%'){
					if(!isVar){
						isVar = true;
						sbVar = new StringBuilder();
					}else{
						isVar = false;
						if(sbVar.length() > 0){
							if(sbVar.indexOf(getTemplateNamingConvention(PNL_DATE)) > -1){
								if(pnlLanguage == null){
									varValue = dateInEnglish;
								}else{
									if(_EN.equals(pnlLanguage)){
										varValue = dateInEnglish;
									}else if(_FR.equals(pnlLanguage)){
										varValue = dateInFrench;
									}else if(_ES.equals(pnlLanguage)){
										varValue = dateInSpanish;
									}else if(_CA.equals(pnlLanguage)){
										varValue = dateInCanadian;
									}else if(_MA.equals(pnlLanguage)){
										varValue = dateInMalaysian;
									}else{
										varValue = dateInEnglish;
									}
								}
							}else if(sbVar.indexOf(getTemplateNamingConvention(PNL_MONTH)) > -1){
								if(pnlLanguage == null){
									varValue = monthInEnglish;
								}else{
									if(_EN.equals(pnlLanguage)){
										varValue = monthInEnglish;
									}else if(_FR.equals(pnlLanguage)){
										varValue = monthInFrench;
									}else if(_ES.equals(pnlLanguage)){
										varValue = monthInSpanish;
									}else if(_CA.equals(pnlLanguage)){
										varValue = monthInCanadian;
									}else if(_MA.equals(pnlLanguage)){
										varValue = monthInMalaysian;
									}else{
										varValue = monthInEnglish;
									}
								}
							}else if(sbVar.indexOf(getTemplateNamingConvention(PNL_YEAR)) > -1){
								if(pnlLanguage == null){
									varValue = yearInEnglish;
								}else{
									if(_EN.equals(pnlLanguage)){
										varValue = yearInEnglish;
									}else if(_FR.equals(pnlLanguage)){
										varValue = yearInFrench;
									}else if(_ES.equals(pnlLanguage)){
										varValue = yearInSpanish;
									}else if(_CA.equals(pnlLanguage)){
										varValue = yearInCanadian;
									}else if(_MA.equals(pnlLanguage)){
										varValue = yearInMalaysian;
									}else{
										varValue = yearInEnglish;
									}
								}
							}else{
								String tmp = getTemplateNamingConvention(PNL_TMPL_PRINTING_NUMBER);
								if(tmp != null){
									varValue = findPreferenceByLevel(sbVar.toString(), part, sbVar.indexOf(tmp) > -1 ? null : pnlLanguage);
								}else{
									varValue = findPreferenceByLevel(sbVar.toString(), part, pnlLanguage);
								}
							}
							if(varValue != null){
								sb.append(varValue);
							}else if(preview){
								sb.append("AAAAA");
							}
						}
					}
					continue;
				}
				if(isVar){
					sbVar.append(c);
				}else{
					sb.append(c);
				}
			}
			ptl.setLineText(sb.toString());
			
			// see if need to override the PNL font Type
			String pnlFontTypeValue = findPreferenceByLevel(getPreferenceNamingConvention(PNL_FONT_TYPE), part, null);
			if(pnlFontTypeValue != null){
				ptl.setFontType(pnlFontTypeValue);
			}
			
			// see if need to override the PNL font Size
			String pnlFontSizeValue = findPreferenceByLevel(getPreferenceNamingConvention(PNL_FONT_SIZE), part, null);
			if(pnlFontSizeValue != null){
				ptl.setFontSize(Float.parseFloat(pnlFontSizeValue));
			}
			
			// see if need to override the PNL font Bold
			String pnlFontBoldValue = findPreferenceByLevel(getPreferenceNamingConvention(PNL_FONT_BOLD), part, null);
			if(pnlFontBoldValue != null){
				ptl.setFontBold(Boolean.parseBoolean(pnlFontBoldValue));
			}
			
			// see if need to override the PNL font Italic
			String pnlFontItalicValue = findPreferenceByLevel(getPreferenceNamingConvention(PNL_FONT_ITALIC), part, null);
			if(pnlFontItalicValue != null){
				ptl.setFontItalic(Boolean.parseBoolean(pnlFontItalicValue));
			}
		}
		pnlInformation.setPnlLines(pnlTemplate.getTemplateLines());
		
		LogUtils.debug("------PNL CONTENT-------");
		LogUtils.debug(pnlInformation.toString());
		return pnlInformation;
	}
	
	/**
	 * This method will retrieve the list of PNL Standard names to be used in PNL Templates variables
	 * from the system properties file
	 * @return Map<String, String>: the convention name and its definition; ex; PNL_TMPL_ID : the template id to use
	 */
	public Map<String, String> getPNLTemplateNamingConventions(){
		Map<String, String> result = new HashMap<String, String>();
		for(Object propKey : System.getProperties().keySet()){
			if(((String)propKey).startsWith("com.epac.cap.pnl.template")){
				String theValue = System.getProperty((String) propKey);
				result.put(theValue.substring(0, theValue.indexOf(":")), theValue.substring(theValue.indexOf(":") + 1));
			}
		}
		return result;
	}
	
	/**
	 * This method will retrieve the corresponding template variable convention name for that property
	 */
	private String getTemplateNamingConvention(String prop){
		String result = null;
		String propValue = System.getProperty(prop);
		if(prop != null && propValue != null){
			result = propValue.substring(0, propValue.indexOf(':'));
		}
		return result;
	}
	
	/**
	 * This method will retrieve the corresponding preference variable convention name for that property
	 */
	public String getPreferenceNamingConvention(String prop){
		String result = null;
		if(prop != null){
			LookupSearchBean lsb = new LookupSearchBean();
			lsb.setNamePrefix(prop);
			lsb.setPrefSubject("Naming_Convention");
			lsb.setOrderByList(Arrays.asList(new OrderBy("createdDate", "desc")));
			List<Preference> foundPrefs = lookupDAO.readAll(null, lsb, Preference.class);
			if(!foundPrefs.isEmpty()){
				result = foundPrefs.get(0).getName();
			}
		}
		return result;
	}
	
	/**
	 * @return the unitValue
	 */
	public Preference getUnitValue() {
		return unitValue;
	}

	/**
	 * @param unitValue the unitValue to set
	 */
	public void setUnitValue(Preference unitValue) {
		this.unitValue = unitValue;
	}

	/**
	 * @return the lookupDAO
	 */
	public LookupDAO getLookupDAO() {
		return lookupDAO;
	}

	/**
	 * @param lookupDAO the lookupDAO to set
	 */
	public void setLookupDAO(LookupDAO lookupDAO) {
		this.lookupDAO = lookupDAO;
	}

	
}
