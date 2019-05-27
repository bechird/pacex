package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.LookupItem;
import com.epac.cap.model.Preference;

/**
 * Comparator for preference lists by group index.
 * @see Integer#compareTo(Integer)
 */
public class PreferenceByGroupingComparator<L extends LookupItem> extends AbstractComparator implements Comparator<L>, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3241159111670706035L;

	@Override
	public int compare(L p1, L p2){
		Integer result = super.nullCompare(p1, p2);
		if (result == null) {
			result = super.compare(((Preference) p1).getGroupingValue(), ((Preference) p2).getGroupingValue());
			if(result == 0){
				result = super.compare(p1.getId(), p2.getId());
			}
		}	
		return result;
	}	
}			
			