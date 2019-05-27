package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.CoverBatch;

/**
 * Comparator for Batches on rolls; 
 * @see Integer#compareTo(Integer)
 */
public class BatchesByRollOrderingComparator extends AbstractComparator implements Comparator<CoverBatch>, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7100195636049943856L;

	@Override
	public int compare(CoverBatch b1, CoverBatch b2){		
		Integer result = super.nullCompare(b1, b2);
		if (result == null) {
				result = super.compare(b1.getCoverBatchId(), b2.getCoverBatchId());
		}	
		return result; 
	
	}	
}			
			