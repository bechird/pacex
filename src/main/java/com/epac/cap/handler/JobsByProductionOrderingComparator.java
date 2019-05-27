package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.Job;

/**
 * Comparator for Jobs by production ordering and split level; 
 * Used when getting order jobs.
 * @see Integer#compareTo(Integer)
 */
public class JobsByProductionOrderingComparator extends AbstractComparator implements Comparator<Job>, java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4750500999038485755L;

	@Override
	public int compare(Job j1, Job j2){
	
		Integer result = super.nullCompare(j1, j2);
		if (result == null) {
			result = super.compare(j1.getProductionOrdering(), j2.getProductionOrdering());
			if(result == 0){
				result = super.compare(j1.getStationId(), j2.getStationId());
				if(result == 0){
					result = super.compare(j1.getSplitLevel(), j2.getSplitLevel());
					if(result == 0){
						result = super.compare(j1.getJobId(), j2.getJobId());
					}
				}
			}
		}	
		return result;//TODO 
	
	}	
}			
			