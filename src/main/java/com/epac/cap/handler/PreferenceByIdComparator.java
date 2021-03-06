package com.epac.cap.handler;

import java.util.Comparator;
import com.epac.cap.model.LookupItem;

/**
 * Comparator for preference lists by id.
 * @see Integer#compareTo(Integer)
 */
public class PreferenceByIdComparator<L extends LookupItem> extends AbstractComparator implements Comparator<L>, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3241159111670706035L;

	@Override
	public int compare(L p1, L p2){
		Integer result = super.nullCompare(p1, p2);
		if (result == null) {
			result = super.compare(p1.getId(), p2.getId());
		}	
		return result;
	}	
}			
			