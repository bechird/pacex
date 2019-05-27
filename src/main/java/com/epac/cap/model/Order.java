package com.epac.cap.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.epac.cap.common.AuditableBean;
import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * Order class that represents an order for production
 */
@Entity
@Table(name = "order_T")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="com.epac.cap")
public class Order extends AuditableBean implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3719005116290899834L;
	private Integer orderId;
	private String orderNum;
	private String status;
	private Date dueDate;
	private Date recievedDate;
	private String priority;
	private String notes;
	private String source;
	private Customer customer;
	private String productionMode;
	private Date completeDate;
	private String allIsbns;
	private Integer quantity;
	private Integer quantityMax;
	private Integer quantityMin;

	//@JsonIgnore
	private Set<OrderPart> orderParts = new HashSet<OrderPart>(0);
	private Set<String> parts = new HashSet<String>(0);
	private Set<Package> orderPackages = new HashSet<Package>(0);

	private OrderPart orderPart;
	private Set<OrderBl> order_Bl;
	
	private String clientId;
	private PNLData pnlData;
	
	private Boolean spotVarnish;
	
	/**
	 * Default constructor
	 */
	public Order() {
	}

	/**
	 * Constructor which sets the identity property
	 */
	public Order(String orderNum) {
		this.orderNum = orderNum;
	}

	/**
	 * Constructor which sets all of the properties
	 */
	public Order(String orderNum, Date dueDate, String notes, String source,
			Customer customer, Set<Job> jobs, Set<OrderPart> orderParts) {
		this.orderNum = orderNum;
		this.dueDate = dueDate;
		this.notes = notes;
		this.source = source;
		this.customer = customer;
		//this.jobs = jobs;
		this.orderParts = orderParts;
	}

	/**
	 * Accessor methods for orderId
	 *
	 * @return orderId  
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "Order_Id", unique = true, nullable = false)
	public Integer getOrderId() {
		return this.orderId;
	}

	/**
	 * @param orderId the orderId to set
	 */
	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public enum OrderStatus { 
		PENDING("PENDING"),
		ACCEPTED("ACCEPTED"),
		ONPROD("ONPROD"),
		COMPLETE("COMPLETE"),
		PACKAGING("PACKAGING"),
		SHIPPING("SHIPPING"),
		SHIPPED("SHIPPED"),
		
		ERROR("ERROR"),
		CANCELLED("CANCELLED"),
		REJECTED("REJECTED"),
		TOEPAC("TOEPAC"),
        DELIVERED("DELIVERED"),
		ONHOLD("ONHOLD");
		private String name;
		private OrderStatus(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}
	
	public enum OrderPriorities { 
		NORMAL("NORMAL"),
		HIGH("HIGH");

		private String name;
		private OrderPriorities(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}
	
	public enum OrderSources { 
		MANUAL("MANUAL"),
		ESPRINT("ESPRINT"),
		PACE("PACE"),
		PACER("PACER"),
		AUTO("AUTO");

		private String name;
		private OrderSources(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}
	
	public enum ProductionModes { 
		STANLY("STANLY"),
		STANLY_ONLY("STANLY_ONLY"),
		PALETT_ONLY("PALETT_ONLY");

		private String name;
		private ProductionModes(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}
	
	/**
	 * Accessor methods for orderNum
	 *
	 * @return orderNum  
	 */

	@Column(name = "Order_Num", nullable = false, length = 25)
	public String getOrderNum() {
		return this.orderNum;
	}

	/**
	 * @param orderNum the orderNum to set
	 */
	public void setOrderNum(String orderNum) {
		this.orderNum = orderNum;
	}

	/**
	 * Accessor methods for status
	 *
	 * @return status  
	 */
	//@ManyToOne(fetch = FetchType.EAGER)
	//@JoinColumn(name = "Status")
	@Column(name = "Status", length = 55)
	public String getStatus() {
		return this.status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Accessor methods for dueDate
	 *
	 * @return dueDate  
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "Due_Date", length = 19)
	public Date getDueDate() {
		return this.dueDate;
	}

	/**
	 * @param dueDate the dueDate to set
	 */
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	/**
	 * @return the recievedDate
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "Recieved_Date", length = 19)
	public Date getRecievedDate() {
		return recievedDate;
	}

	/**
	 * @param recievedDate the recievedDate to set
	 */
	public void setRecievedDate(Date recievedDate) {
		this.recievedDate = recievedDate;
	}

	/**
	 * Accessor methods for priority
	 * @return priority
	 */
	//@ManyToOne(fetch = FetchType.EAGER)
	//@JoinColumn(name = "Priority_Level")
	@Column(name = "Priority_Level", length = 55)
	public String getPriority() {
		return this.priority;
	}

	/**
	 * @param priority the priority to set
	*/
	public void setPriority(String priority) {
		this.priority = priority;
	} 
	
	/**
	 * Accessor methods for notes
	 *
	 * @return notes  
	 */

	@Column(name = "Notes", length = 2000)
	public String getNotes() {
		return this.notes;
	}

	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * Accessor methods for source
	 *
	 * @return source  
	 */

	@Column(name = "Source", length = 15)
	public String getSource() {
		return this.source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * Accessor methods for customer
	 *
	 * @return customer
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Customer_Id")
	public Customer getCustomer() {
		return this.customer;
	}

	/**
	 * @param customer the customer to set
	 */
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	/**
	 * @return the productionMode
	 */
	@Column(name = "Production_Mode", length = 45)
	public String getProductionMode() {
		return productionMode;
	}

	/**
	 * @param productionMode the productionMode to set
	 */
	public void setProductionMode(String productionMode) {
		this.productionMode = productionMode;
	}

	/**
	 * Accessor methods for jobs
	 *
	 * @return jobs  
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "order")
	public Set<Job> getJobs() {
		return this.jobs;
	}*/

	/**
	 * @param jobs the jobs to set
	 
	public void setJobs(Set<Job> jobs) {
		this.jobs = jobs;
	}*/
	
	/**
	 * checks to see if the order has active jobs
	 
	@Transient
	public boolean hasActiveJobs(){
		boolean result = false;
		for(Job j : this.getJobs()){
			if (!Job.JobsStatus.CANCELLED.getName().equals(j.getJobStatus().getId()) && 
					!Job.JobsStatus.COMPLETE.getName().equals(j.getJobStatus().getId())){
				result = true;
				break;
			}
		}
		return result;
	}*/
	
	/**
	 * Accessor methods for orderParts
	 *
	 * @return orderParts  
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name="OrderParts", joinColumns = @JoinColumn( name="orderId"),
            inverseJoinColumns = @JoinColumn( name="id"))
	public Set<OrderPart> getOrderParts() {
		return this.orderParts;
	}

	@OneToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
	@JoinTable(name="OrderPackages", joinColumns = @JoinColumn( name="orderId"),
            inverseJoinColumns = @JoinColumn( name="packageId"))
	public Set<Package> getOrderPackages() {
		return orderPackages;
	}
	
	public void setOrderPackages(Set<Package> packages) {
		this.orderPackages = packages;
	}
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	public Set<OrderBl> getOrder_Bl() {
		return order_Bl;
	}

	public void setOrder_Bl(Set<OrderBl> order_Bl) {
		this.order_Bl = order_Bl;
	}

	/**
	 * @param orderParts the orderParts to set
	 */
	public void setOrderParts(Set<OrderPart> orderParts) {
		this.orderParts = orderParts;
	}
	
	/**
	 * 
	 */
	@Transient
	public Set<String> getParts(){
		if(getOrderParts() != null){
			for(OrderPart op : this.getOrderParts()){
				if(op.getPart() != null){
					this.parts.add(op.getPart().getPartNum());
				}
			}
		}
		return this.parts;
	}
	
	/**
	 * Accessor methods for Quantity
	 *
	 * @return TotalQuantity 
	 */
	@Transient
	public Integer getQuantity() {
		int qty = 0;
		if (getOrderParts() != null) {
			for (OrderPart op : this.getOrderParts()) {
				if (op.getPart() != null && op.getQuantity() != null) {
					qty = qty + op.getQuantity();
				}
			}
		}
		return qty;
	}
	/**
	 * Accessor methods for Isbn List
	 *
	 * @return allIsbns 
	 */
	@Transient
	public String getAllIsbns() {
		StringBuilder isbnList = new StringBuilder();
		if (getOrderParts() != null) {
			for (OrderPart op : this.getOrderParts()) {
				if (op.getPart() != null && op.getPart().getIsbn() != null) {
					isbnList.append(op.getPart().getIsbn()).append(" ");
				}
			}
		}
		return isbnList.toString();
	}
	
	/**
	 * Accessor methods for QuantityMax
	 *
	 * @return TotalMaxQuantity 
	 */
	@Transient
	public Integer getQuantityMax() {
		int qty = 0;
		if (getOrderParts() != null) {
			for (OrderPart op : this.getOrderParts()) {
				if (op.getPart() != null && op.getQuantityMax() != null) {
					qty = qty + op.getQuantityMax();
				}
			}
		}
		return qty;
	}
	
	/**
	 * Accessor methods for QuantityMin
	 *
	 * @return TotalMinQuantity 
	 */
	@Transient
	public Integer getQuantityMin() {
		int qty = 0;
		if (getOrderParts() != null) {
			for (OrderPart op : this.getOrderParts()) {
				if (op.getPart() != null && op.getQuantityMin() != null) {
					qty = qty + op.getQuantityMin();
				}
			}
		}
		return qty;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public void setQuantityMax(Integer quantityMax) {
		this.quantityMax = quantityMax;
	}

	public void setQuantityMin(Integer quantityMin) {
		this.quantityMin = quantityMin;
	}

	/**
	 * 
	 */
	@Transient
	public Set<String> getPartsOrigin(){
		return this.parts;
	}
	
	public void setParts(Set<String> parts){
		this.parts = parts;
	}
	
	/**
	 * @return the first order part as currently an order has only one part which is a book.
	 */
	@Transient
	public OrderPart getOrderPart(){
		OrderPart result = null;
		if(getOrderParts() != null && !getOrderParts().isEmpty()){
			result = getOrderParts().iterator().next();
		}
		return result;
	}
	
	/**
	 * @return the spotVarnish
	 */
	@Transient
	public Boolean getSpotVarnish() {
		spotVarnish = false;
		if(getOrderParts() != null && !getOrderParts().isEmpty()){
			for(OrderPart op : getOrderParts()){
				if(op.getPart().getSpotVarnish()){
					spotVarnish = true;
					break;
				}
			}
		}
		return spotVarnish;
	}

	/**
	 * @param spotVarnish the spotVarnish to set
	 */
	public void setSpotVarnish(Boolean spotVarnish) {
		this.spotVarnish = spotVarnish;
	}

	/**
	 * @return the order part by part num
	 */
	@Transient
	public OrderPart getOrderPartByPartNum(String partNum){
		OrderPart result = null;
		if(getOrderParts() != null && !getOrderParts().isEmpty()){
			for(OrderPart op : getOrderParts()){
				if(op.getPart().getPartNum().equals(partNum)){
					result = op;
					break;
				}
			}
			//in case the partNum param is from a child part, try looking by the parent
			if(result == null && (partNum.endsWith("T") || partNum.endsWith("C") || partNum.endsWith("J") || partNum.endsWith("E"))){
				partNum = partNum.substring(0, partNum.length() - 1);
				for(OrderPart op : getOrderParts()){
					if(op.getPart().getPartNum().equals(partNum)){
						result = op;
						break;
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * @param orderPart the orderPart to set
	 */
	@Transient
	@JsonIgnore
	public OrderPart getOrderPartOrigin() {
		return this.orderPart;
	}

	/**
	 * @param orderPart the orderPart to set
	 */
	public void setOrderPart(OrderPart orderPart) {
		this.orderPart = orderPart;
	}

	/**
	 * @return the clientId
	 */
	@Column(name = "Client_Id", length = 25)
	public String getClientId() {
		return clientId;
	}

	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the pnlData
	 */
	@Transient
	public PNLData getPnlData() {
		return pnlData;
	}

	/**
	 * @param pnlData the pnlData to set
	 */
	public void setPnlData(PNLData pnlData) {
		this.pnlData = pnlData;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getOrderId() == null) ? 0 : getOrderId().hashCode());
		result = prime * result + ((getOrderNum() == null) ? 0 : getOrderNum().hashCode());
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
		if (!(obj instanceof Order)) {
			return false;
		}
		Order other = (Order) obj;
		if (getOrderId() == null) {
			if (other.getOrderId() != null) {
				return false;
			}
		} else if (!getOrderId().equals(other.getOrderId())) {
			return false;
		}
		if (getOrderNum() == null) {
			if (other.getOrderNum() != null) {
				return false;
			}
		} else if (!getOrderNum().equals(other.getOrderNum())) {
			return false;
		}
		return true;
	}
	public Date getCompleteDate() {
		return completeDate;
	}
	public void setCompleteDate(Date completeDate) {
		this.completeDate = completeDate;
	}

	
	
}
