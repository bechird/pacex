package com.epac.cap.repository;

import com.epac.cap.common.AuditableSearchBean;
import com.epac.cap.common.DateUtil;
import java.util.Date;

/**
 * Container for LoadTag search criteria.
 * 
 * @author walid
 * 
 */
@SuppressWarnings("serial")
public class LoadTagSearchBean extends AuditableSearchBean{
	private Integer loadTagId;
	private String tagNum;
	private Integer jobId;
	private String machineId;
	
	private Float quantity;
	private Float waste;
	private String cartNum;
	
	private String searchLoadtagIdPart;
	private String searchJobIdPart;
	private String searchWastePart;
	private String searchQuantityPart;
	private Integer resultOffset;
	
    private Date startDateExact;
    private Date finishDateExact;

	/**
	 * Default constructor.  Sets the default ordering to logId ascending 
	 *
	 **/
	public LoadTagSearchBean(){
		setOrderBy("loadTagId", "desc");
	}

	/**
	 * @return the loadTagId
	 */
	public Integer getLoadTagId() {
		return loadTagId;
	}


	/**
	 * @param loadTagId the loadTagId to set
	 */
	public void setLoadTagId(Integer loadTagId) {
		this.loadTagId = loadTagId;
	}

	/**
	 * @return the tagNum
	 */
	public String getTagNum() {
		return tagNum;
	}

	/**
	 * @param tagNum the tagNum to set
	 */
	public void setTagNum(String tagNum) {
		this.tagNum = tagNum;
	}

	/**
	 * @return the jobId
	 */
	public Integer getJobId() {
		return jobId;
	}

	/**
	 * @param jobId the jobId to set
	 */
	public void setJobId(Integer jobId) {
		this.jobId = jobId;
	}

	/**
	 * @return the machineId
	 */
	public String getMachineId() {
		return machineId;
	}

	/**
	 * @param machineId the machineId to set
	 */
	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}

	public Integer getResultOffset() {
		return resultOffset;
	}

	public void setResultOffset(Integer resultOffset) {
		this.resultOffset = resultOffset;
	}

	public Float getQuantity() {
		return quantity;
	}

	public void setQuantity(Float quantity) {
		this.quantity = quantity;
	}

	public Float getWaste() {
		return waste;
	}

	public void setWaste(Float waste) {
		this.waste = waste;
	}

	public String getCartNum() {
		return cartNum;
	}

	public void setCartNum(String cartNum) {
		this.cartNum = cartNum;
	}

	public String getSearchLoadtagIdPart() {
		return searchLoadtagIdPart;
	}

	public void setSearchLoadtagIdPart(String searchLoadtagIdPart) {
		this.searchLoadtagIdPart = searchLoadtagIdPart;
	}

	public String getSearchJobIdPart() {
		return searchJobIdPart;
	}

	public void setSearchJobIdPart(String searchJobIdPart) {
		this.searchJobIdPart = searchJobIdPart;
	}

	public String getSearchWastePart() {
		return searchWastePart;
	}

	public void setSearchWastePart(String searchWastePart) {
		this.searchWastePart = searchWastePart;
	}

	public String getSearchQuantityPart() {
		return searchQuantityPart;
	}

	public void setSearchQuantityPart(String searchQuantityPart) {
		this.searchQuantityPart = searchQuantityPart;
	}

	public Date getStartDateExact() {
		return startDateExact;
	}

	public void setStartDateExact(Date startDateExact) {
		this.startDateExact = startDateExact;
	}

	public Date getFinishDateExact() {
		return finishDateExact;
	}

	public void setFinishDateExact(Date finishDateExact) {
		this.finishDateExact = finishDateExact;
	}
	
	
}