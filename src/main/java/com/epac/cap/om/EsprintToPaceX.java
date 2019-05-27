package com.epac.cap.om;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.functionel.ImpositionResponse;
import com.epac.cap.functionel.Rip;
import com.epac.cap.handler.LookupHandler;
import com.epac.cap.handler.OrderHandler;
import com.epac.cap.model.BindingType;
import com.epac.cap.model.Client;
import com.epac.cap.model.Lamination;
import com.epac.cap.model.LookupItem;
import com.epac.cap.model.Order;
import com.epac.cap.model.OrderPart;
import com.epac.cap.model.PaperType;
import com.epac.cap.model.Part;
import com.epac.cap.model.Part.PartsCategory;
import com.epac.cap.model.PartCategory;
import com.epac.cap.model.PartCritiria;
import com.epac.cap.model.Preference;
import com.epac.cap.model.Priority.Priorities;
import com.epac.cap.model.WFSDataSupport;
import com.epac.cap.model.WFSLocation;
import com.epac.cap.model.WFSPartWorkflow;
import com.epac.cap.model.WFSProductionStatus;
import com.epac.cap.model.WFSProgress;
import com.epac.cap.model.WFSStatus.ProgressStatus;
import com.epac.cap.utils.Mapper;
import com.epac.om.api.book.Book;
import com.epac.om.api.book.BookStatus;
import com.epac.om.api.book.CoverFinishType;
import com.epac.om.api.book.Metadata;
import com.epac.om.api.order.Package;
import com.epac.om.api.order.PackageBook;
import com.epac.om.api.production.Job;
import com.epac.om.api.production.ProductionOrder;
import com.epac.om.api.utils.LogUtils;
import com.google.common.io.Files;

@Component
public class EsprintToPaceX {

	private Mapper mapper;

	@Autowired
	private OrderHandler orderHandler;
	
	@Autowired
	private ProductionClient productionClient;
	
	@Autowired
	private LookupHandler lookupHandler;

	@Autowired
	private Map<Class<?>, List<? extends LookupItem>> lookupsList;
	
	private String filename;
	
