package com.epac.owd.service;

public class AuthenticationException extends Exception {

	private static final long serialVersionUID = -8533444008166391086L;
	
	public AuthenticationException() {
	}
	
	public AuthenticationException(String message){
		super(message);
	}
}
