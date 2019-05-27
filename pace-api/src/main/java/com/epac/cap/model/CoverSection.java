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
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.SortComparator;

import com.epac.cap.common.AuditableBean;
import com.epac.cap.common.BatchSortedSetDeserializer;
import com.epac.cap.handler.JobsByBatchOrderingComparator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * CoverSection a class representing a section of the batch of covers
 */
@Entity
@Table(name = "coverSection")
public class CoverSection extends AuditableBean implements java.io.Serializable {

	private static final long serialVersionUID = -6444464579824611089L;

	private Integer coverSectionId;
	private String coverSectionName;
	private String machineId;
	private SectionStatus status;
	private Integer quantity;
	private Integer batchId;
	private Lamination laminationType;
	// To be used later
	private PaperType paperType;
	private Integer machineOrdering;
	private Date dueDate;
	private String priority;
	private String path;
	private String copyStatus;
    private String binderId;
    private int sectionReadyQuantity;
	@JsonDeserialize(using = BatchSortedSetDeserializer.class)
	private SortedSet<CoverBatchJob> jobs = new TreeSet<CoverBatchJob>(new JobsByBatchOrderingComparator());

	public CoverSection() {
		super();
		// TODO Auto-generated constructor stub
	}

	

	public CoverSection(Integer coverSectionId, String coverSectionName, String machineId, SectionStatus status,
			Integer quantity, Lamination laminationType, PaperType paperType,
			Integer machineOrdering, Date dueDate, String priority, String path, SortedSet<CoverBatchJob> jobs,Integer sectionReadyQuantity) {
		super();
		this.coverSectionId = coverSectionId;
		this.coverSectionName = coverSectionName;
		this.machineId = machineId;
		this.status = status;
		this.quantity = quantity;
		//this.batchId = coverBatchId;
		this.laminationType = laminationType;
		this.paperType = paperType;
		this.machineOrdering = machineOrdering;
		this.dueDate = dueDate;
		this.priority = priority;
		this.path = path;
		this.jobs = jobs;
		this.sectionReadyQuantity =  sectionReadyQuantity;
	}

	public CoverSection(String section_Name, Integer quantity2, Lamination lamination, String priority2) {
		
		this.coverSectionName = section_Name;
		this.quantity = quantity2;
		this.laminationType = lamination;
		this.priority = priority2;
	}

	public enum copyStatuses {
		NOT_STARTED("NOT_STARTED"),
		IN_PROGRESS("IN_PROGRESS"),
		FINISHED("FINISHED"),
		ERROR("ERROR")
		;

		private String name;
		private copyStatuses(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}

	/**
	 * Accessor methods for coverSectionId
	 *
	 * @return sectionId  
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "Section_Id", unique = true, nullable = false)
	public Integer getCoverSectionId() {
		return coverSectionId;
	}


	/**
	 * @param coverSectionId the Cover SectionId to set
	 */
	public void setCoverSectionId(Integer coverSectionId) {
		this.coverSectionId = coverSectionId;
	}


	/**
	 * Accessor methods for cover sectionName
	 *
	 * @return sectionName  
	 */
	@Column(name = "Section_Name", length = 20)
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
	public Integer getBatchId() {
		return batchId;
	}
	
	/**
	 * @param batchId the Cover batchId to set
	 */
	public void setBatchId(Integer coverBatchId) {
		this.batchId = coverBatchId;
	}
	
	/**
	 * Accessor methods for machine
	 *
	 * @return machine  
	 */
	@Column(name = "Machine_Id", length = 25)
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
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Status")
	public SectionStatus getStatus() {
		return status;
	}
	
	/**
	 * @param status the status to set
	 */
	public void setStatus(SectionStatus status) {
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
	 * Accessor methods for lamination
	 *
	 * @return lamination  
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Lamination")
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
	@Transient
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Paper_Type")
	public PaperType getPaperType() {
		return paperType;
	}

	/**
	 * @param paperType the paperType to set
	 */
	public void setPaperType(PaperType paperType) {
		this.paperType = paperType;
	}
	
	/**
	 * retrieves the most urgent priority of all jobs assigned to the coverBatch
	 */
	//@Transient
	public String getPriority(){
		if(getJobs() != null && !getJobs().isEmpty()){
			CoverBatchJob job = this.getJobs().iterator().next();
			if(job != null && job.getJob() != null){
				this.priority = job.getJob().getJobPriority().getId();
			}
		}
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
		if(getJobs() != null && !getJobs().isEmpty()){
			CoverBatchJob job = this.getJobs().iterator().next();
			if(job != null && job.getJob() != null){
				this.dueDate = job.getJob().getDueDate();
			}
		}
		return this.dueDate;
	}
	
	public void setDueDate(Date dueDate){
		this.dueDate = dueDate;
	}

	/**
	 * Accessor methods for jobs
	 * @return jobs
	 */
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@SortComparator(JobsByBatchOrderingComparator.class)
	public SortedSet<CoverBatchJob> getJobs() {
		return jobs;
	}
	
	/**
	 * @param jobs the jobs to set
	 */
	public void setJobs(SortedSet<CoverBatchJob> jobs) {
		this.jobs = jobs;
	}

	/**
	 * Accessor methods for machineOrdering
	 *
	 * @return machineOrdering  
	 */

	@Column(name = "Machine_Ordering")
	public Integer getMachineOrdering() {
		return this.machineOrdering;
	}

	/**
	 * @param machineOrdering the machineOrdering to set
	 */
	public void setMachineOrdering(Integer machineOrdering) {
		this.machineOrdering = machineOrdering;
	}	
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getCopyStatus() {
		return copyStatus;
	}

	public void setCopyStatus(String copyStatus) {
		this.copyStatus = copyStatus;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((coverSectionId == null) ? 0 : coverSectionId.hashCode());
		result = prime * result + ((coverSectionName == null) ? 0 : coverSectionName.hashCode());
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
		CoverSection other = (CoverSection) obj;
		if (coverSectionId == null) {
			if (other.coverSectionId != null)
				return false;
		} else if (!coverSectionId.equals(other.coverSectionId))
			return false;
		if (coverSectionName == null) {
			if (other.coverSectionName != null)
				return false;
		} else if (!coverSectionName.equals(other.coverSectionName))
			return false;
		return true;
	}



	public String getBinderId() {
		return binderId;
	}



	public void setBinderId(String binderId) {
		this.binderId = binderId;
	}



	public int getSectionReadyQuantity() {
		return sectionReadyQuantity;
	}



	public void setSectionReadyQuantity(int sectionReadyQuantity) {
		this.sectionReadyQuantity = sectionReadyQuantity;
	}




	
}
