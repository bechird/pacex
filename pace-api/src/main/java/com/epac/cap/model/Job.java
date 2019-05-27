package com.epac.cap.model;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.epac.cap.common.AuditableBean;

/**
 * Job class representing a job in the system
 */
@Entity
@Table(name = "job")
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="com.epac.cap")
public class Job extends AuditableBean implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4670252047631389832L;
	private Integer jobId;
	private String jobName;
	
	private Integer orderId;
	
	//private Part part;
	private String partNum;
	
	//private OrderPart orderPart;
	
	private Date dueDate;
	private String partColor;
	private String partPaperId;
	private String productionMode;
	private String partTitle;
	private String partIsbn;
	private String partCategory;
	
	//transient
	private Lamination partLamination;
	private List<String> rasterNames;
	private Integer orderPartsCount;
	private String bindingTypeId;
	private String spineType;
	private String headTailBands;
	private String wireColor;
	private Integer partPagesCount;
	private String partPaperShortName;
	private String impPartHeight;
	private Order order;

	private String stationId;
	private String machineId;
	private JobStatus jobStatus;
	private Priority jobPriority;
	private Priority binderyPriority;
	private Integer rollId;
	private Integer productionOrdering;
	private Integer rollOrdering;
	private Integer machineOrdering;
	private Float hours;
	private JobType jobType;
	private Boolean fileSentFlag;
	private Integer quantityNeeded;
	private Integer quantityProduced;
	private Integer quantityUnaccountedfor;
	private Integer splitLevel;
	private Set<LoadTag> loadTags = new HashSet<LoadTag>(0);
	private float totalWaste;
	
	private JobPrevious prevJobData;
	
	private String bestSheetUsed;

	/**
	 * Default constructor
	 */
	public Job() {
	}

	/**
	 * Constructor which sets all of the properties
	 */
	public Job(Machine machine, Order order, Part part, Roll roll, Station station,
			Integer productionOrdering, Integer rollOrdering, Integer machineOrdering, float hours, JobType jobType,
			Boolean fileSentFlag, Priority jobPriority, Integer quantityProduced,
		    Set<LoadTag> loadTags) {
		//this.machine = machine;
		//this.order = order;
		//this.part = part;
		//this.roll = roll;
		//this.station = station;
		this.productionOrdering = productionOrdering;
		this.rollOrdering = rollOrdering;
		this.machineOrdering = machineOrdering;
		this.hours = hours;
		this.jobType = jobType;
		this.fileSentFlag = fileSentFlag;
		this.jobPriority = jobPriority;
		this.quantityProduced = quantityProduced;
		this.loadTags = loadTags;
	}

	/**
	 * Accessor methods for jobId
	 *
	 * @return jobId  
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "Job_Id", unique = true, nullable = false)
	public Integer getJobId() {
		return this.jobId;
	}

	/**
	 * @param jobId the jobId to set
	 */
	public void setJobId(Integer jobId) {
		this.jobId = jobId;
	}
	
	/**
	 * Accessor methods for machine
	 *
	 * @return machine  
	 

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Machine_Id", insertable = false, updatable = false )
	public Machine getMachine() {
		return this.machine;
	}*/

	/**
	 * @param machine the machine to set
	
	public void setMachine(Machine machine) {
		this.machine = machine;
	} */

	/**
	 * @return the jobName
	 */
	@Column(name = "Job_Name", length = 65)
	public String getJobName() {
		return jobName;
	}

	/**
	 * @param jobName the jobName to set
	 */
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	/**
	 * @return the machineId
	 */
	@Column(name = "Machine_Id", length = 25)
	public String getMachineId() {
		return machineId;
	}

	/**
	 * @param machineId the machineId to set
	 */
	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}

	/**
	 * Accessor methods for order
	 *
	 * @return order  
	 

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Order_Id")*/
	@Transient
	public Order getOrder() {
		return this.order;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(Order order) {
		this.order = order;
	}

	@Column(name = "Order_Id")
	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	/**
	 * Accessor methods for part
	 *
	 * @return part  
	 

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Part_Num")
	public Part getPart() {
		return this.part;
	}*/

	/**
	 * @param part the part to set
	 
	public void setPart(Part part) {
		this.part = part;
	}*/
	
	@Column(name = "Part_Num")
	public String getPartNum() {
		return partNum;
	}

	public void setPartNum(String partNum) {
		this.partNum = partNum;
	}
	
	/**
	 * @return the orderPart (the part that's related to the order) based on this job's part/order couple
	 
	@Transient
	public OrderPart getOrderPart() {
		if(this.getPart().getTopParts().isEmpty()){
			for(OrderPart op : getOrder().getOrderParts()){
				if(op.getPart().getPartNum().equals(this.getPart().getPartNum())){
					orderPart = op;
					break;
				}
			}
		}else{
			for(OrderPart op : getOrder().getOrderParts()){
				if(op.getPart().getPartNum().equals(this.getPart().getPartNum().substring(0, this.getPart().getPartNum().length() - 1))){
					orderPart = op;
					break;
				}
			}
		}
		return orderPart;
	}*/

	/**
	 * @param orderPart the orderPart to set
	 
	public void setOrderPart(OrderPart orderPart) {
		this.orderPart = orderPart;
	}*/

	/**
	 * Accessor methods for roll
	 *
	 * @return roll  
	 

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Roll_Id")*/
	@Column(name = "Roll_Id")
	public Integer getRollId() {
		return this.rollId;
	}

	/**
	 * @param roll the roll to set
	 */
	public void setRollId(Integer roll) {
		this.rollId = roll;
	}

	/**
	 * Accessor methods for station
	 *
	 * @return station  
	 

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Station_Id")
	public Station getStation() {
		return this.station;
	}*/

	/**
	 * @param station the station to set
	 
	public void setStation(Station station) {
		this.station = station;
	}*/

	/**
	 * Accessor methods for status
	 *
	 * @return status  
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Status")
	public JobStatus getJobStatus() {
		return this.jobStatus;
	}
	
	/**
	 * @param status the status to set
	 */
	public void setJobStatus(JobStatus status) {
		this.jobStatus = status;
	}

	/**
	 * @return the stationId
	 */
	@Column(name = "Station_Id", length = 25)
	public String getStationId() {
		return stationId;
	}

	/**
	 * @param stationId the stationId to set
	 */
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	/**
	 * Accessor methods for productionOrdering
	 *
	 * @return productionOrdering  
	 */

	@Column(name = "Production_Ordering")
	public Integer getProductionOrdering() {
		return this.productionOrdering;
	}

	/**
	 * @param productionOrdering the productionOrdering to set
	 */
	public void setProductionOrdering(Integer productionOrdering) {
		this.productionOrdering = productionOrdering;
	}

	/**
	 * Accessor methods for rollOrdering
	 *
	 * @return rollOrdering  
	 */

	@Column(name = "Roll_Ordering")
	public Integer getRollOrdering() {
		return this.rollOrdering;
	}

	/**
	 * @param rollOrdering the rollOrdering to set
	 */
	public void setRollOrdering(Integer rollOrdering) {
		this.rollOrdering = rollOrdering;
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

	/**
	 * Accessor methods for hours
	 *
	 * @return hours  
	 */

	@Column(name = "Hours", precision = 8, scale = 3)
	public Float getHours() {
		return this.hours;
	}

	/**
	 * @param hours the hours to set
	 */
	public void setHours(Float hours) {
		this.hours = hours;
	}

	/**
	 * Accessor methods for jobType
	 *
	 * @return jobType  
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Job_Type")
	public JobType getJobType() {
		return this.jobType;
	}

	/**
	 * @param jobType the jobType to set
	 */
	public void setJobType(JobType jobType) {
		this.jobType = jobType;
	}

	/**
	 * Accessor methods for fileSentFlag
	 *
	 * @return fileSentFlag  
	 */

	@Column(name = "File_Sent_Flag")
	public Boolean getFileSentFlag() {
		return this.fileSentFlag;
	}

	/**
	 * @param fileSentFlag the fileSentFlag to set
	 */
	public void setFileSentFlag(Boolean fileSentFlag) {
		this.fileSentFlag = fileSentFlag;
	}

	/**
	 * Accessor methods for jobPriority
	 *
	 * @return jobPriority  
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Job_Priority")
	public Priority getJobPriority() {
		return this.jobPriority;
	}

	/**
	 * @param jobPriority the jobPriority to set
	 */
	public void setJobPriority(Priority jobPriority) {
		this.jobPriority = jobPriority;
	}

	/**
	 * Accessor methods for binderyPriority
	 *
	 * @return binderyPriority  
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Bindery_Priority")
	public Priority getBinderyPriority() {
		return this.binderyPriority;
	}
	
	/**
	 * @param binderyPriority the binderyPriority to set
	 */
	public void setBinderyPriority(Priority binderyPriority) {
		this.binderyPriority = binderyPriority;
	}
	
	/**
	 * @return the quantityNeeded
	 */
	@Column(name = "Quantity_Needed")
	public Integer getQuantityNeeded() {
		return this.quantityNeeded;
	}

	/**
	 * @param quantityNeeded the quantityNeeded to set
	 */
	public void setQuantityNeeded(Integer quantityNeeded) {
		this.quantityNeeded = quantityNeeded;
	}

	/**
	 * Accessor methods for quantityProduced
	 *
	 * @return quantityProduced  
	 */
	@Column(name = "Quantity_Produced", precision = 18, scale = 3)
	public Integer getQuantityProduced() {
		if(this.quantityProduced == null){
			this.quantityProduced = 0;
		}
		return this.quantityProduced;
	}

	/**
	 * @param quantityProduced the quantityProduced to set
	 */
	public void setQuantityProduced(Integer quantityProduced) {
		this.quantityProduced = quantityProduced;
	}

	/**
	 * @return the quantityUnaccountedfor
	 */
	@Column(name = "Quantity_Unaccounted")
	public Integer getQuantityUnaccountedfor() {
		return quantityUnaccountedfor;
	}

	/**
	 * @param quantityUnaccountedfor the quantityUnaccountedfor to set
	 */
	public void setQuantityUnaccountedfor(Integer quantityUnaccountedfor) {
		this.quantityUnaccountedfor = quantityUnaccountedfor;
	}

	/**
	 * @return the splitLevel
	 */
	@Column(name = "Split_Level")
	public Integer getSplitLevel() {
		return splitLevel;
	}

	/**
	 * @param splitLevel the splitLevel to set
	 */
	public void setSplitLevel(Integer splitLevel) {
		this.splitLevel = splitLevel;
	}

	/**
	 * Accessor methods for loadTags
	 *
	 * @return loadTags  
	 */

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "jobId")
	@OrderBy("loadTagId desc")
	//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<LoadTag> getLoadTags() {
		return this.loadTags;
	}

	/**
	 * @param loadTags the loadTags to set
	 */
	public void setLoadTags(Set<LoadTag> loadTags) {
		this.loadTags = loadTags;
	}
	
	@Transient
	public float getTotalWaste(){
		float result = 0;
		for(LoadTag lt : this.getLoadTags()){
			result += lt.getWaste();
		}
		this.totalWaste = result;
		return this.totalWaste;
	}
	
	public void setTotalWaste(float waste) {
		this.totalWaste = waste;
	}
	
	/**
	 * Accessor methods for dueDate
	 *
	 * @return dueDate  
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "Due_Date", length = 19)
	public Date getDueDate() {
		return this.dueDate;
	}

	/**
	 * @param dueDate the dueDate to set
	 */
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	
	@Column(name = "Part_Title", length = 255)
	public String getPartTitle() {
		return partTitle;
	}

	public void setPartTitle(String partTitle) {
		this.partTitle = partTitle;
	}

	@Column(name = "Part_Isbn", length = 25)
	public String getPartIsbn() {
		return partIsbn;
	}

	public void setPartIsbn(String partIsbn) {
		this.partIsbn = partIsbn;
	}

	@Column(name = "Part_Category", length = 25)
	public String getPartCategory() {
		return partCategory;
	}

	public void setPartCategory(String partCategory) {
		this.partCategory = partCategory;
	}

	@Column(name = "Part_Color", length = 15)
	public String getPartColor() {
		return partColor;
	}

	public void setPartColor(String partColor) {
		this.partColor = partColor;
	}

	@Column(name = "Part_Paper_Id", length = 60)
	public String getPartPaperId() {
		return partPaperId;
	}

	public void setPartPaperId(String partPaperId) {
		this.partPaperId = partPaperId;
	}

	@Column(name = "Production_Mode", length = 45)
	public String getProductionMode() {
		return productionMode;
	}

	public void setProductionMode(String productionMode) {
		this.productionMode = productionMode;
	}

	@Transient
	public Lamination getPartLamination() {
		return partLamination;
	}

	public void setPartLamination(Lamination partLamination) {
		this.partLamination = partLamination;
	}

	@Transient
	public List<String> getRasterNames() {
		return rasterNames;
	}

	public void setRasterNames(List<String> rasterNames) {
		this.rasterNames = rasterNames;
	}

	@Transient
	public Integer getOrderPartsCount() {
		return orderPartsCount;
	}

	public void setOrderPartsCount(Integer orderPartsCount) {
		this.orderPartsCount = orderPartsCount;
	}

	@Transient
	public String getBindingTypeId() {
		return bindingTypeId;
	}

	public void setBindingTypeId(String bindingTypeId) {
		this.bindingTypeId = bindingTypeId;
	}

	@Transient
	public String getSpineType() {
		return spineType;
	}

	public void setSpineType(String spineType) {
		this.spineType = spineType;
	}

	@Transient
	public String getHeadTailBands() {
		return headTailBands;
	}

	public void setHeadTailBands(String headTailBands) {
		this.headTailBands = headTailBands;
	}

	@Transient
	public String getWireColor() {
		return wireColor;
	}

	public void setWireColor(String wireColor) {
		this.wireColor = wireColor;
	}

	@Transient
	public Integer getPartPagesCount() {
		return partPagesCount;
	}

	public void setPartPagesCount(Integer partPagesCount) {
		this.partPagesCount = partPagesCount;
	}

	@Transient
	public String getPartPaperShortName() {
		return partPaperShortName;
	}

	public void setPartPaperShortName(String partPaperShortName) {
		this.partPaperShortName = partPaperShortName;
	}

	/**
	 * @return the prevJobData
	 */
	@Transient
	public JobPrevious getPrevJobData() {
		return prevJobData;
	}

	/**
	 * @param prevJobData the prevJobData to set
	 */
	public void setPrevJobData(JobPrevious prevJobData) {
		this.prevJobData = prevJobData;
	}

	/**
	 * @return the impPartHeight: a concatenation of imposition and part height, needed on the scheduling page
	 */
	@Transient
	public String getImpPartHeight() {
		return impPartHeight;
	}

	/**
	 * @param impPartHeight the impPartHeight to set
	 */
	public void setImpPartHeight(String impPartHeight) {
		this.impPartHeight = impPartHeight;
	}

	/**
	 * @return the bestSheetUsed
	 */
	@Column(name = "Best_Sheet_Used", length = 15)
	public String getBestSheetUsed() {
		return bestSheetUsed;
	}

	/**
	 * @param bestSheetUsed the bestSheetUsed to set
	 */
	public void setBestSheetUsed(String bestSheetUsed) {
		this.bestSheetUsed = bestSheetUsed;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getJobId() == null) ? 0 : getJobId().hashCode());
		//result = prime * result + ((order == null) ? 0 : order.hashCode());
		//result = prime * result + ((part == null) ? 0 : part.hashCode());
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
		if (!(obj instanceof Job)) {
			return false;
		}
		Job other = (Job) obj;
		if (getJobId() == null) {
			if (other.getJobId() != null) {
				return false;
			}
		} else if (!getJobId().equals(other.getJobId())) {
			return false;
		}
		/*if (order == null) {
			if (other.order != null) {
				return false;
			}
		} else if (!order.equals(other.order)) {
			return false;
		}
		if (part == null) {
			if (other.part != null) {
				return false;
			}
		} else if (!part.equals(other.part)) {
			return false;
		}*/
		return true;
	}

}
