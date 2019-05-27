package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * LogCause class representing a log cause
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "log_Cause")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Log_Cause_Id", unique = true, nullable = false, length = 25), name = "id"),
    @AttributeOverride(column = @Column(name = "Name", nullable = false, length = 55), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 100), name = "description")})
public class LogCause extends LookupItem {
	//@JsonIgnore
	//private Set<Log> logs = new HashSet<Log>(0);
	
	/**
	 * 
	 */
	public LogCause() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 */
	public LogCause(String id, String name) {
		super(id, name);
	}
	
	public LogCause(LookupItem item2) {
		super(item2);
	}

	public enum causes { 
		JOBSCOMPLETE("JOBSCOMPLETE"),
		BREAK("BREAK"),
		ONOFF("ONOFF"),
		ENDSHIFT("ENDSHIFT"),
		ISSUE("ISSUE"),
		SERVICE("SERVICE"),
		USERDECISION("USERDECISION"),
		WASTE("WASTE");

		private String name;
		private causes(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}
	
	/**
	 * @return the logs
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "cause")
	public Set<Log> getLogs() {
		return logs;
	}*/

	/**
	 * @param logs the logs to set
	 
	public void setLogs(Set<Log> logs) {
		this.logs = logs;
	}*/
	
}
