package com.epac.cap.handler;

import java.util.Comparator;

import com.epac.cap.model.Job;

/**
 * Comparator for Jobs during scheduling; A job with closer due date is prioritized over another that is farther due.
 * If the job is not linked to an order, so probably it is a service job so has higher priority
 * Also it depends on the priority of the job; 
 * And the binder priority...
 * We also add the paper type to group jobs that require same paper type
 * @see Integer#compareTo(Integer)
 * may need to add at the end ordering by orderId to group split ones and also by highest split level as those are split jobs that need to be processed first
 * also for 4C jobs, 1C jobs should come down the list
 */
public class JobSchedulingComparator extends AbstractComparator implements Comparator<Job>, java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1813710669767888731L;

	@Override
	public int compare(Job j1, Job j2){
		Integer result = super.nullCompare(j1, j2);
		if (result == null) {
			if(j1.getJobPriority().getId().equals("HIGH**") || j1.getJobPriority().getId().equals("HIGH*") ||
			   j2.getJobPriority().getId().equals("HIGH**") || j2.getJobPriority().getId().equals("HIGH*")){
				if(j1.getJobPriority().getId().equals("HIGH**") && !j2.getJobPriority().getId().equals("HIGH**")){
					return -1;
				}
				if(j2.getJobPriority().getId().equals("HIGH**") && !j1.getJobPriority().getId().equals("HIGH**")){
					return 1;
				}
				if(j1.getJobPriority().getId().equals("HIGH*") && !j2.getJobPriority().getId().equals("HIGH*")){
					return -1;
				}
				if(j2.getJobPriority().getId().equals("HIGH*") && !j1.getJobPriority().getId().equals("HIGH*")){
					return 1;
				}
			}
			result = super.compare(j1.getDueDate(), j2.getDueDate());
			if (result == 0) {
				result = super.nullCompare(j1.getJobPriority(), j2.getJobPriority());
				if (result == null || result == 0) {
					result = super.compare(j1.getJobPriority().getId(), j2.getJobPriority().getId());
					if (result == 0) {
						result = super.nullCompare(j1.getBinderyPriority(), j2.getBinderyPriority());
						if(result == null || result == 0){
							result = super.compare(j1.getBinderyPriority().getId(), j2.getBinderyPriority().getId());
							if (result == 0) {
								result = -1 * super.compare(j1.getPartColor(), j2.getPartColor());
								if (result == 0) {
									result = super.compare(j1.getOrderId(), j2.getOrderId());
									if (result == 0) {
										result = -1 * super.compare(j1.getSplitLevel(), j2.getSplitLevel());
										if (result == 0) {
											if(j1.getPartPaperId() != null && j2.getPartPaperId() != null){
												result = super.compare(j1.getPartPaperId(), j2.getPartPaperId());
												if (result == 0) {
													result = super.compare(j1.getJobId(), j2.getJobId());
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		if(result == 0){
			result = super.compare(j1.getJobId(), j2.getJobId());
		}
		return result;
	}
}
