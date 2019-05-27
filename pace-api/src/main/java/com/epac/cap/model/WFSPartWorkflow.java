package com.epac.cap.model;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.SortComparator;

import com.epac.cap.common.AuditableBean;
import com.epac.cap.common.WFSProgressComparableDeserializer;
import com.epac.cap.handler.ProgressesBySequenceRankingComparator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Entity
@Table(name = "WFS_Part_Workflow")
public class WFSPartWorkflow extends AuditableBean implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 914714907382291143L;
	
	private Integer partWorkflowId;
	private String partNum;
	private WFSWorkflow workflow;
	private Boolean isReady;
	private String wfStatus;
	
	@JsonDeserialize(using = WFSProgressComparableDeserializer.class)
	private SortedSet<WFSProgress> progresses = new TreeSet<WFSProgress>(new ProgressesBySequenceRankingComparator());
	private Float rollWidth;
	
	public WFSPartWorkflow() {
		super();
	}
	
	public WFSPartWorkflow(Integer partWorkflowId) {
		super();
		this.partWorkflowId = partWorkflowId;
	}
	
	/**
	 * @return the partWorkflowId
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "partWorkflowId", unique = true, nullable = false, length = 25)
	public Integer getPartWorkflowId() {
		return partWorkflowId;
	}
	/**
	 * @param partWorkflowId the partWorkflowId to set
	 */
	public void setPartWorkflowId(Integer partWorkflowId) {
		this.partWorkflowId = partWorkflowId;
	}
	
	/**
	 * @return the partNum
	 */
	@Column(name = "partNum", length = 25)
	public String getPartNum() {
		return partNum;
	}

	/**
	 * @param partNum the partNum to set
	 */
	public void setPartNum(String partNum) {
		this.partNum = partNum;
	}
	
	/**
	 * @return the workflow
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "workflowId")
	public WFSWorkflow getWorkflow() {
		return workflow;
	}

	/**
	 * @param workflow the workflow to set
	 */
	public void setWorkflow(WFSWorkflow workflow) {
		this.workflow = workflow;
	}
	
	/**
	 * @return the progresses
	 */
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "partWorkflow", orphanRemoval = true)
	@SortComparator(ProgressesBySequenceRankingComparator.class)
	public SortedSet<WFSProgress> getProgresses() {
		return progresses;
	}

	
	public void addProgress(WFSProgress progress){
		if(progresses == null)
			progresses = new TreeSet<WFSProgress>(new ProgressesBySequenceRankingComparator());
		progresses.add(progress);
	}
	/**
	 * @param progresses the progresses to set
	 */
	public void setProgresses(SortedSet<WFSProgress> progresses) {
		this.progresses = progresses;
	}

	/**
	 * @return the wfStatus
	 */
	@Column(name = "wf_status", length = 55)
	public String getWfStatus() {
		return wfStatus;
	}

	/**
	 * @param wfStatus the wfStatus to set
	 */
	public void setWfStatus(String wfStatus) {
		this.wfStatus = wfStatus;
	}

	/**
	 * @return the isReady
	 */
	@Column(name = "wf_isready")
	public Boolean getIsReady() {
		return isReady;
	}

	/**
	 * @param isReady the isReady to set
	 */
	public void setIsReady(Boolean isReady) {
		this.isReady = isReady;
	}

	/**
	 * @return the rollWidth
	 */
	@Column(name = "wf_rollWidth")
	public Float getRollWidth() {
		return rollWidth;
	}

	/**
	 * @param rollWidth the rollWidth to set
	 */
	public void setRollWidth(Float rollWidth) {
		this.rollWidth = rollWidth;
	}
	
	/**
	 * returns the progress that is specific to the action in the param
	 */
	@Transient
	public WFSProgress getProgressByActionName(String name){
		WFSProgress result = null;
		if(!this.getProgresses().isEmpty()){
			for(WFSProgress ds : this.getProgresses()){
				if(ds.getSequence() == null)continue;
				if(ds.getSequence().getWfAction().getName().equals(name)){
					result = ds;
					break;
				}
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((partWorkflowId == null) ? 0 : partWorkflowId.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof WFSPartWorkflow)) {
			return false;
		}
		WFSPartWorkflow other = (WFSPartWorkflow) obj;
		if (partWorkflowId == null) {
			if (other.partWorkflowId != null) {
				return false;
			}
		} else if (!partWorkflowId.equals(other.partWorkflowId)) {
			return false;
		}
		return true;
	}
	
	
}
