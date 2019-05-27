package com.epac.cap.model;

//@Entity
//@Table(name = "WFS_Parameter")
public class WFSParameter {//extends AuditableBean implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7805561622655896447L;
	private long id;
	private String name;
	private String paramType;
	private String pattern;
	
	private Integer actionId;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParamType() {
		return paramType;
	}

	public void setParamType(String paramType) {
		this.paramType = paramType;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * @return the actionId
	 */
	public Integer getActionId() {
		return actionId;
	}

	/**
	 * @param actionId the actionId to set
	 */
	public void setActionId(Integer actionId) {
		this.actionId = actionId;
	}
	
}
