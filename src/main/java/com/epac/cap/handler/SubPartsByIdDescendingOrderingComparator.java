package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.SubPart;

/**
 * Comparator for sub parts of a part; by id name descending (so that Text parts come before Cover parts; this is how production ordering is set)
 * @see Integer#compareTo(Integer)
 */
public class SubPartsByIdDescendingOrderingComparator extends AbstractComparator implements Comparator<SubPart>, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5515897655466685283L;

	@Override
	public int compare(SubPart r1, SubPart r2){
		Integer result = super.nullCompare(r1, r2);
		if (result == null) {
			result = -1 * super.compare(r1.getId().getSubPartNum(), r2.getId().getSubPartNum());
			if(result == 0){
				result = -1 * r1.compareTo(r2);
			}
		}	
		//return result == 0 ? 1 : result;  to avoid excluding elements from the sortedset that holds the list of elements...
		return result;
	}	
}			
			