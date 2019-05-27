package com.epac.cap.handler;

import static org.junit.Assert.*;

import com.epac.cap.model.PaperType;
import com.epac.cap.model.Roll;
import com.epac.cap.model.RollStatus;
import com.epac.cap.model.RollType;
import com.epac.cap.repository.LogSearchBean;
import com.epac.cap.repository.RollSearchBean;
import com.epac.cap.test.BaseTest;

import static com.epac.cap.common.StringUtil.getRandomString;
import static com.epac.cap.common.NumberUtil.getRandomInteger;
import static com.epac.cap.common.DateUtil.isBetweenDays;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.stereotype.Service;

/**
 * Test class for the RollHandler interface.
 * 
 * @author walid
 *
 */
@Service
public class RollHandlerTest extends BaseTest{
	
	private static final Logger logger = Logger.getLogger(RollHandlerTest.class);
	@Resource
	private RollHandler rollHandler;
	@Resource
	private LogHandler logHandler;
	@Resource
	private MachineHandler machineHandler = null;
	@Resource
	private UserHandler userHandler = null;
	@Resource
	private LookupHandler lookupHandler = null;

	private Roll createNewRoll() throws Exception{
		Roll bean = new Roll();
		List<RollType> rollTypes = lookupHandler.readAll(RollType.class);
		List<PaperType> paperTypes = lookupHandler.readAll(PaperType.class);
		List<RollStatus> rollStatuses = lookupHandler.readAll(RollStatus.class);
		
		bean.setCreatedDate(new Date());
		bean.setCreatorId("junit");
		bean.setLastUpdateDate(new Date());
		bean.setLastUpdateId("junit");
		bean.setRollNum(getRandomString(15));					
		bean.setRollTag(getRandomString(25));					
		bean.setParentRollId(null);			
		bean.setMachineOrdering(getRandomInteger(500));			
		bean.setRollType(!rollTypes.isEmpty() ? rollTypes.get(0) : null);					
		bean.setLength(getRandomInteger(500));			
		bean.setWeight(getRandomInteger(500));			
		bean.setPaperType(!paperTypes.isEmpty() ? paperTypes.get(0) : null);					
		bean.setStatus(!rollStatuses.isEmpty() ? rollStatuses.get(0) : null);					
		bean.setUtilization(getRandomInteger(500));			
		
		return bean;
	}
	
	@Test
	public void testCreate() throws Exception{
		Roll bean = createNewRoll();
		rollHandler.create(bean);
		assertNotNull("id property 'rollId' was null after create",bean.getRollId());
		assertTrue("id property 'rollId' was invalid after create", bean.getRollId() > 0);
	}
	
	private Roll getRandomRoll() throws Exception{
		Roll bean = null;
		RollSearchBean searchBean = new RollSearchBean();
		searchBean.setMaxResults(100);
		List<Roll> results = rollHandler.readAll(searchBean);
		if(results != null && !results.isEmpty()){
			bean = results.get(getRandomInteger(results.size()));
		}else{
			bean = createNewRoll();
			rollHandler.create(bean);		
		}
		
		return bean;		
	}	

	@Test
	public void testReadAll() throws Exception{
		getRandomRoll();//ensures there's at least 1 record in the db
		List<Roll> rollResults = rollHandler.readAll();
		assertNotNull("readAll return null",rollResults);
		assertFalse("readAll returned an empty list",rollResults.isEmpty());		
	}
	
	@Test
	public void testUpdate() throws Exception{
		Roll roll = getRandomRoll();
		roll.setLastUpdateId("junit");
		roll.setLastUpdateDate(new Date());
		rollHandler.update(roll);
	}

	@Test
	public void testReadValidId() throws Exception{
		Roll roll = getRandomRoll();
		Roll readRoll = rollHandler.read(roll.getRollId());
		assertNotNull("read return null",readRoll);
		assertEquals("read returned incorrect object by id",roll, readRoll);		
	}

