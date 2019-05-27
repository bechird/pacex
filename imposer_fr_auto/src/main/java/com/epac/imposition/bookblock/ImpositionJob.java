package com.epac.imposition.bookblock;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.epac.imposition.config.Configuration;
import com.epac.imposition.config.Constants;
import com.epac.imposition.model.CueMark;
import com.epac.imposition.model.Datamatrix;
import com.epac.imposition.model.ImpositionBook;
import com.epac.imposition.model.RefreshBar;
import com.epac.imposition.utils.CompositionScheme;
import com.epac.imposition.utils.Format;
import com.epac.imposition.utils.ImpositionType;
import com.epac.om.api.utils.LogUtils;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;

public class ImpositionJob {
	
	private 	String 			markerPdfPath;
	
	private 	String			jdfTemplatePath;
	
	private 	String			mediaProfile;
	
	private 	ImpositionType	impositionType;
	
	private 	int 			customerCode = -1;
	
	private 	float			logoWidth;
	
	private		float			logoHeight;

	private 	float 			milling;

	private 	float 			triming;
	
	private		float			bleed;
	
	private 	float			leftMargin;
	
	private		float			rightMargin;

	private 	float 			topMargin;

	private 	float 			bottomMargin;

	private 	float 			nonprintingarea;

	private 	float 			outputControllerMargin;
	
	private 	float 			printSheetHeight;

	private 	Datamatrix		hunkelerDatamatrix;

	private 	CueMark			hunkelerCueMark;
	
	private 	RefreshBar 		refreshbar;

	private 	ImpositionBook 			book;
	
	private 	CompositionScheme composition;
	
	private float			paperthikness;
	
	private boolean 		isBigSheet;
	
	private boolean 		isTextWithBleed;
	
	private boolean 		isEpacMPb = false;
	
	private boolean 		isRTL;
	
	private 	float 			standardBigSheetHeight;
	
	private 	float 			BigSheetHeightS1;
	private 	float 			BigSheetHeightS2;
	private 	float 			BigSheetHeightS3;

	private float 			sheetWidth;

	private float 			sheetHeight;
	
	private int[]			upperSide;
	
	private int[]			lowerSide;
		
	private int 			sheetPersig;
	
	private float[]			refreshbarYPos;
	
	private float			xPosStartUpper;
	
	private float 			xPosEndUpper;
	
	private float			xPosStartBottom;
	
	private float 			xPosEndBottom;
	private String 			depotLegal;

	private float 			innerMilling;

	private String 			hunkelerLine;
	
	private String				perforation;
	
	//private List<String> 	hunkelerlines 	= new ArrayList<>();

	public static final String PB 			= "PB";
	public static final String PF 			= "PF";
	public static final String FF 			= "FF";
	public static final String PL 			= "PL";
	
	public static final String ALL 			= "ALL";
	
	public static final String S0 = "S0";
	public static final String S1 = "S1";
	public static final String S2 = "S2";
	public static final String S3 = "S3";

	private String formula;
	
	private String prefixHeight     = "";
	
	public String getPrefixHeight() {
		return prefixHeight;
	}

	public void setPrefixHeight(String prefixHeight) {
		this.prefixHeight = prefixHeight;
	}
	
	public static void addImpositionDate(PdfContentByte cb, float x1,float x2, float y, String bookId, String hint, boolean flipped){
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		String text = "Imposed by EPAC v"+App.IMPOSITION_VERSION+" ("+hint+") on "+ sdf.format(new Date());
		FontFactory.defaultEmbedding = true;

		BaseFont helvetica;
		try {
			helvetica = BaseFont.createFont("Helvetica.otf", BaseFont.CP1252, BaseFont.EMBEDDED);
		
			float orientation = 0;
			int align =  Element.ALIGN_LEFT;
			if(flipped){
				orientation = 180;
				align =  Element.ALIGN_RIGHT;
				
			}
	
			
			Font font = new Font(helvetica, 8, Font.NORMAL);
			ColumnText.showTextAligned(cb, align, new Phrase(text, font),
				Format.mm2points(x1).floatValue(), Format.mm2points(y).floatValue(), orientation);
			String batchName = System.getProperty(Constants.COVERS_BATCH_NAME);
			if(!flipped && batchName != null){
				bookId =
						String.valueOf(counter++).
						concat(" / ").concat(String.valueOf(totalBatch)).
						concat(" ").
						concat(System.getProperty(Constants.COVERS_BATCH_NAME)).
						concat(" ").
						concat(bookId);
				y = 5f;
				x2 = 6f;
			

				font = new Font(helvetica, 8, Font.NORMAL);
				float w = font.getCalculatedBaseFont(true).getWidthPoint(bookId, font.getCalculatedSize());
			
				ColumnText.showTextAligned(cb, align, new Phrase(bookId, font),
						Format.mm2points(x2).floatValue(), Format.mm2points(y).floatValue(), orientation);
			}

		} catch (Exception e) {
			LogUtils.error("Error occured while adding imposition date text", e);
		}
	}
	
	public static int counter = 1;
	public static int totalBatch = 0;
	
	
	/*
	public List<String> getHunkelerlines() {
		return hunkelerlines;
	}

	public void setHunkelerlines(List<String> hunkelerlines) {
		this.hunkelerlines = hunkelerlines;
	}
	*/

	public float getBleed() {
		return bleed;
	}

	public void setBleed(float bleed) {
		this.bleed = bleed;
	}

	public float getLeftMargin() {
		return leftMargin;
	}

	public void setLeftMargin(float leftMargin) {
		this.leftMargin = leftMargin;
	}

	public float getRightMargin() {
		return rightMargin;
	}

