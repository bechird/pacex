package com.epac.cap.functionel;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//import org.apache.catalina.startup.HomesUserDatabase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.config.NDispatcher;
import com.epac.cap.model.Part;
import com.epac.cap.model.WFSAction;
import com.epac.cap.model.WFSDataSupport;
import com.epac.cap.model.WFSLocation;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.utils.LogUtils;
import com.google.common.io.Files;
import com.mycila.event.api.Event;
import com.mycila.event.api.Subscriber;
import com.mycila.event.api.topic.TopicMatcher;
import com.mycila.event.api.topic.Topics;

@Component
public class Rip extends WFSAction implements IAction<Part> {

	private static final long serialVersionUID = 1L;

	public static final String [] rasterSMExtensions = {".MB2", ".MBD00", ".MBD01", ".MBD02", ".MBD03", ".MBD08", ".MBD09", ".MBD10", ".MBD11"};

	public static final String [] rasterEMExtensions = {".inf", ".dat00", ".dat01", ".dat02", ".dat03", ".dat08", ".dat09", ".dat10", ".dat11"};
	
	@Autowired
	private LookupDAO lookupDAO;
	
	private ExecutorService executor;
	
	@Override
	public boolean handle(Part part) {
		if(part.getPaperType() == null) return false;
		String dropFolder = part.getPaperType().getDropFolder();
		LogUtils.info("Submit part "+part.getPartNum()+" to RIP, base drop folder path " + dropFolder);
		Set<WFSDataSupport> dataSupports = part.getDataSupportsOnProd();
		String rollWidth = "R".concat(String.valueOf(part.getPartWorkFlowOnProd().getRollWidth()).replace(".", ""));
		
		if(rollWidth.length() > 4){
			rollWidth = rollWidth.substring(0, 4);
		}
		for(WFSDataSupport ds: dataSupports){
			if(ds.getName().equals(WFSDataSupport.NAME_IMPOSE)){
				if(ds.getDescription().equals(Part.PartsCategory.TEXT.getName())){
					if (ds.getDsType().contains("EM.")) {
						String file = ds.getLocationdByType(WFSLocation.DESTINATION).getPath();
						String pattern = new StringBuilder().append(ds.getDsType().substring(0, ds.getDsType().indexOf('.'))).append(part.getPartNum()).append(rollWidth).toString();
						String morePattern = Files.getNameWithoutExtension(file).substring(0, 2).concat(pattern);
						submitEpacModeRipping(file, dropFolder, morePattern);
						if (ds.getDsType().contains(".PDF")) {
							Runnable r = new CheckRaster(System.getProperty("com.epac.cap.directories.raster.EM"), morePattern, part.getPartNum());
							Thread rasterFetchThread = new Thread(r);
							rasterFetchThread.start();
						}
					} else if (ds.getDsType().contains("SM.")) {
						String file = ds.getLocationdByType(WFSLocation.DESTINATION).getPath();
						submitStandardModeRipping(file, dropFolder, new StringBuilder().append(ds.getDsType().substring(0, ds.getDsType().indexOf('.'))).append(part.getPartNum()).append(rollWidth).toString());
						if (ds.getDsType().contains(".PDF")) {
							String pattern = new StringBuilder().append(ds.getDsType().substring(0, ds.getDsType().indexOf('.'))).append(part.getPartNum()).append(rollWidth).toString();
							Runnable r = new CheckRaster(System.getProperty("com.epac.cap.directories.raster.SM"), pattern, part.getPartNum());
							Thread rasterFetchThread = new Thread(r);
							rasterFetchThread.start();
						}
					}
				}
			}
		}
		return true;
	}
	
