package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * JobStatus class representing a job status
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "job_Status")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Job_Status_Id", unique = true, nullable = false, length = 25), name = "id"),
    @AttributeOverride(column = @Column(name = "Name", nullable = false, length = 55), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 100), name = "description")})
public class JobStatus extends LookupItem {
	//@JsonIgnore
	//private Set<Job> jobs = new HashSet<Job>(0);
	
	/**
	 * 
	 */
	public JobStatus() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 */
	public JobStatus(String id, String name) {
		super(id, name);
	}
	
	public JobStatus(LookupItem item2) {
		super(item2);
	}
	
	public enum JobStatuses { 
		NEW("NEW"),
		SCHEDULED("SCHEDULED"),
		ASSIGNED("ASSIGNED"),
		RUNNING("RUNNING"),
		COMPLETE("COMPLETE"),
		COMPLETE_PARTIAL("COMPLETE_PARTIAL"),
		CANCELLED("CANCELLED");

		private String name;
		private JobStatuses(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}

	/**
	 * @return the jobs
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "status")
	public Set<Job> getJobs() {
		return jobs;
	}*/

	/**
	 * @param jobs the jobs to set
	
	public void setJobs(Set<Job> jobs) {
		this.jobs = jobs;
	} */
	
	
}
