package com.epac.imposition.bookblock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.epac.imposition.config.Constants;
import com.epac.imposition.utils.CompositionScheme;
import com.epac.imposition.utils.Format;
import com.epac.om.api.utils.LogUtils;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.exceptions.InvalidPdfException;
import com.itextpdf.text.pdf.BarcodeDatamatrix;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPage;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

public class PdfComposer {

	ImpositionJob job;

	List<Integer> upperSide;
	List<Integer> lowerSide;

	/**
	 * 
	 * @param job
	 */
	public PdfComposer(ImpositionJob job) {
		this.job = job;
	}

	/**
	 * 
	 * @throws ComposerException
	 * @throws IOException
	 * @throws DocumentException
	 */
	public void createFinalFile() throws ComposerException, IOException, DocumentException {

		float sheetHeight = job.getSheetHeight();

			// create surfaces, small sheets of hunkeler
			if (!job.isPopLine())
				createSurfacesFile(job.getBook().getTempOutputFilePath(), job.getBook().getOutputFilePath(), job.getSheetWidth(),
					sheetHeight);
			else
				createPoplineSurfacesFile(job.getBook().getTempOutputFilePath(), job.getBook().getOutputFilePath(), job.getSheetWidth(),
						sheetHeight);
			
			// reorder the pages to fit the printing direction
			if(job.isFlyFolderLine()){
				rotatePDF(job.getBook().getOutputFilePath(), job.getBook().getTempOutputFilePath(), job.getSheetWidth(),
					sheetHeight);
				FileUtils.copyFile(new File(job.getBook().getTempOutputFilePath()), new File(job.getBook().getOutputFilePath()), false);
			}

		if(job.isBigSheet()){
			sheetHeight = job.getPrintSheetHeight() - job.getOutputControllerMargin();
			combineBigSheet(job.getBook().getOutputFilePath(), job.getBook().getTempOutputFilePath(), job.getSheetWidth(),
			sheetHeight);

			FileUtils.copyFile(new File(job.getBook().getTempOutputFilePath()), new File(job.getBook().getOutputFilePath()), false);
		
			// update file path for JDF generation
			//String outputFilePath = updateOutputFilePath();

			//FileUtils.copyFile(new File(job.getBook().getOutputFilePath()), new File(outputFilePath), false);

			//FileUtils.forceDelete(new File(job.getBook().getOutputFilePath()));

			//job.getBook().setOutputFilePath(outputFilePath);
		}
	}

	/**
	 * 
	 * @return
	 */
	private String updateOutputFilePath() {
		System.out.println("start updating outputfilepath pdfcomposer");
		String[] filePath = job.getBook().getOutputFilePath().split("/");
		String fileName = "";
		if (filePath.length == 0)
			fileName = job.getBook().getOutputFilePath();
		else
			fileName = filePath[filePath.length - 1];

		String pdfFolderPath = job.getBook().getOutputFilePath().substring(0, job.getBook().getOutputFilePath().indexOf(".pdf"));
		String pdfFilePath = pdfFolderPath + "/" + fileName;

		pdfFilePath = pdfFilePath.replaceAll(" ", "_");

		return pdfFilePath;
	}
	
	/**
	 * 
	 * @param input
	 * @param dest
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void rotatePDF (String input, String dest, float width, float height) throws DocumentException, IOException{
		Document doc = null;
		PdfWriter writer = null;
		PdfReader resultReader = null;
		try {
			resultReader = PdfComposer.openReader(input);

			// create a new document with the new media size
			Rectangle rectFile = new Rectangle(width, height);
			doc = new Document(getBoxSizeInPoints(rectFile));
			// create a new Writer object that writes content to the new PDF  
			FileOutputStream file = new FileOutputStream(dest);
			writer = PdfWriter.getInstance(doc, file);
			// open the document
			doc.open();


			PdfContentByte cb = writer.getDirectContent();

			for (int i = 1; i <= resultReader.getNumberOfPages(); i++){
				doc.newPage();
				writer.setPageEmpty(false);

				PdfImportedPage page = getPageContent(writer, i, resultReader);
				Float[] tm = new Float[6];
				double angle = Math.PI;
				tm[0] = (float)Math.cos(angle);		//a
				tm[1] = (float)Math.sin(angle);		//b
				tm[2] = (float)-Math.sin(angle);	//c
				tm[3] = (float)Math.cos(angle);		//d
				tm[4] = Format.mm2points(width).floatValue();		//e
				tm[5] = Format.mm2points(height).floatValue(); 		//f	

				cb.addTemplate(page, tm[0], tm[1], tm[2], tm[3], tm[4], tm[5]);

			}

		} catch (Exception e) {
			System.err.println("Exception ..." + e);
			e.printStackTrace();
		}finally{
			doc.close();
			writer.close();
			resultReader.close();
		}
	}
	

	/**
	 * 
	 * @param input
	 * @param dest
	 * @param width
	 *            in mm
	 * @param height
	 *            in mm
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void reorderPDF(String input, String dest, float width, float height) throws DocumentException, IOException {

		Document doc = null;
		PdfWriter writer = null;
		PdfReader resultReader = null;
		try {
			Rectangle rectFile = new Rectangle(width, height);
			doc = new Document(getBoxSizeInPoints(rectFile));
			FileOutputStream file = new FileOutputStream(dest);
			writer = PdfWriter.getInstance(doc, file);
			doc.open();

			resultReader = openReader(input);

			PdfContentByte cb = writer.getDirectContent();
			writer.setPageEvent(new PdfPageEventHelper() {
				public void onEndPage(PdfWriter writer, Document document) {
					writer.addPageDictEntry(PdfName.ROTATE, PdfPage.INVERTEDPORTRAIT);
				}
			});

			for (int i = 1; i <= resultReader.getNumberOfPages(); i++) {
				doc.newPage();
				writer.setPageEmpty(false);
				PdfImportedPage page = getPageContent(writer, i, resultReader);
				Float[] tm = new Float[6];
				tm[0] = 1.0f; // a
				tm[1] = 0.0f; // b
				tm[2] = 0.0f; // c
				tm[3] = 1.0f; // d 
				tm[4] = 0.0f; // e
				tm[5] = 0.0f;

				cb.addTemplate(page, tm[0], tm[1], tm[2], tm[3], tm[4], tm[5]);
				writer.flush();
			}


		} catch (Exception e) {
			System.err.println("Exception ..." + e);
			e.printStackTrace();
		}finally{

			doc.close();
			writer.close();
			resultReader.close();
		}
	}

	/**
	 * 
	 * @param result
	 * @param files
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void mergefiles(String result, String... files) throws DocumentException, IOException {

		Document document = new Document();
		PdfCopy copy = new PdfCopy(document, new FileOutputStream(result));
		document.open();
		PdfReader reader;
		int n;

		for (int i = 0; i < files.length; i++) {
			reader = new PdfReader(files[i]);
			n = reader.getNumberOfPages();
			for (int page = 0; page < n;) {
				copy.addPage(copy.getImportedPage(reader, ++page));
			}
			copy.freeReader(reader);
			reader.close();
		}
		document.close();
	}

	/**
	 * @throws ComposerException
	 * @throws IOException
	 * @throws DocumentException
	 * @throws FileNotFoundException
	 */
	public static PdfImportedPage getPageContent(PdfWriter writer, int pageNbr, PdfReader resultReader)
			throws ComposerException, FileNotFoundException, DocumentException, IOException {

		PdfImportedPage page = writer.getImportedPage(resultReader, pageNbr);

		return page;
	}

