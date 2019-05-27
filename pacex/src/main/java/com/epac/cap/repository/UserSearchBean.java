package com.epac.cap.repository;

import com.epac.cap.common.AuditableSearchBean;

/**
 * Container for User search criteria.
 * 
 * @author walid
 * 
 */
@SuppressWarnings("serial")
public class UserSearchBean extends AuditableSearchBean{
	private String userId;
	private String userIdDiff;
	private String firstName;
	private String lastName;
	private String email;
	private String emailExact;
	private String phoneNum;
	private String loginName;
	private String loginNameExact;
	private String loginPassword;
	private Boolean activeFlag;

	/**
	 * Default constructor.  Sets the default ordering to firstName ascending 
	 *
	 **/
	public UserSearchBean(){
		setOrderBy("firstName");
	}
	
	/**   
	 * Accessor for the userId property
	 *
	 * @return userId
	 */
	public String getUserId(){
		return this.userId;
	}

	/**       
	 * Mutator for the userId property
	 *
	 * @param inUserId the new value for the userId property
	 */	
	public void setUserId(String inUserId){
		this.userId = inUserId;
	}

	/**
	 * @return the userIdDiff
	 */
	public String getUserIdDiff() {
		return userIdDiff;
	}

	/**
	 * @param userIdDiff the userIdDiff to set
	 */
	public void setUserIdDiff(String userIdDiff) {
		this.userIdDiff = userIdDiff;
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
	 * @return the loginNameExact
	 */
	public String getLoginNameExact() {
		return loginNameExact;
	}

	/**
	 * @param loginNameExact the loginNameExact to set
	 */
	public void setLoginNameExact(String loginNameExact) {
		this.loginNameExact = loginNameExact;
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

	/**   
	 * Accessor for the loginName property
	 *
	 * @return loginName
	 */
	public String getLoginName(){
		return this.loginName;
	}

	/**       
	 * Mutator for the loginName property
	 *
	 * @param inLoginName the new value for the loginName property
	 */	
	public void setLoginName(String inLoginName){
		this.loginName = inLoginName;
	}

	/**   
	 * Accessor for the loginPassword property
	 *
	 * @return loginPassword
	 */
	public String getLoginPassword(){
		return this.loginPassword;
	}

	/**       
	 * Mutator for the loginPassword property
	 *
	 * @param inLoginPassword the new value for the loginPassword property
	 */	
	public void setLoginPassword(String inLoginPassword){
		this.loginPassword = inLoginPassword;
	}

	/**   
	 * Accessor for the activeFlag property
	 *
	 * @return activeFlag
	 */
	public Boolean getActiveFlag(){
		return this.activeFlag;
	}

	/**       
	 * Mutator for the activeFlag property
	 *
	 * @param inActiveFlag the new value for the activeFlag property
	 */	
	public void setActiveFlag(Boolean inActiveFlag){
		this.activeFlag = inActiveFlag;
	}

	
}