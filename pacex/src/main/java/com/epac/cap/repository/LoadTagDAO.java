package com.epac.cap.repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.epac.cap.model.LoadTag;

/**
 * Interacts with LoadTag data.  Uses an entity manager for entity persistence.
 *
 * @author walid
 *
 */
@Repository
public class LoadTagDAO extends BaseEntityPersister {

	/** 
	 * creates the bean
	 */	 
	public void create(LoadTag bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * updates the bean
	 */	 
	public void update(LoadTag bean) {
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * deletes the bean
	 */	 
	public boolean delete(LoadTag bean) {		
		if (bean != null){
			//need to re-retrieve the instance b/c it was likely retrieved in a separate
			//transaction (during action.prepareDelete()) making it detached now
			getEntityManager().remove(read(bean.getLoadTagId()));
			getEntityManager().flush();
			return true;
		}else{
			return false;
		}
	}

	/** 
	 * reads the bean by id
	 */	 
	public LoadTag read(Integer loadTagId) {
		return getEntityManager().find(LoadTag.class, loadTagId);
	}

	/** 
	 * reads all the beans
	 */	 
	@SuppressWarnings("unchecked")
	public List<LoadTag> readAll(LoadTagSearchBean searchBean) {
		Criteria criteria = createCriteria(LoadTag.class);
		if(searchBean != null){
			super.addAuditableCriteria(criteria, searchBean);
			if(searchBean.getLoadTagId() != null){
				criteria.add(Restrictions.eq("loadTagId", searchBean.getLoadTagId()));
			}
			if(searchBean.getJobId() != null){
				criteria.add(Restrictions.eq("jobId", searchBean.getJobId()));
			}	
			if(!StringUtils.isBlank(searchBean.getTagNum())){							
				criteria.add(Restrictions.ilike("tagNum", searchBean.getTagNum(), MatchMode.ANYWHERE));		
			}
			if(!StringUtils.isBlank(searchBean.getMachineId())){							
				criteria.add(Restrictions.eq("machineId", searchBean.getMachineId()));		
			}
			if(searchBean.getQuantity() != null){
				criteria.add(Restrictions.eq("quantity", searchBean.getQuantity()));
			}
			if(searchBean.getWaste() != null){
				criteria.add(Restrictions.eq("waste", searchBean.getWaste()));
			}
			if(!StringUtils.isBlank(searchBean.getCartNum())){							
				criteria.add(Restrictions.ilike("cartNum", searchBean.getCartNum(), MatchMode.ANYWHERE));		
			}			
			if(!StringUtils.isBlank(searchBean.getSearchLoadtagIdPart())){					
				criteria.add(Restrictions.sqlRestriction(" {alias}.Load_Tag_Id LIKE '"+searchBean.getSearchLoadtagIdPart()+"%' "));
			}
			if(!StringUtils.isBlank(searchBean.getSearchJobIdPart())){					
				criteria.add(Restrictions.sqlRestriction(" {alias}.Job_Id LIKE '"+searchBean.getSearchJobIdPart()+"%' "));
			}			
			if(!StringUtils.isBlank(searchBean.getSearchWastePart())){					
				criteria.add(Restrictions.sqlRestriction(" {alias}.Waste LIKE '"+searchBean.getSearchWastePart()+"%' "));
			}				
			if(!StringUtils.isBlank(searchBean.getSearchQuantityPart())){					
				criteria.add(Restrictions.sqlRestriction(" {alias}.Quantity LIKE '"+searchBean.getSearchQuantityPart()+"%' "));
			}	

			if(searchBean.getStartDateExact() != null){				
				super.addDateRangeCriteria(
						criteria,"startTime", 
						atStartOfDay(searchBean.getStartDateExact()),
						atEndOfDay(searchBean.getStartDateExact())
						);

			}
			
			if(searchBean.getFinishDateExact() != null){
				super.addDateRangeCriteria(
						criteria,"finishTime", 
						atStartOfDay(searchBean.getFinishDateExact()),
						atEndOfDay(searchBean.getFinishDateExact())
						);
			}	
			
		}else{
			//instantiate a new search bean for the defaults
			searchBean = new LoadTagSearchBean();
		}
		if(searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0){
			criteria.setMaxResults(searchBean.getMaxResults());
		}
		
		if(searchBean.getResultOffset() != null){
			criteria.setFirstResult(searchBean.getResultOffset());
		}
		
		//criteria.setCacheable(true);
	    //criteria.setCacheRegion("com.epac.cap");
		addOrderBy(searchBean.getOrderByList(),criteria);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<LoadTag> l=criteria.list();
		return l;
	}
	
	
	/** 
	 * count
	 */	 
	public Integer getCount() {
		Session session = getHibernateSession();
		String sql = "select count(*) from load_Tag";
		SQLQuery query = session.createSQLQuery(sql);
		query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return  ((Number) query.uniqueResult()).intValue();
	}
	
	
	/** 
	 * count
	 */	 
	public List<LoadTag> fullSearch(String word, Integer maxResult, Integer offset) {		
		Criteria criteria = createCriteria(LoadTag.class);
		
		List<Criterion> criterions = new ArrayList<Criterion>();
		try {
			Criterion c1 = Restrictions.eq("loadTagId", Integer.valueOf(word));
			criterions.add(c1);
		} catch (NumberFormatException e) {}
		
		try {
			Criterion c2 = Restrictions.eq("jobId", Integer.valueOf(word));
			criterions.add(c2);
		} catch (NumberFormatException e) {}
		
		
		try {
			Criterion c3 = Restrictions.eq("quantity", Float.valueOf(word));
			criterions.add(c3);
		} catch (NumberFormatException e) {}

		
		
		Criterion c5 = Restrictions.ilike("cartNum", word, MatchMode.ANYWHERE);
		criterions.add(c5);

		
		try {
			Criterion c6 = Restrictions.eq("usedFlag", Boolean.valueOf(word));
			criterions.add(c6);
		} catch (Exception e) {}

		
		
		Criterion[] criterionArray = new Criterion[criterions.size()];
		criterionArray = criterions.toArray(criterionArray);		
		criteria.add( Restrictions.or(criterionArray) );
		
		
		if(maxResult!=null)criteria.setMaxResults(maxResult);
		if(offset!=null)criteria.setFirstResult(offset);

		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<LoadTag> l=criteria.list();
		return l;
		
	}
	
	public Date atStartOfDay(Date date) {
	    LocalDateTime localDateTime = dateToLocalDateTime(date);
	    LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
	    return localDateTimeToDate(startOfDay);
	}

	public Date atEndOfDay(Date date) {
	    LocalDateTime localDateTime = dateToLocalDateTime(date);
	    LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
	    return localDateTimeToDate(endOfDay);
	}

	private LocalDateTime dateToLocalDateTime(Date date) {
	    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	private Date localDateTimeToDate(LocalDateTime localDateTime) {
	    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}
	
}