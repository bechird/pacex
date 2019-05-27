package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * BindingType class representing a binding type
 */
@Entity
@Table(name = "binding_Type")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Binding_Type_Id", unique = true, nullable = false, length = 25), name = "id"),
    @AttributeOverride(column = @Column(name = "Name", nullable = false, length = 55), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 100), name = "description")})
//@JsonTypeName("bindingTypeTp")
public class BindingType extends LookupItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2483600973152147591L;
	//@JsonIgnore
	//private Set<Part> parts = new HashSet<Part>(0);
	
	/**
	 * 
	 */
	public BindingType() {
	}

	/**
	 * @param id
	 * @param name
	 */
	public BindingType(String id, String name) {
		super(id, name);
	}
	
	public BindingType(LookupItem item2) {
		super(item2);
	}

	/**
	 * @return the parts
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "bindingType")
	public Set<Part> getParts() {
		return parts;
	}*/

	/**
	 * @param parts the parts to set
	 
	public void setParts(Set<Part> parts) {
		this.parts = parts;
	}*/
	
}
