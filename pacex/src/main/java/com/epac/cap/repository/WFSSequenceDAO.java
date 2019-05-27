package com.epac.cap.repository;

import java.util.List;

import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.epac.cap.model.WFSSequence;

@Repository
public class WFSSequenceDAO extends BaseEntityPersister {

	public void save(WFSSequence wfsSequence) {
		getEntityManager().persist(wfsSequence);
		getEntityManager().flush();
	}

	public void update(WFSSequence wfsSequence) {
		getEntityManager().merge(wfsSequence);
		getEntityManager().flush();
	}

	public WFSSequence getSequence(Integer sequenceId) {
		return getEntityManager().find(WFSSequence.class, sequenceId);
	}

	public List<WFSSequence> findByWorkflowId(Integer workflowId) {
		SequenceSearchBean searchBean = new SequenceSearchBean();
		searchBean.setWorkflowId(workflowId);
		return readAll(searchBean);
	}

	public List<WFSSequence> findByActionId(Integer actionId) {
		SequenceSearchBean searchBean = new SequenceSearchBean();
		searchBean.setWorkflowId(actionId);
		return readAll(searchBean);
	}

	public List<WFSSequence> findByProgressId(Integer progressId) {
		SequenceSearchBean searchBean = new SequenceSearchBean();
		searchBean.setWorkflowId(progressId);
		return readAll(searchBean);
	}

	/**
	 * read all the beans
	 */
	@SuppressWarnings("unchecked")
	public List<WFSSequence> readAll(SequenceSearchBean searchBean) {
		Criteria criteria = createCriteria(WFSSequence.class);
		if (searchBean != null) {
			super.addAuditableCriteria(criteria, searchBean);
			if (searchBean.getSequenceId() != null) {
				criteria.add(Restrictions.eq("sequenceId", searchBean.getSequenceId()));
			}
			if (searchBean.getWorkflowId() != null) {
				criteria.add(Restrictions.eq("workflowId", searchBean.getWorkflowId()));
			}
			if (searchBean.getActionId() != null) {
				criteria.add(Restrictions.eq("actionId", searchBean.getActionId()));
			}
			if (searchBean.getProgressId() != null) {
				criteria.add(Restrictions.eq("progressId", searchBean.getProgressId()));
			}
			if (searchBean.getRanking() != null) {
				criteria.add(Restrictions.eq("ranking", searchBean.getRanking()));
			}

		} else {
			// instantiate a new search bean for the defaults
			searchBean = new SequenceSearchBean();
		}
		// if(searchBean.getMaxResults() != null && searchBean.getMaxResults() >
		// 0){
		// criteria.setMaxResults(searchBean.getMaxResults());
		// }
		// addOrderBy(searchBean.getOrderByList(),criteria);
		// criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		List<WFSSequence> l = criteria.list();
		return l;
	}

	

}
