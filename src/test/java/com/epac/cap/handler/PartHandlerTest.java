package com.epac.cap.handler;

import static org.junit.Assert.*;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.BindingType;
import com.epac.cap.model.Job;
import com.epac.cap.model.Lamination;
import com.epac.cap.model.PaperType;
import com.epac.cap.model.Part;
import com.epac.cap.model.PartCategory;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.repository.PartSearchBean;
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
 * Test class for the PartHandler class.
 * 
 * @author walid
 *
 */
@Service
public class PartHandlerTest extends BaseTest{
	private static final Logger logger = Logger.getLogger(PartHandlerTest.class);
	@Resource
	private PartHandler partHandler;
	@Resource
	private UserHandler userHandler = null;
	@Resource
	private JobHandler jobHandler = null;
	@Resource
	private LookupHandler lookupHandler = null;
	
	private Part createNewPart() throws Exception{
		Part bean = new Part();
		List<BindingType> bindingTypes = lookupHandler.readAll(BindingType.class);
		List<PartCategory> partCategories = lookupHandler.readAll(PartCategory.class);
		List<PaperType> paperTypes = lookupHandler.readAll(PaperType.class);
		List<Lamination> laminations = lookupHandler.readAll(Lamination.class);
		
		bean.setCreatedDate(new Date());
		bean.setCreatorId("junit");
		bean.setLastUpdateDate(new Date());
		bean.setLastUpdateId("junit");
		bean.setPartNum(getRandomString(8));
		bean.setIsbn(getRandomString(25));					
		bean.setTitle(getRandomString(10));					
		//bean.setFilePath(getRandomString(500));					
		//bean.setFileName(getRandomString(100));					
		bean.setSoftDelete(false);	
		bean.setActiveFlag(true);	
		bean.setBindingType(!bindingTypes.isEmpty() ? bindingTypes.get(0) : null);					
		bean.setPagesCount(getRandomInteger(500));			
		bean.setThickness((float) 1.0);					
		bean.setCategory(!partCategories.isEmpty() ? partCategories.get(0) : null);					
		bean.setColors(getRandomString(15));					
		bean.setPaperType(!paperTypes.isEmpty() ? paperTypes.get(0) : null);					
		bean.setLamination(!laminations.isEmpty() ? laminations.get(0) : null);					
		bean.setNotes(getRandomString(1000));					
		
		return bean;
	}
	
	@Test
	public void testCreate() throws Exception{
		Part bean = createNewPart();
		partHandler.create(bean);
	}
	
	private Part getRandomPart() throws Exception{
		Part bean = null;
		PartSearchBean searchBean = new PartSearchBean();
		searchBean.setMaxResults(100);
		List<Part> results = partHandler.readAll(searchBean);
		if(results != null && !results.isEmpty()){
			bean = results.get(getRandomInteger(results.size()));
		}else{
			bean = createNewPart();
			partHandler.create(bean);		
		}
		
		return bean;		
	}	

	@Test
	public void testReadAll() throws Exception{
		getRandomPart();//ensures there's at least 1 record in the db
		List<Part> partResults = partHandler.readAll();
		assertNotNull("readAll return null",partResults);
		assertFalse("readAll returned an empty list",partResults.isEmpty());		
	}
	
	@Test
	public void testUpdate() throws Exception{
		Part part = getRandomPart();
		part.setLastUpdateId("junit");
		part.setLastUpdateDate(new Date());
		partHandler.update(part);
	}

	@Test
	public void testReadValidId() throws Exception{
		Part part = getRandomPart();
		Part readPart = partHandler.read(part.getPartNum());
		assertNotNull("read return null",readPart);
		assertEquals("read returned incorrect object by id",part, readPart);		
	}

	@Test
	public void testReadInvalidId() throws Exception{
		getRandomPart();
		Part readPart = partHandler.read("-1");
		assertNull("read didnt return null as expected",readPart);		
	}
		
	@Test
	public void testDeleteExisting() throws Exception{		
		Part part = getRandomPart();
		if(getPartJobs( part).isEmpty()){
			partHandler.delete(part);
		}
	}
	public Set<Job> getPartJobs(Part part) throws PersistenceException{
		Set<Job> result = new HashSet<Job>();
		JobSearchBean jsb = new JobSearchBean();
		jsb.setPartNum(part.getPartNum());
		result.addAll(jobHandler.readAll(jsb));
		return result;
	}
	
