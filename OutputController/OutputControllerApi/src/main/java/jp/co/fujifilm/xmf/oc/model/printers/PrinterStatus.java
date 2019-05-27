/*
 * Copyright(C) 2016 FUJIFILM Corporation, All rights
 * reserved. This computer program is the property of FUJIFILM
 * Corporation, Japan, and may be used and copied only as specifically
 * permitted under written licence agreement signed by FUJIFILM
 * Corporation.
 */
package jp.co.fujifilm.xmf.oc.model.printers;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import jp.co.fujifilm.xmf.oc.model.ErrorInfo;

@XmlRootElement(name="PrinterStatus")
@XmlAccessorType(XmlAccessType.FIELD)
public class PrinterStatus {

    @XmlElement(name="status")
    private String recorderStatus;

    @XmlElement(name="resource")
    private PrinterResourceStatus recorderResourceStatus;

    @XmlElementWrapper(name="errorList")
    @XmlElement(name="error")
    private List<ErrorInfo> errorInfoList;


    public String getRecorderStatus() {
        return this.recorderStatus;
    }
    public void setRecorderStatus(String v) {
        this.recorderStatus = v;
    }

    public PrinterResourceStatus getRecorderResourceStatus() {
        return this.recorderResourceStatus;
    }
    public void setRecorderResourceStatus(PrinterResourceStatus v) {
        this.recorderResourceStatus = v;
    }

    public List<ErrorInfo> getErrorInfoList() {
        return this.errorInfoList;
    }
    public void setErrorInfoList(List<ErrorInfo> v) {
        this.errorInfoList = v;
    }
}