	private void submitEpacModeRipping(String file, String dropFolder, String pattern){
		String dropFolderLocation = System.getProperty("com.epac.cap.directories.xmf.EM");
		File dropFolderDirectory = new File(dropFolderLocation, dropFolder);
		
		if(file.toUpperCase().endsWith(".PDF"))
			dropFolderDirectory = new File(dropFolderDirectory, "pdf");
		
		if(!dropFolderDirectory.exists())
			dropFolderDirectory.mkdirs();
		
		try {
			FileUtils.copyFileToDirectory(new File(file), dropFolderDirectory);
			LogUtils.info("Imposed text file: "+ file + " copied to raster system in "+dropFolderDirectory);
			
		} catch (IOException e) {
			LogUtils.error("Could not copy file "+ file + " to "+dropFolderDirectory);
		}
		
		// in local env, mimic creation of raster files by the xmf
		String donotCareAboutRasterFiles = System.getProperty(ConfigurationConstants.DONOTCOPYRASTER_TEST);
		if("true".equals(donotCareAboutRasterFiles) && file.endsWith(".pdf")){
			String rasterFile = null;
			File srcFile = new File(file);
			String rasterPath = System.getProperty("com.epac.cap.directories.raster.EM").concat(pattern);
			File rasterFolder = new File(rasterPath);
			if(!rasterFolder.exists()){
				rasterFolder.mkdirs();
			}
			try {
				for(String ext : rasterEMExtensions){
					rasterFile = new StringBuilder().append(rasterPath).append(File.separator).append(pattern).append(ext).toString();
					FileUtils.copyFile(srcFile, new File(rasterFile));
					LogUtils.info("Raster file: "+ file + " generated in "+rasterFile);
				}
			} catch (IOException e) {
				LogUtils.error("Could not copy file "+ file + " to "+rasterFile);
			}
		}
	}
	
	private void submitStandardModeRipping(String file, String dropFolder, String pattern){
		String dropFolderLocation = System.getProperty("com.epac.cap.directories.xmf.SM");
		File dropFolderDirectory = new File(dropFolderLocation, dropFolder);
		
		if(file.toUpperCase().endsWith(".PDF"))
			dropFolderDirectory = new File(dropFolderDirectory, "pdf");
		
		if(!dropFolderDirectory.exists())
			dropFolderDirectory.mkdirs();
		
		try {
			FileUtils.copyFileToDirectory(new File(file), dropFolderDirectory);
			LogUtils.info("Imposed text file: "+ file + " copied to raster system in "+dropFolderDirectory);
		} catch (IOException e) {
			LogUtils.error("Could not copy file "+ file + " to "+dropFolderDirectory);
		}
		
		// in local env, mimic creation of raster files by the xmf
		String donotCareAboutRasterFiles = System.getProperty(ConfigurationConstants.DONOTCOPYRASTER_TEST);
		if("true".equals(donotCareAboutRasterFiles) && file.endsWith(".pdf")){
			String rasterPath = System.getProperty("com.epac.cap.directories.raster.SM");
			String rasterFile = null;
			File srcFile = new File(file);
			try {
				for(String ext : rasterSMExtensions){
					rasterFile = new StringBuilder().append(rasterPath).append(pattern).append(ext).toString();
					FileUtils.copyFile(srcFile, new File(rasterFile));
					LogUtils.info("Raster file: "+ file + " generated in "+rasterFile);
				}
			} catch (IOException e) {
				LogUtils.error("Could not copy file "+ file + " to "+rasterFile);
			}
		}
	}
	
	private void submitCoverRipping(String file){
		String dropFolderLocation = System.getProperty("com.epac.cap.directories.cover.input");
		
		File dropFolderDirectory = new File(dropFolderLocation);
		File imposedCoverFile	 = new File(file);
		
		try {
			FileUtils.copyFileToDirectory(imposedCoverFile, dropFolderDirectory);
			LogUtils.info("Imposed cover file: "+ file + " copied to raster system in "+dropFolderDirectory);
		} catch (IOException e) {
			LogUtils.error("Could not copy file "+ file + " to "+dropFolderDirectory);
		}
	}
	
	
	@Override
	public boolean TryFire(String partNb) {
		return true;
	}

	@Override
	public void subscribe() {
		TopicMatcher matcher = Topics.only("cap/events/rip");
		NDispatcher.getDispatcher().subscribe(matcher, Part.class, new Subscriber<Part>() {
			public void onEvent(Event<Part> event) throws Exception {
				LogUtils.debug("Received: " + event.toString() + " calling the ripping method");
				try {
					handle(event.getSource());
				} catch (Exception e) {
					LogUtils.error("Error occured while handling RIP event", e);
				}
			}
		});
	}

	@Override
	public void unsubscribe() {
		// TODO Auto-generated method stub

	}

	public Rip() {
		executor = Executors.newCachedThreadPool();
		subscribe();
	}

}

class CheckRaster implements Runnable {
	
