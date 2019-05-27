package com.epac.cap.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.SortComparator;

import com.epac.cap.common.AuditableBean;
import com.epac.cap.common.MachineSortedSetDeserializer;
import com.epac.cap.common.StringUtil;
import com.epac.cap.handler.BatchesByRollOrderingComparator;
import com.epac.cap.handler.JobsByRollOrderingComparator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Roll a class representing a Roll
 */
@Entity
@Table(name = "roll")
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="com.epac.cap")
public class Roll extends AuditableBean implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2776171480466284590L;
	private Integer rollId;
	//private Machine machine;
	private String machineId;
	private String rollNum;
	private String rollTag;
	private Integer parentRollId;
	private Integer machineOrdering;
	private RollType rollType;
	private Integer length;
	private Integer producedLength;
	private float width;
	private Integer weight;
	private PaperType paperType;
	private RollStatus status;
	private float hours;
	private Integer utilization;
	private String copyStatus;
	private Date creationDate;
	
	@JsonDeserialize(using = MachineSortedSetDeserializer.class)
	private SortedSet<Job> jobs = new TreeSet<Job>(new JobsByRollOrderingComparator());

	@JsonDeserialize(using = MachineSortedSetDeserializer.class)
	private SortedSet<Job> alljobs = new TreeSet<Job>(new JobsByRollOrderingComparator());

	@JsonIgnore
	private Set<CoverBatch> batches = new HashSet<CoverBatch>(0);//new BatchesByRollOrderingComparator());
	
	private Date dueDate;
	private String colors;
	private String priority;
	private Boolean allJobsComplete;
	private Boolean allJobsRipped;
	private Boolean rollJobsStarted;
	private String machineTypeId;
	private String impositionTypeId;
	private String productionMode;
	
	private Float calculatedWaste;

	/**
	 * Default constructor
	 */
	public Roll() {
	}
	
	/**
	 * Constructor which sets the roll id
	 */
	public Roll(Integer id) {
		this.rollId = id;
	}

	/**
	 * Constructor which sets all of the properties
	 */
	public Roll(Machine machine, String rollNum, String rollTag, Roll parentRoll, Integer machineOrdering,
			String rollType, Integer length, float width, Integer weight, 
			float hours, Integer utilization, SortedSet<Job> jobs, Set<Log> logs,SortedSet<CoverBatch> batches) {
		//this.machine = machine;
		this.rollNum = rollNum;
		this.rollTag = rollTag;
		//this.parentRoll = parentRoll;
		this.machineOrdering = machineOrdering;
		this.length = length;
		this.width = width;
		this.weight = weight;
		this.hours = hours;
		this.utilization = utilization;
		this.jobs = jobs;
		//this.logs = logs;
		this.batches = batches;
	}
	
	public Roll(Integer rollId2, Integer length2, Date creationDate, PaperType paper, Float hours2, String rollTag2,
			RollType type, RollStatus status2) {
		this.rollId = rollId2;
		this.length  = length2;
		this.setCreatedDate(creationDate);
		this.paperType = paper;
		this.hours  = hours2;
		this.rollTag = rollTag2;
		this.status = status2;
		this.rollType = type;
		
	}

	public enum copyStatuses {
		NOT_STARTED("NOT_STARTED"),
		IN_PROGRESS("IN_PROGRESS"),
		FINISHED("FINISHED"),
		ERROR("ERROR")
		;

		private String name;
		private copyStatuses(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}

	/**
	 * Accessor methods for rollId
	 *
	 * @return rollId  
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "Roll_Id", unique = true, nullable = false)
	public Integer getRollId() {
		return this.rollId;
	}

	/**
	 * @param rollId the rollId to set
	 */
	public void setRollId(Integer rollId) {
		this.rollId = rollId;
	}

	/**
	 * Accessor methods for machine
	 *
	 * @return machine  
	 

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Machine_Id")*/
	@Column(name = "Machine_Id", length = 25)
	public String getMachineId() {
		return this.machineId;
	}

	/**
	 * @param machine the machine to set
	 */
	public void setMachineId(String machine) {
		this.machineId = machine;
	}

	/**
	 * Accessor methods for rollNum
	 *
	 * @return rollNum  
	 */

	@Column(name = "Roll_Num", length = 15)
	public String getRollNum() {
		return this.rollNum;
	}

	/**
	 * @param rollNum the rollNum to set
	 */
	public void setRollNum(String rollNum) {
		this.rollNum = rollNum;
	}

	/**
	 * Accessor methods for rollTag
	 *
	 * @return rollTag  
	 */

	@Column(name = "Roll_Tag", length = 25)
	public String getRollTag() {
		return this.rollTag;
	}

	/**
	 * @param rollTag the rollTag to set
	 */
	public void setRollTag(String rollTag) {
		this.rollTag = rollTag;
	}

	/**
	 * Accessor methods for parentRoll
	 *
	 * @return parentRoll
	 */
	//@ManyToOne(fetch = FetchType.LAZY)
	//@JoinColumn(name = "Parent_Roll_Id")
	@Column(name = "Parent_Roll_Id")
	public Integer getParentRollId() {
		return this.parentRollId;
	}

	/**
	 * @param parentRoll the parentRoll to set
	 */
	public void setParentRollId(Integer parentRollId) {
		this.parentRollId = parentRollId;
	}

	/**
	 * Accessor methods for machineOrdering
	 *
	 * @return machineOrdering  
	 */

	@Column(name = "Machine_Ordering")
	public Integer getMachineOrdering() {
		return this.machineOrdering;
	}

	/**
	 * @param machineOrdering the machineOrdering to set
	 */
	public void setMachineOrdering(Integer machineOrdering) {
		this.machineOrdering = machineOrdering;
	}

	/**
	 * Accessor methods for rollType
	 *
	 * @return rollType  
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Roll_Type")
	public RollType getRollType() {
		return this.rollType;
	}

	/**
	 * @param rollType the rollType to set
	 */
	public void setRollType(RollType rollType) {
		this.rollType = rollType;
	}

	/**
	 * Accessor methods for length
	 *
	 * @return length  
	 */

	@Column(name = "Length")
	public Integer getLength() {
		return this.length;
	}

	/**
	 * @param length the length to set
	 */
	public void setLength(Integer length) {
		this.length = length;
	}

	/**
	 * Accessor methods for width
	 *
	 * @return width  
	 */

	@Column(name = "Width", precision = 8, scale = 3)
	public float getWidth() {
		return this.width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(float width) {
		this.width = width;
	}

	/**
	 * Accessor methods for weight
	 *
	 * @return weight  
	 */

	@Column(name = "Weight")
	public Integer getWeight() {
		return this.weight;
	}

	/**
	 * @param weight the weight to set
	 */
	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	/**
	 * Accessor methods for paperType
	 *
	 * @return paperType  
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Paper_Type")
	public PaperType getPaperType() {
		return this.paperType;
	}

	/**
	 * @param paperType the paperType to set
	 */
	public void setPaperType(PaperType paperType) {
		this.paperType = paperType;
	}

	/**
	 * Accessor methods for status
	 *
	 * @return status  
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Status")
	public RollStatus getStatus() {
		return this.status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(RollStatus status) {
		this.status = status;
	}

	/**
	 * Accessor methods for hours
	 *
	 * @return hours  
	 */

	@Column(name = "Hours", precision = 8, scale = 3)
	public float getHours() {
		return this.hours;
	}

	/**
	 * @param hours the hours to set
	 */
	public void setHours(float hours) {
		this.hours = hours;
	}

	/**
	 * Accessor methods for utilization
	 * @return utilization  
	 */
	@Column(name = "Utilization")
	public Integer getUtilization() {
		return this.utilization;
	}

	/**
	 * @param utilization the utilization to set
	 */
	public void setUtilization(Integer utilization) {
		this.utilization = utilization;
	}
	
	@Column(name = "CopyStatus", length = 55)
	public String getCopyStatus() {
		return copyStatus;
	}

	public void setCopyStatus(String copyStatus) {
		this.copyStatus = copyStatus;
	}

	/**
	 * Accessor methods for jobs
	 * @return jobs
	 */
	//@OneToMany(fetch = FetchType.EAGER, mappedBy = "rollId", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	//@OrderBy("jobPriority")
	//@OrderBy("rollOrdering")
	// TODO need better way of ordering the jobs on the roll based on the job priority, due date...
	// i think we should use rollOrdering, and so need to maintain this field so we can use it....
	//@SortComparator(JobsByRollOrderingComparator.class)
	//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	
	@Transient
	public SortedSet<Job> getJobs() {
		SortedSet<Job> result = new TreeSet<Job>(this.getAlljobs());
		if(RollType.types.PRODUCED.getName().equals(this.getRollType().getId())){
			for(Job j : getAlljobs()){
				if(!StationCategory.Categories.PLOWFOLDER.getName().equals(j.getStationId())){
					result.remove(j);
				}
			}
		}else{
			for(Job j : getAlljobs()){
				if(!StationCategory.Categories.PRESS.getName().equals(j.getStationId())){
					result.remove(j);
				}
			}
		}
		this.jobs = result;
		return result;
	}

	/**
	 * @param jobs the jobs to set
	 */
	public void setJobs(SortedSet<Job> jobs) {
		this.jobs = jobs;
	}
	
	/**
	 * @return the alljobs
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "rollId", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@SortComparator(JobsByRollOrderingComparator.class)
	public SortedSet<Job> getAlljobs() {
		return alljobs;
	}

	/**
	 * @param alljobs the alljobs to set
	 */
	public void setAlljobs(SortedSet<Job> alljobs) {
		this.alljobs = alljobs;
	}

	@Transient
	public Boolean getAllJobsComplete() {
		setAllJobsComplete(true);
		for(Job jobIter : getJobs()){
			if(!JobStatus.JobStatuses.COMPLETE.toString().equals(jobIter.getJobStatus().getId()) && 
					!JobStatus.JobStatuses.COMPLETE_PARTIAL.toString().equals(jobIter.getJobStatus().getId())){
				setAllJobsComplete(false);
				break;
			}
		}
		return allJobsComplete;
	}
	public void setAllJobsComplete(Boolean allJobsComplete) {
		this.allJobsComplete = allJobsComplete;
	}
	public Boolean getRollJobsStarted() {
		return rollJobsStarted;
	}

	public void setRollJobsStarted(Boolean rollJobsStarted) {
		this.rollJobsStarted = rollJobsStarted;
	}

	@Transient
	public Boolean isRollJobsStarted() {
		setRollJobsStarted(false);
		for(Job jobIter : getJobs()){
			if(jobIter.getQuantityProduced() > 0){
				setRollJobsStarted(true);
				break;
			}
		}
		return rollJobsStarted;
	}
	
	
	/**
	 * @return the allJobsRipped
	 */
	@Transient
	public Boolean getAllJobsRipped() {
		setAllJobsRipped(true);
		for(Job jobIter : getJobs()){
			//if(){
			//if the prod part has rip action done then continue else return false
				
			//}
		}
		return allJobsRipped;
	}

	/**
	 * @param allJobsRipped the allJobsRipped to set
	 */
	public void setAllJobsRipped(Boolean allJobsRipped) {
		this.allJobsRipped = allJobsRipped;
	}

	/**
	 * @return the producedLength this is mostly used for printed roll to say how much length of
	 * it was already processed by the hunkeler station
	 */
	@Transient
	public Integer getProducedLength() {
		this.producedLength = 0;
		Float producedJobsHours = (float) 0;
		for(Job j : this.getJobs()){
			if(j.getQuantityProduced() > 0){
				producedJobsHours = producedJobsHours + (j.getHours() * j.getQuantityProduced() / j.getQuantityNeeded());
			}
		}
		if(producedJobsHours > 0){
			producedLength = (int) (producedJobsHours * this.getLength() / this.getHours());
		}
		return producedLength;
	}

	/**
	 * @param producedLength the producedLength to set
	 */
	public void setProducedLength(Integer producedLength) {
		this.producedLength = producedLength;
	}

	/**
	 * @return the machineTypeId needed when on the plow folder station: by default it is Plow Folder,
	 * but if any of the jobs on the roll has type Pop line or Fly folder then the whole roll
	 * (and so all jobs on the roll) should go on that type of machine.
	 */
	@Transient
	public String getMachineTypeId() {
		if(this.getRollTag() != null && !this.getRollTag().isEmpty()){
			return this.getRollTag();
		}
		if(!RollType.types.PRODUCED.getName().equals(this.getRollType().getId())){
			return "";
		}
		this.machineTypeId = MachineType.types.PLOWFOLDER.toString();
		for(Job jobIter : getJobs()){
			if(jobIter.getJobType() != null){
				if(jobIter.getJobType().getId().contains(MachineType.types.POPLINE.toString())){
					this.machineTypeId = MachineType.types.POPLINE.toString();
					break;
				}
				if(jobIter.getJobType().getId().contains(MachineType.types.FLYFOLDER.toString())){
					this.machineTypeId = MachineType.types.FLYFOLDER.toString();
					break;
				}
			}
		}
		return this.machineTypeId;
	}

	/**
	 * @param machineTypeId the machineTypeId to set
	 */
	public void setMachineTypeId(String machineTypeId) {
		this.machineTypeId = machineTypeId;
	}

	/**
	 * @return the impositionTypeId
	 */
	@Transient
	public String getImpositionTypeId() {
		Integer pressJobsCount = 0, twoUpsCount = 0, threeUpsCount = 0, fourUpsCount = 0;
		for(Job j : this.getAlljobs()){
			if(StationCategory.Categories.PRESS.getName().equals(j.getStationId())){
				pressJobsCount++;
				if(j.getJobType() != null && JobType.JobTypes.PRINTING_2UP.getName().equals(j.getJobType().getId())){
					twoUpsCount++;
				}
				if(j.getJobType() != null && JobType.JobTypes.PRINTING_3UP.getName().equals(j.getJobType().getId())){
					threeUpsCount++;
				}
				if(j.getJobType() != null && JobType.JobTypes.PRINTING_4UP.getName().equals(j.getJobType().getId())){
					fourUpsCount++;
				}
			}
		}
		if(pressJobsCount > 0){
			if(pressJobsCount == twoUpsCount){
				impositionTypeId = "2U";
			}
			if(pressJobsCount == threeUpsCount){
				impositionTypeId = "3U";
			}
			if(pressJobsCount == fourUpsCount){
				impositionTypeId = "4U";
			}
		}
		return impositionTypeId;
	}

	/**
	 * @param impositionTypeId the impositionTypeId to set
	 */
	public void setImpositionTypeId(String impositionTypeId) {
		this.impositionTypeId = impositionTypeId;
	}

	/**
	 * retrieves the most urgent due date of all jobs assigned to the roll
	 */
	@Transient
	public Date getDueDate(){
		for(Job j : this.getAlljobs()){
			if(this.dueDate == null || this.dueDate.after(j.getDueDate())){
				this.dueDate = j.getDueDate();
			}
		}
		return this.dueDate;
	}
	
	public void setDueDate(Date dueDate){
		this.dueDate = dueDate;
	}

	/**
	 * retrieves the most urgent priority of all jobs assigned to the roll
	 */
	@Transient
	public String getPriority(){
		this.priority = Priority.Priorities.NORMAL.getName();
		for(Job j : this.getAlljobs()){
			if(Priority.Priorities.HIGH_SS.getName().equals(j.getJobPriority().getId())){
				this.priority = j.getJobPriority().getId();
				return this.priority;
			}
		}
		for(Job j : this.getAlljobs()){
			if(Priority.Priorities.HIGH_S.getName().equals(j.getJobPriority().getId())){
				this.priority = j.getJobPriority().getId();
				return this.priority;
			}
		}		
		for(Job j : this.getAlljobs()){
			if(Priority.Priorities.HIGH.getName().equals(j.getJobPriority().getId())){
				this.priority = j.getJobPriority().getId();
				return this.priority;
			}
		}
		
		return this.priority;
	}
	
	public void setPriority(String priority){
		this.priority = priority;
	}

	/**
	 * retrieves the colors based on the jobs assigned to the roll, or the machine the roll is set on
	 */
	@Transient
	public String getColors(){
		if(this.machineId != null){
			if(this.machineId.contains(MachineType.types._4C.getName())){
				this.colors = Part.PartColors._4C.getName();
			}else if(this.machineId.contains(MachineType.types._1C.getName())){
				this.colors = Part.PartColors._1C.getName();
			}
		}
		if(this.colors == null && getJobs() != null && !getJobs().isEmpty()){
			this.colors = Part.PartColors._1C.getName();
			for(Job job : getJobs()){
				if(Part.PartColors._4C.getName().equals(job.getPartColor())){
					this.colors = Part.PartColors._4C.getName();
					break;
				}
			}
		}
		return this.colors;
	}
	
	public void setColors(String colors){
		this.colors = colors;
	}
	
	/**
	 * @return the productionMode
	 */
	@Transient
	public String getProductionMode() {
		if(!getJobs().isEmpty()){
			Job j = getJobs().iterator().next();
			this.productionMode = j.getProductionMode();
		}
		return StringUtil.toCamelCase(productionMode);
	}
	
	/**
	 * Re-assigns new ordering for the jobs in the roll; this is the ordering needed for printing/display on press:
	 * if standard mode then inverse orders; if Epac mode then also group by sheet length
	 * reorder only once as the ordering gets persisted to the database
	 */
	@Transient
	public void reorderJobsForPrinting(String mode) {
		int thresholdSM = 3000;
		int thresholdEM = 4000;
		Set<Job> rollJobs = this.getJobs();
		int total = rollJobs.size();
		if(total == 0){
			return;
		}
		Iterator<Job> iterator = rollJobs.iterator();
		// First inverse ordering for both SM and EM
		int i = total + ("EM".equals(mode) ? thresholdEM : thresholdSM);
	    while(iterator.hasNext()) {
	    	Job j = iterator.next();
	    	//see if the reordering happened already and so do not reorder again
	    	if("SM".equals(mode) && j.getRollOrdering() > thresholdSM && j.getRollOrdering() < thresholdEM){
				return;
			}
			if("EM".equals(mode) && j.getRollOrdering() > thresholdEM){
				return;
			}
	    	j.setRollOrdering(i);
	    	i--;
	    }
		if("EM".equals(mode)){
			// Add the grouping
			HashMap<String, List<Job>> hashMap = new HashMap<String, List<Job>>();
			List<String> orderedSheets = new ArrayList<String>();
			for(Job job : rollJobs){
				if (!hashMap.containsKey(job.getImpPartHeight())) {
					orderedSheets.add(job.getImpPartHeight());
				    List<Job> list = new ArrayList<Job>();
				    list.add(job);
				    hashMap.put(job.getImpPartHeight(), list);
				} else {
				    hashMap.get(job.getImpPartHeight()).add(job);
				}
			}
			int j = 1 +  ("EM".equals(mode) ? thresholdEM : thresholdSM);
			for(String sheet : orderedSheets){
				for(Job job : hashMap.get(sheet)){
					job.setRollOrdering(j);
					j++;
				}
			}
		}
	}

	/**
	 * @param productionMode the productionMode to set
	 */
	public void setProductionMode(String productionMode) {
		this.productionMode = productionMode;
	}
	
	/**
	 * @return the creationDate
	 */
	@Transient
	public Date getCreationDate() {
		creationDate = this.getCreatedDate();
		return creationDate;
	}

	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * Accessor methods for logs
	 * @return logs  
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "rollId")
	public Set<Log> getLogs() {
		return this.logs;
	}*/

	/**
	 * @return the calculatedWaste
	 */
	@Column(name = "Calculated_Waste", precision = 12, scale = 5)
	public Float getCalculatedWaste() {
		return calculatedWaste;
	}

	/**
	 * @param calculatedWaste the calculatedWaste to set
	 */
	public void setCalculatedWaste(Float calculatedWaste) {
		this.calculatedWaste = calculatedWaste;
	}

	/**
	 * @param logs the logs to set
	 
	public void setLogs(Set<Log> logs) {
		this.logs = logs;
	}*/

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getRollId() == null) ? 0 : getRollId().hashCode());
		result = prime * result + ((getRollNum() == null) ? 0 : getRollNum().hashCode());
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
		if (!(obj instanceof Roll)) {
			return false;
		}
		Roll other = (Roll) obj;
		if (getRollId() == null) {
			if (other.getRollId() != null) {
				return false;
			}
		} else if (!getRollId().equals(other.getRollId())) {
			return false;
		}
		if (getRollNum() == null) {
			if (other.getRollNum() != null) {
				return false;
			}
		} else if (!getRollNum().equals(other.getRollNum())) {
			return false;
		}
		return true;
	}
	
}
