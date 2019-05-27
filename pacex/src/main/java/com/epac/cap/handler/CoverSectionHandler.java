package com.epac.cap.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.common.SectionRunInfo;
import com.epac.cap.model.BatchStatus;
import com.epac.cap.model.CoverBatch;
import com.epac.cap.model.CoverBatchJob;
import com.epac.cap.model.CoverSection;
import com.epac.cap.model.Job;
import com.epac.cap.model.JobStatus;
import com.epac.cap.model.JobType;
import com.epac.cap.model.Lamination;
import com.epac.cap.model.Machine;
import com.epac.cap.model.PaperType;
import com.epac.cap.model.Part;
import com.epac.cap.model.Preference;
import com.epac.cap.model.Roll;
import com.epac.cap.model.RollStatus;
import com.epac.cap.model.RollType;
import com.epac.cap.model.SectionStatus;
import com.epac.cap.model.StationCategory;
import com.epac.cap.model.WFSDataSupport;
import com.epac.cap.model.WFSLocation;
import com.epac.cap.repository.CoverBatchDAO;
import com.epac.cap.repository.CoverBatchJobDAO;
import com.epac.cap.repository.CoverBatchSearchBean;
import com.epac.cap.repository.CoverSectionDAO;
import com.epac.cap.repository.CoverSectionSearchBean;
import com.epac.cap.repository.JobDAO;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.repository.MachineDAO;
import com.epac.cap.repository.PartDAO;
import com.epac.cap.repository.RollDAO;
import com.epac.cap.repository.RollSearchBean;
import com.epac.cap.utils.BatchFactory;
import com.epac.cap.utils.LogUtils;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Interacts with Cover Section data.  Uses CoverSectionDAO for entity persistence.
 * @author slimj
 *
 */
@Service
public class CoverSectionHandler {
	
	@Autowired
	private CoverBatchDAO coverBatchDAO;
	
	@Autowired
	private CoverSectionDAO coverSectionDAO;
	
	@Autowired
	private MachineDAO machineDAO;
	
	@Autowired
	private RollDAO rollDAO;
	
	@Autowired
	private JobDAO jobDAO;
	
	@Autowired
	private JobHandler jobHandler;
	
	@Autowired
	MachineHandler machineHandler;
	
	@Autowired
	PartDAO partDAO;
	
	@Autowired
	private LookupDAO lookupDAO;
	
	@Autowired
	private CoverBatchJobDAO coverBatchJobDAO;
	
	private static Logger logger = Logger.getLogger(CoverSectionHandler.class);

	/***
	 * No arg constructor.
	 */
	public CoverSectionHandler(){  }
	
	/** 
	 * Calls the corresponding readAll method on the CoverSectionDAO with sepecific criteria.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<CoverSection> readAll(CoverSectionSearchBean searchBean) throws PersistenceException{
		try{
			return getCoverSectionDAO().readAll(searchBean);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a list of Cover Sections : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<CoverSection> findNewSection(){
		return getCoverSectionDAO().findNewSection();
	}
	
	/** 
	 * Calls the corresponding readAll method on the CoverSectionDAO.
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<CoverSection> readAll() throws PersistenceException{
		try{
			return getCoverSectionDAO().readAll(null);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a list of Cover Sections : " + ex.getMessage(),ex);
			throw new PersistenceException(ex);
		}    
	}

	/** 
	 * Calls the corresponding update method on the SectionDAO.
	 *
	 */	 
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void update(CoverSection bean) throws PersistenceException {
		try {
			prepareBeans(bean);
			getCoverSectionDAO().update(bean);		   
		} catch (Exception ex) {
			logger.error("Error occurred updating a section : " + ex.getMessage(),ex); 
			throw new PersistenceException(ex);
		}
	}	
	
	private void prepareBeans(CoverSection section) {
		if (section.getMachineId() != null && StringUtils.isEmpty(section.getMachineId())) {
			section.setMachineId(null);
		}
		// set the status for all jobs on the section
		if (section.getStatus().getId().equals(SectionStatus.statuses.NEW.toString())) {
			for (CoverBatchJob jb : section.getJobs()) {
				jb.getJob().setJobStatus(lookupDAO.read(JobStatus.JobStatuses.SCHEDULED.toString(), JobStatus.class));
				jobDAO.update(jb.getJob());
			}
		} else if (section.getStatus().getId().equals(SectionStatus.statuses.RETIRED.toString())) {
			for (CoverBatchJob jb : section.getJobs()) {
				jb.getJob().setJobStatus(lookupDAO.read(JobStatus.JobStatuses.COMPLETE.toString(), JobStatus.class));
				jobDAO.update(jb.getJob());
			}
		}
		if (section.getStatus() != null && !StringUtils.isEmpty(section.getStatus().getId())) {
			section.setStatus(lookupDAO.read(section.getStatus().getId(), SectionStatus.class));
		} else {
			section.setStatus(null);
		}
	}
	
