package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.Log;

/**
 * Comparator for Logs on stations by chronological time;
 * The ordering is based on the time the log was created
 * @see Integer#compareTo(Integer)
 */
public class LogsByChronologicalOrderingComparator extends AbstractComparator implements Comparator<Log>, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3864434338492356930L;

	@Override
	public int compare(Log j1, Log j2){
		Integer result = super.nullCompare(j1, j2);
		if (result == null) {
			//result = -1 * super.compareForSortedSet(j1.getCreatedDate(), j2.getCreatedDate());
			result = -1 * super.compare(j1.getCreatedDate(), j2.getCreatedDate());
			if(result == 0){
				//result = -1 * super.compareForSortedSet(j1.getLogId(), j2.getLogId());
				result = -1 * super.compare(j1.getLogId(), j2.getLogId());
			}
		}	
		return result;
	}	
}			
			