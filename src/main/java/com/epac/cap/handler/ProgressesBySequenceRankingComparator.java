package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.WFSProgress;

/**
 * Comparator for progresses on partWorkflow; 
 * The ordering is based on the progress sequence ranking.
 * @see Integer#compareTo(Integer)
 */
public class ProgressesBySequenceRankingComparator extends AbstractComparator implements Comparator<WFSProgress>, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5848808307082392943L;
	@Override
	public int compare(WFSProgress j1, WFSProgress j2){
		Integer result = super.nullCompare(j1, j2);
		if (result == null) {
			result = super.nullCompare(j1.getSequence(), j2.getSequence());
			if (result == null || result == 0) {
				result = super.compare(j1.getSequence().getRanking(), j2.getSequence().getRanking());
			}
		}	
		return result;
	}	
}			
			