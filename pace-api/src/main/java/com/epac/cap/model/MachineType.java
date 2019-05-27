package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * MachineType class representing a machine type
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "machine_Type")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Machine_Type_Id", unique = true, nullable = false, length = 25), name = "id"),
    @AttributeOverride(column = @Column(name = "Name", nullable = false, length = 55), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 100), name = "description")})
public class MachineType extends LookupItem {
	
	private String stationCategoryId;
	
	/**
	 * 
	 */
	public MachineType() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 */
	public MachineType(String id, String name) {
		super(id, name);
	}
	
	public MachineType(LookupItem item2) {
		super(item2);
	}

	public enum types { 
		_ALL("ALL"),
		_4C("4C"),
		_1C("1C"),
		PLOWFOLDER("PLOWFOLDER"),
		FLYFOLDER("FLYFOLDER"),
		POPLINE("POPLINE")
		;

		private String name;
		private types(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}

	/**
	 * @return the stationCategoryId
	 */
	@Column(name = "Station_Category_Id", length = 25)
	public String getStationCategoryId() {
		return stationCategoryId;
	}

	/**
	 * @param stationCategoryId the stationCategoryId to set
	 */
	public void setStationCategoryId(String stationCategoryId) {
		this.stationCategoryId = stationCategoryId;
	}
	
	
	
}
