package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * SectionStatus class representing a section status
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "section_status")
@AttributeOverrides({
		@AttributeOverride(column = @Column(name = "Section_Status_Id", unique = true, nullable = false, length = 25), name = "id"),
		@AttributeOverride(column = @Column(name = "Name", nullable = false, length = 55), name = "name"),
		@AttributeOverride(column = @Column(name = "Description", length = 100), name = "description") })
public class SectionStatus extends LookupItem {

	/**
	 * 
	 */
	public SectionStatus() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 */
	public SectionStatus(String id, String name) {
		super(id, name);
	}

	public SectionStatus(LookupItem item2) {
		super(item2);
	}

	public enum statuses {
		NEW("NEW"), ASSIGNED("ASSIGNED"), ONPROD("ONPROD"), RETIRED("RETIRED"),BINDING("BINDING"),PRINTED("PRINTED");

		private String name;

		private statuses(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
	

}
