package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.WFSDataSupport;

/**
 * Comparator for data supports based on their id.
 * @see Integer#compareTo(Integer)
 */
public class WFSDataSupportsByIdOrderingComparator extends AbstractComparator implements Comparator<WFSDataSupport>, java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1117613485555987001L;

	@Override
	public int compare(WFSDataSupport j1, WFSDataSupport j2){
		
		Integer result = super.nullCompare(j1, j2);
		if (result == null){
			if(j1.getDataSupportId() != null && j2.getDataSupportId() != null) 
				result = super.compare(j1.getDataSupportId(), j2.getDataSupportId());
			else{
				result = super.compare(j1.getName(), j2.getName());
				if(result == null || result == 0)
					result = super.compare(j1.getDsType(), j2.getDsType());
			}
		}
		
		return result;
	}	
}			
			