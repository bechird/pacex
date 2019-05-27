package com.epac.cap.handler;

import java.util.Comparator;
import com.epac.cap.model.Part;

/**
 * Comparator for searched/retrieved parts to assign to order.
 * @see Integer#compareTo(Integer)
 */
public class PartsSearchComparator extends AbstractComparator implements Comparator<Part>, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7632360135029881213L;

	@Override
	public int compare(Part p1, Part p2){
		Integer result = super.nullCompare(p1, p2);
		if (result == null) {
			result = -1 * super.compare(p1.getLastPrinted(), p2.getLastPrinted());
			if(result == 0){
				result = super.compare(p1.getIsbn(), p2.getIsbn());
				if(result == 0){
					result = super.compare(p1.getTitle(), p2.getTitle());
					if(result == 0){
						result = super.compare(p1.getAuthor(), p2.getAuthor());
						if(result == 0){
							result = -1 * super.compare(p1.getVersion(), p2.getVersion());
							if(result == 0){
								result = super.compare(p1.getPartNum(), p2.getPartNum());
							}
						}
					}
				}
			}
		}	
		return result;
	}	
}			
			