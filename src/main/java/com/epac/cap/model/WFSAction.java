package com.epac.cap.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.epac.cap.common.AuditableBean;

@Entity
@Table(name = "WFS_Action")
public class WFSAction extends AuditableBean implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer actionId;
	private String name;
	private String actionType;
	
	/**
	 * @return the actionId
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "idaction", unique = true, nullable = false, length = 25)
	public Integer getActionId() {
		return actionId;
	}
	
	/**
	 * @param actionId the actionId to set
	 */
	private void setActionId(Integer actionId) {
		this.actionId = actionId;
	}
	
	/**
	 * @return the name
	 */
	@Column(name = "name", length = 55)
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the type
	 */
	@Column(name = "type", length = 55)
	public String getActionType() {
		return actionType;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setActionType(String type) {
		this.actionType = type;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getActionId() == null) ? 0 : getActionId().hashCode());
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
		if (!(obj instanceof WFSAction)) {
			return false;
		}
		WFSAction other = (WFSAction) obj;
		if (getActionId() == null) {
			if (other.getActionId() != null) {
				return false;
			}
		} else if (!getActionId().equals(other.getActionId())) {
			return false;
		}
		return true;
	}

}
