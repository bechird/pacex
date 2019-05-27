/*
 * Copyright(C) 2016 FUJIFILM Corporation, All rights
 * reserved. This computer program is the property of FUJIFILM
 * Corporation, Japan, and may be used and copied only as specifically
 * permitted under written licence agreement signed by FUJIFILM
 * Corporation.
 */
package jp.co.fujifilm.xmf.oc.model.jobs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * ジョブ情報クラス
 * Job data class
 */
@XmlRootElement(name = "Job")
@XmlAccessorType(XmlAccessType.FIELD)

public class Job {
	
	private String id;

	private double sheetWidth;

	private double sheetHeight;

	private Integer numColors;

	private Integer numSheets;

	public Job() {}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@JsonSerialize(using=InchValueSerializer.class)
	public double getSheetWidth() {
		return sheetWidth;
	}

	public void setSheetWidth(double sheetWidth) {
		this.sheetWidth = sheetWidth;
	}
	
	
	@JsonSerialize(using=InchValueSerializer.class)
	public double getSheetHeight() {
		return sheetHeight;
	}

	public void setSheetHeight(double sheetHeight) {
		this.sheetHeight = sheetHeight;
	}

	public Integer getNumColors() {
		return numColors;
	}

	public void setNumColors(Integer numColors) {
		this.numColors = numColors;
	}

	public Integer getNumSheets() {
		return numSheets;
	}

	public void setNumSheets(Integer numSheets) {
		this.numSheets = numSheets;
	}

}
