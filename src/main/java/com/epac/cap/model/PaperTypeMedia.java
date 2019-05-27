package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * PaperTypeMedia class representing a media support of a paper type
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "paper_Type_Media")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Paper_Type_Media_Id", unique = true, nullable = false, length = 55), name = "id"),
    @AttributeOverride(column = @Column(name = "Name", nullable = false, length = 55), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 100), name = "description")})
@JsonTypeName("paperTypeMediaTp")
public class PaperTypeMedia extends LookupItem {
	//@JsonIgnore
	//private Set<Part> parts = new HashSet<Part>(0);
	
	private String paperTypeId;
	private Integer rollLength;
	private float rollWidth;
	
	/**
	 * 
	 */
	public PaperTypeMedia() {
	}

	/**
	 * @param id
	 * @param name
	 */
	public PaperTypeMedia(String id, String name) {
		super(id, name);
	}
	
	public PaperTypeMedia(LookupItem item2) {
		super(item2);
	}

	/**
	 * @return the rollLength
	 */
	@Column(name = "Roll_Length")
	public Integer getRollLength() {
		return rollLength;
	}

	/**
	 * @param rollLength the rollLength to set
	 */
	public void setRollLength(Integer rollLength) {
		this.rollLength = rollLength;
	}

	/**
	 * @return the rollWidth
	 */
	@Column(name = "Roll_Width")
	public float getRollWidth() {
		return rollWidth;
	}

	/**
	 * @param rollWidth the rollWidth to set
	 */
	public void setRollWidth(float rollWidth) {
		this.rollWidth = rollWidth;
	}

	/**
	 * @return the paperTypeId
	 */
	@Column(name = "Paper_Type_Id", length = 60)
	public String getPaperTypeId() {
		return paperTypeId;
	}

	/**
	 * @param paperTypeId the paperTypeId to set
	 */
	public void setPaperTypeId(String paperTypeId) {
		this.paperTypeId = paperTypeId;
	}
	
}
