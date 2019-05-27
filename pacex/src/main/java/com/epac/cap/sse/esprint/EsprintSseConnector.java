package com.epac.cap.sse.esprint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.annotation.PreDestroy;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.sse.heartbeats.HeartbeatTimer;
import com.epac.cap.sse.interfaces.SseConnector;
import com.epac.cap.sse.manager.ConnectionDetails;
import com.epac.om.api.utils.LogUtils;

@Component
@Scope("prototype")
public class EsprintSseConnector implements SseConnector{
	
	@Autowired
	private CloseableHttpClient sseHttpClient;
	
	private ApplicationEventPublisher publisher;
	
	@Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher= applicationEventPublisher;
    }
	
	//private final String url= "https://production.esprint.com.mt/notification";
	
	private final String WS_TARGET_SSE = "/notification";
	private final String heartbeatString = "<PreEvent></PreEvent>";
		
	private ConnectionDetails connectionDetails;	
	private Thread heartbeatTimer = null;
	private BufferedReader reader;
	private HttpResponse sseHttpResponse = null;
	
	@Override
	public ConnectionDetails call() {
    	
		Integer statusCode = 0;
	
		try {			 	
				
				String service = null;
				try {
					service = System.getProperty(ConfigurationConstants.OM_SERVICE);
				} catch (Exception e) {}
				
				if(service == null) {
					LogUtils.info(ConfigurationConstants.OM_SERVICE + " is not defined, exiting...");
					connectionDetails.setReturnedStatusCode(statusCode);
					return connectionDetails;
				}
				
				String url = service.concat(WS_TARGET_SSE);
			
				HttpGet sseHttpRequest = new HttpGet(url);	
				sseHttpRequest.setHeader("Authorization", "Bearer ".concat(connectionDetails.getToken()));
				sseHttpRequest.setHeader("Accept", "application/json");
				sseHttpRequest.setHeader("owner", "656d665f6f6d5f6f776e6572");
	
				
				sseHttpResponse = sseHttpClient.execute(sseHttpRequest);

				statusCode = sseHttpResponse.getStatusLine().getStatusCode();
				connectionDetails.setReturnedStatusCode(statusCode);
			
				
				reader = new BufferedReader(new InputStreamReader(sseHttpResponse.getEntity().getContent(), StandardCharsets.UTF_8));
				String content = null;
				StringBuilder sb= new StringBuilder();
				int value = 0;
				
				while ((value = reader.read()) != -1) {			    			    
				   				    
				    sb = sb.append((char)value);
				    
				    if(sb.charAt(sb.length()-1) == '\n' && sb.charAt(sb.length()-2) == '\n') {
				    	
				    	sb.deleteCharAt(sb.length()-1);
				    	sb.deleteCharAt(sb.length()-1);
				    	
				    	content= sb.toString();
				    	
				    	sb = new StringBuilder();
				    	content = content.replaceAll("([\\n]*)data:", "").trim();
						
					    //the line is a heartbeat
					    if(content.contains(heartbeatString)) {
				    	
					    	if(heartbeatTimer != null) {
					    		heartbeatTimer.interrupt();
					    	}
					    	heartbeatTimer = new Thread(new HeartbeatTimer(connectionDetails));
					    	heartbeatTimer.start();
					    	
					    }else {    	
    		
				    		//payload ok, publish it
					    	EsprintSseNotification notification = new EsprintSseNotification(content, connectionDetails.getToken());
					    	publisher.publishEvent(notification);			    	
					    	
					    }
					    
						
						
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
