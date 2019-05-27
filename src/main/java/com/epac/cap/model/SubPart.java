package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * SubPart a class representing the sub parts of a part
 */
@Entity
@Table(name = "sub_Part")
public class SubPart implements java.io.Serializable, Comparable<SubPart> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4810370539022006017L;
	private SubPartId id;
	//private Part topPart;
	//private Part subPart;
	
	/**
	 * Default constructor
	 */
	public SubPart() {
		if(this.id == null){
			this.id = new SubPartId();
		}
	}
	
	/**
	 * Constructor which sets the identity property
	 */
	public SubPart(SubPartId id) {
		this.id = id;
		if(this.id == null){
			this.id = new SubPartId();
		}	
	}
	
	public SubPart(String topPartId, String subPartId){
		this.id = new SubPartId(topPartId, subPartId);
	}

	
	
	/**
	 * Accessor methods for id
	 *
	 * @return id  
	 */
	@EmbeddedId

	@AttributeOverrides({
			@AttributeOverride(name = "topPartNum", column = @Column(name = "Top_Part_Num", nullable = false, length = 25)),
			@AttributeOverride(name = "subPartNum", column = @Column(name = "Sub_Part_Num", nullable = false, length = 25)) })
	public SubPartId getId() {
		return this.id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(SubPartId id) {
		this.id = id;
	}
	
	/**
	 * @return the topPart
	 
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Top_Part_Num", referencedColumnName = "Part_Num", nullable = false, insertable = false, updatable = false)
	public Part getTopPart() {
		return topPart;
	}*/

	/**
	 * @param topPart the topPart to set
	 
	public void setTopPart(Part topPart) {
		this.topPart = topPart;
	}*/

	/**
	 * @return the subPart
	 
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Sub_Part_Num", referencedColumnName = "Part_Num", nullable = false, insertable = false, updatable = false)
	public Part getSubPart() {
		return subPart;
	}*/

	/**
	 * @param subPart the subPart to set
	 
	public void setSubPart(Part subPart) {
		this.subPart = subPart;
	}*/

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
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
		if (!(obj instanceof SubPart)) {
			return false;
		}
		SubPart other = (SubPart) obj;
		if (getId() == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (!getId().equals(other.getId())) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(SubPart o) {
		return new org.apache.commons.lang.builder.CompareToBuilder()
	    		.append(getId(), o.getId())
	    		.toComparison();
	}

	
}
