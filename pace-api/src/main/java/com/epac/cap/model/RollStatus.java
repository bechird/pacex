package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * RollStatus class representing a roll status
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "roll_status")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Roll_Status_Id", unique = true, nullable = false, length = 25), name = "id"),
    @AttributeOverride(column = @Column(name = "Name", nullable = false, length = 55), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 100), name = "description")})
public class RollStatus extends LookupItem {
	//@JsonIgnore
	//private Set<Roll> rolls = new HashSet<Roll>(0);
	
	/**
	 * 
	 */
	public RollStatus() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 */
	public RollStatus(String id, String name) {
		super(id, name);
	}
	
	public RollStatus(LookupItem item2) {
		super(item2);
	}
	
	public enum statuses {
		AVAILABLE("AVAILABLE"),
		NEW("NEW"),
		SCHEDULED("SCHEDULED"),
		ASSIGNED("ASSIGNED"),
		ONPROD("ONPROD"),
		EXHAUSTED("EXHAUSTED")
		;

		private String name;
		private statuses(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}

	/**
	 * @return the rolls
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "status")
	public Set<Roll> getRolls() {
		return rolls;
	} */

	/**
	 * @param rolls the rolls to set
	 
	public void setRolls(Set<Roll> rolls) {
		this.rolls = rolls;
	}*/
	
}
