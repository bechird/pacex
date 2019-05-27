package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.LoadTag;

/**
 * Comparator for LoadTags on machines by chronological time;
 * The ordering is based on the time the load tag was created
 * @see Integer#compareTo(Integer)
 */
public class LoadTagsByChronologicalOrderingComparator extends AbstractComparator implements Comparator<LoadTag>, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8995677315386097705L;

	@Override
	public int compare(LoadTag j1, LoadTag j2){
		Integer result = super.nullCompare(j1, j2);
		if (result == null) {
			//result = -1 * super.compareForSortedSet(j1.getCreatedDate(), j2.getCreatedDate());
			result = -1 * super.compare(j1.getCreatedDate(), j2.getCreatedDate());
			if(result == 0){
				result = -1 * super.compare(j1.getLoadTagId(), j2.getLoadTagId());
			}
		}	
		return result;
	}	
}			
			