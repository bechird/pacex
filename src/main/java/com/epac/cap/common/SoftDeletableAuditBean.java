package com.epac.cap.common;

import javax.persistence.Column;

public class SoftDeletableAuditBean extends AuditableBean{
	private Boolean softDeleteFlag = null;
	
	/**
	   * Used to flag a record as soft deleted
	   */
	  @Column(name = "Soft_Delete", insertable = false)
	  public Boolean getSoftDeleteFlag() {
	    return softDeleteFlag;
	  }

	  /**
	   * sets the soft delete flag value
	   */
	   public void setSoftDeleteFlag(Boolean isDeleted) {
	    softDeleteFlag = isDeleted;
	  }
	   
}
