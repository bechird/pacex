package com.epac.cap.sse.printers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.PreDestroy;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.handler.LookupHandler;
import com.epac.cap.model.Preference;
import com.epac.cap.sse.heartbeats.HeartbeatTimer;
import com.epac.cap.sse.interfaces.SseConnector;
import com.epac.cap.sse.manager.ConnectionDetails;
import com.epac.om.api.utils.LogUtils;

@Component
@Scope("prototype")
public class PrinterSseConnector implements SseConnector{
	
	@Autowired
	private LookupHandler lookupHandler;
	
	@Autowired
	private CloseableHttpClient sseHttpClient;
	
	private ApplicationEventPublisher publisher;
	
	@Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher= applicationEventPublisher;
    }

		
	private final String heartbeatString = "HEARTBEAT";
	
	private ConnectionDetails connectionDetails;	
	private Thread heartbeatTimer = null;
	private BufferedReader reader;
	private HttpResponse sseHttpResponse = null;
	
	@Override
	public ConnectionDetails call() {
    	
		Integer statusCode = 0;
		
		try {			 	
			
				
				String printerUrl = connectionDetails.getUrl();
				
				LogUtils.debug("connecting to printer with url: " + printerUrl );	
			
				HttpGet sseHttpRequest = new HttpGet(printerUrl);	
								
				sseHttpResponse = sseHttpClient.execute(sseHttpRequest);

				statusCode = sseHttpResponse.getStatusLine().getStatusCode();
				
				connectionDetails.setReturnedStatusCode(statusCode);
				
				reader = new BufferedReader(new InputStreamReader(sseHttpResponse.getEntity().getContent()));
				String line = null;
				
				while ((line = reader.readLine()) != null) {			    			    
				    if(line.isEmpty())continue;
				    line = line.replaceAll("([\\n]*)data:", "");
				    
				    //the line is a heartbeat
				    if(line.contains(heartbeatString)) {
				    	
				    	if(heartbeatTimer != null) {
				    		heartbeatTimer.interrupt();
				    	}
				    	heartbeatTimer = new Thread(new HeartbeatTimer(connectionDetails));
				    	heartbeatTimer.start();
				    	
				    }else {
				    	
				    	//the line is a payload, publish it
				    	PrinterSseNotification notification = new PrinterSseNotification(line, connectionDetails.getName());
				    	publisher.publishEvent(notification);
				    }
				    
				    
				    
				}
				
	  	} catch (Exception e) {
	  		connectionDetails.setReturnedStatusCode(0);
		} 	
		
		return connectionDetails;
		
	}
	
	@PreDestroy
	public void beforeStop() {
		try {
			reader.close();
		} catch (Exception e) {}
		
		try {
			EntityUtils.consume(sseHttpResponse.getEntity());
		} catch (Exception e) {}
	}

	public ConnectionDetails getConnectionDetails() {
		return connectionDetails;
	}

	public void setConnectionDetails(ConnectionDetails connectionDetails) {
		this.connectionDetails = connectionDetails;
	}

	
	
	
}
