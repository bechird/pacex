package com.epac.cap.model;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.io.FilenameUtils;

import com.epac.cap.common.AuditableBean;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "WFS_Location" )
public class WFSLocation extends AuditableBean implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String DESTINATION 	= "Destination";
	public static final String SOURCE		= "Source";
	
	private Integer locationId;
	private String path;
	private String fileName;
	private String protocol;
	private String locationType;
	private WFSDataSupport dataSupport;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "idlocation", unique = true, nullable = false, length = 25)
	public Integer getLocationId() {
		return locationId;
	}
	
	public void setLocationId(Integer locationId) {
		this.locationId = locationId;
	}
	
	@Column(name = "path", length = 255)
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	@Column(name = "protocol", length = 55)
	public String getProtocol() {
		return protocol;
	}
	
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
	@Column(name = "type", length = 55)
	public String getLocationType() {
		return locationType;
	}
	
	public void setLocationType(String type) {
		this.locationType = type;
	}
	
	@ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="dataSupportId", nullable=false)
	@JsonIgnore
	public WFSDataSupport getDataSupport() {
		return dataSupport;
	}

	public void setDataSupport(WFSDataSupport dataSupport) {
		this.dataSupport = dataSupport;
	}

	
	@Transient
	@JsonIgnore
	public Integer getDataSupportId() {
		return dataSupport != null? dataSupport.getDataSupportId(): null;
	}
	
	/*
	public void setDataSupportId(Integer dataSupportId) {
		this.dataSupportId = dataSupportId;
	}*/

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getLocationId() == null) ? 0 : getLocationId().hashCode());
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
		if (!(obj instanceof WFSLocation)) {
			return false;
		}
		WFSLocation other = (WFSLocation) obj;
		if (getLocationId() == null) {
			if (other.getLocationId() != null) {
				return false;
			}
		} else if (!getLocationId().equals(other.getLocationId())) {
			return false;
		}
		return true;
	}

	/**
	 * @return the fileName
	 */
	@Transient
	public String getFileName() {
		String path = getPath();
		if(path != null)
			this.fileName = FilenameUtils.getName(path);
		else
			this.fileName = "UNKNOWN";
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	

}
