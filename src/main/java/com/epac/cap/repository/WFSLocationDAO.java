package com.epac.cap.repository;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.epac.cap.model.WFSLocation;
import com.epac.cap.model.WFSProgress;

@Repository
public class WFSLocationDAO extends BaseEntityPersister{
	
	public void save(WFSLocation wfsLocation) {
		getEntityManager().persist(wfsLocation);
		getEntityManager().flush();
	}
	
	public void update(WFSLocation wfsLocation) {
		getEntityManager().merge(wfsLocation);
		getEntityManager().flush();
	}
	
	public WFSLocation getLocation(Integer locationId) {
		return getEntityManager().find(WFSLocation.class, locationId);
	}
	
	public WFSLocation getLocationByDSId(Integer wfsDSId) {
		WFSLocationSearchBean lsb = new WFSLocationSearchBean();
		lsb.setWfsDatasupportId(wfsDSId);
		List<WFSLocation> locs =  readAll(lsb);
		return locs.isEmpty() ? null : locs.get(0);
	}
	
	/** 
	 * read all the beans
	 */	 
	@SuppressWarnings("unchecked")
	public List<WFSLocation> readAll(WFSLocationSearchBean searchBean) {
		Criteria criteria = createCriteria(WFSLocation.class);
		if(searchBean != null){
			super.addAuditableCriteria(criteria, searchBean);
			if(searchBean.getWfsDatasupportId() != null){
				criteria.add(Restrictions.eq("dataSupport.dataSupportId", searchBean.getWfsDatasupportId()));
			}

		}else{
			//instantiate a new search bean for the defaults
			searchBean = new WFSLocationSearchBean();
		}
		if(searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0){
			criteria.setMaxResults(searchBean.getMaxResults());
		}
		addOrderBy(searchBean.getOrderByList(),criteria);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<WFSLocation> l=criteria.list();
		return l;
	}

}
