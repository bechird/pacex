package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name = "WFS_ProductionStatus")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "productionStatusId", unique = true, nullable = false, length = 25), name = "id"),
    @AttributeOverride(column = @Column(name = "name", nullable = false, length = 55), name = "name"),
    @AttributeOverride(column = @Column(name = "description", length = 100), name = "description")})
public class WFSProductionStatus extends LookupItem{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3662129792125104615L;

	public enum statuses { 
		ONPROD("1"),
		OBSOLETE("2"),
		TEMPORARY("3"),
		INITIAL("4");

		private String name;
		private statuses(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}
}
