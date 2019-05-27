package com.epac.cap.repository;

import com.epac.cap.common.AuditableSearchBean;

/**
 * Container for Machine search criteria.
 * 
 * @author walid
 * 
 */
@SuppressWarnings("serial")
public class MachineSearchBean extends AuditableSearchBean{
	private String machineId;
	private String machineIdDiff;
	private String name;
	private String nameExact;
	private String description;
	private String status;
	private String statusDiff;
	private String stationId;
	private Integer currentJobId;
	private String serviceSchedule;
	private String speed;
	private String type;
	private boolean listing = false;
	
	/**
	 * Default constructor.  Sets the default ordering to name ascending 
	 *
	 **/
	public MachineSearchBean(){
		setOrderBy("name");
	}
	
	/**   
	 * Accessor for the machineId property
	 *
	 * @return machineId
	 */
	public String getMachineId(){
		return this.machineId;
	}

	/**       
	 * Mutator for the machineId property
	 *
	 * @param inMachineId the new value for the machineId property
	 */	
	public void setMachineId(String inMachineId){
		this.machineId = inMachineId;
	}

	/**
	 * @return the machineIdDiff
	 */
	public String getMachineIdDiff() {
		return machineIdDiff;
	}

	/**
	 * @param machineIdDiff the machineIdDiff to set
	 */
	public void setMachineIdDiff(String machineIdDiff) {
		this.machineIdDiff = machineIdDiff;
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
	 * Accessor for the serviceSchedule property
	 *
	 * @return serviceSchedule
	 */
	public String getServiceSchedule(){
		return this.serviceSchedule;
	}

	/**       
	 * Mutator for the serviceSchedule property
	 *
	 * @param inServiceSchedule the new value for the serviceSchedule property
	 */	
	public void setServiceSchedule(String inServiceSchedule){
		this.serviceSchedule = inServiceSchedule;
	}

	/**   
	 * Accessor for the speed property
	 *
	 * @return speed
	 */
	public String getSpeed(){
		return this.speed;
	}

	/**       
	 * Mutator for the speed property
	 *
	 * @param inSpeed the new value for the speed property
	 */	
	public void setSpeed(String inSpeed){
		this.speed = inSpeed;
	}

	/**
	 * @return the statusDiff
	 */
	public String getStatusDiff() {
		return statusDiff;
	}

	/**
	 * @param statusDiff the statusDiff to set
	 */
	public void setStatusDiff(String statusDiff) {
		this.statusDiff = statusDiff;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
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