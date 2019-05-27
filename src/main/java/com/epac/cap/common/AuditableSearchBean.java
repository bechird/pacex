package com.epac.cap.common;

import java.util.Date;


/**
 * An extension of the default SearchBean which adds the auditing fields.
 * 
 */
public class AuditableSearchBean extends SearchBean {
  /**
   * 
   */
  private static final long serialVersionUID = 1671985453426745142L;
  private String creatorId;
  private Date createdDateFrom;
  private Date createdDateTo;
  private String lastUpdateId;
  private Date lastUpdateDateFrom;
  private Date lastUpdateDateTo;

  /**
   * Accessor for the creatorId property
   * 
   * @return creatorId
   */
  public String getCreatorId() {
    return creatorId;
  }

  /**
   * Mutator for the creatorId property
   * 
   * @param inCreatorId the new value for the creatorId property
   */
  public void setCreatorId(String inCreatorId) {
    creatorId = inCreatorId;
  }

  /**
   * Accessor for the createdDateFrom property
   * 
   * @return createdDateFrom
   */
  public Date getCreatedDateFrom() {
    return createdDateFrom;
  }

  /**
   * Mutator for the createdDateFrom property
   * 
   * @param inCreatedDate the new value for the createdDateFrom property
   */
  public void setCreatedDateFrom(Date inCreatedDate) {
    createdDateFrom = inCreatedDate;
  }

  /**
   * Accessor for the createdDateTo property
   * 
   * @return createdDateTo
   */
  public Date getCreatedDateTo() {
    return createdDateTo;
  }

  /**
   * Mutator for the createdDateTo property
   * 
   * @param inCreatedDate the new value for the createdDateTo property
   */
  public void setCreatedDateTo(Date inCreatedDate) {
    if (!DateUtil.hasTime(inCreatedDate)) {
      // date didnt have a time so add the last end time of the day to it so that
      // date range searches behave as expected
      inCreatedDate = DateUtil.getEnd(inCreatedDate);
    }
    createdDateTo = inCreatedDate;
  }

  /**
   * Accessor for the lastUpdateId property
   * 
   * @return lastUpdateId
   */
  public String getLastUpdateId() {
    return lastUpdateId;
  }

  /**
   * Mutator for the lastUpdateId property
   * 
   * @param inLastUpdateId the new value for the lastUpdateId property
   */
  public void setLastUpdateId(String inLastUpdateId) {
    lastUpdateId = inLastUpdateId;
  }

  /**
   * Accessor for the lastUpdateDateFrom property
   * 
   * @return lastUpdateDateFrom
   */
  public Date getLastUpdateDateFrom() {
    return lastUpdateDateFrom;
  }

  /**
   * Mutator for the lastUpdateDateFrom property
   * 
   * @param inLastUpdateDate the new value for the lastUpdateDateFrom property
   */
  public void setLastUpdateDateFrom(Date inLastUpdateDate) {
    lastUpdateDateFrom = inLastUpdateDate;
  }

  /**
   * Accessor for the lastUpdateDateTo property
   * 
   * @return lastUpdateDateTo
   */
  public Date getLastUpdateDateTo() {
    return lastUpdateDateTo;
  }

  /**
   * Mutator for the lastUpdateDateTo property
   * 
   * @param inLastUpdateDate the new value for the lastUpdateDateTo property
   */
  public void setLastUpdateDateTo(Date inLastUpdateDate) {
    if (!DateUtil.hasTime(inLastUpdateDate)) {
      // date didnt have a time so add the last end time of the day to it so that
      // date range searches behave as expected
      inLastUpdateDate = DateUtil.getEnd(inLastUpdateDate);
    }
    lastUpdateDateTo = inLastUpdateDate;
  }
}
