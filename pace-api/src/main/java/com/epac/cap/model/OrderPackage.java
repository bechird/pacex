package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

//@Entity
//@Table(name = "OrderPackages")
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="com.epac.cap")
public class OrderPackage implements java.io.Serializable  {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//private Order order;
	

	private Package package_;
	
	
	private OrderPackageId id;
	
	public OrderPackage() {
		// TODO Auto-generated constructor stub
	}

	/*@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Order_Id", nullable = false, insertable = false, updatable = false)
	public Order getOrder() {
		return order;
	}*/


	/*public void setOrder(Order order) {
		this.order = order;
	}*/

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "packageId", nullable = false, insertable = false, updatable = false)
	public Package getPackage_() {
		return package_;
	}


	public void setPackage_(Package package_) {
		this.package_ = package_;
	}

	@EmbeddedId
	@AttributeOverrides({ @AttributeOverride(name = "order_Id", column = @Column(name = "Order_Id", nullable = false)),
			@AttributeOverride(name = "packageId", column = @Column(name = "packageId", nullable = false, length = 25)) })
	public OrderPackageId getId() {
		return id;
	}

	public void setId(OrderPackageId id) {
		this.id = id;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
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
		if (!(obj instanceof OrderPackage)) {
			return false;
		}
		OrderPackage other = (OrderPackage) obj;
		if (getId() == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (!getId().equals(other.getId())) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		    StringBuilder builder = new StringBuilder();
		    builder.append("package_ =");
		    builder.append(package_.getPackageId());
		   
		    if (id != null){
		    	 builder.append(", orderId=");
		    	 builder.append(id.getOrderId());
				 builder.append(", packageId=");
				 builder.append(id.getPackageId());
		    }
		    
		    return builder.toString();
		
	}
	
}
