package com.epac.cap.repository;

import java.util.Date;

import com.epac.cap.common.AuditableSearchBean;
import com.epac.cap.common.DateUtil;

/**
 * Container for Log search criteria.
 * 
 * @author walid
 * 
 */
@SuppressWarnings("serial")
public class LogSearchBean extends AuditableSearchBean{
	private Integer logId;
	private String machineId;
	private String event;
	private String result;
	private String cause;
	private Integer currentJobId;
	private Integer rollId;
	private Integer rollLength;
	private Date startTimeFrom;
	private Date startTimeTo;
	private Date finishTimeFrom;
	private Date finishTimeTo;
	private Long counterFeet;
	
	private String searchLogIdPart;
	private String searchMachineIdPart;
	private String searchJobIdPart;
	
	private String searchCausePart;
	private String searchResultPart;
	
    private Date startDateExact;
    private Date finishDateExact;

	private String searchRollIdPart;
	private String searchRollLengthPart;
	private String searchCounterFeetPart;
	
	private Integer resultOffset;

	/**
	 * Default constructor.  Sets the default ordering to logId ascending 
	 *
	 **/
	public LogSearchBean(){
		setOrderBy("logId", "desc");
	}
	
	/**   
	 * Accessor for the logId property
	 *
	 * @return logId
	 */
	public Integer getLogId(){
		return this.logId;
	}

	/**       
	 * Mutator for the logId property
	 *
	 * @param inLogId the new value for the logId property
	 */	
	public void setLogId(Integer inLogId){
		this.logId = inLogId;
	}

	/**
	 * @return the machineId
	 */
	public String getMachineId() {
		return machineId;
	}

