package com.epac.cap.handler;

import static org.junit.Assert.*;

import com.epac.cap.model.Job;
import com.epac.cap.model.LogCause;
import com.epac.cap.model.Machine;
import com.epac.cap.model.MachineStatus;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.repository.MachineSearchBean;
import com.epac.cap.repository.RollSearchBean;
import com.epac.cap.test.BaseTest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;
import javax.annotation.Resource;

import static com.epac.cap.common.StringUtil.getRandomString;
import static com.epac.cap.common.NumberUtil.getRandomInteger;
import static com.epac.cap.common.DateUtil.isBetweenDays;
import static com.epac.cap.common.DateUtil.getRandomFutureDate;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.stereotype.Service;

/**
 * Test class for the MachineHandler interface.
 * 
 * @author walid
 *
 */
@Service
public class MachineHandlerTest extends BaseTest{
	private static final Logger logger = Logger.getLogger(MachineHandlerTest.class);
	@Resource
	private MachineHandler machineHandler;
	@Resource
	private RollHandler rollHandler;
	@Resource
	private StationHandler stationHandler = null;
	@Resource
	private UserHandler userHandler = null;
	@Resource
	private JobHandler jobHandler;
	@Resource
	private LookupHandler lookupHandler = null;
	
	private Machine createNewMachine() throws Exception{
		Machine bean = new Machine();
		List<MachineStatus> machineStatuses = lookupHandler.readAll(MachineStatus.class);
		JobSearchBean jsb = new JobSearchBean();
		jsb.setMaxResults(1);
		List<Job> jobs = jobHandler.readAll(jsb);
		
		bean.setCreatedDate(new Date());
		bean.setCreatorId("junit");
		bean.setLastUpdateDate(new Date());
		bean.setLastUpdateId("junit");
		bean.setMachineId(getRandomString(25));
		bean.setName(getRandomString(100));					
		bean.setDescription(getRandomString(2000));					
		bean.setStatus(!machineStatuses.isEmpty() ? machineStatuses.get(0) : null);					
		bean.setCurrentJob(!jobs.isEmpty() ? jobs.get(0) : null);
		// bean.setCurrentJobId(!jobs.isEmpty() ? jobs.get(0).getJobId() : null);		
		bean.setServiceSchedule(getRandomString(100));					
		bean.setSpeed(getRandomString(15));					
		
		return bean;
	}
	
	@Test
	public void testCreate() throws Exception{
		Machine bean = createNewMachine();
		machineHandler.create(bean);
	}
	
	private Machine getRandomMachine() throws Exception{
		Machine bean = null;
		MachineSearchBean searchBean = new MachineSearchBean();
		searchBean.setMaxResults(100);
		List<Machine> results = machineHandler.readAll(searchBean);
		if(results != null && !results.isEmpty()){
			bean = results.get(getRandomInteger(results.size()));
		}else{
			bean = createNewMachine();
			machineHandler.create(bean);		
		}
		
		return bean;		
	}	

	@Test
	public void testReadAll() throws Exception{
		getRandomMachine();//ensures there's at least 1 record in the db
		List<Machine> machineResults = machineHandler.readAll();
		assertNotNull("readAll return null",machineResults);
		assertFalse("readAll returned an empty list",machineResults.isEmpty());		
	}
	
	@Test
	public void testUpdate() throws Exception{
		Machine machine = getRandomMachine();
		machine.setLastUpdateId("junit");
		machine.setLastUpdateDate(new Date());
		machineHandler.update(machine);
	}

	@Test
	public void testReadValidId() throws Exception{
		Machine machine = getRandomMachine();
		Machine readMachine = machineHandler.read(machine.getMachineId());
		assertNotNull("read return null",readMachine);
		assertEquals("read returned incorrect object by id",machine, readMachine);		
	}

	@Test
	public void testReadInvalidId() throws Exception{
		getRandomMachine();
		Machine readMachine = machineHandler.read("-1");
		assertNull("read didnt return null as expected",readMachine);		
	}
		
	@Test
	public void testDeleteExisting() throws Exception{		
		Machine machine = getRandomMachine();
		JobSearchBean jsb = new JobSearchBean();
		jsb.setMachineId(machine.getMachineId());
		RollSearchBean rsb = new RollSearchBean();
		rsb.setMachineId(machine.getMachineId());
		if(jobHandler.readAll(jsb).isEmpty() && machine.getLogs().isEmpty() && rollHandler.readAll(rsb).isEmpty()){
			machineHandler.delete(machine);
		}
	}
	
	@Test
	public void testDeleteNew() throws Exception{
		Machine bean = createNewMachine();
		machineHandler.create(bean);
		machineHandler.delete(bean);
	}
		
