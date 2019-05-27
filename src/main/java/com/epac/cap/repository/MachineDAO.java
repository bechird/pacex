package com.epac.cap.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.epac.cap.model.Job;
import com.epac.cap.model.Machine;
import com.epac.cap.model.MachineStatus;
import com.epac.cap.model.Pallette;
import com.epac.cap.utils.LogUtils;

/**
 * Interacts with Machine data.  Uses an entity manager for entity persistence.
 *
 * @author walid
 *
 */
@Repository
public class MachineDAO extends BaseEntityPersister {
	@Autowired
	LookupDAO lookupDAO;
	@Autowired
	PalletteDao palletteDao;
	@Autowired
	JobDAO jobDao;

	/** 
	 * creates the bean
	 */	 
	public void create(Machine bean) {
		getEntityManager().persist(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * updates the bean
	 */	 
	public void update(Machine bean) {
		getEntityManager().merge(bean);
		getEntityManager().flush();
	}
	
	/** 
	 * deletes the bean
	 */	 
	public boolean delete(Machine bean) {		
		if (bean != null){
			//need to re-retrieve the instance b/c it was likely retrieved in a separate
			//transaction (during action.prepareDelete()) making it detached now
			getEntityManager().remove(read(bean.getMachineId()));
			getEntityManager().flush();
			return true;
		}else{
			return false;
		}
	}

	/** 
	 * reads the bean
	 */	 
	public Machine read(String machineId) {
		return getEntityManager().find(Machine.class, machineId);
	}

	/** 
	 * reads all beans
	 */	 
	@SuppressWarnings("unchecked")
	public List<Machine> readAll(MachineSearchBean searchBean) {
		Criteria criteria = createCriteria(Machine.class);
		if(searchBean != null){
			super.addAuditableCriteria(criteria, searchBean);
			if(!StringUtils.isBlank(searchBean.getMachineId())){
				criteria.add(Restrictions.eq("machineId", searchBean.getMachineId()));					
			}	
			if(!StringUtils.isBlank(searchBean.getMachineIdDiff())){
				criteria.add(Restrictions.ne("machineId", searchBean.getMachineIdDiff()));					
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
			if(!StringUtils.isBlank(searchBean.getStatus())){							
				criteria.add(Restrictions.eq("status.id", searchBean.getStatus()));		
			}
			if(!StringUtils.isBlank(searchBean.getStatusDiff())){							
				criteria.add(Restrictions.ne("status.id", searchBean.getStatusDiff()));		
			}
			if(!StringUtils.isBlank(searchBean.getStationId())){							
				criteria.add(Restrictions.eq("stationId", searchBean.getStationId()));		
			}
			if(searchBean.getCurrentJobId() != null){
				criteria.add(Restrictions.eq("currentJob.jobId", searchBean.getCurrentJobId()));
			}	
			if(!StringUtils.isBlank(searchBean.getServiceSchedule())){
				criteria.add(Restrictions.ilike("serviceSchedule", searchBean.getServiceSchedule(), MatchMode.ANYWHERE));		
			}
			if(!StringUtils.isBlank(searchBean.getSpeed())){
				criteria.add(Restrictions.ilike("speed", searchBean.getSpeed(), MatchMode.ANYWHERE));		
			}
			if(!StringUtils.isBlank(searchBean.getType())){
				criteria.add(Restrictions.eq("machineType.id", searchBean.getType()));		
			}
		}else{
			//instantiate a new search bean for the defaults
			searchBean = new MachineSearchBean();
		}
		if(searchBean.getMaxResults() != null && searchBean.getMaxResults() > 0){
			criteria.setMaxResults(searchBean.getMaxResults());
		}
		
		if(searchBean.isListing()){
			criteria.setFetchMode("currentJob",  FetchMode.SELECT);
			criteria.setFetchMode("logs",  FetchMode.SELECT);
			criteria.setFetchMode("assignedRolls",  FetchMode.SELECT);
			criteria.setFetchMode("assignedJobs",  FetchMode.SELECT);
			criteria.setFetchMode("assignedSections",  FetchMode.SELECT);
			criteria.setFetchMode("runningRolls",  FetchMode.SELECT);
			criteria.setFetchMode("runningJobs",  FetchMode.SELECT);
			criteria.setFetchMode("runningSections",  FetchMode.SELECT);
			criteria.setFetchMode("pallets",  FetchMode.SELECT);
		}
		
		//criteria.setCacheable(true);
	    //criteria.setCacheRegion("com.epac.cap");
		addOrderBy(searchBean.getOrderByList(),criteria);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);    	
		List<Machine> l=criteria.list();
		
		if(searchBean.isListing()){
			for(Machine m : l){
				m.getLogs().clear();
				m.getAssignedRolls().clear();
				m.getAssignedJobs().clear();
				m.getAssignedSections().clear();
				m.getRunningRolls().clear();
				m.getRunningJobs().clear();
				m.getRunningSections().clear();
				m.getRunningAndAssignedJobs().clear();
				m.getRunningAndAssignedRolls().clear();
				m.getRunningAndAssignedSections().clear();
				m.getPallets().clear();
			}
		}
		return l;
	}
	
	public Integer machinesCount(String stationId, String color){
		Integer result = 1;
		StringBuilder c = new StringBuilder();
		c.append("%").append(color.equals("NA") ? "" : color).append("%");
		
		Session session = getHibernateSession();
		String sql = "select count(*) from machine where station_Id = '" + stationId + "' and status != 'OUTSERVICE' and machineType like '" + c.toString() + "'";
		SQLQuery query = session.createSQLQuery(sql);
		query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		result = ((Number) query.uniqueResult()).intValue();
		
		return result;
	}
	
	public  List<Machine> fetchMachines(){
		
		String sql = "select Machine_Id,Name,Status,OcInputPath,IpAddress from machine ";
		Query query = getEntityManager().createNativeQuery(sql);
		 List<Object[]> results = query.getResultList();
		 List<Machine> machines = new ArrayList<>();
		 for(Object[]m:results){
			 MachineStatus status= lookupDAO.read(MachineStatus.statuses.valueOf(m[2].toString()).toString(), MachineStatus.class);
			 String machineId = null;
			 String name = null;
			 String ocInputPath = null;
			 String IpAdress = null;
			 try{
				 machineId = m[0].toString();
				 name = m[1].toString();
				 ocInputPath = m[3].toString();
				 IpAdress = m[4].toString();
			 }catch(Exception e){
				 
			 }
			
			 
			 Machine machine = new Machine(machineId,name,status,ocInputPath,IpAdress);
			 machines.add(machine);
		 }
		 
		 return machines;
		
	}
	
public  Machine fetchShippingMachines(String machineId){
		
		String sql = "select m.Machine_Id,m.Name,m.Status,m.OcInputPath,m.IpAddress ,m.Current_Job_Id,m.Station_Id,p.id from machine as m join Pallette as p  where p.Machine_Id = m.Machine_Id and p.status in ('PAUSED','ACTIVE') and m.Machine_Id ='"+machineId+"'" ;
		Query query = getEntityManager().createNativeQuery(sql);
		 List<Object[]> results = query.getResultList();
		 Machine machine = new Machine();
		 if(results.size() > 0) {
			 Object[] mach = results.get(0);
			 MachineStatus status= lookupDAO.read(MachineStatus.statuses.valueOf(mach[2].toString()).toString(), MachineStatus.class);
			 String name = null;
			 List<Pallette> pallets = new ArrayList<>();
			 String ocInputPath = null;
			 String IpAdress = null;
			 Integer jobId = null;
			 String stationId = null;
			 try{
				 machineId = mach[0].toString();
				 name = mach[1].toString();
				 if(mach[3] != null )ocInputPath = mach[3].toString();
				 if(mach[4] != null )IpAdress = mach[4].toString();
				 if(mach[5] != null)jobId = Integer.parseInt(mach[5].toString());
				 if(mach[6] != null)stationId = mach[6].toString();
			 }catch(Exception e){
				 
			 }	 
			  machine = new Machine(machineId,name,status,ocInputPath,IpAdress);
			  machine.setStationId(stationId);
			  if(jobId !=null) {
			  Job job = jobDao.read(jobId);
			  machine.setCurrentJob(job);
			  }
			  
		 }
		 
		 for(Object[]m:results){
			 Long idPallette = null ;

			 idPallette = Long.parseLong(m[7].toString());
			 Pallette pallete = palletteDao.read(idPallette);
			 machine.getPallets().add(pallete);
			
		 }
		 
		 return machine;
		
	}
	
}

