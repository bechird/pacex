package com.epac.cap.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * OrderPartId generated by hbm2java
 */
@Embeddable
public class OrderPartId implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1641233114861969581L;
	private Integer orderId;
	private String partNum;

	/**
	 * Default constructor
	 */
	public OrderPartId() {
	}

	/**
	 * Constructor which sets all of the properties
	 */
	public OrderPartId(Integer orderId, String partNum) {
		this.orderId = orderId;
		this.partNum = partNum;
	}

	/**
	 * Accessor methods for orderId
	 *
	 * @return orderId  
	 */

	@Column(name = "Order_Id", nullable = false)
	public Integer getOrderId() {
		return this.orderId;
	}

	/**
	 * @param orderId the orderId to set
	 */
	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	/**
	 * Accessor methods for partNum
	 *
	 * @return partNum  
	 */

	@Column(name = "Part_Num", nullable = false, length = 25)
	public String getPartNum() {
		return this.partNum;
	}

	/**
	 * @param partNum the partNum to set
	 */
	public void setPartNum(String partNum) {
		this.partNum = partNum;
	}

	@Override
	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof OrderPartId))
			return false;
		OrderPartId castOther = (OrderPartId) other;

		return (this.getOrderId() == castOther.getOrderId())
				&& ((this.getPartNum() == castOther.getPartNum()) || (this.getPartNum() != null
						&& castOther.getPartNum() != null && this.getPartNum().equals(castOther.getPartNum())));
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getOrderId();
		result = 37 * result + (getPartNum() == null ? 0 : this.getPartNum().hashCode());
		return result;
	}

}