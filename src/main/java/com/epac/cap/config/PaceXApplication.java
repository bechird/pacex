package com.epac.cap.config;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.epac.cap.utils.LogUtils;

@ApplicationPath("/rest")
public class PaceXApplication  extends ResourceConfig {
	public PaceXApplication() {		
		
		register(JacksonFeature.class);
		register(MultiPartFeature.class);
		register(LoggingFeature.class);
		
		packages("com.epac.cap.service", "com.fasterxml.jackson.jaxrs.json");
		
		LogUtils.debug("PaceX Web Services started...");
	}
    
}