	private void combineBigSheet(String src, String output, float width, float height) {
		int pagesPerSig = job.getComposition().getPagesPerSheet();
		float surfaces = job.getBook().getFinalPageCount() / pagesPerSig * 2;

		int sheetNbr = (int) Math.ceil(surfaces / job.getSheetPersig());
		if (sheetNbr % 2 == 1)
			sheetNbr++;

		Document doc = null;
		PdfWriter writer = null;
		PdfReader reader = null;
		try {
			Rectangle rectFile = new Rectangle(width, height);
			doc = new Document(getBoxSizeInPoints(rectFile));
			FileOutputStream file = new FileOutputStream(output);
			writer = PdfWriter.getInstance(doc, file);
			doc.open();

			reader = openReader(src);

			int evenPage = 2;
			int oddPage  = 1;
			
			Float[] tm = new Float[6];
			tm[0] = 1.0f; // a
			tm[1] = 0.0f; // b
			tm[2] = 0.0f; // c
			tm[3] = 1.0f; // d
			tm[4] = 0.0f; // e
			
			
			for (int i = 1; i <= sheetNbr; i++) {
				doc.newPage();
				writer.setPageEmpty(false);
				PdfContentByte cb = writer.getDirectContent();
				
				for(int x = 1; x <= job.getSheetPersig(); x++){
					PdfImportedPage page = null;
					float y = height - (job.getSheetHeight() * x) + job.getOutputControllerMargin();
					
					
					if (i % 2 == 1) {
						page = writer.getImportedPage(reader, oddPage);
						oddPage += 2;
					}else if (i % 2 == 0){
						page = writer.getImportedPage(reader, evenPage);
						evenPage += 2;
					}
					
					tm[5] = Format.mm2points(y).floatValue(); // f
					cb.addTemplate(page, tm[0], tm[1], tm[2], tm[3], tm[4], tm[5]);
					

				}
			}
				
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				doc.close();
				writer.close();
				reader.close();
			} catch (Exception e2) {}
		}
	}
	

	
	/**
	 * 
	 * @param src
	 * @param output
	 * @param width
	 * @param height
	 */
	public void createBigSheetFile(String src, String output, float width, float height) {
		int pagesPerSig = job.getComposition().getPagesPerSheet();
		float surfaces = job.getBook().getFinalPageCount() / pagesPerSig * 2;

		int sheetNbr = (int) Math.ceil(surfaces / job.getSheetPersig());
		if (sheetNbr % 2 == 1)
			sheetNbr++;

		Document doc = null;
		PdfWriter writer = null;
		PdfReader resultReader = null;
		try {
			Rectangle rectFile = new Rectangle(width, height);
			doc = new Document(getBoxSizeInPoints(rectFile));
			FileOutputStream file = new FileOutputStream(output);
			writer = PdfWriter.getInstance(doc, file);
			doc.open();

			resultReader = openReader(src);

			int lowerJ = 0;
			int upperJ = 0;

			//int countSheet = 0;

			job.setxPosStartUpper(Format.mm2points(job.getNonprintingarea()).floatValue());
			job.setxPosEndUpper(Format.mm2points(width - job.getNonprintingarea() - job.getHunkelerCueMark().getWidth()).floatValue());

			job.setxPosStartBottom(Format.mm2points(job.getNonprintingarea() + job.getHunkelerCueMark().getWidth()).floatValue());
			job.setxPosEndBottom(Format.mm2points(width - job.getNonprintingarea()).floatValue());
			int countSheet = 0;
			for (int i = 1; i <= sheetNbr; i++) {
				doc.newPage();
				writer.setPageEmpty(false);

				int countUpper = job.getSheetPersig() - 1;
				int countLower = job.getSheetPersig() - 1;


				if (i % 2 == 0) {
					countSheet++;

					PdfContentByte cb = writer.getDirectContent();


					for (int j =1;  j <= job.getSheetPersig(); j++) {
						String pop = "00";

						float ypos = job.getSheetHeight() * countUpper;					

						int pageCount = -1;
						if (upperJ < upperSide.size()) {
							pageCount = upperSide.get(upperJ);
							upperJ++;

							PdfImportedPage page = getPageContent(writer, pageCount, resultReader);
							Float[] tm = new Float[6];
							tm[0] = 1.0f; // a
							tm[1] = 0.0f; // b
							tm[2] = 0.0f; // c
							tm[3] = 1.0f; // d
							tm[4] = 0.0f; // e
							tm[5] = Format.mm2points(ypos).floatValue(); // f

							cb.addTemplate(page, tm[0], tm[1], tm[2], tm[3], tm[4], tm[5]);

						} else {
							pop = "99";

							ypos = job.getSheetHeight() * j - job.getHunkelerCueMark().getHeight() + job.getHunkelerCueMark().getyPos();

							if (j == job.getSheetPersig())
								ypos = height - job.getHunkelerCueMark().getHeight() + job.getHunkelerCueMark().getyPos();

							float xpos = job.getNonprintingarea(); //job.getSheetWidth() - job.getNonprintingarea();

							addHunkelerCueMark(xpos, ypos, writer.getDirectContent(), (pop.equalsIgnoreCase("03")));

							
							String text = getBarcodeText("001", "001",
									job.getBook().getDocId().concat(Constants.MATERIAL_TYPE_BOOKBLOCK), "0", pop);
							

							xpos = job.getSheetWidth() - xpos - job.getHunkelerCueMark().getWidth() - job.getHunkelerDatamatrix().getWidth() - 20.0f;

							ypos = ypos - job.getHunkelerDatamatrix().getHeight() + job.getHunkelerDatamatrix().getyPos();
							addDatamatrix(job, xpos, ypos, cb, text, true, width/3);
						}
						countUpper--;
					}

				} else if (i % 2 == 1) {
					PdfContentByte cb = writer.getDirectContent();

					for (int j = 1; j <= job.getSheetPersig(); j++) {

						if (lowerJ >= lowerSide.size())
							break;

						int pageCount = lowerSide.get(lowerJ);

						lowerJ++;

						PdfImportedPage page = getPageContent(writer, pageCount, resultReader);
						Float[] tm = new Float[6];

						float ypos = job.getSheetHeight() * countLower;

						tm[0] = 1.0f; // a
						tm[1] = 0.0f; // b
						tm[2] = 0.0f; // c
						tm[3] = 1.0f; // d
						tm[4] = 0.0f; // e
						tm[5] = Format.mm2points(ypos).floatValue(); // f

						cb.addTemplate(page, tm[0], tm[1], tm[2], tm[3], tm[4], tm[5]);

						countLower--;
					}

					float x = Format.mm2points(job.getSheetWidth() - job.getNonprintingarea()).floatValue();

					PdfContentByte canvas = writer.getDirectContent();
					float urx = x;	
					float llx = x - Format.mm2points(5.0f).floatValue();								
					float ury = doc.getPageSize().getHeight();
					float lly = ury - Format.mm2points(5.0f).floatValue();
					Rectangle rect = new Rectangle(llx, lly, urx, ury);
					rect.setBackgroundColor(new CMYKColor(1f, 1f, 1f, 1f));
					canvas.rectangle(rect);
				}
			}

		} catch (Exception e) {
			LogUtils.error("Exception occured while imposing :" + src +" to " + output ,e);
			
		}finally{
			try {
				doc.close();
				writer.close();
				resultReader.close();
			} catch (Exception e2) {}

		}

	}
	
	
	/**
	 * 
	 * @param src
	 * @param output
	 * @param width
	 * @param height
	 * @throws ComposerException 
	 */
	public void createPoplineSurfacesFile(String src, String output, float width, float height) throws ComposerException {
		int pagesPerSig = job.getComposition().getPagesPerSheet();
		float surfaces = job.getBook().getFinalPageCount() / pagesPerSig * 2;
		double realHeight ;
		int sheetNbr = 0;
		if(job.isBigSheet()){
			sheetNbr = (int) Math.ceil(surfaces / job.getSheetPersig());
			if (sheetNbr % 2 == 1)
				sheetNbr++;
			
			surfaces = sheetNbr * job.getSheetPersig();
		}
		
		
		Document doc = null;
		PdfWriter writer = null;
		PdfReader resultReader = null;

		float countSigUpper = 0;
		float countSigLower = 0;
		
		try {
			Rectangle rectFile = new Rectangle(width, height);
			doc = new Document(getBoxSizeInPoints(rectFile));
			//FileOutputStream file = new FileOutputStream(output);
			writer = PdfWriter.getInstance(doc, new FileOutputStream(output));

			doc.open();

			int lowerJ = 0;
			int upperJ = 0;

			upperSide = new ArrayList<Integer>();
			lowerSide = new ArrayList<Integer>();

			resultReader = openReader(src);
			realHeight = resultReader.getPageSize(1).getHeight();
			PdfContentByte cb = writer.getDirectContent();
			
			int countpages = job.getComposition().getPagesPerSignature();
			int pageNumber = 0;
			for (int i = 1; i <= job.getBook().getBookNbrPages(); i++) {			
				doc.newPage(); 

				PdfContentByte canvas = writer.getDirectContent();
				//SJI POPLINE
				//draw pop line cue mark
				//int MMpage = (job.isPopLinePB() && job.getComposition() == CompositionScheme.THREE_UP) ? 2 : job.getBook().getBookNbrPages();
				int MMpage = job.getBook().getBookNbrPages();
				if (i == MMpage){	
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
				
				int count = (job.getComposition() == CompositionScheme.TWO_UP) ? 2 : 3;
				
				if (i%2 == 1){

					upperSide.add(i);

					pageNumber = job.getUpperSide()[upperJ];
					if (pageNumber > job.getBook().getBookNbrPages())
						return;
					upperJ++;

					System.out.println("upper pageNumber " + pageNumber);
					
					xPos1 = Format.mm2points(6.6f).floatValue();
					xPos2 = doc.getPageSize().getWidth() - Format.mm2points(1.5f).floatValue();
					
					PdfImportedPage page = writer.getImportedPage(resultReader, pageNumber);
					
					for (int j=1; j<=count; j++) {
						Float[] tm = getPopLinePageTransformationMatrix(i, j);
						cb.addTemplate(page, tm[0], tm[1], tm[2], tm[3], tm[4], tm[5]);
						
						if (job.isPopLine() && job.getComposition() == CompositionScheme.TWO_UP && j == 1)
						{
							float textBleed	= Float.parseFloat(System.getProperty(Constants.TEXT_BLEED_VALUE));
							
							float mid = job.getSheetWidth()/2;
							float y = job.getBottomMargin() + textBleed;
							//sji to be tested popline pb
							cb.setLineWidth(0.5f);
							
							final LineSeparator lineSeparator = new LineSeparator();
							lineSeparator.setLineWidth(0.5f);
							y = (float)((realHeight-Format.mm2points(job.getBook().getMetadataHeight()+job.getTriming()+ job.getBottomMargin() + job.getTopMargin()).floatValue())/2);
							//lineSeparator.drawLine(cb, Format.mm2points(mid - 1).floatValue(), Format.mm2points(mid + 1).floatValue(), Format.mm2points(y).floatValue());
							cb.moveTo(Format.mm2points(mid).floatValue(), y + 25f);
							cb.lineTo(Format.mm2points(mid).floatValue(), y + 25f - 3.0f);
							cb.stroke();
							
							//y = job.getSheetHeight() - job.getTopMargin() - textBleed;
							y = Format.mm2points(job.getBook().getBookHeight()).floatValue() - (float)((realHeight-Format.mm2points(job.getBook().getMetadataHeight()+job.getTriming()+ job.getBottomMargin() + job.getTopMargin()).floatValue())/2); 	    
							//lineSeparator.drawLine(cb, Format.mm2points(mid - 1).floatValue(), Format.mm2points(mid + 1).floatValue(), Format.mm2points(y).floatValue());
							cb.moveTo(Format.mm2points(mid).floatValue(), y - 25f);
							cb.lineTo(Format.mm2points(mid).floatValue(), y - 25f + 3.0f);
							cb.stroke();
						}
						
//						float bleed = Format.mm2points(job.getBleed()).floatValue();
//						Rectangle orgbox = resultReader.getPageSize(i);
//
//						setupPopLineCropMark(cb, i, tm[4] + bleed , tm[5] + bleed, tm[4] + orgbox.getWidth() - bleed, tm[5] + orgbox.getHeight() - bleed, true, true);
					}
					
					
					
					boolean addCueMark = true;	

					if(job.isBigSheet()){
						addCueMark = (i/job.getSheetPersig() == countSigLower*2) ? true : false;
					}
					if(job.isPopLine())
					{
						setupPopLineJetPressRefreshbar(cb, xPos1, xPos2, yPos);
					} else
					{
						boolean remove = (i == 2) ? true : false;

						if(!remove)
							setupPopLineJetPressRefreshbar(cb, xPos1, xPos2, yPos);
					}
					if (addCueMark){					
						urx = Format.mm2points(1.5f).floatValue();	
						llx = urx + Format.mm2points(8f).floatValue();								
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
					if (pageNumber > job.getBook().getBookNbrPages())
						return;
					lowerJ++;

					System.out.println("lower pageNumber " + pageNumber);
					
					xPos1 = Format.mm2points(1.5f).floatValue();
					xPos2 = doc.getPageSize().getWidth() - Format.mm2points(6.6f).floatValue();
					
					PdfImportedPage page = writer.getImportedPage(resultReader, pageNumber);
					
					for (int j=1; j<=count; j++) {
						Float[] tm = getPopLinePageTransformationMatrix(i, j);
						cb.addTemplate(page, tm[0], tm[1], tm[2], tm[3], tm[4], tm[5]);
						
						if (job.isPopLine() && job.getComposition() == CompositionScheme.TWO_UP && j == 1)
						{
							float textBleed	= Float.parseFloat(System.getProperty(Constants.TEXT_BLEED_VALUE));
							
							float mid = job.getSheetWidth()/2;
							float y = job.getBottomMargin() + textBleed;
							cb.setLineWidth(0.5f);
							
							final LineSeparator lineSeparator = new LineSeparator();
							lineSeparator.setLineWidth(0.5f);
							y = Format.mm2points(job.getBook().getBookHeight()).floatValue() - (float)((realHeight-Format.mm2points(job.getBook().getMetadataHeight()+job.getTriming()+ job.getBottomMargin() + job.getTopMargin()).floatValue())/2); 	    
							//lineSeparator.drawLine(cb, Format.mm2points(mid - 1).floatValue(), Format.mm2points(mid + 1).floatValue(), Format.mm2points(y).floatValue());
							cb.moveTo(Format.mm2points(mid).floatValue(), y - 24f);
							cb.lineTo(Format.mm2points(mid).floatValue(), y - 24f - 3.0f);
							cb.stroke();
							
							//y = job.getSheetHeight() - job.getTopMargin() - textBleed;
							y = (float)((realHeight-Format.mm2points(job.getBook().getMetadataHeight()+job.getTriming()+ job.getBottomMargin() + job.getTopMargin()).floatValue())/2);

							//lineSeparator.drawLine(cb, Format.mm2points(mid - 1).floatValue(), Format.mm2points(mid + 1).floatValue(), Format.mm2points(y).floatValue());
							cb.moveTo(Format.mm2points(mid).floatValue(), y + 24f);
							cb.lineTo(Format.mm2points(mid).floatValue(), y + 24f + 3.0f);
							cb.stroke();
						}
						
//						float bleed = Format.mm2points(job.getBleed()).floatValue();
//						Rectangle orgbox = resultReader.getPageSize(i);
//
//						setupPopLineCropMark(cb, i, tm[4] - orgbox.getWidth() + bleed , tm[5] - orgbox.getHeight() + bleed, tm[4] - bleed, tm[5] - bleed, true, false);						

					}
					
					boolean addCueMark = true;	

					if(job.isBigSheet()){
						addCueMark = (i/job.getSheetPersig() == countSigUpper*2) ? true : false;
					}
					if(job.isPopLine())
					{
						setupPopLineJetPressRefreshbar(cb, xPos1, xPos2, yPos);
					} else
					{
						boolean remove = (i == 2) ? true : false;

						if(!remove)
							setupPopLineJetPressRefreshbar(cb, xPos1, xPos2, yPos);
					}

					if (addCueMark){					
						urx = Format.mm2points(job.getSheetWidth() - 1.5f).floatValue();	
						llx = urx - Format.mm2points(8f).floatValue();								
						ury = yPos ;
						lly = ury - Format.mm2points(5f).floatValue();
						Rectangle rect = new Rectangle(llx, lly, urx, ury);
						rect.setBackgroundColor(new CMYKColor(1f, 1f, 1f, 1f));
						canvas.rectangle(rect);
						countSigUpper ++;
					}
				}
			/*	if(job.isPopLinePL())
				{
					float textBleed	= Float.parseFloat(System.getProperty(Constants.TEXT_BLEED_VALUE));
					
					float mid = job.getSheetWidth()/2;
					float y = job.getBottomMargin() + textBleed;
					cb.setLineWidth(0.5f);
					
					final LineSeparator lineSeparator = new LineSeparator();
					lineSeparator.setLineWidth(0.5f);
					y = Format.mm2points(job.getBook().getBookHeight()).floatValue() - (float)((realHeight-Format.mm2points(job.getBook().getMetadataHeight()+job.getTriming()+ job.getBottomMargin() + job.getTopMargin()).floatValue())/2); 	    
					//lineSeparator.drawLine(cb, Format.mm2points(mid - 1).floatValue(), Format.mm2points(mid + 1).floatValue(), Format.mm2points(y).floatValue());
					cb.moveTo(Format.mm2points(mid).floatValue(), y - 24f);
					cb.lineTo(Format.mm2points(mid).floatValue(), y - 24f - 3.0f);
					cb.stroke();
					
					//y = job.getSheetHeight() - job.getTopMargin() - textBleed;
					y = (float)((realHeight-Format.mm2points(job.getBook().getMetadataHeight()+job.getTriming()+ job.getBottomMargin() + job.getTopMargin()).floatValue())/2);

					//lineSeparator.drawLine(cb, Format.mm2points(mid - 1).floatValue(), Format.mm2points(mid + 1).floatValue(), Format.mm2points(y).floatValue());
					cb.moveTo(Format.mm2points(mid).floatValue(), y + 24f);
					cb.lineTo(Format.mm2points(mid).floatValue(), y + 24f + 3.0f);
					cb.stroke();
				}
				*/
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

		yPos -= Format.mm2points(0.5f).floatValue();
		lineSeparatorFuji.setLineColor(new CMYKColor(0, 1f, 0, 0));
		lineSeparatorFuji.drawLine(cb, xPos1, xPos2, yPos);

		yPos -= Format.mm2points(0.5f).floatValue();
		lineSeparatorFuji.setLineColor(new CMYKColor(0, 0, 1f, 0));
		lineSeparatorFuji.drawLine(cb, xPos1, xPos2, yPos);

		yPos -= Format.mm2points(0.5f).floatValue();
		lineSeparatorFuji.setLineColor(new CMYKColor(0, 0, 0, 1f));
		lineSeparatorFuji.drawLine(cb, xPos1, xPos2, yPos);
	}

	/**
	 * 
	 * @param src
	 * @param output
	 */
	public void createSurfacesFile(String src, String output, float width, float height) {
		int pagesPerSig = job.getComposition().getPagesPerSheet();
		float surfaces = job.getBook().getFinalPageCount() / pagesPerSig * 2;

		int sheetNbr = 0;
		if(job.isBigSheet()){
			sheetNbr = (int) Math.ceil(surfaces / job.getSheetPersig());
			if (sheetNbr % 2 == 1)
				sheetNbr++;
			
			surfaces = sheetNbr * job.getSheetPersig();
		}
		
		
		Document doc = null;
		PdfWriter writer = null;
		PdfReader resultReader = null;

		//float countSigUpper = 1;
		float countSigLower = 0;
		
		try {
			Rectangle rectFile = new Rectangle(width, height);
			doc = new Document(getBoxSizeInPoints(rectFile));
			//FileOutputStream file = new FileOutputStream(output);
			writer = PdfWriter.getInstance(doc, new FileOutputStream(output));

			doc.open();

			int lowerJ = 0;
			int upperJ = 0;

			upperSide = new ArrayList<Integer>();
			lowerSide = new ArrayList<Integer>();

			resultReader = openReader(src);

			PdfContentByte cb = writer.getDirectContent();

			int countpages = job.getComposition().getPagesPerSignature();
			int pageNumber = 0;
			for (int i = 1; i <= surfaces; i++) {
				doc.newPage();
				writer.setPageEmpty(false);
				
				String pop = "00";

				if (i % 2 == 0) {
					upperSide.add(i);

					for (int j = 0; j < countpages; j++) {
						pageNumber = 0;
						//if no more pages in PDF to add to signature, skip content it will be a blank signature
						if(upperJ < job.getUpperSide().length ){
							pageNumber = job.getUpperSide()[upperJ];
						}
						
						upperJ++;

						if (pageNumber != 0) {

							PdfImportedPage page = getPageContent(writer, pageNumber, resultReader);
							Float[] tm = new Float[6];
							if (job.getComposition() == CompositionScheme.THREE_UP)
								tm = getPageTransformationMatrix_3UP(pageNumber);
							else if (job.getComposition() == CompositionScheme.TWO_UP)
								tm = getPageTransformationMatrix_2UP(pageNumber);
							else 
								tm = getPageTransformationMatrix_4UP(pageNumber);
							
							
							float lineheight = 25f;
							float lineYPosStart	= Format.mm2points(height/2 - lineheight/2).floatValue();
							float lineYPosEnd	= Format.mm2points(height/2 + lineheight/2).floatValue();

							cb.setLineWidth(0.1f);
							if (job.getComposition() == CompositionScheme.THREE_UP){
								
								int []folding = {5, 2};
								if(job.isPloughFolderLine()){
									folding[0] = 1;
									folding[1] = 0;
								}
								
								if(pageNumber % 6 == folding[0]) 
								{
									cb.setColorStroke(BaseColor.BLACK);
									cb.moveTo(tm[4] - Format.mm2points(job.getMilling()).floatValue(), lineYPosStart);
									cb.lineTo(tm[4] - Format.mm2points(job.getMilling()).floatValue(), lineYPosEnd);
									cb.stroke();							
								}else if(pageNumber % 6 == folding[1]){
									cb.setColorStroke(BaseColor.BLACK);
									cb.moveTo(tm[4] - Format.mm2points(job.getTriming()).floatValue(), lineYPosStart);
									cb.lineTo(tm[4] - Format.mm2points(job.getTriming()).floatValue(), lineYPosEnd);
									cb.stroke();							
								}
								
							}else if(job.getComposition() == CompositionScheme.TWO_UP){
								if (pageNumber % 4 == 3)
								{
									cb.moveTo(tm[4] - Format.mm2points(job.getMilling()).floatValue(), lineYPosStart);
									cb.lineTo(tm[4] - Format.mm2points(job.getMilling()).floatValue(), lineYPosEnd);
									cb.stroke();
								}
							}else if (job.getComposition() == CompositionScheme.FOUR_UP){
								
								
								if(pageNumber % 8 == 1) 
								{
									cb.setColorStroke(BaseColor.BLACK);
									cb.moveTo(tm[4] - Format.mm2points(job.getMilling()).floatValue(), lineYPosStart);
									cb.lineTo(tm[4] - Format.mm2points(job.getMilling()).floatValue(), lineYPosEnd);
									cb.stroke();							
								}else if(pageNumber % 8 == 0){
									cb.setColorStroke(BaseColor.BLACK);
									cb.moveTo(tm[4], lineYPosStart);
									cb.lineTo(tm[4], lineYPosEnd);
									cb.stroke();							
								}else if(pageNumber % 8 == 5){
									cb.setColorStroke(BaseColor.BLACK);
									cb.moveTo(tm[4] - Format.mm2points(job.getMilling()).floatValue(), lineYPosStart);
									cb.lineTo(tm[4] - Format.mm2points(job.getMilling()).floatValue(), lineYPosEnd);
									cb.stroke();								
								}
								
							}

							cb.addTemplate(page, tm[0], tm[1], tm[2], tm[3], tm[4], tm[5]);
						}
					}					

			
					if (i == 2)
						pop = System.getProperty(Constants.END_OF_DOCUMENT_CODE, "03");
					else
						
					
						pop = pageNumber == 0? "99":"00";
					 	
					
					String text = getBarcodeText(i, 
							job.getBook().getDocId().concat(Constants.MATERIAL_TYPE_BOOKBLOCK), "0", pop);

					 

					float refPy = 0.0f;
					float refPstart = job.getNonprintingarea() ;
					float refPend = width - job.getNonprintingarea();	
					
					float hMarkY = job.getSheetHeight() - job.getHunkelerCueMark().getHeight();
					float hMarkX = job.getNonprintingarea();
					
					float hCodeX = job.getSheetWidth() - hMarkX - job.getHunkelerCueMark().getWidth() - job.getHunkelerDatamatrix().getWidth() - 20.0f;
					float hCodeY = job.getSheetHeight() - job.getHunkelerDatamatrix().getHeight() - 2.0f;
					
					if(job.isPloughFolderLine()){
						
						if(job.getComposition() == CompositionScheme.TWO_UP ){
							refPy = job.getSheetHeight() - 2.0f;
							refPstart = job.getNonprintingarea();
							refPend   = job.getSheetWidth() - job.getNonprintingarea();
							
							hMarkY = 0f;
							hMarkX = job.getSheetWidth() - job.getNonprintingarea() - job.getHunkelerCueMark().getWidth() ;
							
							hCodeX = job.getNonprintingarea() +  job.getHunkelerCueMark().getWidth() + 20;
							hCodeY = hMarkY + job.getHunkelerCueMark().getHeight();
						}else if(job.getComposition() == CompositionScheme.THREE_UP){
							boolean innerCueMark = Boolean.parseBoolean(System.getProperty(Constants.IMPOSITION_INNER_CUEMARK_ENABLED));
							float trimming = innerCueMark ? job.getTriming() : 0.0f;
							refPy = job.getSheetHeight() - 2.0f;
							refPstart = job.getNonprintingarea();
							refPend   = job.getSheetWidth() - job.getNonprintingarea();
							
							hMarkY = 0f;
							hMarkX = job.getSheetWidth() - 
									job.getNonprintingarea()*2 - 
									job.getBook().getBookWidth() -
									job.getHunkelerCueMark().getWidth() - trimming;
							
							hCodeX = job.getNonprintingarea()*2 + 
									job.getBook().getBookWidth() +
									job.getHunkelerCueMark().getWidth() + trimming + 20;
							hCodeY = hMarkY + job.getHunkelerCueMark().getHeight();
						}else if(job.getComposition() == CompositionScheme.FOUR_UP){
							refPy = job.getSheetHeight() - 2.0f;
							refPstart = job.getNonprintingarea();
							refPend   = job.getSheetWidth() - job.getNonprintingarea();
							
							hMarkY = 0f;
							hMarkX = job.getSheetWidth() - 
									job.getNonprintingarea() - 
									job.getBook().getBookWidth()*2 -
									job.getMilling() *2 -
									job.getHunkelerCueMark().getWidth();
							
							hCodeX = job.getNonprintingarea() + 
									job.getBook().getBookWidth()*2 +
									job.getMilling() *2 +
									job.getHunkelerCueMark().getWidth() + 20;
							hCodeY = hMarkY + job.getHunkelerCueMark().getHeight();
						}
						
					}
					
					boolean remove = (i == 2 && job.isPloughFolderLine()) ? true : false;

					if(!remove)
						setupJetPressRefreshbar(cb, Format.mm2points(refPstart).floatValue(),
							Format.mm2points(refPend).floatValue(),
							Format.mm2points(refPy).floatValue());


					/* draw white rectangle to cut in refresh bar */
					float x = Format.mm2points(job.getSheetWidth() - job.getHunkelerCueMark().getWidth() - hMarkX).floatValue();
					PdfContentByte canvas = writer.getDirectContent();
					float urx = x;	
					float llx = x + Format.mm2points(job.getHunkelerCueMark().getWidth()).floatValue();								
					float ury = Format.mm2points(refPy -1).floatValue();
					float lly = ury + Format.mm2points(job.getHunkelerCueMark().getHeight()+2).floatValue();
					Rectangle rect = new Rectangle(llx, lly, urx, ury);
					rect.setBackgroundColor(CMYKColor.WHITE);
					canvas.rectangle(rect);
					
					addHunkelerCueMark(hMarkX, hMarkY, writer.getDirectContent(), (pop.equalsIgnoreCase("03")));
					
					addDatamatrix(job, hCodeX, hCodeY, cb, text, true, hCodeX);

				} else if (i % 2 == 1/*0*/) {
					lowerSide.add(i);

					for (int j = 0; j < countpages; j++) {
						
						//if no more pages in PDF to add to signature, skip content it will be a blank signature
						if(lowerJ >= job.getLowerSide().length )
							continue;
						
						int pageCount = job.getLowerSide()[lowerJ];
						lowerJ++;

						if (pageCount != 0) {

							PdfImportedPage page = getPageContent(writer, pageCount, resultReader);
							Float[] tm = new Float[6];
							if (job.getComposition() == CompositionScheme.THREE_UP)
								tm = getPageTransformationMatrix_3UP(pageCount);
							else if (job.getComposition() == CompositionScheme.TWO_UP)
								tm = getPageTransformationMatrix_2UP(pageCount);
							else
								tm = getPageTransformationMatrix_4UP(pageCount);
							
							cb.addTemplate(page, tm[0], tm[1], tm[2], tm[3], tm[4], tm[5]);
						}
					}

					boolean remove = (i == 1) ? true : false;

					float refPy = 0.0f; 
					float refPstart = job.getNonprintingarea() + 5.0f;
					float refPend = width - job.getNonprintingarea();
					
					float cueMarkY = 0.0f;
					float cueMarkX = job.getNonprintingarea();
					
					if(job.isPloughFolderLine()){
						if(job.getComposition() == CompositionScheme.TWO_UP || 
						   job.getComposition() == CompositionScheme.FOUR_UP){
							refPy = job.getSheetHeight() - 2.0f;
							refPstart = job.getNonprintingarea();
							refPend   = job.getSheetWidth() - job.getNonprintingarea() - 5.0f;
							
							cueMarkY = job.getSheetHeight() - 5f;
							cueMarkX = job.getNonprintingarea();
						}else if(job.getComposition() == CompositionScheme.THREE_UP){
							refPy = job.getSheetHeight() - 2.0f;
							refPstart = job.getNonprintingarea();
							refPend   = job.getSheetWidth() - job.getNonprintingarea() ;
							
							cueMarkY = job.getSheetHeight() - 5f;
							cueMarkX = job.getNonprintingarea();
						}
					}
					
					if(!remove)
						setupJetPressRefreshbar(cb,
								Format.mm2points(refPstart).floatValue(),
								Format.mm2points(refPend).floatValue(),
								Format.mm2points(refPy).floatValue());

					boolean addCueMark = true;	
					
					if(job.isBigSheet()){
						addCueMark = (i/job.getSheetPersig() == countSigLower*2) ? true : false;
						if(job.isFlyFolderLine())
							cueMarkY = job.getOutputControllerMargin();
						else
							cueMarkY = job.getSheetHeight() - job.getOutputControllerMargin() -5;
					}
					
					
					if(addCueMark){
						float x = Format.mm2points(cueMarkX).floatValue();
						PdfContentByte canvas = writer.getDirectContent();
						float urx = x;	
						float llx = x + Format.mm2points(8.0f).floatValue();								
						float ury = Format.mm2points(cueMarkY).floatValue();
						float lly = ury + Format.mm2points(5.0f).floatValue();
						Rectangle rect = new Rectangle(llx, lly, urx, ury);
						rect.setBackgroundColor(new CMYKColor(1f, 1f, 1f, 1f));
						canvas.rectangle(rect);
						countSigLower ++;
					}
				}
				
				writer.flush();
			}



		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			doc.close();
			writer.close();
			resultReader.close();
		}
	}



	/**
	 * 
	 * @param cb
	 * @param xPos1
	 * @param xPos2
	 * @param yPos
	 * @param aligned
	 * @param xLimit
	 */
	private void setupJetPressRefreshbar(PdfContentByte cb, float xPos1, float xPos2, float yPos) {
		final LineSeparator lineSeparatorFuji = new LineSeparator();
		lineSeparatorFuji.setLineWidth(0.5f);
		lineSeparatorFuji.setLineColor(new CMYKColor(1f, 0, 0, 0));
		lineSeparatorFuji.drawLine(cb, xPos1, xPos2, yPos);

		float separated = Format.mm2points(0.5f).floatValue();

		yPos += separated;
		lineSeparatorFuji.setLineColor(new CMYKColor(0, 1f, 0, 0));
		lineSeparatorFuji.drawLine(cb, xPos1, xPos2, yPos);

		yPos += separated;
		lineSeparatorFuji.setLineColor(new CMYKColor(0, 0, 1f, 0));
		lineSeparatorFuji.drawLine(cb, xPos1, xPos2, yPos);

		yPos += separated;

		lineSeparatorFuji.setLineColor(new CMYKColor(0, 0, 0, 1f));
		lineSeparatorFuji.drawLine(cb, xPos1, xPos2, yPos);
	}

	private Float[] getPageTransformationMatrix_4UP(int page) throws Exception {
		if(job.isPloughFolderLine())
			return getPageTransformationMatrix_4UP_PF(page);
		if(job.isFlyFolderLine())
			return getPageTransformationMatrix_4UP_FF(page);
		throw new Exception("Hunkeler line is not set, please set FF or PF");
	}
	
	private Float[] getPageTransformationMatrix_4UP_FF(int page) throws Exception {
		throw new Exception("Hunkeler FlyFolder line does not support 4UP configuration");
	}

	private Float[] getPageTransformationMatrix_4UP_PF(int page) {
		Float[] tm = new Float[6];
		tm[0] = 1.0f; // a
		tm[1] = 0.0f; // b
		tm[2] = 0.0f; // c
		tm[3] = 1.0f; // d
		tm[4] = 0.0f;
		tm[5] = 0.0f; // f

		switch (page % 8) {

			case 3:
			case 1:
				tm[4] = job.getMilling() *4 + job.getBook().getBookWidth() * 3;
				break;
			case 2:
			case 4:
				tm[4] = 0.0f;
				break;
			case 7:
			case 5:
				tm[4] = job.getMilling() *2 + job.getBook().getBookWidth();	
				break;
			case 6:
			case 0:
				tm[4] = job.getMilling() *2 + job.getBook().getBookWidth() * 2;
				break;
		}

		tm[4] = Format.mm2points(tm[4]).floatValue();

		return tm;
	}

	/**
	 * 
	 * @param page
	 * @return
	 * @throws Exception 
	 */
	private Float[] getPageTransformationMatrix_3UP(int page) throws Exception {
		if(job.isPloughFolderLine())
			return getPageTransformationMatrix_3UP_PF(page);
		if(job.isFlyFolderLine())
			return getPageTransformationMatrix_3UP_FF(page);
		throw new Exception("Hunkeler line is not set, please set FF or PF");
	}
	
	private Float[] getPageTransformationMatrix_3UP_PF(int page) throws Exception {
		Float[] tm = new Float[6];
		tm[0] = 1.0f; // a
		tm[1] = 0.0f; // b
		tm[2] = 0.0f; // c
		tm[3] = 1.0f; // d
		tm[4] = 0.0f;
		tm[5] = 0.0f; // f


		switch (page % 6) {

		case 3:
			tm[4] = job.getInnerMilling();
			break;
		case 2:
			tm[4] = 0.0f;
			break;
		case 5 /*1*/:
			tm[4] = job.getMilling() *2 + job.getBook().getBookWidth();
			break;

		case 0 /*2*/:
			tm[4] = job.getInnerMilling() + job.getBook().getBookWidth();
			break;
		case 1 /*5*/:
			tm[4] = job.getInnerMilling() + job.getMilling() *2 + job.getBook().getBookWidth() *2 ;	
			break;
		case 4 /*4*/:
			tm[4] = job.getBook().getBookWidth() * 2 + job.getMilling() * 2;
			break;

		}

		tm[4] = Format.mm2points(tm[4]).floatValue();

		return tm;
	}
	private Float[] getPageTransformationMatrix_3UP_FF(int page) {
		Float[] tm = new Float[6];
		tm[0] = 1.0f; // a
		tm[1] = 0.0f; // b
		tm[2] = 0.0f; // c
		tm[3] = 1.0f; // d
		tm[4] = 0.0f;
		tm[5] = 0.0f; // f


		switch (page % 6) {

		case 3 /*3*/:
			tm[4] = job.getInnerMilling();
			break;
		case 2 /*0*/:
			tm[4] = job.getInnerMilling() + job.getBook().getBookWidth();
			break;
		case 5 /*1*/:
			tm[4] = job.getInnerMilling() + job.getBook().getBookWidth() * 2 + job.getMilling() * 2;
			break;

		case 0 /*2*/:
			tm[4] = job.getNonprintingarea();
			break;
		case 1 /*5*/:
			tm[4] = job.getNonprintingarea() + job.getBook().getBookWidth() + job.getMilling() * 2;			
			break;
		case 4 /*4*/:
			tm[4] = job.getNonprintingarea() + job.getBook().getBookWidth() * 2 + job.getMilling() * 2;
			break;

		}

		tm[4] = Format.mm2points(tm[4]).floatValue();

		return tm;
	}

	/**
	 * 
	 * @param page
	 * @return
	 * @throws Exception 
	 */
	private Float[] getPageTransformationMatrix_2UP(int page) throws Exception {
		if(job.isPloughFolderLine())
			return getPageTransformationMatrix_2UP_PF(page);
		
		if(job.isFlyFolderLine())
			if (job.isRTL())
				return getPageTransformationMatrix_2UP_FF_RTL(page);
			else
				return getPageTransformationMatrix_2UP_FF(page);
		throw new Exception("Hunkeler line is not set, please set FF or PF");
	}
	
	private Float[] getPageTransformationMatrix_2UP_PF(int page) {
		Float[] tm = new Float[6];
		tm[0] = 1.0f; // a
		tm[1] = 0.0f; // b
		tm[2] = 0.0f; // c
		tm[3] = 1.0f; // d
		tm[4] = 0.0f;
		tm[5] = 0.0f; // f

		switch (page % 4) {
		case 0:
		case 2:
			tm[4] = 0.0f;
			break;
		case 3:
		case 1:
			tm[4] = job.getBook().getBookWidth() + job.getMilling() * 2;
			break;
		}

		tm[4] = Format.mm2points(tm[4]).floatValue();

		return tm;
	}
	
	private Float[] getPageTransformationMatrix_2UP_FF(int page) {
		Float[] tm = new Float[6];
		tm[0] = 1.0f; // a
		tm[1] = 0.0f; // b
		tm[2] = 0.0f; // c
		tm[3] = 1.0f; // d
		tm[4] = 0.0f;
		tm[5] = 0.0f; // f

		switch (page % 4) {
		case 2:
		case 0:
			tm[4] = 0.0f;
			break;
		case 3:
		case 1:
			tm[4] = job.getBook().getBookWidth() + job.getMilling() * 2;
			break;
		}

		tm[4] = Format.mm2points(tm[4]).floatValue();

		return tm;
	}
	
	
	private Float[] getPageTransformationMatrix_2UP_FF_RTL(int page) {
		Float[] tm = new Float[6];
		tm[0] = 1.0f; // a
		tm[1] = 0.0f; // b
		tm[2] = 0.0f; // c
		tm[3] = 1.0f; // d
		tm[4] = 0.0f;
		tm[5] = 0.0f; // f

		switch (page % 4) {
		case 1:
		case 3:
			tm[4] = job.getTriming();
			break;
		case 2:
		case 0:
			tm[4] = job.getTriming() + job.getBook().getBookWidth() + job.getMilling() * 2;
			break;
		}

		tm[4] = Format.mm2points(tm[4]).floatValue();

		return tm;
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
	 * @param page
	 * @param f 
	 * @return
	 * @throws ComposerException
	 */
	private Float[] getPopLinePageTransformationMatrix(int page, int xPages) throws ComposerException {
		
		// Values must be given in points.
		Float[] tm = new Float[6];		
		int rotation = 0;

		if (page % 2 == 1){
			if (job.getComposition() == CompositionScheme.THREE_UP && xPages < 3)
				rotation = 0;
			
			if (job.getComposition() == CompositionScheme.THREE_UP && xPages == 3 )
				rotation = 180;
			
			if (job.getComposition() == CompositionScheme.TWO_UP && xPages == 1)
				rotation = 0;
			
			if (job.getComposition() == CompositionScheme.TWO_UP && xPages > 1)
				rotation = 180;
			
			if (job.getComposition() == CompositionScheme.TWO_UP) {
				switch (xPages) {
				case 1: {
					if (job.isTextWithBleed() && job.isPopLinePL() || job.isPopLinePB()) {
						tm[4] = 0f;
					} else {
						tm[4] = 0f - 3.175f; // e --> 3.175f for 0.25 inch for PL
					}
					break;
				}
				case 2: {
					if (job.isTextWithBleed() && job.isPopLinePL() || job.isPopLinePB()) {
						tm[4] = job.getSheetWidth();
					} else {
						tm[4] = job.getSheetWidth() + 3.175f; // e
					}
					break;
				}
				}
			}else if (job.getComposition() == CompositionScheme.THREE_UP) {
				switch (xPages) {
				case 1:
					tm[4] = job.getLeftMargin(); 	//e
					break;
					
				case 2:
					tm[4] = job.getBook().getBookWidth() + job.getLeftMargin()*2; 	//e
					break;
					
				case 3:
					tm[4] = job.getBook().getBookWidth()*3 + job.getLeftMargin()*2; 	//e
					break;
				}
			}				
		}
		else{
			if (job.getComposition() == CompositionScheme.THREE_UP && xPages == 1)
				rotation = 180;
			
			if (job.getComposition() == CompositionScheme.THREE_UP && xPages > 1 )
				rotation = 0;
			
			if (job.getComposition() == CompositionScheme.TWO_UP && xPages == 1)
				rotation = 180;
			
			if (job.getComposition() == CompositionScheme.TWO_UP && xPages > 1)
				rotation = 0;
			
			if (job.getComposition() == CompositionScheme.TWO_UP) {			
				switch (xPages) {
				case 1: {

					if (job.isTextWithBleed() && job.isPopLinePL() || job.isPopLinePB()) {
						tm[4] = job.getSheetWidth() - job.getBook().getBookWidth() - job.getMilling() * 2;
					} else {
						float ratio = job.getSheetWidth() - job.getBook().getBookWidth() - job.getMilling() * 2;
						tm[4] = ratio - 3.175f; // e
					}
					break;
				}

				case 2: {
					if (job.isTextWithBleed() && job.isPopLinePL() || job.isPopLinePB()) {
						tm[4] = job.getBook().getBookWidth() + job.getMilling() * 2;
					} else {
						float ratio = job.getBook().getBookWidth() + job.getMilling() * 2;
						tm[4] = ratio + 3.175f; // e
					}
					break;

				}
				}
			}else if (job.getComposition() == CompositionScheme.THREE_UP) {
				switch (xPages) {
				case 1:
					tm[4] = job.getBook().getBookWidth() + job.getLeftMargin(); 	//e
					break;
					
				case 2:
					tm[4] = job.getBook().getBookWidth() + job.getLeftMargin(); 	//e
					break;
					
				case 3:
					tm[4] = job.getBook().getBookWidth()*2 + job.getLeftMargin()*2; 	//e
					break;
				}
			}
		}
		
		if (rotation == 0){
			tm[0] = 1.0f;		//a
			tm[1] = 0.0f;		//b
			tm[2] = 0.0f;		//c
			tm[3] = 1.0f;		//d
			tm[5] = 0.0f;	
		}else{
			double angle = Math.PI;
			tm[0] = (float)Math.cos(angle);		//ap
			tm[1] = (float)Math.sin(angle);		//b
			tm[2] = (float)-Math.sin(angle);	//c
			tm[3] = (float)Math.cos(angle);		//d
			tm[5] = Format.mm2points(job.getBook().getBookHeight()).floatValue(); 	    //f
		}

		tm[4] = Format.mm2points(tm[4]).floatValue();

		return tm;
	}

	/**
	 * 
	 * @param xpos
	 * @param ypos
	 * @param canvas
	 * @param separation
	 */
	public void addHunkelerCueMark(float xpos, float ypos, PdfContentByte canvas, boolean separation) {
		float urx = Format.mm2points(job.getSheetWidth() - xpos - job.getHunkelerCueMark().getWidth()).floatValue();
		float llx = Format.mm2points(job.getSheetWidth() - xpos).floatValue();
		float lly = Format.mm2points(ypos + job.getHunkelerCueMark().getyPos()).floatValue();
		float ury = Format.mm2points(ypos + job.getHunkelerCueMark().getHeight() + job.getHunkelerCueMark().getyPos()).floatValue();
		Rectangle rect = new Rectangle(llx, lly, urx, ury);
		rect.setBackgroundColor(new CMYKColor(1f, 1f, 1f, 1f));
		canvas.rectangle(rect);
	}

	/**
	 * 
	 * @param xpos
	 * @param ypos
	 * @param cb
	 * @param text
	 * @param printtext
	 * @throws Exception
	 */
	public static void addDatamatrix(ImpositionJob job, float xpos, float ypos, PdfContentByte cb, String text, boolean printtext, float txtXPos)
			throws Exception {
		float xShift;
		float yShift;

		xShift = Format.mm2points(xpos).floatValue();

		// y-shift
		yShift = Format.mm2points(ypos).floatValue();

		/** iText **/
		BarcodeDatamatrix barcode = TextComposer.getDatamatrixCode(text);
		if (barcode != null) {

			Image image = barcode.createImage();
			image.scaleAbsolute(Format.mm2points(job.getHunkelerDatamatrix().getWidth()).floatValue(),
					Format.mm2points(job.getHunkelerDatamatrix().getHeight()).floatValue());
			image.setAbsolutePosition(xShift, yShift);

			cb.saveState();
			cb.setColorFill(BaseColor.WHITE);
			cb.rectangle(xShift - Format.mm2points(2.0f).floatValue(),
					yShift - Format.mm2points(2.0f).floatValue(),
					image.getWidth() + Format.mm2points(2.0f).floatValue(),
					image.getHeight() + Format.mm2points(2.0f).floatValue());
			cb.fill();
			cb.restoreState();

			// Add Barcode to PDF document
			cb.addImage(image);
		} else
			throw new Exception("datamatrix code is null...");

		if (printtext) {
			FontFactory.defaultEmbedding = true;

			
			
			
			BaseFont helvetica = BaseFont.createFont("Helvetica.otf", BaseFont.CP1252, BaseFont.EMBEDDED);
			Font font = new Font(helvetica, 8, Font.NORMAL);
			float w = font.getCalculatedBaseFont(true).getWidthPoint(text, font.getCalculatedSize());
			int align = Element.ALIGN_LEFT;
			if(job.isPloughFolderLine()){
				align = Element.ALIGN_RIGHT;
				w = -w;
			}
			
			ColumnText.showTextAligned(cb, align, new Phrase(text, font),
					Format.mm2points(txtXPos).floatValue() -w, Format.mm2points(job.getSheetHeight() - 4f).floatValue() - yShift, 0.0f);
		}
	}

	/**
	 * Creates a PdfReader object for the original PDF file.
	 * 
	 * @param srcfile
	 * @return
	 * @throws ComposerException
	 */
	protected static PdfReader openReader(String srcfile) throws ComposerException {
		PdfReader reader = null;
		try {
			reader = new PdfReader(srcfile);
		} catch (InvalidPdfException e) {
			String msg = "Corrupted PDF file. e: " + e.getMessage();
			throw new ComposerException(msg);
		} catch (Exception e) {
			String msg = "Exception while opening " + srcfile + ". " + e.getMessage();
			throw new ComposerException(msg);
		}

		return reader;
	}

	/**
	 * 
	 * @param reader
	 */
	protected void closeReader(PdfReader reader) {
		if (reader != null)
			reader.close();
	}

	/**
	 * Converts values of rectangle to points and returns new rectangle
	 * 
	 * @param box
	 * @return
	 */
	protected Rectangle getBoxSizeInPoints(Rectangle box) {
		Rectangle result = new Rectangle(Format.mm2points(box.getLeft()).floatValue(),
				Format.mm2points(box.getBottom()).floatValue(), Format.mm2points(box.getRight()).floatValue(),
				Format.mm2points(box.getTop()).floatValue());
		return result;
	}


	private String getBarcodeText(String page, String totalpages, String jobID, String glue, String pop)
			throws ComposerException {
		StringBuffer sb = new StringBuffer(31);

		sb.append(pop);

		for (int i = jobID.length(); i < 14; i++) {
			sb.append("0");
		}
		sb.append(jobID);

		for (int i = page.length(); i < 3; i++) {
			sb.append("0");
		}
		sb.append(page);

		for (int i = totalpages.length(); i < 3; i++) {
			sb.append("0");
		}
		sb.append(totalpages);

		// Perforation
		String perforation = job.isPloughFolderLine() ? String.valueOf(job.getPerforation()) : "0";
		sb.append(perforation);

		String cutLength = getCutLength();
		for (int i = cutLength.length(); i < 5; i++) {
			sb.append("0");
		}
		sb.append(cutLength);

		// Imposition
		sb.append(getFolds());

		// HCP
		sb.append("1");

		// WI6
		sb.append("1");
		
		//System.err.println("************************* getBarcodeText ************************* " + perforation);

		return sb.toString();
	}

	/**
	 * 1. item_id (13) 2. item_type_id ( 1) 3. item_version ( 3) 4. folds ( 2)
	 * 5. cut-length ( 4) 6. current signature ( 4) 7. total number of
	 * signatures ( 4) (31)
	 */
	private String getBarcodeText(int page, String jobID, String glue, String pop)
			throws ComposerException {
		StringBuffer sb = new StringBuffer(31);

		sb.append(pop);

		for (int i = jobID.length(); i < 14; i++) {
			sb.append("0");
		}
		sb.append(jobID);

		String currentSig = getCurrentSignature(page);
		for (int i = currentSig.length(); i < 3; i++) {
			sb.append("0");
		}
		sb.append(currentSig);

		int pagesPerSig = job.getComposition().getPagesPerSheet();
		String totalSignatures = Integer.toString(job.getBook().getFinalPageCount() / pagesPerSig);
		for (int i = totalSignatures.length(); i < 3; i++) {
			sb.append("0");
		}
		sb.append(totalSignatures);

		// Perforation
		String perforation = job.isPloughFolderLine() ? String.valueOf(job.getPerforation()) : "0";
		sb.append(perforation);

		String cutLength = getCutLength();
		for (int i = cutLength.length(); i < 5; i++) {
			sb.append("0");
		}
		sb.append(cutLength);

		// Imposition
		sb.append(getFolds());

		// HCP
		sb.append("1");

		// WI6
		sb.append("1");
		
		//System.err.println("************************* getBarcodeText ************************* " + perforation);

		return sb.toString();
	}

	/**
	 * 
	 * @param jobID
	 * @param bIdentifier
	 * @param height
	 * @param thikness
	 * @return
	 */
	public static String getMullerMartiniBarcode (String jobID, String bIdentifier, float height, float thikness){
		StringBuffer sb = new StringBuffer(); 


		for(int i=jobID.length() ; i<14; i++) {
			sb.append('0');
		}
		sb.append(jobID);		

		for(int i=bIdentifier.length() ; i<4; i++) {
			sb.append('0');
		} 
		sb.append(bIdentifier);

		String cThikness = getValueinMM(thikness);	
		for(int i=cThikness.length(); i<3; i++) {
			sb.append('0');
		}	
		sb.append(cThikness);

		String cHeight = getValueinMM(height);	
		for(int i=cHeight.length(); i<4; i++) {
			sb.append('0');
		}
		sb.append(cHeight);

		return sb.toString();
	}


	/**
	 * 
	 * @param valueInInch
	 * @return
	 */
	private static String getValueinMM(float valueInInch) {
		BigDecimal cutLength = BigDecimal.valueOf(valueInInch*10);
		String result = cutLength.setScale(0, RoundingMode.HALF_UP).toString(); 
		return result;
	}

	/**
	 * 
	 * @return
	 */
	private String getCutLength() {
		float cutlength = job.getSheetHeight();
		if (!job.isBigSheet())
			cutlength += job.getOutputControllerMargin();

		BigDecimal cutLength = BigDecimal.valueOf((cutlength) * 10);
		String result = cutLength.setScale(0, RoundingMode.HALF_UP).toString();
		return result;
	}

	/**
	 * 
	 * @return
	 * @throws NullPointerException
	 * @throws ComposerException
	 */
	protected Boolean isTwoUp() throws NullPointerException, ComposerException {
		return job.getBook().getBookWidth() > TextComposer.max3UpWidth;
	}

	/**
	 * 
	 * @param page
	 * @return
	 */
	private String getCurrentSignature(int page) {
		if (page < 0)
			return "000";

		float count = page;
		return Integer.toString(Math.round(count/2));
	}

	/**
	 * Field declared as folds contains the settings for the fly folder. 2up -->
	 * value: 02 3up --> value: 03 empty job --> value: 00
	 */
	private String getFolds() throws ComposerException {
		if (job.getComposition() == CompositionScheme.TWO_UP)
			return TextComposer.FOLD_ID_TWO_UP;
		return TextComposer.FOLD_ID_THREE_UP;
	}

	public ImpositionJob getJob() {
		return job;
	}

	public void setJob(ImpositionJob job) {
		this.job = job;
	}

	public List<Integer> getUpperSide() {
		return upperSide;
	}

	public void setUpperSide(List<Integer> upperSide) {
		this.upperSide = upperSide;
	}

	public List<Integer> getLowerSide() {
		return lowerSide;
	}

	public void setLowerSide(List<Integer> lowerSide) {
		this.lowerSide = lowerSide;
	}
	
}
