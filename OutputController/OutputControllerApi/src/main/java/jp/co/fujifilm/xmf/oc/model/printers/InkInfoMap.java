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
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class InkInfoMap {

    @XmlElement(name="c")
    private InkInfo cyan;

    @XmlElement(name="m")
    private InkInfo magenta;

    @XmlElement(name="y")
    private InkInfo yellow;

    @XmlElement(name="k")
    private InkInfo black;


    public InkInfo getCyanInfo() {
        return this.cyan;
    }
    public void setCyanInfo(InkInfo v) {
        this.cyan = v;
    }

    public InkInfo getMagentaInfo() {
        return this.magenta;
    }
    public void setMagentaInfo(InkInfo v) {
        this.magenta = v;
    }

    public InkInfo getYellowInfo() {
        return this.yellow;
    }
    public void setYellowInfo(InkInfo v) {
        this.yellow = v;
    }

    public InkInfo getBlackInfo() {
        return this.black;
    }
    public void setBlackInfo(InkInfo v) {
        this.black = v;
    }
}
