package com.epac.cap.sse.pacer;

public class PacerSseNotification {
	private String payload;
	
	public PacerSseNotification(String payload) {
		super();
		this.payload = payload;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		return "Pacer [payload=" + payload + "]";
	}
	
	
}
