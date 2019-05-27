package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Lamination class representing a lamination type
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "lamination_Type")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Lamination_Type_Id", unique = true, nullable = false, length = 25), name = "id"),
    @AttributeOverride(column = @Column(name = "Name", nullable = false, length = 55), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 100), name = "description")})
@JsonTypeName("laminationTp")
public class Lamination extends LookupItem {
	//@JsonIgnore
	//private Set<Part> parts = new HashSet<Part>(0);
	
	/**
	 * 
	 */
	public Lamination() {
	}
	
	/**
	 * @param id
	 */
	public Lamination(String id) {
		super(id);
	}

	/**
	 * @param id
	 * @param name
	 */
	public Lamination(String id, String name) {
		super(id, name);
	}
	
	public Lamination(LookupItem item2) {
		super(item2);
	}

	/**
	 * @return the parts
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "lamination")
	public Set<Part> getParts() {
		return parts;
	}*/

	/**
	 * @param parts the parts to set
	 
	public void setParts(Set<Part> parts) {
		this.parts = parts;
	}*/
	
	
}
