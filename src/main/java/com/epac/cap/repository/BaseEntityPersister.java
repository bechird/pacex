package com.epac.cap.repository;


import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;

import com.epac.cap.common.AuditableBean;
import com.epac.cap.common.AuditableSearchBean;
import com.epac.cap.common.OrderBy;

/**
 * Base class for all Services or DAOs which handle the persistence of entities in a database.
 * 
 */

public abstract class BaseEntityPersister {
  private EntityManager em;

  /**
   * Set the entity manager for this transaction
   * 
   * @param em the entity manager
   */
  @PersistenceContext
  public void setEntityManager(EntityManager em) {
    this.em = em;
  }

  /**
   * Return the entity manager for this transaction
   * 
   * @return the entity manager
   */
  protected EntityManager getEntityManager() {
    return em;
  }

  /**
   * Creates a hibernate criteria object for the given persistent entiy
   * 
   * @param entityClass the persistent entity class
   * @return
   */
  @SuppressWarnings("rawtypes")
  protected Criteria createCriteria(Class entityClass) {
    Session session = getHibernateSession();
    return session.createCriteria(entityClass);
  }

  protected Criteria createCriteria(@SuppressWarnings("rawtypes") Class entityClass, String alias) {
    Session session = getHibernateSession();
    return session.createCriteria(entityClass, alias);
  }

  /**
   * @return the hibernate session associated with the entity manager
   */
  protected Session getHibernateSession() {
    Session session = getEntityManager().unwrap(Session.class);
    return session;
  }

  /**
   * Adds the ordering information to the criteria object. If the order by property is null no ordering info will be
   * added. If the order by direction is not ASC or DESC then ASC will be used.
   * 
   * @param orderBy the property to order by
   * @param orderByDir the direction to order by
   * @param criteria the hibernate criteria to add the order by to.
   */
  protected void addOrderBy(String orderBy, String orderByDir, Criteria criteria) {
    this.addOrderBy(orderBy, orderByDir, false, criteria);
  }

  /**
   * 
   * @param orderBy the property to order by
   * @param orderByDir the direction to order by
   * @param ignoreCase whether or not to ignore case for this order by column
   * @param criteria the hibernate criteria to add the order by to.
   */
  protected void addOrderBy(String orderBy, String orderByDir, boolean ignoreCase, Criteria criteria) {
    if (orderBy != null) {
      Order orderStmt = null;
      if (OrderBy.DESC.equals(orderByDir)) {
        orderStmt = Order.desc(orderBy);
      } else {
        orderStmt = Order.asc(orderBy);
      }
      if (ignoreCase) {
        orderStmt = orderStmt.ignoreCase();
      }
      criteria.addOrder(orderStmt);
    }
  }

  /**
   * Adds a date range filtering of the given property to the given criteria. If fromDate and toDate aren't null then a
   * between filter is used, else if fromDate isn't null and toDate is then a greater than or equal fromDate filter is
   * used, else if toDate isn't null then a less than or equal toDate filter is used. If both are null then no filtering
   * is added.
   * 
   * @param criteria the criteria to add the filter to
   * @param propertyName the name of the property to filter on
   * @param fromDate the from date, can be null
   * @param toDate the to date, can be null
   */
  protected void addDateRangeCriteria(Criteria criteria, String propertyName, Date fromDate, Date toDate) {
    if (fromDate != null && toDate != null) {
      criteria.add(Restrictions.between(propertyName, fromDate, toDate));
    } else if (fromDate != null && toDate == null) {
      criteria.add(Restrictions.ge(propertyName, fromDate));
    } else if (fromDate == null && toDate != null) {
      criteria.add(Restrictions.le(propertyName, toDate));
    }
  }

  protected void addAuditableCriteria(Criteria criteria, AuditableSearchBean searchBean) {
    if (!StringUtils.isBlank(searchBean.getCreatorId())) {
      criteria.add(Restrictions.ilike("creatorId", searchBean.getCreatorId(), MatchMode.ANYWHERE));
    }
    addDateRangeCriteria(criteria, "createdDate", searchBean.getCreatedDateFrom(), searchBean.getCreatedDateTo());
    if (!StringUtils.isBlank(searchBean.getLastUpdateId())) {
      criteria.add(Restrictions.disjunction()
              .add(Restrictions.ilike("lastUpdateId", searchBean.getLastUpdateId(), MatchMode.ANYWHERE))
              .add(Restrictions.ilike("creatorId", searchBean.getLastUpdateId(), MatchMode.ANYWHERE)));
    }

    if (searchBean.getLastUpdateDateFrom() != null && searchBean.getLastUpdateDateTo() != null) {
      criteria.add(Restrictions
              .disjunction()
              .add(Restrictions.between("lastUpdateDate", searchBean.getLastUpdateDateFrom(),
                      searchBean.getLastUpdateDateTo()))
              .add(Restrictions.between("createdDate", searchBean.getLastUpdateDateFrom(),
                      searchBean.getLastUpdateDateTo())));
    } else if (searchBean.getLastUpdateDateFrom() != null && searchBean.getLastUpdateDateTo() == null) {
      criteria.add(Restrictions.disjunction()
              .add(Restrictions.ge("lastUpdateDate", searchBean.getLastUpdateDateFrom()))
              .add(Restrictions.ge("createdDate", searchBean.getLastUpdateDateFrom())));
    } else if (searchBean.getLastUpdateDateFrom() == null && searchBean.getLastUpdateDateTo() != null) {
      criteria.add(Restrictions.disjunction().add(Restrictions.le("lastUpdateDate", searchBean.getLastUpdateDateTo()))
              .add(Restrictions.le("createdDate", searchBean.getLastUpdateDateTo())));
    }
  }

