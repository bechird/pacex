package com.epac.owd.service;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.epac.cap.model.PaperType;

public class PaperTypes {
	
    @XmlElement(name="PaperType")
    private List<PaperType> papertypeList;

	public List<PaperType> getPapertypeList() {
		return papertypeList;
	}

	public void setPapertypeList(List<PaperType> papertypeList) {
		this.papertypeList = papertypeList;
	}
 

}