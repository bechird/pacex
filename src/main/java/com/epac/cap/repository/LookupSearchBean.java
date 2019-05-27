package com.epac.cap.repository;

import com.epac.cap.common.AuditableSearchBean;

public class LookupSearchBean extends AuditableSearchBean{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1129891554411050927L;
	private String id;
	private String idDiff;
	private String idPrefix;
	private String name;
	private String namePrefix;
	private String nameExact;
	private String description;
	private String prefGroup;
	private String prefSubject;
	private String partNum;
	private String clientId;
	private String partNumIsNull;
	private String clientIdIsNull;
	
	public LookupSearchBean() {
		setOrderBy("name");
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the idDiff
	 */
	public String getIdDiff() {
		return idDiff;
	}

	/**
	 * @param idDiff the idDiff to set
	 */
	public void setIdDiff(String idDiff) {
		this.idDiff = idDiff;
	}

	/**
	 * @return the idPrefix
	 */
	public String getIdPrefix() {
		return idPrefix;
	}

	/**
	 * @param idPrefix the idPrefix to set
	 */
	public void setIdPrefix(String idPrefix) {
		this.idPrefix = idPrefix;
	}

	/**
	 * @return the name
	 */
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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the prefSubject
	 */
	public String getPrefSubject() {
		return prefSubject;
	}

	/**
	 * @param prefSubject the prefSubject to set
	 */
	public void setPrefSubject(String prefSubject) {
		this.prefSubject = prefSubject;
	}

	/**
	 * @return the namePrefix
	 */
	public String getNamePrefix() {
		return namePrefix;
	}

	/**
	 * @param namePrefix the namePrefix to set
	 */
	public void setNamePrefix(String namePrefix) {
		this.namePrefix = namePrefix;
	}

	/**
	 * @return the prefGroup
	 */
	public String getPrefGroup() {
		return prefGroup;
	}

	/**
	 * @param prefGroup the prefGroup to set
	 */
	public void setPrefGroup(String prefGroup) {
		this.prefGroup = prefGroup;
	}

	/**
	 * @return the partNum
	 */
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
	 * @return the partNumIsNull
	 */
	public String getPartNumIsNull() {
		return partNumIsNull;
	}

	/**
	 * @param partNumIsNull the partNumIsNull to set
	 */
	public void setPartNumIsNull(String partNumIsNull) {
		this.partNumIsNull = partNumIsNull;
	}

	/**
	 * @return the clientIdIsNull
	 */
	public String getClientIdIsNull() {
		return clientIdIsNull;
	}

	/**
	 * @param clientIdIsNull the clientIdIsNull to set
	 */
	public void setClientIdIsNull(String clientIdIsNull) {
		this.clientIdIsNull = clientIdIsNull;
	}
	
}
