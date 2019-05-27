package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.Machine;
import com.epac.cap.model.StationCategory;

/**
 * Comparator for machines on stations; by name descending (so that 4C machines show before 1C for press; for the rest order alphabetically ascending)
 * @see Integer#compareTo(Integer)
 */
public class MachinesByNameOrderingComparator extends AbstractComparator implements Comparator<Machine>, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8969591430740258824L;

	@Override
	public int compare(Machine r1, Machine r2){
		Integer result = super.nullCompare(r1, r2);
		if (result == null) {
			int cmp = StationCategory.Categories.PRESS.toString().equals(r1.getStationId()) ? -1: 1;
			result = cmp * super.compare(r1.getName(), r2.getName());
			if(result == 0){
				result = cmp * super.compare(r1.getMachineId(), r2.getMachineId());
			}
		}	
		return result;
	}	
}			
			