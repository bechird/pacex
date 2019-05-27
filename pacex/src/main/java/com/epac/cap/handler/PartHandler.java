package com.epac.cap.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.config.NDispatcher;
import com.epac.cap.functionel.PrintingTimeCalculator;
import com.epac.cap.functionel.WorkflowEngine;
import com.epac.cap.model.BindingType;
import com.epac.cap.model.Job;
import com.epac.cap.model.Lamination;
import com.epac.cap.model.LookupItem;
import com.epac.cap.model.PaperType;
import com.epac.cap.model.Part;
import com.epac.cap.model.PartCategory;
import com.epac.cap.model.PartCritiria;
import com.epac.cap.model.Preference;
import com.epac.cap.model.SubPart;
import com.epac.cap.model.SubPartId;
import com.epac.cap.model.WFSDataSupport;
import com.epac.cap.model.WFSLocation;
import com.epac.cap.model.WFSPartWorkflow;
import com.epac.cap.model.WFSProductionStatus;
import com.epac.cap.model.WFSProgress;
import com.epac.cap.model.WFSStatus.ProgressStatus;
import com.epac.cap.repository.JobDAO;
import com.epac.cap.repository.JobSearchBean;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.repository.OrderDAO;
import com.epac.cap.repository.OrderSearchBean;
import com.epac.cap.repository.PartDAO;
import com.epac.cap.repository.PartSearchBean;
import com.epac.cap.utils.Format;
import com.epac.cap.utils.LogUtils;
import com.google.common.io.Files;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.mycila.event.api.topic.Topics;


/**
 * Interacts with Part data. Uses PartDAO for entity persistence.
 *
 * @author walid
 *
 */
@Component
@Scope("singleton")
public class PartHandler {

	private static Logger logger = Logger.getLogger(PartHandler.class);

	@Autowired
	private PartDAO partDAO;

	@Autowired
	private LookupDAO lookupDAO;
	
	@Autowired
	private JobDAO jobDAO;
	
	@Autowired
	private OrderDAO orderDAO;

	@Autowired
	private WFSLocationHandler wfsLocationHandler;

	@Autowired
	private WFSDataSupportHandler wfsDataSupportHandler;

	@Autowired
	private WFSWorkflowHandler wfsWorkflowHandler;
	@Autowired
	private LookupHandler lookupHandler;

	@Autowired
	private PrintingTimeCalculator printingTimeCalculator;

	@Value("${com.epac.cap.bookRepository}")
	private String bookRepository;

	public static final String FILE_REPO = "FILEREPOSITORY";
	public static final String SELFCOVER_C = "SELFCOVER";
	public static final String DUSTJACKET_C = "DUSTJACKET";
	public static final String ENDSHEET_C = "PRINTEDENDSHEET";
	public static final String ADM = "Administrative";
	public static final String PNL = "PNL";

	private static Number partNumberCounter = 0;

	public final static  String UNIT_US = "US";
	public final static  String UNIT_FR = "FR";

	private Preference unitValue;

	/**
	 * A method that returns the file path generated using the PartNum,
	 * ISBN, type and Category
	 */
	public String generatePath(String isbn, String partNum, String partCategory, String type) throws PersistenceException {
		String result = null;
		LookupItem item = lookupDAO.read(FILE_REPO, Preference.class);
		if (item != null) {
			result = item.getName();// The value of the FILEREPOSITORY Preference
		} else {
			result = bookRepository;
		}
		result = result + File.separator + isbn + "_" + partNum + File.separator + type + File.separator + partCategory + File.separator;
		return result;
	}

	public String getImposedDirectory(String isbn, String partNum){
		String repository = null;
		LookupItem item = lookupDAO.read(FILE_REPO, Preference.class);
		if (item != null) {
			repository = item.getName();// The value of the FILEREPOSITORY Preference
		} else {
			repository = bookRepository;
		}
		repository = repository + File.separator + isbn + "_" + partNum + File.separator + WFSDataSupport.NAME_IMPOSE;
		return repository;
	}
	public Part findPartByIsbn(String isbn){
		return partDAO.findPartByIsbn(isbn);
	}
	/**
	 * A method that returns the full uploaded file name generated using the FileName
	 */
	public String generateFileName(String fileName) {
		String result = "";
		result = resolveFilenameWithoutExtension(fileName) + "_"+ new Date().getTime() + ".pdf";
		return result;
	}

	/**
	 * A method that returns the uploaded file path generated using the PartNum,
	 * ISBN and Category
	 */
	public File generateFilePath(String isbn, String partNum, String partCategory, String type) throws PersistenceException {
		File result = new File(generatePath( isbn, partNum, partCategory, type) );
		result.mkdirs();
		LogUtils.debug("upload file path is being generated for file with isbn [" +isbn+ "]...");
		return result;
	}

	public String resolveFilenameWithoutExtension(String fileName){
		return Files.getNameWithoutExtension(fileName);
	}

	/*@Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.REQUIRED)
	public String generatePartNb() throws PersistenceException {
		String result = "PX";
		List<Part> parts = new ArrayList<Part>();
		int i = 0;
		String numericString = "";
		PartSearchBean psb = new PartSearchBean();
		psb.setPartNumStart(result);
		parts = readAll(psb);
		parts.sort(Comparator.comparing(Part::getPartNum));
		if (!parts.isEmpty()) {

	 * String mostRecentPartNum = parts.get(0).getPartNum();
	 * if(mostRecentPartNum.length() > 2){
	 * if(mostRecentPartNum.contains("-")){ numericString =
	 * mostRecentPartNum.substring(2,
	 * mostRecentPartNum.lastIndexOf("-")); }else{ numericString =
	 * mostRecentPartNum.substring(2); } try{ i =
	 * Integer.parseInt(numericString); }catch (NumberFormatException
	 * nfe){ result = mostRecentPartNum; } i++; }

			i = parts.size() + 1;
		}

		boolean test = true;
		while (test)
		{
			result = result + i;
			if (partDAO.read(result)==null)
				return result;
			else i += 1;
		}
		return result;
	}*/

