package com.epac.cap.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.epac.cap.common.AuditableBean;

@Entity
@Table(name = "WFS_Datasupport" )
public class WFSDataSupport extends AuditableBean implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String NAME_RASTER 			= "Raster";
	public static final String NAME_DOWNLOAD 		= "Download";
	public static final String NAME_IMPOSE 		= "Impose";
	public static final String NAME_COPY 			= "Copy";
	
	public static final String TYPE_TEXT			= "TEXT";
	public static final String TYPE_COVER			= "COVER";
	
	
	private Integer dataSupportId;
	private String name;
	private String description;
	private String dsType;
	private String partNumb;
	private WFSProductionStatus productionStatus;
	private Integer progressId;
	
	private Set<WFSLocation> locations = new HashSet<WFSLocation>();
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "iddatasupport")
	public Integer getDataSupportId() {
		return dataSupportId;
	}
	
	
	@Column(name = "name", length = 55)
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Column(name = "description", length = 100)
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Column(name = "type", length = 55)
	public String getDsType() {
		return dsType;
	}
	
	public void setDsType(String type) {
		this.dsType = type;
	}
	
	@Column(name = "partnumb", length = 25)
	public String getPartNumb() {
		return partNumb;
	}
	
	public void setPartNumb(String partNumb) {
		this.partNumb = partNumb;
	}
	
	/**
	 * @return the productionStatus
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "productionstatusid")
	public WFSProductionStatus getProductionStatus() {
		return productionStatus;
	}

	/**
	 * @param productionStatus the productionStatus to set
	 */
	public void setProductionStatus(WFSProductionStatus productionStatus) {
		this.productionStatus = productionStatus;
	}
	
	/**
	 * @return the locations
	 */
	@OneToMany(fetch = FetchType.EAGER,cascade=CascadeType.ALL, mappedBy = "dataSupport")
	@OrderBy("locationId desc")
	public Set<WFSLocation> getLocations() {
		return locations;
	}

	/**
	 * @param locations the locations to set
	 */
	public void setLocations(Set<WFSLocation> locations) {
		this.locations = locations;
	}
	
	
	
	public void addLocation(WFSLocation location) {
		if(locations == null)
			locations = new HashSet<WFSLocation>();
		
		location.setDataSupport(this);
		locations.add(location);

	}
	/**
	 * @return the progress
	 *
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "progress_Id", referencedColumnName = "progressid")*/
	@Column(name = "progress_Id")
	public Integer getProgressId() {
		return progressId;
	}

	/**
	 * @param progress the progress to set
	 */
	public void setProgressId(Integer progressId) {
		this.progressId = progressId;
	}
	
	/**
	 * returns the location with that specific type 
	 */
	@Transient
	public WFSLocation getLocationdByType(String type){
		WFSLocation result = null;
		if(!this.getLocations().isEmpty()){
			for(WFSLocation dl : this.getLocations()){
				if(dl.getLocationType().equals(type)){
					result = dl;
					break;
				}
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getDataSupportId() == null) ? 0 : getDataSupportId().hashCode());
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
		if (!(obj instanceof WFSDataSupport)) {
			return false;
		}
		WFSDataSupport other = (WFSDataSupport) obj;
		if (getDataSupportId() == null) {
			if (other.getDataSupportId() != null) {
				return false;
			}
		} else if (!getDataSupportId().equals(other.getDataSupportId())) {
			return false;
		}
		return true;
	}


	public void setDataSupportId(Integer dataSupportId) {
		this.dataSupportId = dataSupportId;
	}
	
}
