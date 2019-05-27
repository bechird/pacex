package com.epac.cap.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.common.SectionRunInfo;
import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.functionel.PrintingTimeCalculator;
import com.epac.cap.functionel.Rip;
import com.epac.cap.model.BatchStatus;
import com.epac.cap.model.CoverBatch;
import com.epac.cap.model.CoverBatchJob;
import com.epac.cap.model.CoverSection;
import com.epac.cap.model.CoverSection.copyStatuses;
import com.epac.cap.model.Job;
import com.epac.cap.model.JobStatus;
import com.epac.cap.model.JobStatus.JobStatuses;
import com.epac.cap.model.JobType;
import com.epac.cap.model.Lamination;
import com.epac.cap.model.Log;
import com.epac.cap.model.LogCause;
import com.epac.cap.model.LogResult;
import com.epac.cap.model.Machine;
import com.epac.cap.model.MachineStatus;
import com.epac.cap.model.MachineType;
import com.epac.cap.model.Order;
import com.epac.cap.model.Part;
import com.epac.cap.model.Preference;
import com.epac.cap.model.Roll;
import com.epac.cap.model.RollStatus;
import com.epac.cap.model.SectionStatus;
import com.epac.cap.model.Station;
import com.epac.cap.model.StationCategory;
import com.epac.cap.model.WFSDataSupport;
import com.epac.cap.model.WFSLocation;
import com.epac.cap.model.WFSPartWorkflow;
import com.epac.cap.model.WFSProgress;
import com.epac.cap.model.WFSStatus.ProgressStatus;
import com.epac.cap.repository.CoverBatchDAO;
import com.epac.cap.repository.CoverSectionDAO;
import com.epac.cap.repository.JobDAO;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.repository.LogDAO;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.repository.MachineDAO;
import com.epac.cap.repository.MachineSearchBean;
import com.epac.cap.repository.OrderDAO;
import com.epac.cap.repository.RollDAO;
import com.epac.cap.repository.StationDAO;
import com.epac.cap.service.NotificationService;
import com.epac.cap.sse.beans.Event;
import com.epac.cap.sse.beans.EventTarget;
import com.epac.cap.utils.Format;
import com.epac.cap.utils.LogUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.itextpdf.text.pdf.PdfReader;

import jp.co.fujifilm.xmf.oc.Printer;
import jp.co.fujifilm.xmf.oc.model.printing.ErrorList;
import jp.co.fujifilm.xmf.oc.model.printing.PrintingJob;
import jp.co.fujifilm.xmf.oc.model.printing.PrintingJobRequest;

/**
 * Interacts with Machine data.  Uses MachineDAO for entity persistence.
 * @author walid
 *
 */

@Service
public class MachineHandler {
	
	private static final String STD_MODE  = "SM";
	private static final String EPAC_MODE = "EM";
	private static final String REST_PRINTING_REQUEST 	= "/printing";

	private static Map<String, CopyLock> locks = new ConcurrentHashMap<String, CopyLock>();
	
	private static Object slock = new Object();

	@Autowired
	private MachineDAO machineDAO;
	
	@Autowired
	private RollDAO rollDAO;
	
	@Autowired
	private CoverBatchDAO coverBatchDAO;
	
	@Autowired
	private CoverSectionDAO coverSectionDAO;
	
	@Autowired
	CoverSectionHandler coverSectionHandler;
	
	@Autowired
	private RollHandler rollHandler;
	
	@Autowired
	private JobHandler jobHandler;
	
	@Autowired
	private CoverSectionHandler sectionHandler;
	
	@Autowired
	private JobDAO jobDAO;
	
	@Autowired
	private LookupDAO lookupDAO;
	
	@Autowired
	private LogDAO logDAO;
	
	@Autowired
	private StationDAO stationDAO;
	
	@Autowired
	private OrderDAO orderDAO;
	
	@Autowired
	private PrintersHandler printersHandler;
	
	@Autowired
	private PartHandler partHandler;
	
	@Autowired
	private PrintingTimeCalculator printingTimeCalculator;
	
	@Autowired
	private SshConnectionsManager sshConnectionsManager;
	
	
	public final static  String UNIT_US = "US";
	public final static  String UNIT_FR = "FR";
	private Preference unitValue;
	private Preference copyCoverRasterFilesValue;
	
	private static ExecutorService executor = Executors.newFixedThreadPool(8);

	private static Logger logger = Logger.getLogger(MachineHandler.class);

	@Autowired
	private NotificationService notificationService;
	
	/*
	private static SseBroadcaster filesCopyInfoBroadcaster = new SseBroadcaster();
	*/
	
