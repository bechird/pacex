package com.epac.owd.web;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.epac.owd.config.Configuration;
import com.epac.owd.main.Main;
import com.epac.owd.utils.LogUtils;

@WebListener
public class OWDContextListener implements ServletContextListener {

	
	
	public void contextInitialized(ServletContextEvent event) {
		


		LogUtils.debug("StaticThreadService.start()");
		Runnable task = new Runnable() {

			@Override
			public void run() {
				LogUtils.start();
				
				LogUtils.debug("Loading configuration...");
				Configuration.load("owd.properties");
								
				try {
					Main.main(null);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				LogUtils.end();
			}
		};
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(task);	
	}
	
	
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		LogUtils.start();
		Configuration.unload();
		
		LogUtils.end();
	}
}