	public void setRightMargin(float rightMargin) {
		this.rightMargin = rightMargin;
	}

	public float getLogoWidth() {
		return logoWidth;
	}

	public void setLogoWidth(float logoWidth) {
		this.logoWidth = logoWidth;
	}

	public float getLogoHeight() {
		return logoHeight;
	}

	public void setLogoHeight(float logoHeight) {
		this.logoHeight = logoHeight;
	}

	public int getCustomerCode() {
		return customerCode;
	}

	public void setCustomerCode(int customerCode) {
		this.customerCode = customerCode;
	}

	public float getPaperthikness() {
		return paperthikness;
	}

	public void setPaperthikness(float paperthikness) {
		this.paperthikness = paperthikness;
	}

	public float[] getRefreshbarYPos() {
		return refreshbarYPos;
	}

	public void setRefreshbarYPos(float[] refreshbarYPos) {
		this.refreshbarYPos = refreshbarYPos;
	}

	public float getxPosStartUpper() {
		return xPosStartUpper;
	}

	public void setxPosStartUpper(float xPosStartUpper) {
		this.xPosStartUpper = xPosStartUpper;
	}

	public float getxPosEndUpper() {
		return xPosEndUpper;
	}

	public void setxPosEndUpper(float xPosEndUpper) {
		this.xPosEndUpper = xPosEndUpper;
	}

	public float getxPosStartBottom() {
		return xPosStartBottom;
	}

	public void setxPosStartBottom(float xPosStartBottom) {
		this.xPosStartBottom = xPosStartBottom;
	}

	public float getxPosEndBottom() {
		return xPosEndBottom;
	}

	public void setxPosEndBottom(float xPosEndBottom) {
		this.xPosEndBottom = xPosEndBottom;
	}

	public boolean isBigSheet() {
		return isBigSheet;
	}

	public void setBigSheet(boolean isBigSheet) {
		this.isBigSheet = isBigSheet;
	}
	
	public String getMarkerPdfPath() {
		return markerPdfPath;
	}

	public void setMarkerPdfPath(String markerPdfPath) {
		this.markerPdfPath = markerPdfPath;
	}

	public String getJdfTemplatePath() {
		return jdfTemplatePath;
	}

	public void setJdfTemplatePath(String jdfTemplatePath) {
		this.jdfTemplatePath = jdfTemplatePath;
	}

	public String getMediaProfile() {
		return mediaProfile;
	}

	public void setMediaProfile(String mediaProfile) {
		this.mediaProfile = mediaProfile;
	}
	
	public float getMilling() {
		if (isPopLinePL())
			return 0.0f;
		else if (isPopLinePB())
			return Constants.PB_DEFAULT_MILLING;
		else
			return milling;
	}

	public void setMilling(float milling) {
		this.milling = milling;
	}

	public float getTriming() {
		return triming;
	}

	public void setTriming(float triming) {
		this.triming = triming;
	}

	public float getTopMargin() {
		return topMargin;
	}

	public void setTopMargin(float topMargin) {
		this.topMargin = topMargin;
	}

	public float getBottomMargin() {
		return bottomMargin;
	}

	public void setBottomMargin(float bottomMargin) {
		this.bottomMargin = bottomMargin;
	}

	public float getNonprintingarea() {
		return nonprintingarea;
	}

	public void setNonprintingarea(float nonprintingarea) {
		this.nonprintingarea = nonprintingarea;
	}

	public float getOutputControllerMargin() {
		return outputControllerMargin;
	}

	public void setOutputControllerMargin(float outputControllerMargin) {
		this.outputControllerMargin = outputControllerMargin;
	}

	public float getPrintSheetHeight() {
		return printSheetHeight;
	}

	public void setPrintSheetHeight(float printSheetHeight) {
		this.printSheetHeight = printSheetHeight;
	}
	
	public ImpositionType getImpositionType() {
		return impositionType;
	}

	public void setImpositionType(ImpositionType impositionType) {
		this.impositionType = impositionType;
	}

	public Datamatrix getHunkelerDatamatrix() {
		return hunkelerDatamatrix;
	}

	public void setHunkelerDatamatrix(Datamatrix hunkelerDatamatrix) {
		this.hunkelerDatamatrix = hunkelerDatamatrix;
	}

	public CueMark getHunkelerCueMark() {
		return hunkelerCueMark;
	}

	public void setHunkelerCueMark(CueMark hunkelerCueMark) {
		this.hunkelerCueMark = hunkelerCueMark;
	}

	public RefreshBar getRefreshbar() {
		return refreshbar;
	}

	public void setRefreshbar(RefreshBar refreshbar) {
		this.refreshbar = refreshbar;
	}

	public ImpositionBook getBook() {
		return book;
	}
	
	public void setBook(ImpositionBook book) {
		this.book = book;
	}

	public CompositionScheme getComposition() {
		return composition;
	}

	public void setComposition(CompositionScheme composition) {
		this.composition = composition;
	}

	public float getSheetWidth() {
		return sheetWidth;
	}

	public void setSheetWidth(float sheetWidth) {
		this.sheetWidth = sheetWidth;
	}

	public float getSheetHeight() {
		return sheetHeight;
	}

	public void setSheetHeight(float sheetHeight) {
		this.sheetHeight = sheetHeight;
	}

	public int[] getUpperSide() {
		return upperSide;
	}

	public void setUpperSide(int[] upperSide) {
		this.upperSide = upperSide;
	}

	public int[] getLowerSide() {
		return lowerSide;
	}

	public void setLowerSide(int[] lowerSide) {
		this.lowerSide = lowerSide;
	}

