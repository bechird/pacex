package com.epac.cap.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.epac.cap.model.DefaultStation;
import com.epac.cap.model.DefaultStationId;
import com.epac.cap.model.Machine;
import com.epac.cap.model.MachineStatus;
import com.epac.cap.model.Station;

/**
 * Interacts with Station data.  Uses an entity manager for entity persistence.
 *
 * @author walid
 *
 */
@Repository
public class StationDAO extends BaseEntityPersister {

	/** 
	 * creates the bean
	 */	 
	public void create(Station bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * creates the default station bean
	 */	 
	public void createDefault(DefaultStation bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * updates the bean
	 */	 
	public void update(Station bean) {
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * updates the default station bean
	 */	 
	public void updateDefault(DefaultStation bean) {
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * deletes the bean
	 */	 
	public boolean delete(Station bean) {		
		if (bean != null){
			//need to re-retrieve the instance b/c it was likely retrieved in a separate
			//transaction (during action.prepareDelete()) making it detached now
			getEntityManager().remove(read(bean.getStationId()));
			getEntityManager().flush();
			return true;
		}else{
			return false;
		}
	}
	
	/** 
	 * deletes the default station bean
	 */	 
	public boolean deleteDefault(DefaultStation bean) {		
		if (bean != null){
			//need to re-retrieve the instance b/c it was likely retrieved in a separate
			//transaction (during action.prepareDelete()) making it detached now
			getEntityManager().remove(readDefault(bean.getId()));
			getEntityManager().flush();
			return true;
		}else{
			return false;
		}
	}

	/** 
	 * reads the bean
	 */	 
	public Station read(String stationId) {
		return getEntityManager().find(Station.class, stationId);
	}

	/** 
	 * reads the default station bean
	 */	 
	public DefaultStation readDefault(DefaultStationId id) {
		return getEntityManager().find(DefaultStation.class, id);
	}
	
	/** 
	 * reads all the beans
	 */	 
	@SuppressWarnings("unchecked")
	public List<Station> readAll(StationSearchBean searchBean) {
		Criteria criteria = createCriteria(Station.class);
		//criteria.createAlias("jobs", "jobs", CriteriaSpecification.LEFT_JOIN);
		//criteria.createAlias("machines", "machines", CriteriaSpecification.LEFT_JOIN);
		if(searchBean != null){
			super.addAuditableCriteria(criteria, searchBean);
			if(!StringUtils.isBlank(searchBean.getStationId())){
				criteria.add(Restrictions.eq("stationId", searchBean.getStationId()));					
			}
			if(!StringUtils.isBlank(searchBean.getStationIdDiff())){
				criteria.add(Restrictions.ne("stationId", searchBean.getStationIdDiff()));					
			}	
			if(!StringUtils.isBlank(searchBean.getParentStationId())){							
				criteria.add(Restrictions.eq("parentStationId", searchBean.getParentStationId()));		
			}
			if(!StringUtils.isBlank(searchBean.getName())){							
				criteria.add(Restrictions.ilike("name", searchBean.getName(), MatchMode.ANYWHERE));		
			}
			if(!StringUtils.isBlank(searchBean.getNameExact())){							
				criteria.add(Restrictions.eq("name", searchBean.getNameExact()));		
			}
			if(!StringUtils.isBlank(searchBean.getDescription())){							
				criteria.add(Restrictions.ilike("description", searchBean.getDescription(), MatchMode.ANYWHERE));		
			}
			if(searchBean.getScheduledHours() != null){
				criteria.add(Restrictions.eq("scheduledHours", searchBean.getScheduledHours()));
			}	
			if(searchBean.getUnscheduledHours() != null){
				criteria.add(Restrictions.eq("unscheduledHours", searchBean.getUnscheduledHours()));
			}	
			if(searchBean.getProductionCapacity() != null){
				criteria.add(Restrictions.eq("productionCapacity", searchBean.getProductionCapacity()));
			}	
			if(searchBean.getProductionOrdering() != null){
				criteria.add(Restrictions.eq("productionOrdering", searchBean.getProductionOrdering()));
			}	
			if(!StringUtils.isBlank(searchBean.getInputType())){							
				criteria.add(Restrictions.eq("inputType", searchBean.getInputType()));		
			}
			if(searchBean.getActiveFlag() != null){
				criteria.add(Restrictions.eq("activeFlag", searchBean.getActiveFlag()));
			}

			if(!StringUtils.isBlank(searchBean.getStationCategoryId())){							
				criteria.add(Restrictions.eq("stationCategoryId", searchBean.getStationCategoryId()));		
			}
		}else{
			//instantiate a new search bean for the defaults
			searchBean = new StationSearchBean();
		}
		if(searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0){
			criteria.setMaxResults(searchBean.getMaxResults());
		}
		
		if(searchBean.isListing()){
			criteria.setFetchMode("jobs",  FetchMode.SELECT);
			criteria.setFetchMode("rolls",  FetchMode.SELECT);
			criteria.setFetchMode("machines",  FetchMode.SELECT);
		}
		
		addOrderBy(searchBean.getOrderByList(),criteria);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		List<Station> l=criteria.list();
		
		if(searchBean.isListing()){
			for(Station s : l){
				s.getMachines().clear();
			}
		}
		return l;
	}
	
	/** 
	 * reads all the default station beans
	 */	 
	@SuppressWarnings("unchecked")
	public List<DefaultStation> readAllDefault(StationSearchBean searchBean) {
		Criteria criteria = createCriteria(DefaultStation.class);
		//criteria.createAlias("jobs", "jobs", CriteriaSpecification.LEFT_JOIN);
		//criteria.createAlias("machines", "machines", CriteriaSpecification.LEFT_JOIN);
		if(searchBean != null){
			super.addAuditableCriteria(criteria, searchBean);
			if(!StringUtils.isBlank(searchBean.getPartCategoryId())){
				criteria.add(Restrictions.eq("id.categoryId", searchBean.getPartCategoryId()));					
			}
			if(!StringUtils.isBlank(searchBean.getPartCritiriaId())){
				criteria.add(Restrictions.eq("id.critiriaId", searchBean.getPartCritiriaId()));					
			}
			if(!StringUtils.isBlank(searchBean.getBindingTypeId())){
				criteria.add(Restrictions.eq("id.bindingTypeId", searchBean.getBindingTypeId()));					
			}
			if(!StringUtils.isBlank(searchBean.getStationCategoryId())){							
				criteria.add(Restrictions.eq("id.stationCategoryId", searchBean.getStationCategoryId()));		
			}
			
			if(searchBean.getProductionOrdering() != null){
				criteria.add(Restrictions.eq("productionOrdering", searchBean.getProductionOrdering()));
			}	
			
		}else{
			//instantiate a new search bean for the defaults
			searchBean = new StationSearchBean();
		}
		if(searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0){
			criteria.setMaxResults(searchBean.getMaxResults());
		}
		//criteria.setCacheable(true);
	    //criteria.setCacheRegion("com.epac.cap");
		addOrderBy(searchBean.getOrderByList(),criteria);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<DefaultStation> l=criteria.list();
		return l;
	}

	public String fetchInputType(String stationId) {
		String sql = "select Input_Type from station s where s.Station_Id = '"+stationId +"'";

		Query query = getEntityManager().createNativeQuery(sql);
		return (String) query.getSingleResult();
	}

	public List<String[]> fetchStationsMenu() {
		String sql = "select Station_Id, Name, Input_Type, Active_Flag from station order by production_ordering, station_id desc";
		Query query = getEntityManager().createNativeQuery(sql);
		
		return (List<String[]>) query.getResultList();
	}
	
	public String readStationName(String stationId){
		String sql = "select Name from station s where s.Station_Id = '"+stationId +"'";
		Query query = getEntityManager().createNativeQuery(sql);
		return (String) query.getSingleResult();
	}

	public List<Object[]> getNameOfSations() {
		String sql = "select  Name,Station_Id from station ";
		Query query = getEntityManager().createNativeQuery(sql);
		return (List<Object[]>) query.getResultList();
	}
	
public  List<Station> fetchStation(){
		
		String sql = "select Station_Id,Name,Station_Category_Id,Description,Active_Flag,Production_Ordering from station ";
		Query query = getEntityManager().createNativeQuery(sql);
		 List<Object[]> results = query.getResultList();
		 List<Station> stations = new ArrayList<>();
		 for(Object[]m:results){
			 String stationId = null;
			 String name = null;
			 String station_Category_Id = null;
			 String description = null;
			 Boolean Active_Flag =  null;
			 Integer Production_Ordering = null;
			 try{
				 stationId = m[0].toString();
				 name = m[1].toString();
				 station_Category_Id = m[2].toString();
				 description = m[3].toString();
				 Active_Flag = Boolean.valueOf(m[4].toString());
				 Production_Ordering = Integer.valueOf(m[5].toString()); 
			 }catch(Exception e){
				 
			 }
			
			 
			 Station station = new Station(stationId,name,station_Category_Id,description,Active_Flag,Production_Ordering);
			 stations.add(station);
		 }
		 
		 return stations;
		
	}
	
}