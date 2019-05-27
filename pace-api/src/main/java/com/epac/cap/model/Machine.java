package com.epac.cap.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.FieldResult;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.EntityResult;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.SortComparator;
import org.hibernate.annotations.Where;

import com.epac.cap.common.AuditableBean;
import com.epac.cap.common.MachineSortedSetDeserializer;
import com.epac.cap.common.BatchSortedSetDeserializer;
import com.epac.cap.common.DateUtil;
import com.epac.cap.handler.SectionsByMachineOrderingComparator;
import com.epac.cap.handler.JobsByMachineOrderingComparator;
import com.epac.cap.handler.LogsByChronologicalOrderingComparator;
import com.epac.cap.handler.RollsByMachineOrderingComparator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Machine class representing a machine in the system
 */
@Entity
@Table(name = "machine")
public class Machine extends AuditableBean implements java.io.Serializable {
	/**
	 * 
	 */
	
	private static final long serialVersionUID = -5433275278416915004L;
	
	private String machineId;
	private String stationId;
	private String name;
	private String description;
	private String serviceSchedule;
	private String speed;
	private String fullIpAddress;
	private String ipAddress;
	private Integer netPort;
	private String ocInputPath;
	private MachineStatus status;
	private MachineType machineType;
	private Job currentJob;
	
	@JsonDeserialize(using = MachineSortedSetDeserializer.class)
	private SortedSet<Log> logs = new TreeSet<Log>(new LogsByChronologicalOrderingComparator());
	@JsonDeserialize(using = MachineSortedSetDeserializer.class)
	private SortedSet<Roll> assignedRolls = new TreeSet<Roll>(new RollsByMachineOrderingComparator());
	@JsonDeserialize(using = MachineSortedSetDeserializer.class)
	private SortedSet<Job> assignedJobs = new TreeSet<Job>(new JobsByMachineOrderingComparator());
	@JsonDeserialize(using = BatchSortedSetDeserializer.class)
	private SortedSet<CoverSection> assignedSections = new TreeSet<CoverSection>(new SectionsByMachineOrderingComparator());
	@JsonDeserialize(using = MachineSortedSetDeserializer.class)
	private SortedSet<Roll> runningRolls = new TreeSet<Roll>(new RollsByMachineOrderingComparator());
	@JsonDeserialize(using = MachineSortedSetDeserializer.class)
	private SortedSet<Job> runningJobs = new TreeSet<Job>(new JobsByMachineOrderingComparator());
	@JsonDeserialize(using = BatchSortedSetDeserializer.class)
	private SortedSet<CoverSection> runningSections = new TreeSet<CoverSection>(new SectionsByMachineOrderingComparator());
	@JsonDeserialize(using = MachineSortedSetDeserializer.class)
	private SortedSet<Job> runningAndAssignedJobs = new TreeSet<Job>(new JobsByMachineOrderingComparator());
	@JsonDeserialize(using = MachineSortedSetDeserializer.class)
	private SortedSet<Roll> runningAndAssignedRolls = new TreeSet<Roll>(new RollsByMachineOrderingComparator());
	@JsonDeserialize(using = BatchSortedSetDeserializer.class)
	private SortedSet<CoverSection> runningAndAssignedSections  = new TreeSet<CoverSection>(new SectionsByMachineOrderingComparator());
	
	private Float rollHours;
	private Float jobHours;
	
	private Roll rollOnProd;
	private Job jobOnProd;
	private CoverSection coverSectionOnProd;
	private Set<Pallette> pallets = new HashSet<Pallette>();
	private Boolean isEpacModePrintingActive;
	private String epacModePrinterStatus;
	private Boolean isEpacModePrinterStatusChanged = false;
	private Boolean isEmfMode;
	
	/**
	 * Default constructor
	 */
	public Machine() {
	}

	/**
	 * Constructor which sets the identity property
	 */
	public Machine(String machineId) {
		this.machineId = machineId;
	}