	@Test
	public void testReadInvalidId() throws Exception{
		getRandomRoll();
		Roll readRoll = rollHandler.read(-1);
		assertNull("read didnt return null as expected",readRoll);		
	}
		
	@Test
	public void testDeleteExisting() throws Exception{		
		Roll roll = getRandomRoll();
		LogSearchBean lsb = new LogSearchBean();
		lsb.setRollId(roll.getRollId());
		if(logHandler.readAll(lsb).isEmpty() && roll.getJobs().isEmpty()){
			rollHandler.delete(roll);
		}
	}
	
	@Test
	public void testDeleteNew() throws Exception{
		Roll bean = createNewRoll();
		rollHandler.create(bean);
		rollHandler.delete(bean);
	}
		
	@Test
	public void testReadAllBlankSearchBean() throws Exception{
		getRandomRoll();//ensures there's at least 1 record in the db
		RollSearchBean searchBean = new RollSearchBean();
		List<Roll> rollResults = rollHandler.readAll(searchBean);
		assertNotNull("readAll return null",rollResults);
		assertFalse("readAll returned an empty list",rollResults.isEmpty());
	}

	@Test
	public void testReadAllSearchBeanFields() throws Exception{
		getRandomRoll();//ensures there's at least 1 record in the db
		RollSearchBean searchBean = new RollSearchBean();
		searchBean.setMaxResults(1);
		List<Roll> rollResults = rollHandler.readAll(searchBean);
		assertNotNull("readAll return null",rollResults);
		assertFalse("readAll returned an empty list",rollResults.isEmpty());
		assertTrue("readAll did not honor max results",rollResults.size() == searchBean.getMaxResults());
	}

