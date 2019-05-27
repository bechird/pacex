package com.epac.cap.repository;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import com.epac.cap.model.Job;
import com.epac.cap.model.Roll;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

/**
 * Interacts with Job data.  Uses an entity manager for entity persistence.
 *
 * @author walid
 *
 */
@Repository
public class JobDAO extends BaseEntityPersister {

	/** 
	 * creates the bean
	 */	 
	public void create(Job bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * updates the bean
	 */	 
	public void update(Job bean) {
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * deletes the bean
	 */	 
	public boolean delete(Job bean) {		
		if (bean != null){
			//need to re-retrieve the instance b/c it was likely retrieved in a separate
			//transaction (during action.prepareDelete()) making it detached now
			getEntityManager().remove(read(bean.getJobId()));
			getEntityManager().flush();
			return true;
		}else{
			return false;
		}
	}

	/** 
	 * reads the bean
	 */	 
	public Job read(Integer jobId) {
		return getEntityManager().find(Job.class, jobId);
	}

	/** 
	 * reads all the beans
	 */
	@SuppressWarnings("unchecked")
	public List<Job> readAll(JobSearchBean searchBean) {
		Criteria criteria = createCriteria(Job.class);
		if(searchBean != null){
			super.addAuditableCriteria(criteria, searchBean);
			if(searchBean.getJobId() != null){
				criteria.add(Restrictions.eq("jobId", searchBean.getJobId()));
			}
			if(searchBean.getOrderId() != null){
				criteria.add(Restrictions.eq("orderId", searchBean.getOrderId()));
			}
			if(!StringUtils.isBlank(searchBean.getStatus())){							
				criteria.add(Restrictions.eq("jobStatus.id", searchBean.getStatus()));		
			}
			if(searchBean.getStatusesNotIn() != null && !searchBean.getStatusesNotIn().isEmpty()){
				criteria.add(Restrictions.not(Restrictions.in("jobStatus.id", searchBean.getStatusesNotIn())));		
			}
			if(searchBean.getStatusesIn() != null && !searchBean.getStatusesIn().isEmpty()){							
				criteria.add(Restrictions.in("jobStatus.id", searchBean.getStatusesIn()));		
			}
			if(!StringUtils.isBlank(searchBean.getPartNum())){
				criteria.add(Restrictions.eq("partNum", searchBean.getPartNum()));
			}
			if(!StringUtils.isBlank(searchBean.getPartFamily())){
				criteria.add(Restrictions.ilike("partNum", searchBean.getPartFamily(), MatchMode.START));
			}
			if(!StringUtils.isBlank(searchBean.getPartCategory())){
				criteria.add(Restrictions.eq("partCategory", searchBean.getPartCategory()));
			}
			if(!StringUtils.isBlank(searchBean.getPartColors())){
				criteria.add(Restrictions.eq("partColor", searchBean.getPartColors()));
			}
			if(!StringUtils.isBlank(searchBean.getPartPapertype())){
				criteria.add(Restrictions.eq("partPaperId", searchBean.getPartPapertype()));
			}
			if(!StringUtils.isBlank(searchBean.getStationId())){
				criteria.add(Restrictions.eq("stationId", searchBean.getStationId()));		
			}
			if(searchBean.getProductionOrdering() != null){
				criteria.add(Restrictions.eq("productionOrdering", searchBean.getProductionOrdering()));
			}
			if(searchBean.getQuantityProduced() != null){
				criteria.add(Restrictions.eq("quantityProduced", searchBean.getQuantityProduced()));
			}
			if(searchBean.getQuantityNeeded() != null){
				criteria.add(Restrictions.eq("quantityNeeded", searchBean.getQuantityNeeded()));
			}
			if(searchBean.getRollOrdering() != null){
				criteria.add(Restrictions.eq("rollOrdering", searchBean.getRollOrdering()));
			}
			if(searchBean.getRollId() != null){
				criteria.add(Restrictions.eq("rollId", searchBean.getRollId()));
			}
			if(searchBean.getRollIdNull() != null && searchBean.getRollIdNull()){
				criteria.add(Restrictions.isNull("rollId"));
			}
			if(!StringUtils.isBlank(searchBean.getMachineId())){							
				criteria.add(Restrictions.eq("machineId", searchBean.getMachineId()));		
			}	
			if(searchBean.getMachineOrdering() != null){
				criteria.add(Restrictions.eq("machineOrdering", searchBean.getMachineOrdering()));
			}	
			if(!StringUtils.isBlank(searchBean.getJobType())){							
				criteria.add(Restrictions.eq("jobType.id", searchBean.getJobType()));		
			}	
			if(searchBean.getFileSentFlag() != null){
				criteria.add(Restrictions.eq("fileSentFlag", searchBean.getFileSentFlag()));
			}	
			if(!StringUtils.isBlank(searchBean.getJobPriority())){
				criteria.add(Restrictions.eq("jobPriority.id", searchBean.getJobPriority()));
			}
			if(searchBean.getSplitLevel() != null){
				criteria.add(Restrictions.eq("splitLevel", searchBean.getSplitLevel()));
			}
			if(searchBean.getMaxSplitLevel() != null){
				criteria.add(Restrictions.lt("splitLevel", searchBean.getMaxSplitLevel()));
			}
			if(searchBean.getHours() != null){
				criteria.add(Restrictions.eq("hours", searchBean.getHours()));
			}
			
			if(!StringUtils.isBlank(searchBean.getSearchJobIdPart())){					
				criteria.add(Restrictions.sqlRestriction(" {alias}.Job_Id LIKE '"+searchBean.getSearchJobIdPart()+"%' "));
			}
			
			if(!StringUtils.isBlank(searchBean.getSearchOrderId())){					
				criteria.add(Restrictions.sqlRestriction(" {alias}.Order_Id LIKE '"+searchBean.getSearchOrderId()+"%' "));
			}
			
			if(!StringUtils.isBlank(searchBean.getSearchPartNum())){					
				criteria.add(Restrictions.ilike("partNum", searchBean.getSearchPartNum(), MatchMode.ANYWHERE));	
			}
			
			if(!StringUtils.isBlank(searchBean.getSearchRollId())){					
				criteria.add(Restrictions.sqlRestriction(" {alias}.Roll_Id LIKE '"+searchBean.getSearchRollId()+"%' "));
			}
			
			if(!StringUtils.isBlank(searchBean.getSearchStatus())){					
				criteria.add(Restrictions.ilike("jobStatus.id", searchBean.getSearchStatus(), MatchMode.ANYWHERE));	
			}
			
			if(!StringUtils.isBlank(searchBean.getSearchStationId())){					
				criteria.add(Restrictions.ilike("stationId", searchBean.getSearchStationId(), MatchMode.ANYWHERE));	
			}
			
			if(!StringUtils.isBlank(searchBean.getSearchSplitLevel())){					
				criteria.add(Restrictions.sqlRestriction(" {alias}.Split_Level LIKE '"+searchBean.getSearchSplitLevel()+"%' "));
			}

			if(!StringUtils.isBlank(searchBean.getSearchHours())){					
				criteria.add(Restrictions.sqlRestriction(" {alias}.Hours LIKE '"+searchBean.getSearchHours()+"%' "));
			}

			if(!StringUtils.isBlank(searchBean.getSearchQuantityNeeded())){					
				criteria.add(Restrictions.sqlRestriction(" {alias}.Quantity_Needed LIKE '"+searchBean.getSearchQuantityNeeded()+"%' "));
			}

			if(!StringUtils.isBlank(searchBean.getSearchQuantityProduced())){					
				criteria.add(Restrictions.sqlRestriction(" {alias}.Quantity_Produced LIKE '"+searchBean.getSearchQuantityProduced()+"%' "));
			}
			
		}else{
			//instantiate a new search bean for the defaults
			searchBean = new JobSearchBean();
		}
		if(searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0){
			criteria.setMaxResults(searchBean.getMaxResults());
		}
		
		if(searchBean.getResultOffset() != null){
			criteria.setFirstResult(searchBean.getResultOffset());
		}
		
		if(searchBean.isListing()){
			//criteria.setFetchMode("order",  FetchMode.SELECT);
			//criteria.setFetchMode("part",  FetchMode.SELECT);
			criteria.setFetchMode("jobStatus",  FetchMode.SELECT);
			criteria.setFetchMode("jobType",  FetchMode.SELECT);			
			criteria.setFetchMode("jobPriority", FetchMode.SELECT);
			criteria.setFetchMode("binderyPriority", FetchMode.SELECT);				
			criteria.setFetchMode("loadTags", FetchMode.SELECT);
		}
		
		//criteria.setCacheable(true);
	    //criteria.setCacheRegion("com.epac.cap");
		addOrderBy(searchBean.getOrderByList(),criteria);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<Job> l=criteria.list();
		return l;
	}
	public List<com.epac.cap.model.Order> fetchJobPackaging(String isbn){
		String sql ="select * from order_T o where o.Status in ('COMPLETE', 'ONPROD') and o.Order_Id in(SELECT j.order_Id FROM `job` j WHERE j.Status in('RUNNING','COMPLETE','COMPLETE_PARTIAL') and j.Station_Id = 'BINDER' and j.`Part_Num` in (select DISTINCT p.Part_Num from part p where p.ISBN = '"+isbn+"'))";


	Query query = getEntityManager().createNativeQuery(sql,com.epac.cap.model.Order.class);
	return (List<com.epac.cap.model.Order> )query.getResultList();
	}
	
	/** 
	 * count of jobs
	 */	 
	public Integer getCount() {
		Session session = getHibernateSession();
		String sql = "select count(*) from job";
		SQLQuery query = session.createSQLQuery(sql);
		query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return  ((Number) query.uniqueResult()).intValue();
	}
	
	/** 
	 * count of order parts
	 */	 
	public Integer orderPartsCount(Integer orderId) {
		Session session = getHibernateSession();
		String sql = "select count(*) from OrderParts where orderId = " + orderId;
		SQLQuery query = session.createSQLQuery(sql);
		query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return  ((Number) query.uniqueResult()).intValue();
	}
	
	/** 
	 * full search
	 */	 
	public List<Job> fullSearch(String word, Integer maxResult, Integer offset) {		
		Criteria criteria = createCriteria(Job.class);
		
		List<Criterion> criterions = new ArrayList<Criterion>();
		try {
			Criterion c1 = Restrictions.eq("jobId", Integer.valueOf(word));
			criterions.add(c1);
		} catch (NumberFormatException e) {}
		
		try {
			Criterion c2 = Restrictions.eq("orderId", Integer.valueOf(word));
			criterions.add(c2);
		} catch (NumberFormatException e) {}
		
		
		Criterion c3 = Restrictions.ilike("partNum", word, MatchMode.ANYWHERE);
		criterions.add(c3);

		try {
			Criterion c4 = Restrictions.eq("rollId", Integer.valueOf(word));
			criterions.add(c4);
		} catch (NumberFormatException e) {}
		
		Criterion c5 = Restrictions.ilike("jobStatus.id", word, MatchMode.ANYWHERE);
		criterions.add(c5);
		
		Criterion c6 = Restrictions.ilike("stationId", word, MatchMode.ANYWHERE);
		criterions.add(c6);
		
		try {
			Criterion c7 = Restrictions.eq("splitLevel", Integer.valueOf(word));
			criterions.add(c7);
		} catch (NumberFormatException e) {}

		
		try {
			Criterion c8 = Restrictions.eq("hours", Float.valueOf(word));
			criterions.add(c8);
		} catch (NumberFormatException e) {}

		
		try {
			Criterion c9 = Restrictions.eq("quantityNeeded", Integer.valueOf(word));
			criterions.add(c9);
		} catch (NumberFormatException e) {}

		
		try {
			Criterion c10 = Restrictions.eq("quantityProduced", Float.valueOf(word));
			criterions.add(c10);
		} catch (NumberFormatException e) {}
		
		Criterion[] criterionArray = new Criterion[criterions.size()];
		criterionArray = criterions.toArray(criterionArray);		
		criteria.add( Restrictions.or(criterionArray) );
		
		
		if(maxResult!=null)criteria.setMaxResults(maxResult);
		if(offset!=null)criteria.setFirstResult(offset);

		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<Job> l=criteria.list();
		return l;
		
	}
	
	/** 
	 * ids
	 */	 
	public List<Integer> getIdsList() {
		List<Integer> ids = new ArrayList<Integer>();

		try {
			Session session = getHibernateSession();
			String sql = "select Job_Id from job";
			SQLQuery query = session.createSQLQuery(sql);
			query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);  
			List<Integer> rows = query.list();		
			
			for(Integer row : rows){			
				ids.add(row);			
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return  ids;
	}
	
}