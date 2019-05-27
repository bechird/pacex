package com.epac.cap.sse.beans;

public class Event {
	
	
	private String target;
	private Boolean error;
	private String message;
	private Object object;
	
	
	public Event(String target, Boolean error, String message, Object object) {
		super();
		this.target = target;
		this.error = error;
		this.message = message;
		this.object = object;
	}


	public String getTarget() {
		return target;
	}


	public void setTarget(String target) {
		this.target = target;
	}


	public Boolean getError() {
		return error;
	}


	public void setError(Boolean error) {
		this.error = error;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public Object getObject() {
		return object;
	}


	public void setObject(Object object) {
		this.object = object;
	}
	
	
	
	
}
