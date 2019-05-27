package com.epac.cap.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * OrderPart class representing the order lines/parts of an order
 */
@Entity
@Table(name = "order_Part")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="com.epac.cap")
public class OrderPart implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 809590566319721502L;
	
	private Long id;
	private Part part;
	private Integer quantity;
	private Integer quantityMax;
	private Integer quantityMin;
	private Float printingHours;

	/**
	 * Default constructor
	 */
	public OrderPart() {
	}

	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return this.id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}


	/**
	 * Accessor methods for part
	 *
	 * @return part  
	 */

	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
	@JoinColumn(name = "Part_Num")
	public Part getPart() {
		return this.part;
	}

	/**
	 * @param part the part to set
	 */
	public void setPart(Part part) {
		this.part = part;
	}

	/**
	 * Accessor methods for quantity
	 *
	 * @return quantity  
	 */
	@Column(name = "Quantity")
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
	 * @return the quantityMax
	 */
	@Column(name = "Quantity_Max")
	public Integer getQuantityMax() {
		return quantityMax;
	}

	/**
	 * @param quantityMax the quantityMax to set
	 */
	public void setQuantityMax(Integer quantityMax) {
		this.quantityMax = quantityMax;
	}

	/**
	 * @return the quantityMin
	 */
	@Column(name = "Quantity_Min")
	public Integer getQuantityMin() {
		return quantityMin;
	}

	/**
	 * @param quantityMin the quantityMin to set
	 */
	public void setQuantityMin(Integer quantityMin) {
		this.quantityMin = quantityMin;
	}

	/**
	 * Accessor methods for printingHours
	 *
	 * @return printingHours  
	 */

	@Column(name = "Printing_Hours")
	public Float getPrintingHours() {
		return this.printingHours;
	}

	/**
	 * @param printingHours the printingHours to set
	 */
	public void setPrintingHours(Float printingHours) {
		this.printingHours = printingHours;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
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
		if (!(obj instanceof OrderPart)) {
			return false;
		}
		OrderPart other = (OrderPart) obj;
		if (getId() == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (!getId().equals(other.getId())) {
			return false;
		}
		return true;
	}

}
