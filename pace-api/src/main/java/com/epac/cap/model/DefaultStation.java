package com.epac.cap.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.epac.cap.common.AuditableBean;


/**
 * DefaultStation class representing a default production station
 */
@Entity
@Table(name = "default_Station")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="com.epac.cap")
public class DefaultStation extends AuditableBean implements java.io.Serializable, Comparable<DefaultStation> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8668079919336621168L;
	private DefaultStationId id;
	//@JsonIgnore
	//private PartCategory category;
	//private Critiria critiria;
	//private StationCategory stationCategory;
	private Integer productionOrdering;

	/**
	 * Default constructor
	 */
	public DefaultStation() {
	}

	/**
	 * Constructor which sets the identity property
	 */
	public DefaultStation(PartCategory category, Critiria critiria, StationCategory stationCategory) {
		this(null,category,critiria,stationCategory);
	}

	/**
	 * Constructor which sets the identity property
	 */
	public DefaultStation(DefaultStationId id, PartCategory category, Critiria critiria, StationCategory stationCategory) {
		this.id = id;
		if(this.id == null){
			this.id = new DefaultStationId();
		}
		//this.category = category;
		//if(this.category != null && this.category.getId() != null){
		//	this.id.setCategoryId(this.category.getId());
		//}
		/*this.critiria = critiria;
		if(this.critiria != null && this.critiria.getId() != null){
			this.id.setCritiriaId(this.critiria.getId());
		}
		this.stationCategory = stationCategory;
		if(this.stationCategory != null && this.stationCategory.getId() != null){
			this.id.setStationCategoryId(this.stationCategory.getId());
		}*/
	}

	/**
	 * Accessor methods for id
	 *
	 * @return id  
	 */
	@EmbeddedId

	@AttributeOverrides({
			@AttributeOverride(name = "categoryId", column = @Column(name = "Category_Id", nullable = false, length = 25)),
			@AttributeOverride(name = "critiriaId", column = @Column(name = "Critiria_Id", nullable = false, length = 25)),
			@AttributeOverride(name = "bindingTypeId", column = @Column(name = "Binding_Type_Id", nullable = false, length = 25)),
			@AttributeOverride(name = "stationCategoryId", column = @Column(name = "Station_Category_Id", nullable = false, length = 25)) })
	public DefaultStationId getId() {
		return this.id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(DefaultStationId id) {
		this.id = id;
	}

	/**
	 * Accessor methods for category
	 *
	 * @return category  
	 

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Category_Id", nullable = false, insertable = false, updatable = false)
	public PartCategory getCategory() {
		return this.category;
	}*/

	/**
	 * @param category the category to set
	 
	public void setCategory(PartCategory category) {
		this.category = category;
	}*/
	
	/**
	 * Accessor methods for critiria
	 *
	 * @return critiria  
	 

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Critiria_Id", nullable = false, insertable = false, updatable = false)
	public Critiria getCritiria() {
		return this.critiria;
	}*/

	/**
	 * @param critiria the critiria to set
	 
	public void setCritiria(Critiria critiria) {
		this.critiria = critiria;
	}*/

	/**
	 * Accessor methods for stationCategory
	 *
	 * @return stationCategory  
	 

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Station_Category_Id", nullable = false, insertable = false, updatable = false)
	public StationCategory getStationCategory() {
		return this.stationCategory;
	}*/

	/**
	 * @param stationCategory the stationCategory to set
	 
	public void setStationCategory(StationCategory stationCategory) {
		this.stationCategory = stationCategory;
	}*/

	/**
	 * Accessor methods for productionOrdering
	 *
	 * @return productionOrdering  
	 */

	@Column(name = "Production_Ordering")
	public Integer getProductionOrdering() {
		return this.productionOrdering;
	}

	/**
	 * @param productionOrdering the productionOrdering to set
	 */
	public void setProductionOrdering(Integer productionOrdering) {
		this.productionOrdering = productionOrdering;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof DefaultStation)) {
			return false;
		}
		DefaultStation other = (DefaultStation) obj;
		if (getId() == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (!getId().equals(other.getId())) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(DefaultStation o) {
	    return new org.apache.commons.lang.builder.CompareToBuilder()
	    		.append(getId(), o.getId())
	    		.toComparison();
	}

}
