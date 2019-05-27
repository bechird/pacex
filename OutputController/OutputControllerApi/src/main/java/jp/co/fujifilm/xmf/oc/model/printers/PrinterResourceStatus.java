/*
 * Copyright(C) 2016 FUJIFILM Corporation, All rights
 * reserved. This computer program is the property of FUJIFILM
 * Corporation, Japan, and may be used and copied only as specifically
 * permitted under written licence agreement signed by FUJIFILM
 * Corporation.
 */
package jp.co.fujifilm.xmf.oc.model.printers;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class PrinterResourceStatus {

    private InkInfoMap ink;
    private PaperInfo paper;


    public InkInfoMap getInkInfo() {
        return this.ink;
    }
    public void setInkInfo(InkInfoMap v) {
        this.ink = v;
    }
   
    public PaperInfo getPaperInfo() {
        return this.paper;
    }
    public void setPaperInfo(PaperInfo v) {
        this.paper = v;
    }
}
