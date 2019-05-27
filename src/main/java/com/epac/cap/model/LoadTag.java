package com.epac.cap.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.epac.cap.common.AuditableBean;

/**
 * LoadTag class representing a load tag
 */
@Entity
@Table(name = "load_Tag")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="com.epac.cap")
public class LoadTag extends AuditableBean implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3324367712543881843L;
	private Integer loadTagId;
	//@JsonIgnore
	private Integer jobId;
	private String machineId;
	private String tagNum;
	private Integer quantity;
	private Date startTime;
	private Date finishTime;
	private float waste;
	private String cartNum;
	private Boolean usedFlag;
	
	/**
	 * Default constructor
	 */
	public LoadTag() {
	}

	/**
	 * Constructor which sets all of the properties
	 */
	public LoadTag(Job job, String tagNum, Integer quantity, Date startTime, Date finishTime, float waste,
			String cartNum, boolean usedFlag) {
		//this.job = job;
		this.tagNum = tagNum;
		this.quantity = quantity;
		this.startTime = startTime;
		this.finishTime = finishTime;
		this.waste = waste;
		this.cartNum = cartNum;
		this.usedFlag = usedFlag;
	}

	/**
	 * Accessor methods for loadTagId
	 *
	 * @return loadTagId  
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "Load_Tag_Id", unique = true, nullable = false)
	public Integer getLoadTagId() {
		return this.loadTagId;
	}

	/**
	 * @param loadTagId the loadTagId to set
	 */
	public void setLoadTagId(Integer loadTagId) {
		this.loadTagId = loadTagId;
	}

	/**
	 * Accessor methods for job
	 *
	 * @return job  
	 

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Job_Id")*/
	@Column(name = "Job_Id")
	public Integer getJobId() {
		return this.jobId;
	}

	/**
	 * @param job the job to set
	 */
	public void setJobId(Integer job) {
		this.jobId = job;
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
	 * Accessor methods for tagNum
	 *
	 * @return tagNum  
	 */

	@Column(name = "Tag_Num", length = 10)
	public String getTagNum() {
		return this.tagNum;
	}

	/**
	 * @param tagNum the tagNum to set
	 */
	public void setTagNum(String tagNum) {
		this.tagNum = tagNum;
	}

	/**
	 * Accessor methods for quantity
	 *
	 * @return quantity  
	 */

	@Column(name = "Quantity", precision = 15, scale = 3)
	public Integer getQuantity() {
		return this.quantity;
	}

	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	/**
	 * Accessor methods for startTime
	 *
	 * @return startTime  
	 */

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "Start_Time", length = 19)
	public Date getStartTime() {
		return this.startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * Accessor methods for finishTime
	 *
	 * @return finishTime  
	 */

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "Finish_Time", length = 19)
	public Date getFinishTime() {
		return this.finishTime;
	}

	/**
	 * @param finishTime the finishTime to set
	 */
	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}

	/**
	 * Accessor methods for waste
	 *
	 * @return waste  
	 */

	@Column(name = "Waste", precision = 10, scale = 3)
	public float getWaste() {
		return this.waste;
	}

	/**
	 * @param waste the waste to set
	 */
	public void setWaste(float waste) {
		this.waste = waste;
	}

	/**
	 * Accessor methods for cartNum
	 *
	 * @return cartNum  
	 */

	@Column(name = "Cart_Num", length = 15)
	public String getCartNum() {
		return this.cartNum;
	}

	/**
	 * @param cartNum the cartNum to set
	 */
	public void setCartNum(String cartNum) {
		this.cartNum = cartNum;
	}

	/**
	 * Accessor methods for usedFlag
	 *
	 * @return usedFlag  
	 */

	@Column(name = "Used_Flag")
	public Boolean getUsedFlag() {
		return this.usedFlag;
	}

	/**
	 * @param usedFlag the usedFlag to set
	 */
	public void setUsedFlag(Boolean usedFlag) {
		this.usedFlag = usedFlag;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getLoadTagId() == null) ? 0 : getLoadTagId().hashCode());
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
		if (!(obj instanceof LoadTag)) {
			return false;
		}
		LoadTag other = (LoadTag) obj;
		if (getLoadTagId() == null) {
			if (other.getLoadTagId() != null) {
				return false;
			}
		} else if (!getLoadTagId().equals(other.getLoadTagId())) {
			return false;
		}
		return true;
	}
	
}
