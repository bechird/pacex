package com.epac.cap.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * StationCategory a class representing a station category like Press, Cover Press, Plow Folder, Bindery...
 */
@Entity
@Table(name = "station_Category")
@AttributeOverrides({
    @AttributeOverride(column = @Column(name = "Category_Id", unique = true, nullable = false, length = 25), name = "id"),
    @AttributeOverride(column = @Column(name = "Name", nullable = false, length = 55), name = "name"),
    @AttributeOverride(column = @Column(name = "Description", length = 100), name = "description")})
public class StationCategory extends LookupItem{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3819496434538981613L;
	//@JsonIgnore
	//private Set<Station> stations = new HashSet<Station>(0);
	
	private Set<DefaultStation> defaultStations = new HashSet<DefaultStation>(0);
	
	/**
	 * 
	 */
	public StationCategory() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 */
	public StationCategory(String id, String name) {
		super(id, name);
	}
	
	public StationCategory(LookupItem item2) {
		super(item2);
	}

	public enum Categories { 
		PRESS("PRESS"),
		PLOWFOLDER("PLOWFOLDER"),
		COVERPRESS("COVERPRESS"),
		LAMINATION("LAMINATION"),
		BINDER("BINDER"),
		ENDSHEET("ENDSHEET"),
		SHIPPING("SHIPPING"),
		PLASTICOIL("PLASTICOIL"),
		CASEBOUND("CASEBOUND")
		;

		private String name;
		private Categories(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}
	
	/**
	 * @return the stations
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "stationCategoryId")
	public Set<Station> getStations() {
		return stations;
	}*/

	/**
	 * @param stations the stations to set
	 
	public void setStations(Set<Station> stations) {
		this.stations = stations;
	}*/
	
	/**
	 * @return the defaultStations
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "id.stationCategoryId")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="com.epac.cap")
	public Set<DefaultStation> getDefaultStations() {
		return defaultStations;
	}

	/**
	 * @param defaultStations the defaultStations to set
	 */
	public void setDefaultStations(Set<DefaultStation> defaultStations) {
		this.defaultStations = defaultStations;
	}
}
