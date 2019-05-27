package com.epac.cap.repository;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.epac.cap.model.CoverBatch;;

/**
 * Interacts with Cover batch data.  Uses an entity manager for entity persistence.
 *
 * @author slimj
 *
 */
@Repository
public class CoverBatchDAO extends BaseEntityPersister {

	/** 
	 * creates the bean
	 */	 
	public void create(CoverBatch bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * updates the bean
	 */	 
	public void update(CoverBatch bean) {
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * deletes the bean
	 */	 
	public boolean delete(CoverBatch bean) {		
		if (bean != null){
			//need to re-retrieve the instance b/c it was likely retrieved in a separate
			//transaction (during action.prepareDelete()) making it detached now
			getEntityManager().remove(read(bean.getCoverBatchId()));
			getEntityManager().flush();
			return true;
		}else{
			return false;
		}
	}

	/** 
	 * reads the bean by id
	 */	 
	public CoverBatch read(Integer coverBatchId) {
		return getEntityManager().find(CoverBatch.class, coverBatchId);
	}

	/** 
	 * reads all the beans
	 */	 
	@SuppressWarnings("unchecked")
	public List<CoverBatch> readAll(CoverBatchSearchBean searchBean) {
		Criteria criteria = createCriteria(CoverBatch.class);
		if(searchBean != null){
			super.addAuditableCriteria(criteria, searchBean);
			if(searchBean.getCoverBatchId() != null){
				criteria.add(Restrictions.eq("coverBatchId", searchBean.getCoverBatchId()));
			}		
			if(!StringUtils.isBlank(searchBean.getCoverBatchName())){							
				criteria.add(Restrictions.eq("coverBatchName", searchBean.getCoverBatchName()));		
			}
			if(!StringUtils.isBlank(searchBean.getCoverBatchTag())){							
				criteria.add(Restrictions.eq("coverBatchTag", searchBean.getCoverBatchTag()));		
			}
			if(searchBean.getRollId() != null){
				criteria.add(Restrictions.eq("rollId", searchBean.getRollId()));
			}
			if(!StringUtils.isBlank(searchBean.getMachineId())){							
				criteria.add(Restrictions.eq("machineId", searchBean.getMachineId()));
			}	
			if(searchBean.getQuantity() != null){							
				criteria.add(Restrictions.eq("quantity", searchBean.getQuantity()));
			}
			if(!StringUtils.isBlank(searchBean.getPaperType())){							
				criteria.add(Restrictions.eq("paperType", searchBean.getPaperType()));		
			}
			/*if(!StringUtils.isBlank(searchBean.getStatus())){							
				criteria.add(Restrictions.eq("status", searchBean.getStatus()));		
			}
			if(!StringUtils.isBlank(searchBean.getLaminationType())){							
				criteria.add(Restrictions.eq("laminationType", searchBean.getLaminationType()));		
			}*/
			if(!StringUtils.isBlank(searchBean.getPriority())){							
				criteria.add(Restrictions.eq("priority", searchBean.getPriority()));
			}	
		}else{
			searchBean = new CoverBatchSearchBean();
		}
		if(searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0){
			criteria.setMaxResults(searchBean.getMaxResults());
		}

		addOrderBy(searchBean.getOrderByList(),criteria);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<CoverBatch> l=criteria.list();
		return l;
	}
	
}
