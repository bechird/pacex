package com.epac.cap.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.epac.cap.common.AuditableBean;

/**
 * Log class that represents the logs for the production cycle
 */
@Entity
@Table(name = "log")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="com.epac.cap")
public class Log extends AuditableBean implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3210537492448246911L;
	private Integer logId;
	//private Machine machine;
	private String machineId;
	private Integer rollId;
	private Integer sectionId;
	private String event;
	private LogResult logResult;
	private LogCause logCause;
	private Integer currentJobId;
	private Integer rollLength;
	private Date startTime;
	private Date finishTime;
	private long counterFeet;
	private String notes;
	private List<String> completedJobQtys = new ArrayList<String>();
	
	/**
	 * Default constructor
	 */
	public Log() {
	}

	/**
	 * Constructor which sets all of the properties
	 */
	public Log(Machine machine, Roll roll, Integer rollLength, Date startTime, Date finishTime, long counterFeet) {
		//this.machine = machine;
		//this.roll = roll;
		this.rollLength = rollLength;
		this.startTime = startTime;
		this.finishTime = finishTime;
		this.counterFeet = counterFeet;
	}
	
	public enum LogEvent { 
		COMPLETE("COMPLETE"),
		PAUSE("PAUSE"),
		STOP("STOP"),
		START("START"),
		RESUME("RESUME"),
		SERVICE("SERVICE"),
		ONOFF("ONOFF");

		private String name;
		private LogEvent(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}

	/**
	 * Accessor methods for logId
	 *
	 * @return logId  
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "Log_Id", unique = true, nullable = false)
	public Integer getLogId() {
		return this.logId;
	}

	/**
	 * @param logId the logId to set
	 */
	public void setLogId(Integer logId) {
		this.logId = logId;
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
	 * Accessor methods for roll
	 *
	 * @return roll  
	 

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Roll_Id")*/
	@Column(name = "Roll_Id")
	public Integer getRollId() {
		return this.rollId;
	}

	/**
	 * @param roll the roll to set
	 */
	public void setRollId(Integer roll) {
		this.rollId = roll;
	}

	/**
	 * Accessor methods for event
	 *
	 * @return event  
	 */

	@Column(name = "Event", length = 55)
	public String getEvent() {
		return this.event;
	}

	/**
	 * @param event the event to set
	 */
	public void setEvent(String event) {
		this.event = event;
	}

	/**
	 * Accessor methods for result
	 *
	 * @return result  
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Result")
	public LogResult getLogResult() {
		return this.logResult;
	}

	/**
	 * @param result the result to set
	 */
	public void setLogResult(LogResult result) {
		this.logResult = result;
	}

	/**
	 * Accessor methods for cause
	 *
	 * @return cause  
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Cause")
	public LogCause getLogCause() {
		return this.logCause;
	}

	/**
	 * @param cause the cause to set
	 */
	public void setLogCause(LogCause cause) {
		this.logCause = cause;
	}

	/**
	 * Accessor methods for currentJob
	 *
	 * @return currentJob  
	 
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Current_Job_Id")*/
	@Column(name = "Current_Job_Id")
	public Integer getCurrentJobId() {
		return this.currentJobId;
	}

	/**
	 * @param currentJob the currentJob to set
	 */
	public void setCurrentJobId(Integer currentJob) {
		this.currentJobId = currentJob;
	}

	/**
	 * Accessor methods for rollLength
	 *
	 * @return rollLength  
	 */

	@Column(name = "Roll_Length")
	public Integer getRollLength() {
		return this.rollLength;
	}

	/**
	 * @param rollLength the rollLength to set
	 */
	public void setRollLength(Integer rollLength) {
		this.rollLength = rollLength;
	}

	/**
	 * Accessor methods for startTime
	 *
	 * @return startTime  
	 */

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "Start_Time", length = 19)
	public Date getStartTime() {
		return this.startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * Accessor methods for finishTime
	 *
	 * @return finishTime  
	 */

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "Finish_Time", length = 19)
	public Date getFinishTime() {
		return this.finishTime;
	}

	/**
	 * @param finishTime the finishTime to set
	 */
	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}

	/**
	 * Accessor methods for counterFeet
	 *
	 * @return counterFeet  
	 */

	@Column(name = "Counter_Feet", length = 500)
	public long getCounterFeet() {
		return this.counterFeet;
	}

	/**
	 * @return the notes
	 */
	@Column(name = "Notes", length = 100)
	public String getNotes() {
		return notes;
	}

	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * @param counterFeet the counterFeet to set
	 */
	public void setCounterFeet(long counterFeet) {
		this.counterFeet = counterFeet;
	}

	/**
	 * @return the completedJobQtys
	 */
	@Transient
	public List<String> getCompletedJobQtys() {
		return completedJobQtys;
	}

	/**
	 * @param completedJobQtys the completedJobQtys to set
	 */
	public void setCompletedJobQtys(List<String> completedJobQtys) {
		this.completedJobQtys = completedJobQtys;
	}
	
	@Column(name = "Section_Id")
	public Integer getSectionId() {
		return sectionId;
	}

	public void setSectionId(Integer sectionId) {
		this.sectionId = sectionId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getLogId() == null) ? 0 : getLogId().hashCode());
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
		if (!(obj instanceof Log)) {
			return false;
		}
		Log other = (Log) obj;
		if (getLogId() == null) {
			if (other.getLogId() != null) {
				return false;
			}
		} else if (!getLogId().equals(other.getLogId())) {
			return false;
		}
		return true;
	}
	
}
