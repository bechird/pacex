package com.epac.imposition.bookblock;



import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.AccessDeniedException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import com.epac.imposition.config.Constants;
import com.epac.imposition.config.LogUtils;
import com.epac.imposition.model.Composer;
import com.epac.imposition.utils.CompositionScheme;
import com.epac.imposition.utils.Format;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

public class TextComposer extends Composer {

	public static final String EPAC	= "EPAC";

	public static final String FOLD_ID_TWO_UP 			= "2";
	public static final String FOLD_ID_THREE_UP 		= "3";

	private Float xShiftEven 		= 0f;			
	private Float xShiftOdd 		= 0f;			
	private Float yShift 			= 0f;	
	static public Float heightMInt =0f;
	static public Float widthMInt =0f;
	private String prefixHeight     = "";

	private CompositionScheme composition;

	private float SheetHeight;
	
	public String getPrefixHeight() {
		return prefixHeight;
	}

	public void setPrefixHeight(String prefixHeight) {
		this.prefixHeight = prefixHeight;
	}

	List<Integer> upperSide;
	List<Integer> lowerSide;


	/**
	 * 
	 * @param arg
	 * @return
	 */
	public String toHex(String arg) {
		return String.format("%x", new BigInteger(1, arg.getBytes()));
	}

	public static String getDayOfYear(){
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_YEAR);
		String strDay = String.valueOf(day);

