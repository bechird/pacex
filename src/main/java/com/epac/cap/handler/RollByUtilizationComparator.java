package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.Roll;

/**
 * Comparator for Rolls during scheduling; mainly by utilization value descending
 * @see Integer#compareTo(Integer)
 */
public class RollByUtilizationComparator extends AbstractComparator implements Comparator<Roll>, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5705708884321270727L;

	@Override
	public int compare(Roll r1, Roll r2){
		Integer result = super.nullCompare(r1, r2);
		if (result == null) {
			result = -1 * super.compare(r1.getUtilization(), r2.getUtilization());
			if (result == 0) {
				result = super.nullCompare(r1.getMachineId(), r2.getMachineId());
				if (result == null || result == 0) {
					result = super.nullCompare(r1.getRollType(), r2.getRollType());
					if(result == null || result == 0){
						result = super.compare(r1.getRollType().getId(), r2.getRollType().getId());
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
