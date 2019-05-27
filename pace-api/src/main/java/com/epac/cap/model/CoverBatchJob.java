package com.epac.cap.model;

import java.beans.Transient;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * CoverBatchJob class representing the CoverBatch /Jobs
 */
@Entity
@Table(name = "coverBatchJob")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "com.epac.cap")
public class CoverBatchJob implements java.io.Serializable {

	private static final long serialVersionUID = 65222160328267576L;

	private Integer id;
	private Job job;
	private Integer quantity;
	private Boolean ready = false;
	private Boolean outfed = false;
	private int outfedQuantity;
	private int inventoryQty;
	private boolean selected = false;
	private Integer indexJob;
	
	public enum DeliveryStatus{
        WAITING,
        AVAILABLE,
        REQUESTED,
        OUTFED,
        DELIVERED,
        MISSING,
        NOREAD
        
}
	private DeliveryStatus status ;

	/**
	 * Default constructor
	 */
	public CoverBatchJob() {
	}

	public CoverBatchJob(Integer id, Job job, Integer quantity) {
		this.id = id;
		this.job = job;
		this.quantity = quantity;
	}
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@OneToOne(fetch = FetchType.EAGER)
	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Boolean getReady() {
		return ready;
	}

	public void setReady(Boolean ready) {
		this.ready = ready;
	}

	public Boolean getOutfed() {
		return outfed;
	}

	public void setOutfed(Boolean outfed) {
		this.outfed = outfed;
	}

	public int getOutfedQuantity() {
		return outfedQuantity;
	}

	public void setOutfedQuantity(int outfedQuantity) {
		this.outfedQuantity = outfedQuantity;
	}

	public int getInventoryQty() {
		return inventoryQty;
	}

	public void setInventoryQty(int inventoryQty) {
		this.inventoryQty = inventoryQty;
	}
	@JsonIgnore
	@Transient
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Enumerated(EnumType.STRING)
	public DeliveryStatus getStatus() {
		return status;
	}

	public void setStatus(DeliveryStatus status) {
		this.status = status;
	}
@Transient
@JsonIgnore
	public Integer getIndexJob() {
		return indexJob;
	}

	public void setIndexJob(Integer index) {
		this.indexJob = index;
	}
	


}
