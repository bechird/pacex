package com.epac.cap.repository;

import com.epac.cap.common.AuditableSearchBean;

@SuppressWarnings("serial")
public class SequenceSearchBean extends AuditableSearchBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer sequenceId;
	private Integer ranking;
	private Integer workflowId;
	private Integer actionId; 
	private Integer progressId;
	
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
	
	/**
	 * @return the ranking
	 */
	public Integer getRanking() {
		return ranking;
	}
	
	/**
	 * @param ranking the ranking to set
	 */
	public void setRanking(Integer ranking) {
		this.ranking = ranking;
	}
	
	/**
	 * @return the workflowId
	 */
	public Integer getWorkflowId() {
		return workflowId;
	}
	
	/**
	 * @param workflowId the workflowId to set
	 */
	public void setWorkflowId(Integer workflowId) {
		this.workflowId = workflowId;
	}
	
	/**
	 * @return the actionId
	 */
	public Integer getActionId() {
		return actionId;
	}
	
	/**
	 * @param actionId the actionId to set
	 */
	public void setActionId(Integer actionId) {
		this.actionId = actionId;
	}
	
	/**
	 * @return the progressId
	 */
	public Integer getProgressId() {
		return progressId;
	}
	
	/**
	 * @param progressId the progressId to set
	 */
	public void setProgressId(Integer progressId) {
		this.progressId = progressId;
	}
	

}
