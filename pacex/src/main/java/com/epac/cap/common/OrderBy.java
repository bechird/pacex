package com.epac.cap.common;

import java.io.Serializable;

/**
 * Models a single query order by criteria. Simply a name and direction and a flag indicating whether case should be
 * considered or not.
 * 
 */
public class OrderBy implements Serializable {
  
    /**
	 * 
	 */
	private static final long serialVersionUID = -4827665559420901757L;
	
  private String name = null;
  private String direction = null;
  private boolean ignoreCase = false;

  /**
   * order by ascending
   */
  public static final String ASC = "asc";

  /**
   * order by descending
   */
  public static final String DESC = "desc";

  /**
   * Default constructor. Sets the ordering direction to asc
   */
  public OrderBy() {
    direction = SearchBean.ASC;
  }

  /**
   * Intantiates an OrderBy with the given orderByName as the name and ascending ordering direction
   * 
   * @param orderByName
   */
  public OrderBy(String orderByName) {
    this();
    name = orderByName;
  }

  /**
   * Intantiates an OrderBy with the given orderByName as the name and the given ordering direction
   * 
   * @param orderByName
   * @param direction
   */
  public OrderBy(String orderByName, String direction) {
    name = orderByName;
    this.direction = direction;
  }

  public OrderBy(String orderByName, String direction, boolean ignoreCase) {
    this(orderByName, direction);
    setIgnoreCase(ignoreCase);
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
   * @return the direction
   */
  public String getDirection() {
    return direction;
  }

  /**
   * @param direction the direction to set
   */
  public void setDirection(String direction) {
    this.direction = direction;
  }

  /**
   * Whether or not to ignore case during ordering on this order by statement
   * 
   * @return
   */
  public boolean isIgnoreCase() {
    return ignoreCase;
  }

  public void setIgnoreCase(boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((direction == null) ? 0 : direction.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  /**
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
    if (!(obj instanceof OrderBy)) {
      return false;
    }
    OrderBy other = (OrderBy) obj;
    if (direction == null) {
      if (other.direction != null) {
        return false;
      }
    } else if (!direction.equals(other.direction)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("OrderBy [direction=");
    builder.append(direction);
    builder.append(", name=");
    builder.append(name);
    builder.append(", ignoreCase=");
    builder.append(ignoreCase);
    builder.append("]");
    return builder.toString();
  }

}