	/** 
	 * Un-Assigning a section from the machine;
	 */
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)
	public void unassignSection(Integer sectionId, String executingUserId) throws PersistenceException {
		try {
			if (sectionId != null) {
				CoverSection section = coverSectionDAO.read(sectionId);
				if (section != null) {
					section.setMachineId(null);
					section.setStatus(lookupDAO.read(SectionStatus.statuses.NEW.toString(), SectionStatus.class));
					section.setMachineOrdering(0);
					for (CoverBatchJob jb : section.getJobs()) {
						jb.getJob().setMachineId(null);
						jb.getJob().setJobStatus(
								lookupDAO.read(JobStatus.JobStatuses.SCHEDULED.toString(), JobStatus.class));
					}
					getCoverSectionDAO().update(section);

					CoverBatch cb = null;
					CoverBatchSearchBean cbsb = new CoverBatchSearchBean();
					cbsb.setCoverBatchId(section.getBatchId());
					List<CoverBatch> cbList = coverBatchDAO.readAll(cbsb);
					if (!cbList.isEmpty() && cbList != null) {
						cb = cbList.get(0);
					} else {
						return;
					}
					int newcount = 0;
					for (CoverSection sc : cb.getSections()) {
						if (sc.getStatus()
								.equals(lookupDAO.read(SectionStatus.statuses.NEW.toString(), SectionStatus.class))) {
							newcount++;
						}
					}
					if (newcount == cb.getSections().size()) {
						cb.setStatus(lookupDAO.read(BatchStatus.statuses.NEW.toString(), BatchStatus.class));
						getCoverBatchDAO().update(cb);

					}

				}
			}
		} catch (Exception ex) {
			logger.error("Error occurred un-assigning the section from the machine : " + ex.getMessage(), ex);
			throw new PersistenceException(ex);
		}
	}	
	
	
	/** 
	 * Create a section pdf file;
	 * @throws Exception 
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	 public synchronized SectionRunInfo createSection(CoverSection section, Boolean recreate) throws Exception 
	{
		List<PdfReader> readers = new ArrayList<PdfReader>();
		List<String> isbnNotImposed = new ArrayList<String>();
		List<File> files = new ArrayList<File>();
		SectionRunInfo srInfo = new SectionRunInfo();
		File fsOut = null;
		File fsSection = null;
		Map<String, Integer> index = new HashMap<>();
		index.put("quantity", 0);
		index.put("index", 1);
		SortedSet<CoverBatchJob> tempJobs = new TreeSet<CoverBatchJob>(section.getJobs());
		try {
			//--[START]------- added precheck for ISBN without imposed cover file
			boolean dsContains = false;
			// preCheck on imposed files
			for (CoverBatchJob cbjob : section.getJobs()) {
				Part pr = partDAO.read(cbjob.getJob().getPartNum());
				Set<WFSDataSupport> onProdDataSupports = pr.getDataSupportsOnProd();
				if (onProdDataSupports.isEmpty() || pr.getPartWorkFlowOnProd() == null) {
					isbnNotImposed.add(pr.getPartNum());
				}
				if (pr.getPartWorkFlowOnProd() != null) {
					for (WFSDataSupport ds : onProdDataSupports) {
						if (ds.getName().equals(WFSDataSupport.NAME_IMPOSE)
								&& ds.getDsType().equalsIgnoreCase(WFSDataSupport.TYPE_COVER)) {
							dsContains = true;
							WFSLocation loc = ds.getLocationdByType(WFSLocation.DESTINATION);
							if (loc != null && loc.getPath() != null) {
								File f = new File(loc.getPath());
								if (!f.exists()) {
									isbnNotImposed.add(pr.getPartNum());
									logger.info("No Imposed Cover found for [" + pr.getPartNum() + "]");
								} else {
									continue;
								}

							}
						}
					}
				}
			}
			if (isbnNotImposed.size() != 0) {
				srInfo.setErrors(isbnNotImposed);
				return srInfo;
			}
			//--[END]----------------------------------
			for (CoverBatchJob cbjob : tempJobs) {
				index.put("quantity", index.get("quantity") + cbjob.getQuantity());
			}
			
			for (CoverBatchJob cbjob : tempJobs) {
				Part pr = partDAO.read(cbjob.getJob().getPartNum());
				File file = BatchFactory.createCoverBatchJob(section, cbjob.getJob(), pr, index, cbjob.getQuantity());
				if (file != null) {
					PdfReader freader = new PdfReader(file.getAbsolutePath());
					readers.add(freader);
					files.add(file);
				}
			}

			Document doc = new Document();
			CoverBatch batch = getCoverBatchDAO().read(section.getBatchId());
			if (batch != null) {
				fsOut = File.createTempFile(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()), "final.pdf");
				LogUtils.debug("created fsOut =["+fsOut.getAbsolutePath()+"]");
				PdfSmartCopy copyPages = new PdfSmartCopy(doc, new FileOutputStream(fsOut));
				doc.open();
				// add 6 empty pages for section tag
				copyPages.addPage(readers.get(0).getPageSize(1), readers.get(0).getPageRotation(1));
				copyPages.addPage(readers.get(0).getPageSize(1), readers.get(0).getPageRotation(1));
				copyPages.addPage(readers.get(0).getPageSize(1), readers.get(0).getPageRotation(1));
				copyPages.addPage(readers.get(0).getPageSize(1), readers.get(0).getPageRotation(1));
				copyPages.addPage(readers.get(0).getPageSize(1), readers.get(0).getPageRotation(1));
				copyPages.addPage(readers.get(0).getPageSize(1), readers.get(0).getPageRotation(1));
				for (PdfReader treader : readers) {
					copyPages.addDocument(treader);
				}
				copyPages.close();
			}
			doc.close();
			for (PdfReader treader : readers)
				treader.close();
			for (File tfile : files) {
				// remove temporary files
				if (tfile.delete()) {
					logger.info(tfile + " is deleted!");
				} else {
					logger.info("Delete operation is failed for [" + tfile + "]");
				}
			}
			// create batch directory
			String repoFolderLocation = System.getProperty("com.epac.cap.directories.batches");
			
			File batchDirectroy = new File(repoFolderLocation, batch.getCoverBatchName());
			batchDirectroy.mkdirs();
			
			File emFileComp = File.createTempFile(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()), "preStep1.pdf");
			// create info header
			PdfReader headReader = new PdfReader(fsOut.getAbsolutePath());
			PdfStamper stampHeader = new PdfStamper(headReader, new FileOutputStream(emFileComp));
			Rectangle page = headReader.getPageSize(1);
			PdfContentByte ovctn = null;
			BaseFont helvetica;
			helvetica = BaseFont.createFont("Helvetica.otf", BaseFont.CP1252, BaseFont.EMBEDDED);
			Font fonthead1 = new Font(helvetica, 50, Font.NORMAL);
			Font fonthead2 = new Font(helvetica, 62, Font.BOLD);

			// add 3 pages tag: sequence number and batch name
			for (int i = 1; i <= 6; i++) {
				if(i % 2 == 0)
					continue;
				
				ovctn = stampHeader.getOverContent(i);
				ColumnText.showTextAligned(ovctn, Element.ALIGN_CENTER,
						new Phrase(section.getCoverSectionName(), fonthead1), page.getWidth() / 2, page.getHeight() / 2,
						0);

				ColumnText.showTextAligned(ovctn, Element.ALIGN_CENTER,
						new Phrase(section.getLaminationType().getName(), fonthead2), page.getWidth() / 2,
						page.getHeight() / 2 - 100, 0);
			}
			stampHeader.close();
			headReader.close();
			if (fsOut.delete()) {
				logger.info(fsOut.getName() + " is deleted!");
			} else {
				logger.info("Delete operation is failed for [" + fsOut.getName() + "]");
			}
			//reverse order
			PdfReader reverseReader = new PdfReader(emFileComp.getAbsolutePath());
	        Rectangle pageSize = reverseReader.getPageSize(1);
			Document docReverse = new Document(pageSize);
			File reverseFile = File.createTempFile(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()), "preStep2.pdf");
			LogUtils.debug("created fsOut =["+fsOut.getAbsolutePath()+"]");
			//PdfSmartCopy reversePages = new PdfSmartCopy(docReverse, new FileOutputStream(reverseFile));
			FileOutputStream file = new FileOutputStream(reverseFile);
			PdfWriter writer = PdfWriter.getInstance(docReverse, file);
			docReverse.open();
			PdfContentByte reverseCb = writer.getDirectContent();

			for(int i = reverseReader.getNumberOfPages(); i >=1 ; i--){
				docReverse.newPage();
				writer.setPageEmpty(false);
				PdfImportedPage reversePage = writer.getImportedPage(reverseReader, i);
				reverseCb.addTemplate(reversePage,0,0);
			}
	        docReverse.close();
			// change first empty page to last
			Document emFileDoc = new Document();
			PdfReader emFileReader = new PdfReader(reverseFile.getAbsolutePath());
			// create section file
						// read from db url then check if null so simple generation
						// otherwise rerun and reset the version of the file with _Vx
						if (section.getPath() != null && !section.getPath().isEmpty()) {
							if (recreate == false) {
								fsSection = new File(section.getPath());
							} else {
								if (!section.getPath().contains("_V")) {

									fsSection = new File(repoFolderLocation + System.getProperty("file.separator")
											+ batch.getCoverBatchName() + System.getProperty("file.separator")
											+ section.getCoverSectionName() + "_V1.pdf");
								} else {
									String[] names = section.getPath().split("_V");
									int version = Integer.parseInt(names[1].split(".pdf")[0]);
									version++;
									fsSection = new File(repoFolderLocation + System.getProperty("file.separator")
											+ batch.getCoverBatchName() + System.getProperty("file.separator")
											+ section.getCoverSectionName() + "_V" + version + ".pdf");
								}
							}
						} else {
							fsSection = new File(
									repoFolderLocation + System.getProperty("file.separator") + batch.getCoverBatchName()
											+ System.getProperty("file.separator") + section.getCoverSectionName() + ".pdf");
						}
			

			PdfSmartCopy emFileCopy = new PdfSmartCopy(emFileDoc, new FileOutputStream(fsSection));
			emFileDoc.open();
			for (int i = 2; i <= emFileReader.getNumberOfPages(); i++) {
				PdfImportedPage impEmpty = emFileCopy.getImportedPage(emFileReader, i);
				emFileCopy.addPage(impEmpty);
			}
			emFileCopy.addPage(emFileReader.getPageSize(1), emFileReader.getPageRotation(1));
			emFileCopy.close();
			emFileDoc.close();
			emFileReader.close();

			if (emFileComp.delete()) {
				logger.info(emFileComp.getName() + " is deleted!");
			} else {
				logger.info("Delete operation is failed for [" + emFileComp.getName() + "]");
			}
			if (reverseFile.delete()) {
				logger.info(reverseFile.getName() + " is deleted!");
			} else {
				logger.info("Delete operation is failed for [" + reverseFile.getName() + "]");
			}
			// set path of the section
			section.setPath(fsSection.getAbsolutePath());
			getCoverSectionDAO().update(section);
			srInfo.setGenFile(fsSection);
			LogUtils.debug("created fsSection =["+fsSection.getAbsolutePath()+"]");

		} catch (DocumentException | IOException ex) {
			logger.error("Error occurred while creating section pdf file : " + ex.getMessage(), ex);
		}
		return srInfo;
	}
	
	/** 
	 * Create a section pdf file;
	 * @throws Exception 
	 */
	@Transactional(rollbackFor=PersistenceException.class, propagation=Propagation.REQUIRED)	
	public List<String> recreateSection(Integer sectionId) throws Exception {
		List<String> errors = new ArrayList<String>();

		if (sectionId != null) {
			CoverSection section = coverSectionDAO.read(sectionId);
			if (section != null) {
				if (section.getMachineId() != null) {
				LogUtils.debug("On machine re creation mode of the section is started...");
				Machine machine = getMachineDAO().read(section.getMachineId());
				section.setCopyStatus(null);
				getCoverSectionDAO().update(section);
				LogUtils.debug("Section "+section.getCoverSectionId()+" is updated");
				getMachineHandler().prepareSection(machine, section, true);
			}
			else {
				LogUtils.debug("out of print re creation mode of the section is started...");
				errors = createSection(section, true).getErrors();
				}
			}
		}
		return errors;
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
	/**
	 * @return the MachineDAO
	 */
	public MachineDAO getMachineDAO() {
		return machineDAO;
	}

	/**
	 * @param dao the MachineDAO to set
	 */
	public void setMachineDAO(MachineDAO dao) {
		this.machineDAO = dao;
	}

	public MachineHandler getMachineHandler() {
		return machineHandler;
	}

	public void setMachineHandler(MachineHandler machineHandler) {
		this.machineHandler = machineHandler;
	}

	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<CoverSection> fullSearch(String query, Integer maxResult, Integer offset) {	
		return coverSectionDAO.fullSearch(query, maxResult, offset);		
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Integer getCount() {	
		return coverSectionDAO.getCount();		
	}
	
}
