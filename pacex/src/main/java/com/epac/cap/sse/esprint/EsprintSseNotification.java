package com.epac.cap.sse.esprint;

public class EsprintSseNotification {
	private String payload;
	private String token;
	
	public EsprintSseNotification(String payload, String token) {
		super();
		this.payload = payload;
		this.token = token;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		return "EsprintSseNotification [payload=" + payload + "]";
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	
}
