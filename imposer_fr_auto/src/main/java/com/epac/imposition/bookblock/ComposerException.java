package com.epac.imposition.bookblock;

public class ComposerException extends Exception {

	private static final long serialVersionUID = 8871107952975054022L;

	public ComposerException(String message) {
		super(message);
	}
	public ComposerException(String message, Throwable t) {
		super(message,t);
	}
	public ComposerException(Exception e) {
		super(e);
	}
	
}
