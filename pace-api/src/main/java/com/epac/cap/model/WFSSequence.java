package com.epac.cap.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.epac.cap.common.AuditableBean;

@Entity
@Table(name = "WFS_Sequence" )
public class WFSSequence extends AuditableBean implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer sequenceId;
	private Integer workflowId;
	private WFSAction wfAction;
	private Integer ranking;
	//private Set<WFSProgress> progresses = new HashSet<WFSProgress>() ;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "idsequence", unique = true, nullable = false, length = 25)
	public Integer getSequenceId() {
		return sequenceId;
	}
	
	private void setSequenceId(Integer sequenceId) {
		this.sequenceId = sequenceId;
	}
	
	@Column(name = "workflowid")
	public Integer getWorkflowId() {
		return workflowId;
	}
	
	public void setWorkflowId(Integer workflowId) {
		this.workflowId = workflowId;
	}
	
	/**
	 * @return the wfAction
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "actionid")
	public WFSAction getWfAction() {
		return wfAction;
	}

	/**
	 * @param wfAction the wfAction to set
	 */
	public void setWfAction(WFSAction wfAction) {
		this.wfAction = wfAction;
	}

	@Column(name = "ranking")
	public Integer getRanking() {
		return ranking;
	}
	
	public void setRanking(Integer ranking) {
		this.ranking = ranking;
	}
	
	/**
	 * @return the progresses
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "sequence", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<WFSProgress> getProgresses() {
		return progresses;
	}*/

	/**
	 * @param progresses the progresses to set
	 
	public void setProgresses(Set<WFSProgress> progresses) {
		this.progresses = progresses;
	}*/

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getSequenceId() == null) ? 0 : getSequenceId().hashCode());
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
		if (!(obj instanceof WFSSequence)) {
			return false;
		}
		WFSSequence other = (WFSSequence) obj;
		if (getSequenceId() == null) {
			if (other.getSequenceId() != null) {
				return false;
			}
		} else if (!getSequenceId().equals(other.getSequenceId())) {
			return false;
		}
		return true;
	}

	
	
}
