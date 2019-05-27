package com.epac.cap.handler;

import static org.junit.Assert.*;

import com.epac.cap.model.Job;
import com.epac.cap.model.JobStatus;
import com.epac.cap.model.Log;
import com.epac.cap.model.LogCause;
import com.epac.cap.model.LogResult;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.repository.LogSearchBean;
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
 * Test class for the LogHandler interface.
 * 
 * @author walid
 *
 */
@Service
public class LogHandlerTest extends BaseTest{
	private static final Logger logger = Logger.getLogger(LogHandlerTest.class);
	@Resource
	private LogHandler logHandler;
	@Resource
	private MachineHandler machineHandler = null;
	@Resource
	private RollHandler rollHandler = null;
	@Resource
	private UserHandler userHandler = null;
	@Resource
	private JobHandler jobHandler;
	@Resource
	private LookupHandler lookupHandler = null;
	
	private Log createNewLog() throws Exception{
		Log bean = new Log();
		List<LogResult> logResults = lookupHandler.readAll(LogResult.class);
		List<LogCause> logCauses = lookupHandler.readAll(LogCause.class);
		JobSearchBean jsb = new JobSearchBean();
		jsb.setMaxResults(1);
		List<Job> jobs = jobHandler.readAll(jsb);
		
		bean.setCreatedDate(new Date());
		bean.setCreatorId("junit");
		bean.setLastUpdateDate(new Date());
		bean.setLastUpdateId("junit");
		bean.setEvent(getRandomString(10));			
		bean.setLogResult(!logResults.isEmpty() ? logResults.get(0) : null);					
		bean.setLogCause(!logCauses.isEmpty() ? logCauses.get(0) : null);					
		bean.setCurrentJobId(!jobs.isEmpty() ? jobs.get(0).getJobId() : null);			
		bean.setRollLength(getRandomInteger(500));			
		bean.setStartTime(getRandomFutureDate(0));
		bean.setFinishTime(getRandomFutureDate(0));
		bean.setCounterFeet(getRandomInteger(50000));			
		
		return bean;
	}
	
	@Test
	public void testCreate() throws Exception{
		Log bean = createNewLog();
		logHandler.create(bean);
		assertNotNull("id property 'logId' was null after create",bean.getLogId());
		assertTrue("id property 'logId' was invalid after create", bean.getLogId() > 0);
	}
	
	private Log getRandomLog() throws Exception{
		Log bean = null;
		LogSearchBean searchBean = new LogSearchBean();
		searchBean.setMaxResults(100);
		List<Log> results = logHandler.readAll(searchBean);
		if(results != null && !results.isEmpty()){
			bean = results.get(getRandomInteger(results.size()));
		}else{
			bean = createNewLog();
			logHandler.create(bean);		
		}
		
		return bean;		
	}	

	@Test
	public void testReadAll() throws Exception{
		getRandomLog();//ensures there's at least 1 record in the db
		List<Log> logResults = logHandler.readAll();
		assertNotNull("readAll return null",logResults);
		assertFalse("readAll returned an empty list",logResults.isEmpty());		
	}
	
	@Test
	public void testUpdate() throws Exception{
		Log log = getRandomLog();
		log.setLastUpdateId("junit");
		log.setLastUpdateDate(new Date());
		logHandler.update(log);
	}

	@Test
	public void testReadValidId() throws Exception{
		Log log = getRandomLog();
		Log readLog = logHandler.read(log.getLogId());
		assertNotNull("read return null",readLog);
		assertEquals("read returned incorrect object by id",log, readLog);		
	}

	@Test
	public void testReadInvalidId() throws Exception{
		getRandomLog();
		Log readLog = logHandler.read(-1);
		assertNull("read didnt return null as expected",readLog);		
	}
		
	@Test
	public void testDeleteExisting() throws Exception{		
		Log log = getRandomLog();
		logHandler.delete(log);
	}
	
	@Test
	public void testDeleteNew() throws Exception{
		Log bean = createNewLog();
		logHandler.create(bean);
		logHandler.delete(bean);
	}
		
	@Test
	public void testReadAllBlankSearchBean() throws Exception{
		getRandomLog();//ensures there's at least 1 record in the db
		LogSearchBean searchBean = new LogSearchBean();
		List<Log> logResults = logHandler.readAll(searchBean);
		assertNotNull("readAll return null",logResults);
		assertFalse("readAll returned an empty list",logResults.isEmpty());
	}

	@Test
	public void testReadAllSearchBeanFields() throws Exception{
		getRandomLog();//ensures there's at least 1 record in the db
		LogSearchBean searchBean = new LogSearchBean();
		searchBean.setMaxResults(1);
		List<Log> logResults = logHandler.readAll(searchBean);
		assertNotNull("readAll return null",logResults);
		assertFalse("readAll returned an empty list",logResults.isEmpty());
		assertTrue("readAll did not honor max results",logResults.size() == searchBean.getMaxResults());
	}

