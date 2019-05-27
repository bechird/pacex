/*
 * Copyright(C) 2016 FUJIFILM Corporation, All rights
 * reserved. This computer program is the property of FUJIFILM
 * Corporation, Japan, and may be used and copied only as specifically
 * permitted under written licence agreement signed by FUJIFILM
 * Corporation.
 */
package jp.co.fujifilm.xmf.oc.model.printing;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ErrorList")
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorList {

    @XmlElement(name = "error")
    private List<Error> errorList;

	public List<Error> getErrorList() {
		return errorList;
	}

	public void setErrorList(List<Error> errorList) {
		this.errorList = errorList;
	}

	public void setStatusAll(int statusCode) {
		Iterator<Error> itr = this.errorList.iterator();
		while (itr.hasNext()) {
			Error error = (Error) itr.next();
			error.setStatus(statusCode);
		}
	}

}