	/***
	 * No arg constructor.  This is the preferred constructor.
	 */
	public MachineHandler(){  
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				LogUtils.error("", e);
			}
		});
	}
  
	
	/*
	public void register(EventOutput eventOutput) {
		filesCopyInfoBroadcaster.add(eventOutput);
	}
	
	public synchronized static void broadcastCopyFilesInfo(Roll roll) {
		OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
		OutboundEvent event = eventBuilder.name("message").mediaType(MediaType.APPLICATION_JSON_TYPE)
				.data(Roll.class, roll).build();
		filesCopyInfoBroadcaster.broadcast(event);
	}
	*/
	
	/** 
	 * Calls the corresponding create method on the MachineDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public void create(Machine bean) throws PersistenceException {
		try {
			getMachineDAO().create(bean);		   
		} catch (Exception ex) {
			logger.error("Error occurred creating a Machine : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Calls the corresponding update method on the MachineDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(Machine bean) throws PersistenceException {
		try {
			if(bean.getCurrentJob() != null){
				if(bean.getCurrentJob().getJobId() == null){
					bean.setCurrentJob(null);
				}
			}
			getMachineDAO().update(bean);
		} catch (Exception ex) {
			logger.error("Error occurred updating a Machine : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * Assigning rolls to machines/printers; assigning the roll to the machine and updating the statuses.
	 */
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public List<String> assignToMachine(String[] selectedRollsForAssignment, String executingUserId) throws PersistenceException {
		List<String> rollsNotAssigned = new ArrayList<String>();
		Machine machine = null;
		String machineId = null;
		try {
			if(selectedRollsForAssignment != null && selectedRollsForAssignment.length > 1){
				machineId = selectedRollsForAssignment[0];
				machine = machineDAO.read(machineId);
				int currentNbRolls = 0;
				Set<Roll> rollsAssigned = machine.getAssignedRolls();
				if(machine != null && rollsAssigned != null){
					currentNbRolls = rollsAssigned.size();
					for(Roll iter : rollsAssigned){
						if(iter.getMachineOrdering() > currentNbRolls){
							currentNbRolls = iter.getMachineOrdering();
						}
					}
				}
				Printer printer = printersHandler.getPrinter(machine.getMachineId());
				if(StationCategory.Categories.PRESS.toString().equals(machine.getStationId())){
					LogUtils.debug("printer = "+printer+", isEpacMode = "+(printer != null && printer.isEpacMode()));
				}
				// TODO, so make sure when roll is complete or no more on that machine, that we update the machin.getRolls
				
				// for the rolls, assign them to the machine and update their status
				// at the same time assign the jobs on the roll to that same machine
				for(int i=1; i< selectedRollsForAssignment.length; i++){
					boolean filesReady = true;
					Roll roll = rollDAO.read(Integer.parseInt(selectedRollsForAssignment[i]));
					if(roll != null && machine != null){
						if(StationCategory.Categories.PLOWFOLDER.toString().equals(machine.getStationId())){
							if(machine.getMachineType() != null){
								if(!machine.getMachineType().getId().equals(roll.getMachineTypeId())){
									rollsNotAssigned.add(selectedRollsForAssignment[i] + ": Roll should not run on this type of machine.");
									continue;
								}
							}
						}
						String sheetSize = null;
						// see if any roll has jobs where the raster files do not exist
						if(StationCategory.Categories.PRESS.toString().equals(machine.getStationId())){
							Set<Job> jobs = roll.getJobs();
							if(printer != null){
								sheetSize = printingTimeCalculator.getBestSheetHeight(roll);
							}
							for(Job j : jobs){
								Part pr = partHandler.read(j.getPartNum());
								if(printer != null ){
									WFSLocation loc = getRasterLocation(j, EPAC_MODE, sheetSize);
									if(loc != null && loc.getPath() != null){
										File f = new File(loc.getPath());
										if(!f.exists()){
											LogUtils.debug("File "+f.getAbsolutePath()+" does not exist for job# "+j.getJobId()+" (Roll# "+roll.getRollId()+")");
											filesReady = false;
											rollsNotAssigned.add(roll.getRollId() + ": File " + f.getAbsolutePath() + " does not exist for job# " + j.getJobId() + "; ");
											break;
										}else{
											if(!"System_Esprint".equals(pr.getCreatorId())){
												WFSPartWorkflow partWorkflowOnProd = pr.getPartWorkFlowOnProd();
												if(partWorkflowOnProd != null){
													WFSProgress actualProgress = partWorkflowOnProd.getProgressByActionName("rip");
													if(actualProgress == null || !ProgressStatus.DONE.getName().equals(actualProgress.getStatus())){
														LogUtils.debug("Raster files (Epac mode) not yet ready for job# "+j.getJobId()+" (Roll# "+roll.getRollId()+")");
														filesReady = false;
														rollsNotAssigned.add(roll.getRollId() + ": Raster files (Epac mode) not yet ready for job# "+j.getJobId() + "; ");
														break;
													}
												}
											}else{// make sure all 9 files exist
												if(!f.isDirectory() || f.list() == null || f.list().length < Rip.rasterEMExtensions.length){
													LogUtils.debug("File "+f.getAbsolutePath()+" does not have all rasters ready for job# "+j.getJobId()+" (Roll# "+roll.getRollId()+")");
													filesReady = false;
													rollsNotAssigned.add(roll.getRollId() + ": File " + f.getAbsolutePath() + " does not have all rasters ready for job# " + j.getJobId() + "; ");
													break;
												}
											}
										}
									}else{
										LogUtils.debug("File location (Epac mode) is null for job# "+j.getJobId()+" (Roll# "+roll.getRollId()+")");
										filesReady = false;
										rollsNotAssigned.add(roll.getRollId() + ": File location (Epac mode) is null for job# "+j.getJobId()+"; ");
										break;
									}
								}else{
									//if(Order.OrderSources.ESPRINT.toString().equals(j.getOrder().getSource())){
										WFSLocation loc = getRasterLocation(j, STD_MODE, null);
										if(loc != null && loc.getPath() != null){
											File f = new File(loc.getPath());
											if(!f.exists()){
												LogUtils.debug("File "+f.getAbsolutePath()+" does not exist for job# "+j.getJobId()+" (Roll# "+roll.getRollId()+")");
												filesReady = false;
												rollsNotAssigned.add(roll.getRollId() + ": File "+f.getAbsolutePath()+" does not exist for job# "+j.getJobId()+"; ");
												break;
											}else{
												if(!"System_Esprint".equals(pr.getCreatorId())){
													WFSPartWorkflow partWorkflowOnProd = pr.getPartWorkFlowOnProd();
													if(partWorkflowOnProd != null){
														WFSProgress actualProgress = partWorkflowOnProd.getProgressByActionName("rip");
														if(actualProgress == null || !ProgressStatus.DONE.getName().equals(actualProgress.getStatus())){
															LogUtils.debug("Raster files (Standard mode) not yet ready for job# "+j.getJobId()+" (Roll# "+roll.getRollId()+")");
															filesReady = false;
															rollsNotAssigned.add(roll.getRollId() + ": Raster files (Standard mode) not yet ready for job# "+j.getJobId()+"; ");
															break;
														}
													}
												}else{// make sure all 9 files exist; which is checked before when rip is done but only for manual orders...
													
												}
											}
										}else{
											LogUtils.debug("File location (Standard mode) is null for job# "+j.getJobId()+" (Roll# "+roll.getRollId()+")");
											filesReady = false;
											rollsNotAssigned.add(roll.getRollId() + ": File location (Standard mode) is null for job# "+j.getJobId()+"; ");
											break;
										}
									//}
								}
							}
							if(!filesReady){
								continue;
							}
						}
						roll.setMachineId(machineId);
						roll.setStatus(lookupDAO.read(RollStatus.statuses.ASSIGNED.toString(), RollStatus.class));
						// set the machineOrdering
						roll.setMachineOrdering(i + currentNbRolls);
						// set the machine id on all jobs of the roll
						for(Job jb : roll.getJobs()){
							jb.setMachineId(machineId);
						}
						if(StationCategory.Categories.PRESS.toString().equals(machine.getStationId())){
							roll.setCopyStatus(Roll.copyStatuses.IN_PROGRESS.toString());
						}
						roll.setLastUpdateDate(new Date());
						roll.setLastUpdateId(executingUserId);
						getRollDAO().update(roll);
						machine.getAssignedRolls().add(roll);
						this.getMachineDAO().update(machine);
						final String fSheetSize = sheetSize;
						/* MachineHandler.broadcastCopyFilesInfo(roll); */
						if(StationCategory.Categories.PRESS.toString().equals(machine.getStationId())){
							Event event=new Event(EventTarget.MachineCopyFiles, false, null, roll);
							notificationService.broadcast(event);
						
							Runnable task = new Runnable() {
								@Override
								public void run() {
									try {
										boolean successfullyCopied = true;
										
										successfullyCopied = copyRasterFilesToPrinter(roll, fSheetSize);
										
										LogUtils.debug("First time copyRasterFilesToPrinter returned "+successfullyCopied+" for roll "+roll.getRollId());
										
										if(!successfullyCopied){
											successfullyCopied = copyRasterFilesToPrinter(roll, fSheetSize);
											LogUtils.debug("Second time copyRasterFilesToPrinter returned "+successfullyCopied+" for roll "+roll.getRollId());
										}
										
										if(successfullyCopied){
											roll.setCopyStatus(Roll.copyStatuses.FINISHED.toString());
											LogUtils.debug("updating roll "+roll.getRollId()+" with status "+Roll.copyStatuses.FINISHED.toString());

											getRollHandler().update(roll);
											LogUtils.debug("New CopyStatus for "+roll.getRollId()+" is "+roll.getCopyStatus());

											Event event=new Event(EventTarget.MachineCopyFiles, false, null, roll);
											notificationService.broadcast(event);
										}else{
											LogUtils.debug("Sending Error event  MachineCopyFiles to web interface, RollID: "+roll.getRollId());
											roll.setCopyStatus(Roll.copyStatuses.ERROR.toString());
											Event event=new Event(EventTarget.MachineCopyFiles, true, "Error occured while copying raster file for roll: " + roll.getRollId() + ". Please try again.", roll);
											notificationService.broadcast(event);
											
											//roll.setMachineId(null);
											//roll.setStatus(lookupDAO.read(RollStatus.statuses.SCHEDULED.toString(), RollStatus.class));
											getRollHandler().update(roll);
										}

									} catch (Exception e) {
										LogUtils.error("Error occured while copying raster files, RollID: "+roll.getRollId(), e);
										roll.setCopyStatus(Roll.copyStatuses.ERROR.toString());
										Event event=new Event(EventTarget.MachineCopyFiles, true, e.getLocalizedMessage(), roll);
										notificationService.broadcast(event);
										
										//roll.setMachineId(null);
										//roll.setStatus(lookupDAO.read(RollStatus.statuses.SCHEDULED.toString(), RollStatus.class));
										try {
											getRollHandler().update(roll);
										} catch (PersistenceException e1) {
											LogUtils.error("Could not update roll #"+roll.getRollId()+" in database: ", e1);
										}
									}
								}
							};
							executor.submit(task);
							
						}
					}
				}
			}
			Map<String, Object> ssePayload = new HashMap<String, Object>();
			ssePayload.put("stationId", machine.getStationId());
			Event RollsEvent = new Event(EventTarget.RollStatus, false, null, ssePayload);
			notificationService.broadcast(RollsEvent);
			return rollsNotAssigned;
		} catch (Exception ex) {
			Event event=new Event(EventTarget.MachineCopyFiles, true, ex.getLocalizedMessage(), machineId);
			notificationService.broadcast(event);
			logger.error("Error occurred assigning rolls to a machine : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	ExecutorService fileCopierExecutor = Executors.newFixedThreadPool(6, new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					LogUtils.error("",e);
				}
			});
			return t;
		}
	});
	
	class CopyResult{
		public boolean successful = true;
		public long copiedSize = 0;
	}
	
	private class CopyLock{
		private int count;
		public void increment(){ count++; }
		public void decrement(){ count--; }
		public boolean exists(){ return count > 0; }
	}
	
	// FIXME: enable/disable copy files en Epac/Standard mode
	public boolean copyRasterFilesToPrinter(Roll roll, String sheetSize) throws Exception{
		LogUtils.start();
		
		Set<Job> jobs = roll.getJobs();
		CountDownLatch latch = new CountDownLatch(jobs.size());
		final CopyResult result = new CopyResult();
		int i = 1;
		Machine machine = machineDAO.read(roll.getMachineId());
		Printer printer = printersHandler.getPrinter(machine.getMachineId());
		//if(printer != null && printer.isEpacMode()){
		if(printer != null){
			long totalFilesSize = calcSizeFileToCopy(roll, EPAC_MODE, sheetSize);
			
			
			// Case of Epac Mode printing
			for (Job j : jobs){
				WFSLocation location = getRasterLocation(j, EPAC_MODE, sheetSize);
				if(location != null && location.getPath() != null){
					// If two rolls are assigned to same/different machine(s) and they contain the same part, 
					// only one will be able to copy raster files at the same time, the other one will be blocked 
					// until the first thread is out from synchronized bloc otherwise there will be an I/O Exception
					CopyLock lock = null;
					synchronized(slock){
						lock = locks.get(location.getPath());
						if(lock == null){
							lock = new CopyLock();
							locks.put(location.getPath(), lock);
						}
						lock.increment();
					}
					
					synchronized (lock) {
						File f = new File(location.getPath());
						if(f.exists()){
							String jobName = String.valueOf(j.getJobId()).concat(f.getName());
							j.setJobName(jobName);
							jobHandler.update(j);
							File jobDirectroy = new File(machine.getOcInputPath(), jobName);
							/*if(jobDirectroy.exists())
								continue;
							*/
							try {
								jobDirectroy.mkdirs();
								int finalI = i;
								File [] files = f.listFiles();
								fileCopierExecutor.submit(new Runnable() {
									@Override
									public void run() {
										LogUtils.debug("copyRaster thread number #"+finalI+" (Job #"+j.getJobId()+") has started");
										StringBuilder sourceList = new StringBuilder("");
										StringBuilder destList = new StringBuilder("");
										/*for (int k = 0; k < files.length; k++) {
											//File source = files[k];
											sourceList.append(files[k].getAbsolutePath()).append(" ");
											String newName = files[k].getName().replace(f.getName(), jobName);
											destList.append(machine.getOcInputPath()).append(File.separator).append(jobName)
											.append(File.separator).append(newName).append(" ");
										}*/
                                        String srcs = f.getName();
                                        String dests = jobName;
										doCopyFile(srcs, dests,result,machine,totalFilesSize,roll, EPAC_MODE);
										
										latch.countDown();
									}
								});
								
							} catch (Exception e) {
								LogUtils.error("Raster file copy failed", e);
							}
							
						}
						
					}
					
					synchronized(slock){
						lock.decrement();
						if(!lock.exists()){
							locks.remove(location.getPath());
						}
					}
				}
				i += 1;
			}
		}else{
			//Case of Normal (Standard) Mode printing
			//List<Job> jobs = new CopyOnWriteArrayList<>(roll.getJobs());
			long totalFilesSize = calcSizeFileToCopy(roll, STD_MODE, null);
			for (Job j : jobs){
				WFSLocation location = getRasterLocation(j, STD_MODE, null);
				if(location != null && location.getPath() != null){
					// If two rolls are assigned to same/different machine(s) and they contain the same part, 
					// only one will be able to copy raster files at the same time, the other one will be blocked 
					// until the first thread is out from synchronized bloc otherwise there will be an I/O Exception
					CopyLock lock = null;
					synchronized(slock){
						lock = locks.get(location.getPath());
						if(lock == null){
							lock = new CopyLock();
							locks.put(location.getPath(), lock);
						}
						lock.increment();
					}
				  synchronized (lock) {
					File f = new File(location.getPath());
					if(f.exists()){
						File srcDirectory = f.getParentFile();
						String filename = Files.getNameWithoutExtension(f.getName());
						StringBuilder sb = new StringBuilder();
						
						File dstDirectroy = new File(machine.getOcInputPath());

						String sheetCount = "NA";
						
						WFSLocation imposedFileLocation = getImposedFileLocation(j);
						if(imposedFileLocation != null){
							try {
						
								  PdfReader reader = new PdfReader(imposedFileLocation.getPath());
								  int pages = reader.getNumberOfPages();
								  LogUtils.debug("File has "+pages+" pages: "+imposedFileLocation.getPath());
								  reader.close();
								 
								  int totalSheetCount = pages/2;
							
								  if(MachineType.types.POPLINE.toString().equals(roll.getMachineTypeId())){
									  // depending on whether 2UP or 3UP:
									  if(JobType.JobTypes.PRINTING_3UP.toString().equals(j.getJobType().getId())){
										  sheetCount = String.valueOf(j.getQuantityNeeded() * totalSheetCount / 3);
									  }else{
										  sheetCount = String.valueOf(j.getQuantityNeeded() * totalSheetCount / 2);
									  }
								  }else{
									  sheetCount = String.valueOf(j.getQuantityNeeded() * totalSheetCount);
								  }
								  
								  LogUtils.debug("Calculated sheetCount: "+sheetCount);

								  int floatingPoint = -1;
								  if((floatingPoint = sheetCount.indexOf('.')) != -1){
									  sheetCount = sheetCount.substring(0, floatingPoint).concat("VRF");
								  }
								  
							} catch (Exception e) {
								LogUtils.error("Error occured while trying to get value for "+ConfigurationConstants.RIP_MAX_QUANTITY, e);
							}
						}else{
							LogUtils.debug("Imposed file for job "+j.getJobId()+" is null");
						}
						
						// this fix was done to prevent cutLength to be empty on XMF jobs not having XMF ID as prefix
						int firstIndex = filename.indexOf("_");
						int lastIndex  = filename.lastIndexOf("_");
						
						if(lastIndex == firstIndex)
							firstIndex = 0;
						else 
							firstIndex +=1;
							
						String cutLength = filename.substring(firstIndex, lastIndex + 1);
						
						String jobName = sb.append("R").append(roll.getRollId())
								.append("_")
								.append(sheetCount)
								.append("_")
								.append(cutLength)
								.append(i).toString();
						
						
						j.setJobName(jobName);
						
						jobHandler.update(j);
						LogUtils.debug("Job #"+j.getJobId()+" has updated it's name as "+jobName);
						int finalI = i;
						fileCopierExecutor.execute(new Runnable() {
							@Override
							public void run() {
								LogUtils.debug("copyRaster thread number #"+finalI+" (Job #"+j.getJobId()+") has started");
								
								StringBuilder sourceList = new StringBuilder("");
								StringBuilder destList = new StringBuilder("");
								for (int k = 0; k < Rip.rasterSMExtensions.length; k++) {
									String dstFilename = filename;
									if(".MB2".equals(Rip.rasterSMExtensions[k])){
										dstFilename = jobName;
									}
									//File srcFile = new File(srcDirectory, filename.concat(rasterExtensions[k]));
									//File dstFile = new File(dstDirectroy, dstFilename.concat(rasterExtensions[k]));
									 
									sourceList.append(srcDirectory.getAbsolutePath()).append(File.separator)
									.append(filename.concat(Rip.rasterSMExtensions[k])).append(" ");
									destList.append(dstDirectroy.getAbsolutePath()).append(File.separator)
									.append(dstFilename.concat(Rip.rasterSMExtensions[k])).append(" ");
								}
								
								doCopyFile(sourceList.toString(), destList.toString(), result, machine,totalFilesSize,roll, STD_MODE);
								
								latch.countDown();
							}
						});
						
					}
				}
				synchronized(slock){
						lock.decrement();
						if(!lock.exists()){
							locks.remove(location.getPath());
						}
				}
				i += 1;
			  }
			}
		}
		
		try{
			LogUtils.debug("Waiting for all ("+jobs.size()+") copyRaster threads to finish");
			latch.await();
		}catch(Exception e){
			LogUtils.error("Waiting for all jobs to finish copy failed for roll #"+roll.getRollNum(), e);
		}
		LogUtils.end();
		LogUtils.debug("All ("+jobs.size()+") copyRaster threads finished with result: isSuccessful="+result.successful);
		return result.successful;
	}
	
	public WFSLocation getRasterLocation(Job pressJob, String printingMode, String sheetSize) throws PersistenceException{
		LogUtils.debug("Job #"+pressJob.getJobId()+" and printing mode ["+printingMode+"] and sheet size ["+sheetSize+"]");
		Job hunkelerJob = jobHandler.getJobByStation(pressJob, StationCategory.Categories.PLOWFOLDER.toString());
		
		String hunkelerLine = hunkelerJob.getJobType().getId();
		String dataSupportType = null;

		if(JobType.JobTypes.PRINTING_FLYFOLDER.toString().equals(hunkelerLine)){
			dataSupportType = "FF".concat(printingMode);
			LogUtils.debug("Hunkeler job is a fly folder job, trying to find ratser file with FF prefix ");
		}else if(JobType.JobTypes.PRINTING_PLOWFOLDER.toString().equals(hunkelerLine)){
			dataSupportType = "PF".concat(printingMode);
			LogUtils.debug("Hunkeler job is a plow folder job, trying to find ratser file with PF"+printingMode+" prefix ");
		}else if(JobType.JobTypes.PRINTING_POPLINE.toString().equals(hunkelerLine)){
			Part parentPart = partHandler.getPartDAO().readTopPart(pressJob.getPartNum());
			if(parentPart == null){
				parentPart = partHandler.read(pressJob.getPartNum());
			}
			LogUtils.debug("TopPart for ["+pressJob.getPartNum()+"] is "+(parentPart != null? parentPart.getPartNum(): "not available"));
			if(parentPart != null && parentPart.getBindingType() != null) {
				if(Part.BindingTypes.LOOSELEAF.getName().equals(parentPart.getBindingType().getId())){
					dataSupportType = "PL".concat(printingMode);
					LogUtils.debug("Hunkeler job is a popline (loose-leaf) job, trying to find ratser file with PL prefix ");
				}else{
					dataSupportType = "PB".concat(printingMode);
					LogUtils.debug("Hunkeler job is a popline (perfect-bound) job, trying to find ratser file with PB prefix ");
				}
			}else{
				LogUtils.debug("Hunkeler job is a popline but couldn't determine top part to get binding type.");
			}
		}
		
		if(dataSupportType == null){
			return null;
		}
		if(sheetSize != null){
			dataSupportType = sheetSize.concat(dataSupportType);
		}
		
		LogUtils.debug("Looking for file path having pattern ["+dataSupportType+"] for book ["+pressJob.getPartIsbn()+"]");
		Part pr = partHandler.read(pressJob.getPartNum());
		Set<WFSDataSupport> onProdDataSupports = pr.getDataSupportsOnProd();
		
		LogUtils.debug("Job #"+pressJob.getJobId()+" has part  ["+pressJob.getPartNum()+"] and ("+pr.getDataSupportsOnProd().size()+") OnProd data supports");
		
		for(WFSDataSupport ds: onProdDataSupports){

			if(ds.getName().equals(WFSDataSupport.NAME_RASTER)){
				WFSLocation location = ds.getLocationdByType(WFSLocation.DESTINATION);
				String filename = location.getPath();
				LogUtils.debug("DataSuppotr #"+ds.getDataSupportId()+" is a raster DS: "+(location != null? filename: "Location is null"));
				if(!StringUtils.isBlank(filename) && filename.contains(dataSupportType))
					return location;
			}
		}
		
		//Reaching here means mostly old raster for EM not supporting best sheet count;
		if(sheetSize != null && dataSupportType.startsWith(sheetSize)){
			LogUtils.debug("No file  having pattern ["+dataSupportType+"] for book ["+pressJob.getPartIsbn()+"] was found, try old raster file names (No best sheet)");
			dataSupportType = dataSupportType.substring(sheetSize.length());
		
			for(WFSDataSupport ds: onProdDataSupports){
				if(ds.getName().equals(WFSDataSupport.NAME_RASTER) && ds.getDsType().equals(dataSupportType)){
					return ds.getLocationdByType(WFSLocation.DESTINATION);
				}
			}
		}
		
		LogUtils.debug("No file  having pattern ["+dataSupportType+"] for book ["+pressJob.getPartIsbn()+"] was found, returning null.");

		return null;
	}
	
	public WFSLocation getImposedFileLocation(Job pressJob) throws PersistenceException{
		LogUtils.debug("Getting imposed text file for job: "+pressJob.getJobId());
		Job hunkelerJob = jobHandler.getJobByStation(pressJob, StationCategory.Categories.PLOWFOLDER.toString());
		
		String hunkelerLine = hunkelerJob.getJobType().getId();
		String dataSupportType = null;
		
		if(JobType.JobTypes.PRINTING_FLYFOLDER.toString().equals(hunkelerLine)){
			dataSupportType = "FFSM";
		}else if(JobType.JobTypes.PRINTING_PLOWFOLDER.toString().equals(hunkelerLine)){
			dataSupportType = "PFSM";
		}else if(JobType.JobTypes.PRINTING_POPLINE.toString().equals(hunkelerLine)){
			Part parentPart = partHandler.getPartDAO().readTopPart(pressJob.getPartNum());
			if(parentPart == null){
				parentPart = partHandler.read(pressJob.getPartNum());
			}
			LogUtils.debug("TopPart for ["+pressJob.getPartNum()+"] is "+(parentPart != null? parentPart.getPartNum(): "not available"));
			if(parentPart != null && parentPart.getBindingType() != null) {
				if(Part.BindingTypes.LOOSELEAF.getName().equals(parentPart.getBindingType().getId())){
					dataSupportType = "PLSM";
					LogUtils.debug("Hunkeler job is a popline (loose-leaf) job, trying to find imposed file with PL prefix ");
				}else{
					dataSupportType = "PBSM";
					LogUtils.debug("Hunkeler job is a popline (perfect-bound) job, trying to find imposed file with PB prefix ");
				}
			}else{
				LogUtils.debug("Hunkeler job is a popline but couldn't determine top part to get binding type.");
			}
		}
		
		if(dataSupportType == null)
			return null;
		Part pr = partHandler.read(pressJob.getPartNum());
		Set<WFSDataSupport> onProdDataSupports = pr.getDataSupportsOnProd();
		LogUtils.debug("Looking for DS of type "+dataSupportType+" (Job: "+pressJob.getJobId()+")");
		for(WFSDataSupport ds: onProdDataSupports){
			if(ds.getName().equals(WFSDataSupport.NAME_IMPOSE) && ds.getDsType().equals(dataSupportType.concat(".PDF"))){
				
				WFSLocation l = ds.getLocationdByType(WFSLocation.DESTINATION);
				if(l != null)
					LogUtils.debug("DS "+ds.getDataSupportId()+" was found for Job: "+pressJob.getJobId()+": "+l.getPath());
				else
					LogUtils.debug("DS "+ds.getDataSupportId()+" was found for Job: "+pressJob.getJobId()+" but file location was missing");
				return l;
			}
		}
		LogUtils.debug("No DS of type "+dataSupportType+" was found for Job: "+pressJob.getJobId());
		return null;
	}
	
	/** 
	 * Assigning sections to machines and updating the statuses.
	 */
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public List<String> assignSectionsToMachine(String[] selectedSectionsForAssignment, String executingUserId) throws PersistenceException {
		try {
			List<String> sectionsNotAssigned = new ArrayList<String>();
			if(selectedSectionsForAssignment != null && selectedSectionsForAssignment.length > 1){
				String machineId = selectedSectionsForAssignment[0];
				Machine machine = machineDAO.read(machineId);
				int currentNbSections = 0;
				Set<CoverSection> sectionsAssigned = machine.getAssignedSections();
				if(machine != null && sectionsAssigned != null){
					currentNbSections = sectionsAssigned.size();
					for(CoverSection iter : sectionsAssigned){
						if(iter.getMachineOrdering() > currentNbSections){
							currentNbSections = iter.getMachineOrdering();
						}
					}
				}
				for(int i=1; i< selectedSectionsForAssignment.length; i++){
					CoverSection section = coverSectionDAO.read(Integer.parseInt(selectedSectionsForAssignment[i]));
					boolean exists = true;
					String nfPart =null;
					String cSection =null;
					boolean dsContains =false;
					int count = 0;
					// preCheck on imposed files
					for (CoverBatchJob cbjob : section.getJobs()) {
						Part pr = partHandler.read(cbjob.getJob().getPartNum());
						Set<WFSDataSupport> onProdDataSupports = pr.getDataSupportsOnProd();
						if (onProdDataSupports.isEmpty() || pr.getPartWorkFlowOnProd() == null) {
							sectionsNotAssigned.add(cbjob.getJob().getPartIsbn() + "/ Section: " + section.getCoverSectionName());
							return sectionsNotAssigned;
						}
						for (WFSDataSupport ds : onProdDataSupports) {
							if (ds.getName().equals(WFSDataSupport.NAME_IMPOSE)
									&& ds.getDsType().equalsIgnoreCase(WFSDataSupport.TYPE_COVER)) {
								dsContains =true;
								WFSLocation loc = ds.getLocationdByType(WFSLocation.DESTINATION);
								if (loc != null && loc.getPath() != null) {
									File f = new File(loc.getPath());
									if (!f.exists()) {
										exists = false;
										nfPart = cbjob.getJob().getPartIsbn();
										cSection = section.getCoverSectionName();
										count++;
										break;
									}
									else
									{
										exists = true;
									}	

								}
							}
						}
						if (exists == false && onProdDataSupports != null && !onProdDataSupports.isEmpty())
							sectionsNotAssigned.add(nfPart + ", ");
					}
					if (dsContains==false)
					{
						for (CoverBatchJob cbjob : section.getJobs()) 
						sectionsNotAssigned.add(cbjob.getJob().getPartIsbn());
						sectionsNotAssigned.add("/ Section: " + section.getCoverSectionName());
						return sectionsNotAssigned;
					}
					if (count != 0)
						sectionsNotAssigned.add("/ Section: " + cSection);

				}
			if (!sectionsNotAssigned.isEmpty())
			return sectionsNotAssigned;
				for(int i=1; i< selectedSectionsForAssignment.length; i++){
					CoverSection section = coverSectionDAO.read(Integer.parseInt(selectedSectionsForAssignment[i]));
					if(section != null && machine != null){
						section.setMachineId(machineId);
						section.setStatus(lookupDAO.read(SectionStatus.statuses.ASSIGNED.toString(), SectionStatus.class));
						// set the machineOrdering
						section.setMachineOrdering(i + currentNbSections);
						section.setCopyStatus(CoverSection.copyStatuses.IN_PROGRESS.toString());

						// set the machine id on all jobs of the section
						for(CoverBatchJob jb : section.getJobs()){
							jb.getJob().setMachineId(machineId);
							//[Commented 11/10/2018 in order to avoid job on prod loading on the machine and so the coverpress become faster]
							//jb.getJob().setJobStatus(lookupDAO.read(JobStatus.JobStatuses.ASSIGNED.toString(), JobStatus.class));
						}
						getCoverSectionDAO().update(section);
						machine.getAssignedSections().add(section);						
						this.getMachineDAO().update(machine);
						if (section != null && machine != null)
						prepareSection(machine, section, false);
					}
					Event event = new Event(EventTarget.MachineCopyFiles, false, null, section);
					notificationService.broadcast(event);
					
				}

			}
			return sectionsNotAssigned;
		} catch (Exception ex) {
			logger.error("Error occurred assigning sections to a machine : " + ex.getMessage(), ex);
			throw new PersistenceException(ex);	
		}
	}	
	
	/** 
	 * prepare the section: create and copy to machine.
	 */
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public void prepareSection(Machine machine,CoverSection section,Boolean recreate)
	{
		Runnable task = new Runnable() {
			@Override
			public void run() {
				List<String> errors = new ArrayList<String>();
				try {
					LogUtils.start();
					String mdropFolder = machine.getOcInputPath();
					long totalFilesSize = 0;
					if (mdropFolder != null && !mdropFolder.isEmpty()) {
						synchronized (mdropFolder) {
							File f = new File(mdropFolder);
							// create section if assigned
							SectionRunInfo srInfo = new SectionRunInfo();
							srInfo = coverSectionHandler.createSection(section, recreate);
							File originalfile = srInfo.getGenFile();
							if (originalfile != null) {
								totalFilesSize = originalfile.length();
								// f.mkdirs();
								String newName = section.getCoverSectionName();
								try {
									doCopySectionFile(originalfile, new File(mdropFolder, newName + ".pdf"),
											totalFilesSize, section);
								} catch (IOException e) {
									LogUtils.error("section file copy failed: " + f.getAbsolutePath(), e);
								}
							}
						}

					}

					LogUtils.end();

				} catch (Exception e) {
					LogUtils.error("Error occured while copying section files, SectionID: "
							+ section.getCoverSectionId(), e);
					/*Event event = new Event(EventTarget.MachineCopyFiles, true, e.getLocalizedMessage(),
							section.getCoverSectionId());
					notificationService.broadcast(event);*/
					section.setCopyStatus(copyStatuses.ERROR.getName());
					//section.setMachineId(null);
					section.setStatus(lookupDAO.read(SectionStatus.statuses.NEW.toString(), SectionStatus.class));
					try {
						coverSectionHandler.update(section);
					} catch (PersistenceException e1) {
						LogUtils.error("Could not update section #"+section.getCoverSectionId()+" in database: ", e1);
					}
					Event event = new Event(EventTarget.MachineCopyFiles, false, null, section);
					notificationService.broadcast(event);

				}
			}
		};
		executor.submit(task);
	}
	
	/** 
	 * Refresh machine status.
	 */
	public void refreshMachineStatus(String stationId) {
		try {
			Map<String, Object> ssePayload = new HashMap<String, Object>();
			//ssePayload.put("machineId", machine.getMachineId());
			ssePayload.put("stationId", stationId);
			Event event = new Event(EventTarget.MachineStatus, false, null, ssePayload);
			notificationService.broadcast(event);
		} catch (Exception ex) {
			Event event = new Event(EventTarget.MachineStatus, true, ex.getLocalizedMessage(), null);
			notificationService.broadcast(event);
		}
	}
	
	/** 
	 * Assigning jobs to machines and updating the statuses.
	 */
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void assignJobsToMachine(String[] selectedJobsForAssignment, String executingUserId) throws PersistenceException {
		try {
			Machine machine =null;
			if(selectedJobsForAssignment != null && selectedJobsForAssignment.length > 1){
				String machineId = selectedJobsForAssignment[0];
				machine = machineDAO.read(machineId);
				int currentNbJobs = 0;
				Set<Job> jobsAssigned = new HashSet<Job>(machine.getRunningAndAssignedJobs());
				if(machine != null && jobsAssigned != null){
					currentNbJobs = jobsAssigned.size();
					for(Job iterJob : jobsAssigned){
						if(iterJob.getMachineOrdering() != null && iterJob.getMachineOrdering() > currentNbJobs){
							currentNbJobs = iterJob.getMachineOrdering();
						}
					}
				}
				// for the jobs, assign them to the machine and update their status
				Job jobToSetAsCurrent = null;
				for(int i=1; i< selectedJobsForAssignment.length; i++){
					Job job = jobDAO.read(Integer.parseInt(selectedJobsForAssignment[i]));
					if(job != null){
						Part pr = partHandler.read(job.getPartNum());
						job.setMachineId(machineId);
						job.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.ASSIGNED.toString(), JobStatus.class));
						// TODO set the machineOrdering
						job.setMachineOrdering(i + currentNbJobs);
						getJobDAO().update(job);
						if(jobToSetAsCurrent == null){
							jobToSetAsCurrent = job;
						}
						// Copy cover Files to Ricoh HotFolder
						// this also applies to end sheet and dust jacket jobs where original file is copied to the cover press
						if(StationCategory.Categories.COVERPRESS.toString().equals(machine.getStationId())){
							String typeOfJob = pr.getPartNum().endsWith("J") ? "DustJacket" : (pr.getPartNum().endsWith("E") ? "EndSheet" : "Cover");
							LogUtils.debug("I'm copying " + typeOfJob + " file to machine: " + machine.getStationId());
							this.setCopyCoverRasterFilesValue(getLookupDAO().read("COPYCOVERRASTERFILES", Preference.class));
							if (!"false".equalsIgnoreCase(this.getCopyCoverRasterFilesValue().getName())){
								LogUtils.start();
								String ricohInputFolder = System.getProperty(ConfigurationConstants.DIR_RICOH_INPUT);
								Set<WFSDataSupport> onProdDataSupports = pr.getDataSupportsOnProd();
								File coverImposedFile = null;
								File coverOriginFile = null;
								String dsName = WFSDataSupport.NAME_IMPOSE;
								
								if(pr.getPartNum().endsWith("J") || pr.getPartNum().endsWith("E")){
									dsName = WFSDataSupport.NAME_DOWNLOAD;
								}else{
									Preference pref = lookupDAO.read("DONOTIMPOSECOVERFORCBL", Preference.class);
									if(pref != null && "true".equals(pref.getName())){
										Part parentPart = partHandler.getPartDAO().readTopPart(job.getPartNum());
										if(parentPart == null){
											parentPart = pr;
										}
										if(Part.BindingTypes.CASEBOUND.getName().equals(parentPart.getBindingType().getId())){
											dsName = WFSDataSupport.NAME_DOWNLOAD;
										}
									}
								}
								for(WFSDataSupport ds: onProdDataSupports){
									if(coverImposedFile == null && ds.getName().equals(WFSDataSupport.NAME_IMPOSE) && ds.getDsType().equalsIgnoreCase(WFSDataSupport.TYPE_COVER)){
										coverImposedFile = new File(ds.getLocationdByType("Destination").getPath());
									}
									if(WFSDataSupport.NAME_DOWNLOAD.equals(dsName)){
										if(coverOriginFile == null && ds.getName().equals(dsName)){
											coverOriginFile = new File(ds.getLocationdByType("Destination").getPath());
										}
									}
								}
								
								Lamination l = pr.getLamination();
								String lamination = "NA";
								if(l != null){
									lamination = l.getId().substring(0,2);
								}
								
								String format = "";
								if(coverImposedFile != null && coverImposedFile.exists()){
									format = coverImposedFile.getName().substring(0, coverImposedFile.getName().indexOf('_'));
								}
								
								if(WFSDataSupport.NAME_IMPOSE.equals(dsName)){
									if(coverImposedFile != null && coverImposedFile.exists()){
										LogUtils.debug("I'm copying the imposed cover file " + coverImposedFile.getAbsolutePath() + " to cover Printer at " + ricohInputFolder);
										FileUtils.copyFile(coverImposedFile, new File(ricohInputFolder + File.separator
												+ format.concat("_")
												+ lamination.concat("_")
												+ job.getJobId() + ".pdf"));
									}else{
										LogUtils.debug("Cover pdf file is wether null or does not exist: " + coverImposedFile);
									}
								}else{
									if(coverOriginFile != null && coverOriginFile.exists()){
										LogUtils.debug("I'm copying the original " + typeOfJob + " file " + coverOriginFile.getAbsolutePath() + " to cover Printer at " + ricohInputFolder);
										if(pr.getPartNum().endsWith("J") || pr.getPartNum().endsWith("E")){
											FileUtils.copyFile(coverOriginFile, new File(ricohInputFolder + File.separator
													+ coverOriginFile.getName()));
										}else{
											FileUtils.copyFile(coverOriginFile, new File(ricohInputFolder + File.separator
													+ format.concat("_")
													+ lamination.concat("_")
													+ job.getJobId() + ".pdf"));
										}
									}else{
										LogUtils.debug(typeOfJob + " pdf file is wether null or does not exist: " + coverOriginFile);
									}
								}
								LogUtils.end();
							}
						}
					}
				}
				// if no current job yet on the machine or this is the only job assigned; assign the first job...
				if(machine.getCurrentJob() == null || jobsAssigned.size() == 0){
					if(jobToSetAsCurrent != null){
						machine.setCurrentJob(jobToSetAsCurrent);
						machineDAO.update(machine);
					}
				}
			}
			Map<String, Object> ssePayload=new HashMap<String, Object>();
			ssePayload.put("assignedJobs", selectedJobsForAssignment);
			ssePayload.put("stationId", machine.getStationId());
			Event event=new Event(EventTarget.Job, false, null, ssePayload);
			notificationService.broadcast(event);
		} catch (Exception ex) {
			Event event=new Event(EventTarget.Job, true, ex.getLocalizedMessage(), null);
			notificationService.broadcast(event);
			logger.error("Error occurred assigning jobs to a machine : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public String startResumeMachine(String machineId, String selectedModeOption, String executingUserId) throws PersistenceException{
		String errorText = null;
		Machine machine = read(machineId); 
		Log oldLog = null;
		Log newLog = new Log();
		if(machine != null){
			Station station = stationDAO.read(machine.getStationId());
			if(station != null){
				CoverSection sectionOnProd = machine.getCoverSectionOnProd();
				Roll rollOnProd = machine.getRollOnProd();
				Job jobOnProd = machine.getJobOnProd();
				if(Station.inputTypes.Roll.toString().equals(station.getInputType())){
					if(rollOnProd != null){
						// if press and machine works on Epac mode then start jobs automatically
						Printer printer = printersHandler.getPrinter(machine.getMachineId());
						if(StationCategory.Categories.PRESS.toString().equals(station.getStationCategoryId()) 
								&& printer != null && printer.isEpacMode()){
							// TODO make sure getIsEmfMode is false
							//if(Boolean.FALSE.equals(machine.getIsEmfMode()) && Boolean.TRUE.equals(machine.getIsEpacModePrintingActive())){
							if( printer.isReady() && Roll.copyStatuses.FINISHED.toString().equals(rollOnProd.getCopyStatus())){
								PrintingJobRequest request = new PrintingJobRequest();
								request.setNumberOfRefreshPattern(0);
								request.setDoPurge(0);
								
								List<PrintingJob> printJobList = new ArrayList<>();
								
								String printingTest = System.getProperty(ConfigurationConstants.PRINTING_TEST);
								String rasterJobId = "";
								int i = 1;
								Set<Job> jobs = rollOnProd.getJobs();
								String sheetSize = printingTimeCalculator.getBestSheetHeight(rollOnProd);
								boolean rollStarted = false;
								for(Job job : jobs){
									if(job.getJobStatus().getId().equals(JobStatuses.COMPLETE.getName()))
										continue;
									if(job.getQuantityProduced() > 0){
										rollStarted = true;
									}
									PrintingJob pj = new PrintingJob();
									pj.setNumberOfCopies(job.getQuantityNeeded() - Float.valueOf(job.getQuantityProduced()).intValue());
									if("true".equals(printingTest)){
										rasterJobId = Integer.toString(i);
									}else{
										if(!StringUtils.isBlank(job.getJobName())){
											rasterJobId = job.getJobName();
										}else{
											WFSLocation loc = getRasterLocation(job, EPAC_MODE, sheetSize);
											if(loc != null){
												rasterJobId = String.valueOf(job.getJobId()).concat(loc.getFileName());
											}
										}
									}
									pj.setId(rasterJobId);
									printJobList.add(pj);
									i++;
								}
								request.setPrintingJobList(printJobList);
								
								if(!rollStarted){
									Preference dummyBookPreference = lookupDAO.read("DUMMYBOOKLENGTH", Preference.class);
									if(dummyBookPreference != null && !StringUtils.isEmpty(dummyBookPreference.getName())){
										Float dummyBookPreferenceValue = Float.parseFloat(dummyBookPreference.getName());
										this.setUnitValue(getLookupDAO().read("UNITSYSTEM", Preference.class));
										if(getUnitValue() != null && UNIT_US.equals(getUnitValue().getName())){
											dummyBookPreferenceValue = Format.feet2m(dummyBookPreferenceValue);
										}
										request.setDummyBookLength(Math.round(dummyBookPreferenceValue));
									}
								}
								
								// FIXME:CANNOT rely on LogEvent to decide whether to start or resume printing 
								//   Need to find another solution
								ObjectMapper mapper = new ObjectMapper();
								try {
									String strRequest = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
									LogUtils.debug("Sending print request to OutputController: "+ strRequest );
								} catch (JsonProcessingException e) {
									LogUtils.error("Could not serialize Epac mode print request", e);
								}
								
								Object obj = printer.print(request);
								try {
									String strResult = null;
									
									if(obj != null)
										strResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
									
									LogUtils.debug("Print request returned: "+ strResult );
								} catch (JsonProcessingException e) {
									LogUtils.error("Could not serialize Epac mode print request", e);
								}
								if(obj != null && obj instanceof ErrorList){
									ErrorList errors = (ErrorList)obj;
									String message  = "";
									for(jp.co.fujifilm.xmf.oc.model.printing.Error e : errors.getErrorList()){
										message += e.getMessage(); 
										
									}
									errorText = message;
									LogUtils.error("Print request failed: "+ message );
									return errorText;
								}else if(obj != null){
									LogUtils.error("Print request failed and didn't return ErrorList: "+ obj.getClass());
								}
								/*
								//Fixed
								if(!rollOnProd.isRollJobsStarted()){
									
								}else{
									LogUtils.debug("Roll #"+rollOnProd.getRollNum()+" has already started, resume printing in Epac mode");
									if(!printer.resume()){
										LogUtils.debug("Roll #"+rollOnProd.getRollNum()+" failed to resume printing in Epac mode");
										errorText = "Printer " + machine.getName() + " could not resume printing";
									}
								}*/
								//printer.print(request);
							}else{
								if(!printer.isReady()){
									// TODO may see if the jobs are done printing then no need to check for readiness as the printer may become not ready
									// since it is done printing... so check on the jobs quantities produced if all done then allow start
									return "Printer " + machine.getName() + " is not ready: "+printer.getPrinterStatus();
								}else if (!Roll.copyStatuses.FINISHED.toString().equals(rollOnProd.getCopyStatus())){
									return "Roll #"+rollOnProd.getRollNum()+" on " + machine.getName() + " has copy status "+rollOnProd.getCopyStatus();
								}
								
							}
						}
						newLog.setRollId(rollOnProd.getRollId());
						newLog.setRollLength(rollOnProd.getLength());
						/*if(machine.getCurrentJob() == null	|| (machine.getCurrentJob().getRollId() != null && 
								!machine.getCurrentJob().getRollId().equals(rollOnProd.getRollId()))){
							Job aJob = !rollOnProd.getJobs().isEmpty() ? rollOnProd.getJobs().iterator().next() : null;
							if(aJob != null){
								aJob.setJobStatus(lookupHandler.read(JobStatus.JobStatuses.RUNNING.toString(), JobStatus.class));
								jobHandler.update(aJob);
								machine.setCurrentJob(aJob);
							}
						}else{*/// if same roll on prod set machine current job as the first one not complete
							for(Job iterJob : rollOnProd.getJobs()){
								if(!JobStatus.JobStatuses.COMPLETE.toString().equals(iterJob.getJobStatus().getId())){
									iterJob.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.RUNNING.toString(), JobStatus.class));
									jobHandler.update(iterJob);
									machine.setCurrentJob(iterJob);
									break;
								}
							}
						//}
						// if plow folder and mode on each job different from this one from parameter then update it
						//also when start roll on hunkeler, activate finishing job if required
						if(StationCategory.Categories.PLOWFOLDER.toString().equals(station.getStationCategoryId())){
							for(Job iterJob : rollOnProd.getJobs()){
								if(!selectedModeOption.equals(iterJob.getProductionMode())){
									iterJob.setProductionMode(selectedModeOption);
									jobDAO.update(iterJob);
									Order or = orderDAO.read(iterJob.getOrderId());
									or.setProductionMode(selectedModeOption);
									orderDAO.update(or);
								}
							}
							Preference finishingPreference = lookupDAO.read("ACTIVATEBINDERFORSTANLY", Preference.class);
							if(finishingPreference != null && "true".equals(finishingPreference.getName()) && Log.LogEvent.START.toString().equals(newLog.getEvent())){
								for(Job hunkelerJob : rollOnProd.getJobs()){
									JobSearchBean jobSearchBean = new JobSearchBean();
									jobSearchBean.setOrderId(hunkelerJob.getOrderId());
									jobSearchBean.setPartFamily((hunkelerJob.getPartNum().endsWith("T") || hunkelerJob.getPartNum().endsWith("C")) ? (hunkelerJob.getPartNum().substring(0, hunkelerJob.getPartNum().length() - 1)) : hunkelerJob.getPartNum());
									
									jobSearchBean.setStationId(StationCategory.Categories.COVERPRESS.toString());
									jobSearchBean.setSplitLevel(hunkelerJob.getSplitLevel());
									jobSearchBean.setStatusesIn(Arrays.asList(JobStatus.JobStatuses.COMPLETE.toString(), JobStatus.JobStatuses.COMPLETE_PARTIAL.toString()));
									List<Job> correspondingJobs = jobDAO.readAll(jobSearchBean);
									if(!correspondingJobs.isEmpty()){
										jobSearchBean.setStationId(StationCategory.Categories.BINDER.toString());
										jobSearchBean.setStatusesIn(Arrays.asList(JobStatus.JobStatuses.NEW.toString()));
										correspondingJobs = jobDAO.readAll(jobSearchBean);
										if(!correspondingJobs.isEmpty()){
											correspondingJobs.get(0).setJobStatus(lookupDAO.read(JobStatus.JobStatuses.SCHEDULED.toString(), JobStatus.class));
											correspondingJobs.get(0).setLastUpdateDate(new Date());
											correspondingJobs.get(0).setLastUpdateId(executingUserId);
											jobDAO.update(correspondingJobs.get(0));
										}
									}
								}
							}
						}
				
					}
					//set the roll/Job (that is supposed to be on production) status to 'ONPROD' in case it is still only 'ASSIGNED'
					// also set the machine current job
					if(rollOnProd != null && !RollStatus.statuses.ONPROD.toString().equals(rollOnProd.getStatus().getId())){
						rollOnProd.setStatus(lookupDAO.read(RollStatus.statuses.ONPROD.toString(), RollStatus.class));
						rollHandler.update(rollOnProd);
					}
				}
				else if (Station.inputTypes.Batch.toString().equals(station.getInputType())) {
					if (sectionOnProd != null){
							if(!SectionStatus.statuses.ONPROD.toString().equals(sectionOnProd.getStatus().getId())) {
						// update section status to ONPROD
						sectionOnProd.setStatus(
								lookupDAO.read(SectionStatus.statuses.ONPROD.toString(), SectionStatus.class));
						sectionHandler.update(sectionOnProd);
						// update batch status to ONPROD
						CoverBatch res = getCoverBatchDAO().read(sectionOnProd.getBatchId());
						if (res != null) {
							res.setStatus(lookupDAO.read(BatchStatus.statuses.ONPROD.toString(), BatchStatus.class));
							getCoverBatchDAO().update(res);
						}
					}
						newLog.setSectionId(sectionOnProd.getCoverSectionId());
						for (CoverBatchJob iterJob : sectionOnProd.getJobs()) {
							if (!JobStatus.JobStatuses.COMPLETE.toString()
									.equals(iterJob.getJob().getJobStatus().getId())) {
								iterJob.getJob().setJobStatus(
										lookupDAO.read(JobStatus.JobStatuses.RUNNING.toString(), JobStatus.class));
								jobHandler.update(iterJob.getJob());
							}
						}
					}
				}
				else{
					if(jobOnProd != null && !JobStatus.JobStatuses.RUNNING.toString().equals(jobOnProd.getJobStatus().getId())){
						jobOnProd.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.RUNNING.toString(), JobStatus.class));
						jobHandler.update(jobOnProd);
					}
					if(jobOnProd != null){
						newLog.setCurrentJobId(jobOnProd.getJobId());
						if(machine.getCurrentJob() == null || !machine.getCurrentJob().equals(jobOnProd.getJobId())){
							machine.setCurrentJob(jobOnProd);
						}
					}
				}
				
				
				if(machine.getLogs() != null && !machine.getLogs().isEmpty()){
					oldLog  = machine.getLogs().iterator().next();
					if(oldLog != null && oldLog.getFinishTime() == null){// only set it for stopped, paused log events, and not the complete event log entry
						oldLog.setFinishTime(new Date());
						logDAO.update(oldLog);
					}
				}
				
				newLog.setMachineId(machineId);
				if(oldLog != null){
					if(Log.LogEvent.PAUSE.toString().equals(oldLog.getEvent())){
						newLog.setEvent(Log.LogEvent.RESUME.toString());
					}else{
						newLog.setEvent(Log.LogEvent.START.toString());
					}
					newLog.setCounterFeet(oldLog.getCounterFeet());
				}else{
					newLog.setEvent(Log.LogEvent.START.toString());
				}
				newLog.setLogResult(lookupDAO.read(LogResult.results.RUNNING.toString(), LogResult.class));
				//newLog.setLogCause(lookupHandler.read(LogCause.causes.ONOFF.toString(), LogCause.class));
				newLog.setStartTime(new Date());
				
				
			}
			if(errorText == null){
				machine.setStatus(lookupDAO.read(MachineStatus.statuses.RUNNING.toString(), MachineStatus.class));
				machine.setLastUpdateDate(new Date());
				machine.setLastUpdateId(executingUserId);
				update(machine);
				newLog.setLogCause(lookupDAO.read(LogCause.causes.USERDECISION.toString(), LogCause.class));
				newLog.setCreatedDate(new Date());
				newLog.setCreatorId(executingUserId);
				logDAO.create(newLog);
				
				Map<String, Object> ssePayload=new HashMap<String, Object>();
				ssePayload.put("machineId", machine.getMachineId());
				ssePayload.put("stationId", machine.getStationId());
				ssePayload.put("status", machine.getStatus());
				Event event=new Event(EventTarget.MachineStatus, false, null, ssePayload);
				notificationService.broadcast(event);
			}
		}
		
		return errorText;
	}
	
	/** 
	 * Calls the corresponding delete method on the MachineDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public boolean delete(Machine bean) throws PersistenceException {
		try {
			return getMachineDAO().delete(bean);
		} catch (Exception ex) {
			logger.error("Error occurred deleting a Machine with id '" + (bean == null ? null : bean.getMachineId()) + "' : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}

	/** 
	 * A convenience method which calls readAll(MachineSearchBean) with a
	 * null search bean. 
	 *
	 * @see #readAll(MachineSearchBean)
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Machine> readAll() throws PersistenceException{
		return this.readAll(null);    
	}
	
	/** 
	 * Calls the corresponding readAll method on the MachineDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Machine> readAll(MachineSearchBean searchBean) throws PersistenceException{
		try{
			return getMachineDAO().readAll(searchBean);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a list of Machines : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Calls the corresponding read method on the MachineDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Machine read(String machineId) throws PersistenceException{
		try{
			Machine machine = null ;
			if(machineId.contains("PALLETT")) {
				machine =  getMachineDAO().fetchShippingMachines(machineId);
			}
			else machine = getMachineDAO().read(machineId);
			
			
			// for the shipping station, read the order of the current job as it is needed when display shipping station
			if(machine != null && StationCategory.Categories.SHIPPING.toString().equals(machine.getStationId())
					&& machine.getCurrentJob() != null){
				Order theOrder = orderDAO.read(machine.getCurrentJob().getOrderId());
				logger.debug("order Of current Job is : "+theOrder.getOrderId());
				machine.getCurrentJob().setOrder(theOrder);
			}
			return machine;
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a Machine with id '" + machineId + "' : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Calls the corresponding read count method on the MachineDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Integer readCount(String stationId, String color) throws PersistenceException{
		try{
			return getMachineDAO().machinesCount(stationId, color);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving machines count fr station '" + stationId + "' : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * Get default machine speed
	 *
	 */
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Float getDefaultMachineSpeed(String stationId)  throws PersistenceException{
		Float machineSpeed = (float) 1;
		String preferenceName = stationId.concat("SPEED");
		try {
			Preference p = lookupDAO.read(preferenceName, Preference.class);
			if(p != null){
				machineSpeed = Float.parseFloat(p.getName());
			}
			/*if(StationCategory.Categories.PRESS.toString().equals(stationId) ||
					StationCategory.Categories.PLOWFOLDER.toString().equals(stationId)){
				if(this.getUnitValue() == null){
					this.setUnitValue(getLookupDAO().read("UNITSYSTEM", Preference.class));
					if(this.getUnitValue() == null){
						this.setUnitValue(new Preference());
						this.getUnitValue().setName(UNIT_US);
					}
				}
				if(UNIT_US.equals(unitValue.getName())){
					
				}else{
					machineSpeed = Format.feet2m(machineSpeed);
				}
			}*/
		} catch (NumberFormatException nfe) {
			if(StationCategory.Categories.PRESS.toString().equals(stationId)){
				if(this.getUnitValue() == null){
					this.setUnitValue(getLookupDAO().read("UNITSYSTEM", Preference.class));
					if(this.getUnitValue() == null){
						this.setUnitValue(new Preference());
						this.getUnitValue().setName(UNIT_US);
					}
				}
				if(UNIT_US.equals(unitValue.getName())){
					machineSpeed = (float) 20000; // TODO; make sure this is feet/h
				}else{
					machineSpeed = (float) 6000; // TODO; make sure this is meter/h
				}
			}
		}
		return machineSpeed == 0 ? (float)1 : machineSpeed;
	}
	         	
	/**
	 * @return the MachineDAO
	 */
	public MachineDAO getMachineDAO() {
		return machineDAO;
	}

	/**
	 * @param dao the MachineDAO to set
	 */
	public void setMachineDAO(MachineDAO dao) {
		this.machineDAO = dao;
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
	 * @return the jobDAO
	 */
	public JobDAO getJobDAO() {
		return jobDAO;
	}

	/**
	 * @param jobDAO the jobDAO to set
	 */
	public void setJobDAO(JobDAO jobDAO) {
		this.jobDAO = jobDAO;
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
	 * @return the copyCoverRasterFilesValue
	 */
	public Preference getCopyCoverRasterFilesValue() {
		return copyCoverRasterFilesValue;
	}

	/**
	 * @param copyCoverRasterFilesValue the copyCoverRasterFilesValue to set
	 */
	public void setCopyCoverRasterFilesValue(Preference copyCoverRasterFilesValue) {
		this.copyCoverRasterFilesValue = copyCoverRasterFilesValue;
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
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Machine> fetchALLMachineShipping(){
		MachineSearchBean bean = new MachineSearchBean();
		bean.setStationId("SHIPPING");
		List<Machine> machines = machineDAO.readAll(bean);
		return machines;
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
	 * @return the logDAO
	 */
	public LogDAO getLogDAO() {
		return logDAO;
	}

	/**
	 * @param logDAO the logDAO to set
	 */
	public void setLogDAO(LogDAO logDAO) {
		this.logDAO = logDAO;
	}

	/**
	 * @return the stationDAO
	 */
	public StationDAO getStationDAO() {
		return stationDAO;
	}

	/**
	 * @param stationDAO the stationDAO to set
	 */
	public void setStationDAO(StationDAO stationDAO) {
		this.stationDAO = stationDAO;
	}

	public CoverBatchDAO getCoverBatchDAO() {
		return coverBatchDAO;
	}


	public void setCoverBatchDAO(CoverBatchDAO coverBatchDAO) {
		this.coverBatchDAO = coverBatchDAO;
	}


	public CoverSectionDAO getCoverSectionDAO() {
		return coverSectionDAO;
	}


	public void setCoverSectionDAO(CoverSectionDAO coverSectionDAO) {
		this.coverSectionDAO = coverSectionDAO;
	}


	/**
	 * @return the orderDAO
	 */
	public OrderDAO getOrderDAO() {
		return orderDAO;
	}

	/**
	 * @param orderDAO the orderDAO to set
	 */
	public void setOrderDAO(OrderDAO orderDAO) {
		this.orderDAO = orderDAO;
	}
	

	
	public void doCopyFileSsh(String[] srcListArray, String[] destListArray, CopyResult result, long totalFilesSize, Roll roll) throws Exception {

		// --------------------------------------- SSH -------------------------------------------------------------------------
			
			LogUtils.debug("Copy whith ssh");
			
			ExecutorService exService = Executors.newFixedThreadPool(3);
			ExecutorCompletionService<Long> ecs =  new ExecutorCompletionService<Long>(exService);
			
			int validFiles = 0;
			for(int i = 0; i < srcListArray.length; i++){
				
				String srcFileString = srcListArray[i];
				String destFileString = destListArray[i];
				
				if(srcFileString == null || srcFileString.isEmpty())continue;
				if(destFileString == null || destFileString.isEmpty())continue;
				
				
				ecs.submit(new NasSshHandler(srcFileString, destFileString, sshConnectionsManager));
				validFiles++;
			}
				

			for(int i = 0; i < validFiles; i++){
				
				Future<Long> sshFuture = ecs.take();
				
				Long fileSize = sshFuture.get();
				
				LogUtils.debug("result fileSize :" + fileSize);
				
				if(fileSize == null) {
            		throw new Exception("Nas : Unable to copy file ");            		
				}else {					
					long copiedFileSize = fileSize;		
					result.copiedSize +=copiedFileSize;
	                Float progressValue = ((float) result.copiedSize / (float)totalFilesSize)*100;
	                roll.setCopyStatus(String.valueOf(progressValue.longValue()));	               
	                Event event = new Event(EventTarget.MachineCopyFiles, false, null, roll);
					notificationService.broadcast(event);
					
				}
				
				
				
			}
			
			
			exService.shutdown();
			exService.awaitTermination(100, TimeUnit.MINUTES);

		// --------------------------------------- END SSH -------------------------------------------------------------------------
	}
	
	public  void doCopyFile(String srcList, String destList, CopyResult result,Machine machine,long totalFilesSize,Roll roll, String mode) {
		
		LogUtils.debug("Copy files: "+srcList+" to: "+destList);
		
		try{		
			
			if(STD_MODE.equals(mode)) {
				LogUtils.debug("Copy files: STANDARD MODE (--> SSH)");
				
				String[] srcListArray = srcList.split(" ");
				String[] destListArray = destList.split(" ");
				
				if(srcListArray.length != destListArray.length){
					throw new Exception("Copy files; source and destination files are not matching...");
			    }
				
				doCopyFileSsh(srcListArray, destListArray, result, totalFilesSize, roll);
			}else {
				LogUtils.debug("Copy files: EPAC MODE (--> COPY FROM OC)");
				doCopyFileRemotely(srcList,destList,result, machine, totalFilesSize, roll);
			}
		} catch (Exception e) {
			
			result.successful = false; 
			String message = e.getMessage();
			if(message == null)
				message = "Error occured while copying raster file for roll: " + roll.getRollId() + ". Please try again.";
			roll.setCopyStatus(Roll.copyStatuses.ERROR.toString());
			Event event=new Event(EventTarget.MachineCopyFiles, true, message, roll);
			notificationService.broadcast(event);
			
			//roll.setMachineId(null);
			//roll.setStatus(lookupDAO.read(RollStatus.statuses.SCHEDULED.toString(), RollStatus.class));
			LogUtils.error("Raster file copy failed: "+message);
			try {
				getRollHandler().update(roll);
			} catch (PersistenceException e1) {
				LogUtils.error("Could not update roll #"+roll.getRollId()+" in database: ", e1);
			}
		}	
	}
	
	private void doCopyFileLocally(File srcFile, File destFile, CopyResult result, long totalFilesSize, Roll roll) throws Exception{
		//-------------------------------------------- NORMAL --------------------------------------------------------------------------
		LogUtils.debug("Copy through pacex");
		
		long ONE_KB = 1024;
		 long ONE_MB = ONE_KB * ONE_KB;
		  long FILE_COPY_BUFFER_SIZE = ONE_MB * 30;
		  
	        if (destFile.exists() && destFile.isDirectory()) {
	            throw new IOException("Destination '" + destFile + "' exists but is a directory");
	        }

	        boolean alreadyCopied = false;
	        try {
	        	alreadyCopied = com.epac.cap.utils.FileUtils.compareFiles(srcFile.toPath(), destFile.toPath());
			} catch (Exception e) {}
	        
	        if(alreadyCopied){
	        	LogUtils.debug("file "+destFile.getPath()+" already exists and identical to source, ignored");
	        	result.copiedSize +=java.nio.file.Files.size(srcFile.toPath());
	        	Float progressValue = ((float) result.copiedSize / (float)totalFilesSize)*100;
	               
                roll.setCopyStatus(String.valueOf(progressValue.longValue()));
                Event event = new Event(EventTarget.MachineCopyFiles, false, null, roll);
				notificationService.broadcast(event);
				return;
	        }
	        
	        
	      
	        FileInputStream fis = null;
	        FileOutputStream fos = null;
	        FileChannel input = null;
	        FileChannel output = null;
	        try {
	            fis = new FileInputStream(srcFile);
	            fos = new FileOutputStream(destFile);
	            input  = fis.getChannel();
	            output = fos.getChannel();
	            long size = input.size();
	            long pos = 0;
	            long count = 0;
	            while (pos < size) {
	                count = size - pos > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : size - pos;
	                pos += output.transferFrom(input, pos, count);
	                result.copiedSize += count;
	                Float progressValue = ((float) result.copiedSize / (float)totalFilesSize)*100;
	               
	                roll.setCopyStatus(String.valueOf(progressValue.longValue()));
	                Event event = new Event(EventTarget.MachineCopyFiles, false, null, roll);
					notificationService.broadcast(event);
	            }
	            
	           
	        } finally {
	            IOUtils.closeQuietly(output);
	            IOUtils.closeQuietly(fos);
	            IOUtils.closeQuietly(input);
	            IOUtils.closeQuietly(fis);
	        }

	        
	        // wait for the last chunk of data to get written in the destination file 
	        try {
				Thread.sleep(10000L);
			} catch (InterruptedException e) {}
	        if (srcFile.length() != destFile.length()) {
	            throw new IOException("Failed to copy full contents from '" +
	                    srcFile + "' to '" + destFile + "'");
	        }
	        
	      //-------------------------------------------- END NORMAL --------------------------------------------------------------------------
		
	}


	public long  calcSizeFileToCopy(Roll roll, String mode, String sheetSize){
		long size = 0L;
		for (Job j : roll.getJobs()){
			WFSLocation loc;
			try {
				loc = getRasterLocation(j, mode, sheetSize);
				
				if(EPAC_MODE.equals(mode)){
					
					if(loc != null && loc.getPath() != null){
						File f = new File(loc.getPath());
						File [] files = f.listFiles();
						for(File file : files){
							size +=file.length();
						}
					}
				}else if(STD_MODE.equals(mode)){
					File f = new File(loc.getPath());
					if(f.exists()){
						File srcDirectory = f.getParentFile();
						String filename = Files.getNameWithoutExtension(f.getName());
						
						for (int k = 0; k < Rip.rasterSMExtensions.length; k++) {
							File srcFile = new File(srcDirectory, filename.concat(Rip.rasterSMExtensions[k]));
							size += srcFile.length();
						}	
					}
				}
				
			} catch (PersistenceException e) {
				LogUtils.error("Error occured while calculating raster files size");
			}
		}
		return size;
	}
	
	@Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.REQUIRED)
	public synchronized void doCopySectionFile(File srcFile, File destFile, long totalFilesSize, CoverSection section)
			throws IOException {
		LogUtils.debug("Copy file: " + srcFile.getAbsolutePath() + " to " + destFile.getAbsolutePath());
		long copiedSize = 0;
		long ONE_KB = 1024;
		long FILE_COPY_BUFFER_SIZE = ONE_KB * ONE_KB * 30;

		if (destFile.exists() && destFile.isDirectory()) {
			throw new IOException("Destination '" + destFile + "' exists but is a directory");
		}

		boolean alreadyCopied = false;
		try {
			alreadyCopied = com.epac.cap.utils.FileUtils.compareFiles(srcFile.toPath(), destFile.toPath());
		} catch (Exception e) {
		}

		if (alreadyCopied)
			return;

		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel input = null;
		FileChannel output = null;
		try {
			fis = new FileInputStream(srcFile);
			fos = new FileOutputStream(destFile);
			input = fis.getChannel();
			output = fos.getChannel();
			long size = input.size();
			long pos = 0;
			long count = 0;
			while (pos < size) {
				count = size - pos > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : size - pos;
				pos += output.transferFrom(input, pos, count);
				copiedSize += count;
				Float progressValue = ((float) copiedSize / (float) totalFilesSize) * 100;

				section.setCopyStatus(String.valueOf(progressValue.longValue()));
				Event event = new Event(EventTarget.MachineCopyFiles, false, null, section);
				notificationService.broadcast(event);
			}
			// set copyStatus of section to finished
			if (pos == size) {
				section.setCopyStatus(copyStatuses.FINISHED.getName());
				try {
					coverSectionHandler.update(section);
				} catch (PersistenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Event event = new Event(EventTarget.MachineCopyFiles, false, null, section);
				notificationService.broadcast(event);
			}

		} finally {
			IOUtils.closeQuietly(output);
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(fis);
		}

		// wait for the last chunk of data to get written in the destination
		// file
		try {
			Thread.sleep(6000L);
		} catch (InterruptedException e) {
		}
		if (srcFile.length() != destFile.length()) {
			// throw new IOException("Failed to copy full contents of section
			// ["+section.getCoverSectionName()+"] from '" +
			// srcFile + "' to '" + destFile + "'");
			section.setCopyStatus(copyStatuses.ERROR.getName());
			try {
				coverSectionHandler.update(section);
			} catch (PersistenceException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			Event event = new Event(EventTarget.MachineCopyFiles, false, null, section);
			notificationService.broadcast(event);
			throw new IOException("Failed to copy full contents of section [" + section.getCoverSectionName()
					+ "] from '" + srcFile + "' to '" + destFile + "'");
		}
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public  List<Machine> fetchMachines(){
		return machineDAO.fetchMachines();
		
	}
	
	@SuppressWarnings("unchecked")
	public int copyfileNas(String srcs , String dests, Machine machine){
		
		Printer printer = printersHandler.getPrinter(machine.getMachineId());

		LogUtils.debug("Copy file request sent: " + srcs + "to "+machine.getIpAddress());
		Map<String,Object> strResult = new LinkedHashMap<>();
        int size = 0;

		//try {
	        ResponseEntity<Map> response = printer.copy(srcs, dests);

					strResult =  response.getBody();//mapper.readValue(obj.toString(), HashMap.class);
					if(strResult.get("error").equals(false)){
						LogUtils.debug("Copy raster success: " );
						size = Integer.parseInt(strResult.get("message").toString());
					}else{
						LogUtils.debug("Copy request returned: "+ strResult.get("message") );
					}
			
				   
			//} catch (Exception e) {
				//LogUtils.error("Could not serialize copy  raster request", e);
			//}
			
			
			return size;
	}
	
	public synchronized void doCopyFileRemotely(String srcs, String dests, CopyResult result, Machine machine,long totalFilesSize, Roll roll) throws IOException {

		

			LogUtils.debug("Copy whith OC");

			int size = copyfileNas(srcs, dests,machine);

			
					LogUtils.debug("Copy File server returned success: File: " + srcs +" with size "+size+"/"+totalFilesSize );
					result.copiedSize += size;
					Float progressValue = (float) (size/ totalFilesSize) * 100;
					Roll rollEvent = new Roll();
					rollEvent.setRollId(roll.getRollId());
					rollEvent.setCopyStatus(progressValue.toString());
					roll.setCopyStatus(progressValue.toString());
					Event event = new Event(EventTarget.MachineCopyFiles, false, null, rollEvent);
					notificationService.broadcast(event);
					return;
				
			

	}

}