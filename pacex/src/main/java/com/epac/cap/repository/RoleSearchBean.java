package com.epac.cap.repository;

import com.epac.cap.common.AuditableSearchBean;

/**
 * Container for Role search criteria.
 * 
 * @author walid
 * 
 */
@SuppressWarnings("serial")
public class RoleSearchBean extends AuditableSearchBean{
	private String roleId;
	private String roleIdDiff;
	private String roleName;
	private String roleNameExact;
	private String roleDescription;

	/**
	 * Default constructor.  Sets the default ordering to roleName ascending 
	 *
	 **/
	public RoleSearchBean(){
		setOrderBy("roleName");
	}
	
	/**   
	 * Accessor for the roleId property
	 *
	 * @return roleId
	 */
	public String getRoleId(){
		return this.roleId;
	}

	/**       
	 * Mutator for the roleId property
	 *
	 * @param inRoleId the new value for the roleId property
	 */	
	public void setRoleId(String inRoleId){
		this.roleId = inRoleId;
	}

	/**
	 * @return the roleIdDiff
	 */
	public String getRoleIdDiff() {
		return roleIdDiff;
	}

	/**
	 * @param roleIdDiff the roleIdDiff to set
	 */
	public void setRoleIdDiff(String roleIdDiff) {
		this.roleIdDiff = roleIdDiff;
	}

	/**
	 * @return the roleNameExact
	 */
	public String getRoleNameExact() {
		return roleNameExact;
	}

	/**
	 * @param roleNameExact the roleNameExact to set
	 */
	public void setRoleNameExact(String roleNameExact) {
		this.roleNameExact = roleNameExact;
	}

	/**   
	 * Accessor for the roleName property
	 *
	 * @return roleName
	 */
	public String getRoleName(){
		return this.roleName;
	}

	/**       
	 * Mutator for the roleName property
	 *
	 * @param inRoleName the new value for the roleName property
	 */	
	public void setRoleName(String inRoleName){
		this.roleName = inRoleName;
	}

	/**   
	 * Accessor for the roleDescription property
	 *
	 * @return roleDescription
	 */
	public String getRoleDescription(){
		return this.roleDescription;
	}

	/**       
	 * Mutator for the roleDescription property
	 *
	 * @param inRoleDescription the new value for the roleDescription property
	 */	
	public void setRoleDescription(String inRoleDescription){
		this.roleDescription = inRoleDescription;
	}

	
}