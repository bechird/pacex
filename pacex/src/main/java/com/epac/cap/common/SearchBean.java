package com.epac.cap.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for search criteria. Contains general properties common to most searches.
 * 
 */
public abstract class SearchBean implements Serializable {

  
  /**
	 * 
	 */
	private static final long serialVersionUID = -2555559002314782635L;
	
  private List<OrderBy> orderByList = new ArrayList<OrderBy>();
  private Integer maxResults = null;
  private Boolean softDeleteFlag = null;

  /**
   * order by ascending
   */
  public static final String ASC = OrderBy.ASC;

  /**
   * order by descending
   */
  public static final String DESC = OrderBy.DESC;

  /**
   * Sets the order by column to the given parameter (ascending). If there are any existing order by elements they will
   * be removed.
   * 
   * @param orderBy the column/property to order by
   * 
   * @see #setOrderBy(String, String)
   */
  public void setOrderBy(String orderBy) {
    this.setOrderBy(orderBy, ASC);
  }

  /**
   * Sets the order by column to the given parameter and given direction. If there are any existing order by elements
   * they will be removed.
   * 
   * @param orderBy the column/property to order by
   * @param direction the direction of the order by
   */
  public void setOrderBy(String orderBy, String direction) {
    if (!getOrderByList().isEmpty()) {
      getOrderByList().clear();
    }
    getOrderByList().add(new OrderBy(orderBy, direction));
  }

  /**
   * Adds the order by column ascending to the list of orderings
   * 
   * @param orderBy the column/property to order by
   */
  public void addOrderBy(String orderBy) {
    getOrderByList().add(new OrderBy(orderBy, ASC));
  }

  /**
   * Adds the order by column ascending to the list of orderings
   * 
   * @param orderBy the column/property to order by
   */
  public void addOrderBy(OrderBy orderBy) {
    getOrderByList().add(orderBy);
  }

  /**
   * Same as addOrderBy but orders descending
   * 
   * @param orderBy the column/property to order by
   * @see #addOrderBy(String)
   */
  public void addOrderByDescending(String orderBy) {
    getOrderByList().add(new OrderBy(orderBy, DESC));
  }

  /**
   * Returns the order by name of the first element in the order by list or null if the list is empty
   * 
   * @return the orderBy
   */
  public String getOrderBy() {
    return getOrderByList().isEmpty() ? null : getOrderByList().get(0).getName();
  }

  /**
   * Sets the order by direction of the first element. If the order by list is empty then no operation is performed.
   * 
   * @param orderByDir the direction of the order by
   */
  public void setOrderByDir(String orderByDir) {
    if (!getOrderByList().isEmpty()) {
      getOrderByList().get(0).setDirection(orderByDir);
    }
  }

  /**
   * Sets the order by direction to "asc" (ascending)
   */
  public void orderByDirAscending() {
    setOrderByDir(ASC);
  }

  /**
   * Sets the order by direction to "desc" (descending)
   */
  public void orderByDirDescending() {
    setOrderByDir(DESC);
  }

  /**
   * Returns the order by direction or "asc" if null
   * 
   * @return the orderByDir
   */
  public String getOrderByDir() {
    String orderByDir = getOrderByList().isEmpty() ? null : getOrderByList().get(0).getDirection();
    return orderByDir == null ? ASC : orderByDir;
  }

  /**
   * Sets the orderByList to the given List of OrderBys. It will ignore the reassignment if the input list is null. This
   * is to prevent NPE. If you want an empty list then use the clear() method of the OrderByList object.
   * 
   * @param orderByList the orderByList to set
   */
  public void setOrderByList(List<OrderBy> orderByList) {
    if (orderByList != null) {
      this.orderByList = orderByList;
    }
  }

  /**
   * A list of orderings
   * 
   * @return the orderByList
   */
  public List<OrderBy> getOrderByList() {
    return orderByList;
  }

  /**
   * @param maxResults the maxResults to set
   */
  public void setMaxResults(Integer maxResults) {
    this.maxResults = maxResults;
  }

  /**
   * @return the maxResults
   */
  public Integer getMaxResults() {
    return maxResults;
  }

  /**
   * @param softDeleteFlag the softDeleteFlag to set
   */
  public void setSoftDeleteFlag(Boolean softDeleteFlag) {
    this.softDeleteFlag = softDeleteFlag;
  }

  /**
   * @return the softDeleteFlag
   */
  public Boolean getSoftDeleteFlag() {
    return softDeleteFlag;
  }

}
