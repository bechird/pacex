package com.epac.cap.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.epac.cap.common.AuditableBean;

@Entity
@Table(name = "WFS_Workflow")
public class WFSWorkflow extends AuditableBean implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer workflowId;
	private String name;
	private Boolean enable;
	private Set<WFSSequence> sequences = new HashSet<WFSSequence>();
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "idworkflow", unique = true, nullable = false, length = 25)
	public Integer getWorkflowId() {
		return workflowId;
	}
	
	private void setWorkflowId(Integer workflowId) {
		this.workflowId = workflowId;
	}
	
	@Column(name = "name", length = 55)
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Column(name = "enable")
	public Boolean isEnable() {
		return enable;
	}
	
	public void setEnable(Boolean enable) {
		this.enable = enable;
	}
	

	/**
	 * @return the sequences
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "workflowId", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<WFSSequence> getSequences() {
		return sequences;
	}

	/**
	 * @param sequences the sequences to set
	 */
	public void setSequences(Set<WFSSequence> sequences) {
		this.sequences = sequences;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getWorkflowId() == null) ? 0 : getWorkflowId().hashCode());
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
		if (!(obj instanceof WFSWorkflow)) {
			return false;
		}
		WFSWorkflow other = (WFSWorkflow) obj;
		if (getWorkflowId() == null) {
			if (other.getWorkflowId() != null) {
				return false;
			}
		} else if (!getWorkflowId().equals(other.getWorkflowId())) {
			return false;
		}
		return true;
	}
	
	

}
