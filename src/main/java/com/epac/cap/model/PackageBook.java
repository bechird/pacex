package com.epac.cap.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@JsonInclude(value = Include.NON_NULL)
@Table(name = "PackageBook" )
public class PackageBook implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum statusPcb{
		COMPLETE,PROGRESS
	}
	public enum typePcb{
		UNDER,OVER,MATCH
	}
	private Long packagePartId;
	
	private String bookId;
	private String barcode;
	private Double weight;
	
	private Integer quantity; 
	private Integer delivered;
	// turn these primitive integers into objects so when they are null they do not appear in the xml
	// instead of having 0 as value
	private statusPcb status;
	private Integer widthQty;
	private Integer depthQty;
	private Integer heightQty;
	private typePcb type;
	
	public PackageBook(){
	}
	
	public PackageBook(String bookId, int quantity) {
		this.bookId = bookId; 
		this.quantity = quantity; 
	}
	
	public PackageBook(String bookId, int quantity, double weight) {
		this.bookId = bookId; 
		this.quantity = quantity; 
		this.weight   = weight;
	}
	
	public PackageBook(String bookId, int quantity, String barcode, double weight) {
		this.bookId = bookId; 
		this.quantity = quantity; 
		this.barcode = barcode;
		this.weight = weight;
	}

	@Column( name = "quantity" )
	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	@Column( name = "delivered" )
	public Integer getDelivered() {
		return delivered;
	}
	
	public void setDelivered(Integer delivered) {
		this.delivered = delivered;
	}

	@Column( name = "bookId" , length = 75)
	public String getBookId() {
		return bookId;
	}

	public void setBookId(String bookId) {
		this.bookId = bookId;
	}
	
	@Column( name = "barcode" , length = 75)
	public String getBarcode() {
		return barcode;
	}
	
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	
	@Column( name = "weight" )
	public Double getWeight() {
		return weight;
	}
	
	public void setWeight(Double weight) {
		this.weight = weight;
	}
	
	@Column(name="widthQty")
	public Integer getWidthQty() {
		return widthQty;
	}

	public void setWidthQty(Integer widthQty) {
		this.widthQty = widthQty;
	}

	@Column(name="depthQty")
	public Integer getDepthQty() {
		return depthQty;
	}

	public void setDepthQty(Integer depthQty) {
		this.depthQty = depthQty;
	}

	@Column(name="heightQty")
	public Integer getHeightQty() {
		return heightQty;
	}

	public void setHeightQty(Integer heightQty) {
		this.heightQty = heightQty;
	}

	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof PackageBook))
			return false;
		
		PackageBook that = (PackageBook) obj;
		if(that.bookId != null && that.bookId.equals(this.bookId))
			return true;
		if(that.barcode != null && that.barcode.equals(this.barcode))
			return true;
		
		return super.equals(obj);
	}

	@Id
	@GeneratedValue( strategy = GenerationType.AUTO)
	@Column(name = "packagePartId")
	public Long getPackagePartId() {
		return packagePartId;
	}

	public void setPackagePartId(long id) {
		this.packagePartId = id;
	}
	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	public statusPcb getStatus() {
		return status;
	}

	public void setStatus(statusPcb status) {
		this.status = status;
	}
	
	@Column(name = "type")
	@Enumerated(EnumType.STRING)
	public typePcb getType() {
		return type;
	}

	public void setType(typePcb type) {
		this.type = type;
	}
	
}
