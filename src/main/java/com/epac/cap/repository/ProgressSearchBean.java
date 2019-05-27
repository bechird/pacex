package com.epac.cap.repository;

import com.epac.cap.common.AuditableSearchBean;

@SuppressWarnings("serial")
public class ProgressSearchBean extends AuditableSearchBean{

	private Integer progressId;
	private String status;
	private long starts;
	private long ends;
	private Integer statusId;
	private Integer sequenceId;
	private String partNumb;
	
	public ProgressSearchBean(){
	}

	public Integer getProgressId() {
		return progressId;
	}

	public void setProgressId(Integer progressId) {
		this.progressId = progressId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getStarts() {
		return starts;
	}

	public void setStarts(long starts) {
		this.starts = starts;
	}

	public long getEnds() {
		return ends;
	}

	public void setEnds(long ends) {
		this.ends = ends;
	}

	public Integer getStatusId() {
		return statusId;
	}

	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}

	/**
	 * @return the partNumb
	 */
	public String getPartNumb() {
		return partNumb;
	}

	/**
	 * @param partNumb the partNumb to set
	 */
	public void setPartNumb(String partNumb) {
		this.partNumb = partNumb;
	}

	/**
	 * @return the sequenceId
	 */
	public Integer getSequenceId() {
		return sequenceId;
	}

	/**
	 * @param sequenceId the sequenceId to set
	 */
	public void setSequenceId(Integer sequenceId) {
		this.sequenceId = sequenceId;
	}

	
}
