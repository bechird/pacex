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
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PrintingJobStatus")
@XmlAccessorType(XmlAccessType.FIELD)
public class PrintingJobStatus {

    private String id;
    private String status;
    private Integer printedSheetCount;
    private Integer totalSheetCount;


    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return this.status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPrintedSheetCount() {
        return this.printedSheetCount;
    }
    public void setPrintedSheetCount(Integer printedSheetCount) {
        this.printedSheetCount = printedSheetCount;
    }

    public Integer getTotalSheetCount() {
        return this.totalSheetCount;
    }
    public void setTotalSheetCount(Integer totalSheetCount) {
        this.totalSheetCount = totalSheetCount;
    }
}
