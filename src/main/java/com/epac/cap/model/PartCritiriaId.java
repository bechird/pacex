package com.epac.cap.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * PartCritiriaId class representing the identifier of a part critiria
 */
@Embeddable
public class PartCritiriaId implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2229375071289602652L;
	private String partNum;
	private String critiriaId;

	/**
	 * Default constructor
	 */
	public PartCritiriaId() {
	}

	/**
	 * Constructor which sets all of the properties
	 */
	public PartCritiriaId(String partNum, String critiriaId) {
		this.partNum = partNum;
		this.critiriaId = critiriaId;
	}

	/**
	 * Accessor methods for partNum
	 *
	 * @return partNum  
	 */

	@Column(name = "Part_Num", nullable = false, length = 25)
	public String getPartNum() {
		return this.partNum;
	}

	/**
	 * @param partNum the partNum to set
	 */
	public void setPartNum(String partNum) {
		this.partNum = partNum;
	}

	/**
	 * Accessor methods for critiriaId
	 *
	 * @return critiriaId  
	 */

	@Column(name = "Critiria_Id", nullable = false, length = 25)
	public String getCritiriaId() {
		return this.critiriaId;
	}

	/**
	 * @param critiriaId the critiriaId to set
	 */
	public void setCritiriaId(String critiriaId) {
		this.critiriaId = critiriaId;
	}

	@Override
	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof PartCritiriaId))
			return false;
		PartCritiriaId castOther = (PartCritiriaId) other;

		return ((this.getPartNum() == castOther.getPartNum()) || (this.getPartNum() != null
				&& castOther.getPartNum() != null && this.getPartNum().equals(castOther.getPartNum())))
				&& ((this.getCritiriaId() == castOther.getCritiriaId())
						|| (this.getCritiriaId() != null && castOther.getCritiriaId() != null
								&& this.getCritiriaId().equals(castOther.getCritiriaId())));
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + (getPartNum() == null ? 0 : this.getPartNum().hashCode());
		result = 37 * result + (getCritiriaId() == null ? 0 : this.getCritiriaId().hashCode());
		return result;
	}

}
