package com.epac.cap.handler;

import static org.junit.Assert.*;

import com.epac.cap.model.Job;
import com.epac.cap.model.JobStatus;
import com.epac.cap.model.JobType;
import com.epac.cap.model.Priority;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.test.BaseTest;

import static com.epac.cap.common.StringUtil.getRandomString;
import static com.epac.cap.common.NumberUtil.getRandomInteger;
import static com.epac.cap.common.DateUtil.isBetweenDays;
import static com.epac.cap.common.DateUtil.getRandomFutureDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.stereotype.Service;

/**
 * Test class for the JobHandler interface.
 * 
 * @author walid
 *
 */
@Service
public class JobHandlerTest extends BaseTest{
	
	private static final Logger logger = Logger.getLogger(JobHandlerTest.class);
	@Resource
	private JobHandler jobHandler;
	@Resource
	private MachineHandler machineHandler = null;
	@Resource
	private OrderHandler orderHandler = null;
	@Resource
	private PartHandler partHandler = null;
	@Resource
	private RollHandler rollHandler = null;
	@Resource
	private StationHandler stationHandler = null;
	@Resource
	private UserHandler userHandler = null;
	@Resource
	private LookupHandler lookupHandler = null;

	private Job createNewJob() throws Exception{
		Job bean = new Job();
		List<JobStatus> jobStatuses = lookupHandler.readAll(JobStatus.class);
		List<JobType> jobTypes = lookupHandler.readAll(JobType.class);
		List<Priority> priorities = lookupHandler.readAll(Priority.class);
		bean.setCreatedDate(new Date());
		bean.setCreatorId("junit");
		bean.setLastUpdateDate(new Date());
		bean.setLastUpdateId("junit");
		bean.setJobStatus(!jobStatuses.isEmpty() ? jobStatuses.get(0) : null);					
		bean.setProductionOrdering(getRandomInteger(500));			
		bean.setRollOrdering(getRandomInteger(500));			
		bean.setMachineOrdering(getRandomInteger(500));			
		bean.setJobType(!jobTypes.isEmpty() ? jobTypes.get(0) : null);					
		bean.setFileSentFlag(true);			
		bean.setJobPriority(!priorities.isEmpty() ? priorities.get(0) : null);			
		
		return bean;
	}
	
	@Test
	public void testMatcher() throws Exception{
		Pattern pattern = Pattern.compile("T(\\d+)");
		Matcher matcher = pattern.matcher("T0");
		assertTrue("Reg. Ex. did not match ...", matcher.matches());
		matcher = pattern.matcher("T12");
		assertTrue("Reg. Ex. did not match ...", matcher.matches());
		matcher = pattern.matcher("T-12");
		assertFalse("Reg. Ex. did not match ...", matcher.matches());
		matcher = pattern.matcher("T");
		assertFalse("Reg. Ex. did not match ...", matcher.matches());
	}
	
	@Test
	public void testCreate() throws Exception{
		Job bean = createNewJob();
		jobHandler.create(bean);
		assertNotNull("id property 'jobId' was null after create",bean.getJobId());
		assertTrue("id property 'jobId' was invalid after create", bean.getJobId() > 0);
	}
	
	private Job getRandomJob() throws Exception{
		Job bean = null;
		JobSearchBean searchBean = new JobSearchBean();
		searchBean.setMaxResults(100);
		List<Job> results = jobHandler.readAll(searchBean);
		if(results != null && !results.isEmpty()){
			bean = results.get(getRandomInteger(results.size()));
		}else{
			bean = createNewJob();
			jobHandler.create(bean);		
		}
		
		return bean;		
	}	

	@Test
	public void testReadAll() throws Exception{
		getRandomJob();//ensures there's at least 1 record in the db
		List<Job> jobResults = jobHandler.readAll();
		assertNotNull("readAll return null",jobResults);
		assertFalse("readAll returned an empty list",jobResults.isEmpty());		
	}
	
	@Test
	public void testUpdate() throws Exception{
		Job job = getRandomJob();
		job.setLastUpdateId("junit");
		job.setLastUpdateDate(new Date());
		jobHandler.update(job);
	}

	@Test
	public void testReadValidId() throws Exception{
		Job job = getRandomJob();
		Job readJob = jobHandler.read(job.getJobId());
		assertNotNull("read return null",readJob);
		assertEquals("read returned incorrect object by id",job, readJob);		
	}

	@Test
	public void testReadInvalidId() throws Exception{
		getRandomJob();
		Job readJob = jobHandler.read(-1);
		assertNull("read didnt return null as expected",readJob);		
	}
		
	@Test
	public void testDeleteExisting() throws Exception{		
		Job job = getRandomJob();
		if(job.getLoadTags().isEmpty()){
			jobHandler.delete(job);
		}
	}
	