	@Test
	public void testReadAllCompleteSearchBean() throws Exception{				
		Log log = getRandomLog();
		LogSearchBean searchBean = new LogSearchBean();
		Calendar cal = Calendar.getInstance();
		searchBean.setLogId(log.getLogId());
		searchBean.setEvent(log.getEvent());
		searchBean.setResult(log.getLogResult() != null ? log.getLogResult().getId() : null);
		searchBean.setCause(log.getLogCause() != null ? log.getLogCause().getId() : null);
		searchBean.setCurrentJobId(log.getCurrentJobId() );
		searchBean.setRollLength(log.getRollLength());

		if(log.getStartTime() != null){
			cal.setTime(log.getStartTime());
			searchBean.setStartTimeFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setStartTimeTo(cal.getTime());
		}else{
			logger.warn("log.getStartTime() was null so not including it in the criteria");
		}

		if(log.getFinishTime() != null){
			cal.setTime(log.getFinishTime());
			searchBean.setFinishTimeFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setFinishTimeTo(cal.getTime());
		}else{
			logger.warn("log.getFinishTime() was null so not including it in the criteria");
		}
		searchBean.setCounterFeet(log.getCounterFeet());
		searchBean.setCreatorId(log.getCreatorId());
		searchBean.setLastUpdateId(log.getLastUpdateId());

		if(log.getCreatedDate() != null){
			cal.setTime(log.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("log.getCreatedDate() was null so not including it in the criteria");
		}

		if(log.getLastUpdateDate() != null){
			cal.setTime(log.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("log.getLastUpdateDate() was null so not including it in the criteria");
		}

		List<Log> logResults = logHandler.readAll(searchBean);
		assertSearchResults(logResults,log);
	}
	
	@Test
	public void testReadAllSearchBeanLogId() throws Exception{
		Log log = getRandomLog();
		LogSearchBean searchBean = new LogSearchBean();
					
		searchBean.setLogId(log.getLogId());
		List<Log> logResults = logHandler.readAll(searchBean);
		assertSearchResults(logResults,log);
		for(Log currBean : logResults){
			if(searchBean.getLogId() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getLogId(), currBean.getLogId());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanEvent() throws Exception{
		Log log = getRandomLog();
		LogSearchBean searchBean = new LogSearchBean();
					
		searchBean.setEvent(log.getEvent());
		List<Log> logResults = logHandler.readAll(searchBean);
		assertSearchResults(logResults,log);
		for(Log currBean : logResults){
			if(searchBean.getEvent() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getEvent().toLowerCase().indexOf(searchBean.getEvent().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanResult() throws Exception{
		Log log = getRandomLog();
		LogSearchBean searchBean = new LogSearchBean();
					
		searchBean.setResult(log.getLogResult() != null ? log.getLogResult().getId() : null);
		List<Log> logResults = logHandler.readAll(searchBean);
		assertSearchResults(logResults,log);
		for(Log currBean : logResults){
			if(searchBean.getResult() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getLogResult().getId().toLowerCase().indexOf(searchBean.getResult().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanCause() throws Exception{
		Log log = getRandomLog();
		LogSearchBean searchBean = new LogSearchBean();
					
		searchBean.setCause(log.getLogCause() != null ? log.getLogCause().getId() : null);
		List<Log> logResults = logHandler.readAll(searchBean);
		assertSearchResults(logResults,log);
		for(Log currBean : logResults){
			if(searchBean.getCause() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getLogCause().getId().toLowerCase().indexOf(searchBean.getCause().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanCurrentJobId() throws Exception{
		Log log = getRandomLog();
		LogSearchBean searchBean = new LogSearchBean();
					
		searchBean.setCurrentJobId(log.getCurrentJobId() );
		List<Log> logResults = logHandler.readAll(searchBean);
		assertSearchResults(logResults,log);
		for(Log currBean : logResults){
			if(searchBean.getCurrentJobId() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getCurrentJobId(), currBean.getCurrentJobId());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanRollLength() throws Exception{
		Log log = getRandomLog();
		LogSearchBean searchBean = new LogSearchBean();
					
		searchBean.setRollLength(log.getRollLength());
		List<Log> logResults = logHandler.readAll(searchBean);
		assertSearchResults(logResults,log);
		for(Log currBean : logResults){
			if(searchBean.getRollLength() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getRollLength(), currBean.getRollLength());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanStartTime() throws Exception{
		Log log = getRandomLog();
		LogSearchBean searchBean = new LogSearchBean();
					
		if(log.getStartTime() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(log.getStartTime());
			searchBean.setStartTimeFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setStartTimeTo(cal.getTime());
		}else{
			logger.warn("log.getStartTime() was null so not including it in the criteria");
		}
						
		List<Log> logResults = logHandler.readAll(searchBean);
		assertSearchResults(logResults,log);
		for(Log currBean : logResults){
			if(searchBean.getStartTimeFrom() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", isBetweenDays(currBean.getStartTime(),searchBean.getStartTimeFrom(),searchBean.getStartTimeTo()));
			
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanFinishTime() throws Exception{
		Log log = getRandomLog();
		LogSearchBean searchBean = new LogSearchBean();
					
		if(log.getFinishTime() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(log.getFinishTime());
			searchBean.setFinishTimeFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setFinishTimeTo(cal.getTime());
		}else{
			logger.warn("log.getFinishTime() was null so not including it in the criteria");
		}
						
		List<Log> logResults = logHandler.readAll(searchBean);
		assertSearchResults(logResults,log);
		for(Log currBean : logResults){
			if(searchBean.getFinishTimeFrom() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", isBetweenDays(currBean.getFinishTime(),searchBean.getFinishTimeFrom(),searchBean.getFinishTimeTo()));
			
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanCounterFeet() throws Exception{
		Log log = getRandomLog();
		LogSearchBean searchBean = new LogSearchBean();
					
		searchBean.setCounterFeet(log.getCounterFeet());
		List<Log> logResults = logHandler.readAll(searchBean);
		assertSearchResults(logResults,log);
		for(Log currBean : logResults){
			if(searchBean.getCounterFeet() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", searchBean.getCounterFeet() == currBean.getCounterFeet());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanCreatorId() throws Exception{
		Log log = getRandomLog();
		LogSearchBean searchBean = new LogSearchBean();
					
		searchBean.setCreatorId(log.getCreatorId());
		List<Log> logResults = logHandler.readAll(searchBean);
		assertSearchResults(logResults,log);
		for(Log currBean : logResults){
			if(searchBean.getCreatorId() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getCreatorId().toLowerCase().indexOf(searchBean.getCreatorId().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanLastUpdateId() throws Exception{
		Log log = getRandomLog();
		LogSearchBean searchBean = new LogSearchBean();
					
		searchBean.setLastUpdateId(log.getLastUpdateId() == null ? log.getLastUpdateId() : log.getLastUpdateId());
		List<Log> logResults = logHandler.readAll(searchBean);
		assertSearchResults(logResults,log);
		for(Log currBean : logResults){
			if(searchBean.getLastUpdateId() != null){
				boolean resultMatchesCreatorId = currBean.getCreatorId().toLowerCase().indexOf(searchBean.getLastUpdateId().toLowerCase()) != -1;
				boolean resultMatchesLastUpdateId = currBean.getLastUpdateId() == null || currBean.getLastUpdateId().toLowerCase().indexOf(searchBean.getLastUpdateId().toLowerCase()) != -1;
				assertTrue("Search results returned a bean that didn't match the criteria", (resultMatchesCreatorId || resultMatchesLastUpdateId));
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanCreatedDate() throws Exception{
		Log log = getRandomLog();
		LogSearchBean searchBean = new LogSearchBean();
					
		if(log.getCreatedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(log.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("log.getCreatedDate() was null so not including it in the criteria");
		}
						
		List<Log> logResults = logHandler.readAll(searchBean);
		assertSearchResults(logResults,log);
		for(Log currBean : logResults){
			if(searchBean.getCreatedDateFrom() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", isBetweenDays(currBean.getCreatedDate(),searchBean.getCreatedDateFrom(),searchBean.getCreatedDateTo()));
			
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanLastUpdateDate() throws Exception{
		Log log = getRandomLog();
		LogSearchBean searchBean = new LogSearchBean();
					
		if(log.getLastModifiedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(log.getLastUpdateDate() == null ? log.getLastModifiedDate() : log.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("log.getLastUpdateDate() was null so not including it in the criteria");
		}
						
		List<Log> logResults = logHandler.readAll(searchBean);
		assertSearchResults(logResults,log);
		for(Log currBean : logResults){
			if(searchBean.getLastUpdateDateFrom() != null){
				boolean resultMatchesCreatedDate = isBetweenDays(currBean.getCreatedDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				boolean resultMatchesLastUpdateDate= currBean.getLastUpdateDate() == null || isBetweenDays(currBean.getLastUpdateDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				assertTrue("Search results returned a bean that didn't match the criteria", (resultMatchesCreatedDate || resultMatchesLastUpdateDate));
			}
		}
	}
	
	private void assertSearchResults(
			List<Log> logResults,
			Log log) {
		if(logResults == null || logResults.isEmpty()){
			logResults = new ArrayList<Log>();
			logResults.add(log);
		}
		assertNotNull("readAll return null",logResults);
		assertFalse("readAll returned an empty list",logResults.isEmpty());
		assertTrue("readAll did not return a bean used to populate the search bean",logResults.contains(log));
	}	
}