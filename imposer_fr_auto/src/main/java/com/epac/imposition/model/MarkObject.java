package com.epac.imposition.model;

public class MarkObject {

	
	private String frontMarkObjectMatrix;
	private String frontMarkObjectRectangle;
	private String frontMarkObjectOrd;
	private String backMarkObjectMatrix;
	private String backMarkObjectRectangle;
	private String backMarkObjectOrd;



	public MarkObject() {
	}

	public MarkObject(String frontMarkObjectMatrix, String frontMarkObjectRectangle,
			String frontMarkObjectOrd, String backMarkObjectMatrix,
			String backMarkObjectRectangle, String backMarkObjectOrd) {
		super();
		this.frontMarkObjectMatrix 			= frontMarkObjectMatrix;
		this.frontMarkObjectRectangle 		= frontMarkObjectRectangle;
		this.frontMarkObjectOrd 			= frontMarkObjectOrd;
		this.backMarkObjectMatrix 			= backMarkObjectMatrix;
		this.backMarkObjectRectangle 		= backMarkObjectRectangle;
		this.backMarkObjectOrd				= backMarkObjectOrd;
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

	public String getBackMarkObjectMatrix() {
		return backMarkObjectMatrix;
	}

	public String getBackMarkObjectRectangle() {
		return backMarkObjectRectangle;
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
