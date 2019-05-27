package com.epac.cap.repository;

import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.epac.cap.model.Customer;

import org.apache.commons.lang.StringUtils;
import java.util.List;

/**
 * Interacts with Customer data.  Uses an entity manager for entity persistence.
 *
 * @author walid
 *
 */
@Repository
public class CustomerDAO extends BaseEntityPersister {

	/** 
	 * creates the bean
	 */	 
	public void create(Customer bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * updates the bean
	 */	 
	public void update(Customer bean) {
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * deletes the bean
	 */	 
	public boolean delete(Customer bean) {		
		if (bean != null){
			//need to re-retrieve the instance b/c it was likely retrieved in a separate
			//transaction (during action.prepareDelete()) making it detached now
			getEntityManager().remove(read(bean.getCustomerId()));
			getEntityManager().flush();
			return true;
		}else{
			return false;
		}
	}

	/** 
	 * reads the bean
	 */	 
	public Customer read(Integer customerId) {
		return getEntityManager().find(Customer.class, customerId);
	}

	/** 
	 * read all the beans
	 */	 
	@SuppressWarnings("unchecked")
	public List<Customer> readAll(CustomerSearchBean searchBean) {
		Criteria criteria = createCriteria(Customer.class);
		if(searchBean != null){
			super.addAuditableCriteria(criteria, searchBean);
			if(searchBean.getCustomerId() != null){
				criteria.add(Restrictions.eq("customerId", searchBean.getCustomerId()));
			}
			if(searchBean.getCustomerIdDiff() != null){
				criteria.add(Restrictions.ne("customerId", searchBean.getCustomerIdDiff()));
			}
			if(!StringUtils.isBlank(searchBean.getFirstName())){							
				criteria.add(Restrictions.ilike("firstName", searchBean.getFirstName(), MatchMode.ANYWHERE));		
			}
			if(!StringUtils.isBlank(searchBean.getLastName())){							
				criteria.add(Restrictions.ilike("lastName", searchBean.getLastName(), MatchMode.ANYWHERE));		
			}
			if(!StringUtils.isBlank(searchBean.getEmail())){							
				criteria.add(Restrictions.ilike("email", searchBean.getEmail(), MatchMode.ANYWHERE));		
			}
			if(!StringUtils.isBlank(searchBean.getEmailExact())){							
				criteria.add(Restrictions.eq("email", searchBean.getEmailExact()));		
			}
			if(!StringUtils.isBlank(searchBean.getPhoneNum())){							
				criteria.add(Restrictions.ilike("phoneNum", searchBean.getPhoneNum(), MatchMode.ANYWHERE));		
			}
		}else{
			//instantiate a new search bean for the defaults
			searchBean = new CustomerSearchBean();
		}
		if(searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0){
			criteria.setMaxResults(searchBean.getMaxResults());
		}
		criteria.setCacheable(true);
	    criteria.setCacheRegion("com.epac.cap");
		addOrderBy(searchBean.getOrderByList(),criteria);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<Customer> l=criteria.list();
		return l;
	}
	
}