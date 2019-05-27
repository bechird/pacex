package com.epac.cap.config;

public abstract class PacexService {
	
	public PacexService() {
		ServiceListener.addService(this);
	}
	
	public abstract void shutdown();
}
