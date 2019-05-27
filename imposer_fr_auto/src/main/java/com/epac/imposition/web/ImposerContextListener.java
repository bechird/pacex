package com.epac.imposition.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoaderListener;

import com.epac.imposition.config.Configuration;
import com.epac.om.api.utils.LogUtils;


@Component
public class ImposerContextListener extends ContextLoaderListener {
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		LogUtils.start();
		
		LogUtils.debug("Loading configuration...");
		Configuration.load("imposition.properties");
		
		LogUtils.end();
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		LogUtils.start();
		Configuration.unload();
		LogUtils.end();
	}
}