	@Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.SUPPORTS)
	public synchronized String  generatePartNb() throws PersistenceException {
		String result = "PX";
		if(partNumberCounter.equals(0)){
			Number res = partDAO.getLastPartNb();
			if(res != null){
				partNumberCounter = res;
			}
		}
		partNumberCounter = partNumberCounter.intValue() + 1;
		return result + partNumberCounter;
	}

	/**
	 * A method that returns the most up to date uploaded file name located in
	 * the specified file path
	 */
	public String findFileName(String filePath) throws NumberFormatException {
		String result = null;
		Map<String, String> existingFiles = new TreeMap<String, String>(Collections.reverseOrder());
		File dir = new File(filePath);
		if (dir.exists() && dir.isDirectory()) {
			String[] names = dir.list();
			for (int i = 0; i < names.length; i++) {
				String fileName = names[i];
				if (fileName.equalsIgnoreCase(".DS_Store"))
					continue;
				String originalName = fileName.substring(0, fileName.lastIndexOf("_"));
				String timestampName = fileName.substring(originalName.length() + 1, fileName.lastIndexOf("."));
				// existingFiles.put(timestampName,
				// originalName+"."+Files.getFileExtension(fileName));
				existingFiles.put(timestampName, fileName);
			}
			if (!existingFiles.isEmpty()) {
				result = existingFiles.values().iterator().next();
			}
		}
		return result;
	}

	/**
	 * Calls the corresponding create method on the PartDAO.
	 *
	 */
	@Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.REQUIRED)
	public void create(Part bean) throws PersistenceException {
		String[] filePaths = null;
		if (bean.getFilePath() != null)
			filePaths = pathSplitter(bean.getFilePath());

		Part coverPart = null;
		Part textPart  = null;
		Part djPart  = null;
		Part esPart  = null;
		String filePath = null;
		try {

			// set the criterias
			Set<String> theCritirias = bean.getCritirias();
			if (!theCritirias.isEmpty()) {
				for (String r : theCritirias) {
					PartCritiria ur = new PartCritiria(bean.getPartNum(), r);
					bean.getPartCritirias().add(ur);
				}
			}
			bean.setActiveFlag(true);
			bean.setReadyToProduce(false);
			
			if(bean.getCreatedDate() == null){
				bean.setCreatedDate(new Date());
			}
			
			if(bean.getLamination() != null && !StringUtils.isEmpty(bean.getLamination().getId())){
				bean.setLamination(lookupDAO.read(bean.getLamination().getId(), Lamination.class));
			}else{
				bean.setLamination(null);
			}
			
			// set the version
			if (bean.getVersion() == null) {
				PartSearchBean psb = new PartSearchBean();
				psb.setIsbnExact(bean.getIsbn());
				psb.setHasNoParent(true);
				psb.setActiveFlag(true);
				List<Part> searchedParts = readAll(psb);
				if (searchedParts.isEmpty()) {
					bean.setVersion(1);
				} else {
					bean.setVersion(searchedParts.size() + 1);
				}
			}
			// set the category based on the criteria and binding type:
			PartCategory bookPC = getLookupDAO().read(Part.PartsCategory.BOOK.getName(), PartCategory.class);
			PartCategory textPC = getLookupDAO().read(Part.PartsCategory.TEXT.getName(), PartCategory.class);
			PartCategory coverPC = getLookupDAO().read(Part.PartsCategory.COVER.getName(), PartCategory.class);
			PartCategory djPC = getLookupDAO().read(Part.PartsCategory.DUSTJACKET.getName(), PartCategory.class);
			PartCategory esPC = getLookupDAO().read(Part.PartsCategory.ENDSHEET.getName(), PartCategory.class);

			//if (bean.getBindingType() != null && Part.BindingTypes.CARDSS.getName().equals(bean.getBindingType().getId())) {
				
				
				
			//} else if (!theCritirias.isEmpty() && theCritirias.contains(SELFCOVER_C)) {
				
				
				
			//} else {
				bean.setCategory(bookPC);
				// if category is a book, add the sub parts; Add sub part/top
				// part relations;
				
			// calculate the best sheet
			Map<String, Float> bs = printingTimeCalculator.getPartBestSheet(bean);
			bean.setBestSheet(bs.isEmpty() ? null: bs.keySet().iterator().next());
			bean.setBestSheetWaste(bs.isEmpty() ? null: bs.values().iterator().next());
			// Add the text
			if (bean.getBindingType() == null || !Part.BindingTypes.CARDSS.getName().equals(bean.getBindingType().getId())) {
				textPart = new Part();
				textPart.setPartNum(bean.getPartNum() + "T");
				if (textPart.getPartNum().length() > 25) {
					textPart.setPartNum(textPart.getPartNum().substring(0, 25));
				}
				textPart.setSoftDelete(bean.getSoftDelete());
				textPart.setActiveFlag(bean.getActiveFlag());
				textPart.setPagesCount(bean.getPagesCount());
				textPart.setColors(bean.getColors());
				textPart.setLength(bean.getLength());
				textPart.setWidth(bean.getWidth());
				textPart.setPaperType(bean.getPaperType());
				textPart.setCategory(textPC);
				textPart.setBindingType(getLookupDAO().read(Part.BindingTypes.DEFAULT.getName(), BindingType.class));
				filePath = generateFilePath(bean.getIsbn(), bean.getPartNum(), textPC.getName(), "Original").getPath();
				//textPart.setFileName(findFileName(filePath));
				//if (!StringUtils.isEmpty(textPart.getFileName())) {
				//textPart.setFilePath(filePath);
				/*} else {
					List<Object> textObjects = new ArrayList<Object>();
					textObjects.add(filePaths[1]);
					textObjects.add(filePath + "/" + bean.getIsbn() + "-" + new Date().getTime() + ".pdf");
					NDispatcher.getDispatcher().publish(Topics.topic("cap/events/download"), textObjects);
				}*/
				textPart.setIsbn(bean.getIsbn());
				textPart.setTitle(bean.getTitle());
				textPart.setBestSheet(bean.getBestSheet());
				textPart.setBestSheetWaste(bean.getBestSheetWaste());
				textPart.setCreatedDate(bean.getCreatedDate());
				textPart.setCreatorId(bean.getCreatorId());
				
				if("System_Esprint".equals(bean.getCreatorId())){
					
					textPart.setFileName(bean.getFileName());
					
					WFSPartWorkflow pwf = new WFSPartWorkflow();
					pwf.setWfStatus(WFSProductionStatus.statuses.ONPROD.getName());
					pwf.setCreatedDate(new Date());
					pwf.setWorkflow(wfsWorkflowHandler.getWorkflow(1));
					if(bean.getPaperType() != null && bean.getPaperType().getMedias() != null && !bean.getPaperType().getMedias().isEmpty()){
						pwf.setRollWidth(bean.getPaperType().getMedias().iterator().next().getRollWidth());
					}
					pwf.setPartNum(textPart.getPartNum());
					pwf.setIsReady(true);

					WFSProgress progress = new WFSProgress();

					progress.setCreatedDate(new Date());
					progress.setStatus(ProgressStatus.DONE.getName());
					progress.setCreatorId(textPart.getCreatorId());
					progress.setPartWorkflow(pwf);

					pwf.addProgress(progress);

					textPart.addWorkflow(pwf);
					getPartDAO().create(textPart);
					SortedSet<WFSDataSupport> dataSupports = bean.getDataSupports();
					
					for(WFSDataSupport ds : dataSupports){
						if("Text".equals(ds.getDescription())){
							LogUtils.debug("DataSupport: Text/"+ ds.getDsType()+"/"+ds.getName());
							ds.setPartNumb(textPart.getPartNum());
							ds.setCreatedDate(new Date());
							wfsDataSupportHandler.save(ds);
							textPart.addDataSupports(ds);
						}
					}
					getPartDAO().update(textPart);
				}else{
					getPartDAO().create(textPart);
				}
				
				SubPart subPart1 = new SubPart(bean.getPartNum(), textPart.getPartNum());
				bean.getSubParts().add(subPart1);
			}
				// Add the cover
			if (theCritirias.isEmpty() || !theCritirias.contains(SELFCOVER_C)) {
				coverPart = new Part();
				coverPart.setPartNum(bean.getPartNum() + "C");
				if (coverPart.getPartNum().length() > 25) {
					coverPart.setPartNum(coverPart.getPartNum().substring(0, 25));
				}
				coverPart.setSoftDelete(bean.getSoftDelete());
				coverPart.setActiveFlag(bean.getActiveFlag());
				coverPart.setThickness(bean.getThickness());
				coverPart.setPaperType(bean.getPaperType());
				coverPart.setLength(bean.getLength());
				coverPart.setWidth(bean.getWidth());
				coverPart.setCoverColor(bean.getCoverColor());
				coverPart.setLamination(bean.getLamination());
				coverPart.setCategory(coverPC);
				coverPart.setBindingType(getLookupDAO().read(Part.BindingTypes.DEFAULT.getName(), BindingType.class));
				filePath = generateFilePath(bean.getIsbn(), bean.getPartNum(), coverPC.getName(), "Original").getPath();
				//coverPart.setFileName(findFileName(filePath));
				//if (!StringUtils.isEmpty(coverPart.getFileName())) {
				//coverPart.setFilePath(filePath);
				/*} else {
					List<Object> coverObjects = new ArrayList<Object>();
					coverObjects.add(filePaths[0]);
					coverObjects.add(filePath + "/" + bean.getIsbn() + "-" + new Date().getTime() + ".pdf");
					NDispatcher.getDispatcher().publish(Topics.topic("cap/events/download"), coverObjects);
				}*/
				coverPart.setIsbn(bean.getIsbn());
				coverPart.setTitle(bean.getTitle());
				// For the case of Loose leaf, only create cover part if there is cover pdf uploaded
				//if((bean.getBindingType() != null && !Part.BindingTypes.LOOSELEAF.getName().equals(bean.getBindingType().getId())) || 
				//		(coverPart.getFileName() != null && !coverPart.getFileName().isEmpty())){
					coverPart.setCreatedDate(bean.getCreatedDate());
					coverPart.setCreatorId(bean.getCreatorId());

					if("System_Esprint".equals(bean.getCreatorId())){
						coverPart.setFileName(bean.getFileName() != null? bean.getFileName().replace("text", "cover"): null);
						WFSPartWorkflow pwf = new WFSPartWorkflow();
						pwf.setWfStatus(WFSProductionStatus.statuses.ONPROD.getName());
						pwf.setCreatedDate(new Date());
						pwf.setPartNum(coverPart.getPartNum());
						pwf.setWorkflow(wfsWorkflowHandler.getWorkflow(3));
						pwf.setIsReady(true);

						WFSProgress progress = new WFSProgress();

						progress.setCreatedDate(new Date());
						progress.setStatus(ProgressStatus.DONE.getName());
						progress.setCreatorId(coverPart.getCreatorId());
						progress.setPartWorkflow(pwf);

						pwf.addProgress(progress);

						coverPart.addWorkflow(pwf);

						getPartDAO().create(coverPart);
						SortedSet<WFSDataSupport> dataSupports = bean.getDataSupports();
						for(WFSDataSupport ds : dataSupports){
							if("Cover".equalsIgnoreCase(ds.getDescription())){
								LogUtils.debug("DataSupport: Cover/"+ ds.getDsType()+"/"+ds.getName());
								ds.setPartNumb(coverPart.getPartNum());
								ds.setCreatedDate(new Date());
								wfsDataSupportHandler.save(ds);
								coverPart.addDataSupports(ds);
							}
						}

						dataSupports.clear();
						getPartDAO().update(coverPart);
					}else{
						getPartDAO().create(coverPart);
					}
					
					
					SubPart subPart2 = new SubPart(bean.getPartNum(), coverPart.getPartNum());
					bean.getSubParts().add(subPart2);
				}
			
			// Add the dustjacket
			if (!theCritirias.isEmpty() && theCritirias.contains(DUSTJACKET_C)) {
				djPart = new Part();
				djPart.setPartNum(bean.getPartNum() + "J");
				if (djPart.getPartNum().length() > 25) {
					djPart.setPartNum(djPart.getPartNum().substring(0, 25));
				}
				djPart.setSoftDelete(bean.getSoftDelete());
				djPart.setActiveFlag(bean.getActiveFlag());
				djPart.setDjPaper(bean.getDjPaper());
				djPart.setDjColor(bean.getDjColor());
				djPart.setCategory(djPC);
				djPart.setBindingType(getLookupDAO().read(Part.BindingTypes.DEFAULT.getName(), BindingType.class));
				filePath = generateFilePath(bean.getIsbn(), bean.getPartNum(), djPC.getName(), "Original").getPath();
				djPart.setIsbn(bean.getIsbn());
				djPart.setTitle(bean.getTitle());
				djPart.setCreatedDate(bean.getCreatedDate());
				djPart.setCreatorId(bean.getCreatorId());
				if("System_Esprint".equals(bean.getCreatorId())){
					djPart.setFileName(bean.getFileName() != null? bean.getFileName().replace("text", "dj"): null);
					getPartDAO().create(djPart);
					SortedSet<WFSDataSupport> dataSupports = bean.getDataSupports();
					for(WFSDataSupport ds : dataSupports){
						if("DustJacket".equalsIgnoreCase(ds.getDescription())){
							LogUtils.debug("DataSupport: DustJacket/"+ ds.getDsType()+"/"+ds.getName());
							ds.setPartNumb(djPart.getPartNum());
							ds.setCreatedDate(new Date());
							wfsDataSupportHandler.save(ds);
							djPart.addDataSupports(ds);
						}
					}
					dataSupports.clear();
					getPartDAO().update(djPart);
				}else{
					getPartDAO().create(djPart);
				}
				SubPart subPart2 = new SubPart(bean.getPartNum(), djPart.getPartNum());
				bean.getSubParts().add(subPart2);
			}
			
			// Add the endSheet
			if (!theCritirias.isEmpty() && theCritirias.contains(ENDSHEET_C)) {
				esPart = new Part();
				esPart.setPartNum(bean.getPartNum() + "E");
				if (esPart.getPartNum().length() > 25) {
					esPart.setPartNum(esPart.getPartNum().substring(0, 25));
				}
				esPart.setSoftDelete(bean.getSoftDelete());
				esPart.setActiveFlag(bean.getActiveFlag());
				esPart.setEsPaper(bean.getEsPaper());
				esPart.setEsColor(bean.getEsColor());
				esPart.setCategory(esPC);
				esPart.setBindingType(getLookupDAO().read(Part.BindingTypes.DEFAULT.getName(), BindingType.class));
				filePath = generateFilePath(bean.getIsbn(), bean.getPartNum(), esPC.getName(), "Original").getPath();
				esPart.setIsbn(bean.getIsbn());
				esPart.setTitle(bean.getTitle());
				esPart.setCreatedDate(bean.getCreatedDate());
				esPart.setCreatorId(bean.getCreatorId());
				if("System_Esprint".equals(bean.getCreatorId())){
					esPart.setFileName(bean.getFileName() != null? bean.getFileName().replace("text", "es"): null);
					getPartDAO().create(esPart);
					SortedSet<WFSDataSupport> dataSupports = bean.getDataSupports();
					for(WFSDataSupport ds : dataSupports){
						if("EndSheet".equalsIgnoreCase(ds.getDescription())){
							LogUtils.debug("DataSupport: EndSheet/"+ ds.getDsType()+"/"+ds.getName());
							ds.setPartNumb(esPart.getPartNum());
							ds.setCreatedDate(new Date());
							wfsDataSupportHandler.save(ds);
							esPart.addDataSupports(ds);
						}
					}
					dataSupports.clear();
					getPartDAO().update(esPart);
				}else{
					getPartDAO().create(esPart);
				}
				SubPart subPart2 = new SubPart(bean.getPartNum(), esPart.getPartNum());
				bean.getSubParts().add(subPart2);
			}
			//}
			
			// Add PNL Pref
			Preference pref = null;
			if(bean.getPnlTemplateId() != null && !bean.getPnlTemplateId().isEmpty()){
				pref = new Preference();
				pref.setId(WorkflowEngine.PNL_TMPL_ID.concat("_").concat(bean.getPartNum()));
				pref.setName(bean.getPnlTemplateId());
				pref.setCreatedDate(new Date());
				pref.setCreatorId(bean.getCreatorId());
				pref.setGroupingValue(ADM);
				pref.setPartNum(bean.getPartNum());
				pref.setPrefSubject(PNL);
				bean.getPnlPreferences().add(pref);
			}
			if(bean.getPnlPageNumber() != null && !bean.getPnlPageNumber().isEmpty()){
				pref = new Preference();
				pref.setId(WorkflowEngine.PNL_PAGE_NUMBER.concat("_").concat(bean.getPartNum()));
				pref.setName(bean.getPnlPageNumber());
				pref.setCreatedDate(new Date());
				pref.setCreatorId(bean.getCreatorId());
				pref.setGroupingValue(ADM);
				pref.setPartNum(bean.getPartNum());
				pref.setPrefSubject(PNL);
				bean.getPnlPreferences().add(pref);
			}
			if(bean.getPnlPrintingNumber() != null && !bean.getPnlPrintingNumber().isEmpty()){
				pref = new Preference();
				pref.setId(WorkflowEngine.PNL_PRINTING_NUMBER.concat("_").concat(bean.getPartNum()));
				pref.setName(bean.getPnlPrintingNumber());
				pref.setCreatedDate(new Date());
				pref.setCreatorId(bean.getCreatorId());
				pref.setGroupingValue(ADM);
				pref.setPartNum(bean.getPartNum());
				pref.setPrefSubject(PNL);
				bean.getPnlPreferences().add(pref);
			}
			if(bean.getPnlNotNeeded() != null && !bean.getPnlNotNeeded().isEmpty()){
				pref = new Preference();
				pref.setId(WorkflowEngine.PNL_EXCLUDED.concat("_").concat(bean.getPartNum()));
				pref.setName(bean.getPnlNotNeeded());
				pref.setCreatedDate(new Date());
				pref.setCreatorId(bean.getCreatorId());
				pref.setGroupingValue(ADM);
				pref.setPartNum(bean.getPartNum());
				pref.setPrefSubject(PNL);
				bean.getPnlPreferences().add(pref);
			}
			if(bean.getPnlLocation() != null && !bean.getPnlLocation().isEmpty()){
				pref = new Preference();
				pref.setId(WorkflowEngine.PNL_LOCATION.concat("_").concat(bean.getPartNum()));
				pref.setName(bean.getPnlLocation());
				pref.setCreatedDate(new Date());
				pref.setCreatorId(bean.getCreatorId());
				pref.setGroupingValue(ADM);
				pref.setPartNum(bean.getPartNum());
				pref.setPrefSubject(PNL);
				bean.getPnlPreferences().add(pref);
			}
			if(bean.getPnlVmargin() != null && !bean.getPnlVmargin().isEmpty()){
				pref = new Preference();
				pref.setId(WorkflowEngine.PNL_VERTICAL_MARGIN.concat("_").concat(bean.getPartNum()));
				pref.setName(bean.getPnlVmargin());
				pref.setCreatedDate(new Date());
				pref.setCreatorId(bean.getCreatorId());
				pref.setGroupingValue(ADM);
				pref.setPartNum(bean.getPartNum());
				pref.setPrefSubject(PNL);
				bean.getPnlPreferences().add(pref);
			}
			if(bean.getPnlHmargin() != null && !bean.getPnlHmargin().isEmpty()){
				pref = new Preference();
				pref.setId(WorkflowEngine.PNL_HORIZONTAL_MARGIN.concat("_").concat(bean.getPartNum()));
				pref.setName(bean.getPnlHmargin());
				pref.setCreatedDate(new Date());
				pref.setCreatorId(bean.getCreatorId());
				pref.setGroupingValue(ADM);
				pref.setPartNum(bean.getPartNum());
				pref.setPrefSubject(PNL);
				bean.getPnlPreferences().add(pref);
			}
			if(bean.getPnlFontType() != null && !bean.getPnlFontType().isEmpty()){
				pref = new Preference();
				pref.setId(WorkflowEngine.PNL_FONT_TYPE.concat("_").concat(bean.getPartNum()));
				pref.setName(bean.getPnlFontType());
				pref.setCreatedDate(new Date());
				pref.setCreatorId(bean.getCreatorId());
				pref.setGroupingValue(ADM);
				pref.setPartNum(bean.getPartNum());
				pref.setPrefSubject(PNL);
				bean.getPnlPreferences().add(pref);
			}
			if(bean.getPnlFontSize() != null && !bean.getPnlFontSize().isEmpty()){
				pref = new Preference();
				pref.setId(WorkflowEngine.PNL_FONT_SIZE.concat("_").concat(bean.getPartNum()));
				pref.setName(bean.getPnlFontSize());
				pref.setCreatedDate(new Date());
				pref.setCreatorId(bean.getCreatorId());
				pref.setGroupingValue(ADM);
				pref.setPartNum(bean.getPartNum());
				pref.setPrefSubject(PNL);
				bean.getPnlPreferences().add(pref);
			}

			getPartDAO().create(bean);
			
			if (textPart != null){
				List<Object> textObjects = new ArrayList<Object>();
				Part text = partDAO.read(textPart.getPartNum());
				SubPart subPart = new SubPart();
				SubPartId id = new SubPartId(bean.getPartNum(), text.getPartNum());
				subPart.setId(id);
				Set<SubPart> topParts = new HashSet<SubPart>();
				topParts.add(subPart);
				text.setTopParts(topParts);
				textObjects.add(text);
				if (filePaths!= null && filePaths.length > 1)
					textObjects.add(filePaths[1]);
				if (!"System_Esprint".equals(textPart.getCreatorId())) {
					NDispatcher.getDispatcher().publish(Topics.topic("cap/events/part/done"), textObjects);
				}
			}
			if (coverPart != null && coverPart.getCreatedDate() != null){
				List<Object> coverObjects = new ArrayList<Object>();
				Part cover = partDAO.read(coverPart.getPartNum());
				SubPart subPart = new SubPart();
				SubPartId id = new SubPartId(bean.getPartNum(), cover.getPartNum());
				subPart.setId(id);
				Set<SubPart> topParts = new HashSet<SubPart>();
				topParts.add(subPart);
				cover.setTopParts(topParts);
				coverObjects.add(cover);
				if (filePaths!= null && filePaths.length > 0)
					coverObjects.add(filePaths[0]);
				if (!"System_Esprint".equals(coverPart.getCreatorId())) {
					NDispatcher.getDispatcher().publish(Topics.topic("cap/events/part/done"), coverObjects);
				}
			}
			if (!bookPC.equals(bean.getCategory())){
				List<Object> bookObjects = new ArrayList<Object>();
				Part book = partDAO.read(bean.getPartNum());
				bookObjects.add(book);
				if (filePaths!= null)
					bookObjects.add(filePaths[0]);
				if (!"System_Esprint".equals(bean.getCreatorId())) {
					NDispatcher.getDispatcher().publish(Topics.topic("cap/events/part/done"), bookObjects);
				}
			}

		} catch (Exception ex) {
			logger.error("Error occurred creating a Part : " + ex.getMessage(), ex);
			throw new PersistenceException(ex);
		}
	}
	
	protected void updatePartJobs(Part part){
		JobSearchBean jsb = new JobSearchBean();
		jsb.setPartNum(part.getPartNum());
		List<Job> partJobs = jobDAO.readAll(jsb);
		for(Job j : partJobs){
			if((part.getColors() != null && !part.getColors().equals(j.getPartColor())) ||
					(part.getPaperType() != null && !part.getPaperType().getId().equals(j.getPartPaperId())) ||
					(part.getTitle() != null && !part.getTitle().equals(j.getPartTitle())) ||	
					(part.getIsbn() != null && !part.getIsbn().equals(j.getPartIsbn())) ||
					(part.getCategory() != null && !part.getCategory().getId().equals(j.getPartCategory()))){
				j.setPartColor(part.getColors());
				if(part.getPaperType() != null) j.setPartPaperId(part.getPaperType().getId());
				j.setPartTitle(part.getTitle());
				j.setPartIsbn(part.getIsbn());
				if(part.getCategory() != null) j.setPartCategory(part.getCategory().getId());
				jobDAO.update(j);
			}
		}
	}

	/**
	 * Calls the corresponding update method on the PartDAO.
	 *
	 */
	@Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.REQUIRED)
	public void update(Part bean) throws PersistenceException {
		try {
			String filePath = null;
			// TODO can we update the criteria at any time? what if jobs already
			// started
			// how if the part is a child part; should just update parent
			// parts?...
			Part oldBean = getPartDAO().read(bean.getPartNum());
			String oldPaperType = null;
			if(oldBean.getPaperType() != null) oldPaperType = oldBean.getPaperType().getId();
			
			if(bean.getLamination() != null && !StringUtils.isEmpty(bean.getLamination().getId())){
				bean.setLamination(lookupDAO.read(bean.getLamination().getId(), Lamination.class));
			}else{
				bean.setLamination(null);
			}
			// calculate the best sheet
			Map<String, Float> bs = printingTimeCalculator.getPartBestSheet(bean);
			bean.setBestSheet(bs.isEmpty() ? null: bs.keySet().iterator().next());
			bean.setBestSheetWaste(bs.isEmpty() ? null: bs.values().iterator().next());
			if (bean.getTopParts().isEmpty()) {
				// update the criterias
				if (!bean.getCritiriasOrigin().isEmpty()) {
					bean.getPartCritirias().clear();
					for (String r : bean.getCritiriasOrigin()) {
						PartCritiria ur = new PartCritiria(bean.getPartNum(), r);
						bean.getPartCritirias().add(ur);
					}
				}
				// set the category based on the criteria and binding type:
				PartCategory bookPC = getLookupDAO().read(Part.PartsCategory.BOOK.getName(), PartCategory.class);
				PartCategory textPC = getLookupDAO().read(Part.PartsCategory.TEXT.getName(), PartCategory.class);
				PartCategory coverPC = getLookupDAO().read(Part.PartsCategory.COVER.getName(), PartCategory.class);
				PartCategory djPC = getLookupDAO().read(Part.PartsCategory.DUSTJACKET.getName(), PartCategory.class);
				PartCategory esPC = getLookupDAO().read(Part.PartsCategory.ENDSHEET.getName(), PartCategory.class);
				
				if (bean.getBindingType() != null
						&& Part.BindingTypes.CARDSS.getName().equals(bean.getBindingType().getId())) {
					
					// remove existing text sub parts
					Set<SubPart> oldSubParts = new HashSet<SubPart>(oldBean.getSubParts());
					//bean.getSubParts().clear();
					for (SubPart sp : oldSubParts) {
						Part p = getPartDAO().read(sp.getId().getSubPartNum());
						if(Part.PartsCategory.TEXT.getName().equals(p.getCategory().getId())){
							delete(p);
							bean.getSubParts().remove(sp);
						}
					}
					
				}
				if (!bean.getCritirias().isEmpty() && bean.getCritirias().contains(SELFCOVER_C)) {
					
					// remove existing cover sub parts
					Set<SubPart> oldSubParts = new HashSet<SubPart>(oldBean.getSubParts());
					//bean.getSubParts().clear();
					for (SubPart sp : oldSubParts) {
						Part p = getPartDAO().read(sp.getId().getSubPartNum());
						if(Part.PartsCategory.COVER.getName().equals(p.getCategory().getId())){
							delete(p);
							bean.getSubParts().remove(sp);
						}
					}
					
				}
				bean.setCategory(bookPC);
				boolean newBean = false;
					// Update/Add the text:
				if (bean.getBindingType() == null || !Part.BindingTypes.CARDSS.getName().equals(bean.getBindingType().getId())) {
					Part textPart = getSubPart(bean, Part.PartsCategory.TEXT.getName());
					newBean = false;
					if (textPart == null) {
						textPart = new Part();
						newBean = true;
					}
					if(!bean.getPartNum().endsWith("T")){
						textPart.setPartNum(bean.getPartNum() + "T");
					}
					if (textPart.getPartNum().length() > 25) {
						textPart.setPartNum(textPart.getPartNum().substring(0, 25));
					}

					textPart.setSoftDelete(bean.getSoftDelete());
					textPart.setActiveFlag(bean.getActiveFlag());
					textPart.setPagesCount(bean.getPagesCount());
					textPart.setColors(bean.getColors());
					textPart.setPaperType(lookupDAO.read(bean.getPaperType().getId(),PaperType.class ));
					textPart.setLength(bean.getLength());
					textPart.setWidth(bean.getWidth());
					textPart.setCategory(textPC);
					textPart.setIsbn(bean.getIsbn());
					textPart.setTitle(bean.getTitle());
					textPart.setBestSheet(bean.getBestSheet());
					textPart.setBestSheetWaste(bean.getBestSheetWaste());
					if (newBean) {
						textPart.setCreatedDate(bean.getCreatedDate());
						textPart.setCreatorId(bean.getCreatorId());
						getPartDAO().create(textPart);
						SubPart subPart1 = new SubPart(bean.getPartNum(), textPart.getPartNum());
						partDAO.createSubPart(subPart1);
						bean.getSubParts().add(subPart1);

						List<Object> textObjects = new ArrayList<Object>();
						textObjects.add(textPart);
						if (!"System_Esprint".equals(textPart.getCreatorId())) {
							NDispatcher.getDispatcher().publish(Topics.topic("cap/events/part/done"), textObjects);
						}
					} else {
						textPart.setLastUpdateDate(bean.getLastModifiedDate());
						textPart.setLastUpdateId(bean.getLastUpdateId());

						// check if the data has changed to trigger the workflow change
						Part oldTextBean = getPartDAO().read(oldBean.getPartNum()+"T");
						if (oldTextBean.getWorkflows().isEmpty())
							getPartDAO().update(textPart);
						else if (oldTextBean.getPartWorkFlowOnProd() != null){
							WFSDataSupport downloadDS = oldTextBean.getDataSupportOnProdByName("Download");
							WFSDataSupport copyDS = oldTextBean.getDataSupportOnProdByName("Copy");
							if (((downloadDS != null) && (copyDS != null) && (!Files
									.getNameWithoutExtension(
											wfsLocationHandler.getLocationByDsId(copyDS.getDataSupportId()).getPath())
									.equals(Files.getNameWithoutExtension(
											wfsLocationHandler.getLocationByDsId(downloadDS.getDataSupportId()).getPath()))))
									|| (oldPaperType != null && !oldPaperType.equals(textPart.getPaperType().getId()))) {
								getPartDAO().update(textPart);

								// set the older workflow along with its data supports to obsolete
								//text workflow
								WFSPartWorkflow oldPartWorkflow = oldTextBean.getPartWorkFlowOnProd();
								if (oldPartWorkflow != null){
									oldPartWorkflow.setWfStatus(WFSProductionStatus.statuses.OBSOLETE.getName());
									oldPartWorkflow.setLastUpdateDate(new Date());
									oldPartWorkflow.setLastUpdateId(oldTextBean.getCreatorId());
									wfsWorkflowHandler.updatePartWorkflow(oldPartWorkflow);
									for(WFSDataSupport dsIter : oldTextBean.getDataSupportsOnProd()){
										if (!dsIter.getName().equalsIgnoreCase("Download")){
											dsIter.setProductionStatus(lookupDAO.read(WFSProductionStatus.statuses.OBSOLETE.getName(),WFSProductionStatus.class ));
											dsIter.setLastUpdateDate(new Date());
											dsIter.setLastUpdateId(oldTextBean.getCreatorId());
											wfsDataSupportHandler.update(dsIter);
										}
									}
								}
								if (!"System_Esprint".equals(textPart.getCreatorId())) {
									NDispatcher.getDispatcher().publish(Topics.topic("cap/events/part/accepted"), textPart);
								}
							} else getPartDAO().update(textPart);
						} else getPartDAO().update(textPart);

					}
					updatePartJobs(textPart);
				}

				newBean = false;
				// Update/Add the cover
				if (bean.getCritirias().isEmpty() || !bean.getCritirias().contains(SELFCOVER_C)) {
					Part coverPart = getSubPart(bean, Part.PartsCategory.COVER.getName());
					if (coverPart == null) {
						coverPart = new Part();
						newBean = true;
					}
					coverPart.setPartNum(bean.getPartNum() + "C");
					if (coverPart.getPartNum().length() > 25) {
						coverPart.setPartNum(coverPart.getPartNum().substring(0, 25));
					}
					coverPart.setSoftDelete(bean.getSoftDelete());
					coverPart.setActiveFlag(bean.getActiveFlag());
					coverPart.setThickness(bean.getThickness());
					coverPart.setCoverColor(bean.getCoverColor());
					coverPart.setPaperType(bean.getPaperType());
					coverPart.setLength(bean.getLength());
					coverPart.setWidth(bean.getWidth());
					coverPart.setLamination(bean.getLamination());
					coverPart.setCategory(coverPC);
					coverPart.setIsbn(bean.getIsbn());
					coverPart.setTitle(bean.getTitle());
					
					if (newBean) {
						// For the case of Loose leaf, only create cover part if
						// there is cover pdf uploaded
						//if (bean.getBindingType() != null && !Part.BindingTypes.LOOSELEAF.getName().equals(bean.getBindingType().getId())
						//		|| (coverPart.getFileName() != null && !coverPart.getFileName().isEmpty())) {
							coverPart.setCreatedDate(bean.getCreatedDate());
							coverPart.setCreatorId(bean.getCreatorId());
							getPartDAO().create(coverPart);
							SubPart subPart2 = new SubPart(bean.getPartNum(), coverPart.getPartNum());
							partDAO.createSubPart(subPart2);
							bean.getSubParts().add(subPart2);

							List<Object> coverObjects = new ArrayList<Object>();
							coverObjects.add(coverPart);
							if (!"System_Esprint".equals(coverPart.getCreatorId())) {
								NDispatcher.getDispatcher().publish(Topics.topic("cap/events/part/done"), coverObjects);
							}
						//}
					} else {
						coverPart.setLastUpdateDate(bean.getLastModifiedDate());
						coverPart.setLastUpdateId(bean.getLastUpdateId());

						// check if the data has changed to trigger the workflow change
						Part oldCoverBean = getPartDAO().read(oldBean.getPartNum()+"C");
						if (oldCoverBean.getWorkflows().isEmpty())
							getPartDAO().update(coverPart);
						else if (oldCoverBean.getPartWorkFlowOnProd() != null){
							WFSDataSupport downloadDS = oldCoverBean.getDataSupportOnProdByName("Download");
							WFSDataSupport copyDS = oldCoverBean.getDataSupportOnProdByName("Copy");
							if (((downloadDS != null) && (copyDS != null) && (!Files
									.getNameWithoutExtension(
											wfsLocationHandler.getLocationByDsId(copyDS.getDataSupportId()).getPath())
									.equals(Files.getNameWithoutExtension(
											wfsLocationHandler.getLocationByDsId(downloadDS.getDataSupportId()).getPath()))))
									|| (oldPaperType != null && !oldPaperType.equals(coverPart.getPaperType().getId()))) {
								getPartDAO().update(coverPart);

								// set the older workflow along with its data supports to obsolete
								//cover workflow
								WFSPartWorkflow oldPartWorkflow = oldCoverBean.getPartWorkFlowOnProd();
								if (oldPartWorkflow != null){
									oldPartWorkflow.setWfStatus(WFSProductionStatus.statuses.OBSOLETE.getName());
									oldPartWorkflow.setLastUpdateDate(new Date());
									oldPartWorkflow.setLastUpdateId(oldCoverBean.getCreatorId());
									wfsWorkflowHandler.updatePartWorkflow(oldPartWorkflow);
									for(WFSDataSupport dsIter : oldCoverBean.getDataSupportsOnProd()){
										if (!dsIter.getName().equalsIgnoreCase("Download")){
											dsIter.setProductionStatus(lookupDAO.read(WFSProductionStatus.statuses.OBSOLETE.getName(),WFSProductionStatus.class ));
											dsIter.setLastUpdateDate(new Date());
											dsIter.setLastUpdateId(oldCoverBean.getCreatorId());
											wfsDataSupportHandler.update(dsIter);
										}
									}
								}
								if (!"System_Esprint".equals(coverPart.getCreatorId())) {
									NDispatcher.getDispatcher().publish(Topics.topic("cap/events/part/accepted"), coverPart);
								}
							} else getPartDAO().update(coverPart);
						} else getPartDAO().update(coverPart);

					}
					// put back the top parts
					// bean.getTopParts().addAll(currentTopParts);
					updatePartJobs(coverPart);
				}
				
				newBean = false;
				// Update/Add the dustJacket
				if (!bean.getCritirias().isEmpty() && bean.getCritirias().contains(DUSTJACKET_C)) {
					Part djPart = getSubPart(bean, Part.PartsCategory.DUSTJACKET.getName());
					if (djPart == null) {
						djPart = new Part();
						newBean = true;
					}
					djPart.setPartNum(bean.getPartNum() + "J");
					if (djPart.getPartNum().length() > 25) {
						djPart.setPartNum(djPart.getPartNum().substring(0, 25));
					}
					djPart.setSoftDelete(bean.getSoftDelete());
					djPart.setActiveFlag(bean.getActiveFlag());

					djPart.setDjColor(bean.getDjColor());
					djPart.setDjPaper(bean.getDjPaper());
					djPart.setCategory(djPC);
					djPart.setIsbn(bean.getIsbn());
					djPart.setTitle(bean.getTitle());
					if (newBean) {
						djPart.setCreatedDate(bean.getCreatedDate());
						djPart.setCreatorId(bean.getCreatorId());
						getPartDAO().create(djPart);
						SubPart subPart2 = new SubPart(bean.getPartNum(), djPart.getPartNum());
						partDAO.createSubPart(subPart2);
						bean.getSubParts().add(subPart2);
					} else {
						djPart.setLastUpdateDate(bean.getLastModifiedDate());
						djPart.setLastUpdateId(bean.getLastUpdateId());
						getPartDAO().update(djPart);
					}
					updatePartJobs(djPart);
				}
				
				newBean = false;
				// Update/Add the endSheet
				if (!bean.getCritirias().isEmpty() && bean.getCritirias().contains(ENDSHEET_C)) {
					Part esPart = getSubPart(bean, Part.PartsCategory.ENDSHEET.getName());
					if (esPart == null) {
						esPart = new Part();
						newBean = true;
					}
					esPart.setPartNum(bean.getPartNum() + "E");
					if (esPart.getPartNum().length() > 25) {
						esPart.setPartNum(esPart.getPartNum().substring(0, 25));
					}
					esPart.setSoftDelete(bean.getSoftDelete());
					esPart.setActiveFlag(bean.getActiveFlag());

					esPart.setEsColor(bean.getEsColor());
					esPart.setEsPaper(bean.getEsPaper());
					esPart.setCategory(esPC);
					esPart.setIsbn(bean.getIsbn());
					esPart.setTitle(bean.getTitle());
					if (newBean) {
						esPart.setCreatedDate(bean.getCreatedDate());
						esPart.setCreatorId(bean.getCreatorId());
						getPartDAO().create(esPart);
						SubPart subPart2 = new SubPart(bean.getPartNum(), esPart.getPartNum());
						partDAO.createSubPart(subPart2);
						bean.getSubParts().add(subPart2);
					} else {
						esPart.setLastUpdateDate(bean.getLastModifiedDate());
						esPart.setLastUpdateId(bean.getLastUpdateId());
						getPartDAO().update(esPart);
					}
					updatePartJobs(esPart);
				}
			}

			// check if the data has changed to trigger the workflow change
			if (bean.getSubParts().isEmpty()) {
				if (oldBean.getPartWorkFlowOnProd() != null) {
					WFSDataSupport downloadDS = oldBean.getDataSupportOnProdByName("Download");
					WFSDataSupport copyDS = oldBean.getDataSupportOnProdByName("Copy");
					if (((downloadDS != null) && (copyDS != null) && (!Files
							.getNameWithoutExtension(
									wfsLocationHandler.getLocationByDsId(copyDS.getDataSupportId()).getPath())
							.equals(Files.getNameWithoutExtension(
									wfsLocationHandler.getLocationByDsId(downloadDS.getDataSupportId()).getPath()))))
							|| (!oldBean.getPaperType().getId().equals(bean.getPaperType().getId()))) {
						getPartDAO().update(bean);

						// set the older workflow along with its data supports to obsolete
						WFSPartWorkflow oldPartWorkflow = oldBean.getPartWorkFlowOnProd();
						if (oldPartWorkflow != null){
							oldPartWorkflow.setWfStatus(WFSProductionStatus.statuses.OBSOLETE.getName());
							oldPartWorkflow.setLastUpdateDate(new Date());
							oldPartWorkflow.setLastUpdateId(oldBean.getCreatorId());
							wfsWorkflowHandler.updatePartWorkflow(oldPartWorkflow);
							for(WFSDataSupport dsIter : oldBean.getDataSupportsOnProd()){
								if (!dsIter.getName().equalsIgnoreCase("Download")){
									dsIter.setProductionStatus(lookupDAO.read(WFSProductionStatus.statuses.OBSOLETE.getName(),WFSProductionStatus.class ));
									dsIter.setLastUpdateDate(new Date());
									dsIter.setLastUpdateId(oldBean.getCreatorId());
									wfsDataSupportHandler.update(dsIter);
								}
							}
						}
						if (!"System_Esprint".equals(bean.getCreatorId())) {
							NDispatcher.getDispatcher().publish(Topics.topic("cap/events/part/accepted"), bean);
						}
					}
				} else
					getPartDAO().update(bean);
			} else
				getPartDAO().update(bean);
			updatePartJobs(bean);
			
			// Update PNL Prefs
			Preference pref = bean.getPnlPreferenceById(WorkflowEngine.PNL_TMPL_ID);
			if(bean.getPnlTemplateId() != null && !bean.getPnlTemplateId().isEmpty()){
				if(pref == null){
					pref = new Preference();
					pref.setId(WorkflowEngine.PNL_TMPL_ID.concat("_").concat(bean.getPartNum()));
					pref.setName(bean.getPnlTemplateId());
					pref.setCreatedDate(new Date());
					pref.setCreatorId(bean.getCreatorId());
					pref.setGroupingValue(ADM);
					pref.setPartNum(bean.getPartNum());
					pref.setPrefSubject(PNL);
					bean.getPnlPreferences().add(pref);
				}else{
					pref.setName(bean.getPnlTemplateId());
				}
			}else if(pref != null){
				//remove the preference
				bean.getPnlPreferences().remove(pref);
			}
			
			pref = bean.getPnlPreferenceById(WorkflowEngine.PNL_PAGE_NUMBER);
			if(bean.getPnlPageNumber() != null && !bean.getPnlPageNumber().isEmpty()){
				if(pref == null){
					pref = new Preference();
					pref.setId(WorkflowEngine.PNL_PAGE_NUMBER.concat("_").concat(bean.getPartNum()));
					pref.setName(bean.getPnlPageNumber());
					pref.setCreatedDate(new Date());
					pref.setCreatorId(bean.getCreatorId());
					pref.setGroupingValue(ADM);
					pref.setPartNum(bean.getPartNum());
					pref.setPrefSubject(PNL);
					bean.getPnlPreferences().add(pref);
				}else{
					pref.setName(bean.getPnlPageNumber());
				}
			}else if(pref != null){
				bean.getPnlPreferences().remove(pref);
			}
			
			pref = bean.getPnlPreferenceById(WorkflowEngine.PNL_PRINTING_NUMBER);
			if(bean.getPnlPrintingNumber() != null && !bean.getPnlPrintingNumber().isEmpty()){
				if(pref == null){
					pref = new Preference();
					pref.setId(WorkflowEngine.PNL_PRINTING_NUMBER.concat("_").concat(bean.getPartNum()));
					pref.setName(bean.getPnlPrintingNumber());
					pref.setCreatedDate(new Date());
					pref.setCreatorId(bean.getCreatorId());
					pref.setGroupingValue(ADM);
					pref.setPartNum(bean.getPartNum());
					pref.setPrefSubject(PNL);
					bean.getPnlPreferences().add(pref);
				}else{
					pref.setName(bean.getPnlPrintingNumber());
				}
			}else if(pref != null){
				bean.getPnlPreferences().remove(pref);
			}
			
			pref = bean.getPnlPreferenceById(WorkflowEngine.PNL_EXCLUDED);
			if(bean.getPnlNotNeeded() != null && !bean.getPnlNotNeeded().isEmpty()){
				if(pref == null){
					pref = new Preference();
					pref.setId(WorkflowEngine.PNL_EXCLUDED.concat("_").concat(bean.getPartNum()));
					pref.setName(bean.getPnlNotNeeded());
					pref.setCreatedDate(new Date());
					pref.setCreatorId(bean.getCreatorId());
					pref.setGroupingValue(ADM);
					pref.setPartNum(bean.getPartNum());
					pref.setPrefSubject(PNL);
					bean.getPnlPreferences().add(pref);
				}else{
					pref.setName(bean.getPnlNotNeeded());
				}
			}else if(pref != null){
				bean.getPnlPreferences().remove(pref);
			}
			
			pref = bean.getPnlPreferenceById(WorkflowEngine.PNL_LOCATION);
			if(bean.getPnlLocation() != null && !bean.getPnlLocation().isEmpty()){
				if(pref == null){
					pref = new Preference();
					pref.setId(WorkflowEngine.PNL_LOCATION.concat("_").concat(bean.getPartNum()));
					pref.setName(bean.getPnlLocation());
					pref.setCreatedDate(new Date());
					pref.setCreatorId(bean.getCreatorId());
					pref.setGroupingValue(ADM);
					pref.setPartNum(bean.getPartNum());
					pref.setPrefSubject(PNL);
					bean.getPnlPreferences().add(pref);
				}else{
					pref.setName(bean.getPnlLocation());
				}
			}else if(pref != null){
				bean.getPnlPreferences().remove(pref);
			}
			
			pref = bean.getPnlPreferenceById(WorkflowEngine.PNL_VERTICAL_MARGIN);
			if(bean.getPnlVmargin() != null && !bean.getPnlVmargin().isEmpty()){
				if(pref == null){
					pref = new Preference();
					pref.setId(WorkflowEngine.PNL_VERTICAL_MARGIN.concat("_").concat(bean.getPartNum()));
					pref.setName(bean.getPnlVmargin());
					pref.setCreatedDate(new Date());
					pref.setCreatorId(bean.getCreatorId());
					pref.setGroupingValue(ADM);
					pref.setPartNum(bean.getPartNum());
					pref.setPrefSubject(PNL);
					bean.getPnlPreferences().add(pref);
				}else{
					pref.setName(bean.getPnlVmargin());
				}
			}else if(pref != null){
				bean.getPnlPreferences().remove(pref);
			}
			
			pref = bean.getPnlPreferenceById(WorkflowEngine.PNL_HORIZONTAL_MARGIN);
			if(bean.getPnlHmargin() != null && !bean.getPnlHmargin().isEmpty()){
				if(pref == null){
					pref = new Preference();
					pref.setId(WorkflowEngine.PNL_HORIZONTAL_MARGIN.concat("_").concat(bean.getPartNum()));
					pref.setName(bean.getPnlHmargin());
					pref.setCreatedDate(new Date());
					pref.setCreatorId(bean.getCreatorId());
					pref.setGroupingValue(ADM);
					pref.setPartNum(bean.getPartNum());
					pref.setPrefSubject(PNL);
					bean.getPnlPreferences().add(pref);
				}else{
					pref.setName(bean.getPnlHmargin());
				}
			}else if(pref != null){
				bean.getPnlPreferences().remove(pref);
			}
			
			pref = bean.getPnlPreferenceById(WorkflowEngine.PNL_FONT_TYPE);
			if(bean.getPnlFontType() != null && !bean.getPnlFontType().isEmpty()){
				if(pref == null){
					pref = new Preference();
					pref.setId(WorkflowEngine.PNL_FONT_TYPE.concat("_").concat(bean.getPartNum()));
					pref.setName(bean.getPnlFontType());
					pref.setCreatedDate(new Date());
					pref.setCreatorId(bean.getCreatorId());
					pref.setGroupingValue(ADM);
					pref.setPartNum(bean.getPartNum());
					pref.setPrefSubject(PNL);
					bean.getPnlPreferences().add(pref);
				}else{
					pref.setName(bean.getPnlFontType());
				}
			}else if(pref != null){
				bean.getPnlPreferences().remove(pref);
			}
			
			pref = bean.getPnlPreferenceById(WorkflowEngine.PNL_FONT_SIZE);
			if(bean.getPnlFontSize() != null && !bean.getPnlFontSize().isEmpty()){
				if(pref == null){
					pref = new Preference();
					pref.setId(WorkflowEngine.PNL_FONT_SIZE.concat("_").concat(bean.getPartNum()));
					pref.setName(bean.getPnlFontSize());
					pref.setCreatedDate(new Date());
					pref.setCreatorId(bean.getCreatorId());
					pref.setGroupingValue(ADM);
					pref.setPartNum(bean.getPartNum());
					pref.setPrefSubject(PNL);
					bean.getPnlPreferences().add(pref);
				}else{
					pref.setName(bean.getPnlFontSize());
				}
			}else if(pref != null){
				bean.getPnlPreferences().remove(pref);
			}
			
			getPartDAO().update(bean);
		} catch (Exception ex) {
			logger.error("Error occurred updating a Part : " + ex.getMessage(), ex);
			throw new PersistenceException(ex);
		}
	}

	/**
	 * finds the sub part of the specified top part, with the specified part
	 * category
	 * 
	 * @throws PersistenceException
	 */
	public Part getSubPart(Part parentPart, String category) throws PersistenceException {
		Part result = null;
		if (!parentPart.getSubParts().isEmpty() && category != null) {
			for (SubPart sp : parentPart.getSubParts()) {
				Part child = this.read(sp.getId().getSubPartNum());
				if (category.equals(child.getCategory().getId())) {
					result = child;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Calls the corresponding delete method on the PartDAO.
	 *
	 */
	@Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.REQUIRED)
	public boolean delete(Part part) throws PersistenceException {
		try {
			OrderSearchBean osb = new OrderSearchBean();
			Set<String> thePart = new HashSet<String>();
			thePart.add(part.getPartNum());
			osb.setPartNumbers(thePart);
			if (orderDAO.readAll(osb).isEmpty()) { // hard delete
				if (part.getTopParts().isEmpty()) {
					if (part.getSubParts().isEmpty()) {
						// delete the pdfs
						// TODO this needs to change ????
						if (part.getFilePath() != null && !part.getFilePath().isEmpty()) {
							File file = new File(part.getFilePath());
							if (file != null && file.exists()) {
								file.delete();
							}
						}
						partDAO.delete(part);
					} else {
						// delete the sub parts in addition to the
						// parent
						Set<String> oldChildren = part.getChildren();
						for (String c : oldChildren) {
							Part child = partDAO.read(c);
							if (child != null) {
								part.getSubParts().remove(part.getSubPartById(c));
								getPartDAO().deleteSubPart(part.getSubPartById(c));
								//partHandler.delete(child);
								child.getTopParts().clear();
								partDAO.update(child);
							}
						}
						getPartDAO().update(part);
						for (String c : oldChildren) {
							Part child = partDAO.read(c);
							if (child != null) {
								// delete the pdf
								// TODO this needs to change ????
								if (part.getFilePath() != null && !part.getFilePath().isEmpty()) {
									File file = new File(part.getFilePath());
									if (file != null && file.exists()) {
										file.delete();
									}
								}
								partDAO.delete(child);
							}
						}
						//delete part criteria
						for(PartCritiria pc : part.getPartCritirias()){
							partDAO.deletePC(pc);
						}
						part.getPartCritirias().clear();
						partDAO.update(part);
						// delete the pdf
						// TODO this needs to change ????
						if (part.getFilePath() != null && !part.getFilePath().isEmpty()) {
							File file = new File(part.getFilePath());
							if (file != null && file.exists()) {
								file.delete();
							}
						}
						partDAO.delete(part);
					}
				} else {
					// delete the child and update the parent
					Part parentPart = partDAO
							.read(part.getTopParts().iterator().next().getId().getTopPartNum());
					for (String c : parentPart.getChildren()) {
						if (c.equals(part.getPartNum())){
							parentPart.getSubParts().remove(parentPart.getSubPartById(c));
							getPartDAO().deleteSubPart(parentPart.getSubPartById(c));
							//partHandler.delete(child);
							part.getTopParts().clear();
							partDAO.update(part);
						}
					}
					partDAO.update(parentPart);
					partDAO.delete(part);
				}
			} else { // soft delete
				if (part.getTopParts().isEmpty()) {
					if (part.getSubParts().isEmpty()) {
						part.setActiveFlag(false);
						partDAO.update(part);
					} else {
						// delete the sub parts in addition to the
						// parent
						for (String c : part.getChildren()) {
							Part child = partDAO.read(c);
							if (child != null) {
								child.setActiveFlag(false);
								partDAO.update(child);
							}
						}
						part.setActiveFlag(false);
						partDAO.update(part);
					}
				} else {
					part.setActiveFlag(false);
					partDAO.update(part);
				}
			}
			return true;
		} catch (Exception ex) {
			logger.error("Error occurred deleting a Part with id '" + (part == null ? null : part.getPartNum()) + "' : "
					+ ex.getMessage(), ex);
			throw new PersistenceException(ex);
		}
	}

	/**
	 * A convenience method which calls readAll(PartSearchBean) with a null
	 * search bean.
	 *
	 * @see #readAll(PartSearchBean)
	 */
	@Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
	public List<Part> readAll() throws PersistenceException {
		return this.readAll(null);
	}

	/**
	 * Calls the corresponding readAll method on the PartDAO.
	 *
	 */
	@Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
	public List<Part> readAll(PartSearchBean searchBean) throws PersistenceException {
		try {
			return getPartDAO().readAll(searchBean);
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a list of Parts : " + ex.getMessage(), ex);
			throw new PersistenceException(ex);
		}
	}

	/**
	 * Calls the corresponding read method on the PartDAO.
	 *
	 */
	@Transactional(rollbackFor = PersistenceException.class, readOnly = true, propagation = Propagation.SUPPORTS)
	public Part read(String partNum) throws PersistenceException {
		try {
			Part part = getPartDAO().read(partNum);
			Preference pref = null;
			if(part != null){
				pref = part.getPnlPreferenceById(WorkflowEngine.PNL_TMPL_ID);
				if(pref != null){
					part.setPnlTemplateId(pref.getName());
				}
				pref = part.getPnlPreferenceById(WorkflowEngine.PNL_PAGE_NUMBER);
				if(pref != null){
					part.setPnlPageNumber(pref.getName());
				}
				pref = part.getPnlPreferenceById(WorkflowEngine.PNL_PRINTING_NUMBER);
				if(pref != null){
					part.setPnlPrintingNumber(pref.getName());
				}
				pref = part.getPnlPreferenceById(WorkflowEngine.PNL_EXCLUDED);
				if(pref != null){
					part.setPnlNotNeeded(pref.getName());
				}
				pref = part.getPnlPreferenceById(WorkflowEngine.PNL_LOCATION);
				if(pref != null){
					part.setPnlLocation(pref.getName());
				}
				pref = part.getPnlPreferenceById(WorkflowEngine.PNL_HORIZONTAL_MARGIN);
				if(pref != null){
					part.setPnlHmargin(pref.getName());
				}
				pref = part.getPnlPreferenceById(WorkflowEngine.PNL_VERTICAL_MARGIN);
				if(pref != null){
					part.setPnlVmargin(pref.getName());
				}
				pref = part.getPnlPreferenceById(WorkflowEngine.PNL_FONT_TYPE);
				if(pref != null){
					part.setPnlFontType(pref.getName());
				}
				pref = part.getPnlPreferenceById(WorkflowEngine.PNL_FONT_SIZE);
				if(pref != null){
					part.setPnlFontSize(pref.getName());
				}
			}
			return part;
		} catch (Exception ex) {
			logger.error("Error occurred retrieving a Part with id '" + partNum + "' : " + ex.getMessage(), ex);
			throw new PersistenceException(ex);
		}
	}

	/**
	 * @return the PartDAO
	 */
	public PartDAO getPartDAO() {
		return partDAO;
	}

	/**
	 * @param dao
	 *            the PartDAO to set
	 */
	public void setPartDAO(PartDAO dao) {
		this.partDAO = dao;
	}

	/**
	 * @return the lookupDAO
	 */
	public LookupDAO getLookupDAO() {
		return lookupDAO;
	}

	/**
	 * @param lookupDAO
	 *            the lookupDAO to set
	 */
	public void setLookupDAO(LookupDAO lookupDAO) {
		this.lookupDAO = lookupDAO;
	}

	private String[] pathSplitter(String paths) {

		String[] filePaths = new String[2];
		if (paths != null && !paths.isEmpty()) {
			filePaths = paths.split(";");
		}
		return filePaths;
	}

	public List<String> getAllIsbn(){
		return getPartDAO().allIsbn();
	}

	@Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.REQUIRED)
	public Part readByIsbn(String isbn){
		return partDAO.readByIsbn(isbn);
	}
	@Transactional(rollbackFor = PersistenceException.class, propagation = Propagation.REQUIRED)
	public List<Part> readDistinctIsbn() throws PersistenceException{
		return partDAO.fetchDistinctIsbn();
	}

	public Map<String,Object> checkPdf(File tmpFile,Part part,String partCategory){
		PdfReader reader;
		Map<String,Object> resultMap = new HashMap<>();
		try {
			unitValue = lookupHandler.read("UNITSYSTEM", Preference.class);
			LogUtils.debug("Start checking the pdf uploaded");
			Boolean check = false;
			float spineValue  = 0;
			Boolean checkTrim = false;
			Boolean checkNubPage = false;
			Boolean checkSpine = false;

			reader = new PdfReader(tmpFile.getPath());
			Rectangle trim = reader.getBoxSize(1, "trim");
			float partWidth = part.getWidth();
			float partHeigth = part.getLength();
			if(UNIT_US.equals(unitValue.getName())){
				partWidth  = Format.inch2mm(partWidth);
				partHeigth = Format.inch2mm(partHeigth);
			}
			float trimWidth = Format.points2mm(trim.getWidth()).floatValue();
			float trimHeigth = Format.points2mm(trim.getHeight()).floatValue();
			int nubmerOfPage  = reader.getNumberOfPages();

			if(Part.PartsCategory.TEXT.getName().equalsIgnoreCase(partCategory)){
				if(Math.abs(trimWidth - partWidth) <= 0.5 && Math.abs(trimHeigth - partHeigth) <= 0.5){
					checkTrim = true;
					check = checkTrim;
				}
				resultMap.put("NumberOfPage", nubmerOfPage);

				if(nubmerOfPage > 2){
					checkNubPage = true;
					check = check && checkNubPage;
				}
			}else if(Part.PartsCategory.COVER.getName().equalsIgnoreCase(partCategory)){
				if(nubmerOfPage <= 2){
					checkNubPage = true;
					check = checkNubPage;
				}
				Part textPart = getSubPart(part, Part.PartsCategory.TEXT.getName());

				WFSDataSupport ds = textPart.getDataSupportOnProdByName(WFSDataSupport.NAME_DOWNLOAD);
				if(ds != null){
					WFSLocation location = ds.getLocationdByType(WFSLocation.DESTINATION);
					if(location != null){
						String  fileTextPath = location.getPath();
						PdfReader reader2 = new PdfReader(fileTextPath);
						Rectangle trimText = reader2.getBoxSize(1, "trim");
						float trimTextWidth = Format.points2mm(trimText.getWidth()).floatValue();
						spineValue = trimWidth - (2*trimTextWidth);
						/*if(Math.abs(spineValue - spineWidth) >= 1.5){
						checkSpine = true;
						check = check && checkSpine;
					}*/if(UNIT_US.equals(unitValue.getName())){
						spineValue = Format.mm2inch(spineValue).floatValue();
					}
						
						resultMap.put("Spine", spineValue);

						
						reader2.close();

					}

				}

			}
			resultMap.put("Trim", check);

			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultMap;
	}
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public Integer getCount() {
		return partDAO.getCount();
	}
	
	
	@Transactional(rollbackFor=PersistenceException.class, readOnly=true, propagation=Propagation.SUPPORTS)
	public List<Part> fullSearch(String word, Integer maxResult, Integer offset) {	
		return partDAO.fullSearch(word, maxResult, offset);		
	}

}