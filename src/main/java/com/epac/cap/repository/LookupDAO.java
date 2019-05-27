package com.epac.cap.repository;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.epac.cap.common.CacheManagingPersister;
import com.epac.cap.model.LookupItem;
import com.epac.cap.model.OrderableLookupItem;
import com.epac.cap.model.PNLTemplateLine;

import java.util.List;

import javax.persistence.Query;

/**
 * Implementation class for the LookupDAO interface. Interacts with various lookup data. Uses an entity manager for
 * entity persistence.
 * 
 */
@Repository
public class LookupDAO extends CacheManagingPersister {

  /**
   * create lookup item
   */
  public void create(LookupItem item) {
    getEntityManager().persist(item);
    getEntityManager().flush();
    clearCollectionsInternal(item.getClass());
  }
  
  /**
   * create PNL Template Line
   */
  public void createTmpLine(PNLTemplateLine item) {
    getEntityManager().persist(item);
    getEntityManager().flush();
    clearCollectionsInternal(item.getClass());
  }

  /**
   * update lookup item
   */
  public void update(LookupItem item) {
    getEntityManager().merge(item);
    getEntityManager().flush();
  }
  
  /**
   * update template line
   */
  public void updateTmpLine(PNLTemplateLine item) {
    getEntityManager().merge(item);
    getEntityManager().flush();
  }
  
  /**
   * delete lookup item
   */
  public <L extends LookupItem> boolean delete(String id, Class<L> itemClass) {
    LookupItem bean = read(id, itemClass);
    if (bean != null) {
      getEntityManager().remove(bean);
      getEntityManager().flush();
      clearCollectionsInternal(bean.getClass());
      return true;
    } else {
      return false;
    }
  }
  
  /**
   * delete PNL Template line
   */
  public boolean deleteTmpLine(Integer tmpLineId) {
	  PNLTemplateLine bean = readTmpLine(tmpLineId);
    if (bean != null) {
      getEntityManager().remove(bean);
      getEntityManager().flush();
      return true;
    } else {
      return false;
    }
  }

  /**
   * read lookup item
   */
  public <L extends LookupItem> L read(String id, Class<L> itemClass) {
    return getEntityManager().find(itemClass, id);
  }
  
  /**
   * read PNL Template line
   */
  public PNLTemplateLine readTmpLine(Integer tmpLineId) {
    return getEntityManager().find(PNLTemplateLine.class, tmpLineId);
  }
  
  /**
   * read all Lookups
   */
  @SuppressWarnings("unchecked")
  public <L extends LookupItem> List<L> readAll(List<String> idList, LookupSearchBean searchBean, Class<L> itemClass) {
    Criteria criteria = createCriteria(itemClass);
    if (idList != null && !idList.isEmpty()) {
      criteria.add(Restrictions.in("id", idList));
    }
    if (OrderableLookupItem.class.isAssignableFrom(itemClass)) {
      criteria.addOrder(Order.asc("pickSequence"));
    } 
    if(searchBean != null){
    	super.addAuditableCriteria(criteria, searchBean);
		if(!StringUtils.isBlank(searchBean.getId())){
			criteria.add(Restrictions.eq("id", searchBean.getId()));					
		}	
		if(!StringUtils.isBlank(searchBean.getIdDiff())){
			criteria.add(Restrictions.ne("id", searchBean.getIdDiff()));					
		}
		if(!StringUtils.isBlank(searchBean.getIdPrefix())){
			criteria.add(Restrictions.ilike("id", searchBean.getIdPrefix(), MatchMode.START));					
		}
		if(!StringUtils.isBlank(searchBean.getName())){							
			criteria.add(Restrictions.ilike("name", searchBean.getName(), MatchMode.ANYWHERE));		
		}
		if(!StringUtils.isBlank(searchBean.getNamePrefix())){							
			criteria.add(Restrictions.ilike("name", searchBean.getNamePrefix(), MatchMode.START));		
		}
		if(!StringUtils.isBlank(searchBean.getNameExact())){							
			criteria.add(Restrictions.eq("name", searchBean.getNameExact()));		
		}
		if(!StringUtils.isBlank(searchBean.getDescription())){							
			criteria.add(Restrictions.ilike("description", searchBean.getDescription(), MatchMode.ANYWHERE));		
		}
		if(!StringUtils.isBlank(searchBean.getPrefGroup())){
			criteria.add(Restrictions.eq("groupingValue", searchBean.getPrefGroup()));
		}
		if(!StringUtils.isBlank(searchBean.getPrefSubject())){
			criteria.add(Restrictions.eq("prefSubject", searchBean.getPrefSubject()));
		}
		
		if(!StringUtils.isBlank(searchBean.getPartNum())){
			criteria.add(Restrictions.eq("partNum", searchBean.getPartNum()));
		}
		if(!StringUtils.isBlank(searchBean.getClientId())){
			criteria.add(Restrictions.eq("clientId", searchBean.getClientId()));
		}
    }else{
		//instantiate a new search bean for the defaults
		searchBean = new LookupSearchBean();
    }
    if(searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0){
		criteria.setMaxResults(searchBean.getMaxResults());
	}
    criteria.setCacheable(true);
    criteria.setCacheRegion("com.epac.cap");
	addOrderBy(searchBean.getOrderByList(),criteria);
	criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
	List<L> l=criteria.list();
	return l;
	
  }
  
  /**
   * read all preference groups
   */
  public List<String> readPrefGroups(String clientId){
	  String sql = null;
	  if(clientId != null && !clientId.isEmpty() && !"undefined".equals(clientId)){
		  sql = "SELECT groupingValue FROM Preference where clientId = '" + clientId + "' order by groupingValue";
	  }else{
		  sql = "SELECT groupingValue FROM Preference where clientId is null or clientId = '' or clientId = 'undefined' order by groupingValue";
	  }
	  Query query = getEntityManager().createQuery(sql, java.lang.String.class);
	  return (List<String>) query.getResultList();
  }
  
  /**
   * read all preference subjects
   */
  public List<String> readPrefSubjects(String groupId, String clientId){
	  String sql = null;
	  if(groupId != null && !groupId.isEmpty() && !"undefined".equals(groupId)){
		  if(clientId != null && !clientId.isEmpty() && !"undefined".equals(clientId)){
			  sql = "SELECT prefSubject FROM Preference where groupingValue = '" + groupId + "' and clientId = '" + clientId + "' order by prefSubject";
		  }else{
			  sql = "SELECT prefSubject FROM Preference where groupingValue = '" + groupId + "' and (clientId is null or clientId = '' or clientId = 'undefined') order by prefSubject";
		  }
	  }else{
		  if(clientId != null && !clientId.isEmpty() && !"undefined".equals(clientId)){
			  sql = "SELECT prefSubject FROM Preference where clientId = '" + clientId + "' order by prefSubject";
		  }else{
			 // sql = "SELECT prefSubject FROM Preference where clientId is null or clientId = '' or clientId = 'undefined' order by prefSubject";
			  sql = "SELECT prefSubject FROM Preference order by prefSubject";
		  }
	  }
	  Query query = getEntityManager().createQuery(sql, java.lang.String.class);
	  return (List<String>) query.getResultList();
  }
  
}
