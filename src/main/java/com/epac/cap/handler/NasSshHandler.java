package com.epac.cap.handler;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.apache.sshd.client.session.ClientSession;

import com.epac.cap.utils.LogUtils;

/**
 * 
 *
 * @author Nabil
 *
 */
public class NasSshHandler implements Callable<Long>{

	// 192.168.30.160
	// admin
	// Fr5-@dmin

	private static Logger logger = Logger.getLogger(NasSshHandler.class);

	
	private String srcFileString;
	private String destFileString;
	private SshConnectionsManager sshConnectionsManager;	
	private ClientSession session;
	

	public NasSshHandler(String srcFileString, String destFileString, SshConnectionsManager sshConnectionsManager) {

		this.srcFileString = srcFileString;
		this.destFileString = destFileString;
		this.sshConnectionsManager = sshConnectionsManager;
		
	}
	

	@Override
	public Long call() throws Exception {
		
		session = sshConnectionsManager.getSessionsQueue().take();
		session = sshConnectionsManager.checkSession(session);	

		// replace		
		srcFileString = sshConnectionsManager.replacePath("com.epac.cap.nas.src.replace", srcFileString);
		destFileString = sshConnectionsManager.replacePath("com.epac.cap.nas.dst.replace", destFileString);
		
		
		// Monterrey
		// $fromList = str_replace('/opt/pacex/repository/raster/', '/share/RASTER/',
		// $fromList);
		// $toList = str_replace('/opt/pacex/repository/raster/', '/share/RASTER/',
		// $toList);
		// $toList = str_replace('/opt/pacex/repository/press/', '/share/RASTER/',
		// $toList);

		// Kentucky
		// $fromList = str_replace('/opt/pacex/repository/raster/',
		// '/share/pacex/raster/', $fromList);
		// $toList = str_replace('/opt/pacex/repository/raster/',
		// '/share/pacex/raster/', $toList);
		// $toList = str_replace('/opt/pacex/repository/press/', '/share/pacex/raster/',
		// $toList);

		// France
		// srcFileString = srcFileString.replace("/opt/esprint/repository/raster/",
		// "/share/FujiRIP/");
		// destFileString = destFileString.replace("/opt/pacex/repository/raster/",
		// "/share/FujiRIP/");
		// destFileString = destFileString.replace("/opt/pacex/repository/press/",
		// "/share/pacex/press/");

		// Tunis
		// $fromList = str_replace('/opt/esprint/repository/raster/', '/share/FujiRIP/',
		// $fromList);
		// $toList = str_replace('/opt/pacex/repository/raster/', '/share/FujiRIP/',
		// $toList);
		// $toList = str_replace('/opt/pacex/repository/press', '/share/pacex/press',
		// $toList);
		
		LogUtils.debug("Starting copy file " + srcFileString + " to --> " + destFileString);
				
		// get file size
		Long fileSize = getFileSize(srcFileString);
		if (fileSize == null) {
			LogUtils.debug("Source file not found,  exiting -->" + srcFileString);
			sshConnectionsManager.getSessionsQueue().offer(session);
			return null;
		}

		// create parent directories
		boolean operationStatus = createDestDirectories(destFileString);
		if (!operationStatus) {
			LogUtils.debug("createDestDirectories failure :"  + destFileString);
			sshConnectionsManager.getSessionsQueue().offer(session);
			return null;
		}

		// copy
		operationStatus = copyFile(srcFileString, destFileString);
		if (!operationStatus) {
			LogUtils.debug("copy FAILURE " + srcFileString + " to --> " + destFileString);
			sshConnectionsManager.getSessionsQueue().offer(session);
			return null;
		}
		
		sshConnectionsManager.getSessionsQueue().offer(session);
		LogUtils.debug("Copy SUCCESS " + srcFileString + " to --> " + destFileString);
		return fileSize;

	}
	
	
	private Long getFileSize(String srcFileString) {

		Long fileSize = null;

		try {			
			

			String sshOutput = session.executeRemoteCommand("ls -l  " + srcFileString);
			logger.debug("getFileSize ssh outPut :" + sshOutput);
			try {
				String parts[] = sshOutput.split("\\s+");
				fileSize = Long.parseLong(parts[4]);
			} catch (Exception e) {}				

			
		} catch (Exception e) {
			logger.debug("Un	ble to get file size", e);
		}

		return fileSize;

	}

	private boolean createDestDirectories(String destFileString) {

		boolean operationStatus = false;

		try {

			String parentsPath = getParentPath(destFileString);
			if (parentsPath == null)return false;

			String sshOutput = session.executeRemoteCommand("mkdir -p  " + parentsPath);
			logger.debug("createDestDirectories ssh outPut :" + sshOutput);	
			
			return true;
			
		} catch (Exception e) {
			logger.debug("ssh, unable to create parent directories", e);
		}

		return operationStatus;

	}

	private String getParentPath(String path) {

		String parentsPath = null;

		try {
			File f = new File(path);
			parentsPath = f.getParent();
		} catch (Exception e) {
			logger.debug("ssh, unable to get parents path", e);
		}

		return parentsPath;

	}

	private boolean copyFile(String srcFileString, String destFileString) {
		
		try {
			
			String sshOutput = session.executeRemoteCommand("cp -R " + srcFileString + " " + destFileString);
			logger.debug("copyFile ssh outPut :" + sshOutput);
			
			return true;
		} catch (Exception e) {
			logger.debug("Unable to copy error", e);
			return false;
		}
	}

	

}