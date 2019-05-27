/**
 * 
 */
package com.epac.cap.functionel;

import java.util.SortedSet;
import java.util.TreeSet;

import com.epac.cap.common.PNLTemplateLineComparableDeserializer;
import com.epac.cap.handler.PNLTemplateLinesPerOrderingComparator;
import com.epac.cap.model.PNLTemplateLine;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 *  Class representing a PNL Information bean to communicate to the Imposer Service
 */
public class PNLInfo {
	
	private Integer pageNumber;
	private Float hMargin;
	private Float vMargin;
	private Float lineSpacing;
	@JsonDeserialize(using = PNLTemplateLineComparableDeserializer.class)
	private SortedSet<PNLTemplateLine> pnlLines = new TreeSet<PNLTemplateLine>(new PNLTemplateLinesPerOrderingComparator());
	// For Preview:
	private Float pageWidth;
	private Float pageHeight;
	private String notes;
	
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
	public SortedSet<PNLTemplateLine> getPnlLines() {
		return pnlLines;
	}
	/**
	 * @param pnlLines the pnlLines to set
	 */
	public void setPnlLines(SortedSet<PNLTemplateLine> pnlLines) {
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
	
	/**
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}
	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public String getTemplateId(){
		String result = null;
		if(!pnlLines.isEmpty()){
			result = pnlLines.iterator().next().getTemplateId();
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Notes ["+getNotes()+"] \n PNLInfo [templateId= " + getTemplateId() + ", pageNumber=" + pageNumber + ", hMargin=" + hMargin + ", vMargin=" + vMargin + ", lineSpacing="
				+ lineSpacing + ",\n pnlLines=" + pnlLines + "]";
	}
	

}
