package com.epac.cap.handler;

import static org.junit.Assert.*;

import com.epac.cap.model.Job;
import com.epac.cap.model.Machine;
import com.epac.cap.model.Station;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.repository.MachineSearchBean;
import com.epac.cap.repository.StationSearchBean;
import com.epac.cap.test.BaseTest;


import static com.epac.cap.common.StringUtil.getRandomString;
import static com.epac.cap.common.NumberUtil.getRandomInteger;
import static com.epac.cap.common.DateUtil.isBetweenDays;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.Date;
import java.util.HashSet;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.stereotype.Service;

/**
 * Test class for the StationHandler interface.
 * 
 * @author walid
 *
 */
@Service
public class StationHandlerTest extends BaseTest{
	
	private static final Logger logger = Logger.getLogger(StationHandlerTest.class);
	@Resource
	private StationHandler stationHandler;
	@Resource
	private UserHandler userHandler = null;
	@Resource
	private JobHandler jobHandler = null;
	@Resource
	private LookupHandler lookupHandler = null;
	@Resource
	private MachineHandler machineHandler;
	
	private Station createNewStation() throws Exception{
		Station bean = new Station();
		List<String> inputTypes = new ArrayList<String>();
		inputTypes.add("Roll");
		inputTypes.add("Sheet");
		bean.setCreatedDate(new Date());
		bean.setCreatorId("junit");
		bean.setLastUpdateDate(new Date());
		bean.setLastUpdateId("junit");
		bean.setStationId(getRandomString(25));
		bean.setParentStationId(null);
		bean.setStationCategoryId(getRandomString(25));
		bean.setName(getRandomString(10));					
		bean.setDescription(getRandomString(2000));					
		bean.setScheduledHours((float) 1);			
		bean.setUnscheduledHours((float) 1);			
		bean.setProductionCapacity((float) 1);			
		bean.setProductionOrdering(getRandomInteger(500));			
		bean.setInputType(!inputTypes.isEmpty() ? inputTypes.get(0) : null);					
		
		return bean;
	}
	
	@Test
	public void testCreate() throws Exception{
		Station bean = createNewStation();
		stationHandler.create(bean);
	}
	
	private Station getRandomStation() throws Exception{
		Station bean = null;
		StationSearchBean searchBean = new StationSearchBean();
		searchBean.setMaxResults(100);
		List<Station> results = stationHandler.readAll(searchBean);
		if(results != null && !results.isEmpty()){
			bean = results.get(getRandomInteger(results.size()));
		}else{
			bean = createNewStation();
			stationHandler.create(bean);		
		}
		
		return bean;		
	}	

	@Test
	public void testReadAll() throws Exception{
		getRandomStation();//ensures there's at least 1 record in the db
		List<Station> stationResults = stationHandler.readAll();
		assertNotNull("readAll return null",stationResults);
		assertFalse("readAll returned an empty list",stationResults.isEmpty());		
	}
	
	@Test
	public void testUpdate() throws Exception{
		Station station = getRandomStation();
		station.setLastUpdateId("junit");
		station.setLastUpdateDate(new Date());
		stationHandler.update(station);
	}

	@Test
	public void testReadValidId() throws Exception{
		Station station = getRandomStation();
		Station readStation = stationHandler.read(station.getStationId());
		assertNotNull("read return null",readStation);
		assertEquals("read returned incorrect object by id",station, readStation);		
	}

	@Test
	public void testReadInvalidId() throws Exception{
		getRandomStation();
		Station readStation = stationHandler.read("-1");
		assertNull("read didnt return null as expected",readStation);		
	}
		
	@Test
	public void testDeleteExisting() throws Exception{		
		Station station = getRandomStation();
		MachineSearchBean msb = new MachineSearchBean();
		msb.setStationId(station != null ? station.getStationId() : null);
		List<Machine> machines = machineHandler.readAll(msb);
		
		Set<Job> result = new HashSet<Job>();
		JobSearchBean jsb = new JobSearchBean();
		jsb.setStationId(station.getStationId());
		result.addAll(jobHandler.readAll(jsb));
		
		if(machines.isEmpty() && result.isEmpty()){
			stationHandler.delete(station);
		}
		
	}
	
	@Test
	public void testDeleteNew() throws Exception{
		Station bean = createNewStation();
		stationHandler.create(bean);
		stationHandler.delete(bean);
	}
		
	@Test
	public void testReadAllBlankSearchBean() throws Exception{
		getRandomStation();//ensures there's at least 1 record in the db
		StationSearchBean searchBean = new StationSearchBean();
		List<Station> stationResults = stationHandler.readAll(searchBean);
		assertNotNull("readAll return null",stationResults);
		assertFalse("readAll returned an empty list",stationResults.isEmpty());
	}

