package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.Roll;

/**
 * Comparator for Rolls on stations; for the overview dashboard
 * The ordering is based on the machine ordering field.
 * @see Integer#compareTo(Integer)
 */
public class RollsByMachineOrderingComparator extends AbstractComparator implements Comparator<Roll>, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4449734464068728934L;

	@Override
	public int compare(Roll r1, Roll r2){
		Integer result = super.nullCompare(r1, r2);
		if (result == null) {
			//result = super.compareForSortedSet(r1.getMachineOrdering(), r2.getMachineOrdering());
			result = super.compare(r1.getMachineOrdering(), r2.getMachineOrdering());
			if(result == 0){
				result = super.compare(r1.getRollId(), r2.getRollId());
			}
		}
		return result;
	}	
}			
			