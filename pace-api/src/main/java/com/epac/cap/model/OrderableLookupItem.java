package com.epac.cap.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * A lookup item which is also orderable using the pick sequence field.
 * 
 */
@MappedSuperclass
public class OrderableLookupItem extends LookupItem {
  
  /**
	 * 
	 */
	private static final long serialVersionUID = -714185443172381630L;
	private Integer pickSequence;

  /**
   * @return the pickSequence
   */
  @Column(name = "PICK_SEQUENCE")
  public Integer getPickSequence() {
    return pickSequence;
  }

  /**
   * @param pickSequence the pickSequence to set
   */
  public void setPickSequence(Integer pickSequence) {
    this.pickSequence = pickSequence;
  }

  @Override
  public String toString() {
    return "OrderableLookupItem [pickSequence=" + pickSequence + ", getId()=" + getId() + ", getName()="
            + getName() + "]";
  }

}
