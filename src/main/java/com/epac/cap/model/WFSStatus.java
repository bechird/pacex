package com.epac.cap.model;

//@Entity
//@Table(name = "WFS_Status")
public class WFSStatus { //extends AuditableBean implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1035816242612776718L;
	
	public enum ProgressStatus { 
		PENDING("PENDING"),
		INPROGRESS("INPROGRESS"),
		DONE("DONE"),
		ERROR("ERROR");

		private String name;
		private ProgressStatus(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}
	
	private long id;
	private String description;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	

}
