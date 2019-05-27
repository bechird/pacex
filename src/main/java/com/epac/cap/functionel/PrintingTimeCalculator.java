package com.epac.cap.functionel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.config.NDispatcher;
import com.epac.cap.model.Job;
import com.epac.cap.model.Part;
import com.epac.cap.model.Preference;
import com.epac.cap.model.Roll;
import com.epac.cap.model.WFSAction;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.repository.PartDAO;
import com.epac.cap.utils.Format;
import com.epac.cap.utils.LogUtils;
import com.mycila.event.api.Event;
import com.mycila.event.api.Subscriber;
import com.mycila.event.api.topic.TopicMatcher;
import com.mycila.event.api.topic.Topics;

@Component
public class PrintingTimeCalculator extends WFSAction implements IAction{
	
	/**
	 * 
	 */

	private static final long serialVersionUID = 1L;
	private static ExecutorService 	executor;
	
	@Autowired
	private LookupDAO 				lookupDAO;

	@Autowired
	private PartDAO 				partDAO;
	
	private static float 			rollWidth;
	
	
	private static float 			threeUpMargins;
	private static float 			twoUpMargins;
	private static float 			fourUpMargins;
	private static float 			heightMargins;
	private static float 			printerSpeed;
	private static float			bleedValue;


	public final static  String UNIT_US = "US";
	public final static  String UNIT_FR = "FR";

	@Override
	public boolean handle(Object parameter) {return true;}
	
	public Float [] calculateJobHoursAndLength(Object parameter){
		
		float 	paperPerJob;
		float 	timePerJob;
		int 	impositionSchema = 1;
		int 	bookSheets = 1;
		float 	paperPerBook = 0;
		
		
		List<Object> parameters = (List<Object>) 	parameter;
		Part part				= (Part) 			parameters.get(0);
		int copyNbr				= (int) 			parameters.get(1);
		float paperWidth		= (float) 			parameters.get(2);
		
		Preference unitValue = lookupDAO.read("UNITSYSTEM", Preference.class);
		if(unitValue == null){
			unitValue = new Preference();
			unitValue.setName(UNIT_US);
		}
		Preference lookupValue = lookupDAO.read("ROLLWIDTH", Preference.class);
		
		if(paperWidth == 0){
			if(part.getPaperType()!= null && !part.getPaperType().getMedias().isEmpty()){

				if(UNIT_US.equals(unitValue.getName())){
					rollWidth = Format.inch2mm(part.getPaperType().getMedias().iterator().next().getRollWidth());
				}else{
					rollWidth = part.getPaperType().getMedias().iterator().next().getRollWidth();
				}
			}else if(lookupValue != null){
				if(UNIT_US.equals(unitValue.getName())){
					rollWidth = Format.inch2mm(Float.parseFloat(lookupValue.getName()));
				}else{
					rollWidth = Float.parseFloat(lookupValue.getName());
				}
			}
		}else{
			if(UNIT_US.equals(unitValue.getName())){
				rollWidth = Format.inch2mm((float) paperWidth);
			}else{
				rollWidth = (float) paperWidth;
			}
		}
		
		lookupValue = lookupDAO.read("THREEUPMARGINS"	, Preference.class);
		if(UNIT_US.equals(unitValue.getName())){
			threeUpMargins 	= (lookupValue != null) ? Format.inch2mm(Float.parseFloat(lookupValue.getName())) : 35;
		}else{
			threeUpMargins 	= (lookupValue != null) ? Float.parseFloat(lookupValue.getName()) : 35;
		}
		
		lookupValue = lookupDAO.read("TWOUPMARGINS"	, Preference.class);
		if(UNIT_US.equals(unitValue.getName())){
			twoUpMargins 	= (lookupValue != null) ? Format.inch2mm(Float.parseFloat(lookupValue.getName())) : 24;
		}else{
			twoUpMargins 	= (lookupValue != null) ? Float.parseFloat(lookupValue.getName()) : 24;
		}
		
		lookupValue = lookupDAO.read("FOURUPMARGINS"	, Preference.class);
		if(UNIT_US.equals(unitValue.getName())){
			fourUpMargins 	= (lookupValue != null) ? Format.inch2mm(Float.parseFloat(lookupValue.getName())) : 48;
		}else{
			fourUpMargins 	= (lookupValue != null) ? Float.parseFloat(lookupValue.getName()) : 48;
		}
		
		lookupValue = lookupDAO.read("HEIGHTMARGINS" , Preference.class);
		if(UNIT_US.equals(unitValue.getName())){
			heightMargins 	= (lookupValue != null) ? Format.inch2mm(Float.parseFloat(lookupValue.getName())) : 20;
		}else{
			heightMargins 	= (lookupValue != null) ? Float.parseFloat(lookupValue.getName()) : 20;
		}
		
		lookupValue = lookupDAO.read("BLEED" , Preference.class);
		if(UNIT_US.equals(unitValue.getName())){
			bleedValue 	= (lookupValue != null) ? Format.inch2mm(Float.parseFloat(lookupValue.getName())) : 3;
		}else{
			bleedValue 	= (lookupValue != null) ? Float.parseFloat(lookupValue.getName()) : 3;
		}
		
		lookupValue = lookupDAO.read("PRESSSPEED", Preference.class);
		//printerSpeed	= (lookupValue != null) ? Format.feet2m(Float.parseFloat(lookupValue.getName())) : 100;
		if(UNIT_US.equals(unitValue.getName())){
			printerSpeed	= (lookupValue != null) ? Format.feet2m(Float.parseFloat(lookupValue.getName())) : 6000; // 6000 meter per hour
		}else{
			printerSpeed	= (lookupValue != null) ? Float.parseFloat(lookupValue.getName()) : 6000;  //meter per hour
		}
		
		impositionSchema = impositionDefiner(UNIT_US.equals(unitValue.getName()) ? Format.inch2mm(part.getWidth()) : part.getWidth());
		bookSheets = sheetNbCalculator(part.getPagesCount() != null ? part.getPagesCount() : 1, impositionSchema);
		paperPerBook = bookSheets * ((UNIT_US.equals(unitValue.getName()) ? Format.inch2mm(part.getLength()) : part.getLength()) + heightMargins);
		
		paperPerJob = paperPerBook * copyNbr;
		//timePerJob = (paperPerJob/1000) / (printerSpeed*60);
		timePerJob = (paperPerJob/1000) / printerSpeed;
		
		// convert paperPerJob to feet for US system and to meter for French system
		paperPerJob = UNIT_US.equals(unitValue.getName()) ? Format.m2feet(paperPerJob / 1000) : (paperPerJob / 1000);
		
		TryFire(null);
		
		return new Float[] {timePerJob,
				  paperPerJob,
				  (float) impositionSchema}; 
	}

