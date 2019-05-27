package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * RollType class representing a roll type
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "roll_Type")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Roll_Type_Id", unique = true, nullable = false, length = 25), name = "id"),
    @AttributeOverride(column = @Column(name = "Name", nullable = false, length = 55), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 100), name = "description")})
public class RollType extends LookupItem {
	//@JsonIgnore
	//private Set<Roll> rolls = new HashSet<Roll>(0);
	
	/**
	 * 
	 */
	public RollType() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 */
	public RollType(String id, String name) {
		super(id, name);
	}
	
	public RollType(LookupItem item2) {
		super(item2);
	}

	public enum types { 
		LEFTOVER("LEFTOVER"),
		NEW("NEW"),
		PRODUCED("PRODUCED")
		;

		private String name;
		private types(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}
	/**
	 * @return the rolls
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "rollType")
	public Set<Roll> getRolls() {
		return rolls;
	}*/

	/**
	 * @param rolls the rolls to set
	 
	public void setRolls(Set<Roll> rolls) {
		this.rolls = rolls;
	}*/
	
}
