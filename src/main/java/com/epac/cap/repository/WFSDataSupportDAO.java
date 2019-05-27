package com.epac.cap.repository;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.epac.cap.model.WFSDataSupport;
import com.epac.cap.model.WFSLocation;

@Repository
public class WFSDataSupportDAO extends BaseEntityPersister{
	
	public void save(WFSDataSupport dataSupport) {
		getEntityManager().persist(dataSupport);
		getEntityManager().flush();
	}
	
	public void update(WFSDataSupport dataSupport) {
		getEntityManager().merge(dataSupport);
		getEntityManager().flush();
	}
	
	public void remove(WFSDataSupport dataSupport) {
		getEntityManager().remove(dataSupport);
		getEntityManager().flush();
	}
	
	public WFSDataSupport get(Integer dataSupportId) {
		return getEntityManager().find(WFSDataSupport.class, dataSupportId);
	}
	
	/** 
	 * read all the beans
	 */	 
	@SuppressWarnings("unchecked")
	public List<WFSDataSupport> readAll(WFSDataSupportSearchBean searchBean) {
		Criteria criteria = createCriteria(WFSDataSupport.class);
		if(searchBean != null){
			super.addAuditableCriteria(criteria, searchBean);
			if(!StringUtils.isBlank(searchBean.getPartNum() )){
				criteria.add(Restrictions.eq("partNumb", searchBean.getPartNum()));
			}
			if(!StringUtils.isBlank(searchBean.getDsName())){
				criteria.add(Restrictions.eq("name", searchBean.getDsName()));
			}
			if(!StringUtils.isBlank(searchBean.getProductionStatusId())){
				criteria.add(Restrictions.eq("productionStatus.id", searchBean.getProductionStatusId()));
			}
		}else{
			//instantiate a new search bean for the defaults
			searchBean = new WFSDataSupportSearchBean();
		}
		if(searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0){
			criteria.setMaxResults(searchBean.getMaxResults());
		}
		addOrderBy(searchBean.getOrderByList(),criteria);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<WFSDataSupport> l=criteria.list();
		return l;
	}

	
}