	public  Order productionOrderToOrder(ProductionOrder productionOrder, String token) throws PersistenceException {
		
		Order order = new Order();
		order.setNotes("");
		order.setOrderNum(productionOrder.getOrder());
		order.setStatus(com.epac.cap.model.Order.OrderStatus.PENDING.getName());
		order.setDueDate(productionOrder.getDeliverBefore());
		order.setPriority(Priorities.NORMAL.getName());
		order.setRecievedDate(new Date(productionOrder.getTimestamp()));
		order.setSource(Order.OrderSources.ESPRINT.toString());
		order.setCreatorId("System_Esprint");
		
		String siren = productionOrder.getDistributer().getSiren();
		order.setClientId(siren);	
		
		Client client = lookupHandler.read(siren, Client.class);
		if(client == null) {
			Client newClient = new Client();
			newClient.setId(siren);
			newClient.setName("undefined");
			newClient.setCreatedDate(new Date());
			newClient.setCreatorId("System_Esprint");
			lookupHandler.create(newClient);
		}
		Set<OrderPart> orderParts = new HashSet<OrderPart>();
		// Take into account duplicates by job id in the jobs list...
		Set<Long> jobsIds = new HashSet<Long>();
		for (Job job : productionOrder.getJobs()) {
			OrderPart orderPart;
			try {
				if(!jobsIds.contains(job.getId())){
					orderPart = jobToOrderPart(job, order, token);
					orderParts.add(orderPart);
					jobsIds.add(job.getId());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		order.setOrderParts(orderParts);
		order.setOrderPart(orderParts.iterator().next());

		Set<com.epac.cap.model.Package> packages = new HashSet<com.epac.cap.model.Package>();
		if(productionOrder.getPackages() != null){
			for (Package pakage : productionOrder.getPackages()) {
				com.epac.cap.model.Package pack = packageToOrderPackage(pakage, order);
				packages.add(pack);
			}
			order.setOrderPackages(packages);
		}
		
		try {
			createOrderService(order);
		} catch (Exception e) {
			LogUtils.error("Error occured while sending order to PaceX", e);
		}
		return order;
	}

	public OrderPart jobToOrderPart(Job job, Order order, String token) throws Exception {

		OrderPart orderPart = new OrderPart();
		Book book = job.getBook();
		if(book.getMetadata().getTextPaperType() == null) {
			book = productionClient.fetchBook(book.getBookId(), token);
		}
		Map<String, Map<String, Object>> files = getBook(job.getBook().getBookId(), token);

		Part part = bookToPart(book, files, order);

		orderPart.setPart(part);
		orderPart.setQuantity(job.getRequested());

		return orderPart;
	}

	public Map<String, Map<String, Object>> getBook(String bookId, String token) {

		Map<String, Map<String, Object>> book = productionClient.getBook(bookId, token);
		return book;
	}

	public Part bookToPart(Book book, Map<String, Map<String, Object>> files, Order order) throws Exception {

		Part part = new Part();
		part.setPartNum(book.getBookId());
		part.setFileName(book.getBookId().concat(".text.pdf"));
		System.out.println(book.getBookId());
		System.out.println(book.getStatus());
		part.setReadyToProduce((book.getStatus().equals(BookStatus.READY)) ? true : false);
		part.setCreatedDate(new Date());
		part.setCreatorId("System_Esprint");
		part.setCategory(getLookUp(PartsCategory.BOOK.getName(), PartCategory.class));
		part.setColors("1C");
		// Fill the part with Data from the Metadata of the Book
		Metadata metadata = book.getMetadata();

		part.setPagesCount(metadata.getTextPageCount());
		part.setWidth((float) metadata.getWidth());
		part.setLength((float) metadata.getHeight());
		part.setSize("" + metadata.getTextPDFFileSize());
		part.setThickness((float) metadata.getThickness());
		part.setIsbn(metadata.getBarcode());
		part.setTitle(metadata.getTitle());
		part.setAuthor(metadata.getAuthor());
		part.setVersion((metadata.getEdition() != null) ? Integer.parseInt(metadata.getEdition()) : 0);
		part.setPublishDate(metadata.getPublishDate());
		part.setPaperType(getLookUp(metadata.getTextPaperType().getCode(), PaperType.class));
		part.setActiveFlag(true);
		part.setReadyToProduce(false);
		part.setPublisher(metadata.getPublisher());
		BindingType bindingType = getLookUp("Perfect Bind", BindingType.class);
		part.setBindingType(bindingType);

		Lamination lamination = pacexToEsprintLamination(metadata.getCoverFinishType());
		part.setLamination(lamination);
		String sv = "SPOTVARNISH";
		Preference p = lookupHandler.read(sv, Preference.class);
		if(p != null && "true".equals(p.getName()) && metadata.getVarnish()){
			Set<String> tmp = new HashSet<String>();
			tmp.add(sv);
			part.setCritirias(tmp);
		}
		
		SortedSet<WFSDataSupport> dataSupport = bookSupportToDataSupport(files, part, order, book.getBookId());
		part.setDataSupports(dataSupport);

		WFSPartWorkflow pwf = new WFSPartWorkflow();
		pwf.setWfStatus(WFSProductionStatus.statuses.ONPROD.getName());
		pwf.setCreatedDate(new Date());
		pwf.setPartNum(part.getPartNum());
		pwf.setIsReady(true);

		WFSProgress progress = new WFSProgress();

		progress.setCreatedDate(new Date());
		progress.setStatus(ProgressStatus.DONE.getName());
		progress.setCreatorId(part.getCreatorId());
		progress.setPartWorkflow(pwf);

		pwf.addProgress(progress);

		part.addWorkflow(pwf);

		return part;
	}

	public <V extends Comparable<V>> TreeSet<V> convertToSortedSet(List<V> list) {
		return new TreeSet<>(list);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortedSet<WFSDataSupport> bookSupportToDataSupport(Map<String, Map<String, Object>> files,
			Part part, Order order, String bookId) {
		SortedSet<WFSDataSupport> sortedDataSupports = new TreeSet<WFSDataSupport>(new Comparator<WFSDataSupport>() {
			@Override
			public int compare(WFSDataSupport o1, WFSDataSupport o2) {
				// a comparator to avoid comparing ds based on null
				// datasupportId attribute
				return 1;
			}
		});
		// Map<String, Object> fyles = files.get("files");
		// Get cover/text files
		Map<String, Object> text = null;
		Map<String, Object> cover = null;
		if(files != null){
			text = (Map<String, Object>) files.get(Book.TEXT);
			cover = (Map<String, Object>) files.get(Book.COVER);
		}
		
		if (text == null || cover == null)
			return sortedDataSupports;

		boolean checkRasterSM = true;
		boolean checkRasterEM = true;
		Preference p;
		try {
			p = lookupHandler.read("CHECKRASTERSM", Preference.class);
			if(p != null && "false".equals(p.getName())){
				checkRasterSM = false;
			}
			p = lookupHandler.read("CHECKRASTEREM", Preference.class);
			if(p != null && "false".equals(p.getName())){
				checkRasterEM = false;
			}
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (text.get(Book.ORIGINAL) != null) {
			LogUtils.debug(((Map) ((ArrayList<Object>) text.get(Book.ORIGINAL)).get(0)).get("file").toString());
			 filename = (String) ((Map) ((ArrayList<Object>) text.get(Book.ORIGINAL)).get(0)).get("file");
			String filepath = System.getProperty(ConfigurationConstants.ESPRINT_DOWNLOAD_DIR).concat(bookId)
					.concat("/").concat(filename);

			WFSDataSupport originalTextDS = new WFSDataSupport();
			originalTextDS.setDsType("Download Text");
			originalTextDS.setName(WFSDataSupport.NAME_DOWNLOAD);
			originalTextDS.setPartNumb(part.getPartNum());
			originalTextDS.setCreatedDate(new Date());
			originalTextDS.setCreatorId(part.getCreatorId());
			originalTextDS.setDescription("Text");
			originalTextDS.setProductionStatus(
					getLookUp(WFSProductionStatus.statuses.ONPROD.toString(), WFSProductionStatus.class));

			WFSLocation originalTextLocation = new WFSLocation();
			originalTextLocation.setCreatedDate(new Date());
			originalTextLocation.setCreatorId(part.getCreatorId());
			originalTextLocation.setPath(filepath);
			originalTextLocation.setFileName(filename);
			originalTextLocation.setLocationType("Destination");
			originalTextDS.addLocation(originalTextLocation);
			sortedDataSupports.add(originalTextDS);
		}else{
			order.setNotes(order.getNotes().concat("No Original files available for part " + part.getPartNum()));
			order.setStatus(com.epac.cap.model.Order.OrderStatus.ERROR.getName());
		}
		if (text.get(Book.PROOFED) != null) {
			 filename = (String) ((Map) ((ArrayList<Object>) text.get(Book.PROOFED)).get(0)).get("file");
			String filepath = System.getProperty(ConfigurationConstants.ESPRINT_PROOFING_DIR).concat(bookId)
					.concat("/").concat(filename);
			WFSDataSupport proofedTextDS = new WFSDataSupport();
			proofedTextDS.setDsType("Proofed");
			proofedTextDS.setName("Proofed Text");
			proofedTextDS.setPartNumb(part.getPartNum());
			proofedTextDS.setCreatedDate(new Date());
			proofedTextDS.setCreatorId(part.getCreatorId());
			proofedTextDS.setDescription("Text");
			proofedTextDS.setProductionStatus(
					getLookUp(WFSProductionStatus.statuses.ONPROD.toString(), WFSProductionStatus.class));

			WFSLocation proofedTextLocation = new WFSLocation();
			proofedTextLocation.setCreatedDate(new Date());
			proofedTextLocation.setCreatorId(part.getCreatorId());
			proofedTextLocation.setPath(filepath);
			proofedTextLocation.setFileName(filename);
			proofedTextLocation.setLocationType("Destination");

			proofedTextDS.addLocation(proofedTextLocation);
			sortedDataSupports.add(proofedTextDS);
		}

		// Get Original, imposed and proofed cover
		if (cover.get(Book.ORIGINAL) != null) {
			 filename = (String) ((Map) ((ArrayList<Object>) cover.get(Book.ORIGINAL)).get(0)).get("file");
			String filepath = System.getProperty(ConfigurationConstants.ESPRINT_DOWNLOAD_DIR).concat(bookId)
					.concat("/").concat(filename);

			WFSDataSupport originalCoverDS = new WFSDataSupport();
			originalCoverDS.setDsType("Download Cover");
			originalCoverDS.setName(WFSDataSupport.NAME_DOWNLOAD);
			originalCoverDS.setPartNumb(part.getPartNum());
			originalCoverDS.setCreatedDate(new Date());
			originalCoverDS.setCreatorId(part.getCreatorId());
			originalCoverDS.setDescription("Cover");
			originalCoverDS.setProductionStatus(
					getLookUp(WFSProductionStatus.statuses.ONPROD.toString(), WFSProductionStatus.class));

			WFSLocation originalCoverLocation = new WFSLocation();
			originalCoverLocation.setCreatedDate(new Date());
			originalCoverLocation.setCreatorId(part.getCreatorId());
			originalCoverLocation.setPath(filepath);
			originalCoverLocation.setFileName(filename);
			originalCoverLocation.setLocationType("Destination");

			originalCoverDS.addLocation(originalCoverLocation);
			sortedDataSupports.add(originalCoverDS);
		}

		if (cover.get(Book.IMPOSED) != null) {
			
			 filename = (String) ((Map) ((ArrayList<Object>) cover.get(Book.IMPOSED)).get(0)).get("file");
			String filepath = System.getProperty(ConfigurationConstants.ESPRINT_IMPOSED_DIR).concat(bookId)
					.concat("/").concat(filename);

			WFSDataSupport imposedCoverDS = new WFSDataSupport();
			imposedCoverDS.setDsType("cover");
			imposedCoverDS.setName(WFSDataSupport.NAME_IMPOSE);
			imposedCoverDS.setPartNumb(part.getPartNum());
			imposedCoverDS.setCreatedDate(new Date());
			imposedCoverDS.setCreatorId(part.getCreatorId());
			imposedCoverDS.setDescription("COVER");
			imposedCoverDS.setProductionStatus(
					getLookUp(WFSProductionStatus.statuses.ONPROD.toString(), WFSProductionStatus.class));

			WFSLocation imposedCoverLocation = new WFSLocation();
			imposedCoverLocation.setCreatedDate(new Date());
			imposedCoverLocation.setCreatorId(part.getCreatorId());
			imposedCoverLocation.setPath(filepath);
			imposedCoverLocation.setFileName(filename);
			imposedCoverLocation.setLocationType("Destination");

			imposedCoverDS.addLocation(imposedCoverLocation);
			sortedDataSupports.add(imposedCoverDS);
		}

		if (cover.get(Book.PROOFED) != null) {
			 filename = (String) ((Map) ((ArrayList<Object>) cover.get(Book.PROOFED)).get(0)).get("file");
			String filepath = System.getProperty(ConfigurationConstants.ESPRINT_PROOFING_DIR).concat(bookId)
					.concat("/").concat(filename);

			WFSDataSupport proofedCoverDS = new WFSDataSupport();
			proofedCoverDS.setDsType("Proofed");
			proofedCoverDS.setName("Proofed Cover");
			proofedCoverDS.setPartNumb(part.getPartNum());
			proofedCoverDS.setCreatedDate(new Date());
			proofedCoverDS.setCreatorId(part.getCreatorId());
			proofedCoverDS.setDescription("Cover");
			proofedCoverDS.setProductionStatus(
					getLookUp(WFSProductionStatus.statuses.ONPROD.toString(), WFSProductionStatus.class));

			WFSLocation proofedCoverLocation = new WFSLocation();
			proofedCoverLocation.setCreatedDate(new Date());
			proofedCoverLocation.setCreatorId(part.getCreatorId());
			proofedCoverLocation.setPath(filepath);
			proofedCoverLocation.setFileName(filename);
			proofedCoverLocation.setLocationType("Destination");

			proofedCoverDS.addLocation(proofedCoverLocation);
			sortedDataSupports.add(proofedCoverDS);

		}

		ArrayList<Object> imposedFFEpacMode = null;
		ArrayList<Object> imposedFFStndrdMode = null;  
		ArrayList<Object> imposedPFEpacMode = null;
		ArrayList<Object> imposedPFStndrdMode = null;
		
		if (text.get(Book.IMPOSED) != null) {
			Map<String, Object> imposed = (Map<String, Object>) text.get(Book.IMPOSED);
			// Get imposed text for FlyFolder
			Map<String, Object> imposedFF = (Map<String, Object>) imposed.get(Book.FF_HUNKELER);
			// Get imposed text for FlyFolder in EPAC mode
			if(imposedFF != null){
				imposedFFEpacMode = (ArrayList<Object>) imposedFF.get(Book.EPAC_MODE);
			}
			
			String pdfFilename = "", jdfFilename = "", pdfFilePath = "", jdfFilePath = "";
			// FlyFolder / Epac Mode
			if(imposedFFEpacMode != null){
				for(Object obj : imposedFFEpacMode){
					pdfFilename = (String) ((Map) (obj)).get("file");
					pdfFilePath = System.getProperty(ConfigurationConstants.ESPRINT_IMPOSED_DIR)
							.concat(part.getPartNum()).concat("/").concat(pdfFilename);
					
					WFSDataSupport imposedFFEpacModeDS = new WFSDataSupport();
					if(!StringUtils.isBlank(pdfFilename) && "pdf".equals(Files.getFileExtension(pdfFilename))){
						imposedFFEpacModeDS.setDsType(ImpositionResponse.FFEM_PDF);
						
					}else{
						imposedFFEpacModeDS.setDsType(ImpositionResponse.FFEM_JDF);
					}
					imposedFFEpacModeDS.setName(WFSDataSupport.NAME_IMPOSE);
					imposedFFEpacModeDS.setPartNumb(part.getPartNum());
					imposedFFEpacModeDS.setDescription("Text");
					imposedFFEpacModeDS.setCreatedDate(new Date());
					imposedFFEpacModeDS.setCreatorId(part.getCreatorId());
					imposedFFEpacModeDS.setProductionStatus(
							getLookUp(WFSProductionStatus.statuses.ONPROD.toString(), WFSProductionStatus.class));
					
					WFSLocation imposedFFEpacModeLocation = new WFSLocation();
					imposedFFEpacModeLocation.setCreatedDate(new Date());
					imposedFFEpacModeLocation.setCreatorId(part.getCreatorId());
					imposedFFEpacModeLocation.setPath(pdfFilePath);
					imposedFFEpacModeLocation.setFileName(pdfFilename);
					imposedFFEpacModeLocation.setLocationType("Destination");
		
					imposedFFEpacModeDS.addLocation(imposedFFEpacModeLocation);
					sortedDataSupports.add(imposedFFEpacModeDS);
		
				}
			}
			// FlyFolder / Standard mode
			if(imposedFF != null){
				imposedFFStndrdMode = (ArrayList<Object>) imposedFF.get(Book.STND_MODE);
			}
			if(imposedFFStndrdMode != null){
				pdfFilename = (String) ((Map) (imposedFFStndrdMode.get(0))).get("file");
				pdfFilePath = System.getProperty(ConfigurationConstants.ESPRINT_IMPOSED_DIR).concat(bookId)
						.concat("/").concat(pdfFilename);

				jdfFilename = (String) ((Map) (imposedFFStndrdMode.get(1))).get("file");
				jdfFilePath = System.getProperty(ConfigurationConstants.ESPRINT_IMPOSED_DIR).concat(bookId)
						.concat("/").concat(jdfFilename);
	
				WFSDataSupport imposedFFStndrdModeDS = new WFSDataSupport();
				imposedFFStndrdModeDS.setDsType(ImpositionResponse.FFSM_PDF);
				imposedFFStndrdModeDS.setName(WFSDataSupport.NAME_IMPOSE);
				imposedFFStndrdModeDS.setDescription("Text");
				imposedFFStndrdModeDS.setPartNumb(part.getPartNum());
				imposedFFStndrdModeDS.setCreatedDate(new Date());
				imposedFFStndrdModeDS.setCreatorId(part.getCreatorId());
				imposedFFStndrdModeDS.setProductionStatus(
						getLookUp(WFSProductionStatus.statuses.ONPROD.toString(), WFSProductionStatus.class));
	
				WFSLocation imposedFFStndrdModeLocation = new WFSLocation();
				imposedFFStndrdModeLocation.setCreatedDate(new Date());
				imposedFFStndrdModeLocation.setCreatorId(part.getCreatorId());
				imposedFFStndrdModeLocation.setPath(pdfFilePath);
				imposedFFStndrdModeLocation.setFileName(pdfFilename);
				imposedFFStndrdModeLocation.setLocationType("Destination");
	
				imposedFFStndrdModeDS.addLocation(imposedFFStndrdModeLocation);
				sortedDataSupports.add(imposedFFStndrdModeDS);
	
				WFSDataSupport jdfFFStndrdModeDS = new WFSDataSupport();
				jdfFFStndrdModeDS.setDsType(ImpositionResponse.FFSM_JDF);
				jdfFFStndrdModeDS.setPartNumb(part.getPartNum());
				jdfFFStndrdModeDS.setCreatedDate(new Date());
				jdfFFStndrdModeDS.setCreatorId(part.getCreatorId());
				jdfFFStndrdModeDS.setDescription("Text");
				jdfFFStndrdModeDS.setProductionStatus(
						getLookUp(WFSProductionStatus.statuses.ONPROD.toString(), WFSProductionStatus.class));
				jdfFFStndrdModeDS.setName(WFSDataSupport.NAME_IMPOSE);
	
				WFSLocation jdfFFStndrdModeLocation = new WFSLocation();
				jdfFFStndrdModeLocation.setCreatedDate(new Date());
				jdfFFStndrdModeLocation.setCreatorId(part.getCreatorId());
				jdfFFStndrdModeLocation.setPath(jdfFilePath);
				jdfFFStndrdModeLocation.setFileName(jdfFilename);
				jdfFFStndrdModeLocation.setLocationType("Destination");
	
				jdfFFStndrdModeDS.addLocation(jdfFFStndrdModeLocation);
				sortedDataSupports.add(jdfFFStndrdModeDS);
			}
			// Get imposed text for PlowFolder
			Map<String, Object> imposedPF = (Map<String, Object>) imposed.get(Book.PF_HUNKELER);
			if(imposedPF != null){
				imposedPFEpacMode = (ArrayList<Object>) imposedPF.get(Book.EPAC_MODE);
			}
			// Plow Folder / Epac Mode
			if(imposedPFEpacMode != null){
				for(Object obj : imposedPFEpacMode){
					pdfFilename = (String) ((Map) (obj)).get("file");
					pdfFilePath = System.getProperty(ConfigurationConstants.ESPRINT_IMPOSED_DIR).concat(bookId)
							.concat("/").concat(pdfFilename);
		
					//jdfFilename = (String) ((Map) (imposedPFEpacMode.get(1))).get("file");
					//jdfFilePath = System.getProperty(ConfigurationConstants.ESPRINT_IMPOSED_DIR).concat(bookId)
							//.concat("/").concat(jdfFilename);
					
					WFSDataSupport imposedPFEpacModeDS = new WFSDataSupport();
					if(!StringUtils.isBlank(pdfFilename) && "pdf".equals(Files.getFileExtension(pdfFilename))){
						imposedPFEpacModeDS.setDsType(ImpositionResponse.PFEM_PDF);
					}else{
						imposedPFEpacModeDS.setDsType(ImpositionResponse.PFEM_JDF);
					}
					imposedPFEpacModeDS.setPartNumb(part.getPartNum());
					imposedPFEpacModeDS.setDescription("Text");
					imposedPFEpacModeDS.setName(WFSDataSupport.NAME_IMPOSE);
					imposedPFEpacModeDS.setProductionStatus(
							getLookUp(WFSProductionStatus.statuses.ONPROD.toString(), WFSProductionStatus.class));
					imposedPFEpacModeDS.setCreatedDate(new Date());
					imposedPFEpacModeDS.setCreatorId(part.getCreatorId());
					
		
					WFSLocation imposedPFEpacModeLocation = new WFSLocation();
					imposedPFEpacModeLocation.setCreatedDate(new Date());
					imposedPFEpacModeLocation.setCreatorId(part.getCreatorId());
					imposedPFEpacModeLocation.setPath(pdfFilePath);
					imposedPFEpacModeLocation.setFileName(pdfFilename);
					imposedPFEpacModeLocation.setLocationType("Destination");
					imposedPFEpacModeDS.addLocation(imposedPFEpacModeLocation);
					
					sortedDataSupports.add(imposedPFEpacModeDS);
				}
			}
			
			// Get imposed text for PlowFolder in Standard Mode
			if(imposedPF != null){
				imposedPFStndrdMode = (ArrayList<Object>) imposedPF.get(Book.STND_MODE);
			}
			if(imposedPFStndrdMode != null){
				// Plow Folder / Standard Mode
				pdfFilename = (String) ((Map) (imposedPFStndrdMode.get(0))).get("file");
				pdfFilePath = System.getProperty(ConfigurationConstants.ESPRINT_IMPOSED_DIR).concat(bookId)
					.concat("/").concat(pdfFilename);

				jdfFilename = (String) ((Map) (imposedPFStndrdMode.get(1))).get("file");
				jdfFilePath = System.getProperty(ConfigurationConstants.ESPRINT_IMPOSED_DIR).concat(bookId)
						.concat("/").concat(jdfFilename);
	
				WFSDataSupport imposedPFStndrdModeDS = new WFSDataSupport();
				imposedPFStndrdModeDS.setDsType(ImpositionResponse.PFSM_PDF);
				imposedPFStndrdModeDS.setPartNumb(part.getPartNum());
				imposedPFStndrdModeDS.setCreatedDate(new Date());
				imposedPFStndrdModeDS.setCreatorId(part.getCreatorId());
				imposedPFStndrdModeDS.setDescription("Text");
				imposedPFStndrdModeDS.setProductionStatus(
						getLookUp(WFSProductionStatus.statuses.ONPROD.toString(), WFSProductionStatus.class));
				imposedPFStndrdModeDS.setName(WFSDataSupport.NAME_IMPOSE);
	
				WFSLocation imposedPFStndrdModeLocation = new WFSLocation();
				imposedPFStndrdModeLocation.setCreatedDate(new Date());
				imposedPFStndrdModeLocation.setCreatorId(part.getCreatorId());
				imposedPFStndrdModeLocation.setPath(pdfFilePath);
				imposedPFStndrdModeLocation.setFileName(pdfFilename);
				imposedPFStndrdModeLocation.setLocationType("Destination");
				imposedPFStndrdModeDS.addLocation(imposedPFStndrdModeLocation);
				sortedDataSupports.add(imposedPFStndrdModeDS);
	
				WFSDataSupport jdfPFStndrdModeDS = new WFSDataSupport();
				jdfPFStndrdModeDS.setDsType(ImpositionResponse.PFSM_JDF);
				jdfPFStndrdModeDS.setPartNumb(part.getPartNum());
				jdfPFStndrdModeDS.setCreatedDate(new Date());
				jdfPFStndrdModeDS.setCreatorId(part.getCreatorId());
				jdfPFStndrdModeDS.setDescription("Text");
				jdfPFStndrdModeDS.setProductionStatus(
						getLookUp(WFSProductionStatus.statuses.ONPROD.toString(), WFSProductionStatus.class));
				jdfPFStndrdModeDS.setName(WFSDataSupport.NAME_IMPOSE);
	
				WFSLocation jdfPFStndrdModeLocation = new WFSLocation();
				jdfPFStndrdModeLocation.setCreatedDate(new Date());
				jdfPFStndrdModeLocation.setCreatorId(part.getCreatorId());
				jdfPFStndrdModeLocation.setPath(jdfFilePath);
				jdfPFStndrdModeLocation.setFileName(jdfFilename);
				jdfPFStndrdModeLocation.setLocationType("Destination");
	
				jdfPFStndrdModeDS.addLocation(jdfPFStndrdModeLocation);
				sortedDataSupports.add(jdfPFStndrdModeDS);
			}
		}

		if (text.get(Book.RASTER) != null) {
			Map<String, Object> raster = (Map<String, Object>) text.get(Book.RASTER);
			Map<String, Object> rasterFF = (Map<String, Object>) raster.get(Book.FF_HUNKELER);
			String filepath = null;

			if (rasterFF != null) {
				ArrayList<Object> rasterFFEpacMode = (ArrayList<Object>) rasterFF.get(Book.EPAC_MODE);

				if (rasterFFEpacMode != null) {
					// FlyFolder / Epac Mode
					for(Object obj : rasterFFEpacMode){
						filename = (String) ((Map) (obj)).get("file");
						filepath = System.getProperty(ConfigurationConstants.ESPRINT_RASTER_DIR).concat(Book.EPAC_MODE)
								.concat("/").concat(filename);
	
						WFSDataSupport rasterFFEpacModeDS = new WFSDataSupport();
						rasterFFEpacModeDS.setDsType("FFEM");
						rasterFFEpacModeDS.setPartNumb(part.getPartNum());
						rasterFFEpacModeDS.setCreatedDate(new Date());
						rasterFFEpacModeDS.setCreatorId(part.getCreatorId());
						rasterFFEpacModeDS.setDescription("Text");
						rasterFFEpacModeDS.setProductionStatus(
								getLookUp(WFSProductionStatus.statuses.ONPROD.toString(), WFSProductionStatus.class));
						rasterFFEpacModeDS.setName(WFSDataSupport.NAME_RASTER);
	
						WFSLocation rasterFFEpacModeLocation = new WFSLocation();
						rasterFFEpacModeLocation.setCreatedDate(new Date());
						rasterFFEpacModeLocation.setCreatorId(part.getCreatorId());
						rasterFFEpacModeLocation.setPath(filepath);
						rasterFFEpacModeLocation.setFileName(filename);
						rasterFFEpacModeLocation.setLocationType("Destination");
	
						rasterFFEpacModeDS.addLocation(rasterFFEpacModeLocation);
						sortedDataSupports.add(rasterFFEpacModeDS);
						File f = new File(filepath);
						if(!f.exists() || !f.isDirectory() || f.list() == null || f.list().length < Rip.rasterEMExtensions.length){
							order.setNotes(order.getNotes().concat("FFEM Raster files are not all available for part " + part.getPartNum()));
							order.setStatus(com.epac.cap.model.Order.OrderStatus.ERROR.getName());
						}
					}
				}else if(imposedFFEpacMode != null && checkRasterEM){
					order.setNotes(order.getNotes().concat("No FFEM Raster files available for part " + part.getPartNum()));
					order.setStatus(com.epac.cap.model.Order.OrderStatus.ERROR.getName());
				}

				// FlyFolder / Standard mode
				ArrayList<Object> rasterFFStndMode = (ArrayList<Object>) rasterFF.get(Book.STND_MODE);

				if (rasterFFStndMode != null) {
					filename = (String) ((Map) (rasterFFStndMode.get(0))).get("file");
					filepath = System.getProperty(ConfigurationConstants.ESPRINT_RASTER_DIR).concat(Book.STND_MODE)
							.concat("/").concat(filename);

					WFSDataSupport rasterFFStndrdModeDS = new WFSDataSupport();
					rasterFFStndrdModeDS.setDsType("FFSM");
					rasterFFStndrdModeDS.setPartNumb(part.getPartNum());
					rasterFFStndrdModeDS.setCreatedDate(new Date());
					rasterFFStndrdModeDS.setCreatorId(part.getCreatorId());
					rasterFFStndrdModeDS.setDescription("Text");
					rasterFFStndrdModeDS.setProductionStatus(
							getLookUp(WFSProductionStatus.statuses.ONPROD.toString(), WFSProductionStatus.class));
					rasterFFStndrdModeDS.setName(WFSDataSupport.NAME_RASTER);

					WFSLocation rasterFFStndrdModeLocation = new WFSLocation();
					rasterFFStndrdModeLocation.setCreatedDate(new Date());
					rasterFFStndrdModeLocation.setCreatorId(part.getCreatorId());
					rasterFFStndrdModeLocation.setPath(filepath);
					rasterFFStndrdModeLocation.setFileName(filename);
					rasterFFStndrdModeLocation.setLocationType("Destination");

					rasterFFStndrdModeDS.addLocation(rasterFFStndrdModeLocation);
					sortedDataSupports.add(rasterFFStndrdModeDS);
					
					File rasterFolder = new File(System.getProperty(ConfigurationConstants.ESPRINT_RASTER_DIR).concat(Book.STND_MODE));
					File[] rasterFiles = rasterFolder.listFiles(new FileFilter() {
						@Override
						public boolean accept(File file) {
							if(file.getName().contains(Files.getNameWithoutExtension(filename)))
								return true;
							return false;
						}
					});
					if((rasterFiles == null || rasterFiles.length < Rip.rasterSMExtensions.length) && checkRasterSM){
						order.setNotes(order.getNotes().concat("FFSM Raster files are not all available for part " + part.getPartNum()));
						order.setStatus(com.epac.cap.model.Order.OrderStatus.ERROR.getName());
					}
				}else if(imposedFFStndrdMode != null && checkRasterSM){
					order.setNotes(order.getNotes().concat("No FFSM Raster files available for part " + part.getPartNum()));
					order.setStatus(com.epac.cap.model.Order.OrderStatus.ERROR.getName());
				}

			}

			Map<String, Object> rasterPF = (Map<String, Object>) raster.get(Book.PF_HUNKELER);

			if (rasterPF != null) {
				ArrayList<Object> rasterPFEpacMode = (ArrayList<Object>) rasterPF.get(Book.EPAC_MODE);

				if (rasterPFEpacMode != null) {
					// Plow Folder / Epac Mode
					for(Object obj : rasterPFEpacMode){
						filename = (String) ((Map) (obj)).get("file");
						filepath = System.getProperty(ConfigurationConstants.ESPRINT_RASTER_DIR).concat(Book.EPAC_MODE)
								.concat("/").concat(filename);
	
						WFSDataSupport rasterPFEpacModeDS = new WFSDataSupport();
						rasterPFEpacModeDS.setDsType("PFEM");
						rasterPFEpacModeDS.setPartNumb(part.getPartNum());
						rasterPFEpacModeDS.setCreatedDate(new Date());
						rasterPFEpacModeDS.setCreatorId(part.getCreatorId());
						rasterPFEpacModeDS.setDescription("Text");
						rasterPFEpacModeDS.setProductionStatus(
								getLookUp(WFSProductionStatus.statuses.ONPROD.toString(), WFSProductionStatus.class));
						rasterPFEpacModeDS.setName(WFSDataSupport.NAME_RASTER);
	
						WFSLocation rasterPFEpacModeLocation = new WFSLocation();
						rasterPFEpacModeLocation.setCreatedDate(new Date());
						rasterPFEpacModeLocation.setCreatorId(part.getCreatorId());
						rasterPFEpacModeLocation.setPath(filepath);
						rasterPFEpacModeLocation.setFileName(filename);
						rasterPFEpacModeLocation.setLocationType("Destination");
						rasterPFEpacModeDS.addLocation(rasterPFEpacModeLocation);
						sortedDataSupports.add(rasterPFEpacModeDS);
						File f = new File(filepath);
						if((!f.exists() || !f.isDirectory() || f.list() == null || f.list().length < Rip.rasterEMExtensions.length) && checkRasterEM){
							order.setNotes(order.getNotes().concat("PFEM Raster files are not all available for part " + part.getPartNum()));
							order.setStatus(com.epac.cap.model.Order.OrderStatus.ERROR.getName());
						}
					}
				}else if(imposedPFEpacMode != null && checkRasterEM){
					order.setNotes(order.getNotes().concat("No PFEM Raster files available for part " + part.getPartNum()));
					order.setStatus(com.epac.cap.model.Order.OrderStatus.ERROR.getName());
				}

				// Get raster text for PlowFolder in Standard Mode
				ArrayList<Object> rasterPFStndrdMode = (ArrayList<Object>) rasterPF.get(Book.STND_MODE);

				if (rasterPFStndrdMode != null) {
					// Plow Folder / Standard Mode
					filename = (String) ((Map) (rasterPFStndrdMode.get(0))).get("file");
					filepath = System.getProperty(ConfigurationConstants.ESPRINT_RASTER_DIR).concat(Book.STND_MODE)
							.concat("/").concat(filename);

					WFSDataSupport rasterPFStndrdModeDS = new WFSDataSupport();
					rasterPFStndrdModeDS.setDsType("PFSM");
					rasterPFStndrdModeDS.setPartNumb(part.getPartNum());
					rasterPFStndrdModeDS.setCreatedDate(new Date());
					rasterPFStndrdModeDS.setCreatorId(part.getCreatorId());
					rasterPFStndrdModeDS.setDescription("Text");
					rasterPFStndrdModeDS.setProductionStatus(
							getLookUp(WFSProductionStatus.statuses.ONPROD.toString(), WFSProductionStatus.class));
					rasterPFStndrdModeDS.setName(WFSDataSupport.NAME_RASTER);

					WFSLocation rasterPFStndrdModeLocation = new WFSLocation();
					rasterPFStndrdModeLocation.setCreatedDate(new Date());
					rasterPFStndrdModeLocation.setCreatorId(part.getCreatorId());
					rasterPFStndrdModeLocation.setPath(filepath);
					rasterPFStndrdModeLocation.setFileName(filename);
					rasterPFStndrdModeLocation.setLocationType("Destination");

					rasterPFStndrdModeDS.addLocation(rasterPFStndrdModeLocation);
					sortedDataSupports.add(rasterPFStndrdModeDS);
					
					File rasterFolder = new File(System.getProperty(ConfigurationConstants.ESPRINT_RASTER_DIR).concat(Book.STND_MODE));
					File[] rasterFiles = rasterFolder.listFiles(new FileFilter() {
						@Override
						public boolean accept(File file) {
							if(file.getName().contains(Files.getNameWithoutExtension(filename)))
								return true;
							return false;
						}
					});
					if((rasterFiles == null || rasterFiles.length < Rip.rasterSMExtensions.length) && checkRasterSM){
						order.setNotes(order.getNotes().concat("PFSM Raster files are not all available for part " + part.getPartNum()));
						order.setStatus(com.epac.cap.model.Order.OrderStatus.ERROR.getName());
					}
				}else if(imposedPFStndrdMode != null && checkRasterSM){//no rasters at all, set order to error
					order.setNotes(order.getNotes().concat("No PFSM Raster files available for part " + part.getPartNum()));
					order.setStatus(com.epac.cap.model.Order.OrderStatus.ERROR.getName());
				}

			}

		}else{//no rasters at all, set order to error
			order.setNotes(order.getNotes().concat("No Raster files available for part " + part.getPartNum()));
			order.setStatus(com.epac.cap.model.Order.OrderStatus.ERROR.getName());
		}

		return sortedDataSupports;
	}

	public  com.epac.cap.model.Package packageToOrderPackage(Package pakage, Order order) {

		com.epac.cap.model.Package pacexPackage = packageToPacexPackage(pakage);

		return pacexPackage;
	}

	private com.epac.cap.model.Package packageToPacexPackage(Package pakage) {

		com.epac.cap.model.Package pacexPackage = new com.epac.cap.model.Package();
		mapper = new Mapper();

		pacexPackage.setTypePackage(pakage.getType().toString());
		pacexPackage.setQuantity(pakage.getQuantity());
		pacexPackage.setCount(pakage.getCount());
		pacexPackage.setWeight(pakage.getWeight());
		pacexPackage.setDestination(pakage.getDestination());
		pacexPackage.setLabel(pakage.getLabel());
		Set<com.epac.cap.model.Package> packages = new HashSet<com.epac.cap.model.Package>();
		if (pakage.getPackages() != null) {
			for (Package pack : pakage.getPackages()) {
				com.epac.cap.model.Package packChild = new com.epac.cap.model.Package();

				packChild.setTypePackage(pack.getType().toString());
				packChild.setQuantity(pack.getQuantity());
				packChild.setCount(pack.getCount());
				packChild.setWeight(pack.getWeight());
				packChild.setDestination(pack.getDestination());
				packChild.setLabel(pack.getLabel());

				Set<com.epac.cap.model.PackageBook> pcbs = new HashSet<com.epac.cap.model.PackageBook>();
				for (PackageBook book : pack.getBooks()) {
					com.epac.cap.model.PackageBook packagePcb = mapper.map(book, com.epac.cap.model.PackageBook.class);
					pcbs.add(packagePcb);
				}
				packChild.setPcbs(pcbs);
				packages.add(packChild);
			}

			pacexPackage.setPackages(packages);
		} else {
			Set<com.epac.cap.model.PackageBook> pcbs = new HashSet<com.epac.cap.model.PackageBook>();
			for (PackageBook book : pakage.getBooks()) {
				com.epac.cap.model.PackageBook packageBook = mapper.map(book, com.epac.cap.model.PackageBook.class);
				pcbs.add(packageBook);
			}
			pacexPackage.setPcbs(pcbs);
		}

		return pacexPackage;
	}

	public com.epac.om.api.order.OrderStatus pacexToEsprintStatus(String omStatus) {

		com.epac.om.api.order.OrderStatus orderStatus = null;

		switch (omStatus) {

		case "ACCEPTED":
			orderStatus = com.epac.om.api.order.OrderStatus.PROCESSING;
			break;
		case "COMPLETE":
			orderStatus = com.epac.om.api.order.OrderStatus.FINISHED;
			break;
		case "ERROR":
			orderStatus = com.epac.om.api.order.OrderStatus.REJECTED;
			break;
		case "CANCELLED":
			orderStatus = com.epac.om.api.order.OrderStatus.CANCELED;
			break;
		case "REJECTED":
			orderStatus = com.epac.om.api.order.OrderStatus.REJECTED;
			break;
		case "PENDING":
			orderStatus = com.epac.om.api.order.OrderStatus.RECEIVED;
			break;
		case "ONPROD":
			orderStatus = com.epac.om.api.order.OrderStatus.PRODUCTION;
			break;
		default:
			orderStatus = null;
			break;
		}

		return orderStatus;
	}

	public Lamination pacexToEsprintLamination(CoverFinishType finishType) {

		Lamination lamination = null;

		switch (finishType.getCode()) {

		case "Glossy_Paperback":
			lamination = getLookUp("Gloss", Lamination.class);
			break;
		case "Matt_Paperback":
			lamination = getLookUp("Matt", Lamination.class);
			break;
		case "NO_FINISHING":
			lamination = getLookUp("NO_FINISHING", Lamination.class);
			break;
		case "Spot_Varnish_Paperback":
			lamination = getLookUp("Silk", Lamination.class);
			break;
		default:
			lamination = (Lamination) lookupsList.get(Lamination.class).get(0);
			break;
		}

		return lamination;
	}

	@SuppressWarnings("unchecked")
	private <T extends LookupItem> T getLookUp(String item, Class<?> type) {
		List<T> typeList = (List<T>) lookupsList.get(type);

		for (LookupItem lookup : typeList) {
			if(( type.equals(PaperType.class) && item.equals(lookup.getId())) || item.equals(lookup.getName()))
				return (T) lookup;
		}
		return null;
	}

	public  String createOrderService(Order order) {

		try {
			Order order_ = orderHandler.readByOrderNum(order.getOrderNum());
			if(order_ != null){
				LogUtils.debug("Order ["+order.getOrderNum()+"] already exists");
				return null;
			}
			orderHandler.create(order);
		} catch (PersistenceException e) {
			LogUtils.info("error occured while saving order ["+order.getOrderNum()+"]");

		}
		
		return order.getOrderNum();
	}

	
}
