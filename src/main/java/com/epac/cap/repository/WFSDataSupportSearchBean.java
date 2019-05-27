package com.epac.cap.repository;

import com.epac.cap.common.AuditableSearchBean;

public class WFSDataSupportSearchBean extends AuditableSearchBean{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 294660371875584190L;
	private String partNum;
	private String dsName;
	private String productionStatusId;

	/**
	 * @return the partNum
	 */
	public String getPartNum() {
		return partNum;
	}

	/**
	 * @param partNum the partNum to set
	 */
	public void setPartNum(String partNum) {
		this.partNum = partNum;
	}

	/**
	 * @return the dsName
	 */
	public String getDsName() {
		return dsName;
	}

	/**
	 * @param dsName the dsName to set
	 */
	public void setDsName(String dsName) {
		this.dsName = dsName;
	}

	/**
	 * @return the productionStatusId
	 */
	public String getProductionStatusId() {
		return productionStatusId;
	}

	/**
	 * @param productionStatusId the productionStatusId to set
	 */
	public void setProductionStatusId(String productionStatusId) {
		this.productionStatusId = productionStatusId;
	}

	
}
