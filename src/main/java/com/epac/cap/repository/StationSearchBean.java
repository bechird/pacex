package com.epac.cap.repository;

import com.epac.cap.common.AuditableSearchBean;

/**
 * Container for Station search criteria.
 * 
 * @author walid
 * 
 */
@SuppressWarnings("serial")
public class StationSearchBean extends AuditableSearchBean{
	private String stationId;
	private String stationIdDiff;
	private String parentStationId;
	private String stationCategoryId;
	
	private String partCategoryId;
	private String partCritiriaId;
	private String bindingTypeId;
	
	private String name;
	private String nameExact;
	private String description;
	private Float scheduledHours;
	private Float unscheduledHours;
	private Float productionCapacity;
	private Integer productionOrdering;
	private String inputType;
	private Boolean activeFlag;
	private boolean listing = false;
	
	/**
	 * Default constructor.  Sets the default ordering to name ascending 
	 *
	 **/
	public StationSearchBean(){
		//setOrderBy("name");
	}
	
	/**   
	 * Accessor for the stationId property
	 *
	 * @return stationId
	 */
	public String getStationId(){
		return this.stationId;
	}

	/**       
	 * Mutator for the stationId property
	 *
	 * @param inStationId the new value for the stationId property
	 */	
	public void setStationId(String inStationId){
		this.stationId = inStationId;
	}

	/**
	 * @return the stationIdDiff
	 */
	public String getStationIdDiff() {
		return stationIdDiff;
	}

	/**
	 * @param stationIdDiff the stationIdDiff to set
	 */
	public void setStationIdDiff(String stationIdDiff) {
		this.stationIdDiff = stationIdDiff;
	}

	/**
	 * @return the nameExact
	 */
	public String getNameExact() {
		return nameExact;
	}

	/**
	 * @param nameExact the nameExact to set
	 */
	public void setNameExact(String nameExact) {
		this.nameExact = nameExact;
	}

	/**
	 * @return the stationCategoryId
	 */
	public String getStationCategoryId() {
		return stationCategoryId;
	}

	/**
	 * @param stationCategoryId the stationCategoryId to set
	 */
	public void setStationCategoryId(String stationCategoryId) {
		this.stationCategoryId = stationCategoryId;
	}

	/**
	 * @return the partCategoryId
	 */
	public String getPartCategoryId() {
		return partCategoryId;
	}

	/**
	 * @param partCategoryId the partCategoryId to set
	 */
	public void setPartCategoryId(String partCategoryId) {
		this.partCategoryId = partCategoryId;
	}

	/**
	 * @return the partCritiriaId
	 */
	public String getPartCritiriaId() {
		return partCritiriaId;
	}

	/**
	 * @param partCritiriaId the partCritiriaId to set
	 */
	public void setPartCritiriaId(String partCritiriaId) {
		this.partCritiriaId = partCritiriaId;
	}

	/**
	 * @return the bindingTypeId
	 */
	public String getBindingTypeId() {
		return bindingTypeId;
	}

	/**
	 * @param bindingTypeId the bindingTypeId to set
	 */
	public void setBindingTypeId(String bindingTypeId) {
		this.bindingTypeId = bindingTypeId;
	}

	/**   
	 * Accessor for the parentStationId property
	 *
	 * @return parentStationId
	 */
	public String getParentStationId(){
		return this.parentStationId;
	}

	/**       
	 * Mutator for the parentStationId property
	 *
	 * @param inParentStationId the new value for the parentStationId property
	 */	
	public void setParentStationId(String inParentStationId){
		this.parentStationId = inParentStationId;
	}

	/**   
	 * Accessor for the name property
	 *
	 * @return name
	 */
	public String getName(){
		return this.name;
	}

	/**       
	 * Mutator for the name property
	 *
	 * @param inName the new value for the name property
	 */	
	public void setName(String inName){
		this.name = inName;
	}

	/**   
	 * Accessor for the description property
	 *
	 * @return description
	 */
	public String getDescription(){
		return this.description;
	}

	/**       
	 * Mutator for the description property
	 *
	 * @param inDescription the new value for the description property
	 */	
	public void setDescription(String inDescription){
		this.description = inDescription;
	}

	/**   
	 * Accessor for the scheduledHours property
	 *
	 * @return scheduledHours
	 */
	public Float getScheduledHours(){
		return this.scheduledHours;
	}

	/**       
	 * Mutator for the scheduledHours property
	 *
	 * @param inScheduledHours the new value for the scheduledHours property
	 */	
	public void setScheduledHours(Float inScheduledHours){
		this.scheduledHours = inScheduledHours;
	}

	/**   
	 * Accessor for the unscheduledHours property
	 *
	 * @return unscheduledHours
	 */
	public Float getUnscheduledHours(){
		return this.unscheduledHours;
	}

	/**       
	 * Mutator for the unscheduledHours property
	 *
	 * @param inUnscheduledHours the new value for the unscheduledHours property
	 */	
	public void setUnscheduledHours(Float inUnscheduledHours){
		this.unscheduledHours = inUnscheduledHours;
	}

	/**   
	 * Accessor for the productionCapacity property
	 *
	 * @return productionCapacity
	 */
	public Float getProductionCapacity(){
		return this.productionCapacity;
	}

	/**       
	 * Mutator for the productionCapacity property
	 *
	 * @param inProductionCapacity the new value for the productionCapacity property
	 */	
	public void setProductionCapacity(Float inProductionCapacity){
		this.productionCapacity = inProductionCapacity;
	}

	/**   
	 * Accessor for the productionOrdering property
	 *
	 * @return productionOrderings
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
	 * Accessor for the inputType property
	 *
	 * @return inputType
	 */
	public String getInputType(){
		return this.inputType;
	}

	/**       
	 * Mutator for the inputType property
	 *
	 * @param inInputType the new value for the inputType property
	 */	
	public void setInputType(String inInputType){
		this.inputType = inInputType;
	}

	/**
	 * @return the activeFlag
	 */
	public Boolean getActiveFlag() {
		return activeFlag;
	}

	/**
	 * @param activeFlag the activeFlag to set
	 */
	public void setActiveFlag(Boolean activeFlag) {
		this.activeFlag = activeFlag;
	}

	/**
	 * @return the listing
	 */
	public boolean isListing() {
		return listing;
	}

	/**
	 * @param listing the listing to set
	 */
	public void setListing(boolean listing) {
		this.listing = listing;
	}

	
}