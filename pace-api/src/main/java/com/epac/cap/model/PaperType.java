package com.epac.cap.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * PaperType class representing a paper type
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "paper_Type")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Paper_Type_Id", unique = true, nullable = false, length = 60), name = "id"),
    @AttributeOverride(column = @Column(name = "Name", nullable = false, length = 60), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 100), name = "description")})
@JsonTypeName("paperTypeTp")
public class PaperType extends LookupItem {
	//@JsonIgnore
	//private Set<Part> parts = new HashSet<Part>(0);
	
	private String shortName;
	private Float thickness;
	private Float weight;
	private String dropFolder;
	private Set<PaperTypeMedia> medias = new HashSet<PaperTypeMedia>(0);
	
	/**
	 * 
	 */
	public PaperType() {
	}

	/**
	 * @param id
	 * @param name
	 */
	public PaperType(String id, String name) {
		super(id, name);
	}
	
	public PaperType(LookupItem item2) {
		super(item2);
	}

	public enum types { 
		ALL("ALL"),
		REGULAR("REGULAR");

		private String name;
		private types(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}

	/**
	 * @return the medias
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "paperTypeId")
	@OrderBy("rollWidth")
	public Set<PaperTypeMedia> getMedias() {
		return medias;
	}

	/**
	 * @param medias the medias to set
	 */
	public void setMedias(Set<PaperTypeMedia> medias) {
		this.medias = medias;
	}

	/**
	 * @return the shortName
	 */
	@Column(name = "shortName",  length = 30)
	public String getShortName() {
		return shortName;
	}

	/**
	 * @param shortName the shortName to set
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * @return the thickness
	 */
	@Column(name = "Thickness")
	public Float getThickness() {
		return thickness;
	}

	/**
	 * @param thickness the thickness to set
	 */
	public void setThickness(Float thickness) {
		this.thickness = thickness;
	}

	/**
	 * @return the weight
	 */
	@Column(name = "Weight")
	public Float getWeight() {
		return weight;
	}

	/**
	 * @param weight the weight to set
	 */
	public void setWeight(Float weight) {
		this.weight = weight;
	}

	/**
	 * @return the dropFolder
	 */
	public String getDropFolder() {
		return dropFolder;
	}

	/**
	 * @param dropFolder the dropFolder to set
	 */
	public void setDropFolder(String dropFolder) {
		this.dropFolder = dropFolder;
	}

	
	/**
	 * @return the rollLength
	 
	@Column(name = "Roll_Length")
	public Integer getRollLength() {
		return rollLength;
	}*/

	/**
	 * @param rollLength the rollLength to set
	
	public void setRollLength(Integer rollLength) {
		this.rollLength = rollLength;
	} */

	/**
	 * @return the rollWidth
	 
	@Column(name = "Roll_Width")
	public float getRollWidth() {
		return rollWidth;
	}*/

	/**
	 * @param rollWidth the rollWidth to set
	 
	public void setRollWidth(float rollWidth) {
		this.rollWidth = rollWidth;
	}*/
	
	/**
	 * @return the parts
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "paperType")
	public Set<Part> getParts() {
		return parts;
	}*/

	/**
	 * @param parts the parts to set
	 
	public void setParts(Set<Part> parts) {
		this.parts = parts;
	}*/
	
}