	public int getSheetPersig() {
		return sheetPersig;
	}

	public void setSheetPersig(int sheetPersig) {
		this.sheetPersig = sheetPersig;
	}
	
	public boolean isRTL() {
		return isRTL;
	}

	public void setRTL(boolean isRTL) {
		this.isRTL = isRTL;
	}
	
	public String getPerforation() {
		return perforation;
	}

	public void setPerforation(String perforation) {
		this.perforation = perforation;
	}


	public float getStandardBigSheetHeight() {
		return standardBigSheetHeight;
	}

	public void setStandardBigSheetHeight(float standardBigSheetHeight) {
		this.standardBigSheetHeight = standardBigSheetHeight;
	}

	public float getBigSheetHeightS1() {
		return BigSheetHeightS1;
	}

	public void setBigSheetHeightS1(float bigSheetHeightS1) {
		BigSheetHeightS1 = bigSheetHeightS1;
	}

	public float getBigSheetHeightS2() {
		return BigSheetHeightS2;
	}

	public void setBigSheetHeightS2(float bigSheetHeightS2) {
		BigSheetHeightS2 = bigSheetHeightS2;
	}

	public float getBigSheetHeightS3() {
		return BigSheetHeightS3;
	}

	public void setBigSheetHeightS3(float bigSheetHeightS3) {
		BigSheetHeightS3 = bigSheetHeightS3;
	}

