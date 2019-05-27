package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Preference a class representing a preference in the system
 */
@Entity
@Table(name = "preference")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Preference_Id", unique = true, nullable = false, length = 100), name = "id"),
    @AttributeOverride(column = @Column(name = "Value", nullable = false, length = 300), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 300), name = "description")})
public class Preference extends LookupItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4032045499400126261L;
	
	private String groupingValue;
	private String prefSubject;
	
	private String partNum;
	private String clientId;
	
	/**
	 * 
	 */
	public Preference() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 */
	public Preference(String id, String name) {
		super(id, name);
	}
	
	public Preference(LookupItem item2) {
		super(item2);
	}

	/**
	 * @return the grouping
	 */
	@Column(name = "Group_Id", length = 45)
	public String getGroupingValue() {
		return groupingValue;
	}

	/**
	 * @param grouping the grouping to set
	 */
	public void setGroupingValue(String grouping) {
		this.groupingValue = grouping;
	}

	/**
	 * @return the partNum
	 */
	@Column(name = "Part_Num", length = 25)
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
	 * @return the clientId
	 */
	@Column(name = "Client_Id", length = 35)
	public String getClientId() {
		return clientId;
	}

	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the prefSubject
	 */
	@Column(name = "Pref_Subject", length = 45)
	public String getPrefSubject() {
		return prefSubject;
	}

	/**
	 * @param prefSubject the prefSubject to set
	 */
	public void setPrefSubject(String prefSubject) {
		this.prefSubject = prefSubject;
	}
	
}
