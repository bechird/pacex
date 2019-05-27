package com.epac.cap.repository;

import java.util.List;

import com.epac.cap.common.AuditableSearchBean;

/**
 * Container for Job search criteria.
 * 
 * @author walid
 * 
 */
@SuppressWarnings("serial")
public class JobSearchBean extends AuditableSearchBean{
	private Integer jobId;
	private String partNum;
	private String partFamily;
	private String partCategory;
	private String partColors;
	private String partPapertype;
	private Integer orderId;
	private String stationId;
	private String status;
	private List<String> statusesNotIn;
	private List<String> statusesIn;
	private Integer productionOrdering;
	private Integer rollOrdering;
	private Integer rollId;
	private Boolean rollIdNull;
	private String machineId;
	private Integer machineOrdering;
	private String jobType;
	private Boolean fileSentFlag;
	private String jobPriority;
	private Integer quantityNeeded;
	private Float quantityProduced;
	private Integer splitLevel;
	private Integer maxSplitLevel;
	private Float hours;
	
	private String searchJobIdPart;
	private String searchOrderId;
	private String searchPartNum;
	private String searchRollId;
	private String searchStatus;
	private String searchStationId;
	private String searchSplitLevel;
	private String searchHours;
	private String searchQuantityNeeded;
	private String searchQuantityProduced;
	
	
	
	private Integer resultOffset;
	private boolean listing = false;
	
	/**
	 * Default constructor.  Sets the default ordering to jobId ascending 
	 *
	 **/
	public JobSearchBean(){
		setOrderBy("jobId");
	}
	
	/**   
	 * Accessor for the jobId property
	 *
	 * @return jobId
	 */
	public Integer getJobId(){
		return this.jobId;
	}

	/**       
	 * Mutator for the jobId property
	 *
	 * @param inJobId the new value for the jobId property
	 */	
	public void setJobId(Integer inJobId){
		this.jobId = inJobId;
	}

	/**
	 * @return the orderId
	 */
	public Integer getOrderId() {
		return orderId;
	}

	/**
	 * @param orderId the orderId to set
	 */
	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	/**
	 * @return the stationId
	 */
	public String getStationId() {
		return stationId;
	}

	/**
	 * @param stationId the stationId to set
	 */
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	/**
	 * @return the partNum
	 */
	public String getPartNum() {
		return partNum;
	}

	/**
	 * @param partNum the partNum to set
	 */
	public void setPartNum(String partNum) {
		this.partNum = partNum;
	}

	/**
	 * @return the partFamily
	 */
	public String getPartFamily() {
		return partFamily;
	}

