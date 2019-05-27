package com.epac.cap.repository;

import com.epac.cap.common.AuditableSearchBean;
import com.epac.cap.model.Job;

/**
 * Container for Cover Batch Job search criteria.
 * 
 * @author slimj
 * 
 */

@SuppressWarnings("serial")
public class CoverBatchJobSearchBean extends AuditableSearchBean {

	private Integer id;
	private Job job;
	private Integer quantity;

	/**
	 * Default constructor. Sets the default ordering to coverBatchJob Id ascending
	 *
	 **/
	public CoverBatchJobSearchBean() {
		setOrderBy("id");
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

}
