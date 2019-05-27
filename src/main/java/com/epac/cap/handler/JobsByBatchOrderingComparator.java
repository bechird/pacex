package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.CoverBatchJob;

/**
 * Comparator for Jobs on Batches; 
 * The ordering is based on the Batch ordering field.
 * @see Integer#compareTo(Integer)
 */
public class JobsByBatchOrderingComparator extends AbstractComparator implements Comparator<CoverBatchJob>, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4335405429622302112L;

	/**
	 * 
	 */
	@Override
	public int compare(CoverBatchJob j1, CoverBatchJob j2) {
		Integer result = super.nullCompare(j1, j2);
		if (result == null) {
			result = super.compare(j1.getJob().getJobId(), j2.getJob().getJobId());
			result = -1 * result;
		}
		return result;// TODO

	}
}
	