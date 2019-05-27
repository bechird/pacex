package com.epac.cap.handler;


import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.functionel.PrintingTimeCalculator;
import com.epac.cap.model.Job;
import com.epac.cap.model.JobStatus;
import com.epac.cap.model.Lamination;
import com.epac.cap.model.Machine;
import com.epac.cap.model.Part;
import com.epac.cap.model.Roll;
import com.epac.cap.model.Station;
import com.epac.cap.model.StationCategory;
import com.epac.cap.model.WFSDataSupport;
import com.epac.cap.model.WFSLocation;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.repository.PartDAO;
import com.epac.cap.repository.RollDAO;
import com.epac.cap.repository.StationDAO;
import com.epac.cap.repository.StationSearchBean;
import com.epac.cap.sse.printers.PrinterSseService;
import com.epac.cap.utils.LogUtils;

import jp.co.fujifilm.xmf.oc.EventListener;
import jp.co.fujifilm.xmf.oc.Printer;
import jp.co.fujifilm.xmf.oc.model.Event;
import jp.co.fujifilm.xmf.oc.model.Event.EventType;
import jp.co.fujifilm.xmf.oc.model.printing.PrintingJob;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Interacts with Station data.  Uses StationDAO for entity persistence.
 * @author walid
 *
 */
@Service
public class StationHandler {
	
	@Autowired
	private StationDAO stationDAO;
	
	@Autowired
	private MachineHandler machineHandler;
	
	@Autowired
	private RollDAO rollDAO;
	
	@Autowired
	private RollHandler rollHandler;
	
	@Autowired
	private JobHandler jobHandler;
	
	@Autowired
	private PartDAO partDAO;
	
	@Autowired
	private LookupDAO lookupDAO;
	
	@Autowired
	private PrintersHandler printers;
	
	@Autowired
	private PrintingTimeCalculator printingTimeCalculator;
	
	@Autowired
	private PrinterSseService printerSseService;

	private static Logger logger = Logger.getLogger(StationHandler.class);
	
	/***
	 * No arg constructor.  This is the preferred constructor.
	 */
	public StationHandler(){  }
  
