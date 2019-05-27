package com.epac.cap.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.model.BatchStatus;
import com.epac.cap.model.CoverBatch;
import com.epac.cap.model.CoverBatchJob;
import com.epac.cap.model.CoverSection;
import com.epac.cap.model.Job;
import com.epac.cap.model.JobStatus;
import com.epac.cap.model.JobType;
import com.epac.cap.model.Lamination;
import com.epac.cap.model.Part;
import com.epac.cap.model.Preference;
import com.epac.cap.model.Roll;
import com.epac.cap.model.RollType;
import com.epac.cap.model.SectionStatus;
import com.epac.cap.model.Station;
import com.epac.cap.model.StationCategory;
import com.epac.cap.repository.CoverBatchDAO;
import com.epac.cap.repository.CoverBatchJobDAO;
import com.epac.cap.repository.CoverBatchSearchBean;
import com.epac.cap.repository.CoverSectionDAO;
import com.epac.cap.repository.JobDAO;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.repository.PartDAO;
import com.epac.cap.repository.RollDAO;
import com.epac.cap.repository.RollSearchBean;

/**
 * Interacts with Cover Batch data.  Uses CoverBatchDAO for entity persistence.
 * @author slimj
 *
 */
@Service
public class CoverBatchHandler {
	
	@Autowired
	private CoverBatchDAO coverBatchDAO;
	
	@Autowired
	private CoverSectionDAO coverSectionDAO;
	
	@Autowired
	private RollDAO rollDAO;
	
	@Autowired
	private JobDAO jobDAO;
	
	@Autowired
	private JobHandler jobHandler;
	
	@Autowired
	PartDAO partDAO;
	
	@Autowired
	private LookupDAO lookupDAO;
	
	@Autowired
	private CoverBatchJobDAO coverBatchJobDAO;
	
	private static Logger logger = Logger.getLogger(CoverBatchHandler.class);

	/***
	 * No arg constructor.
	 */
	public CoverBatchHandler(){  }
	
