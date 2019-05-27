package com.epac.cap.repository;

import com.epac.cap.common.AuditableSearchBean;

public class WFSLocationSearchBean extends AuditableSearchBean{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3589082208317078042L;
	private Integer wfsDatasupportId;

	/**
	 * @return the wfsDatasupportId
	 */
	public Integer getWfsDatasupportId() {
		return wfsDatasupportId;
	}

	/**
	 * @param wfsDatasupportId the wfsDatasupportId to set
	 */
	public void setWfsDatasupportId(Integer wfsDatasupportId) {
		this.wfsDatasupportId = wfsDatasupportId;
	}
	
	
	
}
