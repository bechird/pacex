package com.epac.cap.repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.epac.cap.common.DateUtil;
import com.epac.cap.model.PaperType;
import com.epac.cap.model.Roll;
import com.epac.cap.model.RollStatus;
import com.epac.cap.model.RollType;
import com.epac.cap.utils.LogUtils;

/**
 * Interacts with Roll data.  Uses an entity manager for entity persistence.
 *
 * @author walid
 *
 */
@Repository
public class RollDAO extends BaseEntityPersister {
	@Autowired
	LookupDAO lookupDAO;

	/** 
	 * creates the bean
	 */	 
	public void create(Roll bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * updates the bean
	 */	 
	public void update(Roll bean) {
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}
	
	public void updateCopyStatus(String copStatus,Integer id){
		String sql = "update roll set CopyStatus = "+copStatus +"where Roll_Id="+id;
		Query query = getEntityManager().createNativeQuery(sql);
		query.executeUpdate();
		//return (List<Order> )query.getResultList();
	}
	
	/** 
	 * deletes the bean
	 */	 
	public boolean delete(Roll bean) {		
		if (bean != null){
			//need to re-retrieve the instance b/c it was likely retrieved in a separate
			//transaction (during action.prepareDelete()) making it detached now
			getEntityManager().remove(read(bean.getRollId()));
			getEntityManager().flush();
			return true;
		}else{
			return false;
		}
	}

	/** 
	 * reads the bean by id
	 */	 
	public Roll read(Integer rollId) {
		return getEntityManager().find(Roll.class, rollId);
	}

	/** 
	 * reads all the beans
	 */	 
	@SuppressWarnings("unchecked")
	public List<Roll> readAll(RollSearchBean searchBean) {

		Criteria criteria = createCriteria(Roll.class);
		if(searchBean != null){
			super.addAuditableCriteria(criteria, searchBean);
			if(searchBean.getRollId() != null){
				criteria.add(Restrictions.eq("rollId", searchBean.getRollId()));
			}		
			if(!StringUtils.isBlank(searchBean.getRollNum())){							
				criteria.add(Restrictions.ilike("rollNum", searchBean.getRollNum(), MatchMode.ANYWHERE));		
			}
			if(!StringUtils.isBlank(searchBean.getRollTag())){							
				criteria.add(Restrictions.ilike("rollTag", searchBean.getRollTag(), MatchMode.ANYWHERE));		
			}
			if(searchBean.getParentRollId() != null){
				criteria.add(Restrictions.eq("parentRollId", searchBean.getParentRollId()));
			}
			if(!StringUtils.isBlank(searchBean.getMachineId())){							
				criteria.add(Restrictions.eq("machineId", searchBean.getMachineId()));
			}
			if(searchBean.getMachineOrdering() != null){
				criteria.add(Restrictions.eq("machineOrdering", searchBean.getMachineOrdering()));
			}	
			if(!StringUtils.isBlank(searchBean.getRollType())){							
				criteria.add(Restrictions.eq("rollType.id", searchBean.getRollType()));
			}
			if(searchBean.getTypeIn() != null && searchBean.getTypeIn().length > 0){
				criteria.add(Restrictions.in("rollType.id", searchBean.getTypeIn()));		
			}
			if(searchBean.getLength() != null){
				criteria.add(Restrictions.eq("length", searchBean.getLength()));
			}	
			if(searchBean.getWeight() != null){
				criteria.add(Restrictions.eq("weight", searchBean.getWeight()));
			}	
			if(!StringUtils.isBlank(searchBean.getPaperType())){							
				criteria.add(Restrictions.eq("paperType.id", searchBean.getPaperType()));		
			}
			if(!StringUtils.isBlank(searchBean.getStatus())){							
				criteria.add(Restrictions.eq("status.id", searchBean.getStatus()));		
			}
			if(searchBean.getStatusIn() != null && searchBean.getStatusIn().length > 0){							
				criteria.add(Restrictions.in("status.id", searchBean.getStatusIn()));		
			}
			if(searchBean.getUtilization() != null){
				criteria.add(Restrictions.eq("utilization", searchBean.getUtilization()));
			}
			if(searchBean.getHours() != null){
				criteria.add(Restrictions.eq("hours", searchBean.getHours()));
			}	
			
			if(!StringUtils.isBlank(searchBean.getSearchRollIdPart())){					
				criteria.add(Restrictions.sqlRestriction(" {alias}.Roll_Id LIKE '"+searchBean.getSearchRollIdPart()+"%' "));
			}
			
			if(!StringUtils.isBlank(searchBean.getSearchRollType())){							
				criteria.add(Restrictions.ilike("rollType.id", searchBean.getSearchRollType(), MatchMode.ANYWHERE));		
			}

			if(!StringUtils.isBlank(searchBean.getSearchStatus())){							
				criteria.add(Restrictions.ilike("status.id", searchBean.getSearchStatus(), MatchMode.ANYWHERE));		
			}
			
			if(!StringUtils.isBlank(searchBean.getSearchLength())){					
				criteria.add(Restrictions.sqlRestriction(" {alias}.Length LIKE '"+searchBean.getSearchLength()+"%' "));
			}
			
			if(!StringUtils.isBlank(searchBean.getSearchMachineId())){					
				criteria.add(Restrictions.sqlRestriction(" {alias}.Machine_Id LIKE '"+searchBean.getSearchMachineId()+"%' "));
			}

			if(!StringUtils.isBlank(searchBean.getSearchPaperType())){					
				criteria.add(Restrictions.ilike("paperType.id", searchBean.getSearchPaperType(), MatchMode.ANYWHERE));	
			}
			
			if(!StringUtils.isBlank(searchBean.getSearchHours())){					
				criteria.add(Restrictions.sqlRestriction(" {alias}.hours LIKE '"+searchBean.getSearchHours()+"%' "));	
			}
			
			if(!StringUtils.isBlank(searchBean.getSearchUtilization())){					
				criteria.add(Restrictions.sqlRestriction(" {alias}.utilization LIKE '"+searchBean.getSearchUtilization()+"%' "));	
			}
			
			if(searchBean.getCreationDateExact() != null){				
				super.addDateRangeCriteria(
						criteria,"createdDate", 
						DateUtil.getStart(searchBean.getCreationDateExact()),
						DateUtil.getEnd(searchBean.getCreationDateExact())
						);
			}
			
		}else{
			//instantiate a new search bean for the defaults
			searchBean = new RollSearchBean();
		}
		
		if(searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0){
			criteria.setMaxResults(searchBean.getMaxResults());
		}

		if(searchBean.getResultOffset() != null){
			criteria.setFirstResult(searchBean.getResultOffset());
		}
		
		if(searchBean.isListing()){
			criteria.setFetchMode("alljobs",  FetchMode.SELECT);			
			criteria.setFetchMode("rollType",  FetchMode.SELECT);
			criteria.setFetchMode("paperType",  FetchMode.SELECT);
			criteria.setFetchMode("status",  FetchMode.SELECT);			
		}
		
		//criteria.setCacheable(true);
	    //criteria.setCacheRegion("com.epac.cap");
		addOrderBy(searchBean.getOrderByList(),criteria);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);  	
		List<Roll> l=criteria.list();
		
		if(searchBean.isListing()){
			for(Roll r : l){
				r.getAlljobs().clear();
			}
		}
		
		return l;
	}
	
