package com.epac.cap.repository;

import com.epac.cap.model.*;
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.apache.commons.lang.StringUtils;
import java.util.List;

/**
 * Implementation class for the RoleDAO interface. 
 * Interacts with Role data.  Uses an entity manager for entity persistence.
 *
 * @author walid
 *
 */
@Repository
public class RoleDAO extends BaseEntityPersister {

	/** 
	 * create the bean
	 */	 
	public void create(Role bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * update the bean 
	 */	 
	public void update(Role bean) {
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * delete the bean
	 */	 
	public boolean delete(Role bean) {		
		if (bean != null){
			//need to re-retrieve the instance b/c it was likely retrieved in a separate
			//transaction (during action.prepareDelete()) making it detached now
			getEntityManager().remove(read(bean.getRoleId()));
			getEntityManager().flush();
			return true;
		}else{
			return false;
		}
	}

	/** 
	 *read the bean with id
	 */	 
	public Role read(String roleId) {
		return getEntityManager().find(Role.class, roleId);
	}

	/** 
	 *read all with criteria
	 */	 
	@SuppressWarnings("unchecked")
	public List<Role> readAll(RoleSearchBean searchBean) {
		Criteria criteria = createCriteria(Role.class);
		//criteria.createAlias("userRoles", "userRoles", CriteriaSpecification.LEFT_JOIN);
		if(searchBean != null){
			super.addAuditableCriteria(criteria, searchBean);
			if(!StringUtils.isBlank(searchBean.getRoleId())){
				criteria.add(Restrictions.eq("roleId", searchBean.getRoleId()).ignoreCase());					
			}
			if(!StringUtils.isBlank(searchBean.getRoleIdDiff())){
				criteria.add(Restrictions.ne("roleId", searchBean.getRoleIdDiff()).ignoreCase());					
			}
			if(!StringUtils.isBlank(searchBean.getRoleName())){							
				criteria.add(Restrictions.ilike("roleName", searchBean.getRoleName(), MatchMode.ANYWHERE));		
			}
			if(!StringUtils.isBlank(searchBean.getRoleNameExact())){							
				criteria.add(Restrictions.eq("roleName", searchBean.getRoleNameExact()).ignoreCase());		
			}
			if(!StringUtils.isBlank(searchBean.getRoleDescription())){							
				criteria.add(Restrictions.ilike("roleDescription", searchBean.getRoleDescription(), MatchMode.ANYWHERE));		
			}
		}else{
			//instantiate a new search bean for the defaults
			searchBean = new RoleSearchBean();
		}
		if(searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0){
			criteria.setMaxResults(searchBean.getMaxResults());
		}
		addOrderBy(searchBean.getOrderByList(),criteria);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<Role> l=criteria.list();
		return l;
	}
	
}