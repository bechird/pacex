package com.epac.imposition.model;

public class Sheet {

	  private String sheetNb;
	  private String frontSurfaceBox;
	  private String frontContentObjectMatrix;
	  private String frontContentObjectRectangle;
	  private String frontContentObjectOrd;
	  private String frontMarkObjectMatrix;
	  private String frontMarkObjectRectangle;
	  private String frontMarkObjectOrd;
	  private String backSurfaceBox;
	  private String backContentObjectMatrix;
	  private String backContentObjectRectangle;
	  private String backContentObjectOrd;
	  private String backMarkObjectMatrix;
	  private String backMarkObjectRectangle;
	  private String backMarkObjectOrd;

	public String getSheetNb() {
		return sheetNb;
	}

	public Sheet(String sheetNb, String frontSurfaceBox, String frontContentObjectMatrix,
			String frontContentObjectRectangle, String frontContentObjectOrd, String backSurfaceBox,
			String backContentObjectMatrix, String backContentObjectRectangle, String backContentObjectOrd) {
		super();
		this.sheetNb = sheetNb;
		this.frontSurfaceBox = frontSurfaceBox;
		this.frontContentObjectMatrix = frontContentObjectMatrix;
		this.frontContentObjectRectangle = frontContentObjectRectangle;
		this.frontContentObjectOrd = frontContentObjectOrd;
		this.backSurfaceBox = backSurfaceBox;
		this.backContentObjectMatrix = backContentObjectMatrix;
		this.backContentObjectRectangle = backContentObjectRectangle;
		this.backContentObjectOrd = backContentObjectOrd;
	}

	public String getFrontSurfaceBox() {
		return frontSurfaceBox;
	}

	public String getFrontContentObjectMatrix() {
		return frontContentObjectMatrix;
	}

	public String getFrontContentObjectRectangle() {
		return frontContentObjectRectangle;
	}

	public String getFrontContentObjectOrd() {
		return frontContentObjectOrd;
	}

	public String getFrontMarkObjectMatrix() {
		return frontMarkObjectMatrix;
	}

	public String getFrontMarkObjectRectangle() {
		return frontMarkObjectRectangle;
	}

	public String getFrontMarkObjectOrd() {
		return frontMarkObjectOrd;
	}

	public String getBackSurfaceBox() {
		return backSurfaceBox;
	}

	public String getBackContentObjectMatrix() {
		return backContentObjectMatrix;
	}

	public String getBackContentObjectRectangle() {
		return backContentObjectRectangle;
	}

	public String getBackContentObjectOrd() {
		return backContentObjectOrd;
	}

	public String getBackMarkObjectMatrix() {
		return backMarkObjectMatrix;
	}

	public String getBackMarkObjectRectangle() {
		return backMarkObjectRectangle;
	}

	public void setSheetNb(String sheetNb) {
		this.sheetNb = sheetNb;
	}

	public void setFrontSurfaceBox(String frontSurfaceBox) {
		this.frontSurfaceBox = frontSurfaceBox;
	}

	public void setFrontContentObjectMatrix(String frontContentObjectMatrix) {
		this.frontContentObjectMatrix = frontContentObjectMatrix;
	}

	public void setFrontContentObjectRectangle(String frontContentObjectRectangle) {
		this.frontContentObjectRectangle = frontContentObjectRectangle;
	}

	public void setFrontContentObjectOrd(String frontContentObjectOrd) {
		this.frontContentObjectOrd = frontContentObjectOrd;
	}

	public void setFrontMarkObjectMatrix(String frontMarkObjectMatrix) {
		this.frontMarkObjectMatrix = frontMarkObjectMatrix;
	}

	public void setFrontMarkObjectRectangle(String frontMarkObjectRectangle) {
		this.frontMarkObjectRectangle = frontMarkObjectRectangle;
	}

	public void setFrontMarkObjectOrd(String frontMarkObjectOrd) {
		this.frontMarkObjectOrd = frontMarkObjectOrd;
	}

	public void setBackSurfaceBox(String backSurfaceBox) {
		this.backSurfaceBox = backSurfaceBox;
	}

	public void setBackContentObjectMatrix(String backContentObjectMatrix) {
		this.backContentObjectMatrix = backContentObjectMatrix;
	}

	public void setBackContentObjectRectangle(String backContentObjectRectangle) {
		this.backContentObjectRectangle = backContentObjectRectangle;
	}

	public void setBackContentObjectOrd(String backContentObjectOrd) {
		this.backContentObjectOrd = backContentObjectOrd;
	}

	public void setBackMarkObjectMatrix(String backMarkObjectMatrix) {
		this.backMarkObjectMatrix = backMarkObjectMatrix;
	}

	public void setBackMarkObjectRectangle(String backMarkObjectRectangle) {
		this.backMarkObjectRectangle = backMarkObjectRectangle;
	}

	public String getBackMarkObjectOrd() {
		return backMarkObjectOrd;
	}

	public void setBackMarkObjectOrd(String backMarkObjectOrd) {
		this.backMarkObjectOrd = backMarkObjectOrd;
	}
}
