package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.Job;

/**
 * Comparator for Jobs on rolls; 
 * The ordering is based on the roll ordering field.
 * @see Integer#compareTo(Integer)
 */
public class JobsByRollOrderingComparator extends AbstractComparator implements Comparator<Job>, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2893266305685446764L;

	@Override
	public int compare(Job j1, Job j2){
		/*Integer result = super.nullCompare(j1, j2);
		if (result == null) {
			if(j1.equals(j2)){
				result = 0;
			}else{
				result = super.compareForSortedSet(j1.getRollOrdering(), j2.getRollOrdering());
			}
		}	
		return result;//TODO */
		
		Integer result = super.nullCompare(j1, j2);
		if (result == null) {
			result = super.compare(j1.getRollOrdering(), j2.getRollOrdering());
			if(result == 0){
				result = super.compare(j1.getJobId(), j2.getJobId());
			}
		}	
		return result;//TODO 
	
	}	
}			
			