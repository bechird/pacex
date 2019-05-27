package com.epac.imposition.cover;

import com.epac.imposition.model.ImpositionBook;

public class Cover {

	
	private String 	docId;
	
	private float	rawheight;
	
	private float	frontMargin;
	
	private float  	trimboxHeight;
	
	private float 	trimboxWidth;
	
	private float 	finalHeight;
	
	private float 	finalWidth;
	
	private float  	bleedboxHeight;
	
	private float 	bleedboxWidth;
	
	private double  thikness;
	
	private String 	filePath;

	private ImpositionBook book;


	
	public Cover(ImpositionBook book, 
			float rawheight, 
			float frontmargin, 
			float finalHeight, 
			float finalWidth, 
			double thickness, 
			String filePath) {
		this.docId = book.getDocId();
		this.book = book;
		this.rawheight = rawheight;
		this.finalHeight = finalHeight;
		this.finalWidth = finalWidth;
		this.thikness = thickness;
		this.filePath = filePath;
		this.frontMargin = frontmargin;
	}

	public ImpositionBook getBook() {
		return book;
	}
	
	public void setBook(ImpositionBook book) {
		this.book = book;
	}

	public float getFrontMargin() {
		return frontMargin;
	}
	
	public void setFrontMargin(float frontMargin) {
		this.frontMargin = frontMargin;
	}

	public float getFinalHeight() {
		return finalHeight;
	}

	public void setFinalHeight(float finalHeight) {
		this.finalHeight = finalHeight;
	}

	public float getFinalWidth() {
		return finalWidth;
	}

	public void setFinalWidth(float finalWidth) {
		this.finalWidth = finalWidth;
	}

	public float getRawheight() {
		return rawheight;
	}

	public void setRawheight(float rawheight) {
		this.rawheight = rawheight;
	}

	public float getBleedboxHeight() {
		return bleedboxHeight;
	}

	public void setBleedboxHeight(float bleedboxHeight) {
		this.bleedboxHeight = bleedboxHeight;
	}

	public float getBleedboxWidth() {
		return bleedboxWidth;
	}

	public void setBleedboxWidth(float bleedboxWidth) {
		this.bleedboxWidth = bleedboxWidth;
	}

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public float getTrimboxHeight() {
		return trimboxHeight;
	}

	public void setTrimboxHeight(float trimboxHeight) {
		this.trimboxHeight = trimboxHeight;
	}

	public float getTrimboxWidth() {
		return trimboxWidth;
	}

	public void setTrimboxWidth(float trimboxWidth) {
		this.trimboxWidth = trimboxWidth;
	}

	public double getThikness() {
		return thikness;
	}

	public void setThikness(double thikness) {
		this.thikness = thikness;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	
	
	
}

