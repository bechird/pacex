package com.epac.owd.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;

public class LocalFileFetcher {

	/**
	 * Creates a WatchService and registers the given directory
	 * @return 
	 */
	public LocalFileFetcher(Path dir) throws IOException {
		
		
		File directory = dir.toFile();
		
		File[] files = directory.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return FilenameUtils.getExtension(name).equalsIgnoreCase("xml");
			}
		});
		
		for (File file : files) {
			try {
				PACEXmlParser paceXmlParser = new PACEXmlParser();
				paceXmlParser.parse(file.getAbsolutePath());
			} catch (Exception e) {}
		}
		
		
		FileSystemManager fsManager = VFS.getManager();
		 FileObject listendir = fsManager.resolveFile(dir.toFile().getAbsolutePath());

		 DefaultFileMonitor fm = new DefaultFileMonitor(new FileListener() {
			
			@Override
			public void fileDeleted(FileChangeEvent arg0) throws Exception {
				
			}
			
			@Override
			public void fileCreated(FileChangeEvent event) throws Exception {
				try {
					String file = event.getFile().getURL().getFile();
					if (FilenameUtils.getExtension(file).equalsIgnoreCase("xml")){
						PACEXmlParser paceXmlParser = new PACEXmlParser();
						paceXmlParser.parse(file);
					}
				} catch (Exception x) {
					// ignore to keep sample readbale
				}
			}
			
			@Override
			public void fileChanged(FileChangeEvent event) throws Exception {
				try {
					String file = event.getFile().getURL().getFile();
					if (FilenameUtils.getExtension(file).equalsIgnoreCase("xml")){
						PACEXmlParser paceXmlParser = new PACEXmlParser();
						paceXmlParser.parse(file);
					}
				} catch (Exception x) {
					// ignore to keep sample readbale
				}
			}
		});
		 fm.setRecursive(true);
		 fm.setDelay(5000);
		 fm.addFile(listendir);
		
		 fm.start();
		
	}

}