	/** 
	 * Calls the corresponding readAll method on the CoverBatchDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<CoverBatch> readAll(CoverBatchSearchBean searchBean) throws PersistenceException{
		try{
			return getCoverBatchDAO().readAll(searchBean);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a list of Cover Batches : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	/** 
	 * create a batch associated to a roll;
	 */
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void createBatch(Integer rollId, String executingUserId, Integer recreateBId) throws PersistenceException {
		try {
			Roll theRoll = null;
			RollSearchBean rsb = new RollSearchBean();
			rsb.setRollId(rollId);
			List<Roll> rolls = rollDAO.readAll(rsb);
			if(!rolls.isEmpty() && rollId != null){
				theRoll = rolls.get(0);
			}else
			{
				return;
			}	
			List<Job> coverJobs = new ArrayList<>();
			Job cCoverJob = null;
			if (theRoll.getAlljobs() != null)
			{	
			//create corresponding cover list for production
			for (Job job: theRoll.getAlljobs())
			{
				if(!StationCategory.Categories.PRESS.getName().equals(job.getStationId())){
					continue;
				}
				cCoverJob = jobHandler.getJobByStation(job, StationCategory.Categories.COVERPRESS.toString());
				if (cCoverJob == null)
				{
					//create corresponding cover job
					Job newJob = new Job();
					newJob.setOrderId(job.getOrderId());
					//Part coverPart = partDAO.read(job.getPartNum().substring(0, job.getPartNum().length() - 1).concat("C"));
					newJob.setPartNum(job.getPartNum().substring(0, job.getPartNum().length() - 1).concat("C"));
					newJob.setQuantityNeeded(job.getQuantityNeeded());
					newJob.setSplitLevel(job.getSplitLevel());
					newJob.setStationId(StationCategory.Categories.COVERPRESS.toString());
					newJob.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.SCHEDULED.toString(), JobStatus.class));
					newJob.setJobPriority(job.getJobPriority());
					newJob.setJobType(lookupDAO.read(JobType.JobTypes.PRINTING.toString(), JobType.class));
					newJob.setBinderyPriority(job.getBinderyPriority());
					newJob.setDueDate(job.getDueDate());
					newJob.setPartColor(job.getPartColor());
					newJob.setPartPaperId(job.getPartPaperId());
					newJob.setProductionMode(job.getProductionMode());
					newJob.setPartTitle(job.getPartTitle());
					newJob.setPartIsbn(job.getPartIsbn());
					newJob.setPartCategory(job.getPartCategory());
					
					newJob.setCreatedDate(new Date());
					newJob.setCreatorId(executingUserId);
					jobDAO.create(newJob);
					cCoverJob = newJob;
				}
				coverJobs.add(cCoverJob);
				//update previous cover press status
				JobSearchBean jsb = new JobSearchBean();
				jsb.setOrderId(cCoverJob.getOrderId());
				Part p = partDAO.read(cCoverJob.getPartNum());
				jsb.setPartFamily(!p.getTopParts().isEmpty() ? cCoverJob.getPartNum().substring(0, cCoverJob.getPartNum().length() - 1) : cCoverJob.getPartNum());
				jsb.setStationId(StationCategory.Categories.COVERPRESS.toString());
				jsb.setMaxSplitLevel(cCoverJob.getSplitLevel());
				List<Job> result = jobDAO.readAll(jsb);
				//TODO: check if quantity of old jobs should be updated
				if(!result.isEmpty()){
					for(Job j : result){
						j.setJobStatus(lookupDAO.read(JobStatus.JobStatuses.COMPLETE_PARTIAL.toString(), JobStatus.class));
						jobDAO.update(j);
					}
				}
			}
			for(Job job : coverJobs)
			{
				Part pr = partDAO.read(job.getPartNum());
				if (pr.getLamination() == null)
					pr.setLamination(lookupDAO.read("NOLAM", Lamination.class));
				job.setPartLamination(pr.getLamination());
			}	
			//regroup each cover job by lamination type
			Map<Object, List<Job>> joblistGrouped =
					coverJobs.stream().collect(Collectors.groupingBy(w -> w.getPartLamination()));
			
			List<CoverSection> createdSections = new ArrayList<>();
			int counter = 1;

			//create batches by max quantity and lamination type
			int maxQty = Integer.parseInt(getLookupDAO().read("COVERBATCH_MAX_QTY", Preference.class).getName());
			for (Map.Entry<Object, List<Job>> entry : joblistGrouped.entrySet())
			{
				List<Job> cvJobsForProd = entry.getValue();
				int currentLot = maxQty;
				boolean allZeros;
				int currentIndex;
				//create section
				CoverSection section = new CoverSection();
				CoverBatchJob cbJob = new  CoverBatchJob();
				SortedSet<CoverBatchJob>  jobList = new TreeSet<CoverBatchJob>(new JobsByBatchOrderingComparator());
				//section.setJobs(jobList);
				for(Job tJob: cvJobsForProd)
				{
					tJob.setQuantityProduced(tJob.getQuantityNeeded());
				}	
				do {
					allZeros = true;
					
					for(currentIndex = 0;currentIndex<cvJobsForProd.size();currentIndex++) {
						
						if(cvJobsForProd.get(currentIndex).getQuantityProduced()>0) {
							allZeros = false;
							break;
						}
					}
					if (allZeros && !jobList.isEmpty())
					{
						//create pdf batch file
						int quantity =0;
						//save jobs associated to a batch 
						for(CoverBatchJob job : jobList)
						{
							quantity+=job.getQuantity();
						}
						//create the section
						section.setJobs(jobList);									
						section.setCoverSectionName("A"+theRoll.getRollId()+" "+counter);
						section.setCreatedDate(new Date());
						section.setQuantity(quantity);
						section.setCreatorId(executingUserId);
						section.setPriority(theRoll.getPriority());
						section.setStatus(lookupDAO.read(BatchStatus.statuses.NEW.toString(), SectionStatus.class));
						section.setLaminationType((Lamination) entry.getKey());
						//coverBatchDAO.update(batch);
						createdSections.add(section);									
						jobList = new TreeSet<CoverBatchJob>(new JobsByBatchOrderingComparator());
						currentLot = maxQty;
						section = new CoverSection();
						counter++;
					}
					if(!allZeros) {
														
						if(cvJobsForProd.get(currentIndex).getQuantityProduced() <= currentLot) {
							currentLot = (int) (currentLot - cvJobsForProd.get(currentIndex).getQuantityProduced());				
							cbJob.setJob(cvJobsForProd.get(currentIndex));
							cbJob.setQuantity((int) cvJobsForProd.get(currentIndex).getQuantityProduced());	
							jobList.add(cbJob);
							cbJob = new  CoverBatchJob();
							cvJobsForProd.get(currentIndex).setQuantityProduced(0);
						}else {
							int diff = (int) (cvJobsForProd.get(currentIndex).getQuantityProduced() - currentLot);
							cbJob.setJob(cvJobsForProd.get(currentIndex));
							cbJob.setQuantity(currentLot);
							jobList.add(cbJob);
							cbJob = new  CoverBatchJob();
							cvJobsForProd.get(currentIndex).setQuantityProduced(diff);
							currentLot = 0;
						}
									
						if(currentLot == 0) {
							//create pdf batch file
							int quantity =0;
							//save jobs associated to a batch 
							for(CoverBatchJob job : jobList)
							{
								quantity+=job.getQuantity();
							}
							//create the section
							section.setJobs(jobList);									
							section.setCoverSectionName("A"+theRoll.getRollId()+" "+counter);
							section.setCreatedDate(new Date());
							section.setQuantity(quantity);
							section.setCreatorId(executingUserId);
							section.setPriority(theRoll.getPriority());
							section.setStatus(lookupDAO.read(BatchStatus.statuses.NEW.toString(), SectionStatus.class));
							section.setLaminationType((Lamination) entry.getKey());
							//coverBatchDAO.update(batch);
							createdSections.add(section);									
							jobList = new TreeSet<CoverBatchJob>(new JobsByBatchOrderingComparator());
							currentLot = maxQty;
							section = new CoverSection();
							counter++;
						}
					}
					
				}while(!allZeros);
				
			

			}
			int qty =0;
			//save jobs associated to a batch 
			for(CoverSection tsection : createdSections)
			{
				qty+=tsection.getQuantity();
			}

				if (recreateBId != null) {
					// recreate the existing batch
					CoverBatch batch = null;
					CoverBatchSearchBean csb = new CoverBatchSearchBean();
					csb.setCoverBatchId(recreateBId);
					List<CoverBatch> batches = coverBatchDAO.readAll(csb);
					if(batches != null && !batches.isEmpty() ){
						batch = batches.get(0);
					}
					batch.setCreatedDate(new Date());
					batch.setCreatorId(executingUserId);
					batch.setPriority(theRoll.getPriority());
					batch.setQuantity(qty);
					batch.setStatus(lookupDAO.read(BatchStatus.statuses.NEW.toString(), BatchStatus.class));
					SortedSet<CoverSection> tsections = batch.getSections();
					Iterator<CoverSection> it = tsections.iterator();
					int icount= 0;
					

					while (it.hasNext())
					{     

						if (icount > createdSections.size() -1)
						break;	
						CoverSection cv = it.next();
						createdSections.get(icount).setCoverSectionId(cv.getCoverSectionId());
						icount++;
					}
					SortedSet<CoverSection> bsect = new TreeSet<CoverSection>(new SectionsByBatchOrderingComparator()); 
					for (CoverSection sect : createdSections) {
						sect.setCoverSectionName(sect.getCoverSectionName() + " of " + createdSections.size());
						sect.setBatchId(batch.getCoverBatchId());

						bsect.add(sect);
					}
					batch.setSections(bsect);
					coverBatchDAO.update(batch);

				} else {
					// create the new batch
					CoverBatch cbatch = new CoverBatch();
					cbatch.setCoverBatchName("A" + theRoll.getRollId());
					cbatch.setRoll(theRoll);
					cbatch.setCreatedDate(new Date());
					cbatch.setCreatorId(executingUserId);
					cbatch.setPriority(theRoll.getPriority());
					cbatch.setQuantity(qty);
					cbatch.setStatus(lookupDAO.read(BatchStatus.statuses.NEW.toString(), BatchStatus.class));
					SortedSet<CoverSection> bsect = new TreeSet<CoverSection>(new SectionsByBatchOrderingComparator()); // Set_section_name
					for (CoverSection sect : createdSections) {
						sect.setCoverSectionName(sect.getCoverSectionName() + " of " + createdSections.size());
						bsect.add(sect);
					}
					cbatch.setSections(bsect);
					coverBatchDAO.update(cbatch);
				}
			}
		} catch (Exception ex) {
			logger.error("Error occurred while creating the batch : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}
	
	/** 
	 * recreate a batch associated to a roll;
	 */
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public boolean reCreateBatch(Integer batchId, String executingUserId) throws PersistenceException {
		try {
			boolean access = false;
			CoverBatch batch = null;
			CoverBatchSearchBean csb = new CoverBatchSearchBean();
			csb.setCoverBatchId(batchId);
			List<CoverBatch> batches = coverBatchDAO.readAll(csb);
			if (!batches.isEmpty() && batches != null) {
				batch = batches.get(0);
				int count = 0; 
				for (CoverSection sc : batch.getSections()) {
					if (sc.getStatus()
							.equals(lookupDAO.read(SectionStatus.statuses.NEW.toString(), SectionStatus.class))) {
						count++;
					}
				}
				if (count == batch.getSections().size()) {
					createBatch(batch.getRoll().getRollId(), executingUserId, batchId);
					access = true;
				}
				
			}
			return access;
		} catch (Exception ex) {
			logger.error("Error occurred while re-creating the batch : " + ex.getMessage(), ex);
			throw new PersistenceException(ex);
		}
	}

	/**
	 * @return the CoverBatchDAO
	 */
	public CoverBatchDAO getCoverBatchDAO() {
		return coverBatchDAO;
	}

	/**
	 * @param dao the CoverBatchDAO to set
	 */
	public void setCoverBatchDAO(CoverBatchDAO dao) {
		this.coverBatchDAO = dao;
	}
	
	/**
	 * @return the CoverSectionDAO
	 */
	public CoverSectionDAO getCoverSectionDAO() {
		return coverSectionDAO;
	}
	
	/**
	 * @param dao the CoverSectionDAO to set
	 */
	public void setCoverSectionDAO(CoverSectionDAO coverSectionDAO) {
		this.coverSectionDAO = coverSectionDAO;
	}

	public LookupDAO getLookupDAO() {
		return lookupDAO;
	}

	public void setLookupDAO(LookupDAO lookupDAO) {
		this.lookupDAO = lookupDAO;
	}

}
