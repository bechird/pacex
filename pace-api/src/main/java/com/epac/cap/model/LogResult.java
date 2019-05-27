package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * LogResult class representing a log result
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "log_Result")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Log_Result_Id", unique = true, nullable = false, length = 25), name = "id"),
    @AttributeOverride(column = @Column(name = "Name", nullable = false, length = 55), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 100), name = "description")})
public class LogResult extends LookupItem {
	//@JsonIgnore
	//private Set<Log> logs = new HashSet<Log>(0);
	
	/**
	 * 
	 */
	public LogResult() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 */
	public LogResult(String id, String name) {
		super(id, name);
	}
	
	public LogResult(LookupItem item2) {
		super(item2);
	}
	
	public enum results { 
		ROLL_PRODUCED("ROLL_PRODUCED"),
		LEFTOVERROLL("LEFTOVERROLL"),
		TASKENDED("TASKENDED"),
		RUNNING("RUNNING"),
		SERVICE("SERVICE"),
		REPAIR("REPAIR");

		private String name;
		private results(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}

	/**
	 * @return the logs
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "result")
	public Set<Log> getLogs() {
		return logs;
	}*/

	/**
	 * @param logs the logs to set
	 
	public void setLogs(Set<Log> logs) {
		this.logs = logs;
	}*/
	
	
}
