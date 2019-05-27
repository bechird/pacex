package com.epac.cap.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.epac.cap.common.AuditableBean;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Role represents a role in the system
 */
@Entity
@Table(name = "role")
//@JsonIgnoreProperties(value = { "userRoles" })
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="com.epac.cap")
public class Role extends AuditableBean implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2169922101642154030L;
	private String roleId;
	private String roleName;
	private String roleDescription;
	@JsonIgnore
	private Set<UserRole> userRoles = new HashSet<UserRole>(0);
	private Set<String> users = new HashSet<String>(0);

	/**
	 * Default constructor
	 */
	public Role() {
	}

	/**
	 * Constructor which sets the identity property
	 */
	public Role(String roleId, String roleName) {
		this.roleId = roleId;
		this.roleName = roleName;
	}

	/**
	 * Constructor which sets all of the properties
	 */
	public Role(String roleId, String roleName, String roleDescription) {
		this.roleId = roleId;
		this.roleName = roleName;
		this.roleDescription = roleDescription;
	}

	/**
	 * Accessor methods for roleId
	 *
	 * @return roleId
	 */
	@Id
	@Column(name = "Role_Id", unique = true, nullable = false, length = 50)
	public String getRoleId() {
		return this.roleId;
	}

	/**
	 * @param roleId
	 *            the roleId to set
	 */
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	/**
	 * Accessor methods for roleName
	 *
	 * @return roleName
	 */

	@Column(name = "Role_Name", nullable = false, length = 100)
	public String getRoleName() {
		return this.roleName;
	}

	/**
	 * @param roleName
	 *            the roleName to set
	 */
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	/**
	 * Accessor methods for roleDescription
	 *
	 * @return roleDescription
	 */

	@Column(name = "Role_Description", length = 100)
	public String getRoleDescription() {
		return this.roleDescription;
	}

	/**
	 * @param roleDescription
	 *            the roleDescription to set
	 */
	public void setRoleDescription(String roleDescription) {
		this.roleDescription = roleDescription;
	}

	/**
	 * @return the userRoles
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "id.roleId", cascade = CascadeType.ALL, orphanRemoval = true)
	//@JsonIgnore
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="com.epac.cap")
	public Set<UserRole> getUserRoles() {
		return userRoles;
	}

	/**
	 * @param userRoles
	 *            the userRoles to set
	 */
	public void setUserRoles(Set<UserRole> userRoles) {
		this.userRoles = userRoles;
	}

	/**
	 * Accessor methods for users
	 */
	@Transient
	//@JsonIgnore
	public Set<String> getUsers() {
		users = new HashSet<String>();
		for (UserRole ur : this.getUserRoles()) {
			users.add(ur.getId().getUserId());
		}
		return users;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getRoleId() == null) ? 0 : getRoleId().hashCode());
		result = prime * result + ((getRoleName() == null) ? 0 : getRoleName().hashCode());
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
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof Role)) {
			return false;
		}
		Role other = (Role) obj;
		if (getRoleId() == null) {
			if (other.getRoleId() != null) {
				return false;
			}
		} else if (!getRoleId().equals(other.getRoleId())) {
			return false;
		}
		if (getRoleName() == null) {
			if (other.getRoleName() != null) {
				return false;
			}
		} else if (!getRoleName().equals(other.getRoleName())) {
			return false;
		}
		return true;
	}

}
