package com.epac.cap.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.epac.cap.common.AuditableSearchBean;
import com.epac.cap.model.Customer;
import com.epac.cap.model.Pallette.PalletteStatus;
import com.epac.cap.model.Pallette.PalletteType;
import com.epac.cap.model.PalletteBook;

public class PalletteSearchBean extends AuditableSearchBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private Customer customer;
	private PalletteStatus statusPallette;
	private PalletteType typePallette;
	private Date startDate;
	private String palletteName;
	private Date endDate;
	private String machineId;
	private List<PalletteBook> books = new ArrayList<PalletteBook>(0);
	private Integer blNumber;
	private String destination;
	private Date delivredDate;
	private Integer resultOffset;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Customer getCustomer() {
		return customer;
	}
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	public PalletteStatus getStatusPallette() {
		return statusPallette;
	}
	public void setStatusPallette(PalletteStatus statusPallette) {
		this.statusPallette = statusPallette;
	}
	public PalletteType getTypePallette() {
		return typePallette;
	}
	public void setTypePallette(PalletteType typePallette) {
		this.typePallette = typePallette;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public String getMachineId() {
		return machineId;
	}
	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}
	public List<PalletteBook> getBooks() {
		return books;
	}
	public void setBooks(List<PalletteBook> books) {
		this.books = books;
	}
	public String getPalletteName() {
		return palletteName;
	}
	public void setPalletteName(String palletteName) {
		this.palletteName = palletteName;
	}
	public Integer getResultOffset() {
		return resultOffset;
	}
	public void setResultOffset(Integer resultOffset) {
		this.resultOffset = resultOffset;
	}
	public Integer getBlNumber() {
		return blNumber;
	}
	public void setBlNumber(Integer blNumber) {
		this.blNumber = blNumber;
	}
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public Date getDelivredDate() {
		return delivredDate;
	}
	public void setDelivredDate(Date delivredDate) {
		this.delivredDate = delivredDate;
	} 
	
	

}
