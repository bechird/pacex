package com.epac.cap.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.epac.cap.common.AuditableBean;

/**
 * Customer class representing a customer
 */
@Entity
@Table(name = "customer")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="com.epac.cap")
public class Customer extends AuditableBean implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 595658271772920687L;
	private Integer customerId;
	private String firstName;
	private String lastName;
	private String fullName;
	private String email;
	private String phoneNum;
	
	private String clientId;
	
	//@JsonIgnore
	//private Set<Order> orders = new HashSet<Order>(0);
	
	/**
	 * Default constructor
	 */
	public Customer() {
	}

	/**
	 * Constructor which sets the identity property
	 */
	public Customer(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

	/**
	 * Constructor which sets all of the properties
	 */
	public Customer(String firstName, String lastName, String email, String phoneNum) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.phoneNum = phoneNum;
	}

	/**
	 * Accessor methods for customerId
	 *
	 * @return customerId  
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "Customer_Id", unique = true, nullable = false)
	public Integer getCustomerId() {
		return this.customerId;
	}

	/**
	 * @param customerId the customerId to set
	 */
	public void setCustomerId(Integer customerId) {
		this.customerId = customerId;
	}

	/**
	 * Accessor methods for firstName
	 *
	 * @return firstName  
	 */

	@Column(name = "First_Name", nullable = false, length = 50)
	public String getFirstName() {
		return this.firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Accessor methods for lastName
	 *
	 * @return lastName  
	 */

	@Column(name = "Last_Name", nullable = false, length = 50)
	public String getLastName() {
		return this.lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the full name
	 */
	@Transient
	public String getFullName() {
		this.fullName = this.firstName + " " + this.lastName;
		return this.fullName;
	}
	
	/**
	 * @param lastName the lastName to set
	 */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	/**
	 * Accessor methods for email
	 *
	 * @return email  
	 */

	@Column(name = "Email", length = 70)
	public String getEmail() {
		return this.email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Accessor methods for phoneNum
	 *
	 * @return phoneNum  
	 */

	@Column(name = "Phone_Num", length = 15)
	public String getPhoneNum() {
		return this.phoneNum;
	}

	/**
	 * @param phoneNum the phoneNum to set
	 */
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	/**
	 * @return the orders
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "customer")
	public Set<Order> getOrders() {
		return orders;
	}*/

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
	 * @param orders the orders to set
	 
	public void setOrders(Set<Order> orders) {
		this.orders = orders;
	}*/

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getCustomerId() == null) ? 0 : getCustomerId().hashCode());
		result = prime * result + ((getEmail() == null) ? 0 : getEmail().hashCode());
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
		if (!(obj instanceof Customer)) {
			return false;
		}
		Customer other = (Customer) obj;
		if (getCustomerId() == null) {
			if (other.getCustomerId() != null) {
				return false;
			}
		} else if (!getCustomerId().equals(other.getCustomerId())) {
			return false;
		}
		if (getEmail() == null) {
			if (other.getEmail() != null) {
				return false;
			}
		} else if (!getEmail().equals(other.getEmail())) {
			return false;
		}
		return true;
	}

	
}
