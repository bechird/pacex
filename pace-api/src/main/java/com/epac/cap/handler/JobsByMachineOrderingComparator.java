package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.Job;

/**
 * Comparator for Jobs on stations; for the overview dashboard
 * The ordering is based on the machine ordering field.
 * @see Integer#compareTo(Integer)
 */
public class JobsByMachineOrderingComparator extends AbstractComparator implements Comparator<Job>, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6809934989381851266L;

	@Override
	public int compare(Job j1, Job j2){
		Integer result = super.nullCompare(j1, j2);
		if (result == null) {
			//result = super.compareForSortedSet(j1.getMachineOrdering(), j2.getMachineOrdering());
			result = super.compare(j1.getMachineOrdering(), j2.getMachineOrdering());
			if(result == 0){
				result = super.compare(j1.getJobId(), j2.getJobId());
			}
		}	
		return result;
	}	
}			
			