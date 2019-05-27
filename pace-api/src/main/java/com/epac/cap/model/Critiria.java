package com.epac.cap.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnore;
/**
 * Critiria class representing a critiria bean
 */
@Entity
@Table(name = "critiria")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Critiria_Id", unique = true, nullable = false, length = 25), name = "id"),
    @AttributeOverride(column = @Column(name = "Name", nullable = false, length = 55), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 100), name = "description")})
public class Critiria extends LookupItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = -489123215006518875L;
	private Integer activeFlag;
	@JsonIgnore
	private Set<PartCritiria> partCritirias = new HashSet<PartCritiria>(0);
	private Set<String> parts = new HashSet<String>(0);

	/**
	 * 
	 */
	public Critiria() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 */
	public Critiria(String id, String name) {
		super(id, name);
	}

	public Critiria(LookupItem item2) {
		super(item2);
	}
	
	/**
	 * Accessor methods for activeFlag
	 *
	 * @return activeFlag  
	 */

	@Column(name = "Active_Flag")
	public Integer getActiveFlag() {
		return this.activeFlag;
	}

	/**
	 * @param activeFlag the activeFlag to set
	 */
	public void setActiveFlag(Integer activeFlag) {
		this.activeFlag = activeFlag;
	}

	/**
	 * Accessor methods for partCritirias
	 *
	 * @return partCritirias  
	 */

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "id.critiriaId", cascade = CascadeType.ALL, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="com.epac.cap")
	public Set<PartCritiria> getPartCritirias() {
		return this.partCritirias;
	}

	/**
	 * @param partCritirias the partCritirias to set
	 */
	public void setPartCritirias(Set<PartCritiria> partCritirias) {
		this.partCritirias = partCritirias;
	}

	/**
	 * Accessor methods for part
	 */
	@Transient
	//@JsonIgnore
	public Set<String> getParts() {
		parts = new HashSet<String>();
		for (PartCritiria ur : this.getPartCritirias()) {
			parts.add(ur.getId().getPartNum());
		}
		return parts;
	}
}
