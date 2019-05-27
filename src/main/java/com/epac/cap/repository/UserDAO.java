package com.epac.cap.repository;

import com.epac.cap.model.*;
import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.apache.commons.lang.StringUtils;
import java.util.List;

/**
 * Implementation class for the UserDAO interface. Interacts with User data.
 * Uses an entity manager for entity persistence.
 *
 * @author walid
 *
 */
@Repository
public class UserDAO extends BaseEntityPersister {

	/**
	 * create the bean
	 */
	public void create(User bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}

	/**
	 * update the bean
	 */
	public void update(User bean) {
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}

	/**
	 * delete the bean
	 */
	public boolean delete(User bean) {
		if (bean != null) {
			// need to re-retrieve the instance b/c it was likely retrieved in a separate
			// transaction (during action.prepareDelete()) making it detached now
			getEntityManager().remove(read(bean.getUserId()));
			getEntityManager().flush();
			return true;
		} else {
			return false;
		}
	}

	public User findByActiveEmail(String email) {
		UserSearchBean searchBean = new UserSearchBean();
		searchBean.setEmail(email);
		searchBean.setActiveFlag(true);
		
		List<User> list = readAll(searchBean);
		if (list == null || list.size() == 0) {
			return null;
		} else {
			return list.get(0);
		}
	}
	public User findByEmail(String email) {
		UserSearchBean searchBean = new UserSearchBean();
		searchBean.setEmail(email);
		List<User> list = readAll(searchBean);
		if (list == null || list.size() == 0) {
			return null;
		} else {
			return list.get(0);
		}
	}
	public User findByUserName(String userName) {
		UserSearchBean searchBean = new UserSearchBean();
		searchBean.setLoginNameExact(userName);
		List<User> list = readAll(searchBean);
		if (list == null || list.size() == 0) {
			return null;
		} else {
			return list.get(0);
		}
	}
	/**
	 * read by id
	 */
	public User read(String userId) {
		return getEntityManager().find(User.class, userId);
	}

	/**
	 * read all with criteria
	 */
	@SuppressWarnings("unchecked")
	public List<User> readAll(UserSearchBean searchBean) {
		Criteria criteria = createCriteria(User.class);
		if (searchBean != null) {
			super.addAuditableCriteria(criteria, searchBean);
			if (!StringUtils.isBlank(searchBean.getUserId())) {
				criteria.add(Restrictions.eq("userId", searchBean.getUserId()));
			}
			if (!StringUtils.isBlank(searchBean.getUserIdDiff())) {
				criteria.add(Restrictions.ne("userId", searchBean.getUserIdDiff()));
			}
			if (!StringUtils.isBlank(searchBean.getFirstName())) {
				criteria.add(Restrictions.ilike("firstName", searchBean.getFirstName(), MatchMode.ANYWHERE));
			}
			if (!StringUtils.isBlank(searchBean.getLastName())) {
				criteria.add(Restrictions.ilike("lastName", searchBean.getLastName(), MatchMode.ANYWHERE));
			}
			if (!StringUtils.isBlank(searchBean.getEmail())) {
				criteria.add(Restrictions.ilike("email", searchBean.getEmail(), MatchMode.ANYWHERE));
			}
			if (!StringUtils.isBlank(searchBean.getEmailExact())) {
				criteria.add(Restrictions.eq("email", searchBean.getEmailExact()));
			}
			if (!StringUtils.isBlank(searchBean.getPhoneNum())) {
				criteria.add(Restrictions.ilike("phoneNum", searchBean.getPhoneNum(), MatchMode.ANYWHERE));
			}
			if (!StringUtils.isBlank(searchBean.getLoginName())) {
				criteria.add(Restrictions.ilike("loginName", searchBean.getLoginName(), MatchMode.ANYWHERE));
			}
			if (!StringUtils.isBlank(searchBean.getLoginNameExact())) {
				criteria.add(Restrictions.eq("loginName", searchBean.getLoginNameExact()));
			}
			if (!StringUtils.isBlank(searchBean.getLoginPassword())) {
				criteria.add(Restrictions.ilike("loginPassword", searchBean.getLoginPassword(), MatchMode.ANYWHERE));
			}
			if (searchBean.getActiveFlag() != null) {
				criteria.add(Restrictions.eq("activeFlag", searchBean.getActiveFlag()));
			}
		} else {
			// instantiate a new search bean for the defaults
			searchBean = new UserSearchBean();
		}
		if (searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0) {
			criteria.setMaxResults(searchBean.getMaxResults());
		}
		addOrderBy(searchBean.getOrderByList(), criteria);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		List<User> l = criteria.list();
		return l;
	}

}