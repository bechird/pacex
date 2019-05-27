package com.epac.cap.common;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author walid A class that represents an auditable object which has the
 *         common properties used to track who added/modified the record and
 *         when...
 *
 */
@MappedSuperclass
public class AuditableBean {

	private String creatorId = null;
	@JsonIgnore
	private String lastUpdateId = null;
	@JsonIgnore
	private Date createdDate = null;
	@JsonIgnore
	private Date lastUpdateDate = null;

	private Date lastModifiedDate;
	private String lastModifiedByUserName;

	/**
	 * @return the createByUser
	 */
	/*
	 * @ManyToOne(fetch = FetchType.EAGER, optional = true)
	 * 
	 * @JoinColumn(name = "Creator_id", referencedColumnName = "User_Id",
	 * nullable = true, insertable = false, updatable = false) public User
	 * getCreatedByUser() { return createdByUser; }
	 * 
	 *//**
		 * @param createByUser
		 *            the createByUser to set
		 *//*
		 * public void setCreatedByUser(User createByUser) { createdByUser =
		 * createByUser; }
		 */

	/**
	 * @return the user id of the one who created this record
	 */
	@Column(name = "Creator_id", updatable = false, length = 55)
	public String getCreatorId() {
		return creatorId;
	}

	/**
	 * @param creatorId
	 *            the user id of the one who created this record
	 */
	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	/**
	 * @param modByUser
	 *            the modByUser to set
	 */
	/*
	 * public void setUpdatedByUser(User modByUser) { updatedByUser = modByUser;
	 * }
	 * 
	 *//**
		 * @return the modByUser
		 *//*
		 * @ManyToOne(fetch = FetchType.EAGER, optional = true)
		 * 
		 * @JoinColumn(name = "Modifier_Id", referencedColumnName = "User_Id",
		 * nullable = true, insertable = false, updatable = false) public User
		 * getUpdatedByUser() { return updatedByUser; }
		 */

	/**
	 * @return the user id of the one who last updated this record
	 */
	@Column(name = "Modifier_Id", insertable = false, length = 55)
	public String getLastUpdateId() {
		return lastUpdateId;
	}

	/**
	 * @param lastUpdateId
	 *            the user id of the one who last updated this record
	 */
	public void setLastUpdateId(String lastUpdateId) {
		this.lastUpdateId = lastUpdateId;
	}

	/**
	 * @return the date this record was created
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "Creation_Date", updatable = false)
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * @param createdDate
	 *            the date this record was created
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * @return the date this record was last updated
	 */
	@Column(name = "Modification_Date", insertable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	/**
	 * @param lastUpdateDate
	 *            the date this record was last updated
	 */
	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	/**
	 * Convenience method to get the last modified date. Will return the last
	 * updated date if set or the created date otherwise.
	 * 
	 * @return the last updated date if set or the created date otherwise.
	 */
	@Transient
	public Date getLastModifiedDate() {
		Date lastModifiedDate = null;
		if (getLastUpdateDate() != null) {
			lastModifiedDate = getLastUpdateDate();
		} else {
			lastModifiedDate = getCreatedDate();
		}
		this.lastModifiedDate = lastModifiedDate;
		return lastModifiedDate;
	}

	/**
	 * Convenience method to get the last modified by user. Will return the last
	 * updated by user if set or the created by user otherwise.
	 * 
	 * @return the the last updated by user if set or the created by user
	 *         otherwise.
	 */
	/*
	 * @Transient public User getLastModifiedByUser() { try { User user =
	 * this.getLastModifiedByUser(); if (user != null) { return user; } else {
	 * return getCreatedByUser(); } } catch (Exception e) { // hibernate throws
	 * an error if the referenced entity can't be found! return null; } }
	 */

	/**
	 * Convenience method to get the last modified by user name. Will return the
	 * last updated by user if set or the created by user otherwise.
	 * 
	 * @return the the last updated by user if set or the created by user
	 *         otherwise.
	 */
	/*
	 * @Transient public String getLastModifiedByUserName() { try { String
	 * result = ""; User user = getLastModifiedByUser(); if (user != null &&
	 * user.getLastFirstMIName() != null) { result = user.getLastFirstMIName();
	 * } return result; } catch (Exception e) { // hibernate throws an error if
	 * the referenced entity can't be found! return null; } }
	 */

	@Transient
	public String getLastModifiedByUserName() {
		try {
			String result = this.getLastUpdateId();
			if (!(result != null && !result.isEmpty())) {
				result = this.getCreatorId();
			}
			this.lastModifiedByUserName = result;
			return result;
		} catch (Exception e) {
			// hibernate throws an error if the referenced entity can't be
			// found!
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdDate == null) ? 0 : createdDate.hashCode());
		result = prime * result + ((creatorId == null) ? 0 : creatorId.hashCode());
		result = prime * result + ((lastUpdateDate == null) ? 0 : lastUpdateDate.hashCode());
		result = prime * result + ((lastUpdateId == null) ? 0 : lastUpdateId.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AuditableBean)) {
			return false;
		}
		AuditableBean other = (AuditableBean) obj;
		if (createdDate == null) {
			if (other.createdDate != null) {
				return false;
			}
		} else if (!createdDate.equals(other.createdDate)) {
			return false;
		}
		if (creatorId == null) {
			if (other.creatorId != null) {
				return false;
			}
		} else if (!creatorId.equals(other.creatorId)) {
			return false;
		}
		if (lastUpdateDate == null) {
			if (other.lastUpdateDate != null) {
				return false;
			}
		} else if (!lastUpdateDate.equals(other.lastUpdateDate)) {
			return false;
		}
		if (lastUpdateId == null) {
			if (other.lastUpdateId != null) {
				return false;
			}
		} else if (!lastUpdateId.equals(other.lastUpdateId)) {
			return false;
		}
		return true;
	}

}
