package com.epac.cap.sse.esprint;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.om.ProductionOrderManager;
import com.epac.cap.sse.interfaces.TokenGetter;
import com.epac.cap.sse.manager.ConnectionDetails;
import com.epac.cap.sse.manager.SseManager;
import com.epac.om.api.common.Notification;
import com.epac.om.api.utils.LogUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EsprintToPacexService {
	
	@Autowired
	private  SseManager sseManager;
	
	@Autowired
	private ProductionOrderManager productionOrderManager;	
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@PostConstruct
    public void init() {
		
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		
		// Only listen to orders from ESPRINT if the system preference indicates it
		if (!"true".equals(System.getProperty(ConfigurationConstants.ESPRINT_CONNECT))){
			LogUtils.info("Connection to Esprint is set to false in properties file, exiting...");
		}else {
			TokenGetter tokenGetter = new EsprintTokenGetter();
	    	ConnectionDetails connectionDetails = new ConnectionDetails("Esprint", tokenGetter, EsprintSseConnector.class, 12);
	    	sseManager.add(connectionDetails);
		}
		
		
	}
	
	
	@EventListener
	public void onReceiveMssage(EsprintSseNotification notification) {		
		String line = notification.getPayload();
		String token = notification.getToken();
		Notification n = productionOrderManager.resolveNotification(line);
		productionOrderManager.doHandleNotification(n, token);
	}
	
	
	


}