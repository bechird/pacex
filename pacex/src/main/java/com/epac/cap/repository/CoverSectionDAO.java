package com.epac.cap.repository;

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
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.epac.cap.model.CoverBatch;
import com.epac.cap.model.CoverSection;
import com.epac.cap.model.Lamination;
import com.epac.cap.model.Pallette;
import com.epac.cap.model.PaperType;
import com.epac.cap.model.Roll;
import com.epac.cap.model.RollStatus;
import com.epac.cap.model.RollType;
import com.epac.cap.utils.AoDataParser;
import com.epac.cap.utils.LogUtils;;


/**
 * Interacts with Cover batch data.  Uses an entity manager for entity persistence.
 *
 * @author slimj
 *
 */
@Repository
public class CoverSectionDAO extends BaseEntityPersister {

	/** 
	 * creates the bean
	 */	
	@Autowired
	LookupDAO lookupDAO;
	
	public void create(CoverSection bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * updates the bean
	 */	 
	public void update(CoverSection bean) {
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * deletes the bean
	 */	 
	public boolean delete(CoverSection bean) {		
		if (bean != null){
			//need to re-retrieve the instance b/c it was likely retrieved in a separate
			//transaction (during action.prepareDelete()) making it detached now
			getEntityManager().remove(read(bean.getCoverSectionId()));
			getEntityManager().flush();
			return true;
		}else{
			return false;
		}
	}

	/** 
	 * reads the bean by id
	 */	 
	public CoverSection read(Integer coverSectionId) {
		return getEntityManager().find(CoverSection.class, coverSectionId);
	}

	/** 
	 * reads all the beans
	 */	 
	@SuppressWarnings("unchecked")
	public List<CoverSection> readAll(CoverSectionSearchBean searchBean) {
		Criteria criteria = createCriteria(CoverSection.class);
		if(searchBean != null){
			super.addAuditableCriteria(criteria, searchBean);
			if(searchBean.getCoverSectionId() != null){
				criteria.add(Restrictions.eq("coverSectionId", searchBean.getCoverSectionId()));
			}		
			if(!StringUtils.isBlank(searchBean.getCoverSectionName())){
				criteria.add(Restrictions.eq("coverSectionName", searchBean.getCoverSectionName()));		
			}
			if(!StringUtils.isBlank(searchBean.getMachineId())){	
				criteria.add(Restrictions.eq("machineId", searchBean.getMachineId()));
			}	
			if(searchBean.getQuantity() != null){
				criteria.add(Restrictions.eq("quantity", searchBean.getQuantity()));
			}
			if(!StringUtils.isBlank(searchBean.getPaperType())){	
				criteria.add(Restrictions.eq("paperType", searchBean.getPaperType()));		
			}
			if(!StringUtils.isBlank(searchBean.getStatus())){
				criteria.add(Restrictions.eq("status.id", searchBean.getStatus()));		
			}
			if(!StringUtils.isBlank(searchBean.getStatusName())){
				criteria.add(Restrictions.eq("status.name", searchBean.getStatusName()));		
			}
			if(!StringUtils.isBlank(searchBean.getLaminationTypeId())){
				criteria.add(Restrictions.eq("laminationType.id", searchBean.getLaminationTypeId()));		
			}
			if(!StringUtils.isBlank(searchBean.getPriority())){
				criteria.add(Restrictions.eq("priority", searchBean.getPriority()));
			}
			if(searchBean.getDueDate() != null){
				criteria.add(Restrictions.eq("dueDate", searchBean.getDueDate()));
			}
		}else{
			searchBean = new CoverSectionSearchBean();
		}
		if(searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0){
			criteria.setFetchMode("jobs",  FetchMode.SELECT);
			criteria.setMaxResults(searchBean.getMaxResults());
		}

		if(searchBean.getResultOffset() != null){
			criteria.setFirstResult(searchBean.getResultOffset());
		}
		
		addOrderBy(searchBean.getOrderByList(),criteria);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		List<CoverSection> l=criteria.list();
		return l;
	}
	
	
	/** 
	 * count
	 */	 
	public Integer getCount() {
		Session session = getHibernateSession();
		String sql = "select count(*) from coverSection";
		SQLQuery query = session.createSQLQuery(sql);
		query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return  ((Number) query.uniqueResult()).intValue();
	}
	
	
	public List<CoverSection> fullSearch(String query, Integer maxResult, Integer offset) {		
		Criteria criteria = createCriteria(CoverSection.class);
		
		List<Criterion> criterions = new ArrayList<Criterion>();
		
		Criterion c = AoDataParser.fullSearchCriterionBuilder("coverSectionId", "eq", "integer", query);
		if(c != null)criterions.add(c);

		c = AoDataParser.fullSearchCriterionBuilder("coverSectionName", "ilike", "string", query);
		if(c != null)criterions.add(c);
		
		c = AoDataParser.fullSearchCriterionBuilder("quantity", "eq", "integer", query);
		if(c != null)criterions.add(c);

		c = AoDataParser.fullSearchCriterionBuilder("priority", "ilike", "string", query);
		if(c != null)criterions.add(c);
		
		c = AoDataParser.fullSearchCriterionBuilder("dueDate", "eq", "date", query);
		if(c != null)criterions.add(c);
		
		
		Criterion[] criterionArray = new Criterion[criterions.size()];
		criterionArray = criterions.toArray(criterionArray);		
		criteria.add( Restrictions.or(criterionArray) );
		
		
		criteria.setFetchMode("jobs",  FetchMode.SELECT);
		if(maxResult!=null)criteria.setMaxResults(maxResult);
		if(offset!=null)criteria.setFirstResult(offset);

		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<CoverSection> l=criteria.list();
		return l;
		
	}

	public List<CoverSection> findNewSection() {
		
		String sql = "SELECT Section_Id,Section_Name,Quantity,Lamination,priority FROM `coverSection` where status = 'NEW' ";
		Query query = getEntityManager().createNativeQuery(sql);
		 List<Object[]> results = query.getResultList();
		 LogUtils.debug("result All :"+results.size());
		 List<CoverSection> sections = new ArrayList<>();
		 for(Object[]r:results){
			 String section_Name = null;
			 Integer id = null;
			 Integer quantity = null;
			 Lamination lamination = null;
			 String priority = null;
			 try{
				 id = Integer.parseInt(r[0].toString());
				 section_Name = r[1].toString();
				 quantity = Integer.parseInt(r[2].toString());
				 lamination = lookupDAO.read(r[3].toString(),Lamination.class);
				 priority = r[4].toString();
				 
			 }catch(Exception e){
				 
			 }
			
			 
			 CoverSection section = new CoverSection(section_Name,quantity,lamination,priority);
			 section.setCoverSectionId(id);
			 sections.add(section);
		 }
		 
		return sections;
	}
}