  /**
   * @param orderBy the bean containing ordering information
   * @param criteria the Hibernate criteria object to add the ordering to
   * @see BaseEntityPersister#addOrderBy(String, String, Criteria)
   */
  protected void addOrderBy(OrderBy orderBy, Criteria criteria) {
    if (orderBy != null) {
      this.addOrderBy(orderBy.getName(), orderBy.getDirection(), orderBy.isIgnoreCase(), criteria);
    }
  }

  /**
   * Adds the order bys to the criteria object
   * 
   * @param orderByList the list of order by beans to add
   * @param criteria the Hibernate criteria object to add the ordering to
   * @see BaseEntityPersister#addOrderBy(String, String, Criteria)
   */
  protected void addOrderBy(List<OrderBy> orderByList, Criteria criteria) {
    if (orderByList != null) {
      for (OrderBy orderBy : orderByList) {
        this.addOrderBy(orderBy, criteria);
      }
    }
  }

  /**
   * Clear the entire hibernate cache, this is not specific to a dao or subject matter
   */
  protected void clearCacheInternal() {
    clearQueriesInternal();
    clearCollectionsInternal();
    clearEntitiesInternal();
    // just to be sure, also call hibernate clear method which should clear out
    // all of the above
    Session session = getHibernateSession();
    if (session.isOpen()) {
      // ensure the session isnt closed, otherwise an error is thrown
      session.clear();
    }
  }

  protected void pruneEntities(Object bean) {
    Session session = getHibernateSession();
    SessionFactory sessionFactory = session.getSessionFactory();
    sessionFactory.getClassMetadata(bean.getClass());
  }

  /**
   * Clear all entities from the cache, this is not specific to a dao or subject matter
   */
  protected void clearEntitiesInternal() {
    Session session = getHibernateSession();
    SessionFactory sessionFactory = session.getSessionFactory();
    sessionFactory.getCache().evictEntityRegions();
  }

  /**
   * Clears a specific entity class from the cache
   * 
   * @param persistentClass
   */
  protected void clearEntityInternal(@SuppressWarnings("rawtypes") Class persistentClass) {
    Session session = getHibernateSession();
    SessionFactory sessionFactory = session.getSessionFactory();
    ClassMetadata classMetadata = sessionFactory.getClassMetadata(persistentClass);

    if (classMetadata != null) {
      sessionFactory.getCache().evictEntityRegion(classMetadata.getEntityName());
    }
  }

  /**
   * Clears a specific entity instance from the cache
   * 
   * @param persistentClass
   * @param beanId
   */
  protected void clearEntityInternal(@SuppressWarnings("rawtypes") Class persistentClass, Serializable beanId) {
    Session session = getHibernateSession();
    SessionFactory sessionFactory = session.getSessionFactory();
    ClassMetadata classMetadata = sessionFactory.getClassMetadata(persistentClass);

    if (classMetadata != null) {
      sessionFactory.getCache().evictEntity(classMetadata.getEntityName(), beanId);
    }
  }

  /**
   * Clear all collections from the hibernate cache, this is not specific to a dao or subject matter
   */
  protected void clearCollectionsInternal() {
    Session session = getHibernateSession();
    SessionFactory sessionFactory = session.getSessionFactory();
    sessionFactory.getCache().evictCollectionRegions();
  }

  /**
   * Clears all collections caches which have the type of the given class. Specific to a subject matter
   * 
   * @param clazz
   */
  protected void clearCollectionsInternal(@SuppressWarnings("rawtypes") Class clazz) {
    Session session = getHibernateSession();
    SessionFactory sessionFactory = session.getSessionFactory();
    for (Object o : sessionFactory.getAllCollectionMetadata().values()) {
      CollectionMetadata collectionMetadata = (CollectionMetadata) o;
      if (collectionMetadata.getElementType().getName().equals(clazz.getName())) {
        sessionFactory.getCache().evictCollectionRegion(collectionMetadata.getRole());
      }
    }

  }

  /**
   * Clear all queries from the hibernate cache, this is not specific to a dao or subject matter
   */
  protected void clearQueriesInternal() {
    Session session = getHibernateSession();
    SessionFactory sessionFactory = session.getSessionFactory();

    sessionFactory.getCache().evictQueryRegions();
  }

  protected void evict(Object bean) {
    Session session = getHibernateSession();
    SessionFactory sessionFactory = session.getSessionFactory();
    sessionFactory.getCache().evictEntityRegion(bean.getClass());
  }

  public void flush() {
    getEntityManager().flush();
  }

  protected void refresh(AuditableBean bean) {
    getEntityManager().refresh(bean);
  }
}
