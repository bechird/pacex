package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * BatchStatus class representing a batch status
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "batch_status")
@AttributeOverrides({
		@AttributeOverride(column = @Column(name = "Batch_Status_Id", unique = true, nullable = false, length = 25), name = "id"),
		@AttributeOverride(column = @Column(name = "Name", nullable = false, length = 55), name = "name"),
		@AttributeOverride(column = @Column(name = "Description", length = 100), name = "description") })
public class BatchStatus extends LookupItem {

	/**
	 * 
	 */
	public BatchStatus() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 */
	public BatchStatus(String id, String name) {
		super(id, name);
	}

	public BatchStatus(LookupItem item2) {
		super(item2);
	}

	public enum statuses {
		NEW("NEW"), ASSIGNED("ASSIGNED"), ONPROD("ONPROD"),COMPLETE("COMPLETE"),COMPLETE_PARTIAL("COMPLETE_PARTIAL"),CANCELLED("CANCELLED");

		private String name;

		private statuses(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
	

}
