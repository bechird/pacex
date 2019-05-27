package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * JobType class representing a job type
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "job_Type")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Job_Type_Id", unique = true, nullable = false, length = 25), name = "id"),
    @AttributeOverride(column = @Column(name = "Name", nullable = false, length = 55), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 100), name = "description")})
public class JobType extends LookupItem {
	//@JsonIgnore
	//private Set<Job> jobs = new HashSet<Job>(0);
	
	/**
	 * 
	 */
	public JobType() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 */
	public JobType(String id, String name) {
		super(id, name);
	}
	
	public JobType(LookupItem item2) {
		super(item2);
	}

	public enum JobTypes { 
		BINDING("BINDING"),
		PRINTING("PRINTING"),
		PRINTING_2UP("PRINTING_2UP"),
		PRINTING_3UP("PRINTING_3UP"),
		PRINTING_4UP("PRINTING_4UP"),
		PRINTING_POPLINE("PRINTING_POPLINE"),
		PRINTING_FLYFOLDER("PRINTING_FLYFOLDER"),
		PRINTING_PLOWFOLDER("PRINTING_PLOWFOLDER")
		;

		private String name;
		private JobTypes(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}
	/**
	 * @return the jobs
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "jobType")
	public Set<Job> getJobs() {
		return jobs;
	}*/

	/**
	 * @param jobs the jobs to set
	 
	public void setJobs(Set<Job> jobs) {
		this.jobs = jobs;
	}*/
	
	
}
