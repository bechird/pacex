package com.epac.cap.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.epac.cap.common.OrderBy;

public class AoData {
	

	private Integer ajaxRequestId;
	private Integer startIndex;
	private Integer pageLength;
	private String generalFilter;
	
	private Integer sortingColumnIndex;
	private String sortingDirection;
	private String sortingColumnName;
	
	
	private List<String> columnsNames = new LinkedList<String>();
	private Map<String, String> columnsFilters = new HashMap<String, String>();

	
	public Integer getAjaxRequestId() {
		return ajaxRequestId;
	}
	public void setAjaxRequestId(Integer ajaxRequestId) {
		this.ajaxRequestId = ajaxRequestId;
	}
	public Integer getStartIndex() {
		return startIndex;
	}
	public void setStartIndex(Integer startIndex) {
		this.startIndex = startIndex;
	}
	public Integer getPageLength() {
		return pageLength;
	}
	public void setPageLength(Integer pageLength) {
		this.pageLength = pageLength;
	}
	public String getGeneralFilter() {
		return generalFilter;
	}
	public void setGeneralFilter(String generalFilter) {
		this.generalFilter = generalFilter;
	}
	public Integer getSortingColumnIndex() {
		return sortingColumnIndex;
	}
	public void setSortingColumnIndex(Integer sortingColumnIndex) {
		this.sortingColumnIndex = sortingColumnIndex;
	}
	public String getSortingDirection() {
		return sortingDirection;
	}
	public void setSortingDirection(String sortingDirection) {
		this.sortingDirection = sortingDirection;
	}
	public String getSortingColumnName() {
		try {
			return columnsNames.get(this.sortingColumnIndex);
		} catch (Exception e) {
			return null;
		}
	}
	public void setSortingColumnName(String sortingColumnName) {
		this.sortingColumnName = sortingColumnName;
	}
	public List<String> getColumnsNames() {
		return columnsNames;
	}
	public void setColumnsNames(List<String> columnsNames) {
		this.columnsNames = columnsNames;
	}
	public Map<String, String> getColumnsFilters() {
		return columnsFilters;
	}
	public void setColumnsFilters(Map<String, String> columnsFilters) {
		this.columnsFilters = columnsFilters;
	}
	
	
	public boolean isGeneralFiltering() {
		if(generalFilter != null && !generalFilter.isEmpty()) {
			return true;
		}else {
			return false;
		}
	}
	public boolean isColumnsFiltering() {
		if( columnsFilters.size() > 0 ) {
			return true;
		}else {
			return false;
		}
	}
	
	public String getOrderBy() {
		if(sortingDirection.equals("asc"))return OrderBy.ASC;
		if(sortingDirection.equals("desc"))return OrderBy.DESC;		
		return null;
	}
	
	
	
}
