package com.epac.cap.repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.epac.cap.model.Log;

/**
 * Interacts with Log data.  Uses an entity manager for entity persistence.
 *
 * @author walid
 *
 */
@Repository
public class LogDAO extends BaseEntityPersister {

	/** 
	 * creates the bean
	 */	 
	public void create(Log bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * updates the bean
	 */	 
	public void update(Log bean) {
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * deletes the bean
	 */	 
	public boolean delete(Log bean) {		
		if (bean != null){
			//need to re-retrieve the instance b/c it was likely retrieved in a separate
			//transaction (during action.prepareDelete()) making it detached now
			getEntityManager().remove(read(bean.getLogId()));
			getEntityManager().flush();
			return true;
		}else{
			return false;
		}
	}

	/** 
	 * reads the bean by id
	 */	 
	public Log read(Integer logId) {
		return getEntityManager().find(Log.class, logId);
	}

	/** 
	 * reads all beans
	 */	 
	@SuppressWarnings("unchecked")
	public List<Log> readAll(LogSearchBean searchBean) {
		Criteria criteria = createCriteria(Log.class);
		
		criteria.createAlias("logCause", "lc");
		criteria.createAlias("logResult", "lr");
		
		if(searchBean != null){
			super.addAuditableCriteria(criteria, searchBean);
			if(searchBean.getLogId() != null){
				criteria.add(Restrictions.eq("logId", searchBean.getLogId()));
			}
			if(searchBean.getMachineId() != null){
				criteria.add(Restrictions.eq("machineId", searchBean.getMachineId()));
			}
			if(!StringUtils.isBlank(searchBean.getEvent())){							
				criteria.add(Restrictions.ilike("event", searchBean.getEvent(), MatchMode.ANYWHERE));		
			}
			if(!StringUtils.isBlank(searchBean.getResult())){							
				criteria.add(Restrictions.eq("logResult.id", searchBean.getResult()));		
			}
			if(!StringUtils.isBlank(searchBean.getCause())){							
				criteria.add(Restrictions.eq("logCause.id", searchBean.getCause()));		
			}
			if(searchBean.getCurrentJobId() != null){
				criteria.add(Restrictions.eq("currentJobId", searchBean.getCurrentJobId()));
			}
			if(searchBean.getRollId() != null){
				criteria.add(Restrictions.eq("rollId", searchBean.getRollId()));
			}	
			if(searchBean.getRollLength() != null){
				criteria.add(Restrictions.eq("rollLength", searchBean.getRollLength()));
			}	
			super.addDateRangeCriteria(criteria,"startTime",searchBean.getStartTimeFrom(),searchBean.getStartTimeTo());
			super.addDateRangeCriteria(criteria,"finishTime",searchBean.getFinishTimeFrom(),searchBean.getFinishTimeTo());
			if(searchBean.getCounterFeet() != null){
				criteria.add(Restrictions.eq("counterFeet", searchBean.getCounterFeet()));
			}
			
			if(!StringUtils.isBlank(searchBean.getSearchLogIdPart())){					
				criteria.add(Restrictions.sqlRestriction(" Log_Id LIKE '"+searchBean.getSearchLogIdPart()+"%' "));
			}
			if(!StringUtils.isBlank(searchBean.getSearchMachineIdPart())){							
				criteria.add(Restrictions.ilike("machineId", searchBean.getSearchMachineIdPart(), MatchMode.ANYWHERE));		
			}			
			if(!StringUtils.isBlank(searchBean.getSearchJobIdPart())){					
				criteria.add(Restrictions.sqlRestriction(" Current_Job_Id LIKE '"+searchBean.getSearchJobIdPart()+"%' "));
			}

			if(!StringUtils.isBlank(searchBean.getSearchCausePart())){					
				criteria.add(Restrictions.ilike("lc.name", searchBean.getSearchCausePart(), MatchMode.ANYWHERE));	
			}

			if(!StringUtils.isBlank(searchBean.getSearchResultPart())){					
				criteria.add(Restrictions.ilike("lr.name", searchBean.getSearchResultPart(), MatchMode.ANYWHERE));	
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
			
			if(!StringUtils.isBlank(searchBean.getSearchRollIdPart())){					
				criteria.add(Restrictions.sqlRestriction(" Roll_Id LIKE '"+searchBean.getSearchRollIdPart()+"%' "));
			}

			if(!StringUtils.isBlank(searchBean.getSearchRollLengthPart())){					
				criteria.add(Restrictions.sqlRestriction(" Roll_Length LIKE '"+searchBean.getSearchRollLengthPart()+"%' "));
			}

			if(!StringUtils.isBlank(searchBean.getSearchCounterFeetPart())){					
				criteria.add(Restrictions.sqlRestriction(" Counter_Feet LIKE '"+searchBean.getSearchCounterFeetPart()+"%' "));
			}
			
		}else{
			//instantiate a new search bean for the defaults
			searchBean = new LogSearchBean();
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
		List<Log> l=criteria.list();
		return l;
	}
	
	/** 
	 * reads all beans
	 */	 
	@SuppressWarnings("unchecked")
	public List<Log> readTaskEndedLogsBetween(Date startDateTime, Date endDateTime) {
		
		Criteria criteria = createCriteria(Log.class);		
		criteria.add(Restrictions.in("event", Arrays.asList("STOP","COMPLETE")));
		super.addDateRangeCriteria(criteria,"startTime", startDateTime, endDateTime);			
		List<Log> l=criteria.list();
		return l;
	}

	
	/** 
	 * count
	 */	 
	public Integer getCount() {
		Session session = getHibernateSession();
		String sql = "select count(*) from log";
		SQLQuery query = session.createSQLQuery(sql);
		query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return  ((Number) query.uniqueResult()).intValue();
	}
	
	
	/** 
	 * count
	 */	 
	public List<Log> fullSearch(String word, Integer maxResult, Integer offset) {		
		Criteria criteria = createCriteria(Log.class);
		
		List<Criterion> criterions = new ArrayList<Criterion>();
		try {
			Criterion c1 = Restrictions.eq("logId", Integer.valueOf(word));
			criterions.add(c1);
		} catch (NumberFormatException e) {}
		
		
		Criterion c2 = Restrictions.ilike("machineId", word, MatchMode.ANYWHERE);
		criterions.add(c2);
		
		
		try {
			Criterion c3 = Restrictions.eq("currentJobId", Integer.valueOf(word));
			criterions.add(c3);
		} catch (NumberFormatException e) {}

		
		Criterion c4 = Restrictions.ilike("event", word, MatchMode.ANYWHERE);
		criterions.add(c4);

		try {
			Criterion c5 = Restrictions.eq("rollId", Integer.valueOf(word));
			criterions.add(c5);
		} catch (Exception e) {}

		
		try {
			Criterion c6 = Restrictions.eq("rollLength", Integer.valueOf(word));
			criterions.add(c6);
		} catch (Exception e) {}
		
		try {
			Criterion c7 = Restrictions.eq("counterFeet", Long.valueOf(word));
			criterions.add(c7);
		} catch (Exception e) {}
		
		
		Criterion[] criterionArray = new Criterion[criterions.size()];
		criterionArray = criterions.toArray(criterionArray);		
		criteria.add( Restrictions.or(criterionArray) );
		
		
		if(maxResult!=null)criteria.setMaxResults(maxResult);
		if(offset!=null)criteria.setFirstResult(offset);

		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<Log> l=criteria.list();
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