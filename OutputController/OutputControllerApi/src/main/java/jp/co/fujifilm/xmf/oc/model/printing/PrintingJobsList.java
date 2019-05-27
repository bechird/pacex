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
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PrintingJobsList")
@XmlAccessorType(XmlAccessType.FIELD)
public class PrintingJobsList {

    @XmlElement(name = "job")
    private List<PrintingJobStatus> printingJobStatusList;

    /**
     *
     * @return
     */
    public List<PrintingJobStatus> getPrintingJobStatusList() {
        return this.printingJobStatusList;
    }
    /**
     *
     * @param printingJobStatus
     */
    public void setPrintingJobStatusList(List<PrintingJobStatus> printingJobStatusList) {
        this.printingJobStatusList = printingJobStatusList;
    }
}
