package com.epac.cap.model;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SortComparator;

import com.epac.cap.handler.DefaultStationsByProductionOrderingComparator;

/**
 * PartCategory a class representing a part category like Book, text, cover...
 */
@Entity
@Table(name = "part_Category")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Category_Id", unique = true, nullable = false, length = 25), name = "id"),
    @AttributeOverride(column = @Column(name = "Name", nullable = false, length = 55), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 100), name = "description")})
public class PartCategory extends LookupItem{
	/**
	 * 
	 */
	private static final long serialVersionUID = -544055057275148277L;
	//@JsonIgnore
	private SortedSet<DefaultStation> defaultStations = new TreeSet<DefaultStation>(new DefaultStationsByProductionOrderingComparator());

	//@JsonIgnore
	//private Set<Part> parts = new HashSet<Part>(0);
	
	/**
	 * 
	 */
	public PartCategory() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 */
	public PartCategory(String id, String name) {
		super(id, name);
	}

	public PartCategory(LookupItem item2) {
		super(item2);
	}
		
	/**
	 * Accessor methods for defaultStations
	 * @return defaultStations  
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "id.categoryId", cascade = CascadeType.ALL, orphanRemoval = true)
	@SortComparator(DefaultStationsByProductionOrderingComparator.class)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="com.epac.cap")
	public SortedSet<DefaultStation> getDefaultStations() {
		return this.defaultStations;
	}

	/**
	 * @param defaultStations the defaultStations to set
	 */
	public void setDefaultStations(SortedSet<DefaultStation> defaultStations) {
		this.defaultStations = defaultStations;
	}

	/**
	 * @return the parts
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "category")
	public Set<Part> getParts() {
		return parts;
	}*/

	/**
	 * @param parts the parts to set
	 
	public void setParts(Set<Part> parts) {
		this.parts = parts;
	}*/

}