	/** 
	 * Calls the corresponding create method on the StationDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public void create(Station bean) throws PersistenceException {
		try {
			getStationDAO().create(bean);		   
		} catch (Exception ex) {
			logger.error("Error occurred creating a Station : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Calls the corresponding update method on the StationDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void moveUp(Integer itemId, String stationId, String level) throws PersistenceException {
		try {
		   Station station = stationDAO.read(stationId);
		   if(station != null){
			    List<Job> jobsList = jobHandler.getStationJobs(stationId);
				TreeSet<Job> jobsListTS = new TreeSet<Job>(new JobsByMachineOrderingComparator());
				jobsListTS.addAll(jobsList);
				
			    if(Station.inputTypes.Roll.toString().equalsIgnoreCase(station.getInputType())){
			    	TreeSet<Roll> rollsListTS = new TreeSet<Roll>(new RollsByMachineOrderingComparator());
			    	rollsListTS.addAll(jobHandler.getStationRolls(jobsList));
			    	if("one".equals(level)){
			    		Roll prevRoll = null;
			    		for(Roll roll : rollsListTS){
			    			if(roll.getRollId().equals(itemId)){
			    				//switch the machine ordering between our roll and its previous roll
			    				if(prevRoll != null){
			    					Integer rollSwitchInt = roll.getMachineOrdering();
			    					roll.setMachineOrdering(prevRoll.getMachineOrdering()); 
			    					rollDAO.update(roll);
			    					prevRoll.setMachineOrdering(rollSwitchInt); 
			    					rollDAO.update(prevRoll);
			    				}
			    			}
			    			prevRoll = roll;
			    		}
			    	}else if("top".equals(level)){
			    		int positionIndex = 0;
			    		Roll prevRoll = null;
			    		Integer orderingOfTargetEntry = null;
			    		Integer orderingOfSelectedEntry = null;
			    		for(Roll roll : rollsListTS){
			    			if(positionIndex >= 3){
			    				if(positionIndex == 3){
			    					orderingOfTargetEntry = roll.getMachineOrdering();
			    				}else{
			    					if(roll.getRollId().equals(itemId)){
			    						orderingOfSelectedEntry = roll.getMachineOrdering();
			    						roll.setMachineOrdering(orderingOfTargetEntry); rollDAO.update(roll);
			    						prevRoll.setMachineOrdering(orderingOfSelectedEntry); rollDAO.update(prevRoll);
			    						break;
			    					}
			    					prevRoll.setMachineOrdering(roll.getMachineOrdering()); rollDAO.update(prevRoll);
			    				}
			    			}
			    			positionIndex ++;
			    			prevRoll = roll;
			    		}
			    	}
			    }else{
			    	if("one".equals(level)){
				    	Job prevJob = null;
				    	for(Job job : jobsListTS){
			    			if(job.getJobId().equals(itemId)){
			    				//switch the machine ordering between our job and its previous job
			    				if(prevJob != null){
			    					Integer jobSwitchInt = job.getMachineOrdering();
			    					job.setMachineOrdering(prevJob.getMachineOrdering()); 
			    					jobHandler.update(job);
			    					prevJob.setMachineOrdering(jobSwitchInt); 
			    					jobHandler.update(prevJob);
			    				}
			    			}
			    			prevJob = job;
			    		}
				    }else if("top".equals(level)){
				    	int positionIndex = 0;
			    		Job prevJob = null;
			    		Integer orderingOfTargetEntry = null;
			    		Integer orderingOfSelectedEntry = null;
			    		for(Job job : jobsListTS){
			    			if(positionIndex >= 3){
			    				if(positionIndex == 3){
			    					orderingOfTargetEntry = job.getMachineOrdering();
			    				}else{
			    					if(job.getJobId().equals(itemId)){
			    						orderingOfSelectedEntry = job.getMachineOrdering();
			    						job.setMachineOrdering(orderingOfTargetEntry); jobHandler.update(job);
			    						prevJob.setMachineOrdering(orderingOfSelectedEntry); jobHandler.update(prevJob);
			    						break;
			    					}
			    					prevJob.setMachineOrdering(job.getMachineOrdering()); jobHandler.update(prevJob);
			    				}
			    			}
			    			positionIndex ++;
			    			prevJob = job;
			    		}
				    }
			    }
		   }
		} catch (Exception ex) {
			logger.error("Error occurred while changing the order of some jobs on the overview dashboard section: " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Used to move up the jobs/rolls on the station so they get produced first
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(Station bean) throws PersistenceException {
		try {
			getStationDAO().update(bean);		   
		} catch (Exception ex) {
			logger.error("Error occurred updating a Station : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
		
	/** 
	 * Calls the corresponding delete method on the StationDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public boolean delete(Station bean) throws PersistenceException {
		try {
			return getStationDAO().delete(bean);
		} catch (Exception ex) {
			logger.error("Error occurred deleting a Station with id '" + (bean == null ? null : bean.getStationId()) + "' : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}

	/** 
	 * A convenience method which calls readAll(StationSearchBean) with a
	 * null search bean. 
	 *
	 * @see #readAll(StationSearchBean)
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Station> readAll() throws PersistenceException{
		return this.readAll(null);    
	}
	
	/** 
	 * Calls the corresponding readAll method on the StationDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Station> readAll(StationSearchBean searchBean) throws PersistenceException{
		try{
			return getStationDAO().readAll(searchBean);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a list of Stations : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	
	/** 
	 * Calls the corresponding read method on the StationDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Station read(String stationId) throws PersistenceException{
		try{
			return getStationDAO().read(stationId);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a Station with id '" + stationId + "' : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Calls the corresponding fetchStationsMenu method on the StationDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<String[]> readStationsForMenu() throws PersistenceException{
		try{
			return getStationDAO().fetchStationsMenu();
		} catch (Exception ex) {
			logger.error("Error occurred retrieving stations For menu" + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	     	 
	/** 
	 * Calls the corresponding readStationName method on the StationDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public String readStationName(String stationId) throws PersistenceException{
		try{
			return getStationDAO().readStationName(stationId);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving station's name" + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Loads a station data.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public Station loadStation(String stationId) throws PersistenceException {
		try {
			Station station = getStationDAO().read(stationId);
			// For the press station, prepare printers for the EPAC mode functioning
			// first see which ones work for Emf mode, in which case disregard those printers
			// then ping each printer to check for Epac mode
			// finally initiate sse connection on each one to watch Epac mode status for each printer
			if(StationCategory.Categories.PRESS.toString().equals(stationId) && station != null){
				for(Machine m : station.getMachines()){
					Roll rop = m.getRollOnProd();
					if(Boolean.TRUE.equals(m.getIsEmfMode())){
						m.setIsEpacModePrintingActive(Boolean.FALSE);
					}else{
						try{
							Printer printer = printers.getPrinter(m.getMachineId());
							if(StringUtils.isBlank(m.getIpAddress())){
								m.setIsEpacModePrintingActive(Boolean.FALSE);
								if(printer != null){
									printers.shutdownPrinter(m.getMachineId());
								}
								continue;
							}else{
								if(printer == null){
									m.setIsEpacModePrintingActive(Boolean.TRUE);
								}else{
									m.setIsEpacModePrintingActive(printer.isEpacMode());
								}
							}
							//we need the raster names to be able to localize the jobs running and update their printing status on the front end (in the loadstation method on front end)
							if(rop != null){
								for(Job job : rop.getJobs()){
									Part pr = partDAO.read(job.getPartNum());
									List<String> rasters = new ArrayList<String>();
									if(pr != null){
										for(WFSDataSupport ds : pr.getDataSupportsOnProd()){
											if(ds.getName().equals(WFSDataSupport.NAME_RASTER)){
												WFSLocation location = ds.getLocationdByType(WFSLocation.DESTINATION);
												if(location != null){
													rasters.add(location.getFileName());
												}
											}
										}
									}
									job.setRasterNames(rasters);
								}
							}
							if(printer == null){
								printer = new Printer("http://".concat(m.getIpAddress().concat(m.getNetPort() != null ? ":".concat(Integer.toString(m.getNetPort())): "")), m.getMachineId());
								EventListener el  = new EventListener() {
									@Override
									public void handleEvent(Event event) {
										LogUtils.debug("Event received from OutputController ["+m.getMachineId()+"]: "+event.getType());
										if(event.getType() == EventType.PRINTING) {
											Roll roll = null;
											try{
												roll = rollHandler.getOnProdRollByMachine(m.getMachineId());
											}catch(Exception e){
												LogUtils.error("Error occured while reading machine instance from DB: "+m.getMachineId());
											}
											
											if(roll == null){
												LogUtils.debug("No roll on prod on the machine ["+m.getMachineId()+"], event ignored");
												return;
											}
										
											Set<Job> jobs = roll.getJobs();
								
											List list = (List) event.getObject();
											for (Object object : list) {
												if(object instanceof PrintingJob) {
													PrintingJob printingJob = (PrintingJob)object;
													for (Job job : jobs) {
														if(JobStatus.JobStatuses.COMPLETE.getName().equals(job.getJobStatus().getId())){
															continue;
														}
														String jobName = job.getJobName();
														if(StringUtils.isBlank(jobName)){
															WFSLocation location = null;
															try {
																String sheetSize = printingTimeCalculator.getBestSheetHeight(roll, job,
																		machineHandler.getBsValue(), machineHandler.getUseOptimizedSheetAlgo());
																location = machineHandler.getRasterLocation(job, "EM", sheetSize);
																if(location != null){
																	jobName = String.valueOf(job.getJobId()).concat(location.getFileName());
																}
															} catch (PersistenceException e1) {
																LogUtils.error("Error occured while trying to get job name from hunkeler station");
															}
														}
														if(StringUtils.isBlank(jobName) || !jobName.equals(printingJob.getId())){
															continue;
														}
														
														Double produced = 0.0;
														if(printingJob.getPrintedSheetCount() != null){
															produced = Math.floor(printingJob.getPrintedSheetCount()/printingJob.getJob().getNumSheets());
														}
																
														// update produced quantity
														job.setQuantityProduced(produced.intValue());
														
														if("COMPLETED".equals(printingJob.getStatus())) {
															LogUtils.debug(m.getMachineId()+": Update job #"+job.getJobId()+" produced="+produced+" and status=COMPLETE");
															// set status to complete
															job.setQuantityProduced(job.getQuantityNeeded());
															job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.COMPLETE.getName(), JobStatus.class));
														}else if ("FAILED".equals(printingJob.getStatus()) && job.getQuantityProduced() > 0) {
															LogUtils.debug(m.getMachineId()+": Update job #"+job.getJobId()+" produced="+produced+" and status=FAILED");
															// set status to partial complete
															job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.COMPLETE_PARTIAL.getName(), JobStatus.class));
														}else if ("RUNNING".equals(printingJob.getStatus())) {
															LogUtils.debug(m.getMachineId()+": Update job #"+job.getJobId()+" produced="+produced+" and status=RUNNING");
															// set status to running
															job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.RUNNING.getName(), JobStatus.class));
														}
														try {
															jobHandler.update(job);
														} catch (PersistenceException e) {
															LogUtils.error("Error occured while updating status for job #"+job.getJobId());
														}
														LogUtils.debug(m.getMachineId()+": Job "+job.getJobId()+" updated: [status = "+job.getJobStatus().getName()+", produced: "+job.getQuantityProduced()+"]");
													}
	
												}else {
													LogUtils.error(m.getMachineId()+": Event object is not a PrintingJob instance, ignored");
												}
											}
										}
									}
								};
								printer.addListener(el);
								printerSseService.connect("http://".concat(m.getIpAddress().concat(m.getNetPort() != null ? ":".concat(Integer.toString(m.getNetPort())): "")), m.getMachineId(), el);								
								printers.addPrinter(m.getMachineId(), printer);
							}
						}catch(Exception e){
							LogUtils.error("", e);
							m.setIsEpacModePrintingActive(Boolean.FALSE);
							machineHandler.update(m);
						}
					}
				}
			}
			// spine and head/tail: needed for display on the front end when case bound station
			if(StationCategory.Categories.CASEBOUND.toString().equals(stationId) && station != null){
				for(Machine m : station.getMachines()){
					Job job = m.getJobOnProd();
					if(job != null){
						Part pr = partDAO.read(job.getPartNum());
						if(pr != null){
							job.setSpineType(pr.getSpineType());
							job.setHeadTailBands(pr.getHeadTailBands());
						}
					}
				}
			}
			// wire color: needed for display on the front end when coil bind station
			if(StationCategory.Categories.PLASTICOIL.toString().equals(stationId) && station != null){
				for(Machine m : station.getMachines()){
					Job job = m.getJobOnProd();
					if(job != null){
						Part pr = partDAO.read(job.getPartNum());
						if(pr != null){
							job.setWireColor(pr.getWireColor());
						}
					}
				}
			}
			// pages count: needed for display on the front end for cover press station
			if(StationCategory.Categories.COVERPRESS.toString().equals(stationId) && station != null && !Station.inputTypes.Batch.toString().equalsIgnoreCase(station.getInputType())){
				for(Machine m : station.getMachines()){
					for(Job job : m.getRunningAndAssignedJobs()){
						Part pr = partDAO.read(job.getPartNum());
						if(pr != null){
							job.setPartPagesCount(pr.getPagesCount());
						}
					}
				}
			}
			if(StationCategory.Categories.PLOWFOLDER.toString().equals(stationId) && station != null){
				for(Machine m : station.getMachines()){
					Roll rop = m.getRollOnProd();
					if(rop != null){
						for(Job job : rop.getJobs()){
							Part pr = partDAO.read(job.getPartNum());
							if(pr != null){
								job.setPartPagesCount(pr.getPagesCount());
							}
						}
					}
				}
			}
			// prepare the cart numbers to show on the stations
			if(station != null && !StationCategory.Categories.PRESS.toString().equals(stationId) && 
					!StationCategory.Categories.PLOWFOLDER.toString().equals(stationId) && !StationCategory.Categories.COVERPRESS.toString().equals(stationId)){
				for(Machine m : station.getMachines()){
					for(Job job : m.getAssignedJobs()){
						job.setPrevJobData(jobHandler.findPrevJobData(job.getJobId()));
					}
				}
			}
			//set the lamination when cover or lam station
			if(station != null && (StationCategory.Categories.COVERPRESS.toString().equals(stationId) || StationCategory.Categories.LAMINATION.toString().equals(stationId))){
				for(Machine m : station.getMachines()){
					for(Job job : m.getRunningAndAssignedJobs()){
						String laminationId = partDAO.findLamination(job.getPartNum());
						Part pr = partDAO.read(job.getPartNum().endsWith("C") ? job.getPartNum().replace("C", "") : job.getPartNum());
						if(pr != null && pr.getSpotVarnish()){
							job.setPartLamination(new Lamination(laminationId + "/SV"));
						}else{
							job.setPartLamination(new Lamination(laminationId));
						}
					}
				}
			}
			return station;
		} catch (Exception ex) {
			logger.error("Error occurred loading a Station with id '" + stationId + "' : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	/**
	 * @return the StationDAO
	 */
	public StationDAO getStationDAO() {
		return stationDAO;
	}

