package com.epac.cap.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.epac.cap.common.AuditableBean;

/***
 * A simple bean containing a string id and string name and description representing an item of a certain lookup/reference
 * data type thats also auditable.
 * All lookup type beans will extend this class and simply provide column attribute annotation overrides.
 * 
 */
@MappedSuperclass
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="com.epac.cap")
public abstract class LookupItem extends AuditableBean implements Serializable, Comparable<LookupItem> {
  
  /**
	 * 
	 */
	private static final long serialVersionUID = 3974817067325702109L;
	
	private String id;
	private String name;
    private String description;

  /**
   * No op constructor
   */
  public LookupItem() {}

  public LookupItem(String id) {
	    setId(id);
	  }
  
  public LookupItem(LookupItem item2) {
	  this.setId(item2.getId());
	  this.setName(item2.getName());
	  this.setDescription(item2.getDescription());
	  this.setCreatorId(item2.getCreatorId());
	  this.setCreatedDate(item2.getCreatedDate());
	  this.setLastUpdateDate(item2.getLastUpdateDate());
	  this.setLastUpdateId(item2.getLastUpdateId());
  }
  
  public LookupItem(String id, String name) {
    this.id = id;
    this.name = name;
  }

  /**
   * @return the id
   */
  @Id
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  
  /**
	 * @return the name
	 */
    @NotNull
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
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = prime + ((getName() == null) ? 0 : getName().hashCode());
    result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
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
    if (!(obj instanceof LookupItem)) {
      return false;
    }
    LookupItem other = (LookupItem) obj;
    if (getName() == null) {
      if (other.getName() != null) {
        return false;
      }
    } else if (!getName().equals(other.getName())) {
      return false;
    }
    if (getId() == null) {
      if (other.getId() != null) {
        return false;
      }
    } else if (!getId().equals(other.getId())) {
      return false;
    }
    return true;
  }

  /**
   * @see java.lang.Comparable#compareTo(Object)
   */
  @Override
  public int compareTo(LookupItem object) {
    return new CompareToBuilder().append(getId(), object.getId()).append(getName(), object.getName())
            .toComparison();
  }

  @Override
  public String toString() {
    return "LookupItem [id=" + getId() + ", name=" + getName() + "]";
  }

}
