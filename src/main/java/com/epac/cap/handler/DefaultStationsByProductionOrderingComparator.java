package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.DefaultStation;

/**
 * Comparator for default stations on part categories; 
 * The ordering is based on the production ordering field.
 * @see Integer#compareTo(Integer)
 */
public class DefaultStationsByProductionOrderingComparator extends AbstractComparator implements Comparator<DefaultStation>, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1589738172001933464L;

	@Override
	public int compare(DefaultStation j1, DefaultStation j2){
		Integer result = super.nullCompare(j1, j2);
		if (result == null) {
			//result = super.compareForSortedSet(j1.getProductionOrdering(), j2.getProductionOrdering());
			result = super.compare(j1.getProductionOrdering(), j2.getProductionOrdering());
			if(result == 0){
				result = j1.compareTo(j2);
			}
		}
		return result;
	}	
}			
			