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

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.epac.cap.common.AuditableBean;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * User class that represents a user of the system
 */
@Entity
@Table(name = "user")
public class User extends AuditableBean implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1943682400808191545L;
	private String userId;
	private String firstName;
	private String lastName;
	private String firstLastName;
	private String nameInitials;
	private String email;
	private String phoneNum;
	private String loginName;
	private String loginPassword;
	private Boolean activeFlag;
	
	private String language;
	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@JsonIgnore
	private Set<UserRole> userRoles = new HashSet<UserRole>(0);
	private Set<String> roles = new HashSet<String>();
	
	/**
	 * Default constructor
	 */
	public User() {
	}

	/**
	 * Constructor which sets the identity property
	 */
	public User(String userId, String firstName, String lastName) {
		this.userId = userId;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	/**
	 * Constructor which sets all of the properties
	 */
	public User(String userId, String firstName, String lastName, String email, String phoneNum, String loginName,
			String loginPassword) {
		this.userId = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.phoneNum = phoneNum;
		this.loginName = loginName;
		this.loginPassword = loginPassword;
	}

	/**
	 * Accessor methods for userId
	 *
	 * @return userId  
	 */
	@Id
	@Column(name = "User_Id", unique = true, nullable = false, length = 35)
	public String getUserId() {
		return this.userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * Accessor methods for firstName
	 *
	 * @return firstName  
	 */

	@Column(name = "First_Name", nullable = false, length = 50)
	public String getFirstName() {
		return this.firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Accessor methods for lastName
	 *
	 * @return lastName  
	 */

	@Column(name = "Last_Name", nullable = false, length = 50)
	public String getLastName() {
		return this.lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	 /**
	   * Returns the user's name in the form of (last, first middle initial.)
	   * 
	   * @return the user's name in the form of (last, first middle initial.)
	   
	  @Transient
	  @JsonIgnore
	  public String getLastFirstMIName() {
	    StringBuilder name = new StringBuilder();
	    name.append(getLastName());
	    if (!StringUtils.isBlank(getFirstName())) {
	      name.append(", ").append(getFirstName());
	    }

	    return name.toString();
	  }*/
	  
	  /**
	   * Returns the user's name in the form of (first last name.)
	   * 
	   */
	  @Transient
	  public String getFirstLastName() {
	    StringBuilder name = new StringBuilder();
	    name.append(getFirstName());
	    if (!StringUtils.isBlank(getLastName())) {
	      name.append(" ").append(getLastName());
	    }
	    firstLastName = name.toString();
	    return firstLastName;
	  }
	  
	  /**
	   * Returns the user's name initials
	   * 
	   */
	  @Transient
	  public String getNameInitials() {
	    StringBuilder name = new StringBuilder();
	    name.append(getFirstName().substring(0, 1).toUpperCase());
	    if (!StringUtils.isBlank(getLastName())) {
	      name.append(getLastName().substring(0, 1).toUpperCase());
	    }
	    nameInitials = name.toString();
	    return nameInitials;
	  }
	  
	/**
	 * Accessor methods for email
	 *
	 * @return email  
	 */

	@Column(name = "Email", length = 70)
	public String getEmail() {
		return this.email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Accessor methods for phoneNum
	 *
	 * @return phoneNum  
	 */

	@Column(name = "Phone_Num", length = 15)
	public String getPhoneNum() {
		return this.phoneNum;
	}

	/**
	 * @param phoneNum the phoneNum to set
	 */
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	/**
	 * Accessor methods for loginName
	 *
	 * @return loginName  
	 */

	@Column(name = "Login_Name", length = 50)
	public String getLoginName() {
		return this.loginName;
	}

	/**
	 * @param loginName the loginName to set
	 */
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	/**
	 * Accessor methods for loginPassword
	 *
	 * @return loginPassword  
	 */

	@Column(name = "Login_Password", length = 255)
	public String getLoginPassword() {
		return this.loginPassword;
	}

	/**
	 * @param loginPassword the loginPassword to set
	 */
	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}

	/**
	 * Accessor methods for active flag
	 *
	 * @return activeFlag  
	 */
	@Column(name = "Active_Flag")
	public Boolean getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(Boolean activeFlag) {
		this.activeFlag = activeFlag;
	}
	
	/**
	 * @return the userRoles
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "id.userId", cascade = CascadeType.ALL, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="com.epac.cap")
	public Set<UserRole> getUserRoles() {
		return userRoles;
	}

	/**
	 * @param userRoles the userRoles to set
	 */
	public void setUserRoles(Set<UserRole> userRoles) {
		this.userRoles = userRoles;
	}
	
	/**
	  * Accessor methods for roles
	  */ 
	  @Transient
	  public Set<String> getRoles(){
		  this.roles.clear();
		  for(UserRole ur : this.getUserRoles()){
			  this.roles.add(ur.getId().getRoleId());
		  }
		  return this.roles;
	  }
	  
	  /**
	  * Accessor methods for roles
	  */ 
	  @Transient
	  public Set<String> getRolesOrigin(){
		  return this.roles;
	  }
		  
	  /**
	  * Accessor methods for roles string
	  */ 
	  @Transient
	  @JsonIgnore
	  public String getRolesString(){
		  StringBuilder result = new StringBuilder();
		  for(String ur : this.getRoles()){
			  if(result.length() > 0){
				  result.append(", ");
			  }
			  result.append(ur);
		  }
		  return result.toString();
	  }
		  
	  public void setRoles(Set<String> roles){
		  this.roles = roles;
	  }
	  
	 /**
	  * tells if this user has the role in parameter
	  */
	  @Transient
	  public boolean hasAnyRole(String[] roles){
		  boolean result = false;
		  for(String ur : roles){
			  if(this.getRoles().contains(ur)){
				  result = true;
				  break;
			  }
		  }
		  return result;
	  }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getEmail() == null) ? 0 : getEmail().hashCode());
		result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
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
		if (!(obj instanceof User)) {
			return false;
		}
		User other = (User) obj;
		if (getEmail() == null) {
			if (other.getEmail() != null) {
				return false;
			}
		} else if (!getEmail().equals(other.getEmail())) {
			return false;
		}
		if (getUserId() == null) {
			if (other.getUserId() != null) {
				return false;
			}
		} else if (!getUserId().equals(other.getUserId())) {
			return false;
		}
		return true;
	}
	
	/*public static void main(String[] args) throws Exception {
		UserRoleId id = new UserRoleId();
		
		id.setRoleId("admin");
		id.setUserId("user");
		
		UserRole r =new UserRole();
		
		r.setId(id);
		Set<UserRole> ur = new HashSet<UserRole>();
		ur.add(r);
		
		User user = new User();
		
		user.setUserRoles(ur);
		
		
		ObjectMapper mapper = new ObjectMapper();
		
		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user));
	}*/
}
