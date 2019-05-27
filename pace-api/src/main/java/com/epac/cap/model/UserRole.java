package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * UserRole class representing a user role association
 */
@Entity
@Table(name = "user_Role")
public class UserRole implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8842328790845894672L;
	private UserRoleId id;
	//@JsonIgnore
	//private User user;
	//@JsonIgnore
	//private Role role;

	/**
	 * Default constructor
	 */
	public UserRole() {
		if(this.id == null){
			this.id = new UserRoleId();
		}
	}
	
	/**
	 * Constructor which sets the identity property
	 */
	public UserRole(UserRoleId id) {
		this.id = id;
		if(this.id == null){
			this.id = new UserRoleId();
		}	
	}
	
	public UserRole(String userId, String roleId){
		this.id = new UserRoleId(userId, roleId);
	}

	/**
	 * Constructor which sets the two parties
	 
	public UserRole(User user, Role role) {
		this(null, user, role);
	}*/
	
	/**
	 * Constructor which sets the identity property
	 
	public UserRole(UserRoleId id, User user, Role role) {
		this.id = id;
		if(this.id == null){
			this.id = new UserRoleId();
		}
		this.user = user;
		if(user != null && user.getUserId() != null){
			this.id.setUserId(user.getUserId());
		}
		
		this.role = role;
		if(role != null && role.getRoleId() != null){
			this.id.setRoleId(role.getRoleId());
		}
	}*/

	/**
	 * Accessor methods for id
	 *
	 * @return id  
	 */
	@EmbeddedId

	@AttributeOverrides({
			@AttributeOverride(name = "userId", column = @Column(name = "User_Id", nullable = false, length = 35)),
			@AttributeOverride(name = "roleId", column = @Column(name = "Role_Id", nullable = false, length = 50)) })
	public UserRoleId getId() {
		return this.id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(UserRoleId id) {
		this.id = id;
	}

	/**
	 * @return the user
	 
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "User_Id", nullable = false, insertable = false, updatable = false)
//	@XmlTransient
	public User getUser() {
		return user;
	}*/

	/**
	 * @param user the user to set
	 
	public void setUser(User user) {
		this.user = user;
	}*/

	/**
	 * @return the role
	 
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Role_Id", nullable = false, insertable = false, updatable = false)
	//@XmlTransient
	public Role getRole() {
		return role;
	}*/

	/**
	 * @param role the role to set
	 
	public void setRole(Role role) {
		this.role = role;
	}*/

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
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
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof UserRole)) {
			return false;
		}
		UserRole other = (UserRole) obj;
		if (getId() == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (!getId().equals(other.getId())) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return  super.toString() ;
	}
	
	
}
