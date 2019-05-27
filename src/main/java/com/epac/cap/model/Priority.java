package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Priority class representing a priority (could be for order, roll, job...)
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "priority")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Priority_Id", unique = true, nullable = false, length = 25), name = "id"),
    @AttributeOverride(column = @Column(name = "Name", nullable = false, length = 55), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 100), name = "description")})
public class Priority extends LookupItem {
	//@JsonIgnore
	//private Set<Job> jobs = new HashSet<Job>(0);
	//@JsonIgnore
	//private Set<Order> orders = new HashSet<Order>(0);
	
	/**
	 * 
	 */
	public Priority() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 */
	public Priority(String id, String name) {
		super(id, name);
	}
	
	public Priority(LookupItem item2) {
		super(item2);
	}

	public enum Priorities { 
		HIGH_SS("HIGH**"),
		HIGH_S("HIGH*"),
		HIGH("HIGH"),
		NORMAL("NORMAL");

		private String name;
		private Priorities(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}
	
	/**
	 * @return the jobs
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "jobPriority")
	public Set<Job> getJobs() {
		return jobs;
	}*/

	/**
	 * @param jobs the jobs to set
	 
	public void setJobs(Set<Job> jobs) {
		this.jobs = jobs;
	}*/

	/**
	 * @return the orders
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "priority")
	public Set<Order> getOrders() {
		return orders;
	}*/

	/**
	 * @param orders the orders to set
	 
	public void setOrders(Set<Order> orders) {
		this.orders = orders;
	}*/
	
}
