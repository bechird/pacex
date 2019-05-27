package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.CoverBatch;
import com.epac.cap.model.CoverSection;

/**
 * Comparator for sections on cover station; for the overview dashboard
 * The ordering is based on the sectionId field.
 * @see Integer#compareTo(Integer)
 */
public class SectionsByMachineOrderingComparator extends AbstractComparator implements Comparator<CoverSection>, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5334371095682423925L;


	@Override
	public int compare(CoverSection cb1, CoverSection cb2){
		Integer result = super.nullCompare(cb1, cb2);
		if (result == null) {
			result = super.compare(cb1.getCoverSectionId(), cb2.getCoverSectionId());
		}	
		return result;
	}	
}			
			