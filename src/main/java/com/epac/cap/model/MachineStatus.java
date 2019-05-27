package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * MachineStatus class representing a machine status
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "machine_Status")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Machine_Status_Id", unique = true, nullable = false, length = 25), name = "id"),
    @AttributeOverride(column = @Column(name = "Name", nullable = false, length = 55), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 100), name = "description")})
public class MachineStatus extends LookupItem {
	//@JsonIgnore
	//private Set<Machine> machines = new HashSet<Machine>(0);
	
	/**
	 * 
	 */
	public MachineStatus() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 */
	public MachineStatus(String id, String name) {
		super(id, name);
	}
	
	public MachineStatus(LookupItem item2) {
		super(item2);
	}

	public enum statuses { 
		OFF("OFF"),
		ON("ON"),
		RUNNING("RUNNING"),
		SERVICE("SERVICE"),
		OUTSERVICE("OUTSERVICE")
		;

		private String name;
		private statuses(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}
	
	/**
	 * @return the machines
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "status")
	public Set<Machine> getMachines() {
		return machines;
	}*/

	/**
	 * @param machines the machines to set
	 
	public void setMachines(Set<Machine> machines) {
		this.machines = machines;
	}*/

	
}