	/**
	 * @param partFamily the partFamily to set
	 */
	public void setPartFamily(String partFamily) {
		this.partFamily = partFamily;
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
	 * @return the statusesNotIn
	 */
	public List<String> getStatusesNotIn() {
		return statusesNotIn;
	}

	/**
	 * @param statusesNotIn the statusesNotIn to set
	 */
	public void setStatusesNotIn(List<String> statusesNotIn) {
		this.statusesNotIn = statusesNotIn;
	}

	/**
	 * @return the statusesIn
	 */
	public List<String> getStatusesIn() {
		return statusesIn;
	}

	/**
	 * @param statusesIn the statusesIn to set
	 */
	public void setStatusesIn(List<String> statusesIn) {
		this.statusesIn = statusesIn;
	}

	/**   
	 * Accessor for the productionOrdering property
	 *
	 * @return productionOrdering
	 */
	public Integer getProductionOrdering(){
		return this.productionOrdering;
	}

	/**       
	 * Mutator for the productionOrdering property
	 *
	 * @param inProductionOrdering the new value for the productionOrdering property
	 */	
	public void setProductionOrdering(Integer inProductionOrdering){
		this.productionOrdering = inProductionOrdering;
	}

	/**   
	 * Accessor for the rollOrdering property
	 *
	 * @return rollOrdering
	 */
	public Integer getRollOrdering(){
		return this.rollOrdering;
	}

	/**       
	 * Mutator for the rollOrdering property
	 *
	 * @param inRollOrdering the new value for the rollOrdering property
	 */	
	public void setRollOrdering(Integer inRollOrdering){
		this.rollOrdering = inRollOrdering;
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
	 * Accessor for the jobType property
	 *
	 * @return jobType
	 */
	public String getJobType(){
		return this.jobType;
	}

	/**       
	 * Mutator for the jobType property
	 *
	 * @param inJobType the new value for the jobType property
	 */	
	public void setJobType(String inJobType){
		this.jobType = inJobType;
	}

	/**   
	 * Accessor for the fileSentFlag property
	 *
	 * @return fileSentFlag
	 */
	public Boolean getFileSentFlag(){
		return this.fileSentFlag;
	}

	/**       
	 * Mutator for the fileSentFlag property
	 *
	 * @param inFileSentFlag the new value for the fileSentFlag property
	 */	
	public void setFileSentFlag(Boolean fileSentFlag){
		this.fileSentFlag = fileSentFlag;
	}

	/**   
	 * Accessor for the jobPriority property
	 *
	 * @return jobPriority
	 */
	public String getJobPriority(){
		return this.jobPriority;
	}

	/**       
	 * Mutator for the jobPriority property
	 *
	 * @param inJobPriority the new value for the jobPriority property
	 */	
	public void setJobPriority(String jobPriority){
		this.jobPriority = jobPriority;
	}

	/**
	 * @return the partCategory
	 */
	public String getPartCategory() {
		return partCategory;
	}

	/**
	 * @param partCategory the partCategory to set
	 */
	public void setPartCategory(String partCategory) {
		this.partCategory = partCategory;
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
	 * @return the rollIdNull
	 */
	public Boolean getRollIdNull() {
		return rollIdNull;
	}

	/**
	 * @param rollIdNull the rollIdNull to set
	 */
	public void setRollIdNull(Boolean rollIdNull) {
		this.rollIdNull = rollIdNull;
	}

	/**
	 * @return the partColors
	 */
	public String getPartColors() {
		return partColors;
	}

	/**
	 * @param partColors the partColors to set
	 */
	public void setPartColors(String partColors) {
		this.partColors = partColors;
	}

	/**
	 * @return the partPapertype
	 */
	public String getPartPapertype() {
		return partPapertype;
	}

	/**
	 * @param partPapertype the partPapertype to set
	 */
	public void setPartPapertype(String partPapertype) {
		this.partPapertype = partPapertype;
	}

	/**
	 * @return the quantityNeeded
	 */
	public Integer getQuantityNeeded() {
		return quantityNeeded;
	}

	/**
	 * @param quantityNeeded the quantityNeeded to set
	 */
	public void setQuantityNeeded(Integer quantityNeeded) {
		this.quantityNeeded = quantityNeeded;
	}

	/**
	 * @return the quantityProduced
	 */
	public Float getQuantityProduced() {
		return quantityProduced;
	}

	/**
	 * @param quantityProduced the quantityProduced to set
	 */
	public void setQuantityProduced(Float quantityProduced) {
		this.quantityProduced = quantityProduced;
	}

	/**
	 * @return the splitLevel
	 */
	public Integer getSplitLevel() {
		return splitLevel;
	}

	/**
	 * @param splitLevel the splitLevel to set
	 */
	public void setSplitLevel(Integer splitLevel) {
		this.splitLevel = splitLevel;
	}

	public Integer getMaxSplitLevel() {
		return maxSplitLevel;
	}

	public void setMaxSplitLevel(Integer maxSplitLevel) {
		this.maxSplitLevel = maxSplitLevel;
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

	public String getSearchJobIdPart() {
		return searchJobIdPart;
	}

	public void setSearchJobIdPart(String searchJobIdPart) {
		this.searchJobIdPart = searchJobIdPart;
	}

	public boolean isListing() {
		return listing;
	}

	public void setListing(boolean listing) {
		this.listing = listing;
	}

	public String getSearchOrderId() {
		return searchOrderId;
	}

	public void setSearchOrderId(String searchOrderId) {
		this.searchOrderId = searchOrderId;
	}

	public String getSearchPartNum() {
		return searchPartNum;
	}

	public void setSearchPartNum(String searchPartNum) {
		this.searchPartNum = searchPartNum;
	}

	public String getSearchRollId() {
		return searchRollId;
	}

	public void setSearchRollId(String searchRollId) {
		this.searchRollId = searchRollId;
	}

	public String getSearchStatus() {
		return searchStatus;
	}

	public void setSearchStatus(String searchStatus) {
		this.searchStatus = searchStatus;
	}

	public String getSearchStationId() {
		return searchStationId;
	}

	public void setSearchStationId(String searchStationId) {
		this.searchStationId = searchStationId;
	}

	public String getSearchSplitLevel() {
		return searchSplitLevel;
	}

	public void setSearchSplitLevel(String searchSplitLevel) {
		this.searchSplitLevel = searchSplitLevel;
	}

	public String getSearchHours() {
		return searchHours;
	}

	public void setSearchHours(String searchHours) {
		this.searchHours = searchHours;
	}

	public String getSearchQuantityNeeded() {
		return searchQuantityNeeded;
	}

	public void setSearchQuantityNeeded(String searchQuantityNeeded) {
		this.searchQuantityNeeded = searchQuantityNeeded;
	}

	public String getSearchQuantityProduced() {
		return searchQuantityProduced;
	}

	public void setSearchQuantityProduced(String searchQuantityProduced) {
		this.searchQuantityProduced = searchQuantityProduced;
	}	
	
	
	
}