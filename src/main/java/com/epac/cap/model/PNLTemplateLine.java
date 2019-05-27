/**
 * 
 */
package com.epac.cap.model;

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
@Entity
@Table(name = "PNL_Template_Line")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="com.epac.cap")
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
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "Template_Line_Id", unique = true, nullable = false)
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
	@Column(name = "Template_Id", nullable = false)
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
	@Column(name = "Ordering")
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
	@Column(name = "Template_Line_Text", length = 255)
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
	@Column(name = "Font_Type", length = 55)
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
	@Column(name = "Font_Size")
	public Float getFontSize() {
		return fontSize;
	}
	/**
	 * @param fontSize the fontSize to set
	 */
	public void setFontSize(Float fontSize) {
		this.fontSize = fontSize;
	}
	
	/**
	 * @return the fontBold
	 */
	@Column(name = "Font_Bold")
	public Boolean getFontBold() {
		return fontBold;
	}

	/**
	 * @param fontBold the fontBold to set
	 */
	public void setFontBold(Boolean fontBold) {
		this.fontBold = fontBold;
	}

	/**
	 * @return the fontItalic
	 */
	@Column(name = "Font_Italic")
	public Boolean getFontItalic() {
		return fontItalic;
	}

	/**
	 * @param fontItalic the fontItalic to set
	 */
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PNLTemplateLine [lineText=" + lineText + ", fontType=" + fontType + ", fontSize=" + fontSize
				+ ", fontBold=" + fontBold + ", fontItalic=" + fontItalic + "]\n";
	}

}
