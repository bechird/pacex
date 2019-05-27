package com.epac.owd.service;

public class PacexClientException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public PacexClientException(Throwable t) {
		super(t);
	}
	public PacexClientException(String message) {
		super(message);
	}
	
	public PacexClientException(String message,Throwable t ) {
		super(message,t);
	}
	public PacexClientException() {
		super();
	}
}
