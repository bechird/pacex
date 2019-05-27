package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.Roll;

/**
 * Comparator for Rolls during scheduling; A roll on a machine is prioritized over another that is not installed on a machine.
 * Also a left over roll is more prioritized over a new roll.
 * And the bigger the Utilization on the roll, the more it is closer to go for production...
 * We also add the paper type to group rolls that have same paper type
 * @see Integer#compareTo(Integer)
 */
public class RollSchedulingComparator extends AbstractComparator implements Comparator<Roll>, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8345083608991534520L;

	@Override
	public int compare(Roll r1, Roll r2){
		Integer result = super.nullCompare(r1, r2);
		if (result == null) {
			result = super.nullCompare(r1.getMachineId(), r2.getMachineId());
			if (result == null || result == 0) {
				result = super.nullCompare(r1.getRollType(), r2.getRollType());
				if(result == null || result == 0){
					result = super.compare(r1.getRollType().getId(), r2.getRollType().getId());
					if (result == 0) {
						result = super.compare(r1.getUtilization(), r2.getUtilization());
						if (result == 0) {
							result = super.nullCompare(r1.getPaperType(), r2.getPaperType());
							if (result == null || result == 0) {
								result = super.compare(r1.getPaperType().getId(), r2.getPaperType().getId());
								if(result == 0){
									//result = super.compareForSortedSet(r1.getRollId(), r2.getRollId());
									result = super.compare(r1.getRollId(), r2.getRollId());
								}
							}
						}
					}
				}
			}
		}
		return result;
	}
}
