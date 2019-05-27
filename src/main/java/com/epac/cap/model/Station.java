package com.epac.cap.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.SortComparator;

import com.epac.cap.common.AuditableBean;
import com.epac.cap.handler.MachinesByNameOrderingComparator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * Station a class representing a station in the system
 */
@Entity
@Table(name = "station")
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "stationId")
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="com.epac.cap")
public class Station extends AuditableBean implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2893659812849765231L;
	private String stationId;
	private String stationCategoryId;
	private String parentStationId;
	private String name;
	private String description;
	private Float scheduledHours;
	private Float unscheduledHours;
	private Float productionCapacity;
	private Integer productionOrdering;
	private String inputType;
	private Boolean activeFlag;
	private List<List<Float[]>> jobsPercentages;
	//@JsonIgnore
	private Set<Job> jobs = new HashSet<Job>(0);
	private Set<Roll> rolls = new HashSet<Roll>(0);
	@JsonManagedReference
	//@XmlTransient
	@JsonIgnore
	private SortedSet<Machine> machines = new TreeSet<Machine>(new MachinesByNameOrderingComparator());
	
	private Set<MachineType> pfMachineTypes = new HashSet<MachineType>(0);
	
	//@JsonIgnore
	//private Set<DefaultStation> defaultStations = new HashSet<DefaultStation>(0);
	
	/**
	 * Default constructor
	 */
	public Station() {
	}

	/**
	 * Constructor which sets the identity property
	 */
	public Station(String stationId) {
		this.stationId = stationId;
	}

	/**
	 * Constructor which sets all of the properties
	 */
	public Station(String stationId, String parentStationId, String name, String description, Float scheduledHours,
			Float unscheduledHours, Float productionCapacity, Integer productionOrdering, Set<Job> jobs
			, SortedSet<Machine> machines
			) {
		this.stationId = stationId;
		this.parentStationId = parentStationId;
		this.name = name;
		this.description = description;
		this.scheduledHours = scheduledHours;
		this.unscheduledHours = unscheduledHours;
		this.productionCapacity = productionCapacity;
		this.productionOrdering = productionOrdering;
		//this.jobs = jobs;
		this.machines = machines;
	}
	
	public Station(String stationId, String name, String station_Category_Id, String description, Boolean active_Flag,
			Integer production_Ordering) {
		
		this.activeFlag = active_Flag;
		this.stationCategoryId = station_Category_Id;
		this.stationId = stationId;
		this.name = name;
		this.description = description;
		this.productionOrdering = production_Ordering;
	}

	public enum inputTypes { 
		Roll("Roll"),
		Sheet("Sheet"),
		Job("Job"),
		Batch("Batch")
		;

		private String name;
		private inputTypes(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}

	/**
	 * Accessor methods for stationId
	 *
	 * @return stationId  
	 */
	@Id

	@Column(name = "Station_Id", unique = true, nullable = false, length = 25)
	public String getStationId() {
		return this.stationId;
	}

	/**
	 * @param stationId the stationId to set
	 */
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	/**
	 * @return the stationCategory
	 */
	@Column(name = "Station_Category_Id", length = 25)
	public String getStationCategoryId() {
		return stationCategoryId;
	}

	/**
	 * @param stationCategory the stationCategory to set
	 */
	public void setStationCategoryId(String stationCategoryId) {
		this.stationCategoryId = stationCategoryId;
	}

	/**
	 * Accessor methods for parentStationId
	 *
	 * @return parentStationId
	 */
	//@ManyToOne(fetch = FetchType.EAGER)
	@Column(name = "Parent_Station_Id", length = 25)
	public String getParentStationId() {
		return this.parentStationId;
	}

	/**
	 * @param parentStationId the parentStationId to set
	 */
	public void setParentStationId(String parentStationId) {
		this.parentStationId = parentStationId;
	}

	/**
	 * Accessor methods for name
	 *
	 * @return name  
	 */

	@Column(name = "Name", length = 75)
	public String getName() {
		return this.name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Accessor methods for description
	 *
	 * @return description  
	 */

	@Column(name = "Description", length = 200)
	public String getDescription() {
		return this.description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Accessor methods for scheduledHours
	 *
	 * @return scheduledHours  
	 */

	@Column(name = "Scheduled_Hours")
	public Float getScheduledHours() {
		return this.scheduledHours;
	}

	/**
	 * @param scheduledHours the scheduledHours to set
	 */
	public void setScheduledHours(Float scheduledHours) {
		this.scheduledHours = scheduledHours;
	}

	/**
	 * Accessor methods for unscheduledHours
	 *
	 * @return unscheduledHours  
	 */

	@Column(name = "Unscheduled_Hours")
	public Float getUnscheduledHours() {
		return this.unscheduledHours;
	}

	/**
	 * @param unscheduledHours the unscheduledHours to set
	 */
	public void setUnscheduledHours(Float unscheduledHours) {
		this.unscheduledHours = unscheduledHours;
	}
	
	/**
	 * @return the jobsPercentages
	 */
	@Transient
	public List<List<Float[]>> getJobsPercentages() {
		return jobsPercentages;
	}

	/**
	 * @param jobsPercentages the jobsPercentages to set
	 */
	public void setJobsPercentages(List<List<Float[]>> jobsPercentages) {
		this.jobsPercentages = jobsPercentages;
	}

	/**
	 * Accessor methods for productionCapacity
	 *
	 * @return productionCapacity  
	 */

	@Column(name = "Production_Capacity")
	public Float getProductionCapacity() {
		return this.productionCapacity;
	}

	/**
	 * @param productionCapacity the productionCapacity to set
	 */
	public void setProductionCapacity(Float productionCapacity) {
		this.productionCapacity = productionCapacity;
	}

	/**
	 * Accessor methods for productionOrdering
	 *
	 * @return productionOrdering  
	 */

	@Column(name = "Production_Ordering")
	public Integer getProductionOrdering() {
		return this.productionOrdering;
	}

	/**
	 * @param productionOrdering the productionOrdering to set
	 */
	public void setProductionOrdering(Integer productionOrdering) {
		this.productionOrdering = productionOrdering;
	}

	/**
	 * Accessor methods for inputType
	 *
	 * @return inputType  
	 */
	@Column(name = "Input_Type", length = 55)
	public String getInputType() {
		return this.inputType;
	}

	/**
	 * @param inputType the inputType to set
	 */
	public void setInputType(String inputType) {
		this.inputType = inputType;
	}
	
	/**
	 * Accessor methods for activeFlag
	 *
	 * @return activeFlag  
	 */

	@Column(name = "Active_Flag")
	public Boolean getActiveFlag() {
		return this.activeFlag;
	}

	/**
	 * @param activeFlag the activeFlag to set
	 */
	public void setActiveFlag(Boolean activeFlag) {
		this.activeFlag = activeFlag;
	}

	/**
	 * Accessor methods for jobs
	 *
	 * @return jobs  
	 

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "station")*/
	@Transient
	public Set<Job> getJobs() {
		return this.jobs;
	}

	/**
	 * @param jobs the jobs to set
	 */
	public void setJobs(Set<Job> jobs) {
		this.jobs = jobs;
	}

	/**
	 * @return the rolls needed to show on the overview schedule board for the station; those rolls are retrieved
	 * by looping through the jobs on the station and getting the roll the job is on.
	 */
	@Transient
	public Set<Roll> getRolls() {
		return rolls;
	}

	/**
	 * @param rolls the rolls to set
	 */
	public void setRolls(Set<Roll> rolls) {
		this.rolls = rolls;
	}

	/**
	 * Accessor methods for machines
	 *
	 * @return machines  
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "stationId", cascade = CascadeType.ALL, orphanRemoval = true)
	//@OrderBy("name desc")
	@SortComparator(MachinesByNameOrderingComparator.class)
	//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public SortedSet<Machine> getMachines() {
		return this.machines;
	}

	/**
	 * @param machines the machines to set
	 */
	public void setMachines(SortedSet<Machine> machines) {
		this.machines = machines;
	}
	
	/**
	 * Method that tells if this station has pop line machine in it 
	 **/
	@Transient
	public boolean hasPopLine() {
		boolean result = false;
		for(Machine m : getMachines()){
			if(MachineType.types.POPLINE.getName().equals(m.getMachineType())){
				result = true;
				break;
			}
		}
		return result;
	}
	
	/**
	 * Method that tells if this station has fly folder machine in it 
	 **/
	@Transient
	public boolean hasFlyFolder() {
		boolean result = false;
		for(Machine m : getMachines()){
			if(MachineType.types.FLYFOLDER.getName().equals(m.getMachineType())){
				result = true;
				break;
			}
		}
		return result;
	}
	
	/**
	 * Method that returns the machine types for the plow folder station
	 **/
	@Transient
	public Set<MachineType> getPfMachineTypes() {
		this.pfMachineTypes = new HashSet<MachineType>();
		for(Machine m : getMachines()){
			if(m.getMachineType() != null){
				if(MachineType.types.PLOWFOLDER.getName().equals(m.getMachineType().getId()) ||
				   MachineType.types.FLYFOLDER.getName().equals(m.getMachineType().getId())	||
				   MachineType.types.POPLINE.getName().equals(m.getMachineType().getId())){
					this.pfMachineTypes.add(m.getMachineType());
				}
			}
		}
		return this.pfMachineTypes;
	}
	
	public void setPfMachineTypes(Set<MachineType> mt){
		this.pfMachineTypes = mt;
	}

	/**
	 * @return the defaultStations
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "stationCategory")
	public Set<DefaultStation> getDefaultStations() {
		return defaultStations;
	}*/

	/**
	 * @param defaultStations the defaultStations to set
	 
	public void setDefaultStations(Set<DefaultStation> defaultStations) {
		this.defaultStations = defaultStations;
	}*/

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result + ((getStationId() == null) ? 0 : getStationId().hashCode());
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
		if (!(obj instanceof Station)) {
			return false;
		}
		Station other = (Station) obj;
		if (getName() == null) {
			if (other.getName() != null) {
				return false;
			}
		} else if (!getName().equals(other.getName())) {
			return false;
		}
		if (getStationId() == null) {
			if (other.getStationId() != null) {
				return false;
			}
		} else if (!getStationId().equals(other.getStationId())) {
			return false;
		}
		return true;
	}

}
