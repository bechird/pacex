package com.epac.cap.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.epac.cap.common.AuditableBean;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Entity
@Table(name = "WFS_Progress")
@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WFSProgress extends AuditableBean implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer progressId;
	private WFSPartWorkflow partWorkflow;
	private WFSSequence sequence;
	private WFSDataSupport dataSupport;
	private String status;
	private Long starts;
	private Long ends;
	
	
	public WFSProgress() {
		// TODO Auto-generated constructor stub
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "progressid", unique = true, nullable = false, length = 25)
	public Integer getProgressId() {
		return progressId;
	}

	@ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "partWorkflowId")
	@JsonIgnore
	public WFSPartWorkflow getPartWorkflow() {
		return partWorkflow;
	}
	
	public void setPartWorkflow(WFSPartWorkflow partWorkflow) {
		this.partWorkflow = partWorkflow;
	}
	
	@Column(name = "status", length = 55)
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	@Column(name = "starts")
	public Long getStarts() {
		return starts;
	}
	
	public void setStarts(Long starts) {
		this.starts = starts;
	}
	
	@Column(name = "ends")
	public Long getEnds() {
		return ends;
	}
	
	public void setEnds(Long ends) {
		this.ends = ends;
	}
	
	
	/**
	 * @return the sequence
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "sequenceid")
	public WFSSequence getSequence() {
		return sequence;
	}

	/**
	 * @param sequence the sequence to set
	 */
	public void setSequence(WFSSequence sequence) {
		this.sequence = sequence;
	}

	/**
	 * @return the dataSupport
	 */
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "dataSupport_Id", referencedColumnName = "iddatasupport")
	public WFSDataSupport getDataSupport() {
		return dataSupport;
	}

	/**
	 * @param dataSupport the dataSupport to set
	 */
	public void setDataSupport(WFSDataSupport dataSupport) {
		this.dataSupport = dataSupport;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getProgressId() == null) ? 0 : getProgressId().hashCode());
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
		if (!(obj instanceof WFSProgress)) {
			return false;
		}
		WFSProgress other = (WFSProgress) obj;
		if (getProgressId() == null) {
			if (other.getProgressId() != null) {
				return false;
			}
		} else if (!getProgressId().equals(other.getProgressId())) {
			return false;
		}
		return true;
	}


	public void setProgressId(Integer progressId) {
		this.progressId = progressId;
	}
	
/*
	@Override
	public int compareTo(WFSProgress that) {
			if(this.getSequence().getRanking() > that.getSequence().getRanking())
				return 1;
				
			if(this.getSequence().getRanking() < that.getSequence().getRanking())
				return -1;
			
			return 0;
	}
*/
}
