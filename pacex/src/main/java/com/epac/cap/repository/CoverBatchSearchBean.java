package com.epac.cap.repository;

import java.util.Date;

import javax.persistence.Column;

import com.epac.cap.common.AuditableSearchBean;
import com.epac.cap.model.BatchStatus;
import com.epac.cap.model.Lamination;

/**
 * Container for Cover Batch search criteria.
 * 
 * @author slimj
 * 
 */

@SuppressWarnings("serial")
public class CoverBatchSearchBean extends AuditableSearchBean{
	
	private Integer coverBatchId;
	private String coverBatchName;
	private String machineId;
	private String status;
	private String coverBatchTag;
	private Integer quantity;
	private Integer rollId;
	private Lamination laminationType;
	private String paperType;
	private Date dueDate;
	private String priority;
	private Integer machineOrdering;

	
	/**
	 * Default constructor.  Sets the default ordering to coverBatchId ascending 
	 *
	 **/
	public CoverBatchSearchBean() {
		setOrderBy("coverBatchId");
	}



	/**
	 * Accessor methods for batchId
	 *
	 * @return batchId  
	 */
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
	 * Accessor methods for machine
	 *
	 * @return machine  
	 */
	public String getMachineId() {
		return machineId;
	}
	
	/**
	 * @param machine the machine to set
	 */
	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}
	
	/**
	 * Accessor methods for status
	 *
	 * @return status  
	 */
	public String getStatus() {
		return status;
	}
	
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * Accessor methods for coverBatchTag
	 *
	 * @return coverBatchTag  
	 */
	public String getCoverBatchTag() {
		return this.coverBatchTag;
	}

	/**
	 * @param coverBatchTag the coverBatchTag to set
	 */
	public void setCoverBatchTag(String bTag) {
		this.coverBatchTag = bTag;
	}	
	/**
	 * Accessor methods for quantity
	 *
	 * @return quantity  
	 */
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
	 * Accessor methods for rollId
	 *
	 * @return rollId  
	 */	
	public Integer getRollId() {
		return rollId;
	}
	
	/**
	 * @param rollId the rollId to set
	 */
	public void setRollId(Integer rollId) {
		this.rollId = rollId;
	}

	/**
	 * Accessor methods for lamination
	 *
	 * @return lamination  
	 */
	public Lamination getLaminationType() {
		return laminationType;
	}

	/**
	 * @param lamination the lamination to set
	 */
	public void setLaminationType(Lamination laminationType) {
		this.laminationType = laminationType;
	}
	
	/**
	 * Accessor methods for paperType
	 *
	 * @return paperType  
	 */
	public String getPaperType() {
		return paperType;
	}

	/**
	 * @param paperType the paperType to set
	 */
	public void setPaperType(String paperType) {
		this.paperType = paperType;
	}
	
	/**
	 * retrieves the most urgent priority of all jobs assigned to the coverBatch
	 */
	public String getPriority(){
		return this.priority;
	}
	
	public void setPriority(String priority){
		this.priority = priority;
	}
	
	/**
	 * retrieves the most urgent due date of all jobs assigned to the Cover batch
	 */
	public Date getDueDate(){
		return this.dueDate;
	}
	
	public void setDueDate(Date dueDate){
		this.dueDate = dueDate;
	}
	/**
	 * Accessor methods for machineOrdering
	 *
	 * @return machineOrdering  
	 */
	public Integer getMachineOrdering() {
		return this.machineOrdering;
	}

	/**
	 * @param machineOrdering the machineOrdering to set
	 */
	public void setMachineOrdering(Integer machineOrdering) {
		this.machineOrdering = machineOrdering;
	}	
}
