package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.PNLTemplateLine;

/**
 * Comparator for PNL Template Lines 
 * The ordering is based on the ordering field.
 * @see Integer#compareTo(Integer)
 */
public class PNLTemplateLinesPerOrderingComparator extends AbstractComparator implements Comparator<PNLTemplateLine>, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4449734464068728934L;

	@Override
	public int compare(PNLTemplateLine r1, PNLTemplateLine r2){
		Integer result = super.nullCompare(r1, r2);
		if (result == null) {
			result = super.compare(r1.getOrdering(), r2.getOrdering());
			if(result == 0){
				result = super.compare(r1.getId(), r2.getId());
			}
		}
		return result;
	}	
}			
			