package com.epac.cap.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import com.epac.cap.utils.LogUtils;

public class Configuration {
	
	private static String filename;
	public static ConfigurationWatchDog watchdog = null;
	
	private static final String CONFIG_PATH = "/etc/epac";
	
	public static void load(String name) {
		filename = name;
		reload();
	}
	
	public static void reload(){
		InputStream inputStream = null;
		try {
			Properties prop = new Properties();
			File file = new File(CONFIG_PATH, filename);
			if(!file.exists()){
				LogUtils.debug("Configuration file  " + file.getAbsolutePath() +" does not exist, trying classpath");
				file = new File(ClassLoader.getSystemResource(filename).getFile());
			}
			
			if(!file.exists()){
				Exception e = new FileNotFoundException("property file '"
						+ filename
						+ "' not found neither in "+CONFIG_PATH+" nor in classpath");
				LogUtils.error(
						"Failed loading configuration file  "
								+ filename, e);

				throw e;
			}
			
			if(watchdog == null){
				watchdog = new ConfigurationWatchDog(file.getAbsolutePath());
				watchdog.start();
			}
			
			inputStream = new FileInputStream(file);
			prop.load(inputStream);
			
			Properties fProps = new Properties();
			fProps.putAll(System.getProperties());
			fProps.putAll(prop);
			
			System.setProperties(fProps);
		
			LogUtils.debug("Configuration file ["+file.getAbsolutePath()+"] Loaded successfully ");
		} catch (Exception e) {
			LogUtils.error("Failed loading configuration file "+ filename, e);
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {}
		}

	}
	
	public static void unload(){
		//stop watchDog silently
		try {
			watchdog.interrupt();
		} catch (Exception e) {}
	}
	
	
}

class ConfigurationWatchDog extends Thread {


	static final public long DEFAULT_DELAY = 60000;
	
	private String filename;
	private long delay = DEFAULT_DELAY;

	private File file;
	private long lastModif = 0;
	private boolean warnedAlready = false;
	private boolean interrupted = false;

	public ConfigurationWatchDog(String filename) {
		super("FileWatchdog");
		this.filename = filename;
		file = new File(filename);
		setDaemon(true);
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	protected void doOnChange() {
		LogUtils.debug("File changed " + this.filename + ", reloading...");
		Configuration.reload();
	}

	private void checkAndConfigure() {
		boolean fileExists;
		try {
			fileExists = file.exists();
		} catch (SecurityException e) {
			LogUtils.warn("Was not allowed to read check file existance, file:["+ filename + "].");
			interrupted = true; // there is no point in continuing
			return;
		}

		if (fileExists) {
			long l = file.lastModified(); 
			if (l > lastModif) { 
				lastModif = l; 
				doOnChange();
				warnedAlready = false;
			}
		} else {
			if (!warnedAlready) {
				LogUtils.debug("[" + filename + "] does not exist.");
				warnedAlready = true;
			}
		}
	}

	public void run() {
		while (!interrupted) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				LogUtils.info("Configuration thread interrupted!");
				break;
			}
			checkAndConfigure();
		}
	}
}


