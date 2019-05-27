package com.epac.cap.repository;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.LoadTag;
import com.epac.cap.model.Part;
import com.epac.cap.model.Part.PartsCategory;
import com.epac.cap.model.PartCritiria;
import com.epac.cap.model.PartCritiriaId;
import com.epac.cap.model.SubPart;
import com.epac.cap.model.SubPartId;

/**
 * Interacts with Part data.  Uses an entity manager for entity persistence.
 *
 * @author walid
 *
 */
@Repository
public class PartDAO extends BaseEntityPersister  {

	/** 
	 * create the bean
	 */	 
	public void create(Part bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * create the sub part bean
	 */	 
	public void createSubPart(SubPart bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * update the bean
	 */	 
	public void update(Part bean) {
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * delete the bean
	 */	 
	public boolean delete(Part bean) {		
		if (bean != null){
			//need to re-retrieve the instance b/c it was likely retrieved in a separate
			//transaction (during action.prepareDelete()) making it detached now
			getEntityManager().remove(read(bean.getPartNum()));
			getEntityManager().flush();
			return true;
		}else{
			return false;
		}
	}
	
	/** 
	 * delete the subpart bean
	 */	 
	public boolean deleteSubPart(SubPart bean) {		
		if (bean != null){
			//need to re-retrieve the instance b/c it was likely retrieved in a separate
			//transaction (during action.prepareDelete()) making it detached now
			getEntityManager().remove(readSubPart(bean.getId()));
			getEntityManager().flush();
			return true;
		}else{
			return false;
		}
	}
	
	/** 
	 * delete the partCriteria
	 */	 
	public boolean deletePC(PartCritiria bean) {		
		if (bean != null){
			//need to re-retrieve the instance b/c it was likely retrieved in a separate
			//transaction (during action.prepareDelete()) making it detached now
			getEntityManager().remove(readPartCritiria(bean.getId()));
			getEntityManager().flush();
			return true;
		}else{
			return false;
		}
	}

	/** 
	 * read the bean by id
	 */	 
	public Part read(String partNum) {
		return getEntityManager().find(Part.class, partNum);
	}
	
	/** 
	 * read the subPart bean by id
	 */	 
	public SubPart readSubPart(SubPartId SubPartId) {
		return getEntityManager().find(SubPart.class, SubPartId);
	}
	
	/** 
	 * read the partCritiria bean by id
	 */	 
	public PartCritiria readPartCritiria(PartCritiriaId partCritiriaId) {
		return getEntityManager().find(PartCritiria.class, partCritiriaId);
	}
	
	/** 
	 * generates new part id
	 */	 
	public Number getLastPartNb() {
		Session session = getHibernateSession();
		try {
			//String sql = "select Part_Num from part Order By Creation_Date desc limit 1";
			String sql = "SELECT CAST(SUBSTRING(Part_Num, 3) as unsigned) as pn FROM part ORDER BY pn desc limit 1";
			SQLQuery query = session.createSQLQuery(sql);
			
			/*StringBuilder result = new StringBuilder((String) query.uniqueResult());
			result.replace(0, 2, "");
			if(result.indexOf("T") > -1 || result.indexOf("C") > -1){
				result.replace(result.length() - 1, result.length() - 1, "");
			}
			try {
				return  NumberFormat.getInstance().parse(result.toString());
			} catch (ParseException e) {
				return 0;
			}*/
			
			return  (Number) query.uniqueResult();
		} catch (Exception e) {
			return 0;
		}
	}
	public Part findPartByIsbn(String isbn) {
		Session session = getHibernateSession();
		String sql = "select part from Part part where isbn = '"+isbn+"' limit 1";
		SQLQuery query = session.createSQLQuery(sql);
		
	return (Part)query.uniqueResult();
	
	}

	public List<String> allIsbn() {
		String sql = "select isbn from part ";
		Query query = getEntityManager().createNativeQuery(sql);
		return (List<String> )query.getResultList();
	}
	/** 
	 * read all the beans
	 */	 
	@SuppressWarnings("unchecked")
	public List<Part> readAll(PartSearchBean searchBean) {
		Criteria criteria = createCriteria(Part.class);
		if(searchBean != null){
			super.addAuditableCriteria(criteria, searchBean);
			if(!StringUtils.isBlank(searchBean.getPartNum())){
				criteria.add(Restrictions.eq("partNum", searchBean.getPartNum()));					
			}
			if(!StringUtils.isBlank(searchBean.getPartNumStart())){
				criteria.add(Restrictions.ilike("partNum", searchBean.getPartNumStart(), MatchMode.START));					
			}
			if(!StringUtils.isBlank(searchBean.getPartNumLike())){
				criteria.add(Restrictions.ilike("partNum", searchBean.getPartNumLike(), MatchMode.ANYWHERE));					
			}
			if(!StringUtils.isBlank(searchBean.getIsbn())){							
				criteria.add(Restrictions.ilike("isbn", searchBean.getIsbn(), MatchMode.ANYWHERE));		
			}
			if(!StringUtils.isBlank(searchBean.getIsbnExact())){							
				criteria.add(Restrictions.eq("isbn", searchBean.getIsbnExact()));		
			}
			if(!StringUtils.isBlank(searchBean.getTitle())){							
				criteria.add(Restrictions.ilike("title", searchBean.getTitle(), MatchMode.ANYWHERE));		
			}
			if(searchBean.getVersion() != null){
				criteria.add(Restrictions.eq("version", searchBean.getVersion()));
			}
			if(!StringUtils.isBlank(searchBean.getAuthor())){							
				criteria.add(Restrictions.ilike("author", searchBean.getAuthor(), MatchMode.ANYWHERE));		
			}
			if(!StringUtils.isBlank(searchBean.getFilePath())){							
				criteria.add(Restrictions.ilike("filePath", searchBean.getFilePath(), MatchMode.ANYWHERE));		
			}
			if(!StringUtils.isBlank(searchBean.getFileName())){							
				criteria.add(Restrictions.ilike("fileName", searchBean.getFileName(), MatchMode.ANYWHERE));		
			}
			if(searchBean.getSoftDelete() != null){
				criteria.add(Restrictions.eq("softDelete", searchBean.getSoftDelete()));
			}
			if(searchBean.getActiveFlag() != null){
				criteria.add(Restrictions.eq("activeFlag", searchBean.getActiveFlag()));
			}
			/*if(searchBean.getNonNullIsbns() != null){
				if(Boolean.TRUE.equals(searchBean.getNonNullIsbns())){
					criteria.add(Restrictions.isNotNull("isbn"));
				}
			}*/
			if(Boolean.TRUE.equals(searchBean.getHasNoParent())){
				criteria.add(Restrictions.isEmpty("topParts"));
			}
			if(!StringUtils.isBlank(searchBean.getBindingTypeId())){							
				criteria.add(Restrictions.eq("bindingType.id", searchBean.getBindingTypeId()));		
			}
			if(searchBean.getPagesCount() != null){
				criteria.add(Restrictions.eq("pagesCount", searchBean.getPagesCount()));
			}	
			if(searchBean.getThickness()!= null){							
				criteria.add(Restrictions.eq("thickness", searchBean.getThickness()));		
			}
			if(!StringUtils.isBlank(searchBean.getCategoryId())){							
				criteria.add(Restrictions.eq("category.id", searchBean.getCategoryId()));		
			}
			if(!StringUtils.isBlank(searchBean.getColors())){							
				criteria.add(Restrictions.ilike("colors", searchBean.getColors(), MatchMode.ANYWHERE));		
			}
			if(!StringUtils.isBlank(searchBean.getPaperType())){							
				criteria.add(Restrictions.eq("paperType.id", searchBean.getPaperType()));		
			}
			if(!StringUtils.isBlank(searchBean.getLamination())){							
				criteria.add(Restrictions.eq("lamination.id", searchBean.getLamination()));		
			}
			if(!StringUtils.isBlank(searchBean.getCritiriaId())){
				criteria.createAlias("partCritirias", "partCritirias", CriteriaSpecification.LEFT_JOIN);
				criteria.add(Restrictions.eq("partCritirias.id.critiriaId", searchBean.getCritiriaId()));		
			}
			if(!StringUtils.isBlank(searchBean.getNotes())){							
				criteria.add(Restrictions.ilike("notes", searchBean.getNotes(), MatchMode.ANYWHERE));		
			}
		}else{
			//instantiate a new search bean for the defaults
			searchBean = new PartSearchBean();
		}
		if(searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0){
			criteria.setMaxResults(searchBean.getMaxResults());
		}
		
		if(searchBean.getResultOffset() != null){
			criteria.setFirstResult(searchBean.getResultOffset());
		}
		
		if(searchBean.isListing()){
			criteria.setFetchMode("bindingType",  FetchMode.SELECT);
			criteria.setFetchMode("category",  FetchMode.SELECT);
			criteria.setFetchMode("paperType",  FetchMode.SELECT);
			criteria.setFetchMode("lamination",  FetchMode.SELECT);
			
			criteria.setFetchMode("subParts", FetchMode.SELECT);
			criteria.setFetchMode("topParts", FetchMode.SELECT);
			criteria.setFetchMode("partCritirias", FetchMode.SELECT);
			criteria.setFetchMode("dataSupports", FetchMode.SELECT);
		    criteria.setFetchMode("workflows", FetchMode.SELECT);
		}
		
		criteria.setCacheable(true);
	    criteria.setCacheRegion("com.epac.cap");
		addOrderBy(searchBean.getOrderByList(),criteria);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<Part> l=criteria.list();
		return l;
	}
	
	@SuppressWarnings("unchecked")
	public List<Part> readDistinctIsbn() {
		String sql = "select DISTINCT p.ISBN from part p where p.Part_Num in(select j.`Part_Num` FROM `job` j WHERE j.Status in('RUNNING','COMPLETE','COMPLETE_PARTIAL') "+
	     "and j.Station_Id = 'BINDER' and j.Order_Id in (select o.Order_Id from order_T o where o.Status in ('ONPROD', 'COMPLETE')))";
		 //   Criteria crit = createCriteria(Part.class).addQueryHint(sql);
		   // crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).setResultTransformer(Transformers.aliasToBean(Part.class));
		    Session session = getHibernateSession();
		    SQLQuery query = session.createSQLQuery(sql);
			query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			List<String> lst = (List<String>) query.list();
			List<Part> parts = new ArrayList<>(lst.size());
			 for(String isbn : lst){
				 Part p = new Part();
				 p.setIsbn(isbn);
				 parts.add(p);
			 }
		return parts;
	}
	
	public Part readTopPart(String subPartNum){
		String sql = "select * from part p where p.Part_Num  = (select sub.Top_Part_Num FROM sub_Part sub WHERE sub.Sub_Part_Num = '"+subPartNum+"')";
		 Query query = getEntityManager().createNativeQuery(sql, Part.class);
		 List l = query.getResultList();
		 if(!l.isEmpty()){
			 return (Part) l.get(0);
		 }
		 return null;
	}
	
	public Part readByIsbn(String isbn) {
		
		PartSearchBean searchBean = new PartSearchBean();
		searchBean.setIsbn(isbn);
		searchBean.setCategoryId(PartsCategory.BOOK.getName());
		List<Part> parts = readAll(searchBean);
		Part part = parts.get(0);
		return part;
	}
	public List<Part> fetchDistinctIsbn() throws PersistenceException{
		return readDistinctIsbn();
	/*	String sql ="select distinct `ISBN` FROM `part`";


	Query query = getEntityManager().createNativeQuery(sql,Part.class);
	return (List<Part> )query.getResultList();*/
	}
	
	/** 
	 * count
	 */	 
	public Integer getCount() {
		Session session = getHibernateSession();
		String sql = "select count(*) from part";
		SQLQuery query = session.createSQLQuery(sql);
		query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return  ((Number) query.uniqueResult()).intValue();
	}
	
	/** 
	 * find the lamination
	 */	 
	public String findLamination(String partNum) {
		String sql = "select Lamination from part where Part_Num = '" + partNum + "'";
		Query query = getEntityManager().createNativeQuery(sql);
		return  (String)query.getSingleResult();
	}
	
	/** 
	 * count
	 */	 
	public List<Part> fullSearch(String word, Integer maxResult, Integer offset) {		
		Criteria criteria = createCriteria(Part.class);
		
		List<Criterion> criterions = new ArrayList<Criterion>();
		try {
			Criterion c1 = Restrictions.ilike("partNum", word, MatchMode.ANYWHERE);
			criterions.add(c1);
		} catch (Exception e) {}
		

		try {
			Criterion c2 = Restrictions.ilike("isbn", word, MatchMode.ANYWHERE);
			criterions.add(c2);
		} catch (Exception e) {}

		
		try {
			Criterion c3 = Restrictions.ilike("title", word, MatchMode.ANYWHERE);
			criterions.add(c3);
		} catch (Exception e) {}
		
		
		try {
			Criterion c4 = Restrictions.ilike("category.id", word, MatchMode.ANYWHERE);
			criterions.add(c4);
		} catch (Exception e) {}		

		
		try {
			Criterion c5 = Restrictions.ilike("colors", word, MatchMode.ANYWHERE);
			criterions.add(c5);
		} catch (Exception e) {}	

		
		try {
			Criterion c7 = Restrictions.ilike("bindingType.id", word, MatchMode.ANYWHERE);
			criterions.add(c7);
		} catch (Exception e) {}

		
		try {
			Criterion c8 = Restrictions.ilike("paperType.id", word, MatchMode.ANYWHERE);
			criterions.add(c8);
		} catch (Exception e) {}

		try {
			Criterion c9 = Restrictions.ilike("lamination.id", word, MatchMode.ANYWHERE);
			criterions.add(c9);
		} catch (Exception e) {}
		
		Criterion[] criterionArray = new Criterion[criterions.size()];
		criterionArray = criterions.toArray(criterionArray);		
		criteria.add( Restrictions.or(criterionArray) );
		
		
		if(maxResult!=null)criteria.setMaxResults(maxResult);
		if(offset!=null)criteria.setFirstResult(offset);

		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<Part> l=criteria.list();
		return l;
		
	}
	
}