		if(day < 10)
			strDay = "00".concat(strDay);
		else if(day < 100)
			strDay = "0".concat(strDay);
		return strDay;

	}

	/**
	 * 
	 * @return
	 */
	public static String getWeekNbr (){
		Date nowdate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(nowdate);
		int week = cal.get(Calendar.WEEK_OF_YEAR);

		String strWeek = String.valueOf(week);
		StringBuffer sb = new StringBuffer();
		for(int i=strWeek.length() ; i<2; i++) {
			sb.append('0');
		}
		sb.append(strWeek);

		return sb.toString();
	}

	/**
	 * 
	 * @return
	 */
	public static String getYearNbr (){

		DateFormat df = new SimpleDateFormat("yy"); // Just the year, with 2 digits
		String formattedDate = df.format(Calendar.getInstance().getTime());

		StringBuffer sb = new StringBuffer();
		for(int i=formattedDate.length() ; i<2; i++) {
			sb.append('0');
		}
		sb.append(formattedDate);

		return sb.toString();
	}



	/**
	 * 
	 * @return 
	 */
	public static String generatePrintNbr (){
		StringBuffer sb = new StringBuffer();

		sb.append(String.format("%x", new BigInteger(1, "EPAC".getBytes())));

		//sb.append("01");

		sb.append(getDayOfYear());

		sb.append(getYearNbr());

		System.out.println("generatePrintNbr : " + sb.toString());

		return sb.toString();
	}


	public TextComposer(ImpositionJob job) throws ComposerException {
		super(job); 
		calculateMax3UpWidth();
		calculateMax4UpWidth();
	}

	public TextComposer(ImpositionJob job, float max3Up) throws ComposerException {
		super(job); 
		max3UpWidth = max3Up; 
	}


	public void setMax3UpWidth(float max3Up) { max3UpWidth = max3Up; }
	public static float getMax3UpWidth() { return max3UpWidth; }
	public static float getMax4UpWidth() { return max4UpWidth; }
	public Float getContentRotation() { return rotation; }

	/**
	 * calculate max 3UP page width
	 */
	public void calculateMax3UpWidth () {
		float textArea = 0.0f;
		if (job.isPopLine())
			textArea = job.getSheetWidth() - job.getTriming()*3;
		else
			textArea = job.getSheetWidth() - job.getMilling()*2 - job.getNonprintingarea() - job.getInnerMilling();
		max3UpWidth = textArea/3;
		System.out.println("max 3UP width = " + max3UpWidth);
	}

	public void calculateMax4UpWidth () {
		float textArea = job.getSheetWidth() - job.getMilling()*4 - job.getNonprintingarea();
		max4UpWidth = textArea/4;
		System.out.println("max 4UP width = " + max4UpWidth);
	}



	/**
	 * 
	 */
	public void compose() throws ComposerException {
		System.out.println("Start composing " + srcFilename + " - (result file will be: " + composedFilename + ")"); 
		try {
			//PL:test if pl with bleed bypass else it's without bleed and crop
			if (!job.isPopLinePL())
			{cropPdf(job.getBook().getFilePath(), job.getBook().getTempOutputFilePath());

			//Files.copy(Paths.get(job.getBook().getTempOutputFilePath()), new FileOutputStream(job.getBook().getFilePath()));
			FileUtils.copyFile(new File(job.getBook().getTempOutputFilePath()), new File(job.getBook().getFilePath()));

			//check last page of the book if it's empty otherwise add blank page for "print number"
			if(job.getDepotLegal() != null){
				boolean imprintpage = checkImprintPage(job.getBook().getFilePath(), job.getBook().getTempOutputFilePath());
				if (imprintpage){
					cropPdf(job.getBook().getTempOutputFilePath(), job.getBook().getFilePath());

					if (job.isRTL()){

						String TempFile = File.createTempFile(job.getBook().getDocId(), ".temp.text.pdf").getAbsolutePath();
						PdfReader readerTemp = new PdfReader(job.getBook().getFilePath());
						PdfStamper stamperTemp = new PdfStamper(readerTemp, new FileOutputStream(TempFile));

						//if job is RTL deplace 2 pages from the end 
						int n = readerTemp.getNumberOfPages() + 1;
						readerTemp.selectPages(String.format("%d, 1-%d", n, n-1));
						readerTemp.selectPages(String.format("%d, 1-%d", n, n-1));

						try{stamperTemp.close();}catch(Exception e){}
						try{readerTemp.close();}catch(Exception e){}

						FileUtils.copyFile(new File(TempFile), new File(job.getBook().getFilePath()));
					}
				}
			}

			}
			openReader(job.getBook().getFilePath());

			getItemSettingsFromPdf();

			calculateComposingParams();

			createComposedFile();
			
			if (job.isPopLine())
				setupPoplinePagesOrder();

			System.out.println("Finished composing producible item " + composedFilename + ")");

		} catch(Exception e) {
			LogUtils.error("Error occured while imposing file: "+ job.getBook().getFilePath(), e);
			throw new ComposerException(e);		
		}finally{
			try{reader.close();}catch(Exception e){}
		}
	}


	/**
	 * 
	 * @param src
	 * @param output
	 * @throws Exception
	 */
	public boolean checkImprintPage(String src, String output) throws Exception{
		PdfReader reader = new PdfReader(src);

		int pageToCheck = job.isRTL() ? 1 : reader.getNumberOfPages();

		// grab last page & check if it's empty, otherwise add an empty one for imprint.
		PdfDictionary pageDict = reader.getPageN(pageToCheck);
		PdfDictionary resDict = (PdfDictionary) pageDict.getAsDict( PdfName.RESOURCES );

		boolean noFontsOrImages = true;
		if (resDict != null) {
			noFontsOrImages = resDict.get( PdfName.FONT ) == null 
					&& resDict.get( PdfName.XOBJECT ) == null;
		}

		LogUtils.debug("is last page empty? "+ noFontsOrImages);
		// add one page for imprint.
		if(!noFontsOrImages){
			LogUtils.debug("last page is not empty, adding new page for \"Imprimé en France\"");	

			int n = reader.getNumberOfPages() + 1;
			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(output));
			stamper.insertPage(n, reader.getPageSizeWithRotation(1));

			if (job.isRTL()) // if job is RTL we need to add 2 pages to keep odd and even order 
				stamper.insertPage(n+1, reader.getPageSizeWithRotation(1));

			try{stamper.close();}catch(Exception e){}
			try{reader.close();}catch(Exception e){}

			return true;									
		}
		try{reader.close();}catch(Exception e){}
		return false;
	}


	public boolean reorderPDF(String input, String dest) throws DocumentException, IOException {

		Document doc = null;
		PdfWriter writer = null;
		PdfReader resultReader = null;
		try {
			resultReader = new PdfReader(new FileInputStream(input));
			Rectangle rectFile = new Rectangle(resultReader.getPageSize(1).getWidth(), resultReader.getPageSize(1).getHeight());
			doc = new Document(rectFile);
			FileOutputStream file = new FileOutputStream(dest);
			writer = PdfWriter.getInstance(doc, file);
			doc.open();

			PdfContentByte cb = writer.getDirectContent();
			for (int i = resultReader.getNumberOfPages(); i >= 1; i--) {
				doc.newPage();
				writer.setPageEmpty(false);				
				PdfImportedPage page = writer.getImportedPage(resultReader, i);
				Float[] tm = new Float[6];
				tm[0] = 1.0f; // a
				tm[1] = 0.0f; // b
				tm[2] = 0.0f; // c
				tm[3] = 1.0f; // d 
				tm[4] = 0.0f; // e
				tm[5] = 0.0f;

				cb.addTemplate(page, tm[0], tm[1], tm[2], tm[3], tm[4], tm[5]);
			}

			return true;
		} catch (Exception e) {
			System.err.println("Exception ..." + e);
			e.printStackTrace();
			return false;
		}finally{
			doc.close();
			writer.close();
			resultReader.close();
		}
	}

	/**
	 * 
	 * @throws ComposerException
	 */
	private void calculateComposingParams() throws ComposerException {
		try {

			setComposingScheme();
			setPageSize();				
			setPageShift(); 
			setRotation();		
			setFinalNoOfPages();

			job.getBook().setBookWidth(paperWidth);
			job.getBook().setBookHeight(paperHeight);

			System.out.println("Calculated composing parameters. ");  
			System.out.println("Finished calculating composing parameters. ");  

		} catch(Exception e) {
			String msg = "Cannot calculate composing parameters. e: " + e.getMessage();
			LogUtils.error("Cannot calculate composing parameters", e);
			throw new ComposerException(msg);
		}
	}

	/**
	 * 
	 * @throws ComposerException
	 */
	private void setComposingScheme() throws ComposerException {
		if (job.isPopLine()) {
			composition = CompositionScheme.TWO_UP;

			if(job.getBook().getBookWidth() < max3UpWidth && job.isPopLinePB()) 
				composition = CompositionScheme.THREE_UP;
		}
		else{
			if(job.isFlyFolderLine())
				composition = CompositionScheme.THREE_UP;
			else
				composition = CompositionScheme.FOUR_UP;

			if(job.getBook().getBookWidth() > max4UpWidth) 
				composition = CompositionScheme.THREE_UP;

			if(job.getBook().getBookWidth() > max3UpWidth) 
				composition = CompositionScheme.TWO_UP;
		}

		job.setComposition(composition);
	}


	public Rectangle lastTrimbox;

	public static boolean compareRectangles(Rectangle rect1, Rectangle rect2) {
		float toleranceInPoints = Format.mm2points(1.0f).floatValue();

		return 	  (!(Math.abs(rect1.getHeight()  	- rect2.getHeight()) 	> toleranceInPoints)
				&& !(Math.abs(rect1.getWidth() 		- rect2.getWidth()) 	> toleranceInPoints)
				&& !(Math.abs(rect1.getTop() 		- rect2.getTop()) 		> toleranceInPoints)
				&& !(Math.abs(rect1.getBottom() 	- rect2.getBottom()) 	> toleranceInPoints)
				&& !(Math.abs(rect1.getLeft() 		- rect2.getLeft()) 		> toleranceInPoints)
				&& !(Math.abs(rect1.getRight() 		- rect2.getRight()) 	> toleranceInPoints));
	}
	/**
	 * 
	 * @param pdfFilePath
	 * @param filename
	 * @throws DocumentException
	 * @throws IOException
	 * @throws ComposerException
	 */
	public void cropPdf(String pdfFilePath, String filename) throws DocumentException, IOException, ComposerException {
		File inputFile = new File(pdfFilePath);
		if(!inputFile.exists()){
			throw new FileNotFoundException(inputFile.getName());
		}

		if(!inputFile.canRead()){
			throw new AccessDeniedException(inputFile.getName());
		}

		float textBleed	= Float.parseFloat(System.getProperty(Constants.TEXT_BLEED_VALUE));
		PdfReader reader = null;
		PdfStamper stamper = null;
		try {
			
			//replace "new FileInputStream(inputFile)" by "pdfFilePath" to avoid out of memory exception for big files from Cengage
			//FileInputStream causes "out of memory" exception when it try to open the file
			reader = new PdfReader(pdfFilePath);
			for (int i = 1; i <= reader.getNumberOfPages(); i++) {

				PdfDictionary pdfDictionary = reader.getPageN(i);
				PdfArray bleedArray = new PdfArray();
				PdfArray cropArray = new PdfArray(); 

				Rectangle trimbox = reader.getBoxSize(i, "trim");
				Rectangle bleedbox = reader.getBoxSize(i, "trim");


				if (trimbox != null){
					cropArray.add(new PdfNumber(trimbox.getLeft()));
					cropArray.add(new PdfNumber(trimbox.getBottom()));
					cropArray.add(new PdfNumber(trimbox.getLeft() + trimbox.getWidth()));
					cropArray.add(new PdfNumber(trimbox.getBottom() + trimbox.getHeight()));		
				}else if (reader.getBoxSize(i-1, "trim") != null){
					trimbox = reader.getBoxSize(i-1, "trim");
					bleedbox = reader.getBoxSize(i-1, "trim");

					cropArray.add(new PdfNumber(trimbox.getLeft()));
					cropArray.add(new PdfNumber(trimbox.getBottom()));
					cropArray.add(new PdfNumber(trimbox.getLeft() + trimbox.getWidth()));
					cropArray.add(new PdfNumber(trimbox.getBottom() + trimbox.getHeight()));
				}else if (reader.getBoxSize(i+1, "trim") != null){
					trimbox = reader.getBoxSize(i+1, "trim");
					bleedbox = reader.getBoxSize(i+1, "trim");

					cropArray.add(new PdfNumber(trimbox.getLeft()));
					cropArray.add(new PdfNumber(trimbox.getBottom()));
					cropArray.add(new PdfNumber(trimbox.getLeft() + trimbox.getWidth()));
					cropArray.add(new PdfNumber(trimbox.getBottom() + trimbox.getHeight()));
				}
				else{					
					throw new ComposerException("Neither bleed or trim box are set for " + getSrcFilename() + " for the page "+i);
				}
				
				if (reader.getBoxSize(i, "bleed") == null || reader.getBoxSize(i, "bleed").equals(reader.getBoxSize(i, "trim"))) {
					bleedbox.setRight(trimbox.getRight());
					bleedbox.setLeft(trimbox.getLeft());
					bleedbox.setTop(trimbox.getTop());
					bleedbox.setBottom(trimbox.getBottom());
				}
				else {
					if (i%2 == 1){
						bleedbox.setRight(trimbox.getRight() + Format.mm2points(textBleed).floatValue());
					}else{	
						bleedbox.setLeft(trimbox.getLeft() - Format.mm2points(textBleed).floatValue());
					}

					bleedbox.setTop(trimbox.getTop() + Format.mm2points(textBleed).floatValue());
					bleedbox.setBottom(trimbox.getBottom() - Format.mm2points(textBleed).floatValue());
				}


				bleedArray.add(new PdfNumber(bleedbox.getLeft()));
				bleedArray.add(new PdfNumber(bleedbox.getBottom()));
				bleedArray.add(new PdfNumber(bleedbox.getLeft() + bleedbox.getWidth()));
				bleedArray.add(new PdfNumber(bleedbox.getBottom() + bleedbox.getHeight()));


				pdfDictionary.put(PdfName.CROPBOX, bleedArray);
				pdfDictionary.put(PdfName.MEDIABOX, bleedArray);
				pdfDictionary.put(PdfName.TRIMBOX, cropArray);
				pdfDictionary.put(PdfName.BLEEDBOX, bleedArray);
			}

			stamper = new PdfStamper(reader, new FileOutputStream(filename));
		}catch(Exception e){
		}finally {
			try{stamper.close();}catch(Exception e){}
			try{reader.close();}catch(Exception e){}
		}


	}

	/** 
	 	Creates the composed PDF. Inserts pages from original PDF, adds additional white pages
		if necessary. Page count %4 (2up) or %6 (3up) must equal 0. Adds barcode for 
		post printing processing (Hunkeler).
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	private void createComposedFile() throws ComposerException, DocumentException, IOException {
		System.out.println("Creating composed file ...");
		float textBleed	= Float.parseFloat(System.getProperty(Constants.TEXT_BLEED_VALUE));

		Document doc = null;
		PdfWriter writer = null;
		try {
			// create a new document with the new media size
			doc = new Document(getBoxSizeInPoints(mediaBox));
			// create a new Writer object that writes content to the new PDF  
			FileOutputStream file = new FileOutputStream(composedFilename);
			System.out.println("Creating composed file created ...");
			writer = PdfWriter.getInstance(doc, file);
			System.out.println("Creating composed file processing ...");
			// open the document
			doc.open();
		

			// Retrieve an instance of the ContentByte to add changes to the imported PDF pages
			// PdfImportedPages cannot be changed, the direct content is needed to do this.
			PdfContentByte cb = writer.getDirectContent();
			
			int pagesPerSig = composition.getPagesPerSheet();

			int upperCount = 0;
			int lowerCount = 0;

			int surfaces = job.isPopLine() ? originalNoOfPages : job.getBook().getFinalPageCount() /pagesPerSig;
			if (surfaces%2 == 1)
				surfaces++;

			int length = surfaces * (pagesPerSig/2);

			int[] upperSide = new int[length];
			int[] lowerSide = new int[length];

			Rectangle orgbox = reader.getBoxSize(1, "trim");
			Rectangle bleedbox = reader.getBoxSize(1, "bleed");
			float bleedwidth = (bleedbox.getWidth() - orgbox.getWidth());
			if (job.isTextWithBleed() && job.isPopLinePL())	
			 bleedwidth = (bleedbox.getWidth() - orgbox.getWidth())/2;
			float bleedheight = (bleedbox.getHeight() - orgbox.getHeight())/2;
			widthMInt = doc.getPageSize().getWidth();
			heightMInt = doc.getPageSize().getHeight();
			yShift = Format.points2mm(((heightMInt)-getOrigBleedBox().getHeight())/2).floatValue();  
			//xShiftEven = (doc.getPageSize().getWidth()-bleedbox.getWidth())/2;
			//yShift = (doc.getPageSize().getHeight()-bleedbox.getHeight())/2;
			
//			if (bleedheight == 0)
//				bleedheight = 3.0f; 
//			if (bleedwidth == 0)
//				bleedwidth = 3.0f;
			
			Float[] tm = null;
			for(int i=1; i <= finalNoOfPages; i++) {
				doc.newPage();
				if (job.getComposition() == CompositionScheme.TWO_UP){
					/** Fly Folder Line	**/
					if(job.isFlyFolderLine()){
						if (i%4 == 2 || i%4 == 3){
							upperSide[upperCount] = i;
							upperCount++;
						}else if (i%4 == 0 || i%4 == 1){
							lowerSide[lowerCount] = i;
							lowerCount++;
						}
					}
					/** Plough Folder Line	**/
					else if (job.isPloughFolderLine()){ 
						if (i%4 == 2 || i%4 == 3){
							lowerSide[lowerCount] = i;
							lowerCount++;
						}else if (i%4 == 0 || i%4 == 1){
							upperSide[upperCount] = i;
							upperCount++;
						}
					}

				}else if (job.getComposition() == CompositionScheme.THREE_UP){
					if(job.isFlyFolderLine()){
						if (i%6 == 2 || i%6 == 5 || i%6 == 3){
							upperSide[upperCount] = i;
							upperCount++;
						}else if (i%6 == 4 || i%6 == 0 || i%6 == 1){
							lowerSide[lowerCount] = i;
							lowerCount++;
						}
					}else{
						if (i%6 == 3 || i%6 == 0 || i%6 == 1){
							upperSide[upperCount] = i;
							upperCount++;
						}else if (i%6 == 2 || i%6 == 5 || i%6 == 4){
							lowerSide[lowerCount] = i;
							lowerCount++;
						}
					}

				}else if (job.getComposition() == CompositionScheme.FOUR_UP){
					if (i%8 == 4 || i%8 == 5 || i%8 == 1 || i%8 == 0){
						upperSide[upperCount] = i;
						upperCount++;
					}else{
						lowerSide[lowerCount] = i;
						lowerCount++;
					}
				}


				tm = getPageTransformationMatrix(i);
				if(i <= originalNoOfPages) {			
					PdfImportedPage page = writer.getImportedPage(reader, i);
					cb.addTemplate(page, tm[0], tm[1], tm[2], tm[3], tm[4], tm[5]);
				} else{ 
					writer.setPageEmpty(false);

				}
				/*if (job.isPopLine() && job.isBigSheet() && i == finalNoOfPages) {
					float llx = doc.getPageSize().getWidth() - Format.mm2points(1.5f).floatValue();
					float urx = llx - Format.mm2points(job.getHunkelerCueMark().getWidth()).floatValue();
					float lly = Format.mm2points(job.getHunkelerCueMark().getyPos()).floatValue();
					float ury = lly + Format.mm2points(job.getHunkelerCueMark().getHeight()).floatValue();
					Rectangle rect = new Rectangle(llx, lly, urx, ury);
					rect.setBackgroundColor(new CMYKColor(1f, 1f, 1f, 1f));
					cb.rectangle(rect);
				}*/
				
				if (job.isRTL()){
					if (i % 2 == 0)
						setupCropMark(cb, i, bleedheight + Format.mm2points(0.5f).floatValue(), tm[4], tm[5] + bleedheight,
								tm[4] + orgbox.getWidth(), tm[5] + orgbox.getHeight() + bleedheight);
					else
						setupCropMark(cb, i, bleedheight + Format.mm2points(0.5f).floatValue(),  tm[4] + bleedwidth, tm[5] +bleedheight,
								tm[4] + bleedwidth + orgbox.getWidth(), tm[5] + orgbox.getHeight() + bleedheight);
				}else {

				float bleedH = bleedheight == 0 ? Format.mm2points(1.5f).floatValue() : bleedheight;
				
					if (i % 2 == 1)
					{	
						//paire
						if (job.isTextWithBleed() && job.isPopLinePL())
						{setupCropMark(cb, i, bleedH + Format.mm2points(0.5f).floatValue(), tm[4] + 9f, tm[5] + bleedheight,
								tm[4] + orgbox.getWidth() + bleedwidth, tm[5] + orgbox.getHeight() + bleedheight);}
						else 
							{setupCropMark(cb, i, bleedH + Format.mm2points(0.5f).floatValue(), tm[4], tm[5] + bleedheight,
									tm[4] + orgbox.getWidth(), tm[5] + orgbox.getHeight() + bleedheight);}
					}else{
						//impair
						if (job.isTextWithBleed() && job.isPopLinePL())
						{setupCropMark(cb, i, bleedH + Format.mm2points(0.5f).floatValue(),  tm[4] + bleedwidth, tm[5] +bleedheight,
								doc.getPageSize().getWidth() - 9.2232f, tm[5] + orgbox.getHeight() + bleedheight);}
						else{
							setupCropMark(cb, i, bleedH + Format.mm2points(0.5f).floatValue(),  tm[4] + bleedwidth, tm[5] +bleedheight,
									tm[4] + bleedwidth + orgbox.getWidth(), tm[5] + orgbox.getHeight() + bleedheight);
						}
				}
				}
				BaseFont helvetica = BaseFont.createFont("Gudea-Regular.ttf", BaseFont.CP1252, BaseFont.EMBEDDED);
				Font font = new Font(helvetica, 10, Font.NORMAL);
				font.setColor(new CMYKColor(0f, 0f, 0f, 1f));

				//Imprimé en France
				int imprintPage = job.isRTL() ? 1 : reader.getNumberOfPages();

				if (i== imprintPage && job.getDepotLegal() != null){
					FontFactory.defaultEmbedding = true;


					String text1 = "Achevé d'imprimer en France par EPAC Technologies";
					String text2 = "N° d'édition: " + generatePrintNbr();
					String text3 = "Dépôt légal: " .concat(job.getDepotLegal());

					float textWidth = font.getCalculatedBaseFont(true).getWidthPoint(text3, font.getCalculatedSize());
					float bleedWidth = Format.mm2points(job.getBook().getBookWidth() + textBleed).floatValue();

					float xshift = 0;
					if((i % 2) == 1) 
						xshift = xShiftOdd;
					else 
						xshift = xShiftEven + Format.points2mm(bleedwidth).floatValue();

					float y	= Format.mm2points(48.0f).floatValue();
					float x = Format.mm2points(xshift).floatValue() + (bleedWidth - Format.mm2points(job.getLogoWidth()).floatValue())/2;


					String logoPath = null;
					if (job.getCustomerCode() == Constants.CUSTOMER_SEJER)
						logoPath = "logo_SEJER_noir.jpg";
					else if (job.getCustomerCode() == Constants.CUSTOMER_SOGEDIF)
						logoPath = "logo_SOGEDIF_noir.jpg";

					if (logoPath != null){
						InputStream stream = TextComposer.class.getResourceAsStream("/"+logoPath);

						Image logo = null;
						if(stream != null){
							System.out.println("Image.getInstance stream : " + stream);
							BufferedImage image = ImageIO.read(stream);
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							ImageIO.write(image, "jpeg", baos);

							logo = Image.getInstance(baos.toByteArray());

						}else{
							System.out.println("Image.getInstance logoPath : " + logoPath);
							logo = Image.getInstance(logoPath);
						}

						if (logo != null){
							logo.scaleAbsolute(Format.mm2points(job.getLogoWidth()).floatValue(), Format.mm2points(job.getLogoHeight()).floatValue());
							logo.setAbsolutePosition(x - Format.mm2points(job.getLogoWidth()).floatValue()/2, y);
							logo.setAlignment(Element.ALIGN_CENTER);
							cb.addImage(logo);
						}

						y = logo.getHeight() + Format.mm2points(5.0f).floatValue();	
					}

					float w;

					w = font.getCalculatedBaseFont(true).getWidthPoint(text1, font.getCalculatedSize());
					x = (float) (xshift + (job.getBook().getBook().getMetadata().getWidth() - Format.points2mm(w).floatValue())/2);
					ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Paragraph(text1, font),
							Format.mm2points(x).floatValue(), y, 0.0f);

					w = font.getCalculatedBaseFont(true).getWidthPoint(text2, font.getCalculatedSize());
					x = (float) (xshift + (job.getBook().getBook().getMetadata().getWidth() - Format.points2mm(w).floatValue())/2);
					ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Paragraph(text2, font),
							Format.mm2points(x).floatValue(), y - Format.mm2points(7.0f).floatValue(), 0.0f);

					w = font.getCalculatedBaseFont(true).getWidthPoint(text3, font.getCalculatedSize());
					x = (float) (xshift + (job.getBook().getBook().getMetadata().getWidth() - Format.points2mm(w).floatValue())/2);
					ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Paragraph(text3, font),
							Format.mm2points(x).floatValue(), y - Format.mm2points(14.0f).floatValue(), 0.0f);

				}

				//MM barcode and imposition version
				
				//if the job is PB we can disable the MM datamatrix
				boolean MM_Enabled = Boolean.parseBoolean(System.getProperty(Constants.IMPOSITION_MM_DATAMATRIX_ENABLED));
				boolean enabled = true;
				if (job.isPopLinePB())
					enabled = MM_Enabled;
				
				if (i == 1 && enabled && !job.isPopLinePL()){
					float y = 2f;
					float x = 50f;

					if (job.isRTL()){
						y = job.getSheetHeight() -job.getHunkelerDatamatrix().getHeight() - 2f;
						x = job.getBook().getBookWidth() - job.getHunkelerDatamatrix().getWidth() - 50f;
					}

					String text = PdfComposer.getMullerMartiniBarcode(job.getBook().getDocId().concat(Constants.MATERIAL_TYPE_BOOKBLOCK), "0000", 
							job.getSheetHeight(), job.getPaperthikness()*(job.getBook().getFinalPageCount()/2));

					// place Binder datamatrix 
					PdfComposer.addDatamatrix(job, x, y, cb, text, false, 0.0f);

					float ypos = job.getBook().getBookHeight() - 5f;
					if (job.isRTL())
						ypos = 5f;

					float x1 = 5f;
					float x2 = 100f; //job.getBook().getBookWidth() + job.getTriming() +job.getMilling();

					String bookId   = String.valueOf(job.getBook().getBook().getId());
					String hunkeler = job.getHunkelerLine()+(job.isBigSheet()? "EM":"SM");

					ImpositionJob.addImpositionDate(cb, x1, x2, ypos, bookId, hunkeler ,true);
					
				}
			}


			job.setUpperSide(upperSide);
			job.setLowerSide(lowerSide);
		} catch(Exception e) {
			LogUtils.error("Error occured while imposing file: "+ composedFilename , e);
			throw new ComposerException(e);

		} finally {
			try {
				if(doc != null) 
					doc.close();
				if(writer != null)
					writer.close();
			} catch(Exception e) {
				System.err.println("Cannot close document/writer. e: " + e.getMessage());
			}
		}
	}


	private void setupPoplinePagesOrder() {	

		int upperCount = 0;
		int lowerCount = 0;

		int surfaces = originalNoOfPages;
		if (surfaces%2 == 1)
			surfaces++;

		int length = surfaces/2;

		System.out.println("*** setupPoplinePagesOrder ***");
		System.out.println("originalNoOfPages "+surfaces);
		System.out.println("length "+length);			

		int[] upperSide = new int[length];
		int[] lowerSide = new int[length];

		for(int i=1; i <= surfaces; i++) {				
			if (i%2 == 0){
				upperSide[upperCount] = i;
				upperCount++;
			}else {
				lowerSide[lowerCount] = i;
				lowerCount++;
			}

		}


		System.out.println(Arrays.toString(upperSide));
		System.out.println(Arrays.toString(lowerSide));
		System.out.println("***  ***");
		job.setUpperSide(upperSide);
		job.setLowerSide(lowerSide);
	}

	/**
	 * 
	 * @throws ComposerException
	 */
	private void setPageSize() throws ComposerException {
		if (job.isPopLine()){
			//paperWidth = job.getSheetWidth();

			if(composition == CompositionScheme.TWO_UP) {
				if (job.isPopLinePB())
					paperWidth = (job.getSheetWidth() - job.getMilling()*2)/2;
				else
					paperWidth = (job.getSheetWidth())/2;
			}			
			else if(composition == CompositionScheme.THREE_UP) 
				paperWidth = max3UpWidth;
		}else{
			if(composition == CompositionScheme.TWO_UP) 
				paperWidth = (job.getSheetWidth() - job.getMilling()*2)/2;
			else if(composition == CompositionScheme.THREE_UP) 
				paperWidth = max3UpWidth;
			else if(composition == CompositionScheme.FOUR_UP) 
				paperWidth = max4UpWidth;
			else
				throw new RuntimeException("Composition Schema is not set");
		}


		SheetHeight = (float) (job.getBook().getBookHeight() + job.getBottomMargin() + job.getTopMargin());

		if (job.isBigSheet()){			
			int sheetNbr = (int) (job.getPrintSheetHeight()/SheetHeight);
			
			if (prefixHeight.equalsIgnoreCase(ImpositionJob.S0))
				job.setPrintSheetHeight(sheetNbr * SheetHeight);
			
			paperHeight = job.getPrintSheetHeight()/sheetNbr;	

			job.setSheetPersig(sheetNbr);
		}else{
			paperHeight = SheetHeight;
			job.setSheetPersig(composition.getPagesPerSignature());
		}


		job.setSheetHeight(paperHeight);

		if (job.isBigSheet())
			job.updateOutputFilePath(job.getPrintSheetHeight());
		else
			job.updateOutputFilePath(paperHeight);


		mediaBox = new Rectangle(paperWidth.floatValue(), paperHeight.floatValue());

	}

	/**
	 * The pages of a book block are not symmetrical laid out on the normalized 
	 * page width (normalized page width 2up = 230.0mm, 3up=153.333mm).
	 * Instead we shift odd pages to the left and even pages to the right. 
	 */
	private void setPageShift() throws ComposerException {	

		if (job.isPopLine()){
			//			float leftRightMargin = (job.getSheetWidth() - (job.getBook().getBookWidth() * 2) - job.getMilling()*2)/2;
			//			if (leftRightMargin < job.getHunkelerCueMark().getWidth() + Constants.CROPMARK_DEFAULT_SIZE){
			//				String msg = "This book can not be composed with the current paper width, there is no extra space for the cue mark zone.\n"+
			//						"Please verify your configuration:\n"+
			//						"- paper width = " + job.getSheetWidth() + "\n" +
			//						"- book width = " + job.getBook().getBookWidth() + "\n" +
			//						"- left/right margin = " + leftRightMargin + "\n";
			//				throw new ComposerException(msg);
			//			}
			
			float leftRightMargin = (job.getSheetWidth() - (job.getBook().getBookWidth()*2) - job.getMilling())/2;

			if (job.getComposition() == CompositionScheme.THREE_UP)
				leftRightMargin = job.getTriming();

//			if (leftRightMargin < job.getTriming()){
//				String msg = "This book can not be composed with the current paper width, there is no extra space for the cue mark zone.\n"+
//						"Please verify your configuration:\n"+
//						"- paper width = " + job.getSheetWidth() + "\n" +
//						"- book width = " + job.getBook().getBookWidth() + "\n" +
//						"- left/right margin = " + leftRightMargin + "\n";
//				throw new ComposerException(msg);
//			}

			job.setLeftMargin(leftRightMargin);
			job.setRightMargin(leftRightMargin); 

			xShiftOdd = 0.0f;

			xShiftEven = paperWidth - Format.points2mm(getOrigBleedBox().getWidth()).floatValue();
 
			//yShift = /*(paperHeight - SheetHeight)/2 +*/ job.getBottomMargin();
		    yShift = ((SheetHeight)-Format.points2mm(getOrigBleedBox().getHeight()).floatValue())/2;    //job.getBottomMargin();//((SheetHeight)-Format.points2mm(getOrigBleedBox().getHeight()).floatValue())/2; //(paperHeight - SheetHeight)/2 ;//job.getBottomMargin();

		}else{		
			if (job.isRTL()){  //book right to left
				xShiftOdd = paperWidth - Format.points2mm(getOrigBleedBox().getWidth()).floatValue();

				xShiftEven = 0.0f;
			}else {  //book left to right
				xShiftOdd = 0.0f;

				xShiftEven = paperWidth - Format.points2mm(getOrigBleedBox().getWidth()).floatValue();
			}

			System.out.println("********** set page shift:" + xShiftOdd + " - " + xShiftEven);
			
			yShift = ((SheetHeight)-Format.points2mm(getOrigBleedBox().getHeight()).floatValue())/2; 

		}

		// DO NOT center page inside sheet vertically, should be aligned ont the bottom edge; cover has to be 2mm off the bb bottom edge
	}

	/**
	 * The loose leaf production area in Edison (so far the only one) is located 
	 * on the same side as the book block production. The Duerselen PB11 requires
	 * the book blocks to be feeded food first. Therefore it is required to
	 * either turn the book blocks by 180 ° hardware-wise or during composing. 
	 * We currently turn the content of the book blocks during the composing
	 * process. 
	 * Having a flexible way of solving the turning with hardware is superior. 
	 * We have to consider that the loose leaf production are will not always be
	 * on the same side. 
	 */
	private void setRotation() throws ComposerException {
		rotation = 0.0f;
	}

	/** We require a page count that is divisible by 4 (2up) or 6 (3up). 
	 *  If that is not the case for the page count found in the original 
	 *  PDF we add the missing pages. 
	 */
	private void setFinalNoOfPages() {		
		if (job.isPopLine()){
			finalNoOfPages = originalNoOfPages;
		}else{
			int pagesPerSignature = composition.getPagesPerSheet();
			int remainder = (originalNoOfPages) % pagesPerSignature;
			int extraPages = 0;
			if(remainder != 0)
				extraPages = pagesPerSignature - ((originalNoOfPages ) % pagesPerSignature);
			finalNoOfPages = (originalNoOfPages) + extraPages; 
		}
		job.getBook().setFinalPageCount(finalNoOfPages);
	}




	/**
	 * 
	 * @param page
	 * @return
	 * @throws ComposerException
	 */
	private Float[] getPageTransformationMatrix(int page) throws ComposerException {

		float xShift; 
		if((page % 2) == 1) xShift = xShiftOdd;
		//if((page % 2) == 1) xShift = xShiftEven;
    
		else xShift = xShiftEven;

		// Values must be given in points.
		//		Float[] tm = new Float[6];
		//		double angle = Math.PI;
		//		tm[0] = (float)Math.cos(angle);		//a
		//		tm[1] = (float)Math.sin(angle);		//b
		//		tm[2] = (float)-Math.sin(angle);	//c
		//		tm[3] = (float)Math.cos(angle);		//d

		Float[] tm = new Float[6];
		tm[0] = 1.0f; // a
		tm[1] = 0.0f; // b
		tm[2] = 0.0f; // c
		tm[3] = 1.0f; // d

		tm[4] = Format.mm2points(xShift).floatValue();		//e
		tm[5] = Format.mm2points(yShift).floatValue(); 	    //f	

		return tm;
	}

	private Rectangle getReferenceBox(int page) throws ComposerException {
		Rectangle ref = reader.getBoxSize(page, "bleed");
		if(ref == null) {
			//log.warn("The trim box is not set. Trying the crop box. ");
			ref = reader.getBoxSize(page, "trim"); 
		}
		if(ref == null) {
			System.err.println("Neither bleed nor trim box is set. ");
			throw new ComposerException("No reference box for page " + page + " found. ");
		}

		if(ref.getWidth() != referenceBox.getWidth()) System.out.println("Width of reference box on page " + page + " differs from the one on page 1.");
		if(ref.getHeight() != referenceBox.getHeight()) System.out.println("Height of the reference box on page " + page + " differs from the on on page 1.");

		return ref;
	}

	@Override
	public String getReport() {
		return null;
	}

	@Override
	protected void updateProducibleItem() throws ComposerException {}


	/**
	 * 
	 * @param cb
	 * @param page
	 * @param bleedheight => bleed value to draw cropmark out of the bleed area
	 * @param llx
	 * @param lly
	 * @param urx
	 * @param ury
	 */
	private void setupCropMark(PdfContentByte cb, int page, float bleedheight, float llx, float lly, float urx, float ury) {
//sji to be changed later
		final LineSeparator lineSeparator = new LineSeparator();
		lineSeparator.setLineWidth(0.2f);
		lineSeparator.setLineColor(BaseColor.BLACK);
		cb.setLineWidth(0.2f);

		// top right
		if((job.isFlyFolderLine() &&
				((page % 6 != 5 && composition == CompositionScheme.THREE_UP) || (page % 4 != 3 && composition == CompositionScheme.TWO_UP)))
				|| (job.isPloughFolderLine() && 
						((page % 8 != 3 && composition == CompositionScheme.FOUR_UP) 
								|| (composition == CompositionScheme.THREE_UP) 
								|| (page % 4 != 3 && composition == CompositionScheme.TWO_UP)))
				|| (job.isPopLine())){
			//			cb.setColorFill(BaseColor.RED);
			//			lineSeparator.setLineColor(BaseColor.RED);
			if((page % 2 == 1) && job.isPopLine() || !job.isPopLine())
			lineSeparator.drawLine(cb, urx + Format.mm2points(6.0f).floatValue(), urx + bleedheight, ury);
			cb.moveTo(urx, ury + bleedheight);
			cb.lineTo(urx, ury + Format.mm2points(6.0f).floatValue());
			cb.stroke();
		}

		// top left
		if(job.isFlyFolderLine() 
				|| job.isPloughFolderLine()	&& 
				((page % 6 != 2 && composition == CompositionScheme.THREE_UP))  || (composition == CompositionScheme.FOUR_UP)  || (composition == CompositionScheme.TWO_UP)
				|| (job.isPopLine())){
			//			cb.setColorFill(BaseColor.BLUE);
			//			lineSeparator.setLineColor(BaseColor.BLUE);
			if ((page % 2 == 0) && job.isPopLine() || !job.isPopLine())
			lineSeparator.drawLine(cb, llx - Format.mm2points(6.0f).floatValue(), llx - bleedheight, ury);
			cb.moveTo(llx, ury + bleedheight);
			cb.lineTo(llx, ury + Format.mm2points(6.0f).floatValue());
			cb.stroke();
		}

		// bottom right
		//		cb.setColorFill(BaseColor.LIGHT_GRAY);
		//		lineSeparator.setLineColor(BaseColor.LIGHT_GRAY);
		if((page % 2 == 1) && job.isPopLine() || !job.isPopLine())
		lineSeparator.drawLine(cb, urx + Format.mm2points(6.0f).floatValue(), urx + bleedheight, lly);
		cb.moveTo(urx, lly - bleedheight);
		cb.lineTo(urx, lly - Format.mm2points(6.0f).floatValue());
		cb.stroke();


		// bottom left
		if(job.isPloughFolderLine() &&
				((page % 8 != 0 && composition == CompositionScheme.FOUR_UP) || (page % 6 != 0 && composition == CompositionScheme.THREE_UP) || (page % 4 != 0 && composition == CompositionScheme.TWO_UP))
				|| job.isFlyFolderLine() && 
				((page % 6 != 0 && composition == CompositionScheme.THREE_UP) || (page % 4 != 0 && composition == CompositionScheme.TWO_UP))
				|| (job.isPopLine())){
			//			cb.setColorFill(BaseColor.GREEN);
			//			lineSeparator.setLineColor(BaseColor.GREEN);
			if ((page % 2 == 0) && job.isPopLine() || !job.isPopLine())
			lineSeparator.drawLine(cb, llx - Format.mm2points(6.0f).floatValue(), llx - bleedheight, lly);
			cb.moveTo(llx, lly - bleedheight);
			cb.lineTo(llx, lly - Format.mm2points(6.0f).floatValue());
			cb.stroke();
		}

		//			cb.setColorFill(BaseColor.BLACK);
		//			lineSeparator.setLineColor(BaseColor.BLACK);
	}


	private void createPopLineComposedFile() throws ComposerException, DocumentException, IOException {
		System.out.println("Creating composed file ...");		

		Document doc = null;
		PdfWriter writer = null;

		float countSigLower = 0;

		try {
			// create a new document with the new media size
			doc = new Document(getBoxSizeInPoints(mediaBox));
			// create a new Writer object that writes content to the new PDF  
			FileOutputStream file = new FileOutputStream(composedFilename);
			System.out.println("Creating composed file created ...");
			writer = PdfWriter.getInstance(doc, file);
			System.out.println("Creating composed file processing ...");
			// open the document
			doc.open();


			int lowerJ = 0;
			int upperJ = 0;

			upperSide = new ArrayList<Integer>();
			lowerSide = new ArrayList<Integer>();

			// Retrieve an instance of the ContentByte to add changes to the imported PDF pages
			// PdfImportedPages cannot be changed, the direct content is needed to do this.
			PdfContentByte cb = writer.getDirectContent();

			int pageNumber = 0;

			for(int i=1; i<=originalNoOfPages; i++) {
				doc.newPage(); 

				PdfContentByte canvas = writer.getDirectContent();

				//draw pop line cue mark
				if (i == 2){	
					float llx = doc.getPageSize().getWidth() - Format.mm2points(1.5f).floatValue();
					float urx = llx - Format.mm2points(job.getHunkelerCueMark().getWidth()).floatValue();	
					float lly = Format.mm2points(job.getHunkelerCueMark().getyPos()).floatValue();
					float ury = lly + Format.mm2points(job.getHunkelerCueMark().getHeight()).floatValue();	
					Rectangle rect = new Rectangle(llx, lly, urx, ury);
					rect.setBackgroundColor(new CMYKColor(1f, 1f, 1f, 1f));
					canvas.rectangle(rect);	
				}


				float urx = 0.0f;	
				float llx = 0.0f;								
				float ury = 0.0f;
				float lly = 0.0f;

				float xPos1 = 0.0f;
				float xPos2 = 0.0f;
				float yPos = doc.getPageSize().getHeight() - 2.0f;
				if (job.isBigSheet())
					yPos = yPos - Format.mm2points(job.getOutputControllerMargin()).floatValue();

				if (i%2 == 1){

					upperSide.add(i);

					pageNumber = job.getUpperSide()[upperJ];
					if (pageNumber > originalNoOfPages)
						return;
					upperJ++;

					System.out.println("upper pageNumber " + pageNumber);

					xPos1 = Format.mm2points(6.6f).floatValue();
					xPos2 = doc.getPageSize().getWidth() - Format.mm2points(1.5f).floatValue();					

					PdfImportedPage page = writer.getImportedPage(reader, pageNumber);
					Float[] tm = getPopLinePageTransformationMatrix(i, 180, 1);
					cb.addTemplate(page, tm[0], tm[1], tm[2], tm[3], tm[4], tm[5]);

					float bleed = Format.mm2points(job.getBleed()).floatValue();
					Rectangle orgbox = reader.getPageSize(i);

					setupPopLineCropMark(cb, i, tm[4] + bleed , tm[5] + bleed, tm[4] +orgbox.getWidth() - bleed, tm[5] + orgbox.getHeight() - bleed, true, false);


					tm = getPopLinePageTransformationMatrix(i, 0, 2);
					cb.addTemplate(page, tm[0], tm[1], tm[2], tm[3], tm[4], tm[5]);

					if (job.getComposition() != CompositionScheme.THREE_UP)
						setupPopLineCropMark(cb, i, tm[4] - orgbox.getWidth() + bleed, tm[5] - orgbox.getHeight() + bleed, tm[4] - bleed, tm[5] - bleed, false, true);

					drawPopLineMillingMark(cb, orgbox.getWidth() + Format.mm2points(job.getLeftMargin() + job.getMilling()).floatValue(),
							tm[5] - orgbox.getHeight() + bleed, 
							tm[5] - bleed, job.isPopLinePB());	

					if (job.getComposition() == CompositionScheme.THREE_UP) {
						tm = getPopLinePageTransformationMatrix(i, 0, 3);
						cb.addTemplate(page, tm[0], tm[1], tm[2], tm[3], tm[4], tm[5]);	

						setupPopLineCropMark(cb, i, tm[4] - orgbox.getWidth() + bleed, tm[5] - orgbox.getHeight() + bleed, tm[4] - bleed, tm[5] - bleed, false, true);

						drawPopLineMillingMark(cb, orgbox.getWidth()*2 + Format.mm2points(job.getLeftMargin() + job.getMilling()*2).floatValue(),
								tm[5] + bleed, 
								tm[5] + orgbox.getHeight() - bleed, false);
					}

					boolean addCueMark = true;	

					if(job.isBigSheet()){
						addCueMark = (i/job.getSheetPersig() == countSigLower*2) ? true : false;
					}

					if (addCueMark){					
						urx = Format.mm2points(1.5f).floatValue();	
						llx = urx + Format.mm2points(5f).floatValue();								
						ury = yPos ;
						lly = ury - Format.mm2points(5f).floatValue();
						Rectangle rect = new Rectangle(llx, lly, urx, ury);
						rect.setBackgroundColor(new CMYKColor(1f, 1f, 1f, 1f));
						canvas.rectangle(rect);
						countSigLower ++;
					}

				}else{

					lowerSide.add(i);

					pageNumber = job.getLowerSide()[lowerJ];
					if (pageNumber > originalNoOfPages)
						return;
					lowerJ++;

					System.out.println("lower pageNumber " + pageNumber);

					PdfImportedPage page = writer.getImportedPage(reader, pageNumber);
					Float[] tm = getPopLinePageTransformationMatrix(i, 180, 1);
					cb.addTemplate(page, tm[0], tm[1], tm[2], tm[3], tm[4], tm[5]);


					xPos1 = Format.mm2points(1.5f).floatValue();
					xPos2 = doc.getPageSize().getWidth() - Format.mm2points(6.6f).floatValue();


					float bleed = Format.mm2points(job.getBleed()).floatValue();
					Rectangle orgbox = reader.getPageSize(i);

					setupPopLineCropMark(cb, i, tm[4] - orgbox.getWidth() + bleed , tm[5] - orgbox.getHeight() + bleed, tm[4] - bleed, tm[5] - bleed, true, false);						

					tm = getPopLinePageTransformationMatrix(i, 0, 2);
					cb.addTemplate(page, tm[0], tm[1], tm[2], tm[3], tm[4], tm[5]);

					if (job.getComposition() != CompositionScheme.THREE_UP)
						setupPopLineCropMark(cb, i, tm[4] + bleed, tm[5] + bleed, tm[4] + orgbox.getWidth() - bleed, tm[5] + orgbox.getHeight() - bleed, false, true);

					drawPopLineMillingMark(cb, orgbox.getWidth() + Format.mm2points(job.getLeftMargin() + job.getMilling()).floatValue(),
							tm[5] + bleed, 
							tm[5] + orgbox.getHeight() - bleed, job.isPopLinePB());

					if (job.getComposition() == CompositionScheme.THREE_UP) {	
						tm = getPopLinePageTransformationMatrix(i, 0, 3);
						cb.addTemplate(page, tm[0], tm[1], tm[2], tm[3], tm[4], tm[5]);

						setupPopLineCropMark(cb, i, tm[4] + bleed, tm[5] + bleed, tm[4] + orgbox.getWidth() - bleed, tm[5] + orgbox.getHeight() - bleed, false, true);

						drawPopLineMillingMark(cb, orgbox.getWidth()*2 + Format.mm2points(job.getLeftMargin() + job.getMilling()*2).floatValue(),
								tm[5] + bleed, 
								tm[5] + orgbox.getHeight() - bleed, false);
					}
				}

				//MM barcode and imposition version
				if (pageNumber == 1 && job.isPopLinePB()){
					int pages = 2;
					if (job.getComposition() == CompositionScheme.THREE_UP)
						pages = 3;

					String text = PdfComposer.getMullerMartiniBarcode(job.getBook().getDocId().concat(Constants.MATERIAL_TYPE_BOOKBLOCK), "0000", 
							job.getSheetHeight(), job.getPaperthikness()*(job.getBook().getFinalPageCount()/2));
					// place Binder datamatrix 

					float y = job.getBook().getBookHeight() - job.getHunkelerDatamatrix().getHeight() - 2f;
					float datamatrixXPos = job.getSheetWidth()/pages - job.getHunkelerDatamatrix().getWidth() - 50f;
					float ypos = 4f;
					float x1 = 20f;
					float x2 = 100f; //job.getBook().getBookWidth() + job.getTriming() +job.getMilling();

					addFirstPageDetails(text, cb, y, datamatrixXPos, ypos, x1, x2);

					//sji position to be tested
					y = 2f;
					datamatrixXPos = job.getSheetWidth()/pages + 50f;
					ypos = job.getBook().getBookHeight() - 2f;
					x1 = job.getSheetWidth() - 80f;
					x2 = job.getSheetWidth() - 150f; //job.getBook().getBookWidth() + job.getTriming() +job.getMilling();

					addFirstPageDetails(text, cb, y, datamatrixXPos, ypos, x1, x2);

					y = 2f;
					datamatrixXPos = (job.getSheetWidth()/pages)*2 + 50f;
					ypos = job.getBook().getBookHeight() - 2f;
					x1 = job.getSheetWidth() - 80f;
					x2 = job.getSheetWidth() - 150f; //job.getBook().getBookWidth() + job.getTriming() +job.getMilling();

					addFirstPageDetails(text, cb, y, datamatrixXPos, ypos, x1, x2);
				}else
					setupPopLineJetPressRefreshbar(cb, xPos1, xPos2, yPos);


			}
		} catch(Exception e) {
			e.printStackTrace();
			String msg = "Cannot create composed file. e:" + e.getMessage();
			throw new ComposerException(msg);

		} finally {
			try {
				if(doc != null) 
					doc.close();
				if(writer != null)
					writer.close();
			} catch(Exception e) {
				System.err.println("Cannot close document/writer. e: " + e.getMessage());
			}
		}
	}


	/**
	 * 
	 * @param text
	 * @param cb
	 * @param datamatrixYPos
	 * @param dateYPos
	 * @param dateX1
	 * @param dateX2
	 * @throws Exception
	 */
	public void addFirstPageDetails (String text, PdfContentByte cb, float datamatrixYPos,float datamatrixXPos, float dateYPos, float dateX1, float dateX2) throws Exception{
		PdfComposer.addDatamatrix(job, datamatrixXPos, datamatrixYPos, cb, text, false, 0.0f);

		String bookId   = String.valueOf(job.getBook().getBook().getId());
		String hunkeler = job.getHunkelerLine()+(job.isBigSheet()? "EM":"SM");

		ImpositionJob.addImpositionDate(cb, dateX1, dateX2, dateYPos, bookId, hunkeler ,true);
	}


	/**
	 * 
	 * @param page
	 * @return
	 * @throws ComposerException
	 */
	private Float[] getPopLinePageTransformationMatrix(int page, float rotation, int xPages) throws ComposerException {

		// Values must be given in points.
		Float[] tm = new Float[6];

		if (page % 2 == 1){
			if (rotation == 0){
				tm[0] = 1.0f;		//a
				tm[1] = 0.0f;		//b
				tm[2] = 0.0f;		//c
				tm[3] = 1.0f;		//d
				//				tm[4] = Format.mm2points(job.getLeftMargin()).floatValue();		//e
				tm[5] = Format.mm2points(job.getBottomMargin()).floatValue(); 	    //f	

				tm[4] = referenceBox.getWidth() + Format.mm2points(job.getLeftMargin()).floatValue(); 	//e

			}else{
				double angle = Math.PI;
				tm[0] = (float)Math.cos(angle);		//ap
				tm[1] = (float)Math.sin(angle);		//b
				tm[2] = (float)-Math.sin(angle);	//c
				tm[3] = (float)Math.cos(angle);		//d
				//				tm[4] = referenceBox.getWidth()*xPages + Format.mm2points(job.getLeftMargin() + job.getMilling()*xPages).floatValue(); 	//e
				tm[5] = referenceBox.getHeight() + Format.mm2points(job.getTopMargin()).floatValue(); 	    //f

				tm[4] = Format.mm2points(job.getLeftMargin()).floatValue();		//e
			}
		}
		else{
			if (rotation == 0){
				tm[0] = 1.0f;		//a
				tm[1] = 0.0f;		//b
				tm[2] = 0.0f;		//c
				tm[3] = 1.0f;		//d
				tm[4] = referenceBox.getWidth()*(xPages-1) + Format.mm2points(job.getLeftMargin() + job.getMilling()*xPages).floatValue();		//e
				tm[5] = Format.mm2points(job.getBottomMargin()).floatValue(); 	    //f	
			}else{
				double angle = Math.PI;
				tm[0] = (float)Math.cos(angle);		//a
				tm[1] = (float)Math.sin(angle);		//b
				tm[2] = (float)-Math.sin(angle);	//c
				tm[3] = (float)Math.cos(angle);		//d
				tm[4] = referenceBox.getWidth() + Format.mm2points(job.getLeftMargin()).floatValue();		//e
				tm[5] = referenceBox.getHeight() + Format.mm2points(job.getTopMargin()).floatValue(); 	    //f
			}
		}


		return tm;
	}


	/**
	 * 
	 * @param cb
	 * @param xpos
	 * @param ypos
	 */
	private void drawPopLineMillingMark (PdfContentByte cb, float xpos, float ypos1, float ypos2, boolean isPB){
		final LineSeparator lineSeparator = new LineSeparator();
		lineSeparator.setLineWidth(0.2f);

		if(isPB){
			lineSeparator.drawLine(cb, xpos - Format.mm2points(2.0f).floatValue(), xpos + Format.mm2points(2.0f).floatValue(), ypos1);

			cb.moveTo(xpos - Format.mm2points(2.0f).floatValue(), ypos1 - Format.mm2points(2.0f).floatValue());
			cb.lineTo(xpos - Format.mm2points(2.0f).floatValue(), ypos1 - Format.mm2points(8.0f).floatValue());
			cb.stroke();

			cb.moveTo(xpos + Format.mm2points(2.0f).floatValue(), ypos1 - Format.mm2points(2.0f).floatValue());
			cb.lineTo(xpos + Format.mm2points(2.0f).floatValue(), ypos1 - Format.mm2points(8.0f).floatValue());
			cb.stroke();
		}

		cb.moveTo(xpos, ypos1);
		cb.lineTo(xpos, ypos1 - Format.mm2points(6.0f).floatValue());
		cb.stroke();

		if(isPB){
			lineSeparator.drawLine(cb, xpos - Format.mm2points(2.0f).floatValue(), xpos + Format.mm2points(2.0f).floatValue(), ypos2);
			cb.moveTo(xpos - Format.mm2points(2.0f).floatValue(), ypos2 + Format.mm2points(2.0f).floatValue());
			cb.lineTo(xpos - Format.mm2points(2.0f).floatValue(), ypos2 + Format.mm2points(8.0f).floatValue());
			cb.stroke();

			cb.moveTo(xpos + Format.mm2points(2.0f).floatValue(), ypos2 + Format.mm2points(2.0f).floatValue());
			cb.lineTo(xpos + Format.mm2points(2.0f).floatValue(), ypos2 + Format.mm2points(8.0f).floatValue());
			cb.stroke();
		}
		cb.moveTo(xpos, ypos2);
		cb.lineTo(xpos, ypos2 + Format.mm2points(6.0f).floatValue());
		cb.stroke();
	}

	/**
	 * 
	 * @param cb
	 * @param x
	 */
	private void setupPopLineCropMark(PdfContentByte cb, int page, float llx, float lly, float urx,
			float ury, boolean drawleft, boolean drawright) {

		final LineSeparator lineSeparator = new LineSeparator();
		lineSeparator.setLineWidth(0.2f);

		float cropSize = job.getBleed() * 2;

		if (drawleft){
			//

			lineSeparator.drawLine(cb, llx - Format.mm2points(cropSize).floatValue(), llx - Format.mm2points(job.getBleed()).floatValue(), lly);
			cb.moveTo(llx, lly - Format.mm2points(job.getBleed()).floatValue());
			cb.lineTo(llx, lly - Format.mm2points(cropSize).floatValue());
			cb.stroke();
			//

			//
			lineSeparator.drawLine(cb, llx - Format.mm2points(cropSize).floatValue(), llx - Format.mm2points(job.getBleed()).floatValue(), ury);			
			cb.moveTo(llx, ury + Format.mm2points(job.getBleed()).floatValue());
			cb.lineTo(llx, ury + Format.mm2points(cropSize).floatValue());
			cb.stroke();
			//
		}

		if (drawright){
			//
			lineSeparator.drawLine(cb, urx + Format.mm2points(cropSize).floatValue(), urx + Format.mm2points(job.getBleed()).floatValue(), ury);
			cb.moveTo(urx, ury + Format.mm2points(job.getBleed()).floatValue());
			cb.lineTo(urx, ury + Format.mm2points(cropSize).floatValue());
			cb.stroke();
			//

			//
			lineSeparator.drawLine(cb, urx + Format.mm2points(cropSize).floatValue(), urx + Format.mm2points(job.getBleed()).floatValue(), lly);
			cb.moveTo(urx, lly - Format.mm2points(job.getBleed()).floatValue());
			cb.lineTo(urx, lly - Format.mm2points(cropSize).floatValue());
			cb.stroke();
			//		
		}
	}


	/**
	 * 
	 * @param cb
	 * @param xPos1
	 * @param xPos2
	 * @param yPos
	 */
	private void setupPopLineJetPressRefreshbar(PdfContentByte cb, float xPos1, float xPos2, float yPos) {
		final LineSeparator lineSeparatorFuji = new LineSeparator();
		lineSeparatorFuji.setLineWidth(0.5f);

		lineSeparatorFuji.setLineColor(new CMYKColor(1f, 0, 0, 0));
		lineSeparatorFuji.drawLine(cb, xPos1, xPos2, yPos);

		yPos -= Format.mm2points(1.0f).floatValue();
		lineSeparatorFuji.setLineColor(new CMYKColor(0, 1f, 0, 0));
		lineSeparatorFuji.drawLine(cb, xPos1, xPos2, yPos);

		yPos -= Format.mm2points(1.0f).floatValue();
		lineSeparatorFuji.setLineColor(new CMYKColor(0, 0, 1f, 0));
		lineSeparatorFuji.drawLine(cb, xPos1, xPos2, yPos);

		yPos -= Format.mm2points(1.0f).floatValue();
		lineSeparatorFuji.setLineColor(new CMYKColor(0, 0, 0, 1f));
		lineSeparatorFuji.drawLine(cb, xPos1, xPos2, yPos);
	}

}
