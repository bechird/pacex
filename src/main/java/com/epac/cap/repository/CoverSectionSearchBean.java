package com.epac.cap.repository;

import java.util.Date;

import javax.persistence.Column;

import com.epac.cap.common.AuditableSearchBean;
import com.epac.cap.model.BatchStatus;
import com.epac.cap.model.Lamination;
import com.epac.cap.model.PaperType;
import com.epac.cap.model.SectionStatus;

/**
 * Container for Cover section search criteria.
 * 
 * @author slimj
 * 
 */

@SuppressWarnings("serial")
public class CoverSectionSearchBean extends AuditableSearchBean{
	
	private Integer coverSectionId;
	private String coverSectionName;
	private String machineId;
	private String status;
	private Integer quantity;
	private Integer coverBatchId;
	private Lamination laminationType;
	// To be used later
	private String paperType;
	private Integer machineOrdering;
	private Date dueDate;
	private String priority;
	private String path;
	
	private String statusName;
	private String laminationTypeId;
	private Integer resultOffset;


	
	/**
	 * Default constructor.  Sets the default ordering to coverSectionId ascending 
	 *
	 **/
	public CoverSectionSearchBean() {
		setOrderBy("coverSectionId");
	}


	/**
	 * Accessor methods for coverSectionId
	 *
	 * @return coverSectionId  
	 */
	public Integer getCoverSectionId() {
		return coverSectionId;
	}


	/**
	 * @param coverSectionId the coverSectionId to set
	 */
	public void setCoverSectionId(Integer coverSectionId) {
		this.coverSectionId = coverSectionId;
	}


	/**
	 * Accessor methods for coverSectionName
	 *
	 * @return coverSectionName  
	 */
	public String getCoverSectionName() {
		return coverSectionName;
	}


	/**
	 * @param coverSectionName the coverSectionName to set
	 */
	public void setCoverSectionName(String coverSectionName) {
		this.coverSectionName = coverSectionName;
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


	public Integer getResultOffset() {
		return resultOffset;
	}


	public void setResultOffset(Integer resultOffset) {
		this.resultOffset = resultOffset;
	}


	public String getStatusName() {
		return statusName;
	}


	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}


	public String getLaminationTypeId() {
		return laminationTypeId;
	}


	public void setLaminationTypeId(String laminationTypeId) {
		this.laminationTypeId = laminationTypeId;
	}	
	
	
}
