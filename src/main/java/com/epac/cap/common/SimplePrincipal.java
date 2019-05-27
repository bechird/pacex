package com.epac.cap.common;

import java.security.Principal;
import java.util.Arrays;

/**
 * A simple implementation of the Principal interface which also adds basic role support.
 * 
 */
public class SimplePrincipal implements Principal {
  protected String name;
  protected String[] roles;

  /**
   * Simply inits the name to "" and the roles to an empty array.
   */
  public SimplePrincipal() {
    name = "";
    roles = new String[0];
  }

  /**
   * Simply inits the name to the userName argument and the roles to an empty array.
   * 
   * @param userName the name of the Principal
   */
  public SimplePrincipal(String userName) {
    name = userName;
    roles = new String[0];
  }

  /**
   * Initializes a SimplePrincipal with the given userName and userRoles. If userRoles are null then an empty array will
   * be used instead.
   * 
   * @param userName the name of the Principal
   * @param userRoles the roles of the Principal
   */
  public SimplePrincipal(String userName, String[] userRoles) {
    name = userName;
    if (userRoles == null) {
      roles = new String[0];
    } else {
      roles = userRoles;
    }
  }

  /**
   * @see java.security.Principal#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * @return the roles
   */
  public String[] getRoles() {
    return roles;
  }

  /**
   * Determines if a principal has the given role
   * 
   * @param roleName the role to check for
   * @return true if this principal has a role with the given name
   */
  public boolean isUserInRole(String roleName) {
    for (int i = 0; i < roles.length; i++) {
      if (roleName.equals(roles[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  /**
   * Compares equality on the name. Any other class implementing Principal with the same name will be considered as
   * equal to a SimplePrincipal.
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
    if (!(obj instanceof Principal)) {
      return false;
    }
    Principal other = (Principal) obj;
    if (name == null) {
      if (other.getName() != null) {
        return false;
      }
    } else if (!name.equals(other.getName())) {
      return false;
    }

    return true;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    final int maxLen = 10;
    StringBuilder builder = new StringBuilder();
    builder.append("SimplePrincipal [name=");
    builder.append(name);
    builder.append(", roles=");
    builder.append(roles != null ? Arrays.asList(roles).subList(0, Math.min(roles.length, maxLen)) : null);
    builder.append("]");
    return builder.toString();
  }


}
