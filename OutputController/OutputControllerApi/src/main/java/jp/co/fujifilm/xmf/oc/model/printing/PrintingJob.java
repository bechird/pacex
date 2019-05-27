/*
 * Copyright(C) 2016 FUJIFILM Corporation, All rights
 * reserved. This computer program is the property of FUJIFILM
 * Corporation, Japan, and may be used and copied only as specifically
 * permitted under written licence agreement signed by FUJIFILM
 * Corporation.
 */
package jp.co.fujifilm.xmf.oc.model.printing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.fujifilm.xmf.oc.model.jobs.Job;

@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrintingJob {

    private Job job;

    private String status;
    private Integer printedSheetCount;
    private Integer partialSheetCount;
    private Integer totalSheetCount;
    private Integer numberOfCopies;

    @JsonIgnore
    public String getId() {
    	if(job == null)
    		return null;
        return job.getId();
    }

    public void setId(String id) {
    	if(job == null)
    		job = new Job();
    	
        this.job.setId(id);;
    }
    
   
    public Job getJob() {
		return job;
	}
    
    public void setJob(Job job) {
		this.job = job;
	}

    public Integer getNumberOfCopies() {
        return numberOfCopies;
    }

    public void setNumberOfCopies(Integer numberOfCopies) {
        this.numberOfCopies = numberOfCopies;
    }
    
    public String getStatus() {
		return status;
	}
    
    public Integer getTotalSheetCount() {
		return totalSheetCount;
	}
    
    public Integer getPrintedSheetCount() {
		return printedSheetCount;
	}
    
    public void setStatus(String status) {
		this.status = status;
	}
    
    public void setPrintedSheetCount(Integer printedSheetCount) {
		this.printedSheetCount = printedSheetCount;
	}
    
    public void setTotalSheetCount(Integer totalSheetCount) {
		this.totalSheetCount = totalSheetCount;
	}
    
    public Integer getPartialSheetCount() {
		return partialSheetCount;
	}
    
    public void setPartialSheetCount(int partialSheetCount) {
		this.partialSheetCount = partialSheetCount;
	}
    
    @Override
    public boolean equals(Object obj) {
    	if(obj instanceof PrintingJob){
    		PrintingJob that = (PrintingJob) obj;
    		return that.getId() != null && that.getId().equals(this.getId());
    	}
    	
    	if(obj instanceof Job){
    		Job that = (Job) obj;
    		return that.getId() != null && that.getId().equals(this.getId());
    	}
    	return super.equals(obj);
    }
    
}
