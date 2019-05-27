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
public class InkInfo {

    private String status;
    private int remainingAmount;


    public String getStatus(){
        return this.status;
    }
    public void setStatus(String v){
        this.status = v;
    }

    public int getRemainingAmount(){
        return this.remainingAmount;
    }
    public void setRemainingAmount(int v){
        this.remainingAmount = v;
    }
}