	/** 
	 * count
	 */	 
	public Integer getCount() {
		Session session = getHibernateSession();
		String sql = "select count(*) from roll";
		SQLQuery query = session.createSQLQuery(sql);
		query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return  ((Number) query.uniqueResult()).intValue();
	}
	
	
	/** 
	 * count
	 */	 
	public List<Roll> fullSearch(String word, Integer maxResult, Integer offset) {		
		Criteria criteria = createCriteria(Roll.class);
		
		List<Criterion> criterions = new ArrayList<Criterion>();
		try {
			Criterion c1 = Restrictions.eq("rollId", Integer.valueOf(word));
			criterions.add(c1);
		} catch (NumberFormatException e) {}
		
		Criterion c2 = Restrictions.ilike("rollType.id", word, MatchMode.ANYWHERE);
		criterions.add(c2);
		
		Criterion c3 = Restrictions.ilike("status.id", word, MatchMode.ANYWHERE);
		criterions.add(c3);

		try {
			Criterion c4 = Restrictions.eq("length", Integer.valueOf(word));
			criterions.add(c4);
		} catch (NumberFormatException e) {}
		
		Criterion c5 = Restrictions.ilike("machineId", word, MatchMode.ANYWHERE);
		criterions.add(c5);
		
		Criterion c6 = Restrictions.ilike("paperType.id", word, MatchMode.ANYWHERE);
		criterions.add(c6);
		
		try {
			Criterion c7 = Restrictions.eq("hours", Float.valueOf(word));
			criterions.add(c7);
		} catch (NumberFormatException e) {}

		
		try {
			Criterion c8 = Restrictions.eq("utilization", Integer.valueOf(word));
			criterions.add(c8);
		} catch (NumberFormatException e) {}
		
		Criterion[] criterionArray = new Criterion[criterions.size()];
		criterionArray = criterions.toArray(criterionArray);		
		criteria.add( Restrictions.or(criterionArray) );
		
		
		if(maxResult!=null)criteria.setMaxResults(maxResult);
		if(offset!=null)criteria.setFirstResult(offset);

		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<Roll> l=criteria.list();
		return l;
		
	}
	
	
	/** 
	 * ids
	 */	 
	public List<Integer> getIdsList() {
		List<Integer> ids = new ArrayList<Integer>();

		try {
			Session session = getHibernateSession();
			String sql = "select Roll_Id from roll";
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

	public List<Roll> fetchRoll() {
		String sql = "select Roll_Id,Length,Creation_Date,Paper_Type,Hours,Roll_Tag,Roll_Type,Status from roll where Status = 'SCHEDULED' and Roll_Type = 'NEW'";
		Query query = getEntityManager().createNativeQuery(sql);
		 List<Object[]> results = query.getResultList();
		 LogUtils.debug("result All :"+results.size());
		 List<Roll> rolls = new ArrayList<>();
		 for(Object[]r:results){
			 RollStatus status= lookupDAO.read(RollStatus.statuses.valueOf(r[7].toString()).toString(), RollStatus.class);
			 Integer rollId = null;
			 Integer length = null;
			 Date CreationDate = null;
			 PaperType Paper = null;
			 Float hours = null;
			 RollType type = lookupDAO.read(r[6].toString(), RollType.class);
             String rollTag = null;
			 try{
				 rollId = Integer.parseInt(r[0].toString());
				 length = Integer.parseInt(r[1].toString());
				 CreationDate = (Date)r[2];
				 Paper = lookupDAO.read(r[3].toString(),PaperType.class);
				 hours = Float.valueOf(r[4].toString());
				 rollTag = r[5].toString();
				 
			 }catch(Exception e){
				 
			 }
			
			 
			 Roll roll = new Roll(rollId,length,CreationDate,Paper,hours,rollTag,type,status);
			 rolls.add(roll);
		 }
		 
		return rolls;
	}
	
}