package com.epac.imposition.cover;

public class Datamatrix {
	
	private float 	width;
	
	private float 	height;
	
	private float 	xPos;		//absolute distance from the left/right edge of the cover
	
	private float 	yPos;		//absolute distance from the bottom edge of the cover
	
	private String 	content;


	
	public Datamatrix(float width, float height, float xPos, float yPos, String content) {
		super();
		this.width = width;
		this.height = height;
		this.xPos = xPos;
		this.yPos = yPos;
		this.content = content;
	}

	@Override
	public String toString() {
		return "Datamatrix [width=" + width + ", height=" + height + ", xPos=" + xPos + ", yPos=" + yPos + ", content="
				+ content + "]";
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public float getxPos() {
		return xPos;
	}

	public void setxPos(float xPos) {
		this.xPos = xPos;
	}

	public float getyPos() {
		return yPos;
	}

	public void setyPos(float yPos) {
		this.yPos = yPos;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	
	
	
	

}