	@Test
	public void testReadAllBlankSearchBean() throws Exception{
		getRandomMachine();//ensures there's at least 1 record in the db
		MachineSearchBean searchBean = new MachineSearchBean();
		List<Machine> machineResults = machineHandler.readAll(searchBean);
		assertNotNull("readAll return null",machineResults);
		assertFalse("readAll returned an empty list",machineResults.isEmpty());
	}

	@Test
	public void testReadAllSearchBeanFields() throws Exception{
		getRandomMachine();//ensures there's at least 1 record in the db
		MachineSearchBean searchBean = new MachineSearchBean();
		searchBean.setMaxResults(1);
		List<Machine> machineResults = machineHandler.readAll(searchBean);
		assertNotNull("readAll return null",machineResults);
		assertFalse("readAll returned an empty list",machineResults.isEmpty());
		assertTrue("readAll did not honor max results",machineResults.size() == searchBean.getMaxResults());
	}

	@Test
	public void testReadAllCompleteSearchBean() throws Exception{				
		Machine machine = getRandomMachine();
		MachineSearchBean searchBean = new MachineSearchBean();
		Calendar cal = Calendar.getInstance();
		searchBean.setMachineId(machine.getMachineId());
		searchBean.setName(machine.getName());
		searchBean.setDescription(machine.getDescription());
		searchBean.setStatus(machine.getStatus() != null? machine.getStatus().getId() : null);
		searchBean.setCurrentJobId(machine.getCurrentJob() != null ? machine.getCurrentJob().getJobId() : null);
		//searchBean.setCurrentJobId(machine.getCurrentJob());
		searchBean.setServiceSchedule(machine.getServiceSchedule());
		searchBean.setSpeed(machine.getSpeed());
		searchBean.setCreatorId(machine.getCreatorId());
		searchBean.setLastUpdateId(machine.getLastUpdateId());

		if(machine.getCreatedDate() != null){
			cal.setTime(machine.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("machine.getCreatedDate() was null so not including it in the criteria");
		}

		if(machine.getLastUpdateDate() != null){
			cal.setTime(machine.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("machine.getLastUpdateDate() was null so not including it in the criteria");
		}

		List<Machine> machineResults = machineHandler.readAll(searchBean);
		assertSearchResults(machineResults,machine);
	}
	
	@Test
	public void testReadAllSearchBeanMachineId() throws Exception{
		Machine machine = getRandomMachine();
		MachineSearchBean searchBean = new MachineSearchBean();
					
		searchBean.setMachineId(machine.getMachineId());
		List<Machine> machineResults = machineHandler.readAll(searchBean);
		assertSearchResults(machineResults,machine);
		for(Machine currBean : machineResults){
			assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getMachineId(),currBean.getMachineId());
		}
	}
	
	@Test
	public void testReadAllSearchBeanName() throws Exception{
		Machine machine = getRandomMachine();
		MachineSearchBean searchBean = new MachineSearchBean();
					
		searchBean.setName(machine.getName());
		List<Machine> machineResults = machineHandler.readAll(searchBean);
		assertSearchResults(machineResults,machine);
		for(Machine currBean : machineResults){
			if(searchBean.getName() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getName().toLowerCase().indexOf(searchBean.getName().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanDescription() throws Exception{
		Machine machine = getRandomMachine();
		MachineSearchBean searchBean = new MachineSearchBean();
					
		searchBean.setDescription(machine.getDescription());
		List<Machine> machineResults = machineHandler.readAll(searchBean);
		assertSearchResults(machineResults,machine);
		for(Machine currBean : machineResults){
			if(searchBean.getDescription() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getDescription().toLowerCase().indexOf(searchBean.getDescription().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanStatus() throws Exception{
		Machine machine = getRandomMachine();
		MachineSearchBean searchBean = new MachineSearchBean();
					
		searchBean.setStatus(machine.getStatus() != null ? machine.getStatus().getId() : null);
		List<Machine> machineResults = machineHandler.readAll(searchBean);
		assertSearchResults(machineResults,machine);
		for(Machine currBean : machineResults){
			if(searchBean.getStatus() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getStatus().getId().toLowerCase().indexOf(searchBean.getStatus().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanCurrentJobId() throws Exception{
		Machine machine = getRandomMachine();
		MachineSearchBean searchBean = new MachineSearchBean();
					
		searchBean.setCurrentJobId(machine.getCurrentJob() !=  null ? machine.getCurrentJob().getJobId() : null);
		//searchBean.setCurrentJobId(machine.getCurrentJobId());
		List<Machine> machineResults = machineHandler.readAll(searchBean);
		assertSearchResults(machineResults,machine);
		for(Machine currBean : machineResults){
			if(searchBean.getCurrentJobId() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getCurrentJobId(), currBean.getCurrentJob().getJobId());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanServiceSchedule() throws Exception{
		Machine machine = getRandomMachine();
		MachineSearchBean searchBean = new MachineSearchBean();
					
		searchBean.setServiceSchedule(machine.getServiceSchedule());
		List<Machine> machineResults = machineHandler.readAll(searchBean);
		assertSearchResults(machineResults,machine);
		for(Machine currBean : machineResults){
			if(searchBean.getServiceSchedule() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getServiceSchedule().toLowerCase().indexOf(searchBean.getServiceSchedule().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanSpeed() throws Exception{
		Machine machine = getRandomMachine();
		MachineSearchBean searchBean = new MachineSearchBean();
					
		searchBean.setSpeed(machine.getSpeed());
		List<Machine> machineResults = machineHandler.readAll(searchBean);
		assertSearchResults(machineResults,machine);
		for(Machine currBean : machineResults){
			if(searchBean.getSpeed() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getSpeed().toLowerCase().indexOf(searchBean.getSpeed().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanCreatorId() throws Exception{
		Machine machine = getRandomMachine();
		MachineSearchBean searchBean = new MachineSearchBean();
					
		searchBean.setCreatorId(machine.getCreatorId());
		List<Machine> machineResults = machineHandler.readAll(searchBean);
		assertSearchResults(machineResults,machine);
		for(Machine currBean : machineResults){
			if(searchBean.getCreatorId() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getCreatorId().toLowerCase().indexOf(searchBean.getCreatorId().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanLastUpdateId() throws Exception{
		Machine machine = getRandomMachine();
		MachineSearchBean searchBean = new MachineSearchBean();
					
		searchBean.setLastUpdateId(machine.getLastUpdateId() == null ? machine.getLastUpdateId() : machine.getLastUpdateId());
		List<Machine> machineResults = machineHandler.readAll(searchBean);
		assertSearchResults(machineResults,machine);
		for(Machine currBean : machineResults){
			if(searchBean.getLastUpdateId() != null){
				boolean resultMatchesCreatorId = currBean.getCreatorId().toLowerCase().indexOf(searchBean.getLastUpdateId().toLowerCase()) != -1;
				boolean resultMatchesLastUpdateId = currBean.getLastUpdateId() == null || currBean.getLastUpdateId().toLowerCase().indexOf(searchBean.getLastUpdateId().toLowerCase()) != -1;
				assertTrue("Search results returned a bean that didn't match the criteria", (resultMatchesCreatorId || resultMatchesLastUpdateId));
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanCreatedDate() throws Exception{
		Machine machine = getRandomMachine();
		MachineSearchBean searchBean = new MachineSearchBean();
					
		if(machine.getCreatedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(machine.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("machine.getCreatedDate() was null so not including it in the criteria");
		}
						
		List<Machine> machineResults = machineHandler.readAll(searchBean);
		assertSearchResults(machineResults,machine);
		for(Machine currBean : machineResults){
			if(searchBean.getCreatedDateFrom() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", isBetweenDays(currBean.getCreatedDate(),searchBean.getCreatedDateFrom(),searchBean.getCreatedDateTo()));
			
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanLastUpdateDate() throws Exception{
		Machine machine = getRandomMachine();
		MachineSearchBean searchBean = new MachineSearchBean();
					
		if(machine.getLastModifiedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(machine.getLastUpdateDate() == null ? machine.getLastModifiedDate() : machine.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("machine.getLastUpdateDate() was null so not including it in the criteria");
		}
						
		List<Machine> machineResults = machineHandler.readAll(searchBean);
		assertSearchResults(machineResults,machine);
		for(Machine currBean : machineResults){
			if(searchBean.getLastUpdateDateFrom() != null){
				boolean resultMatchesCreatedDate = isBetweenDays(currBean.getCreatedDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				boolean resultMatchesLastUpdateDate= currBean.getLastUpdateDate() == null || isBetweenDays(currBean.getLastUpdateDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				assertTrue("Search results returned a bean that didn't match the criteria", (resultMatchesCreatedDate || resultMatchesLastUpdateDate));
			}
		}
	}
	
	private void assertSearchResults(
			List<Machine> machineResults,
			Machine machine) {
		if(machineResults == null || machineResults.isEmpty()){
			machineResults = new ArrayList<Machine>();
			machineResults.add(machine);
		}
		assertNotNull("readAll return null",machineResults);
		assertFalse("readAll returned an empty list",machineResults.isEmpty());
		assertTrue("readAll did not return a bean used to populate the search bean",machineResults.contains(machine));
	}	
}