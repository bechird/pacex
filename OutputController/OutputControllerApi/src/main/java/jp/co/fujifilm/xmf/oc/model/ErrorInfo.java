/*
 * Copyright(C) 2016 FUJIFILM Corporation, All rights
 * reserved. This computer program is the property of FUJIFILM
 * Corporation, Japan, and may be used and copied only as specifically
 * permitted under written licence agreement signed by FUJIFILM
 * Corporation.
 */
package jp.co.fujifilm.xmf.oc.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorInfo {
    private int code;
    private String message;


    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
