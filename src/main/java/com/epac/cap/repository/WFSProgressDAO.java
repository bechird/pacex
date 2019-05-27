package com.epac.cap.repository;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.epac.cap.model.Part;
import com.epac.cap.model.WFSProgress;

@Repository
public class WFSProgressDAO extends BaseEntityPersister{
	
	public void save(WFSProgress wfsProgress) {
		getEntityManager().persist(wfsProgress);
		getEntityManager().flush();
	}
	
	public void update(WFSProgress wfsProgress) {
		getEntityManager().merge(wfsProgress);
		getEntityManager().flush();
	}
	
	public WFSProgress getProgress(Integer progressId) {
		return getEntityManager().find(WFSProgress.class, progressId);
	}
	
	/*public List<WFSProgress> findByPartNumb(String partNb) {
		ProgressSearchBean searchBean = new ProgressSearchBean();
		searchBean.setPartNumb(partNb);
		return readAll(searchBean);
	}*/
	
	public WFSProgress findBySequenceId(ProgressSearchBean searchBean) {
		List<WFSProgress> progress =  readAll(searchBean);
		if (!progress.isEmpty())
			return progress.get(0);
		else return null;
	}
	
	/** 
	 * read all the beans
	 */	 
	@SuppressWarnings("unchecked")
	public List<WFSProgress> readAll(ProgressSearchBean searchBean) {
		Criteria criteria = createCriteria(WFSProgress.class);
		if(searchBean != null){
			super.addAuditableCriteria(criteria, searchBean);
			if(searchBean.getProgressId() != null){
				criteria.add(Restrictions.eq("progressId", searchBean.getStatusId()));
			}
			/*if(!StringUtils.isBlank(searchBean.getPartNumb())){
				criteria.add(Restrictions.eq("partNumb", searchBean.getPartNumb()));					
			}*/
			if(searchBean.getStatusId() != null){
				criteria.add(Restrictions.eq("statusId", searchBean.getStatusId()));
			}
			if(searchBean.getSequenceId() != null){
				criteria.add(Restrictions.eq("sequenceId", searchBean.getSequenceId()));
			}
			if(searchBean.getStatus() != null){
				criteria.add(Restrictions.ilike("status", searchBean.getStatus()));
			}

		}else{
			//instantiate a new search bean for the defaults
			searchBean = new ProgressSearchBean();
		}
		if(searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0){
			criteria.setMaxResults(searchBean.getMaxResults());
		}
		addOrderBy(searchBean.getOrderByList(),criteria);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<WFSProgress> l=criteria.list();
		return l;
	}

}
