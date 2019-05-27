package com.epac.imposition.model;

import java.util.ArrayList;

/**
 *  Class representing a PNL Information bean to communicate to the Imposer Service
 */
public class PNLInfo {
	
	private Integer pageNumber;
	private Float hMargin;
	private Float vMargin;
	private ArrayList<PNLTemplateLine> pnlLines = new ArrayList<PNLTemplateLine>();
	// For Preview:
	private Float pageWidth;
	private Float pageHeight;
	private Float lineSpacing;
	
	
	public PNLInfo(Integer pageNumber, Float hMargin, Float vMargin, ArrayList<PNLTemplateLine> pnlLines,
			Float pageWidth, Float pageHeight, Float lineSpacing) {
		super();
		this.pageNumber = pageNumber;
		this.hMargin = hMargin;
		this.vMargin = vMargin;
		this.pnlLines = pnlLines;
		this.pageWidth = pageWidth;
		this.pageHeight = pageHeight;
		this.lineSpacing = lineSpacing;
	}
	/**
	 * @return the pageNumber: Page number on which the PNL will be injected
	 */
	public Integer getPageNumber() {
		return pageNumber;
	}
	/**
	 * @param pageNumber the pageNumber to set
	 */
	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}
	/**
	 * @return the horizontal Margin: The margin to leave from the left hand edge of the page till the PNL block
	 */
	public Float gethMargin() {
		return hMargin;
	}
	/**
	 * @param pnl Horizontal Margin the pnl Horizontal Margin to set
	 */
	public void sethMargin(Float hMargin) {
		this.hMargin = hMargin;
	}
	/**
	 * @return the pnl Vertical Margin: The margin to leave from the bottom edge of the page till the PNL block
	 */
	public Float getvMargin() {
		return vMargin;
	}
	/**
	 * @param pnlVerticalMargin the pnlVerticalMargin to set
	 */
	public void setvMargin(Float vMargin) {
		this.vMargin = vMargin;
	}
	/**
	 * @return the pnlLines: the lines of text and style to include in the PNL
	 */
	public ArrayList<PNLTemplateLine> getPnlLines() {
		return pnlLines;
	}
	/**
	 * @param pnlLines the pnlLines to set
	 */
	public void setPnlLines(ArrayList<PNLTemplateLine> pnlLines) {
		this.pnlLines = pnlLines;
	}
	/**
	 * @return the pageWidth
	 */
	public Float getPageWidth() {
		return pageWidth;
	}
	/**
	 * @param pageWidth the pageWidth to set
	 */
	public void setPageWidth(Float pageWidth) {
		this.pageWidth = pageWidth;
	}
	/**
	 * @return the pageHeight
	 */
	public Float getPageHeight() {
		return pageHeight;
	}
	/**
	 * @param pageHeight the pageHeight to set
	 */
	public void setPageHeight(Float pageHeight) {
		this.pageHeight = pageHeight;
	}
	/**
	 * @return the lineSpacing
	 */
	public Float getLineSpacing() {
		return lineSpacing;
	}
	/**
	 * @param lineSpacing the lineSpacing to set
	 */
	public void setLineSpacing(Float lineSpacing) {
		this.lineSpacing = lineSpacing;
	}
	
}