	/**
	 * @param dao the StationDAO to set
	 */
	public void setStationDAO(StationDAO dao) {
		this.stationDAO = dao;
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
	 * @return the rollDAO
	 */
	public RollDAO getRollDAO() {
		return rollDAO;
	}

	/**
	 * @param rollDAO the rollDAO to set
	 */
	public void setRollDAO(RollDAO rollDAO) {
		this.rollDAO = rollDAO;
	}

	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Object[]> getNameOfSations() {
		
		try{
			return getStationDAO().getNameOfSations();
		} catch (Exception ex) {
			logger.error("Error occurred retrieving stations name" + ex.getMessage(),ex);
		}
		return new ArrayList<Object[]>();
	}
	public List<Station> fetchStation(){
		return stationDAO.fetchStation();
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

	/**
	 * @return the printers
	 */
	public PrintersHandler getPrinters() {
		return printers;
	}

	/**
	 * @param printers the printers to set
	 */
	public void setPrinters(PrintersHandler printers) {
		this.printers = printers;
	}

	/**
	 * @return the printingTimeCalculator
	 */
	public PrintingTimeCalculator getPrintingTimeCalculator() {
		return printingTimeCalculator;
	}

	/**
	 * @param printingTimeCalculator the printingTimeCalculator to set
	 */
	public void setPrintingTimeCalculator(PrintingTimeCalculator printingTimeCalculator) {
		this.printingTimeCalculator = printingTimeCalculator;
	}

	/**
	 * @return the printerSseService
	 */
	public PrinterSseService getPrinterSseService() {
		return printerSseService;
	}

	/**
	 * @param printerSseService the printerSseService to set
	 */
	public void setPrinterSseService(PrinterSseService printerSseService) {
		this.printerSseService = printerSseService;
	}
 
}