package com.epac.cap.repository;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import com.epac.cap.model.CoverBatchJob;

/**
 * Interacts with Cover batch JOB data.  Uses an entity manager for entity persistence.
 *
 * @author slimj
 *
 */
@Repository
public class CoverBatchJobDAO extends BaseEntityPersister {

	/** 
	 * creates the bean
	 */	 
	public void create(CoverBatchJob bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * updates the bean
	 */	 
	public void update(CoverBatchJob bean) {
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * deletes the bean
	 */	 
	public boolean delete(CoverBatchJob bean) {		
		if (bean != null){
			//need to re-retrieve the instance b/c it was likely retrieved in a separate
			//transaction (during action.prepareDelete()) making it detached now
			getEntityManager().remove(read(bean.getId()));
			getEntityManager().flush();
			return true;
		}else{
			return false;
		}
	}

	/** 
	 * reads the bean by id
	 */	 
	public CoverBatchJob read(Integer coverBatchJobId) {
		return getEntityManager().find(CoverBatchJob.class, coverBatchJobId);
	}

	/** 
	 * reads all the beans
	 */	 
	@SuppressWarnings("unchecked")
	public List<CoverBatchJob> readAll(CoverBatchJobSearchBean searchBean) {
		Criteria criteria = createCriteria(CoverBatchJob.class);
		if(searchBean != null){
			super.addAuditableCriteria(criteria, searchBean);
			if(searchBean.getId() != null){
				criteria.add(Restrictions.eq("id", searchBean.getId()));
			}
		}else{
			searchBean = new CoverBatchJobSearchBean();
		}
		if(searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0){
			criteria.setMaxResults(searchBean.getMaxResults());
		}

		addOrderBy(searchBean.getOrderByList(),criteria);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<CoverBatchJob> l=criteria.list();
		return l;
	}
	
}
