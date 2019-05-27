package com.epac.cap.sse.interfaces;

import java.util.concurrent.Callable;

import org.springframework.context.ApplicationEventPublisherAware;

import com.epac.cap.sse.manager.ConnectionDetails;

public interface SseConnector extends Callable<ConnectionDetails>, ApplicationEventPublisherAware{
	
	void setConnectionDetails(ConnectionDetails connectionDetails);
	
}
