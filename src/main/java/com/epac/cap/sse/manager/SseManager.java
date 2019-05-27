package com.epac.cap.sse.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.epac.cap.handler.LookupHandler;
import com.epac.cap.service.NotificationService;
import com.epac.cap.sse.beans.Event;
import com.epac.cap.sse.beans.EventTarget;
import com.epac.cap.sse.interfaces.SseConnector;
import com.epac.om.api.utils.LogUtils;


@Service
public class SseManager {
	
	@Autowired
	private LookupHandler lookupHandler;
	
	@Autowired
	private CloseableHttpClient sseHttpClient;
	
	@Autowired
    ThreadPoolTaskExecutor threadPool;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private NotificationService notificationService;

	private CopyOnWriteArrayList<ConnectionDetails> connections= new CopyOnWriteArrayList<ConnectionDetails>();
	
	public void add(ConnectionDetails connectionDetails) {
		LogUtils.debug("adding to SseManager");
		connections.add(connectionDetails);		
	}
	
	public void getTokenForConnection(ConnectionDetails connectionDetails) {
		String token = connectionDetails.getTokenGetter().getToken(sseHttpClient, lookupHandler);
		connectionDetails.setToken(token);
	}
	
	
	public void createAndExecuteSseThread(ConnectionDetails connectionDetails) {

		Class<?> s= connectionDetails.getSseConnectorClass();
    	try {
    		
    		if( connectionDetails.getToken() != null && !connectionDetails.getToken().equals("-1")) {
    			
    			SseConnector sseConnectorObject = (SseConnector) applicationContext.getBean(s);
        		connectionDetails.setTimerEnded(new AtomicBoolean());
        		sseConnectorObject.setConnectionDetails(connectionDetails);        		
        		connectionDetails.setFuture(threadPool.submit(sseConnectorObject));				
        		//LogUtils.debug("SSE for class : " + connectionDetails.getSseConnectorClass().getName() + " STARTED SUCCESSFULY");
									
			}
    		
    		/*
    		if( connectionDetails.getToken() != null && connectionDetails.getToken().equals("-1")) {
    			
    			LogUtils.debug("token credentials for class : " + connectionDetails.getTokenGetter().getClass().getName() + " are INVALID");
									
			}
 
    		if( connectionDetails.getToken() == null) {
    			
    			LogUtils.debug("CONNECTION PROBLEM for token class : " + connectionDetails.getTokenGetter().getClass().getName());
									
			}
			*/
    		
		} catch (Exception e) {
			LogUtils.debug(e.getMessage());
		}

	}
	
	@Scheduled(fixedRate = 5500)
    public void checkConnections() {       
		
		List<ServerSseStatus> serversStatusList = new ArrayList<ServerSseStatus>();
		
		for(int i=0; i<connections.size(); i++) {
			
			ConnectionDetails connectionDetails = connections.get(i);
			
			ServerSseStatus status= new ServerSseStatus();
			status.setName(connectionDetails.getName());			

			
			//thread is completed ==> must an error
			if(connectionDetails.getFuture() != null && connectionDetails.getFuture().isDone()) {					
				
				status.setConnected(false);
				
				//check the returned status code
				Integer statusCode = connectionDetails.getReturnedStatusCode();

				status.setMessage("Trying to Reconnect, ENDED with status code :" + statusCode);				
				
				//401 : token invalidated ==> request new token
				if(statusCode.intValue() == 401) {
					getTokenForConnection(connectionDetails);					
				}
				
				createAndExecuteSseThread(connectionDetails);
				
			}
			
			//the connection is new, we need to start it
			if( connectionDetails.getFuture() == null ) {
				getTokenForConnection(connectionDetails);
				if(connectionDetails.getToken() != null) {
					createAndExecuteSseThread(connectionDetails);
					status.setMessage("New connection found");
				}				
			}

			//timer Ended => didn't receive heartbeat in time 
			if(connectionDetails.getTimerEnded() != null && connectionDetails.getTimerEnded().get() == true) {

				status.setConnected(false);
				status.setMessage("Didn't receive heartbeat in time");
				
				//cancel main thread
				connectionDetails.getFuture().cancel(true);
				
				//remake new thread
				createAndExecuteSseThread(connectionDetails);
				
			}
			
			//last time we didn't make it to get a token, retry to get the token 
			if(connectionDetails.getToken() == null) {	
				status.setConnected(false);
				status.setMessage("Didn't receive heartbeat in time, Trying to Reconnect");
				
				getTokenForConnection(connectionDetails);
				if(connectionDetails.getToken() != null) {
					createAndExecuteSseThread(connectionDetails);	
				}				
			}
			
			
			//can't get the token 
			if(connectionDetails.getToken() != null && connectionDetails.getToken().equals("-1")) {	
				status.setConnected(false);
				status.setMessage("The token credentials are wrong or inexistant");
	
			}
			
			serversStatusList.add(status);
			
		}
		
		try {
			Event event = new Event(EventTarget.SseStatus, false, null, serversStatusList);
			notificationService.broadcast(event);
		} catch (Exception e) {}
		
    }
	


}