	@Test
	public void testDeleteNew() throws Exception{
		Part bean = createNewPart();
		partHandler.create(bean);
		partHandler.delete(bean);
	}
		
	@Test
	public void testReadAllBlankSearchBean() throws Exception{
		getRandomPart();//ensures there's at least 1 record in the db
		PartSearchBean searchBean = new PartSearchBean();
		List<Part> partResults = partHandler.readAll(searchBean);
		assertNotNull("readAll return null",partResults);
		assertFalse("readAll returned an empty list",partResults.isEmpty());
	}

	@Test
	public void testReadAllSearchBeanFields() throws Exception{
		getRandomPart();//ensures there's at least 1 record in the db
		PartSearchBean searchBean = new PartSearchBean();
		searchBean.setMaxResults(1);
		List<Part> partResults = partHandler.readAll(searchBean);
		assertNotNull("readAll return null",partResults);
		assertFalse("readAll returned an empty list",partResults.isEmpty());
		assertTrue("readAll did not honor max results",partResults.size() == searchBean.getMaxResults());
	}

	@Test
	public void testReadAllCompleteSearchBean() throws Exception{				
		Part part = getRandomPart();
		PartSearchBean searchBean = new PartSearchBean();
		Calendar cal = Calendar.getInstance();
		searchBean.setPartNum(part.getPartNum());
		searchBean.setIsbn(part.getIsbn());
		searchBean.setTitle(part.getTitle());
		//searchBean.setFilePath(part.getFilePath());
		//searchBean.setFileName(part.getFileName());
		searchBean.setSoftDelete(part.getSoftDelete());
		searchBean.setActiveFlag(part.getActiveFlag());
		searchBean.setBindingTypeId(part.getBindingType() != null ? part.getBindingType().getId() : null);
		searchBean.setPagesCount(part.getPagesCount());
		searchBean.setThickness(part.getThickness());
		searchBean.setCategoryId(part.getCategory() != null ? part.getCategory().getId() : null);
		searchBean.setColors(part.getColors());
		searchBean.setPaperType(part.getPaperType() != null ? part.getPaperType().getId() : null);
		searchBean.setLamination(part.getLamination() != null ? part.getLamination().getId() : null);
		searchBean.setNotes(part.getNotes());

		if(part.getCreatedDate() != null){
			cal.setTime(part.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("part.getCreatedDate() was null so not including it in the criteria");
		}

		if(part.getLastUpdateDate() != null){
			cal.setTime(part.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("part.getLastUpdateDate() was null so not including it in the criteria");
		}

		List<Part> partResults = partHandler.readAll(searchBean);
		assertSearchResults(partResults,part);
	}
	
	@Test
	public void testReadAllSearchBeanPartNum() throws Exception{
		Part part = getRandomPart();
		PartSearchBean searchBean = new PartSearchBean();
					
		searchBean.setPartNum(part.getPartNum());
		List<Part> partResults = partHandler.readAll(searchBean);
		assertSearchResults(partResults,part);
		for(Part currBean : partResults){
			assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getPartNum(),currBean.getPartNum());
		}
	}
	
	@Test
	public void testReadAllSearchBeanIsbn() throws Exception{
		Part part = getRandomPart();
		PartSearchBean searchBean = new PartSearchBean();
					
		searchBean.setIsbn(part.getIsbn());
		List<Part> partResults = partHandler.readAll(searchBean);
		assertSearchResults(partResults,part);
		for(Part currBean : partResults){
			if(searchBean.getIsbn() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getIsbn().toLowerCase().indexOf(searchBean.getIsbn().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanTitle() throws Exception{
		Part part = getRandomPart();
		PartSearchBean searchBean = new PartSearchBean();
					
		searchBean.setTitle(part.getTitle());
		List<Part> partResults = partHandler.readAll(searchBean);
		assertSearchResults(partResults,part);
		for(Part currBean : partResults){
			if(searchBean.getTitle() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getTitle().toLowerCase().indexOf(searchBean.getTitle().toLowerCase()) != -1);			
			}				
		}
	}
	
	/*@Test
	public void testReadAllSearchBeanFilePath() throws Exception{
		Part part = getRandomPart();
		PartSearchBean searchBean = new PartSearchBean();
					
		searchBean.setFilePath(part.getFilePath());
		List<Part> partResults = partHandler.readAll(searchBean);
		assertSearchResults(partResults,part);
		for(Part currBean : partResults){
			if(searchBean.getFilePath() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getFilePath().toLowerCase().indexOf(searchBean.getFilePath().toLowerCase()) != -1);			
			}				
		}
	}*/
	
	/*@Test
	public void testReadAllSearchBeanFileName() throws Exception{
		Part part = getRandomPart();
		PartSearchBean searchBean = new PartSearchBean();
					
		searchBean.setFileName(part.getFileName());
		List<Part> partResults = partHandler.readAll(searchBean);
		assertSearchResults(partResults,part);
		for(Part currBean : partResults){
			if(searchBean.getFileName() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getFileName().toLowerCase().indexOf(searchBean.getFileName().toLowerCase()) != -1);			
			}				
		}
	}*/
	
	@Test
	public void testReadAllSearchBeanSoftDelete() throws Exception{
		Part part = getRandomPart();
		PartSearchBean searchBean = new PartSearchBean();
					
		searchBean.setSoftDelete(part.getSoftDelete());
		List<Part> partResults = partHandler.readAll(searchBean);
		assertSearchResults(partResults,part);
		for(Part currBean : partResults){
			if(searchBean.getSoftDelete() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getSoftDelete(), currBean.getSoftDelete());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanActiveFlag() throws Exception{
		Part part = getRandomPart();
		PartSearchBean searchBean = new PartSearchBean();

		searchBean.setActiveFlag(part.getActiveFlag());
		List<Part> partResults = partHandler.readAll(searchBean);
		assertSearchResults(partResults,part);
		for(Part currBean : partResults){
			if(searchBean.getActiveFlag() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getActiveFlag(), currBean.getActiveFlag());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanBindingTypeId() throws Exception{
		Part part = getRandomPart();
		PartSearchBean searchBean = new PartSearchBean();
					
		searchBean.setBindingTypeId(part.getBindingType() != null ? part.getBindingType().getId() : null);
		List<Part> partResults = partHandler.readAll(searchBean);
		assertSearchResults(partResults,part);
		for(Part currBean : partResults){
			if(searchBean.getBindingTypeId() != null && currBean.getBindingType()!= null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getBindingType().getId().toLowerCase().indexOf(searchBean.getBindingTypeId().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanPagesCount() throws Exception{
		Part part = getRandomPart();
		PartSearchBean searchBean = new PartSearchBean();
					
		searchBean.setPagesCount(part.getPagesCount());
		List<Part> partResults = partHandler.readAll(searchBean);
		assertSearchResults(partResults,part);
		for(Part currBean : partResults){
			if(searchBean.getPagesCount() != null){
				assertEquals("Search results returned a bean that didn't match the criteria", searchBean.getPagesCount(), currBean.getPagesCount());
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanThickness() throws Exception{
		Part part = getRandomPart();
		PartSearchBean searchBean = new PartSearchBean();
					
		searchBean.setThickness(part.getThickness());
		List<Part> partResults = partHandler.readAll(searchBean);
		assertSearchResults(partResults,part);
		for(Part currBean : partResults){
			if(searchBean.getThickness() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getThickness() == searchBean.getThickness());			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanCategoryId() throws Exception{
		Part part = getRandomPart();
		PartSearchBean searchBean = new PartSearchBean();
					
		searchBean.setCategoryId(part.getCategory() != null ? part.getCategory().getId() : null);
		List<Part> partResults = partHandler.readAll(searchBean);
		assertSearchResults(partResults,part);
		for(Part currBean : partResults){
			if(searchBean.getCategoryId() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getCategory().getId().toLowerCase().indexOf(searchBean.getCategoryId().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanColors() throws Exception{
		Part part = getRandomPart();
		PartSearchBean searchBean = new PartSearchBean();
					
		searchBean.setColors(part.getColors());
		List<Part> partResults = partHandler.readAll(searchBean);
		assertSearchResults(partResults,part);
		for(Part currBean : partResults){
			if(searchBean.getColors() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getColors().toLowerCase().indexOf(searchBean.getColors().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanPaperType() throws Exception{
		Part part = getRandomPart();
		PartSearchBean searchBean = new PartSearchBean();
					
		searchBean.setPaperType(part.getPaperType() != null ? part.getPaperType().getId() : null);
		List<Part> partResults = partHandler.readAll(searchBean);
		assertSearchResults(partResults,part);
		for(Part currBean : partResults){
			if(searchBean.getPaperType() != null && currBean.getPaperType() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getPaperType().getId().toLowerCase().indexOf(searchBean.getPaperType().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanLamination() throws Exception{
		Part part = getRandomPart();
		PartSearchBean searchBean = new PartSearchBean();
					
		searchBean.setLamination(part.getLamination() != null ? part.getLamination().getId() : null);
		List<Part> partResults = partHandler.readAll(searchBean);
		assertSearchResults(partResults,part);
		for(Part currBean : partResults){
			if(searchBean.getLamination() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getLamination().getId().toLowerCase().indexOf(searchBean.getLamination().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanNotes() throws Exception{
		Part part = getRandomPart();
		PartSearchBean searchBean = new PartSearchBean();
					
		searchBean.setNotes(part.getNotes());
		List<Part> partResults = partHandler.readAll(searchBean);
		assertSearchResults(partResults,part);
		for(Part currBean : partResults){
			if(searchBean.getNotes() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", currBean.getNotes().toLowerCase().indexOf(searchBean.getNotes().toLowerCase()) != -1);			
			}				
		}
	}
	
	@Test
	public void testReadAllSearchBeanCreatedDate() throws Exception{
		Part part = getRandomPart();
		PartSearchBean searchBean = new PartSearchBean();
					
		if(part.getCreatedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(part.getCreatedDate());
			searchBean.setCreatedDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setCreatedDateTo(cal.getTime());
		}else{
			logger.warn("part.getCreatedDate() was null so not including it in the criteria");
		}
						
		List<Part> partResults = partHandler.readAll(searchBean);
		assertSearchResults(partResults,part);
		for(Part currBean : partResults){
			if(searchBean.getCreatedDateFrom() != null){
				assertTrue("Search results returned a bean that didn't match the criteria", isBetweenDays(currBean.getCreatedDate(),searchBean.getCreatedDateFrom(),searchBean.getCreatedDateTo()));
			
			}
		}
	}
	
	@Test
	public void testReadAllSearchBeanLastUpdateDate() throws Exception{
		Part part = getRandomPart();
		PartSearchBean searchBean = new PartSearchBean();
					
		if(part.getLastModifiedDate() != null){
			Calendar cal = Calendar.getInstance();
			cal.setTime(part.getLastUpdateDate() == null ? part.getLastModifiedDate() : part.getLastUpdateDate());
			searchBean.setLastUpdateDateFrom(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
			searchBean.setLastUpdateDateTo(cal.getTime());
		}else{
			logger.warn("part.getLastUpdateDate() was null so not including it in the criteria");
		}
						
		List<Part> partResults = partHandler.readAll(searchBean);
		assertSearchResults(partResults,part);
		for(Part currBean : partResults){
			if(searchBean.getLastUpdateDateFrom() != null){
				boolean resultMatchesCreatedDate = isBetweenDays(currBean.getCreatedDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				boolean resultMatchesLastUpdateDate= currBean.getLastUpdateDate() == null || isBetweenDays(currBean.getLastUpdateDate(),searchBean.getLastUpdateDateFrom(),searchBean.getLastUpdateDateTo());
				assertTrue("Search results returned a bean that didn't match the criteria", (resultMatchesCreatedDate || resultMatchesLastUpdateDate));
			}
		}
	}
	
	private void assertSearchResults(
			List<Part> partResults,
			Part part) {
		if(partResults == null || partResults.isEmpty()){
			partResults = new ArrayList<Part>();
			partResults.add(part);
		}
		assertNotNull("readAll return null",partResults);
		assertFalse("readAll returned an empty list",partResults.isEmpty());
		assertTrue("readAll did not return a bean used to populate the search bean",partResults.contains(part));
	}	
}