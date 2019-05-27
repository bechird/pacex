package com.epac.cap.functionel;

import java.util.ArrayList;
import java.util.List;

public class ImpositionRequest {
	
	private String cover;
	private String text;
	private String textOutput;
	private String coverOutput;
	private String barcode;
	private String bookId;
	private float bookThickness;
	private float bookWidth;
	private float bookHeight;
	private float paperThickness;
	private float rollWidth;
	private List<String> hunkelerLines = new ArrayList<>();
	private PNLInfo pnlInformation;
	
	public String getCover() {
		return cover;
	}
	public void setCover(String cover) {
		this.cover = cover;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getTextOutput() {
		return textOutput;
	}
	public void setTextOutput(String textOutput) {
		this.textOutput = textOutput;
	}
	public String getCoverOutput() {
		return coverOutput;
	}
	public void setCoverOutput(String coverOutput) {
		this.coverOutput = coverOutput;
	}
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public String getBookId() {
		return bookId;
	}
	public void setBookId(String bookId) {
		this.bookId = bookId;
	}
	public float getBookThickness() {
		return bookThickness;
	}
	public void setBookThickness(float bookThickness) {
		this.bookThickness = bookThickness;
	}
	public float getBookWidth() {
		return bookWidth;
	}
	public void setBookWidth(float bookWidth) {
		this.bookWidth = bookWidth;
	}
	public float getBookHeight() {
		return bookHeight;
	}
	public void setBookHeight(float bookHeight) {
		this.bookHeight = bookHeight;
	}
	public float getPaperThickness() {
		return paperThickness;
	}
	public void setPaperThickness(float paperThickness) {
		this.paperThickness = paperThickness;
	}
	public float getRollWidth() {
		return rollWidth;
	}
	public void setRollWidth(float rollWidth) {
		this.rollWidth = rollWidth;
	}
	public List<String> getHunkelerLines() {
		return hunkelerLines;
	}
	public void setHunkelerLines(List<String> hunkelerLines) {
		this.hunkelerLines = hunkelerLines;
	}
	/**
	 * @return the pnlInformation
	 */
	public PNLInfo getPnlInformation() {
		return pnlInformation;
	}
	/**
	 * @param pnlInformation the pnlInformation to set
	 */
	public void setPnlInformation(PNLInfo pnlInformation) {
		this.pnlInformation = pnlInformation;
	}

}