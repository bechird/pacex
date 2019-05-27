package com.epac.imposition.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 *  Class representing a PNL Template Line
 */

public class PNLTemplateLine {
	
	private Integer id;
	private String templateId;
	private Integer ordering;
	private String lineText;
	private String fontType;
	private Float fontSize;
	private Boolean fontBold;
	private Boolean fontItalic;
	/**
	 * 
	 */
	public PNLTemplateLine() {
		super();
	}
	
	/**
	 * @param id
	 */
	public PNLTemplateLine(Integer id) {
		super();
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * @return the templateId
	 */
	public String getTemplateId() {
		return templateId;
	}
	/**
	 * @param templateId the templateId to set
	 */
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
	/**
	 * @return the ordering
	 */
	public Integer getOrdering() {
		return ordering;
	}
	/**
	 * @param ordering the ordering to set
	 */
	public void setOrdering(Integer ordering) {
		this.ordering = ordering;
	}
	/**
	 * @return the lineText
	 */
	public String getLineText() {
		return lineText;
	}
	/**
	 * @param lineText the lineText to set
	 */
	public void setLineText(String lineText) {
		this.lineText = lineText;
	}
	/**
	 * @return the fontType
	 */
	public String getFontType() {
		return fontType;
	}
	/**
	 * @param fontType the fontType to set
	 */
	public void setFontType(String fontType) {
		this.fontType = fontType;
	}
	/**
	 * @return the fontSize
	 */
	public Float getFontSize() {
		return fontSize;
	}
	/**
	 * @param fontSize the fontSize to set
	 */
	public void setFontSize(Float fontSize) {
		this.fontSize = fontSize;
	}
	
	public Boolean getFontBold() {
		return fontBold;
	}

	public void setFontBold(Boolean fontBold) {
		this.fontBold = fontBold;
	}

	public Boolean getFontItalic() {
		return fontItalic;
	}

	public void setFontItalic(Boolean fontItalic) {
		this.fontItalic = fontItalic;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PNLTemplateLine)) {
			return false;
		}
		PNLTemplateLine other = (PNLTemplateLine) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

}
