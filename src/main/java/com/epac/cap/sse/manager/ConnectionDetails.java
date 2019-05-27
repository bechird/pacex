package com.epac.cap.sse.manager;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.epac.cap.sse.interfaces.TokenGetter;

public class ConnectionDetails {
	
	private String name;
	private TokenGetter tokenGetter;
	private Class<?> sseConnectorClass;
	private Integer heartbeatInterval;
	private Integer returnedStatusCode = null;
	private String token = null;
	private AtomicBoolean timerEnded = null;
	private Future<ConnectionDetails> future = null;
	private String url = null;
	
	
	public ConnectionDetails(String name, TokenGetter tokenGetter, Class<?> sseConnectorClass, Integer heartbeatInterval) {
		super();
		this.name = name;
		this.tokenGetter = tokenGetter;
		this.sseConnectorClass = sseConnectorClass;
		this.heartbeatInterval = heartbeatInterval;
	}
	public TokenGetter getTokenGetter() {
		return tokenGetter;
	}
	public void setTokenGetter(TokenGetter tokenGetter) {
		this.tokenGetter = tokenGetter;
	}

	public Integer getHeartbeatInterval() {
		return heartbeatInterval;
	}
	public void setHeartbeatInterval(Integer heartbeatInterval) {
		this.heartbeatInterval = heartbeatInterval;
	}

	public Integer getReturnedStatusCode() {
		return returnedStatusCode;
	}
	public void setReturnedStatusCode(Integer returnedStatusCode) {
		this.returnedStatusCode = returnedStatusCode;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Class<?> getSseConnectorClass() {
		return sseConnectorClass;
	}
	public void setSseConnectorClass(Class<?> sseConnectorClass) {
		this.sseConnectorClass = sseConnectorClass;
	}
	public AtomicBoolean getTimerEnded() {
		return timerEnded;
	}
	public void setTimerEnded(AtomicBoolean timerEnded) {
		this.timerEnded = timerEnded;
	}
	public Future<ConnectionDetails> getFuture() {
		return future;
	}
	public void setFuture(Future<ConnectionDetails> future) {
		this.future = future;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
		
}