	@Test
	public void testReadAllCompleteSearchBean() throws Exception{				
		Roll roll = getRandomRoll();
		RollSearchBean searchBean = new RollSearchBean();
		Calendar cal = Calendar.getInstance();
		searchBean.setRollId(roll.getRollId());
		searchBean.setRollNum(roll.getRollNum());
		searchBean.setRollTag(roll.getRollTag());
		searchBean.setParentRollId(roll.getParentRollId());
		searchBean.setMachineOrdering(roll.getMachineOrdering());
		searchBean.setRollType(roll.getRollType() != null ? roll.getRollType().getId(): null);
		searchBean.setLength(roll.getLength());
		searchBean.setWeight(roll.getWeight());
		searchBean.setPaperType(roll.getPaperType() != null ? roll.getPaperType().getId(): null);
		searchBean.setStatus(roll.getStatus() != null ? roll.getStatus().getId() : null);
		searchBean.setUtilization(roll.getUtilization());
		searchBean.setCreatorId(roll.getCreatorId());
		searchBean.setLastUpdateId(roll.getLastUpdateId());

		if(roll.getCreatedDate() != null){
			cal.setTime(roll.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("roll.getCreatedDate() was null so not including it in the criteria");
		}

		if(roll.getLastUpdateDate() != null){
			cal.setTime(roll.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("roll.getLastUpdateDate() was null so not including it in the criteria");
		}

		List<Roll> rollResults = rollHandler.readAll(searchBean);
		assertSearchResults(rollResults,roll);
	}
	
	@Test
	public void testReadAllSearchBeanRollId() throws Exception{
		Roll roll = getRandomRoll();
		RollSearchBean searchBean = new RollSearchBean();
					
		searchBean.setRollId(roll.getRollId());
		List<Roll> rollResults = rollHandler.readAll(searchBean);
		assertSearchResults(rollResults,roll);
		for(Roll currBean : rollResults){
			if(searchBean.getRollId() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getRollId(), currBean.getRollId());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanRollNum() throws Exception{
		Roll roll = getRandomRoll();
		RollSearchBean searchBean = new RollSearchBean();
					
		searchBean.setRollNum(roll.getRollNum());
		List<Roll> rollResults = rollHandler.readAll(searchBean);
		assertSearchResults(rollResults,roll);
		for(Roll currBean : rollResults){
			if(searchBean.getRollNum() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getRollNum().toLowerCase().indexOf(searchBean.getRollNum().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanRollTag() throws Exception{
		Roll roll = getRandomRoll();
		RollSearchBean searchBean = new RollSearchBean();
					
		searchBean.setRollTag(roll.getRollTag());
		List<Roll> rollResults = rollHandler.readAll(searchBean);
		assertSearchResults(rollResults,roll);
		for(Roll currBean : rollResults){
			if(searchBean.getRollTag() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getRollTag().toLowerCase().indexOf(searchBean.getRollTag().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanParentRollId() throws Exception{
		Roll roll = getRandomRoll();
		RollSearchBean searchBean = new RollSearchBean();
					
		searchBean.setParentRollId(roll.getParentRollId());
		List<Roll> rollResults = rollHandler.readAll(searchBean);
		assertSearchResults(rollResults,roll);
		for(Roll currBean : rollResults){
			if(searchBean.getParentRollId() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getParentRollId(), currBean.getParentRollId());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanMachineOrdering() throws Exception{
		Roll roll = getRandomRoll();
		RollSearchBean searchBean = new RollSearchBean();
					
		searchBean.setMachineOrdering(roll.getMachineOrdering());
		List<Roll> rollResults = rollHandler.readAll(searchBean);
		assertSearchResults(rollResults,roll);
		for(Roll currBean : rollResults){
			if(searchBean.getMachineOrdering() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getMachineOrdering(), currBean.getMachineOrdering());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanRollType() throws Exception{
		Roll roll = getRandomRoll();
		RollSearchBean searchBean = new RollSearchBean();
					
		searchBean.setRollType(roll.getRollType() != null ? roll.getRollType().getId() : null);
		List<Roll> rollResults = rollHandler.readAll(searchBean);
		assertSearchResults(rollResults,roll);
		for(Roll currBean : rollResults){
			if(searchBean.getRollType() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getRollType().getId().toLowerCase().indexOf(searchBean.getRollType().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanLength() throws Exception{
		Roll roll = getRandomRoll();
		RollSearchBean searchBean = new RollSearchBean();
					
		searchBean.setLength(roll.getLength());
		List<Roll> rollResults = rollHandler.readAll(searchBean);
		assertSearchResults(rollResults,roll);
		for(Roll currBean : rollResults){
			if(searchBean.getLength() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getLength(), currBean.getLength());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanWeight() throws Exception{
		Roll roll = getRandomRoll();
		RollSearchBean searchBean = new RollSearchBean();
					
		searchBean.setWeight(roll.getWeight());
		List<Roll> rollResults = rollHandler.readAll(searchBean);
		assertSearchResults(rollResults,roll);
		for(Roll currBean : rollResults){
			if(searchBean.getWeight() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getWeight(), currBean.getWeight());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanPaperType() throws Exception{
		Roll roll = getRandomRoll();
		RollSearchBean searchBean = new RollSearchBean();
					
		searchBean.setPaperType(roll.getPaperType() != null ? roll.getPaperType().getId() : null);
		List<Roll> rollResults = rollHandler.readAll(searchBean);
		assertSearchResults(rollResults,roll);
		for(Roll currBean : rollResults){
			if(searchBean.getPaperType() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getPaperType().getId().toLowerCase().indexOf(searchBean.getPaperType().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanStatus() throws Exception{
		Roll roll = getRandomRoll();
		RollSearchBean searchBean = new RollSearchBean();
					
		searchBean.setStatus(roll.getStatus() != null ? roll.getStatus().getId() : null);
		List<Roll> rollResults = rollHandler.readAll(searchBean);
		assertSearchResults(rollResults,roll);
		for(Roll currBean : rollResults){
			if(searchBean.getStatus() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getStatus().getId().toLowerCase().indexOf(searchBean.getStatus().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanUtilization() throws Exception{
		Roll roll = getRandomRoll();
		RollSearchBean searchBean = new RollSearchBean();
					
		searchBean.setUtilization(roll.getUtilization());
		List<Roll> rollResults = rollHandler.readAll(searchBean);
		assertSearchResults(rollResults,roll);
		for(Roll currBean : rollResults){
			if(searchBean.getUtilization() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getUtilization(), currBean.getUtilization());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanCreatorId() throws Exception{
		Roll roll = getRandomRoll();
		RollSearchBean searchBean = new RollSearchBean();
					
		searchBean.setCreatorId(roll.getCreatorId());
		List<Roll> rollResults = rollHandler.readAll(searchBean);
		assertSearchResults(rollResults,roll);
		for(Roll currBean : rollResults){
			if(searchBean.getCreatorId() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getCreatorId().toLowerCase().indexOf(searchBean.getCreatorId().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanLastUpdateId() throws Exception{
		Roll roll = getRandomRoll();
		RollSearchBean searchBean = new RollSearchBean();
					
		searchBean.setLastUpdateId(roll.getLastUpdateId() == null ? roll.getLastUpdateId() : roll.getLastUpdateId());
		List<Roll> rollResults = rollHandler.readAll(searchBean);
		assertSearchResults(rollResults,roll);
		for(Roll currBean : rollResults){
			if(searchBean.getLastUpdateId() != null){
				boolean resultMatchesCreatorId = currBean.getCreatorId().toLowerCase().indexOf(searchBean.getLastUpdateId().toLowerCase()) != -1;
				boolean resultMatchesLastUpdateId = currBean.getLastUpdateId() == null || currBean.getLastUpdateId().toLowerCase().indexOf(searchBean.getLastUpdateId().toLowerCase()) != -1;
				assertTrue("Search results returned a bean that didn't match the criteria", (resultMatchesCreatorId || resultMatchesLastUpdateId));
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanCreatedDate() throws Exception{
		Roll roll = getRandomRoll();
		RollSearchBean searchBean = new RollSearchBean();
					
		if(roll.getCreatedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(roll.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("roll.getCreatedDate() was null so not including it in the criteria");
		}
						
		List<Roll> rollResults = rollHandler.readAll(searchBean);
		assertSearchResults(rollResults,roll);
		for(Roll currBean : rollResults){
			if(searchBean.getCreatedDateFrom() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", isBetweenDays(currBean.getCreatedDate(),searchBean.getCreatedDateFrom(),searchBean.getCreatedDateTo()));
			
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanLastUpdateDate() throws Exception{
		Roll roll = getRandomRoll();
		RollSearchBean searchBean = new RollSearchBean();
					
		if(roll.getLastModifiedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(roll.getLastUpdateDate() == null ? roll.getLastModifiedDate() : roll.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("roll.getLastUpdateDate() was null so not including it in the criteria");
		}
						
		List<Roll> rollResults = rollHandler.readAll(searchBean);
		assertSearchResults(rollResults,roll);
		for(Roll currBean : rollResults){
			if(searchBean.getLastUpdateDateFrom() != null){
				boolean resultMatchesCreatedDate = isBetweenDays(currBean.getCreatedDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				boolean resultMatchesLastUpdateDate= currBean.getLastUpdateDate() == null || isBetweenDays(currBean.getLastUpdateDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				assertTrue("Search results returned a bean that didn't match the criteria", (resultMatchesCreatedDate || resultMatchesLastUpdateDate));
			}
		}
	}
	
	private void assertSearchResults(
			List<Roll> rollResults,
			Roll roll) {
		if(rollResults == null || rollResults.isEmpty()){
			rollResults = new ArrayList<Roll>();
			rollResults.add(roll);
		}
		assertNotNull("readAll return null",rollResults);
		assertFalse("readAll returned an empty list",rollResults.isEmpty());
		assertTrue("readAll did not return a bean used to populate the search bean",rollResults.contains(roll));
	}	
}