	@Override
	public boolean TryFire(String partNb) {
		//Elamine: No subscriber is implemented to handle this notification
		/*
		Map<String, Float> timeLengthAndImp = new HashMap<>();
		timeLengthAndImp.put("paperLength", paperPerJob);
		timeLengthAndImp.put("timeNeeded", timePerJob);
		timeLengthAndImp.put("impositionScheme", (float) impositionScheme);
		NDispatcher.getDispatcher().publish(Topics.topic("cap/events/PrintingTimeCalculation/done"), true);
		*/
		return true;
	}
	@Override
	public void subscribe() {
		TopicMatcher matcher = Topics.only("cap/events/PrintingTimeCalculation");
		NDispatcher.getDispatcher().subscribe(matcher, List.class, new Subscriber<List>() {
			public void onEvent(Event<List> event) throws Exception {
				LogUtils.debug("Received: " + event.toString() + " calling the download method ");
				Runnable task = new Runnable() {
					@Override
					public void run() {
						try {
							handle(event.getSource());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				executor.execute(task);
			}
		});
		
	}

	@Override
	public void unsubscribe() {
		// TODO Auto-generated method stub
		
	}
	
	public PrintingTimeCalculator() {
		executor = Executors.newCachedThreadPool();
		subscribe();
	}
	
	//Elamine: Should not set impositionScheme as class attribute
	// 		   Class has one instance as component and this method is accessed by
	// 		   multiple threads and can cause wrong imposition values		
	private int impositionDefiner(float width) {
		if (rollWidth >= ((width+bleedValue) * 4 + fourUpMargins))
			return 4;
		if (rollWidth >= ((width+bleedValue) * 3 + threeUpMargins))
			return 3;
		if (rollWidth >= ((width+bleedValue) * 2 + twoUpMargins))
			return 2;
		return 2;
	}

	private int sheetNbCalculator(int pageCount, int impositionSchema) {
		int bookSheets = pageCount / (impositionSchema * 2);
		if (pageCount % (impositionSchema * 2) > 0)
			bookSheets = (pageCount / (impositionSchema * 2)) + 1;
		
		return bookSheets;

	}
	
	public Integer getJobQuantity(Float jobHours, Part part) {
		int result = 0;
		float paperWidth = 0;
		
		Preference unitValue = lookupDAO.read("UNITSYSTEM", Preference.class);
		if(unitValue == null){
			unitValue = new Preference();
			unitValue.setName(UNIT_US);
		}
		Preference lookupValue = lookupDAO.read("ROLLWIDTH", Preference.class);
		
		if(paperWidth == 0){
			if(part.getPaperType() != null && !part.getPaperType().getMedias().isEmpty()){
				if(UNIT_US.equals(unitValue.getName())){
					rollWidth = Format.inch2mm(part.getPaperType().getMedias().iterator().next().getRollWidth());
				}else{
					rollWidth = part.getPaperType().getMedias().iterator().next().getRollWidth();
				}
			}else if(lookupValue != null){
				if(UNIT_US.equals(unitValue.getName())){
					rollWidth = Format.inch2mm(Float.parseFloat(lookupValue.getName()));
				}else{
					rollWidth = Float.parseFloat(lookupValue.getName());
				}
			}
		}else{
			if(UNIT_US.equals(unitValue.getName())){
				rollWidth = Format.inch2mm((float) paperWidth);
			}else{
				rollWidth = (float) paperWidth;
			}
		}
		
		lookupValue = lookupDAO.read("THREEUPMARGINS"	, Preference.class);
		if(UNIT_US.equals(unitValue.getName())){
			threeUpMargins 	= (lookupValue != null) ? Format.inch2mm(Float.parseFloat(lookupValue.getName())) : 33;
		}else{
			threeUpMargins 	= (lookupValue != null) ? Float.parseFloat(lookupValue.getName()) : 33;
		}
		
		lookupValue = lookupDAO.read("TWOUPMARGINS"	, Preference.class);
		if(UNIT_US.equals(unitValue.getName())){
			twoUpMargins 	= (lookupValue != null) ? Format.inch2mm(Float.parseFloat(lookupValue.getName())) : 25;
		}else{
			twoUpMargins 	= (lookupValue != null) ? Float.parseFloat(lookupValue.getName()) : 25;
		}
		
		lookupValue = lookupDAO.read("FOURUPMARGINS"	, Preference.class);
		if(UNIT_US.equals(unitValue.getName())){
			fourUpMargins 	= (lookupValue != null) ? Format.inch2mm(Float.parseFloat(lookupValue.getName())) : 48;
		}else{
			fourUpMargins 	= (lookupValue != null) ? Float.parseFloat(lookupValue.getName()) : 48;
		}
		
		lookupValue = lookupDAO.read("HEIGHTMARGINS" , Preference.class);
		if(UNIT_US.equals(unitValue.getName())){
			heightMargins 	= (lookupValue != null) ? Format.inch2mm(Float.parseFloat(lookupValue.getName())) : 20;
		}else{
			heightMargins 	= (lookupValue != null) ? Float.parseFloat(lookupValue.getName()) : 20;
		}
		
		lookupValue = lookupDAO.read("BLEED" , Preference.class);
		if(UNIT_US.equals(unitValue.getName())){
			bleedValue 	= (lookupValue != null) ? Format.inch2mm(Float.parseFloat(lookupValue.getName())) : 3;
		}else{
			bleedValue 	= (lookupValue != null) ? Float.parseFloat(lookupValue.getName()) : 3;
		}
		
		lookupValue = lookupDAO.read("PRESSSPEED", Preference.class);
		if(UNIT_US.equals(unitValue.getName())){
			printerSpeed	= (lookupValue != null) ? Format.feet2m(Float.parseFloat(lookupValue.getName())) : 6000; // 6000 meter per hour
		}else{
			printerSpeed	= (lookupValue != null) ? Float.parseFloat(lookupValue.getName()) : 6000;  //meter per hour
		}
		
		int impositionSchema = impositionDefiner(UNIT_US.equals(unitValue.getName()) ? Format.inch2mm(part.getWidth()) : part.getWidth());
		int bookSheets = sheetNbCalculator(part.getPagesCount() != null ? part.getPagesCount() : 1, impositionSchema);
		float paperPerBook = bookSheets * ((UNIT_US.equals(unitValue.getName()) ? Format.inch2mm(part.getLength()) : part.getLength()) + heightMargins);
		
		result = 	(int) Math.ceil(jobHours * printerSpeed * 1000 / paperPerBook);
		return result;
	}
	
	/**
	 * Calculates/finds the best sheet height that should be used based on the list of jobs provided; this is needed for Epac mode printing to 
	 * return the best common sheet height to use for all these jobs
	 */
	 public String getBestSheetHeight(Roll roll){
		 
		 Preference bsValue = lookupDAO.read("BESTSHEETUSE", Preference.class);
		 if(bsValue != null && "false".equals(bsValue.getName())){
			 LogUtils.debug("Returning S0 for best sheet feature as it is disabled...");
			 return "S0";
		 }
		 
		 Set<Job> jobs = roll.getJobs();
		 
		 boolean sameBookHeight = true;
		 
		 Preference unitValue = lookupDAO.read("UNITSYSTEM", Preference.class);
		 if(unitValue == null){
			 unitValue = new Preference();
			 unitValue.setName(UNIT_US);
		 }
		 
		 if(jobs == null || jobs.isEmpty()){
			 LogUtils.debug("Roll #"+roll.getRollId()+": No jobs in the roll, best sheet size is S0");
			 return "S0";
		 }
		 
		 // check the case where all jobs have same part height
		 Part p = partDAO.read(jobs.iterator().next().getPartNum());
		 float partHeight = p.getLength();
		 for(Job j : jobs){
			 p = partDAO.read(j.getPartNum());
			 if(p.getLength() != partHeight){
				 sameBookHeight = false;
				 break;
			 }
		 }
		 if(sameBookHeight){
			 LogUtils.debug("Roll #"+roll.getRollId()+": All jobs have the same height, best sheet size is S0");
			 return "S0";
		 }
		 
		 LogUtils.debug("Roll #"+roll.getRollId()+": Calculating minimum paper waste for each sheet size...");
		 Map<String, Float> sheetSizes = new HashMap<>();
		 // suppose we have at maximum 5 best sheet sizes, we can activate/desactivate any sheet size
		 for(int i=1; i<=5; i++){
			 String strSheetSize = System.getProperty("com.epac.cap.sheet.S"+i+".height");
			 if(strSheetSize == null)
				 continue;
			 
			 sheetSizes.put("S"+i, Float.parseFloat(strSheetSize));
			 
		 }
		 
		 if(sheetSizes.isEmpty()){
			 LogUtils.debug("Roll #"+roll.getRollId()+": None of the sheet sizes parameters is activated");
			 return "";
		 }
		 
		 LogUtils.debug("Roll #"+roll.getRollId()+": sheet sizes found in configuration file: "+sheetSizes);
		 
		 //look for the sheet to use by calculating the waste for each sheet type and looking for the minimum
		 Map<String, Float> wastes = new HashMap<>();
		 for(String sheetName: sheetSizes.keySet() ){
			 float waste = 0;
			 for(Job j : jobs){
				 p = partDAO.read(j.getPartNum());
				 float signatureHeight = (UNIT_US.equals(unitValue.getName()) ? Format.inch2mm(p.getLength()) : p.getLength()) + 26;
				 int nbSigPerSheet = (int) Math.floor(sheetSizes.get(sheetName) / signatureHeight);
				 int imp =  Math.round(calculateJobHoursAndLength(Arrays.asList(p, j.getQuantityNeeded(), (float) 0.0))[2]);
				 int nbSignatures = (int) Math.ceil((float)p.getPagesCount() / (2 * imp));
				 int nbSheets = (int) Math.ceil((float)nbSignatures / (float)nbSigPerSheet);
				 waste = waste + (nbSheets * (sheetSizes.get(sheetName) - (nbSigPerSheet * signatureHeight)));
				 int emptyWastedSignatures = (nbSheets * nbSigPerSheet) - nbSignatures;
				 waste = waste + (emptyWastedSignatures * signatureHeight);
			 }
			 wastes.put(sheetName, waste);
			 LogUtils.debug("Roll #"+roll.getRollId()+": total waste when "+sheetName+" is used is: "+waste);
		 }
		 String minWasteSheet = sheetSizes.keySet().iterator().next();
		 for(String sheetName : wastes.keySet()){
			 if(wastes.get(sheetName) < wastes.get(minWasteSheet)){
				 minWasteSheet = sheetName;
			 }
		 }
		 LogUtils.debug("Roll #"+roll.getRollId()+": minimum waste is generated by sheet "+minWasteSheet+" so it's the best sheet size to be used");
		 
		 return minWasteSheet;
	 }

	/**
	 * @return the lookupDAO
	 */
	public LookupDAO getLookupDAO() {
		return lookupDAO;
	}

	/**
	 * @param lookupDAO the lookupDAO to set
	 */
	public void setLookupDAO(LookupDAO lookupDAO) {
		this.lookupDAO = lookupDAO;
	}

	public PartDAO getPartDAO() {
		return partDAO;
	}

	public void setPartDAO(PartDAO partDAO) {
		this.partDAO = partDAO;
	}

}