	@Test
	public void testReadAllSearchBeanFields() throws Exception{
		getRandomStation();//ensures there's at least 1 record in the db
		StationSearchBean searchBean = new StationSearchBean();
		searchBean.setMaxResults(1);
		List<Station> stationResults = stationHandler.readAll(searchBean);
		assertNotNull("readAll return null",stationResults);
		assertFalse("readAll returned an empty list",stationResults.isEmpty());
		assertTrue("readAll did not honor max results",stationResults.size() == searchBean.getMaxResults());
	}

	@Test
	public void testReadAllCompleteSearchBean() throws Exception{				
		Station station = getRandomStation();
		StationSearchBean searchBean = new StationSearchBean();
		Calendar cal = Calendar.getInstance();
		searchBean.setStationId(station.getStationId());
		searchBean.setParentStationId(station.getParentStationId());
		searchBean.setName(station.getName());
		searchBean.setDescription(station.getDescription());
		searchBean.setScheduledHours(station.getScheduledHours());
		searchBean.setUnscheduledHours(station.getUnscheduledHours());
		searchBean.setProductionCapacity(station.getProductionCapacity());
		searchBean.setProductionOrdering(station.getProductionOrdering());
		searchBean.setInputType(station.getInputType() != null ? station.getInputType() : null);
		searchBean.setCreatorId(station.getCreatorId());
		searchBean.setLastUpdateId(station.getLastUpdateId());

		if(station.getCreatedDate() != null){
			cal.setTime(station.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("station.getCreatedDate() was null so not including it in the criteria");
		}

		if(station.getLastUpdateDate() != null){
			cal.setTime(station.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("station.getLastUpdateDate() was null so not including it in the criteria");
		}

		List<Station> stationResults = stationHandler.readAll(searchBean);
		assertSearchResults(stationResults,station);
	}
	
	@Test
	public void testReadAllSearchBeanStationId() throws Exception{
		Station station = getRandomStation();
		StationSearchBean searchBean = new StationSearchBean();
					
		searchBean.setStationId(station.getStationId());
		List<Station> stationResults = stationHandler.readAll(searchBean);
		assertSearchResults(stationResults,station);
		for(Station currBean : stationResults){
			assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getStationId(),currBean.getStationId());
		}
	}
	
	@Test
	public void testReadAllSearchBeanParentStationId() throws Exception{
		Station station = getRandomStation();
		StationSearchBean searchBean = new StationSearchBean();
					
		//searchBean.setParentStationId(station.getParentStation() != null ? station.getParentStation().getStationId() : null);
		searchBean.setParentStationId(station.getParentStationId());
		List<Station> stationResults = stationHandler.readAll(searchBean);
		assertSearchResults(stationResults,station);
		for(Station currBean : stationResults){
			if(searchBean.getParentStationId() != null && currBean.getParentStationId() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getParentStationId().toLowerCase().indexOf(searchBean.getParentStationId().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanName() throws Exception{
		Station station = getRandomStation();
		StationSearchBean searchBean = new StationSearchBean();
					
		searchBean.setName(station.getName());
		List<Station> stationResults = stationHandler.readAll(searchBean);
		assertSearchResults(stationResults,station);
		for(Station currBean : stationResults){
			if(searchBean.getName() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getName().toLowerCase().indexOf(searchBean.getName().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanDescription() throws Exception{
		Station station = getRandomStation();
		StationSearchBean searchBean = new StationSearchBean();
					
		searchBean.setDescription(station.getDescription());
		List<Station> stationResults = stationHandler.readAll(searchBean);
		assertSearchResults(stationResults,station);
		for(Station currBean : stationResults){
			if(searchBean.getDescription() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getDescription().toLowerCase().indexOf(searchBean.getDescription().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanScheduledHours() throws Exception{
		Station station = getRandomStation();
		StationSearchBean searchBean = new StationSearchBean();
					
		searchBean.setScheduledHours(station.getScheduledHours());
		List<Station> stationResults = stationHandler.readAll(searchBean);
		assertSearchResults(stationResults,station);
		for(Station currBean : stationResults){
			if(searchBean.getScheduledHours() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getScheduledHours(), currBean.getScheduledHours());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanUnscheduledHours() throws Exception{
		Station station = getRandomStation();
		StationSearchBean searchBean = new StationSearchBean();
					
		searchBean.setUnscheduledHours(station.getUnscheduledHours());
		List<Station> stationResults = stationHandler.readAll(searchBean);
		assertSearchResults(stationResults,station);
		for(Station currBean : stationResults){
			if(searchBean.getUnscheduledHours() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getUnscheduledHours(), currBean.getUnscheduledHours());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanProductionCapacity() throws Exception{
		Station station = getRandomStation();
		StationSearchBean searchBean = new StationSearchBean();
					
		searchBean.setProductionCapacity(station.getProductionCapacity());
		List<Station> stationResults = stationHandler.readAll(searchBean);
		assertSearchResults(stationResults,station);
		for(Station currBean : stationResults){
			if(searchBean.getProductionCapacity() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getProductionCapacity(), currBean.getProductionCapacity());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanProductionOrdering() throws Exception{
		Station station = getRandomStation();
		StationSearchBean searchBean = new StationSearchBean();
					
		searchBean.setProductionOrdering(station.getProductionOrdering());
		List<Station> stationResults = stationHandler.readAll(searchBean);
		assertSearchResults(stationResults,station);
		for(Station currBean : stationResults){
			if(searchBean.getProductionOrdering() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getProductionOrdering(), currBean.getProductionOrdering());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanInputType() throws Exception{
		Station station = getRandomStation();
		StationSearchBean searchBean = new StationSearchBean();
					
		searchBean.setInputType(station.getInputType() != null ? station.getInputType() : null);
		List<Station> stationResults = stationHandler.readAll(searchBean);
		assertSearchResults(stationResults,station);
		for(Station currBean : stationResults){
			if(searchBean.getInputType() != null && currBean.getInputType() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getInputType().toLowerCase().indexOf(searchBean.getInputType().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanCreatorId() throws Exception{
		Station station = getRandomStation();
		StationSearchBean searchBean = new StationSearchBean();
					
		searchBean.setCreatorId(station.getCreatorId());
		List<Station> stationResults = stationHandler.readAll(searchBean);
		assertSearchResults(stationResults,station);
		for(Station currBean : stationResults){
			if(searchBean.getCreatorId() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getCreatorId().toLowerCase().indexOf(searchBean.getCreatorId().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanLastUpdateId() throws Exception{
		Station station = getRandomStation();
		StationSearchBean searchBean = new StationSearchBean();
					
		searchBean.setLastUpdateId(station.getLastUpdateId() == null ? station.getLastUpdateId() : station.getLastUpdateId());
		List<Station> stationResults = stationHandler.readAll(searchBean);
		assertSearchResults(stationResults,station);
		for(Station currBean : stationResults){
			if(searchBean.getLastUpdateId() != null){
				boolean resultMatchesCreatorId = currBean.getCreatorId().toLowerCase().indexOf(searchBean.getLastUpdateId().toLowerCase()) != -1;
				boolean resultMatchesLastUpdateId = currBean.getLastUpdateId() == null || currBean.getLastUpdateId().toLowerCase().indexOf(searchBean.getLastUpdateId().toLowerCase()) != -1;
				assertTrue("Search results returned a bean that didn't match the criteria", (resultMatchesCreatorId || resultMatchesLastUpdateId));
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanCreatedDate() throws Exception{
		Station station = getRandomStation();
		StationSearchBean searchBean = new StationSearchBean();
					
		if(station.getCreatedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(station.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("station.getCreatedDate() was null so not including it in the criteria");
		}
						
		List<Station> stationResults = stationHandler.readAll(searchBean);
		assertSearchResults(stationResults,station);
		for(Station currBean : stationResults){
			if(searchBean.getCreatedDateFrom() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", isBetweenDays(currBean.getCreatedDate(),searchBean.getCreatedDateFrom(),searchBean.getCreatedDateTo()));
			
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanLastUpdateDate() throws Exception{
		Station station = getRandomStation();
		StationSearchBean searchBean = new StationSearchBean();
					
		if(station.getLastModifiedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(station.getLastUpdateDate() == null ? station.getLastModifiedDate() : station.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("station.getLastUpdateDate() was null so not including it in the criteria");
		}
						
		List<Station> stationResults = stationHandler.readAll(searchBean);
		assertSearchResults(stationResults,station);
		for(Station currBean : stationResults){
			if(searchBean.getLastUpdateDateFrom() != null){
				boolean resultMatchesCreatedDate = isBetweenDays(currBean.getCreatedDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				boolean resultMatchesLastUpdateDate= currBean.getLastUpdateDate() == null || isBetweenDays(currBean.getLastUpdateDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				assertTrue("Search results returned a bean that didn't match the criteria", (resultMatchesCreatedDate || resultMatchesLastUpdateDate));
			}
		}
	}
	
	private void assertSearchResults(
			List<Station> stationResults,
			Station station) {
		if(stationResults == null || stationResults.isEmpty()){
			stationResults = new ArrayList<Station>();
			stationResults.add(station);
		}
		assertNotNull("readAll return null",stationResults);
		assertFalse("readAll returned an empty list",stationResults.isEmpty());
		assertTrue("readAll did not return a bean used to populate the search bean",stationResults.contains(station));
	}	
}