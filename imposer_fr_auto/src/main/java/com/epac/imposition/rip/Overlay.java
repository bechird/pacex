package com.epac.imposition.rip;

public class Overlay {

    private String overlayPage;
    private String relativeOverlayPath;
    
	public Overlay(String overlayPage, String relativeOverlayPath) {
		super();
		this.overlayPage = overlayPage;
		this.relativeOverlayPath = relativeOverlayPath;
	}

	public String getOverlayPage() {
		return overlayPage;
	}

	public String getRelativeOverlayPath() {
		return relativeOverlayPath;
	}

	public void setOverlayPage(String overlayPage) {
		this.overlayPage = overlayPage;
	}

	public void setRelativeOverlayPath(String relativeOverlayPath) {
		this.relativeOverlayPath = relativeOverlayPath;
	}
    
    
	
}