	/**
	 * Constructor which sets all of the properties
	 */
	public Machine(String machineId, Station station, String name, String description, 
			String serviceSchedule, String speed, SortedSet<Log> logs, SortedSet<Job> jobs, SortedSet<Roll> rolls) {
		this.machineId = machineId;
		//this.station = station;
		this.name = name;
		this.description = description;
		this.serviceSchedule = serviceSchedule;
		this.speed = speed;
		this.logs = logs;
		//this.jobs = jobs;
		//this.rolls = rolls;
	}
	public Machine(String machineId, String name,MachineStatus status,String ocInPutPath, String ipAddress) {
		this.machineId = machineId;
		this.name = name;
		this.status = status;
		this.ocInputPath = ocInPutPath;
		this.ipAddress = ipAddress;
	}
	/**
	 * Accessor methods for machineId
	 *
	 * @return machineId  
	 */
	@Id

	@Column(name = "Machine_Id", unique = true, nullable = false, length = 25)
	public String getMachineId() {
		return this.machineId;
	}

	/**
	 * @param machineId the machineId to set
	 */
	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}
	
	/**
	 * Accessor methods for station
	 *
	 * @return station  
	 

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Station_Id")
	public Station getStation() {
		return this.station;
	}*/

	/**
	 * @param station the station to set
	 
	public void setStation(Station station) {
		this.station = station;
	}*/

	/**
	 * @return the stationId
	 */
	@Column(name = "Station_Id", length = 25)
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
	 * Accessor methods for name
	 *
	 * @return name  
	 */

	@Column(name = "Name", length = 100)
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
	 * Accessor methods for status
	 *
	 * @return status  
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Status")
	public MachineStatus getStatus() {
		return this.status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(MachineStatus status) {
		this.status = status;
	}

	/**
	 * Accessor methods for currentJob
	 *
	 * @return currentJob
	 */
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Current_Job_Id", referencedColumnName = "Job_Id")
	public Job getCurrentJob() {
		return this.currentJob;
	}

	/**
	 * @param currentJob the currentJob to set
	 */
	public void setCurrentJob(Job currentJob) {
		this.currentJob = currentJob;
	}

	/**
	 * Accessor methods for serviceSchedule
	 *
	 * @return serviceSchedule  
	 */

	@Column(name = "Service_Schedule", length = 100)
	public String getServiceSchedule() {
		return this.serviceSchedule;
	}

	/**
	 * @return the currentJobId
	 
	@Column(name = "Current_Job_Id")
	public Integer getCurrentJobId() {
		return currentJobId;
	}*/

	/**
	 * @param currentJobId the currentJobId to set
	 
	public void setCurrentJobId(Integer currentJobId) {
		this.currentJobId = currentJobId;
	}*/

	/**
	 * @param serviceSchedule the serviceSchedule to set
	 */
	public void setServiceSchedule(String serviceSchedule) {
		this.serviceSchedule = serviceSchedule;
	}

	/**
	 * Accessor methods for speed
	 *
	 * @return speed  
	 */
	@Column(name = "Speed", length = 15)
	public String getSpeed() {
		return this.speed;
	}

	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(String speed) {
		this.speed = speed;
	}

	/**
	 * @return the isEpacModePrintingActive
	 */
	@Transient
	public Boolean getIsEpacModePrintingActive() {
		return isEpacModePrintingActive;
	}

	/**
	 * @param isEpacModePrintingActive the isEpacModePrintingActive to set
	 */
	public void setIsEpacModePrintingActive(Boolean isEpacModePrintingActive) {
		this.isEpacModePrintingActive = isEpacModePrintingActive;
	}

	/**
	 * @return the epacModePrinterStatus
	 */
	@Transient
	public String getEpacModePrinterStatus() {
		return epacModePrinterStatus;
	}

	/**
	 * @param epacModePrinterStatus the epacModePrinterStatus to set
	 */
	public void setEpacModePrinterStatus(String epacModePrinterStatus) {
		this.epacModePrinterStatus = epacModePrinterStatus;
	}

	/**
	 * @return the isEpacModePrinterStatusChanged
	 */
	@Transient
	public Boolean getIsEpacModePrinterStatusChanged() {
		return isEpacModePrinterStatusChanged;
	}

	/**
	 * @param isEpacModePrinterStatusChanged the isEpacModePrinterStatusChanged to set
	 */
	public void setIsEpacModePrinterStatusChanged(Boolean isEpacModePrinterStatusChanged) {
		this.isEpacModePrinterStatusChanged = isEpacModePrinterStatusChanged;
	}

	/**
	 * @return the isEmfMode
	 */
	@Transient
	public Boolean getIsEmfMode() {
		return isEmfMode;
	}

	/**
	 * @param isEmfMode the isEmfMode to set
	 */
	public void setIsEmfMode(Boolean isEmfMode) {
		this.isEmfMode = isEmfMode;
	}

	/**
	 * @return the type
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "MachineType")
	public MachineType getMachineType() {
		return machineType;
	}

	/**
	 * @param type the type to set
	 */
	public void setMachineType(MachineType type) {
		this.machineType = type;
	}
	
	/**
	 * @return the ipAddress
	 */
	@Column(name = "IpAddress", length = 45)
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * @param ipAddress the ipAddress to set
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * @return the netPort
	 */
	@Column(name = "Port")
	public Integer getNetPort() {
		return netPort;
	}

	/**
	 * @param netPort the netPort to set
	 */
	public void setNetPort(Integer netPort) {
		this.netPort = netPort;
	}
	
	/**
	 * @return the ocInputPath
	 */
	@Column(name = "OcInputPath", length = 100)
	public String getOcInputPath() {
		return ocInputPath;
	}

	/**
	 * @param ocInputPath the ocInputPath to set
	 */
	public void setOcInputPath(String ocInputPath) {
		this.ocInputPath = ocInputPath;
	}

	@Transient
	public String getFullIpAddress(){
		this.fullIpAddress = "";
		if(!StringUtils.isBlank(this.getIpAddress())){
			this.fullIpAddress = this.getIpAddress();
		}
		if(this.getNetPort() != null){
			this.fullIpAddress = this.fullIpAddress + ":" + this.getNetPort();
		}
		return this.fullIpAddress;
	}
	
	/**
	 * @param fullIpAddress the fullIpAddress to set
	 */
	public void setFullIpAddress(String fullIpAddress) {
		this.fullIpAddress = fullIpAddress;
	}
	
	/**
	 * @return the logs
	 * Here we try to limit the number of returned records for performance reason and based on the need; so we only get records that are
	 * only 3 days old or less
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "machineId")
	@SortComparator(LogsByChronologicalOrderingComparator.class)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="com.epac.cap")
	//FIXME: elamine - try and fix if does not work
	@Formula("(select log from Log log where log.machineId = machineId desc limit 0, 20 )" )
	public SortedSet<Log> getLogs() {
		Date limitDate = DateUtil.addDaysToDate(new Date(), -3);
		logs.removeIf(alog ->  alog.getCreatedDate() != null && alog.getCreatedDate().before(limitDate));
		return logs;
	}

	/**
	 * @param logs the logs to set
	 */
	public void setLogs(SortedSet<Log> logs) {
		this.logs = logs;
	}
	
	/**
	 * @return the assignedRolls
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "machineId", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@Where(clause = "status = 'ASSIGNED'")
	@SortComparator(RollsByMachineOrderingComparator.class)
	public SortedSet<Roll> getAssignedRolls() {
		return assignedRolls;
	}

	/**
	 * @param assignedRolls the assignedRolls to set
	 */
	public void setAssignedRolls(SortedSet<Roll> assignedRolls) {
		this.assignedRolls = assignedRolls;
	}
	
	/**
	 * @return the runningRolls
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "machineId")
	@Where(clause = "status = 'ONPROD'")
	@SortComparator(RollsByMachineOrderingComparator.class)
	public SortedSet<Roll> getRunningRolls() {
		return runningRolls;
	}

	/**
	 * @param runningRolls the runningRolls to set
	 */
	public void setRunningRolls(SortedSet<Roll> runningRolls) {
		this.runningRolls = runningRolls;
	}
	
	/**
	 * @return the runningAndAssignedRolls
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "machineId")
	@Where(clause = "Status = 'ONPROD' or Status = 'ASSIGNED'")
	@SortComparator(RollsByMachineOrderingComparator.class)*/
	@Transient
	public SortedSet<Roll> getRunningAndAssignedRolls() {
		runningAndAssignedRolls.addAll(this.getRunningRolls());
		runningAndAssignedRolls.addAll(this.getAssignedRolls());
		return runningAndAssignedRolls;
	}
	
	/**
	 * @param runningAndAssignedRolls the runningAndAssignedRolls to set
	 */
	public void setRunningAndAssignedRolls(SortedSet<Roll> runningAndAssignedRolls) {
		this.runningAndAssignedRolls = runningAndAssignedRolls;
	}
	
	/**
	 * @return the assignedSections
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "machineId", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@Where(clause = "status = 'ASSIGNED'")
	@SortComparator(SectionsByMachineOrderingComparator.class)
	public SortedSet<CoverSection> getAssignedSections() {
		return assignedSections;
	}

	/**
	 * @param assignedSections the assignedSections to set
	 */
	public void setAssignedSections(SortedSet<CoverSection> assignedSections) {
		this.assignedSections = assignedSections;
	}	
	
	/**
	 * @return the runningBatches
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "machineId")
	@Where(clause = "status = 'ONPROD'")
	@SortComparator(SectionsByMachineOrderingComparator.class)
	public SortedSet<CoverSection> getRunningSections() {
		return runningSections;
	}

	/**
	 * @param runningSections the runningBatches to set
	 */
	public void setRunningSections(SortedSet<CoverSection> runningSections) {
		this.runningSections = runningSections;
	}
	
	/**
	 * @return the runningAndAssignedSections
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "machineId")
	@Where(clause = "Status = 'ONPROD' or Status = 'ASSIGNED'")
	@SortComparator(SectionsByMachineOrderingComparator.class)*/
	@Transient
	public SortedSet<CoverSection> getRunningAndAssignedSections() {
		runningAndAssignedSections.addAll(this.getRunningSections());
		runningAndAssignedSections.addAll(this.getAssignedSections());
		return runningAndAssignedSections;
	}
	
	/**
	 * @param runningAndAssignedSections the runningAndAssignedSections to set
	 */
	public void setRunningAndAssignedSections(SortedSet<CoverSection> runningAndAssignedSections) {
		this.runningAndAssignedSections = runningAndAssignedSections;
	}

	/**
	 * @return the assignedJobs
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "machineId", cascade = {CascadeType.MERGE})
	@Where(clause = "Status = 'ASSIGNED'")
	@SortComparator(JobsByMachineOrderingComparator.class)
	public SortedSet<Job> getAssignedJobs() {
		return assignedJobs;
	}

	/**
	 * @param assignedJobs the assignedJobs to set
	 */
	public void setAssignedJobs(SortedSet<Job> assignedJobs) {
		this.assignedJobs = assignedJobs;
	}
	
	/**
	 * @return the runningJobs
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "machineId")
	@Where(clause = "Status = 'RUNNING'")
	@SortComparator(JobsByMachineOrderingComparator.class)
	public SortedSet<Job> getRunningJobs() {
		return runningJobs;
	}

	/**
	 * @param runningJobs the runningJobs to set
	 */
	public void setRunningJobs(SortedSet<Job> runningJobs) {
		this.runningJobs = runningJobs;
	}

	/**
	 * @return the runningAndAssignedJobs
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "machineId")
	@Where(clause = "Status = 'RUNNING' or Status = 'ASSIGNED'")
	@SortComparator(JobsByMachineOrderingComparator.class)*/
	@Transient
	public SortedSet<Job> getRunningAndAssignedJobs() {
		runningAndAssignedJobs.addAll(this.getRunningJobs());
		runningAndAssignedJobs.addAll(this.getAssignedJobs());
		return runningAndAssignedJobs;
	}

	/**
	 * @param runningAndAssignedJobs the runningAndAssignedJobs to set
	 */
	public void setRunningAndAssignedJobs(SortedSet<Job> runningAndAssignedJobs) {
		this.runningAndAssignedJobs = runningAndAssignedJobs;
	}
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "machineId")
	public Set<Pallette> getPallets() {
		return pallets;
	}

	public void setPallets(Set<Pallette> pallets) {
		this.pallets = pallets;
	}

	/**
	 * Returns the roll associated to this machine, which is on production (running), or if none the one to be produced next 
	 */
	@Transient
	public Roll getRollOnProd(){
		if(this.getRunningRolls().size() > 0){
			this.rollOnProd = this.getRunningRolls().iterator().next();
		}
		if(this.rollOnProd == null && this.getAssignedRolls().size() > 0){
			this.rollOnProd = this.getAssignedRolls().iterator().next();
		}
		return this.rollOnProd;
	}
	public void setRollOnProd(Roll roll){
		this.rollOnProd = roll;
	}

	/**
	 * @return the cover sectionOnProd
	 */
	@Transient
	public CoverSection getCoverSectionOnProd() {
		if(this.getRunningSections().size() > 0){
			this.coverSectionOnProd = this.getRunningSections().iterator().next();
		}
		if(this.coverSectionOnProd == null && this.getAssignedSections().size() > 0){
			this.coverSectionOnProd = this.getAssignedSections().iterator().next();
		}
		return this.coverSectionOnProd;
	}

	/**
	 * @param coverSectionOnProd the coverSectionOnProd to set
	 */
	public void setCoverSectionOnProd(CoverSection coverSectionOnProd) {
		this.coverSectionOnProd = coverSectionOnProd;
	}
	/**
	 * @return the jobOnProd
	 */
	@Transient
	public Job getJobOnProd() {
		if(this.getRunningJobs().size() > 0){
			this.jobOnProd = this.getRunningJobs().iterator().next();
		}
		if(this.jobOnProd == null && this.getAssignedJobs().size() > 0){
			this.jobOnProd = this.getAssignedJobs().iterator().next();
		}
		return this.jobOnProd;
	}

	/**
	 * @param jobOnProd the jobOnProd to set
	 */
	public void setJobOnProd(Job jobOnProd) {
		this.jobOnProd = jobOnProd;
	}
	
	@Transient
	public Float getJobHours(){
		this.jobHours = (float) 0;
		for(Job job : this.getRunningAndAssignedJobs()){
			this.jobHours += job.getHours();
		}
		return this.jobHours;
	}
	public void setJobHours(Float hours){
		this.jobHours = hours;
	}
	
	@Transient
	public Float getRollHours(){
		this.rollHours = (float) 0;
		for(Roll roll : this.getRunningAndAssignedRolls()){
			this.rollHours += roll.getHours();
		}
		return this.rollHours;
	}
	
	public void setRollHours(Float hours){
		this.rollHours = hours;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getMachineId() == null) ? 0 : getMachineId().hashCode());
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
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
		if (!(obj instanceof Machine)) {
			return false;
		}
		Machine other = (Machine) obj;
		if (getMachineId() == null) {
			if (other.getMachineId() != null) {
				return false;
			}
		} else if (!getMachineId().equals(other.getMachineId())) {
			return false;
		}
		if (getName() == null) {
			if (other.getName() != null) {
				return false;
			}
		} else if (!getName().equals(other.getName())) {
			return false;
		}
		return true;
	}

}

