package com.epac.cap.model;


/**
 * PNLData class used to hold PNL data coming from the XML order file
 */
public class PNLData implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2510210255211055281L;
	
	private Boolean pnlNotNeeded;
	private String pnlLevel;
	private String pnlTemplate;
	private String pnlLocation;
	private String pnlLanguage;
	private Integer pnlPageNumber;
	private String pnlNumber;
	private Float pnlHmargin;
	private Float pnlVmargin;
	private String pnlFontType;
	private Float pnlFontSize;
	private Boolean pnlFontBold;
	private Boolean pnlFontItalic;
	
	/**
	 * 
	 */
	public PNLData() {
		super();
	}
	/**
	 * @return the pnlNotNeeded
	 */
	public Boolean getPnlNotNeeded() {
		return pnlNotNeeded;
	}
	/**
	 * @param pnlNotNeeded the pnlNotNeeded to set
	 */
	public void setPnlNotNeeded(Boolean pnlNotNeeded) {
		this.pnlNotNeeded = pnlNotNeeded;
	}
	/**
	 * @return the pnlLevel
	 */
	public String getPnlLevel() {
		return pnlLevel;
	}
	/**
	 * @param pnlLevel the pnlLevel to set
	 */
	public void setPnlLevel(String pnlLevel) {
		this.pnlLevel = pnlLevel;
	}
	/**
	 * @return the pnlTemplate
	 */
	public String getPnlTemplate() {
		return pnlTemplate;
	}
	/**
	 * @param pnlTemplate the pnlTemplate to set
	 */
	public void setPnlTemplate(String pnlTemplate) {
		this.pnlTemplate = pnlTemplate;
	}
	/**
	 * @return the pnlLocation
	 */
	public String getPnlLocation() {
		return pnlLocation;
	}
	/**
	 * @param pnlLocation the pnlLocation to set
	 */
	public void setPnlLocation(String pnlLocation) {
		this.pnlLocation = pnlLocation;
	}
	/**
	 * @return the pnlLanguage
	 */
	public String getPnlLanguage() {
		return pnlLanguage;
	}
	/**
	 * @param pnlLanguage the pnlLanguage to set
	 */
	public void setPnlLanguage(String pnlLanguage) {
		this.pnlLanguage = pnlLanguage;
	}
	/**
	 * @return the pnlPageNumber
	 */
	public Integer getPnlPageNumber() {
		return pnlPageNumber;
	}
	/**
	 * @param pnlPageNumber the pnlPageNumber to set
	 */
	public void setPnlPageNumber(Integer pnlPageNumber) {
		this.pnlPageNumber = pnlPageNumber;
	}
	/**
	 * @return the pnlNumber
	 */
	public String getPnlNumber() {
		return pnlNumber;
	}
	/**
	 * @param pnlNumber the pnlNumber to set
	 */
	public void setPnlNumber(String pnlNumber) {
		this.pnlNumber = pnlNumber;
	}
	/**
	 * @return the hMargin
	 */
	public Float getPnlHmargin() {
		return pnlHmargin;
	}
	/**
	 * @param hMargin the hMargin to set
	 */
	public void setPnlHmargin(Float hMargin) {
		this.pnlHmargin = hMargin;
	}
	/**
	 * @return the vMargin
	 */
	public Float getPnlVmargin() {
		return pnlVmargin;
	}
	/**
	 * @param vMargin the vMargin to set
	 */
	public void setPnlVmargin(Float vMargin) {
		this.pnlVmargin = vMargin;
	}
	/**
	 * @return the pnlFontType
	 */
	public String getPnlFontType() {
		return pnlFontType;
	}
	/**
	 * @param pnlFontType the pnlFontType to set
	 */
	public void setPnlFontType(String pnlFontType) {
		this.pnlFontType = pnlFontType;
	}
	/**
	 * @return the pnlFontSize
	 */
	public Float getPnlFontSize() {
		return pnlFontSize;
	}
	/**
	 * @param pnlFontSize the pnlFontSize to set
	 */
	public void setPnlFontSize(Float pnlFontSize) {
		this.pnlFontSize = pnlFontSize;
	}
	/**
	 * @return the pnlFontBold
	 */
	public Boolean getPnlFontBold() {
		return pnlFontBold;
	}
	/**
	 * @param pnlFontBold the pnlFontBold to set
	 */
	public void setPnlFontBold(Boolean pnlFontBold) {
		this.pnlFontBold = pnlFontBold;
	}
	/**
	 * @return the pnlFontItalic
	 */
	public Boolean getPnlFontItalic() {
		return pnlFontItalic;
	}
	/**
	 * @param pnlFontItalic the pnlFontItalic to set
	 */
	public void setPnlFontItalic(Boolean pnlFontItalic) {
		this.pnlFontItalic = pnlFontItalic;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PNLData [pnlNotNeeded=" + pnlNotNeeded + ", pnlLevel=" + pnlLevel + ", pnlTemplate=" + pnlTemplate
				+ ", pnlLocation=" + pnlLocation + ", pnlLanguage=" + pnlLanguage + ", pnlPageNumber=" + pnlPageNumber
				+ ", pnlNumber=" + pnlNumber + ", hMargin=" + pnlHmargin + ", vMargin=" + pnlVmargin + ", pnlFontType="
				+ pnlFontType + ", pnlFontSize=" + pnlFontSize + ", pnlFontBold=" + pnlFontBold + ", pnlFontItalic="
				+ pnlFontItalic + "]";
	}
	
}
