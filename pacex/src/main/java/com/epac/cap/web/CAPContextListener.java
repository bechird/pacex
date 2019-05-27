package com.epac.cap.web;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.springframework.stereotype.Component;

import com.epac.cap.config.Configuration;
import com.epac.cap.utils.LogUtils;


@WebListener
@Component
public class CAPContextListener implements ServletContextListener {
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		LogUtils.start();
		
		LogUtils.debug("Loading configuration...");
		Configuration.load("cap.properties");
		
		LogUtils.end();
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		LogUtils.start();
		Configuration.unload();
		
		 try {
			 LogUtils.info("Calling MySQL AbandonedConnectionCleanupThread shutdown");
             com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.shutdown();

         } catch (Exception e) {
        	 LogUtils.error("Error calling MySQL AbandonedConnectionCleanupThread shutdown {}", e);
         }

         ClassLoader cl = Thread.currentThread().getContextClassLoader();

         Enumeration<Driver> drivers = DriverManager.getDrivers();
         while (drivers.hasMoreElements()) {
             Driver driver = drivers.nextElement();

             if (driver.getClass().getClassLoader() == cl) {

                 try {
                	 LogUtils.info("Deregistering JDBC driver {} "+ driver);
                     DriverManager.deregisterDriver(driver);

                 } catch (SQLException ex) {
                	 LogUtils.error("Error deregistering JDBC driver {} "+ driver, ex);
                 }

             } else {
            	 LogUtils.info("Not deregistering JDBC driver {} as it does not belong to this webapp's ClassLoader "+ driver);
             }
         }
     
		LogUtils.end();
	}
}
