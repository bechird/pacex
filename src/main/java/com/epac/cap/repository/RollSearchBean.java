package com.epac.cap.repository;

import java.util.Date;

import com.epac.cap.common.AuditableSearchBean;

/**
 * Container for Roll search criteria.
 * 
 * @author walid
 * 
 */
@SuppressWarnings("serial")
public class RollSearchBean extends AuditableSearchBean{
	private Integer rollId;
	private String rollNum;
	private String rollTag;
	private Integer parentRollId;
	private String machineId;
	private Integer machineOrdering;
	private String rollType;
	private Integer length;
	private Integer weight;
	private String paperType;
	private String status;
	private String[] statusIn;
	private String[] typeIn;
	private Integer utilization;
	private Float hours;
	
	private String searchRollType;
	private String searchRollIdPart;
	private String searchStatus;
	private String searchLength;
	private String searchMachineId;
	private String searchPaperType;
	private String searchHours;
	private String searchUtilization;
	private Date creationDateExact;
	
	private Integer resultOffset;
	private boolean listing = false;

	/**
	 * Default constructor.  Sets the default ordering to rollId ascending 
	 *
	 **/
	public RollSearchBean(){
		setOrderBy("rollId", "desc");
	}
	
	/**   
	 * Accessor for the rollId property
	 *
	 * @return rollId
	 */
	public Integer getRollId(){
		return this.rollId;
	}

	/**       
	 * Mutator for the rollId property
	 *
	 * @param inRollId the new value for the rollId property
	 */	
	public void setRollId(Integer inRollId){
		this.rollId = inRollId;
	}

	/**   
	 * Accessor for the rollNum property
	 *
	 * @return rollNum
	 */
	public String getRollNum(){
		return this.rollNum;
	}

	/**       
	 * Mutator for the rollNum property
	 *
	 * @param inRollNum the new value for the rollNum property
	 */	
	public void setRollNum(String inRollNum){
		this.rollNum = inRollNum;
	}

	/**   
	 * Accessor for the rollTag property
	 *
	 * @return rollTag
	 */
	public String getRollTag(){
		return this.rollTag;
	}

	/**       
	 * Mutator for the rollTag property
	 *
	 * @param inRollTag the new value for the rollTag property
	 */	
	public void setRollTag(String inRollTag){
		this.rollTag = inRollTag;
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
	 * Accessor for the parentRollId property
	 *
	 * @return parentRollId
	 */
	public Integer getParentRollId(){
		return this.parentRollId;
	}

	/**       
	 * Mutator for the parentRollId property
	 *
	 * @param inParentRollId the new value for the parentRollId property
	 */	
	public void setParentRollId(Integer inParentRollId){
		this.parentRollId = inParentRollId;
	}

	/**   
	 * Accessor for the machineOrdering property
	 *
	 * @return machineOrdering
	 */
	public Integer getMachineOrdering(){
		return this.machineOrdering;
	}

	/**       
	 * Mutator for the machineOrdering property
	 *
	 * @param inMachineOrdering the new value for the machineOrdering property
	 */	
	public void setMachineOrdering(Integer inMachineOrdering){
		this.machineOrdering = inMachineOrdering;
	}

	/**   
	 * Accessor for the rollType property
	 *
	 * @return rollType
	 */
	public String getRollType(){
		return this.rollType;
	}

	/**       
	 * Mutator for the rollType property
	 *
	 * @param inRollType the new value for the rollType property
	 */	
	public void setRollType(String inRollType){
		this.rollType = inRollType;
	}

	/**   
	 * Accessor for the length property
	 *
	 * @return length
	 */
	public Integer getLength(){
		return this.length;
	}

	/**       
	 * Mutator for the length property
	 *
	 * @param inLength the new value for the length property
	 */	
	public void setLength(Integer inLength){
		this.length = inLength;
	}

	/**   
	 * Accessor for the weight property
	 *
	 * @return weight
	 */
	public Integer getWeight(){
		return this.weight;
	}

	/**       
	 * Mutator for the weight property
	 *
	 * @param inWeight the new value for the weight property
	 */	
	public void setWeight(Integer inWeight){
		this.weight = inWeight;
	}

	/**   
	 * Accessor for the paperType property
	 *
	 * @return paperType
	 */
	public String getPaperType(){
		return this.paperType;
	}

	/**       
	 * Mutator for the paperType property
	 *
	 * @param inPaperType the new value for the paperType property
	 */	
	public void setPaperType(String inPaperType){
		this.paperType = inPaperType;
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
	 * Accessor for the utilization property
	 *
	 * @return utilization
	 */
	public Integer getUtilization(){
		return this.utilization;
	}

	/**       
	 * Mutator for the utilization property
	 *
	 * @param inUtilization the new value for the utilization property
	 */	
	public void setUtilization(Integer inUtilization){
		this.utilization = inUtilization;
	}

	/**
	 * @return the statusIn
	 */
	public String[] getStatusIn() {
		return statusIn;
	}

	/**
	 * @param statusIn the statusIn to set
	 */
	public void setStatusIn(String[] statusIn) {
		this.statusIn = statusIn;
	}

	/**
	 * @return the typeIn
	 */
	public String[] getTypeIn() {
		return typeIn;
	}

	/**
	 * @param typeIn the typeIn to set
	 */
	public void setTypeIn(String[] typeIn) {
		this.typeIn = typeIn;
	}

	public Integer getResultOffset() {
		return resultOffset;
	}

	public void setResultOffset(Integer resultOffset) {
		this.resultOffset = resultOffset;
	}

	public Float getHours() {
		return hours;
	}

	public void setHours(Float hours) {
		this.hours = hours;
	}

	public String getSearchRollIdPart() {
		return searchRollIdPart;
	}

	public void setSearchRollIdPart(String searchRollIdPart) {
		this.searchRollIdPart = searchRollIdPart;
	}

	public boolean isListing() {
		return listing;
	}

	public void setListing(boolean listing) {
		this.listing = listing;
	}

	public String getSearchRollType() {
		return searchRollType;
	}

	public void setSearchRollType(String searchRollType) {
		this.searchRollType = searchRollType;
	}

	public String getSearchStatus() {
		return searchStatus;
	}

	public void setSearchStatus(String searchStatus) {
		this.searchStatus = searchStatus;
	}

	public String getSearchLength() {
		return searchLength;
	}

	public void setSearchLength(String searchLength) {
		this.searchLength = searchLength;
	}

	public String getSearchMachineId() {
		return searchMachineId;
	}

	public void setSearchMachineId(String searchMachineId) {
		this.searchMachineId = searchMachineId;
	}

	public String getSearchPaperType() {
		return searchPaperType;
	}

	public void setSearchPaperType(String searchPaperType) {
		this.searchPaperType = searchPaperType;
	}

	public String getSearchHours() {
		return searchHours;
	}

	public void setSearchHours(String searchHours) {
		this.searchHours = searchHours;
	}

	public String getSearchUtilization() {
		return searchUtilization;
	}

	public void setSearchUtilization(String searchUtilization) {
		this.searchUtilization = searchUtilization;
	}

	/**
	 * @return the creationDateExact
	 */
	public Date getCreationDateExact() {
		return creationDateExact;
	}

	/**
	 * @param creationDateExact the creationDateExact to set
	 */
	public void setCreationDateExact(Date creationDateExact) {
		this.creationDateExact = creationDateExact;
	}	
	
	
	
}