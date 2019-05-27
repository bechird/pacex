package com.epac.cap.sse.printers;

public class PrinterSseNotification {
	private String printerName;
	private String payload;
	
	public PrinterSseNotification(String payload, String printerName) {
		super();
		this.printerName = printerName;
		this.payload = payload;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getPrinterName() {
		return printerName;
	}

	public void setPrinterName(String printerName) {
		this.printerName = printerName;
	}

	@Override
	public String toString() {
		return "Pacer [payload=" + payload + "]";
	}
	
	
}
