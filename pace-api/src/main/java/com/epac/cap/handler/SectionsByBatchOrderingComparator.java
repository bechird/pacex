package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.CoverSection;

/**
 * Comparator for sections on Batch; The ordering is based on the sectionId
 * ordering field.
 * 
 * @see Integer#compareTo(Integer)
 */
public class SectionsByBatchOrderingComparator extends AbstractComparator
		implements Comparator<CoverSection>, java.io.Serializable {

	private static final long serialVersionUID = 6272873421048306821L;

	@Override
	public int compare(CoverSection s1, CoverSection s2) {
		Integer result = super.nullCompare(s1, s2);
		if (result == null) {
			if (s1.getCoverSectionId() != null || s2.getCoverSectionId() != null) {
				result = super.compare(s1.getCoverSectionId(), s2.getCoverSectionId());
			} else {
				result = super.compare(s1.getCoverSectionName(), s2.getCoverSectionName());
			}
		}
		return result;

	}
}