	@Test
	public void testDeleteNew() throws Exception{
		Job bean = createNewJob();
		jobHandler.create(bean);
		jobHandler.delete(bean);
	}
		
	@Test
	public void testReadAllBlankSearchBean() throws Exception{
		getRandomJob();//ensures there's at least 1 record in the db
		JobSearchBean searchBean = new JobSearchBean();
		List<Job> jobResults = jobHandler.readAll(searchBean);
		assertNotNull("readAll return null",jobResults);
		assertFalse("readAll returned an empty list",jobResults.isEmpty());
	}

	@Test
	public void testReadAllSearchBeanFields() throws Exception{
		getRandomJob();//ensures there's at least 1 record in the db
		JobSearchBean searchBean = new JobSearchBean();
		searchBean.setMaxResults(1);
		List<Job> jobResults = jobHandler.readAll(searchBean);
		assertNotNull("readAll return null",jobResults);
		assertFalse("readAll returned an empty list",jobResults.isEmpty());
		assertTrue("readAll did not honor max results",jobResults.size() == searchBean.getMaxResults());
	}

	@Test
	public void testReadAllCompleteSearchBean() throws Exception{				
		Job job = getRandomJob();
		JobSearchBean searchBean = new JobSearchBean();
		Calendar cal = Calendar.getInstance();
		searchBean.setJobId(job.getJobId());
		searchBean.setStatus(job.getJobStatus() != null ? job.getJobStatus().getId() : null);
		searchBean.setProductionOrdering(job.getProductionOrdering());
		searchBean.setRollOrdering(job.getRollOrdering());
		searchBean.setMachineOrdering(job.getMachineOrdering());
		searchBean.setJobType(job.getJobType() != null ? job.getJobType().getId() : null);
		searchBean.setFileSentFlag(job.getFileSentFlag());
		searchBean.setJobPriority(job.getJobPriority() != null ? job.getJobPriority().getId() : null);
		searchBean.setCreatorId(job.getCreatorId());
		searchBean.setLastUpdateId(job.getLastUpdateId());

		if(job.getCreatedDate() != null){
			cal.setTime(job.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("job.getCreatedDate() was null so not including it in the criteria");
		}

		if(job.getLastUpdateDate() != null){
			cal.setTime(job.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("job.getLastUpdateDate() was null so not including it in the criteria");
		}

		List<Job> jobResults = jobHandler.readAll(searchBean);
		assertSearchResults(jobResults,job);
	}
	
	@Test
	public void testReadAllSearchBeanJobId() throws Exception{
		Job job = getRandomJob();
		JobSearchBean searchBean = new JobSearchBean();
					
		searchBean.setJobId(job.getJobId());
		List<Job> jobResults = jobHandler.readAll(searchBean);
		assertSearchResults(jobResults,job);
		for(Job currBean : jobResults){
			if(searchBean.getJobId() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getJobId(), currBean.getJobId());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanStatus() throws Exception{
		Job job = getRandomJob();
		JobSearchBean searchBean = new JobSearchBean();
					
		searchBean.setStatus(job.getJobStatus() != null ? job.getJobStatus().getId() : null);
		List<Job> jobResults = jobHandler.readAll(searchBean);
		assertSearchResults(jobResults,job);
		for(Job currBean : jobResults){
			String currBeanId = currBean.getJobStatus() != null ? currBean.getJobStatus().getId() : "";
			if(searchBean.getStatus() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBeanId.toLowerCase().indexOf(searchBean.getStatus().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanProductionOrdering() throws Exception{
		Job job = getRandomJob();
		JobSearchBean searchBean = new JobSearchBean();
					
		searchBean.setProductionOrdering(job.getProductionOrdering());
		List<Job> jobResults = jobHandler.readAll(searchBean);
		assertSearchResults(jobResults,job);
		for(Job currBean : jobResults){
			if(searchBean.getProductionOrdering() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getProductionOrdering(), currBean.getProductionOrdering());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanRollOrdering() throws Exception{
		Job job = getRandomJob();
		JobSearchBean searchBean = new JobSearchBean();
					
		searchBean.setRollOrdering(job.getRollOrdering());
		List<Job> jobResults = jobHandler.readAll(searchBean);
		assertSearchResults(jobResults,job);
		for(Job currBean : jobResults){
			if(searchBean.getRollOrdering() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getRollOrdering(), currBean.getRollOrdering());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanMachineOrdering() throws Exception{
		Job job = getRandomJob();
		JobSearchBean searchBean = new JobSearchBean();
					
		searchBean.setMachineOrdering(job.getMachineOrdering());
		List<Job> jobResults = jobHandler.readAll(searchBean);
		assertSearchResults(jobResults,job);
		for(Job currBean : jobResults){
			if(searchBean.getMachineOrdering() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getMachineOrdering(), currBean.getMachineOrdering());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanJobType() throws Exception{
		Job job = getRandomJob();
		JobSearchBean searchBean = new JobSearchBean();
					
		searchBean.setJobType(job.getJobType().getId());
		List<Job> jobResults = jobHandler.readAll(searchBean);
		assertSearchResults(jobResults,job);
		for(Job currBean : jobResults){
			String currBeanId = currBean.getJobType() != null ? currBean.getJobType().getId() : "";
			if(searchBean.getJobType() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBeanId.toLowerCase().indexOf(searchBean.getJobType().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanFileSentFlag() throws Exception{
		Job job = getRandomJob();
		JobSearchBean searchBean = new JobSearchBean();
					
		searchBean.setFileSentFlag(job.getFileSentFlag());
		List<Job> jobResults = jobHandler.readAll(searchBean);
		assertSearchResults(jobResults,job);
		for(Job currBean : jobResults){
			if(searchBean.getFileSentFlag() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getFileSentFlag(), currBean.getFileSentFlag());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanJobPriority() throws Exception{
		Job job = getRandomJob();
		JobSearchBean searchBean = new JobSearchBean();
					
		searchBean.setJobPriority(job.getJobPriority().getId());
		List<Job> jobResults = jobHandler.readAll(searchBean);
		assertSearchResults(jobResults,job);
		for(Job currBean : jobResults){
			String currBeanId = currBean.getJobPriority() != null ? currBean.getJobPriority().getId() : "";
			if(searchBean.getJobPriority() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getJobPriority(), currBeanId);
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanCreatorId() throws Exception{
		Job job = getRandomJob();
		JobSearchBean searchBean = new JobSearchBean();
					
		searchBean.setCreatorId(job.getCreatorId());
		List<Job> jobResults = jobHandler.readAll(searchBean);
		assertSearchResults(jobResults,job);
		for(Job currBean : jobResults){
			if(searchBean.getCreatorId() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getCreatorId().toLowerCase().indexOf(searchBean.getCreatorId().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanLastUpdateId() throws Exception{
		Job job = getRandomJob();
		JobSearchBean searchBean = new JobSearchBean();
					
		searchBean.setLastUpdateId(job.getLastUpdateId() == null ? job.getLastUpdateId() : job.getLastUpdateId());
		List<Job> jobResults = jobHandler.readAll(searchBean);
		assertSearchResults(jobResults,job);
		for(Job currBean : jobResults){
			if(searchBean.getLastUpdateId() != null){
				boolean resultMatchesCreatorId = currBean.getCreatorId().toLowerCase().indexOf(searchBean.getLastUpdateId().toLowerCase()) != -1;
				boolean resultMatchesLastUpdateId = currBean.getLastUpdateId() == null || currBean.getLastUpdateId().toLowerCase().indexOf(searchBean.getLastUpdateId().toLowerCase()) != -1;
				assertTrue("Search results returned a bean that didn't match the criteria", (resultMatchesCreatorId || resultMatchesLastUpdateId));
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanCreatedDate() throws Exception{
		Job job = getRandomJob();
		JobSearchBean searchBean = new JobSearchBean();
					
		if(job.getCreatedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(job.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("job.getCreatedDate() was null so not including it in the criteria");
		}
						
		List<Job> jobResults = jobHandler.readAll(searchBean);
		assertSearchResults(jobResults,job);
		for(Job currBean : jobResults){
			if(searchBean.getCreatedDateFrom() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", isBetweenDays(currBean.getCreatedDate(),searchBean.getCreatedDateFrom(),searchBean.getCreatedDateTo()));
			
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanLastUpdateDate() throws Exception{
		Job job = getRandomJob();
		JobSearchBean searchBean = new JobSearchBean();
					
		if(job.getLastModifiedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(job.getLastUpdateDate() == null ? job.getLastModifiedDate() : job.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("job.getLastUpdateDate() was null so not including it in the criteria");
		}
						
		List<Job> jobResults = jobHandler.readAll(searchBean);
		assertSearchResults(jobResults,job);
		for(Job currBean : jobResults){
			if(searchBean.getLastUpdateDateFrom() != null){
				boolean resultMatchesCreatedDate = isBetweenDays(currBean.getCreatedDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				boolean resultMatchesLastUpdateDate= currBean.getLastUpdateDate() == null || isBetweenDays(currBean.getLastUpdateDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				assertTrue("Search results returned a bean that didn't match the criteria", (resultMatchesCreatedDate || resultMatchesLastUpdateDate));
			}
		}
	}
	
	private void assertSearchResults(
			List<Job> jobResults,
			Job job) {
		if(jobResults == null || jobResults.isEmpty()){
			jobResults = new ArrayList<Job>();
			jobResults.add(job);
		}
		assertNotNull("readAll return null",jobResults);
		assertFalse("readAll returned an empty list",jobResults.isEmpty());
		assertTrue("readAll did not return a bean used to populate the search bean",jobResults.contains(job));
	}	
}