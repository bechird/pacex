/*
 * Copyright(C) 2016 FUJIFILM Corporation, All rights
 * reserved. This computer program is the property of FUJIFILM
 * Corporation, Japan, and may be used and copied only as specifically
 * permitted under written licence agreement signed by FUJIFILM
 * Corporation.
 */
package jp.co.fujifilm.xmf.oc.model.printing;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PrintingJobRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class PrintingJobRequest {

    @XmlElementWrapper
    @XmlElement(name = "job")
    private List<PrintingJob> printingJobList;

    private Integer doPurge;
    
    private Integer numberOfRefreshPattern;

    private Integer dummyBookLength;
    private Integer repeatJobList;
    
    public Integer getDummyBookLength() {
		return dummyBookLength;
	}
    
    public void setDummyBookLength(Integer dummyBookLength) {
		this.dummyBookLength = dummyBookLength;
	}
    
    public Integer getRepeatJobList() {
		return repeatJobList;
	}
    
    public void setRepeatJobList(Integer repeatJobList) {
		this.repeatJobList = repeatJobList;
	}

	public Integer getDoPurge() {
        return doPurge;
    }

    public void setDoPurge(Integer doPurge) {
        this.doPurge = doPurge;
    }

    public Integer getNumberOfRefreshPattern() {
        return numberOfRefreshPattern;
    }

    public void setNumberOfRefreshPattern(Integer numberOfRefreshPattern) {
        this.numberOfRefreshPattern = numberOfRefreshPattern;
    }

    public List<PrintingJob> getPrintingJobList() {
        return printingJobList;
    }

    public void setPrintingJobList(List<PrintingJob> printingJobList) {
        this.printingJobList = printingJobList;
    }


}
