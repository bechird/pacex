package com.epac.cap.repository;

import com.epac.cap.common.AuditableSearchBean;

/**
 * Container for Customer search criteria.
 * 
 * @author walid
 * 
 */
@SuppressWarnings("serial")
public class CustomerSearchBean extends AuditableSearchBean{
	private Integer customerId;
	private Integer customerIdDiff;
	private String firstName;
	private String lastName;
	private String email;
	private String emailExact;
	private String phoneNum;

	/**
	 * Default constructor.  Sets the default ordering to firstName ascending 
	 *
	 **/
	public CustomerSearchBean(){
		setOrderBy("firstName");
	}
	
	/**   
	 * Accessor for the customerId property
	 *
	 * @return customerId
	 */
	public Integer getCustomerId(){
		return this.customerId;
	}

	/**       
	 * Mutator for the customerId property
	 *
	 * @param inCustomerId the new value for the customerId property
	 */	
	public void setCustomerId(Integer inCustomerId){
		this.customerId = inCustomerId;
	}

	/**
	 * @return the customerIdDiff
	 */
	public Integer getCustomerIdDiff() {
		return customerIdDiff;
	}

	/**
	 * @param customerIdDiff the customerIdDiff to set
	 */
	public void setCustomerIdDiff(Integer customerIdDiff) {
		this.customerIdDiff = customerIdDiff;
	}

	/**
	 * @return the emailExact
	 */
	public String getEmailExact() {
		return emailExact;
	}

	/**
	 * @param emailExact the emailExact to set
	 */
	public void setEmailExact(String emailExact) {
		this.emailExact = emailExact;
	}

	/**   
	 * Accessor for the firstName property
	 *
	 * @return firstName
	 */
	public String getFirstName(){
		return this.firstName;
	}

	/**       
	 * Mutator for the firstName property
	 *
	 * @param inFirstName the new value for the firstName property
	 */	
	public void setFirstName(String inFirstName){
		this.firstName = inFirstName;
	}

	/**   
	 * Accessor for the lastName property
	 *
	 * @return lastName
	 */
	public String getLastName(){
		return this.lastName;
	}

	/**       
	 * Mutator for the lastName property
	 *
	 * @param inLastName the new value for the lastName property
	 */	
	public void setLastName(String inLastName){
		this.lastName = inLastName;
	}

	/**   
	 * Accessor for the email property
	 *
	 * @return email
	 */
	public String getEmail(){
		return this.email;
	}

	/**       
	 * Mutator for the email property
	 *
	 * @param inEmail the new value for the email property
	 */	
	public void setEmail(String inEmail){
		this.email = inEmail;
	}

	/**   
	 * Accessor for the phoneNum property
	 *
	 * @return phoneNum
	 */
	public String getPhoneNum(){
		return this.phoneNum;
	}

	/**       
	 * Mutator for the phoneNum property
	 *
	 * @param inPhoneNum the new value for the phoneNum property
	 */	
	public void setPhoneNum(String inPhoneNum){
		this.phoneNum = inPhoneNum;
	}
}