package com.epac.cap.common;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.repository.BaseEntityPersister;

import java.io.Serializable;

/**
 * A public API for managing the 2nd level cache.
 * 
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class CacheManagingPersister extends BaseEntityPersister {

  @Override
  public void clearCacheInternal() {
    super.clearCacheInternal();
  }

  @Override
  public void clearEntitiesInternal() {
    super.clearEntitiesInternal();
  }

  @Override
  public void clearEntityInternal(@SuppressWarnings("rawtypes") Class persistentClass) {
    super.clearEntityInternal(persistentClass);
  }

  @Override
  public void clearEntityInternal(@SuppressWarnings("rawtypes") Class persistentClass, Serializable beanId) {
    super.clearEntityInternal(persistentClass, beanId);
  }

  @Override
  public void clearCollectionsInternal() {
    super.clearCollectionsInternal();
  }

  @Override
  public void clearCollectionsInternal(@SuppressWarnings("rawtypes") Class clazz) {
    super.clearCollectionsInternal(clazz);
  }

  @Override
  public void clearQueriesInternal() {
    super.clearQueriesInternal();
  }

  @Override
  protected void evict(Object bean) {
    super.evict(bean);
  }
}
