package com.epac.cap.repository;

import java.util.Date;
import java.util.Set;

import com.epac.cap.common.AuditableSearchBean;
import com.epac.cap.common.DateUtil;

/**
 * Container for Order search criteria.
 * 
 * @author walid
 * 
 */
@SuppressWarnings("serial")
public class OrderSearchBean extends AuditableSearchBean{
	private Integer orderId;
	private Integer orderIdDiff;
	private String orderNum;
	private String orderNumExact;
	private String status;
	private Date dueDateFrom;
	private Date dueDateTo;
	private String priorityLevel;
	private String notes;
	private String source;
	private Integer customerId;
	private Set<String> partNumbers;
    private String company;
    private Date completeDate;
    private String email;
    private String fullName;
    
    private String searchOrderIdPart;
    private Date receivedDateExact;
    private Date dueDateExact;
    
    private String searchIsbn;
    private String searchQuantity;
    
    private Integer resultOffset;
    private boolean listing = false;
    
    private String clientId;
    
	/**
	 * Default constructor.  Sets the default ordering to orderId ascending 
	 *
	 **/
	public OrderSearchBean(){
		setOrderBy("orderId", "desc");
	}
	
	/**   
	 * Accessor for the orderId property
	 *
	 * @return orderId
	 */
	public Integer getOrderId(){
		return this.orderId;
	}

	/**       
	 * Mutator for the orderId property
	 *
	 * @param inOrderId the new value for the orderId property
	 */	
	public void setOrderId(Integer inOrderId){
		this.orderId = inOrderId;
	}

	/**   
	 * Accessor for the orderNum property
	 *
	 * @return orderNum
	 */
	public String getOrderNum(){
		return this.orderNum;
	}

	/**       
	 * Mutator for the orderNum property
	 *
	 * @param inOrderNum the new value for the orderNum property
	 */	
	public void setOrderNum(String inOrderNum){
		this.orderNum = inOrderNum;
	}

	/**
	 * @return the orderIdDiff
	 */
	public Integer getOrderIdDiff() {
		return orderIdDiff;
	}

	/**
	 * @param orderIdDiff the orderIdDiff to set
	 */
	public void setOrderIdDiff(Integer orderIdDiff) {
		this.orderIdDiff = orderIdDiff;
	}

	/**
	 * @return the orderNumExact
	 */
	public String getOrderNumExact() {
		return orderNumExact;
	}

	/**
	 * @param orderNumExact the orderNumExact to set
	 */
	public void setOrderNumExact(String orderNumExact) {
		this.orderNumExact = orderNumExact;
	}

	/**   
	 * Accessor for the status property
	 *
	 * @return status
	 */
	public String getStatus(){
		return this.status;
	}

	/**       
	 * Mutator for the status property
	 *
	 * @param inStatus the new value for the status property
	 */	
	public void setStatus(String inStatus){
		this.status = inStatus;
	}

	/**   
	 * Accessor for the dueDateFrom property
	 *
	 * @return dueDateFrom
	 */
	public Date getDueDateFrom(){
		return this.dueDateFrom;
	}

	/**       
	 * Mutator for the dueDateFrom property
	 *
	 * @param inDueDate the new value for the dueDateFrom property
	 */	
	public void setDueDateFrom(Date inDueDate){
		this.dueDateFrom = inDueDate;
	}

	/**   
	 * Accessor for the dueDateTo property
	 *
	 * @return dueDateTo
	 */
	public Date getDueDateTo(){
		return this.dueDateTo;
	}

	/**       
	 * Mutator for the dueDateTo property
	 *
	 * @param inDueDate the new value for the dueDateTo property
	 */	
	public void setDueDateTo(Date inDueDate){
		if(!DateUtil.hasTime(inDueDate)){
			//date didnt have a time so add the last end time of the day to it so that 
			//date range searches behave as expected
			inDueDate = DateUtil.getEnd(inDueDate);
		}
		this.dueDateTo = inDueDate;
	}
	
	/**   
	 * Accessor for the priorityLevel property
	 *
	 * @return priorityLevel
	 */
	public String getPriorityLevel(){
		return this.priorityLevel;
	}

	/**       
	 * Mutator for the priorityLevel property
	 *
	 * @param inPriorityLevel the new value for the priorityLevel property
	 */	
	public void setPriorityLevel(String inPriorityLevel){
		this.priorityLevel = inPriorityLevel;
	}

	/**   
	 * Accessor for the notes property
	 *
	 * @return notes
	 */
	public String getNotes(){
		return this.notes;
	}

	/**       
	 * Mutator for the notes property
	 *
	 * @param inNotes the new value for the notes property
	 */	
	public void setNotes(String inNotes){
		this.notes = inNotes;
	}

	/**   
	 * Accessor for the source property
	 *
	 * @return source
	 */
	public String getSource(){
		return this.source;
	}

	/**       
	 * Mutator for the source property
	 *
	 * @param inSource the new value for the source property
	 */	
	public void setSource(String inSource){
		this.source = inSource;
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
	 * @return the partNumbers
	 */
	public Set<String> getPartNumbers() {
		return partNumbers;
	}

	/**
	 * @param partNumbers the partNumbers to set
	 */
	public void setPartNumbers(Set<String> partNumbers) {
		this.partNumbers = partNumbers;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public Date getCompleteDate() {
		return completeDate;
	}

	public void setCompleteDate(Date completeDate) {
		this.completeDate = completeDate;
	}

	public Integer getResultOffset() {
		return resultOffset;
	}

	public void setResultOffset(Integer resultOffset) {
		this.resultOffset = resultOffset;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Date getReceivedDateExact() {
		return receivedDateExact;
	}

	public void setReceivedDateExact(Date receivedDateExact) {
		this.receivedDateExact = receivedDateExact;
	}

	public Date getDueDateExact() {
		return dueDateExact;
	}

	public void setDueDateExact(Date dueDateExact) {
		this.dueDateExact = dueDateExact;
	}

	public String getSearchOrderIdPart() {
		return searchOrderIdPart;
	}

	public void setSearchOrderIdPart(String searchOrderIdPart) {
		this.searchOrderIdPart = searchOrderIdPart;
	}

	public String getSearchIsbn() {
		return searchIsbn;
	}

	public void setSearchIsbn(String searchIsbn) {
		this.searchIsbn = searchIsbn;
	}

	public String getSearchQuantity() {
		return searchQuantity;
	}

	public void setSearchQuantity(String searchQuantity) {
		this.searchQuantity = searchQuantity;
	}

	public boolean isListing() {
		return listing;
	}

	public void setListing(boolean listing) {
		this.listing = listing;
	}

	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	
}