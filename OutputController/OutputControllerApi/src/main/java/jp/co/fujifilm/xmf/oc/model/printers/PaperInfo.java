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
public class PaperInfo {

    private int feedLength;

    private int width;

    private int remainingLength;


    public int getFeedLength() {
        return this.feedLength;
    }
    public void setFeedLength(int v) {
        this.feedLength = v;
    }
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getRemainingLength() {
		return remainingLength;
	}
	public void setRemainingLength(int remainingLength) {
		this.remainingLength = remainingLength;
	}
}
