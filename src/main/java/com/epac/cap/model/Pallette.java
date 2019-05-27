package com.epac.cap.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.epac.cap.common.AuditableBean;

@Entity
@Table(name = "Pallette")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="com.epac.cap")
public class Pallette extends AuditableBean implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public enum PalletteStatus{ACTIVE,PAUSED,COMPLETE,DELIVERED};
	public enum PalletteType{MIXTE,SINGLE};


	private Long id;
	private Customer customer;
	private PalletteStatus statusPallette;
	private PalletteType typePallette;
	private Date startDate;
	private Date endDate;
	private String machineId;
	private String palletteName;
	private String destination;
	private List<PalletteBook> books = new ArrayList<PalletteBook>(0); 
	private Integer count;
	private String palletteSlip;
	private Date delivredDate;
	private Integer blNumber;
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "id", unique = true, nullable = false)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	public PalletteStatus getStatusPallette() {
		return statusPallette;
	}
	public void setStatusPallette(PalletteStatus statusPallette) {
		this.statusPallette = statusPallette;
	}
	@Column(name = "type")
	@Enumerated(EnumType.STRING)
	public PalletteType getTypePallette() {
		return typePallette;
	}
	public void setTypePallette(PalletteType typePallette) {
		this.typePallette = typePallette;
	}

	@Column(name = "startDate")
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Column(name = "endDate")
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "id.palletteId",fetch = FetchType.EAGER)
	public List<PalletteBook> getBooks() {
		return books;
	}
	public void setBooks(List<PalletteBook> books) {
		this.books = books;
	}
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Customer_Id")
	public Customer getCustomer() {
		return this.customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	@Column(name = "Machine_Id", length = 25)
	public String getMachineId() {
		return machineId;
	}
	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}

	@Column(name = "palletteName", length = 55)
	public String getPalletteName() {
		return palletteName;
	}
	public void setPalletteName(String palletteName) {
		this.palletteName = palletteName;
	}
	@Column(name = "destination", length = 200)
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	@Transient
	public String getPalletteSlip() {
		Calendar date = new GregorianCalendar(); 
		date.setTime(new Date());
		//int year = date.get(Calendar.YEAR);
		String palletteSlip = ""+count;
		return palletteSlip;
	}
	@Column(name = "count")
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public void setPalletteSlip(String palletteSlip) {
		this.palletteSlip = palletteSlip;
	}
	public Date getDelivredDate() {
		return delivredDate;
	}
	public void setDelivredDate(Date delivredDate) {
		this.delivredDate = delivredDate;
	}
	public void setBlNumber(Integer numBL) {

		this.blNumber = numBL;
	}
	public Integer getBlNumber() {

		return this.blNumber;
	}

}