	/**
	 * 
	 * @param sheetheight
	 */
	public void updateOutputFilePath (float sheetheight){
		//sji to be changed output filename; changed by walid please make sure it is correct
		String file = this.getBook().getOutputFilePath();
		int index = file.lastIndexOf(File.separatorChar);
		String heightprefix = String.valueOf(Format.mm2inch(sheetheight+ nonprintingarea).floatValue());
		if (this.isPopLine()) {
			String prefix = isBigSheet() ? "S0" : heightprefix;
			file = file.substring(0, index).concat(File.separator).concat(prefix).concat("_").concat(getHunkelerLine())
					.concat(isBigSheet() ? "EM" : "SM").concat(file.substring(index + 1));
			if (isBigSheet()) {
				setEpacMPb(true);
				setBigSheet(false);
			}
		} else {
			String prefix = isBigSheet() ? getPrefixHeight() : heightprefix;
			file = file.substring(0, index).concat(File.separator).concat(prefix).concat("_").concat(getHunkelerLine())
					.concat(isBigSheet() ? "EM" : "SM").concat(file.substring(index + 1));
		}
		this.getBook().setOutputFilePath(file);
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static ImpositionType getImpositionTypeFromConfig (byte type){
		ImpositionType impoType = ImpositionType.ALL;
		
		switch (type) {
		case 0:
			impoType = ImpositionType.TEXT;
			break;
		case 1:
			impoType = ImpositionType.COVER;	
			break;
		case 2:
			impoType = ImpositionType.ALL;
			break;
		}
		
		return impoType;
	}
	
	
	/**
	 * 
	 * @param inputfolder
	 * @param outputfilepath
	 * @return
	 * @throws ComposerException
	 */
	public static ImpositionJob create (String inputfilepath, String outputfilepath, String profile, String docID, float rollwidth) throws ComposerException {
		ImpositionJob job = new ImpositionJob();

		Configuration.load("imposition.properties");

		try{
			String  hunkelerLine 		= System.getProperty(Constants.IMPOSITION_HUNKELER_LINE);
			String  perforation 		= System.getProperty(Constants.IMPOSITION_POP_PERFORATION, "2");
			String  jdfTemplatePath 	= System.getProperty(Constants.IMPOSITION_JDF_TEMPLATE_PATH);
			String  markerPdfPath 		= System.getProperty(Constants.IMPOSITION_REFRESHBAR_PATH);
			
			float	milling 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_MILLING));
			float	bleed 				= Float.parseFloat(System.getProperty(Constants.IMPOSITION_BLEED));
			float	triming 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_TRIMING));
			
			
			float	nonprinting 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_NON_PRINTING_AREA));

			float	ocMargin 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_OUTPUTCONTROLLER_MARGIN));
			
			float	datamatrix_xPos 	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_DATAMATRIX_XPOS));
			float	datamatrix_yPos 	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_DATAMATRIX_YPOS));
			float	datamatrix_width 	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_DATAMATRIX_WIDTH));
			float	datamatrix_height 	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_DATAMATRIX_HEIGHT));			
			
			float	printSheetHeight	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_PRINTER_SHEET_HEIGHT));
			
			float	printSheetType		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_PRINTER_SHEET_TYPE));

			/*
			boolean popEnabled			= Boolean.parseBoolean(System.getProperty(Constants.IMPOSITION_PL_ENABLED));
			boolean pfEnabled			= Boolean.parseBoolean(System.getProperty(Constants.IMPOSITION_PF_ENABLED));
			boolean ffEnabled			= Boolean.parseBoolean(System.getProperty(Constants.IMPOSITION_FF_ENABLED));
			
			if (popEnabled)
				job.hunkelerlines.add(PL);
			
			if (pfEnabled)
				job.hunkelerlines.add(PF);
			
			if (ffEnabled)
				job.hunkelerlines.add(FF);
			*/
			float	cueMark_yPos;
			float	cueMark_width;
			float	cueMark_height;
			
			if (hunkelerLine.equalsIgnoreCase(PL) || hunkelerLine.equalsIgnoreCase(PB))
				cueMark_yPos 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_POP_CUEMARK_YPOS));
			else
				cueMark_yPos 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_CUEMARK_YPOS));
			
			if (hunkelerLine.equalsIgnoreCase(PL) || hunkelerLine.equalsIgnoreCase(PB))
				cueMark_width		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_POP_CUEMARK_WIDTH));
			else
				cueMark_width		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_CUEMARK_WIDTH));
			
			if (hunkelerLine.equalsIgnoreCase(PL) || hunkelerLine.equalsIgnoreCase(PB))
				cueMark_height 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_POP_CUEMARK_HEIGHT));
			else
				cueMark_height 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_CUEMARK_HEIGHT));
			
			
			job.setPerforation(perforation);
			job.setHunkelerLine(hunkelerLine);
			
			job.setJdfTemplatePath(jdfTemplatePath);
			job.setMarkerPdfPath(markerPdfPath);
			
			job.setMediaProfile(profile);
			job.setPrintSheetHeight(printSheetHeight);
			
			job.setNonprintingarea(nonprinting);			
			job.setMilling(milling);
			job.setTriming(triming);
			
			job.setBleed(bleed);
			
			job.setOutputControllerMargin(ocMargin);
			
			job.setSheetWidth(rollwidth);
			
			job.setBigSheet(printSheetType == 1);
			
			Datamatrix datamatrix = new Datamatrix();
			datamatrix.setWidth(datamatrix_width);
			datamatrix.setHeight(datamatrix_height);
			datamatrix.setxPos(datamatrix_xPos);
			datamatrix.setyPos(datamatrix_yPos);
			
			CueMark cueMark = new CueMark();
			cueMark.setWidth(cueMark_width);
			cueMark.setHeight(cueMark_height);
			cueMark.setyPos(cueMark_yPos);

			ImpositionBook impositionBook = new ImpositionBook();
			impositionBook.setDocId(docID);
			impositionBook.setFilePath(inputfilepath);
			impositionBook.setTempOutputFilePath(outputfilepath.substring(0, outputfilepath.indexOf(".pdf"))+ "_temp.pdf");
			impositionBook.setOutputFilePath(outputfilepath);
			job.setBook(impositionBook);
			
			job.setHunkelerCueMark(cueMark);
			job.setHunkelerDatamatrix(datamatrix);
			
			if (!hunkelerLine.equalsIgnoreCase(ALL)){
				String topMarginKey 		= "com.epac.composer."+hunkelerLine+".topmargin";
				String bottomMarginKey		= "com.epac.composer."+hunkelerLine+".bottommargin";
				
				float	top_margin 			= Float.parseFloat(System.getProperty(topMarginKey));
				float	bottom_margin 		= Float.parseFloat(System.getProperty(bottomMarginKey));
				
				job.setTopMargin(top_margin);
				job.setBottomMargin(bottom_margin);
			}

		}catch(Exception e){
			throw new ComposerException("Error while parsing values from config file... "+e.getMessage());
		}

		return job;
	}

	/**
	 * 
	 * @param book
	 * @param inputFile
	 * @param outputfilepath
	 * @param docID
	 * @param rollwidth
	 * @param PrintedSheetType
	 * @param hunkelerLine
	 * @return
	 * @throws ComposerException
	 */
	public static ImpositionJob create (com.epac.om.api.book.Book book, String inputFile, String outputfilepath, float rollwidth, String PrintedSheetType, String hunkelerLine, String perforation, String[] bestSheets) throws ComposerException {
		ImpositionJob job = new ImpositionJob();


		try{
			String 	jdfTemplatePath 	= System.getProperty(Constants.IMPOSITION_JDF_TEMPLATE_PATH);
			String 	markerPdfPath 		= System.getProperty(Constants.IMPOSITION_REFRESHBAR_PATH);
		
	
			//String  perforation 		= System.getProperty(Constants.IMPOSITION_POP_PERFORATION, "2");
			float	logoWidth 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_CUSTOMER_LOGO_WIDTH));

			float	logoHeight 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_CUSTOMER_LOGO_HEIGHT));
			float	milling 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_MILLING));
			float	triming 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_TRIMING));
			
			float	bleed 				= Float.parseFloat(System.getProperty(Constants.IMPOSITION_BLEED));
			
			float	nonprinting 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_NON_PRINTING_AREA));

			float	innerMilling 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_INNER_MILLING));
			
			float	ocMargin 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_OUTPUTCONTROLLER_MARGIN));
			
			float	datamatrix_xPos 	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_DATAMATRIX_XPOS));
			float	datamatrix_yPos 	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_DATAMATRIX_YPOS));
			float	datamatrix_width 	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_DATAMATRIX_WIDTH));
			float	datamatrix_height 	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_DATAMATRIX_HEIGHT));			
			
			//float	printSheetHeight	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_PRINTER_SHEET_HEIGHT));
			float	printSheetType		= Float.parseFloat(PrintedSheetType);
			boolean depotLegal			= Boolean.parseBoolean(System.getProperty(Constants.IMPOSITION_ADD_DEPOT_LEGAL));
			byte	impositionType		= Byte.parseByte(System.getProperty(Constants.IMPOSITION_TYPE));

			/*
			boolean popEnabled			= Boolean.parseBoolean(System.getProperty(Constants.IMPOSITION_PL_ENABLED));
			boolean pfEnabled			= Boolean.parseBoolean(System.getProperty(Constants.IMPOSITION_PF_ENABLED));
			boolean ffEnabled			= Boolean.parseBoolean(System.getProperty(Constants.IMPOSITION_FF_ENABLED));
			
			if (popEnabled)
				job.hunkelerlines.add(PL);
			
			if (pfEnabled)
				job.hunkelerlines.add(PF);
			
			if (ffEnabled)
				job.hunkelerlines.add(FF);
			*/
			
			float printSheetHeight	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_PRINTER_SHEET_STANDARD_HEIGHT, "0.0"));		
			float bigSheetHeightS1  = Float.parseFloat(System.getProperty(Constants.IMPOSITION_PRINTER_S1_HEIGHT, "0.0"));	
			float bigSheetHeightS2  = Float.parseFloat(System.getProperty(Constants.IMPOSITION_PRINTER_S2_HEIGHT, "0.0"));			
			float bigSheetHeightS3  = Float.parseFloat(System.getProperty(Constants.IMPOSITION_PRINTER_S3_HEIGHT, "0.0"));
		
			job.setStandardBigSheetHeight(printSheetHeight);
			if(bestSheets == null || bestSheets.length == 0){
				job.setBigSheetHeightS1(bigSheetHeightS1);
				job.setBigSheetHeightS2(bigSheetHeightS2);
				job.setBigSheetHeightS3(bigSheetHeightS3);
			}else{
				if(bestSheets[1].equals(S1)){
					job.setBigSheetHeightS1(bigSheetHeightS1);
				}else{
					job.setBigSheetHeightS1(0);
				}
				if(bestSheets[1].equals(S2)){
					job.setBigSheetHeightS2(bigSheetHeightS2);
				}else{
					job.setBigSheetHeightS2(0);
				}
				if(bestSheets[1].equals(S3)){
					job.setBigSheetHeightS3(bigSheetHeightS3);
				}else{
					job.setBigSheetHeightS3(0);
				}
			}
			
			
			float	cueMark_yPos;
			float	cueMark_width;
			float	cueMark_height;
			
			if (hunkelerLine.equalsIgnoreCase(PL) || hunkelerLine.equalsIgnoreCase(PB))
				cueMark_yPos 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_POP_CUEMARK_YPOS));
			else
				cueMark_yPos 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_CUEMARK_YPOS));
			
			if (hunkelerLine.equalsIgnoreCase(PL) || hunkelerLine.equalsIgnoreCase(PB))
				cueMark_width		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_POP_CUEMARK_WIDTH));
			else
				cueMark_width		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_CUEMARK_WIDTH));
			
			if (hunkelerLine.equalsIgnoreCase(PL) || hunkelerLine.equalsIgnoreCase(PB))
				cueMark_height 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_POP_CUEMARK_HEIGHT));
			else
				cueMark_height 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_CUEMARK_HEIGHT));
			
			
			// convert from μm to mm (x 10e-3)
			float	paperthikness	= (float)book.getMetadata().getTextPaperType().getPaperThickness() / 1000; //Float.parseFloat(System.getProperty(Constants.IMPOSITION_PAPER_THIKNESS));

			
			job.setPerforation(perforation);
			job.setJdfTemplatePath(jdfTemplatePath);
			job.setMarkerPdfPath(markerPdfPath);
			
			job.setPaperthikness(paperthikness);
			job.setPrintSheetHeight(printSheetHeight);
			
			job.setInnerMilling(innerMilling);
			job.setImpositionType(getImpositionTypeFromConfig(impositionType));
			
			job.setHunkelerLine(hunkelerLine);
			job.setLogoWidth(logoWidth);
			job.setLogoHeight(logoHeight);
			
			job.setNonprintingarea(nonprinting);			
			job.setMilling(milling);
			job.setTriming(triming);
			job.setBigSheet(printSheetType == 1);
			job.setOutputControllerMargin(ocMargin);
			
			job.setBleed(bleed);
			
			job.setSheetWidth(rollwidth);
			
			Datamatrix datamatrix = new Datamatrix();
			datamatrix.setWidth(datamatrix_width);
			datamatrix.setHeight(datamatrix_height);
			datamatrix.setxPos(datamatrix_xPos);
			datamatrix.setyPos(datamatrix_yPos);
			
			CueMark cueMark = new CueMark();
			cueMark.setWidth(cueMark_width);
			cueMark.setHeight(cueMark_height);
			cueMark.setyPos(cueMark_yPos);

			ImpositionBook impositionBook = ImpositionBook.generateImpositionBook(book);
			impositionBook.setFilePath(inputFile);
			impositionBook.setDocId(book.getMetadata().getBarcode());
			impositionBook.setTempOutputFilePath(File.createTempFile(book.getBookId(), ".pdf").getAbsolutePath());
			impositionBook.setOutputFilePath(outputfilepath);
			job.setBook(impositionBook);
			
			Date publishDate = book.getMetadata().getPublishDate();
			if(publishDate != null && depotLegal){
				SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy", Locale.FRANCE);
				job.setDepotLegal(format.format(publishDate));
			}
			
			job.setHunkelerCueMark(cueMark);
			job.setHunkelerDatamatrix(datamatrix);
			
			if (!hunkelerLine.equalsIgnoreCase(ALL)){
				String topMarginKey 		= "com.epac.composer."+hunkelerLine+".topmargin";
				String bottomMarginKey		= "com.epac.composer."+hunkelerLine+".bottommargin";
				
				float	top_margin 			= Float.parseFloat(System.getProperty(topMarginKey));
				float	bottom_margin 		= Float.parseFloat(System.getProperty(bottomMarginKey));
				
				job.setTopMargin(top_margin);
				job.setBottomMargin(bottom_margin);
			}
			
		}catch(Exception e){
			throw new ComposerException("Error while parsing values from config file... ",e);
		}

		return job;
	}
	
	public static ImpositionJob setUpImpositionBook (String HunkelerLine, com.epac.om.api.book.Book book, String inputFile, String outputfilepath ) throws ComposerException{
		ImpositionJob job = ImpositionJob.create(book, inputFile, outputfilepath, HunkelerLine);
			
		job.setHunkelerLine(HunkelerLine);
		
		String topMarginKey 		= "com.epac.composer."+HunkelerLine+".topmargin";
		String bottomMarginKey		= "com.epac.composer."+HunkelerLine+".bottommargin";
		
		job.setTopMargin(Float.parseFloat(System.getProperty(topMarginKey)));
		job.setBottomMargin(Float.parseFloat(System.getProperty(bottomMarginKey)));
		
		return job;
	}
	
	/**
	 * 
	 * @param book
	 * @return
	 * @throws ComposerException
	 */
	public static ImpositionJob create (com.epac.om.api.book.Book book, String inputFile, String outputfilepath) throws ComposerException {
		ImpositionJob job = new ImpositionJob();

		try{
			String 	jdfTemplatePath 	= System.getProperty(Constants.IMPOSITION_JDF_TEMPLATE_PATH);
			String 	markerPdfPath 		= System.getProperty(Constants.IMPOSITION_REFRESHBAR_PATH);
			String  hunkelerLine 		= System.getProperty(Constants.IMPOSITION_HUNKELER_LINE);
			String  perforation 		= System.getProperty(Constants.IMPOSITION_POP_PERFORATION, "0");
			
			float 	rollWidth 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_ROLL_WIDTH));			
			int	customerCode 			= Integer.parseInt(System.getProperty(Constants.IMPOSITION_CUSTOMER_CODE, "-1"));
			float	logoWidth 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_CUSTOMER_LOGO_WIDTH));
			float	logoHeight 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_CUSTOMER_LOGO_HEIGHT));
			float	milling 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_MILLING));
			float	innerMilling 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_INNER_MILLING));
			float	triming 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_TRIMING));
			
			float	bleed 				= Float.parseFloat(System.getProperty(Constants.IMPOSITION_BLEED));
						
			float	nonprinting 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_NON_PRINTING_AREA));

			float	ocMargin 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_OUTPUTCONTROLLER_MARGIN));
			
			float	datamatrix_xPos 	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_DATAMATRIX_XPOS));
			float	datamatrix_yPos 	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_DATAMATRIX_YPOS));
			float	datamatrix_width 	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_DATAMATRIX_WIDTH));
			float	datamatrix_height 	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_DATAMATRIX_HEIGHT));			
			//float	printSheetHeight	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_PRINTER_SHEET_HEIGHT));
			float	printSheetType		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_PRINTER_SHEET_TYPE));
			boolean depotLegal			= Boolean.parseBoolean(System.getProperty(Constants.IMPOSITION_ADD_DEPOT_LEGAL));
			byte	impositionType		= Byte.parseByte(System.getProperty(Constants.IMPOSITION_TYPE));
			
			boolean isRTL			= Boolean.parseBoolean(System.getProperty(Constants.IMPOSITION_RIGHT_TO_LEFT));
			
			float printSheetHeight	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_PRINTER_SHEET_STANDARD_HEIGHT, "0.0"));		
			float bigSheetHeightS1  = Float.parseFloat(System.getProperty(Constants.IMPOSITION_PRINTER_S1_HEIGHT, "0.0"));	
			float bigSheetHeightS2  = Float.parseFloat(System.getProperty(Constants.IMPOSITION_PRINTER_S2_HEIGHT, "0.0"));			
			float bigSheetHeightS3  = Float.parseFloat(System.getProperty(Constants.IMPOSITION_PRINTER_S3_HEIGHT, "0.0"));
		
			job.setStandardBigSheetHeight(printSheetHeight);
			job.setBigSheetHeightS1(bigSheetHeightS1);
			job.setBigSheetHeightS2(bigSheetHeightS2);
			job.setBigSheetHeightS3(bigSheetHeightS3);
			
			
			// convert from μm to mm (x 10e-3)
			float	paperthikness	= (float)book.getMetadata().getTextPaperType().getPaperThickness() / 1000; //Float.parseFloat(System.getProperty(Constants.IMPOSITION_PAPER_THIKNESS));

			float	cueMark_yPos;
			float	cueMark_width;
			float	cueMark_height;
			
			if (hunkelerLine.equalsIgnoreCase(PL) || hunkelerLine.equalsIgnoreCase(PB))
				cueMark_yPos 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_POP_CUEMARK_YPOS));
			else
				cueMark_yPos 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_CUEMARK_YPOS));
			
			if (hunkelerLine.equalsIgnoreCase(PL) || hunkelerLine.equalsIgnoreCase(PB))
				cueMark_width		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_POP_CUEMARK_WIDTH));
			else
				cueMark_width		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_CUEMARK_WIDTH));
			
			if (hunkelerLine.equalsIgnoreCase(PL) || hunkelerLine.equalsIgnoreCase(PB))
				cueMark_height 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_POP_CUEMARK_HEIGHT));
			else
				cueMark_height 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_CUEMARK_HEIGHT));
			

			job.setRTL(isRTL);
			
			job.setHunkelerLine(hunkelerLine);
			
			job.setPerforation(perforation);
			
			job.setBleed(bleed);
			
			job.setJdfTemplatePath(jdfTemplatePath);
			job.setMarkerPdfPath(markerPdfPath);
			
			job.setPaperthikness(paperthikness);
			job.setPrintSheetHeight(printSheetHeight);
			
			job.setImpositionType(getImpositionTypeFromConfig(impositionType));
			
			job.setCustomerCode(customerCode);
			job.setLogoWidth(logoWidth);
			job.setLogoHeight(logoHeight);
			
			job.setNonprintingarea(nonprinting);			
			job.setMilling(milling);
			job.setInnerMilling(innerMilling);
			job.setTriming(triming);
			job.setBigSheet(printSheetType == 1);
			job.setOutputControllerMargin(ocMargin);
			
			job.setSheetWidth(rollWidth);
			
			Datamatrix datamatrix = new Datamatrix();
			datamatrix.setWidth(datamatrix_width);
			datamatrix.setHeight(datamatrix_height);
			datamatrix.setxPos(datamatrix_xPos);
			datamatrix.setyPos(datamatrix_yPos);
			
			CueMark cueMark = new CueMark();
			cueMark.setWidth(cueMark_width);
			cueMark.setHeight(cueMark_height);
			cueMark.setyPos(cueMark_yPos);

			ImpositionBook impositionBook = ImpositionBook.generateImpositionBook(book);
			impositionBook.setFilePath(inputFile);
			impositionBook.setDocId(book.getMetadata().getBarcode());
			impositionBook.setTempOutputFilePath(File.createTempFile(book.getBookId(), ".pdf").getAbsolutePath());
			impositionBook.setOutputFilePath(outputfilepath);
			job.setBook(impositionBook);
			
			Date publishDate = book.getMetadata().getPublishDate();
			if(publishDate != null && depotLegal){
				SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy", Locale.FRANCE);
				job.setDepotLegal(format.format(publishDate));
			}
			
			job.setHunkelerCueMark(cueMark);
			job.setHunkelerDatamatrix(datamatrix);
			
			
			if (!hunkelerLine.equalsIgnoreCase(ALL)){
				String topMarginKey 		= "com.epac.composer."+hunkelerLine+".topmargin";
				String bottomMarginKey		= "com.epac.composer."+hunkelerLine+".bottommargin";
				
				float	top_margin 			= Float.parseFloat(System.getProperty(topMarginKey));
				float	bottom_margin 		= Float.parseFloat(System.getProperty(bottomMarginKey));
				
				job.setTopMargin(top_margin);
				job.setBottomMargin(bottom_margin);
			}

		}catch(Exception e){
			throw new ComposerException("Error while parsing values from config file... ",e);
		}

		return job;
	}
	
	/**
	 * 
	 * @param book
	 * @param inputFile
	 * @param outputfilepath
	 * @return
	 * @throws ComposerException
	 */
	public static ImpositionJob create (com.epac.om.api.book.Book book, String inputFile, String outputfilepath, String hunkelerLine) throws ComposerException {
		ImpositionJob job = new ImpositionJob();

		try{
			String 	jdfTemplatePath 	= System.getProperty(Constants.IMPOSITION_JDF_TEMPLATE_PATH);
			String 	markerPdfPath 		= System.getProperty(Constants.IMPOSITION_REFRESHBAR_PATH);
			//String  hunkelerLine 		= System.getProperty(Constants.IMPOSITION_HUNKELER_LINE);
			String  perforation 		= System.getProperty(Constants.IMPOSITION_POP_PERFORATION, "0");
			
			float 	rollWidth 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_ROLL_WIDTH));			
			int	customerCode 			= Integer.parseInt(System.getProperty(Constants.IMPOSITION_CUSTOMER_CODE, "-1"));
			float	logoWidth 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_CUSTOMER_LOGO_WIDTH));
			float	logoHeight 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_CUSTOMER_LOGO_HEIGHT));
			float	milling 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_MILLING));
			float	innerMilling 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_INNER_MILLING));
			float	triming 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_TRIMING));
			
			float	bleed 				= Float.parseFloat(System.getProperty(Constants.IMPOSITION_BLEED));
						
			float	nonprinting 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_NON_PRINTING_AREA));

			float	ocMargin 			= Float.parseFloat(System.getProperty(Constants.IMPOSITION_OUTPUTCONTROLLER_MARGIN));
			
			float	datamatrix_xPos 	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_DATAMATRIX_XPOS));
			float	datamatrix_yPos 	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_DATAMATRIX_YPOS));
			float	datamatrix_width 	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_DATAMATRIX_WIDTH));
			float	datamatrix_height 	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_DATAMATRIX_HEIGHT));			
			//float	printSheetHeight	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_PRINTER_SHEET_HEIGHT));
			float	printSheetType		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_PRINTER_SHEET_TYPE));
			boolean depotLegal			= Boolean.parseBoolean(System.getProperty(Constants.IMPOSITION_ADD_DEPOT_LEGAL));
			byte	impositionType		= Byte.parseByte(System.getProperty(Constants.IMPOSITION_TYPE));
			
			boolean isRTL			= Boolean.parseBoolean(System.getProperty(Constants.IMPOSITION_RIGHT_TO_LEFT));
			
			float printSheetHeight	= Float.parseFloat(System.getProperty(Constants.IMPOSITION_PRINTER_SHEET_STANDARD_HEIGHT, "0.0"));		
			float bigSheetHeightS1  = Float.parseFloat(System.getProperty(Constants.IMPOSITION_PRINTER_S1_HEIGHT, "0.0"));	
			float bigSheetHeightS2  = Float.parseFloat(System.getProperty(Constants.IMPOSITION_PRINTER_S2_HEIGHT, "0.0"));			
			float bigSheetHeightS3  = Float.parseFloat(System.getProperty(Constants.IMPOSITION_PRINTER_S3_HEIGHT, "0.0"));
		
			job.setStandardBigSheetHeight(printSheetHeight);
			job.setBigSheetHeightS1(bigSheetHeightS1);
			job.setBigSheetHeightS2(bigSheetHeightS2);
			job.setBigSheetHeightS3(bigSheetHeightS3);
			
			
			// convert from μm to mm (x 10e-3)
			float	paperthikness	= (float)book.getMetadata().getTextPaperType().getPaperThickness() / 1000; //Float.parseFloat(System.getProperty(Constants.IMPOSITION_PAPER_THIKNESS));

			float	cueMark_yPos;
			float	cueMark_width;
			float	cueMark_height;
			
			if (hunkelerLine.equalsIgnoreCase(PL) || hunkelerLine.equalsIgnoreCase(PB))
				cueMark_yPos 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_POP_CUEMARK_YPOS));
			else
				cueMark_yPos 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_CUEMARK_YPOS));
			
			if (hunkelerLine.equalsIgnoreCase(PL) || hunkelerLine.equalsIgnoreCase(PB))
				cueMark_width		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_POP_CUEMARK_WIDTH));
			else
				cueMark_width		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_CUEMARK_WIDTH));
			
			if (hunkelerLine.equalsIgnoreCase(PL) || hunkelerLine.equalsIgnoreCase(PB))
				cueMark_height 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_POP_CUEMARK_HEIGHT));
			else
				cueMark_height 		= Float.parseFloat(System.getProperty(Constants.IMPOSITION_HUNKELER_CUEMARK_HEIGHT));
			

			job.setRTL(isRTL);
			
			job.setHunkelerLine(hunkelerLine);
			
			job.setPerforation(perforation);
			
			job.setBleed(bleed);
			
			job.setJdfTemplatePath(jdfTemplatePath);
			job.setMarkerPdfPath(markerPdfPath);
			
			job.setPaperthikness(paperthikness);
			job.setPrintSheetHeight(printSheetHeight);
			
			job.setImpositionType(getImpositionTypeFromConfig(impositionType));
			
			job.setCustomerCode(customerCode);
			job.setLogoWidth(logoWidth);
			job.setLogoHeight(logoHeight);
			
			job.setNonprintingarea(nonprinting);			
			job.setMilling(milling);
			job.setInnerMilling(innerMilling);
			job.setTriming(triming);
			job.setBigSheet(printSheetType == 1);
			job.setOutputControllerMargin(ocMargin);
			
			job.setSheetWidth(rollWidth);
			
			Datamatrix datamatrix = new Datamatrix();
			datamatrix.setWidth(datamatrix_width);
			datamatrix.setHeight(datamatrix_height);
			datamatrix.setxPos(datamatrix_xPos);
			datamatrix.setyPos(datamatrix_yPos);
			
			CueMark cueMark = new CueMark();
			cueMark.setWidth(cueMark_width);
			cueMark.setHeight(cueMark_height);
			cueMark.setyPos(cueMark_yPos);

			ImpositionBook impositionBook = ImpositionBook.generateImpositionBook(book);
			impositionBook.setFilePath(inputFile);
			impositionBook.setDocId(book.getMetadata().getBarcode());
			impositionBook.setTempOutputFilePath(File.createTempFile(book.getBookId(), ".pdf").getAbsolutePath());
			impositionBook.setOutputFilePath(outputfilepath);
			job.setBook(impositionBook);
			
			Date publishDate = book.getMetadata().getPublishDate();
			if(publishDate != null && depotLegal){
				SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy", Locale.FRANCE);
				job.setDepotLegal(format.format(publishDate));
			}
			
			job.setHunkelerCueMark(cueMark);
			job.setHunkelerDatamatrix(datamatrix);
			
			
			if (!hunkelerLine.equalsIgnoreCase(ALL)){
				String topMarginKey 		= "com.epac.composer."+hunkelerLine+".topmargin";
				String bottomMarginKey		= "com.epac.composer."+hunkelerLine+".bottommargin";
				
				float	top_margin 			= Float.parseFloat(System.getProperty(topMarginKey));
				float	bottom_margin 		= Float.parseFloat(System.getProperty(bottomMarginKey));
				
				job.setTopMargin(top_margin);
				job.setBottomMargin(bottom_margin);
			}

		}catch(Exception e){
			throw new ComposerException("Error while parsing values from config file... ",e);
		}

		return job;
	}
	
	
	
	public void setDepotLegal(String depotLegal) {
		this.depotLegal = depotLegal;
	}
	public String getDepotLegal() {
		return depotLegal;
	}

	public void setInnerMilling(float im){
		this.innerMilling = im;
	}
	public float getInnerMilling() {
		return innerMilling;
	}

	public String getHunkelerLine() {
		return hunkelerLine;
	}

	public void setHunkelerLine(String hunkelerLine) {
		this.hunkelerLine = hunkelerLine;
	}

	public boolean isPloughFolderLine() {
		return PF.equals(hunkelerLine);
	}
	
	public boolean isFlyFolderLine() {
		return FF.equals(hunkelerLine);
	}
	
	public boolean isPopLine() {
		return PL.equals(hunkelerLine) || PB.equals(hunkelerLine);
	}
	
	public boolean isPopLinePL() {
		return PL.equals(hunkelerLine);
	}
	
	public boolean isPopLinePB() {
		return PB.equals(hunkelerLine);
	}
	
	public boolean isAllLines() {
		return ALL.equals(hunkelerLine);
	}
	
	public String getBookDirection (){
		if (isRTL)
			return Constants.BOOK_RTL;
		
		else 
			return Constants.BOOK_LTR;
	}

	public boolean isEpacMPb() {
		return isEpacMPb;
	}

	public void setEpacMPb(boolean isEpacMPb) {
		this.isEpacMPb = isEpacMPb;
	}

	public boolean isTextWithBleed() {
		return isTextWithBleed;
	}

	public void setTextWithBleed(boolean isTextWithBleed) {
		this.isTextWithBleed = isTextWithBleed;
	}


}
