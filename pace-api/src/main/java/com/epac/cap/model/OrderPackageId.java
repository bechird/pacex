package com.epac.cap.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class OrderPackageId implements java.io.Serializable {

	
	private static final long serialVersionUID = 1641233114861969581L;
	private Integer orderId;
	private Long packageId;
	
	
	public OrderPackageId() {
		// TODO Auto-generated constructor stub
	}


	@Column(name = "order_Id")
	public Integer getOrderId() {
		return orderId;
	}



	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}


	@Column(name = "packageId")
	public Long getPackageId() {
		return packageId;
	}



	public void setPackageId(long packageId) {
		this.packageId = packageId;
	}
	@Override
	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof OrderPackageId))
			return false;
		OrderPackageId castOther = (OrderPackageId) other;

		return (this.getOrderId() == castOther.getOrderId())
				&& ((this.getPackageId() == castOther.getPackageId()) || (this.getPackageId()!= null
						&& castOther.getPackageId() != null && this.getPackageId().equals(castOther.getPackageId())));
	}

	@Override
	public int hashCode() {
		int result = 17;
		if (this.getOrderId() != null)
		result = 37 * result + this.getOrderId();
		result = 37 * result + (getPackageId() == null ? 0 : this.getPackageId().hashCode());
		return result;
	}

	
}