	private String pattern;
	private String rasterPath;
	private String partNumb;
	private boolean exists = false;
	
	public CheckRaster(String rasterPath,String pattern, String partNumb) {
		this.pattern = pattern;
		this.rasterPath = rasterPath;
		this.partNumb = partNumb;
	}

    public void run(){
       LogUtils.debug("Raster File Search Thread is running: "+this.pattern);
       checkRasterFile(rasterPath,pattern);
    }
    
	private void checkRasterFile(String rasterPath, String pattern) {
		String raster = null;
		Date date = new Date();
		boolean shutdown = false;
		int maxHours = Integer.parseInt(System.getProperty(ConfigurationConstants.RIP_MAX_HOURS));
		
		Map<String, Long> currentFiles = new HashMap<String, Long>();
		Map<String, Long> oldFiles = new HashMap<String, Long>();
		File[] files = null;
		
		while (((TimeUnit.MILLISECONDS.toHours(new Date().getTime() - date.getTime())) < maxHours) && !shutdown) {
			LogUtils.info("Waiting for rip files with pattern " + pattern + " under " + rasterPath + "; so far number of generated files is " + currentFiles.size());
			try {
				Thread.sleep(Integer.parseInt(System.getProperty(ConfigurationConstants.RIP_INTERVAL)));
			} catch (InterruptedException e) {
				LogUtils.debug("WatchDog thread stopped");
				break;
			}
			File rasterFolder = new File(rasterPath);

			oldFiles.clear();
			oldFiles.putAll(currentFiles);
			
			files = rasterFolder.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					if(file.getName().contains(pattern))
						return true;
					return false;
				}
			});
			if(pattern.contains("EM")){
				if (files != null && files.length > 0) {
					files = files[0].listFiles();
				}
			}
			
			currentFiles.clear();
			if (files != null && files.length > 0) {
				for(File f : files){
					currentFiles.put(f.getAbsolutePath(), f.length());
				}
			}
			if (files != null && files.length > 0) {
				if(pattern.contains("SM")){
					if(files.length != Rip.rasterSMExtensions.length || oldFiles.isEmpty() || oldFiles.size() != Rip.rasterSMExtensions.length){
						continue;
					}
					boolean underConstruction = false;
					for(String fname : currentFiles.keySet()){
						if(currentFiles.get(fname).longValue() != oldFiles.get(fname).longValue()){
							LogUtils.info("File " + fname + " is still being generated");
							underConstruction = true;
							break;
						}
					}
					if(underConstruction){
						continue;
					}
					exists = true;	
					shutdown = true;
					for(File f : files){
						if("MB2".equals(FilenameUtils.getExtension(f.getName()))){
							raster = f.getAbsolutePath();
							break;
						}
					}
				}
				if(pattern.contains("EM")){
					//files = files[0].listFiles();
					if(files.length != Rip.rasterEMExtensions.length || oldFiles.isEmpty() || oldFiles.size() != Rip.rasterEMExtensions.length){
						continue;
					}
					boolean underConstruction = false;
					for(String fname : currentFiles.keySet()){
						if(currentFiles.get(fname).longValue() != oldFiles.get(fname).longValue()){
							LogUtils.info("File " + fname + " is still being generated");
							underConstruction = true;
							break;
						}
					}
					if(underConstruction){
						continue;
					}
					
					exists = true;	
					shutdown = true;
					raster = files[0].getParentFile().getAbsolutePath();
				}
			}

		}

		if (exists){
			LogUtils.info("Rip has finished and output files with pattern " + pattern + " are found under " + rasterPath);
			
			List<String> params = new ArrayList<>();
			
			params.add(partNumb);
			params.add(pattern.startsWith("S") ? pattern.substring(2, 6) : pattern.substring(0, 4));
			params.add(raster);
			NDispatcher.getDispatcher().publish(Topics.topic("cap/events/ripping/done"), params);
			
		} else {
			List<String> params = new ArrayList<>();
			params.add(partNumb);
			params.add(pattern.substring(0, 6));
			LogUtils.info("Time has elapsed and rip has not finished, it may be caused by a problem on the XMF !!");
			NDispatcher.getDispatcher().publish(Topics.topic("cap/events/ripping/error"), params);
		}

		Thread.currentThread().interrupt();
	}
  }
