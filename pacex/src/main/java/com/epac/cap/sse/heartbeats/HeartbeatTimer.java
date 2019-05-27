package com.epac.cap.sse.heartbeats;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epac.cap.sse.manager.ConnectionDetails;

public class HeartbeatTimer implements Runnable{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private Integer waitDelay;
	private AtomicBoolean timerEnded;	
	private ConnectionDetails connectionDetails;
	
	public HeartbeatTimer(ConnectionDetails connectionDetails) {
		this.connectionDetails = connectionDetails;
		this.timerEnded = connectionDetails.getTimerEnded();
		this.waitDelay = connectionDetails.getHeartbeatInterval() * 2;
	}
	

	@Override
	public void run() {
		try {
			TimeUnit.SECONDS.sleep(waitDelay);
			timerEnded.set(true);
        } catch (InterruptedException ie) {}		
	}

}
