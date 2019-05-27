package com.epac.cap.model;

import java.util.Date;
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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.SortComparator;

import com.epac.cap.common.AuditableBean;
import com.epac.cap.handler.SectionsByBatchOrderingComparator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * CoverBatch a class representing a batch of covers
 */
@Entity
@Table(name = "coverBatch")
public class CoverBatch extends AuditableBean implements java.io.Serializable {

	private static final long serialVersionUID = -6880385451019237261L;

	private Integer coverBatchId;
	private String coverBatchName;
	private BatchStatus status;
	private Integer quantity;
	private Date dueDate;
	private String priority;
	private Roll roll;
	
	private SortedSet<CoverSection> sections = new TreeSet<CoverSection>(new SectionsByBatchOrderingComparator());

	public CoverBatch() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CoverBatch(Integer coverBatchId, String coverBatchName, BatchStatus status, Integer quantity, Date dueDate,
			String priority, Roll roll, SortedSet<CoverSection> sections) {
		super();
		this.coverBatchId = coverBatchId;
		this.coverBatchName = coverBatchName;
		this.status = status;
		this.quantity = quantity;
		this.dueDate = dueDate;
		this.priority = priority;
		this.roll = roll;
		this.sections = sections;
	}

	/**
	 * Accessor methods for batchId
	 *
	 * @return batchId  
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "Batch_Id", unique = true, nullable = false)
	public Integer getCoverBatchId() {
		return coverBatchId;
	}
	
	/**
	 * @param batchId the Cover batchId to set
	 */
	public void setCoverBatchId(Integer coverBatchId) {
		this.coverBatchId = coverBatchId;
	}

	/**
	 * Accessor methods for coverBatchName
	 *
	 * @return coverBatchName  
	 */

	@Column(name = "Batch_Name", length = 15)
	public String getCoverBatchName() {
		return coverBatchName;
	}
	
	/**
	 * @param coverBatchName the coverBatchName to set
	 */
	public void setCoverBatchName(String coverBatchName) {
		this.coverBatchName = coverBatchName;
	}
	
	/**
	 * Accessor methods for status
	 *
	 * @return status  
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Status")
	public BatchStatus getStatus() {
		return status;
	}
	
	/**
	 * @param status the status to set
	 */
	public void setStatus(BatchStatus status) {
		this.status = status;
	}
	
	/**
	 * Accessor methods for quantity
	 *
	 * @return quantity  
	 */
	@Column(name = "Quantity")
	public Integer getQuantity() {
		return quantity;
	}
	
	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	/**
	 * Accessor methods for roll
	 *
	 * @return roll 
	 */	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Roll_Id")
	public Roll getRoll() {
		return roll;
	}
	
	/**
	 * @param roll the roll to set
	 */
	public void setRoll(Roll roll) {
		this.roll = roll;
	}
	
	/**
	 * retrieves the most urgent priority of all jobs assigned to the coverBatch
	 */
	//@Transient
	public String getPriority(){
		return this.priority;
	}
	
	public void setPriority(String priority){
		this.priority = priority;
	}
	
	/**
	 * retrieves the most urgent due date of all jobs assigned to the Cover batch
	 */
	//@Transient
	public Date getDueDate(){
		return this.dueDate;
	}
	
	public void setDueDate(Date dueDate){
		this.dueDate = dueDate;
	}

	/**
	 * Accessor methods for sections
	 * @return sections
	 */
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)	
	@JoinColumn(name="batchId", referencedColumnName="Batch_Id")
	@SortComparator(SectionsByBatchOrderingComparator.class)
	public SortedSet<CoverSection> getSections() {
		return sections;
	}
	
	/**
	 * @param sections the sections to set
	 */
	public void setSections(SortedSet<CoverSection> sections) {
		this.sections = sections;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((coverBatchId == null) ? 0 : coverBatchId.hashCode());
		result = prime * result + ((coverBatchName == null) ? 0 : coverBatchName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CoverBatch other = (CoverBatch) obj;
		if (coverBatchId == null) {
			if (other.coverBatchId != null)
				return false;
		} else if (!coverBatchId.equals(other.coverBatchId))
			return false;
		if (coverBatchName == null) {
			if (other.coverBatchName != null)
				return false;
		} else if (!coverBatchName.equals(other.coverBatchName))
			return false;
		return true;
	}

}