	/**
	 * @param machineId the machineId to set
	 */
	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}

	/**   
	 * Accessor for the event property
	 *
	 * @return event
	 */
	public String getEvent(){
		return this.event;
	}

	/**       
	 * Mutator for the event property
	 *
	 * @param inEvent the new value for the event property
	 */	
	public void setEvent(String inEvent){
		this.event = inEvent;
	}

	/**   
	 * Accessor for the result property
	 *
	 * @return result
	 */
	public String getResult(){
		return this.result;
	}

	/**       
	 * Mutator for the result property
	 *
	 * @param inResult the new value for the result property
	 */	
	public void setResult(String inResult){
		this.result = inResult;
	}

	/**   
	 * Accessor for the cause property
	 *
	 * @return cause
	 */
	public String getCause(){
		return this.cause;
	}

	/**       
	 * Mutator for the cause property
	 *
	 * @param inCause the new value for the cause property
	 */	
	public void setCause(String inCause){
		this.cause = inCause;
	}

	/**   
	 * Accessor for the currentJobId property
	 *
	 * @return currentJobId
	 */
	public Integer getCurrentJobId(){
		return this.currentJobId;
	}

	/**       
	 * Mutator for the currentJobId property
	 *
	 * @param inCurrentJobId the new value for the currentJobId property
	 */	
	public void setCurrentJobId(Integer inCurrentJobId){
		this.currentJobId = inCurrentJobId;
	}

	/**
	 * @return the rollId
	 */
	public Integer getRollId() {
		return rollId;
	}

	/**
	 * @param rollId the rollId to set
	 */
	public void setRollId(Integer rollId) {
		this.rollId = rollId;
	}

	/**   
	 * Accessor for the rollLength property
	 *
	 * @return rollLength
	 */
	public Integer getRollLength(){
		return this.rollLength;
	}

	/**       
	 * Mutator for the rollLength property
	 *
	 * @param inRollLength the new value for the rollLength property
	 */	
	public void setRollLength(Integer inRollLength){
		this.rollLength = inRollLength;
	}

	/**   
	 * Accessor for the startTimeFrom property
	 *
	 * @return startTimeFrom
	 */
	public Date getStartTimeFrom(){
		return this.startTimeFrom;
	}

	/**       
	 * Mutator for the startTimeFrom property
	 *
	 * @param inStartTime the new value for the startTimeFrom property
	 */	
	public void setStartTimeFrom(Date inStartTime){
		this.startTimeFrom = inStartTime;
	}

	/**   
	 * Accessor for the startTimeTo property
	 *
	 * @return startTimeTo
	 */
	public Date getStartTimeTo(){
		return this.startTimeTo;
	}

	/**       
	 * Mutator for the startTimeTo property
	 *
	 * @param inStartTime the new value for the startTimeTo property
	 */	
	public void setStartTimeTo(Date inStartTime){
		if(!DateUtil.hasTime(inStartTime)){
			//date didnt have a time so add the last end time of the day to it so that 
			//date range searches behave as expected
			inStartTime = DateUtil.getEnd(inStartTime);
		}
		this.startTimeTo = inStartTime;
	}
	
	/**   
	 * Accessor for the finishTimeFrom property
	 *
	 * @return finishTimeFrom
	 */
	public Date getFinishTimeFrom(){
		return this.finishTimeFrom;
	}

	/**       
	 * Mutator for the finishTimeFrom property
	 *
	 * @param inFinishTime the new value for the finishTimeFrom property
	 */	
	public void setFinishTimeFrom(Date inFinishTime){
		this.finishTimeFrom = inFinishTime;
	}

	/**   
	 * Accessor for the finishTimeTo property
	 *
	 * @return finishTimeTo
	 */
	public Date getFinishTimeTo(){
		return this.finishTimeTo;
	}

	/**       
	 * Mutator for the finishTimeTo property
	 *
	 * @param inFinishTime the new value for the finishTimeTo property
	 */	
	public void setFinishTimeTo(Date inFinishTime){
		if(!DateUtil.hasTime(inFinishTime)){
			//date didnt have a time so add the last end time of the day to it so that 
			//date range searches behave as expected
			inFinishTime = DateUtil.getEnd(inFinishTime);
		}
		this.finishTimeTo = inFinishTime;
	}
	
	/**   
	 * Accessor for the counterFeet property
	 *
	 * @return counterFeet
	 */
	public Long getCounterFeet(){
		return this.counterFeet;
	}

	/**       
	 * Mutator for the counterFeet property
	 *
	 * @param inCounterFeet the new value for the counterFeet property
	 */	
	public void setCounterFeet(Long inCounterFeet){
		this.counterFeet = inCounterFeet;
	}
	

	public Integer getResultOffset() {
		return resultOffset;
	}

	public void setResultOffset(Integer resultOffset) {
		this.resultOffset = resultOffset;
	}

	public String getSearchLogIdPart() {
		return searchLogIdPart;
	}

	public void setSearchLogIdPart(String searchLogIdPart) {
		this.searchLogIdPart = searchLogIdPart;
	}

	public String getSearchMachineIdPart() {
		return searchMachineIdPart;
	}

	public void setSearchMachineIdPart(String searchMachineIdPart) {
		this.searchMachineIdPart = searchMachineIdPart;
	}

	public String getSearchJobIdPart() {
		return searchJobIdPart;
	}

	public void setSearchJobIdPart(String searchJobIdPart) {
		this.searchJobIdPart = searchJobIdPart;
	}

	public String getSearchCausePart() {
		return searchCausePart;
	}

	public void setSearchCausePart(String searchCausePart) {
		this.searchCausePart = searchCausePart;
	}

	public String getSearchResultPart() {
		return searchResultPart;
	}

	public void setSearchResultPart(String searchResultPart) {
		this.searchResultPart = searchResultPart;
	}

	public Date getStartDateExact() {
		return startDateExact;
	}

	public void setStartDateExact(Date startDateExact) {
		this.startDateExact = startDateExact;
	}

	public Date getFinishDateExact() {
		return finishDateExact;
	}

	public void setFinishDateExact(Date finishDateExact) {
		this.finishDateExact = finishDateExact;
	}

	public String getSearchRollIdPart() {
		return searchRollIdPart;
	}

	public void setSearchRollIdPart(String searchRollIdPart) {
		this.searchRollIdPart = searchRollIdPart;
	}

	public String getSearchRollLengthPart() {
		return searchRollLengthPart;
	}

	public void setSearchRollLengthPart(String searchRollLengthPart) {
		this.searchRollLengthPart = searchRollLengthPart;
	}

	public String getSearchCounterFeetPart() {
		return searchCounterFeetPart;
	}

	public void setSearchCounterFeetPart(String searchCounterFeetPart) {
		this.searchCounterFeetPart = searchCounterFeetPart;
	}

	
	
}