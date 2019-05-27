package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * OrderStatus class representing an order status
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "order_Status")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Order_Status_Id", unique = true, nullable = false, length = 25), name = "id"),
    @AttributeOverride(column = @Column(name = "Name", nullable = false, length = 55), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 100), name = "description")})
public class OrderStatus extends LookupItem {
	//@JsonIgnore
	//private Set<Order> orders = new HashSet<Order>(0);
	
	/**
	 * 
	 */
	public OrderStatus() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 */
	public OrderStatus(String id, String name) {
		super(id, name);
	}
	
	public OrderStatus(LookupItem item2) {
		super(item2);
	}

	/**
	 * @return the orders
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "status")
	public Set<Order> getOrders() {
		return orders;
	}*/

	/**
	 * @param orders the orders to set
	 
	public void setOrders(Set<Order> orders) {
		this.orders = orders;
	}*/
	
}
