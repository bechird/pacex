package com.epac.imposition.cover;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.io.FileUtils;

import com.epac.imposition.bookblock.ComposerException;
import com.epac.imposition.config.Constants;
import com.epac.imposition.config.LogUtils;
import com.epac.imposition.utils.Format;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.exceptions.InvalidPdfException;
import com.itextpdf.text.pdf.BarcodeDatamatrix;
import com.itextpdf.text.pdf.BaseFont;
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

public class CoverImposer {
	
	private ImpositionJob 		mImpositionJob;
	private PdfReader 			mReader;
	private Float 				xShift = 0f;	
	private Float 				yShift = 0f;
	private Rectangle 			origTrimBox; 
	private Rectangle 			origBleedBox;
	private Rectangle 			origCropBox;

	private double calculatedThickness = 0;

	private String 				imposedFilePath;

	/**
	 * 
	 * @param job
	 * @throws ComposerException
	 */
	public CoverImposer(ImpositionJob job) throws ComposerException {	
		mImpositionJob = job;
		imposedFilePath = generateOutputPath().getAbsolutePath();
	}




	public CoverImposer(ImpositionJob mImpositionJob, String imposedFilePath) {
		super();
		this.mImpositionJob = mImpositionJob;
		this.imposedFilePath = imposedFilePath;
	}




	/**
	 * 
	 * @throws ComposerException
	 */
	public void compose() throws ComposerException {
		if (mImpositionJob == null)
			throw new ComposerException("NullPointerException... The imposition is not initialized...");

		System.out.println("Start composing " + mImpositionJob.getCover().getFilePath());
		System.out.println("result file will be: " + imposedFilePath); 
		try {		
			cropPdf(mImpositionJob.getCover().getFilePath(), imposedFilePath);
			FileUtils.copyFile(new File(imposedFilePath), new File(mImpositionJob.getCover().getFilePath()), false);

			mReader = openReader(mImpositionJob.getCover().getFilePath());

			getItemSettingsFromPdf();

			chooseCoverSheet();

			setPageShift();

			createComposedFile();

			closeReader(mReader);

			System.out.println("Finished composing producible item " + generateOutputPath() + ")");

		} catch(Exception e) {
			LogUtils.error("Cannot compose producible item "+mImpositionJob.getCover().getFilePath(), e);
			String msg = "Cannot compose producible item " + mImpositionJob.getCover().getFilePath();
			throw new ComposerException(msg, e);

		}
	}

	private void chooseCoverSheet() throws ComposerException{
		LogUtils.debug("Choose cover sheet size");
		
		float printerMargins = Float.parseFloat(System.getProperty(Constants.COVER_PRINTER_MARGIN));
		float totalMargins = Float.parseFloat(System.getProperty(Constants.COVER_TOTAL_MARGIN));
		float coverBleed	= Float.parseFloat(System.getProperty(Constants.COVER_BLEED_VALUE));
				
		float coverWidth  = (float) (mImpositionJob.getCover().getBleedboxWidth() + (2*printerMargins));
		float coverHeight = (float) (mImpositionJob.getCover().getTrimboxHeight() + totalMargins + coverBleed);
		
		float mSheetHeight = Float.parseFloat(System.getProperty(Constants.IMPOSITION_M_SHEET_HEIGHT));
		float mSheetWidth  = Float.parseFloat(System.getProperty(Constants.IMPOSITION_M_SHEET_WIDTH));
		
		float sheetHeight;
		float sheetWidth;
		
		if(coverWidth > mSheetWidth || coverHeight > mSheetHeight ){
			
			float lSheetHeight = Float.parseFloat(System.getProperty(Constants.IMPOSITION_L_SHEET_HEIGHT));
			float lSheetWidth  = Float.parseFloat(System.getProperty(Constants.IMPOSITION_L_SHEET_WIDTH));
			
			if(coverWidth > lSheetWidth || coverHeight > lSheetHeight ){
				
				float xlSheetHeight = Float.parseFloat(System.getProperty(Constants.IMPOSITION_XL_SHEET_HEIGHT));
				float xlSheetWidth  = Float.parseFloat(System.getProperty(Constants.IMPOSITION_XL_SHEET_WIDTH));
				
				if(coverWidth > xlSheetWidth || coverHeight > xlSheetHeight ){
					try{ mReader.close();}catch(Exception e){}
					throw new ComposerException("Cover does not fit into any sheet M nor XL. Please check the settings.");
				}else{
					sheetHeight = xlSheetHeight;
					sheetWidth  = xlSheetWidth;
					LogUtils.debug("Chosen cover sheet size is XL");
				}
			}else{
				sheetHeight = lSheetHeight;
				sheetWidth  = lSheetWidth;
				LogUtils.debug("Chosen cover sheet size is XL");
			}
			
			
			
			
		}else{
			sheetHeight = mSheetHeight;
			sheetWidth  = mSheetWidth;
			LogUtils.debug("Chosen cover sheet size is M");
		}
		
		
		mImpositionJob.setSheetHeight(sheetHeight);
		mImpositionJob.setSheetWidth(sheetWidth);

	}

	/**
	 * 
	 * @return
	 */
	private File generateOutputPath (){

		String filePath = mImpositionJob.getCover().getFilePath();
		int index = filePath.lastIndexOf(File.separatorChar);
		if(index < 0)
			index = 0;

		String filename = filePath.substring(index);


		File output = new File(System.getProperty(Constants.IMPOSER_COVER_OUTPUT_FOLDER));
		if(!output.exists())
			output.mkdirs();

		return new File(output, filename);
	}


	/**
	 * 
	 * @throws ComposerException
	 */
	private void getItemSettingsFromPdf() throws ComposerException {
		System.out.println("Retrieving item settings from PDF ....");
		try {	
			getBoxes();
			updateProductSize();

			calculatedThickness = mImpositionJob.getCover().getThikness();
			if (origTrimBox != null){
				calculatedThickness = mImpositionJob.getCover().getTrimboxWidth() - (mImpositionJob.getCover().getFinalWidth()*2);
			}
			
			mImpositionJob.setCalculatedThickness(calculatedThickness);
		} catch(Exception e) {
			String msg = "Cannot retrieve item settings from " + mImpositionJob.getCover().getFilePath() + ". e:" + e.getMessage();
			throw new ComposerException(msg);
		}
	} 

	/**
	 * 
	 * @throws ComposerException
	 */
	private void updateProductSize() throws ComposerException {
		
		float coverBleed	= Float.parseFloat(System.getProperty(Constants.COVER_BLEED_VALUE));
		
		if (origTrimBox != null){
			mImpositionJob.getCover().setTrimboxWidth(Format.points2mm(origTrimBox.getWidth()).floatValue());
			mImpositionJob.getCover().setTrimboxHeight(Format.points2mm(origTrimBox.getHeight()).floatValue());
		}else{
			Rectangle page = mReader.getPageSize(1);
			mImpositionJob.getCover().setTrimboxWidth(Format.points2mm(page.getWidth()).floatValue());
			mImpositionJob.getCover().setTrimboxHeight(Format.points2mm(page.getHeight()).floatValue());
		}

		mImpositionJob.getCover().setBleedboxWidth(mImpositionJob.getCover().getTrimboxWidth() + coverBleed*2);
		mImpositionJob.getCover().setBleedboxHeight(mImpositionJob.getCover().getTrimboxHeight() + coverBleed*2);
		/*
		if (origBleedBox != null){
			mImpositionJob.getCover().setBleedboxWidth(Format.points2mm(origBleedBox.getWidth()).floatValue());
			mImpositionJob.getCover().setBleedboxHeight(Format.points2mm(origBleedBox.getHeight()).floatValue());
		}else{
			Rectangle page = mReader.getPageSize(1);
			mImpositionJob.getCover().setBleedboxWidth(Format.points2mm(page.getWidth()).floatValue());
			mImpositionJob.getCover().setBleedboxHeight(Format.points2mm(page.getHeight()).floatValue());
		}*/
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
		DataInputStream dis = new DataInputStream(new FileInputStream(pdfFilePath));
		PdfReader reader = new PdfReader(dis);
		
		
		try {
			float coverBleed	= Format.mm2points(Float.parseFloat(System.getProperty(Constants.COVER_BLEED_VALUE))).floatValue();
			
			
		
			
			for (int i = 1; i <= reader.getNumberOfPages(); i++) {

				PdfDictionary pdfDictionary = reader.getPageN(i);
				Rectangle trimbox = reader.getBoxSize(i, "trim");
				PdfArray bleedBox = new PdfArray();
				
				if (trimbox == null){
					throw new ComposerException("Neither bleed or trim box are set for " + mImpositionJob.getCover().getFilePath());
				}
				
				bleedBox.add(new PdfNumber(trimbox.getLeft() - coverBleed));
				bleedBox.add(new PdfNumber(trimbox.getBottom() - coverBleed));
				bleedBox.add(new PdfNumber(trimbox.getRight()  + coverBleed));
				bleedBox.add(new PdfNumber(trimbox.getTop() + coverBleed));

				pdfDictionary.put(PdfName.CROPBOX, bleedBox);
				pdfDictionary.put(PdfName.MEDIABOX, bleedBox);
				pdfDictionary.put(PdfName.BLEEDBOX, bleedBox);
			}



			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(filename));
			stamper.close();
		}finally {
			reader.close();
		}
	}

	/** Creates a PdfReader object for the original PDF file. **/
	private PdfReader openReader(String srcfile) throws ComposerException {
		PdfReader reader = null;
		try {
			reader = new PdfReader(srcfile);

		} catch(InvalidPdfException e) {
			String msg = "Corrupted PDF file. e: " + e.getMessage();
			throw new ComposerException(msg);
		} catch(Exception e) { 
			String msg = "Exception while opening " + srcfile + ". " + e.getMessage();
			throw new ComposerException(msg); 
		}

		return reader;
	}

	/** closes a specified reader if it's already opened. **/
	private void closeReader(PdfReader reader) {
		if(reader != null) 
			reader.close();
	}


	/**
	 * 
	 * @throws ComposerException
	 * @throws DocumentException
	 * @throws IOException
	 */
	private void createComposedFile() throws ComposerException, DocumentException, IOException {
		LogUtils.debug("Creating composed file ...");

		Document doc = null;
		PdfWriter writer = null;
		String sheetType = "M";
		try {

			float sheetWidth    = mImpositionJob.getSheetWidth();
			float mSheetWidth   = Float.parseFloat(System.getProperty(Constants.IMPOSITION_M_SHEET_WIDTH));
			float lSheetWidth   = Float.parseFloat(System.getProperty(Constants.IMPOSITION_L_SHEET_WIDTH));
			float xlSheetWidth  = Float.parseFloat(System.getProperty(Constants.IMPOSITION_XL_SHEET_WIDTH));

			if(sheetWidth == mSheetWidth)
				sheetType = "M";
			else if(sheetWidth == lSheetWidth)
				sheetType = "L";
			else if(sheetWidth == xlSheetWidth)
				sheetType = "XL";
			
			File f = new File(mImpositionJob.getCover().getFilePath());
			String filename = f.getName();

			// create a new document with the new media size
			Rectangle rectFile = new Rectangle(mImpositionJob.getSheetWidth(), mImpositionJob.getSheetHeight());
			// create a new document with the new media size
			doc = new Document(getBoxSizeInPoints(rectFile));

			String output = imposedFilePath;
			// create a new Writer object that writes content to the new PDF  
			FileOutputStream file = new FileOutputStream(output);

			writer = PdfWriter.getInstance(doc, file);

			// open the document
			doc.open();

			// Retrieve an instance of the ContentByte to add changes to the imported PDF pages
			// PdfImportedPages cannot be changed, the direct content is needed to do this.
			PdfContentByte cb = writer.getDirectContent();
			float coverBleed	= Format.mm2points(Float.parseFloat(System.getProperty(Constants.COVER_BLEED_VALUE))).floatValue();

			for (int i = 1; i <= mReader.getNumberOfPages(); i++){

				doc.newPage();
				writer.setPageEmpty(false); 

				PdfImportedPage page = writer.getImportedPage(mReader, i);
				Float[] tm = getPageTransformationMatrix();
				cb.addTemplate(page, tm[0], tm[1], tm[2], tm[3], tm[4], tm[5] );

				float llx = xShift + coverBleed;
				float lly = yShift + coverBleed;

				float urx = llx + origTrimBox.getWidth();
				float ury = lly + origTrimBox.getHeight();
				LogUtils.debug("xShift: "+Format.points2mm(xShift));
				LogUtils.debug("yShift: "+Format.points2mm(yShift));
				LogUtils.debug("llx: "+Format.points2mm(llx));
				LogUtils.debug("lly: "+Format.points2mm(lly));
				LogUtils.debug("urx: "+Format.points2mm(urx));
				LogUtils.debug("ury: "+Format.points2mm(ury));


				drawCutMark(cb, llx, lly, urx, ury);

				/* draw thickness lines */
				float xPos = mImpositionJob.getFoldingEdgeXpos();

				float xPos1 = (float) (mImpositionJob.getFoldingEdgeXpos() - mImpositionJob.getCalculatedThickness());

				float xPos2 = (float) (mImpositionJob.getFoldingEdgeXpos() - mImpositionJob.getCalculatedThickness()/2);

				cb.setLineWidth(0.5f);
				drawVerticalLines(cb, xPos, Format.points2mm(yShift).floatValue() - 1.0f, 
						Format.points2mm(yShift - 10.0f).floatValue() - 1.0f);

				//center line
				drawVerticalLines(cb, xPos2, Format.points2mm(yShift).floatValue() - 1.0f,
						Format.points2mm(yShift - 10.0f).floatValue() - 1.0f);

				drawVerticalLines(cb, xPos1, Format.points2mm(yShift).floatValue() - 1.0f,
						Format.points2mm(yShift - 10.0f).floatValue() - 1.0f);

				drawVerticalLines(cb, xPos, mImpositionJob.getCover().getBleedboxHeight() + Format.points2mm(yShift).floatValue() + 1.0f, 
						mImpositionJob.getCover().getBleedboxHeight() + Format.points2mm(yShift + 10.0f).floatValue() + 1.0f);

				//center line
				drawVerticalLines(cb, xPos2, mImpositionJob.getCover().getBleedboxHeight() + Format.points2mm(yShift).floatValue() + 1.0f,
						mImpositionJob.getCover().getBleedboxHeight() + Format.points2mm(yShift + 10.0f).floatValue() + 1.0f);

				drawVerticalLines(cb, xPos1, mImpositionJob.getCover().getBleedboxHeight() + Format.points2mm(yShift).floatValue() + 1.0f,
						mImpositionJob.getCover().getBleedboxHeight() + Format.points2mm(yShift + 10.0f).floatValue() + 1.0f);

				//MM vision system marks 
				cb.setLineWidth(Format.mm2points(0.5f).floatValue());
				
				float thirdXpos = xPos;
				float secondXpos = xPos1;
				float firstXpos = thirdXpos - 65.0f;
				
				// thrid mark
				drawVerticalLines(cb, thirdXpos, mImpositionJob.getSheetHeight() - 3.0f, 
						mImpositionJob.getSheetHeight() - 6.0f);


				String alignment = mImpositionJob.getCover().getBook().getBook().getMetadata().getCoverAlignment();
				ScoringAlignment scroing = ScoringAlignment.CENTER;
				if(alignment != null){
					switch (alignment) {
					case "C":
						scroing = ScoringAlignment.CENTER;
						break;
	
					case "B":
						scroing = ScoringAlignment.BACK;
						break;
	
					case "F":
						scroing = ScoringAlignment.FRONT;
						break;
					}
				}
				if(scroing == ScoringAlignment.BACK)
					drawHorizontalLine(cb, thirdXpos - 1.75f, thirdXpos, mImpositionJob.getSheetHeight() - 4.5f );
				else if(scroing == ScoringAlignment.FRONT)
					drawHorizontalLine(cb, thirdXpos + 1.75f, thirdXpos, mImpositionJob.getSheetHeight() - 4.5f );
				else
					drawHorizontalLine(cb, thirdXpos - 1.75f, thirdXpos + 1.75f, mImpositionJob.getSheetHeight() - 4.5f );			
				// End third mark

				
				// second mark 
				drawVerticalLines(cb, secondXpos, mImpositionJob.getSheetHeight() - 3.0f, 
						mImpositionJob.getSheetHeight() - 6.0f);
				drawHorizontalLine(cb, secondXpos - 1.75f, secondXpos, mImpositionJob.getSheetHeight() - 4.5f );
				// End second mark
				
				
				//first mark
				drawVerticalLines(cb, firstXpos, mImpositionJob.getSheetHeight() - 3.0f, 
						mImpositionJob.getSheetHeight() - 6.0f);
				
				drawHorizontalLine(cb, firstXpos - 1.75f, firstXpos, mImpositionJob.getSheetHeight() - 4.5f );
				// End first mark
				
				if (i == 1){
					//add bottom datamatrix
					String bText = getBarcodeText(mImpositionJob.getCover().getDocId().concat(Constants.MATERIAL_TYPE_COVER), mImpositionJob.getCover().getRawheight(),
							mImpositionJob.getCalculatedThickness(), mImpositionJob.getCover().getFrontMargin());

					mImpositionJob.getBinderDatamatrix().setContent(bText);

					float bXpos = (float)(mImpositionJob.getFoldingEdgeXpos()
							+ mImpositionJob.getBinderDatamatrix().getxPos());
					float bYpos = mImpositionJob.getSheetHeight() - 
							mImpositionJob.getBinderDatamatrix().getyPos() -
							mImpositionJob.getBinderDatamatrix().getHeight();

					mImpositionJob.getBinderDatamatrix().setyPos(bYpos);
					mImpositionJob.getBinderDatamatrix().setxPos(bXpos);

					drawDatamatrix(cb, mImpositionJob.getBinderDatamatrix(), 0);


					//add Trimmer datamatrix
					String tText = BarcodeGenerator.getMullerMartiniBarcode(mImpositionJob.getCover().getDocId().concat(Constants.MATERIAL_TYPE_BOUNDBOOK), mImpositionJob.getCover().getFinalHeight(),
							mImpositionJob.getCover().getFinalWidth(), mImpositionJob.getyPosition());

					mImpositionJob.getTrimmerDatamatrix().setContent(tText);
//					float tXpos = (float) (mImpositionJob.getFoldingEdgeXpos() 
//							- mImpositionJob.getCalculatedThickness() - mImpositionJob.getTrimmerDatamatrix().getxPos() - mImpositionJob.getTrimmerDatamatrix().getWidth());
				
					float tXpos = secondXpos - 70.0f - (mImpositionJob.getTrimmerDatamatrix().getWidth() - 2.0f);
					mImpositionJob.getTrimmerDatamatrix().setxPos(tXpos);

					// if not defined, put binding DM 6mm above trim
					if(mImpositionJob.getTrimmerDatamatrix().getyPos() == 0){
						bYpos =  mImpositionJob.getSheetHeight() + 2 /* 2mm above bleedbox*/ + 2f /* quite zone (white) on the barcode*/- mImpositionJob.getCover().getBleedboxHeight() - Float.parseFloat(System.getProperty(Constants.IMPOSITION_COVER_DATAMATRIX_TRIMMER_OFFSET, "0")) -  mImpositionJob.getyPosition();
						mImpositionJob.getTrimmerDatamatrix().setyPos(bYpos);
					}else{
						bYpos =  mImpositionJob.getSheetHeight() - 
								mImpositionJob.getTrimmerDatamatrix().getyPos() - 
								mImpositionJob.getTrimmerDatamatrix().getHeight();;
								mImpositionJob.getTrimmerDatamatrix().setyPos(bYpos);
					}

					drawDatamatrix(cb, mImpositionJob.getTrimmerDatamatrix(), 0);
					float x1 = bXpos + mImpositionJob.getBinderDatamatrix().getWidth() +  10.0f;
					float x2 = mImpositionJob.getSheetWidth();
					float y = mImpositionJob.getSheetHeight() - 6.0F;
					String bookId = String.valueOf(mImpositionJob.getCover().getBook().getBook().getId());

					com.epac.imposition.bookblock.ImpositionJob.addImpositionDate(cb,x1, x2, y, bookId,sheetType, false );
				}

				

				String text = "0.6 " + scroing.getLetter();
				if ((filename.indexOf("_0.4_") != -1) || (filename.indexOf("_0,4_") != -1)) {
					text = "0.4 " + scroing.getLetter();
				}
				BaseFont helvetica = BaseFont.createFont("Helvetica.otf", "Cp1252", true);
				Font font = new Font(helvetica, 8.0F, 0);
				float w = font.getCalculatedBaseFont(true).getWidthPoint(text, font.getCalculatedSize());
				ColumnText.showTextAligned(cb, 0, new Phrase(text, font), 
						Format.mm2points(Float.valueOf(xPos2 - 3.4F)).floatValue(), Format.mm2points(Float.valueOf(mImpositionJob.getCover().getBleedboxHeight() + 4.0F)).floatValue() + yShift.floatValue(), 0.0F);

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
				LogUtils.error("Cannot close document/writer." + e);
			}

			try{

				File input = new File(imposedFilePath);
				File parent = input.getParentFile();

				File output = new File(parent, sheetType.concat("_").concat(input.getName()));
				if(output.exists())
					output.delete();

				FileUtils.moveFile(input, output);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	
	/**
	 * 
	 * @param cb
	 */
	public void setupPeterMark (PdfContentByte cb){

	}

	/**
	 * 
	 * @param cb
	 * @param xPos
	 * @param yStart
	 * @param yEnd
	 */
	private void drawVerticalLines (PdfContentByte cb, float xPos, float yStart, float yEnd){

		cb.setColorStroke(BaseColor.BLACK);
		cb.moveTo(Format.mm2points(xPos).floatValue(), Format.mm2points(yStart).floatValue());
		cb.lineTo(Format.mm2points(xPos).floatValue(), Format.mm2points(yEnd).floatValue());
		cb.stroke();
	}
	
	/**
	 * 
	 * @param cb
	 * @param xStart
	 * @param xEnd
	 * @param yPos
	 */
	private void drawHorizontalLine(PdfContentByte cb, float xStart, float xEnd, float yPos)
	{
		cb.setColorStroke(BaseColor.BLACK);
		cb.moveTo(Format.mm2points(Float.valueOf(xStart)).floatValue(), Format.mm2points(Float.valueOf(yPos)).floatValue());
		cb.lineTo(Format.mm2points(Float.valueOf(xEnd)).floatValue(), Format.mm2points(Float.valueOf(yPos)).floatValue());
		cb.stroke();
	}


	/**
	 * 
	 * @param cb
	 * @param x
	 */
	private void drawCutMark(PdfContentByte cb, float llx, float lly, float urx, float ury) {

		final LineSeparator lineSeparator = new LineSeparator();
		lineSeparator.setLineWidth(0.5f);
		cb.setLineWidth(0.5f);

		cb.setColorStroke(BaseColor.BLACK);
		float from = 4.0f;
		float to   = 6.0f;
		//left
		// vertical
		lineSeparator.drawLine(cb, llx - Format.mm2points(to).floatValue(), llx - Format.mm2points(from).floatValue(), lly);
		cb.moveTo(llx, lly - Format.mm2points(from).floatValue());
		cb.lineTo(llx, lly - Format.mm2points(to).floatValue());
		cb.stroke();
		//

		//
		lineSeparator.drawLine(cb, llx - Format.mm2points(to).floatValue(), llx - Format.mm2points(from).floatValue(), ury);
		cb.moveTo(llx, ury + Format.mm2points(from).floatValue());
		cb.lineTo(llx, ury + Format.mm2points(to).floatValue());
		cb.stroke();
		//

		//right
		//
		lineSeparator.drawLine(cb, urx + Format.mm2points(to).floatValue(), urx + Format.mm2points(from).floatValue(), ury);
		cb.moveTo(urx, ury + Format.mm2points(from).floatValue());
		cb.lineTo(urx, ury + Format.mm2points(to).floatValue());
		cb.stroke();
		//

		//
		lineSeparator.drawLine(cb, urx + Format.mm2points(to).floatValue(), urx + Format.mm2points(from).floatValue(), lly);
		cb.moveTo(urx, lly - Format.mm2points(from).floatValue());
		cb.lineTo(urx, lly - Format.mm2points(to).floatValue());
		cb.stroke();
		//
	}

	/**
	 * 
	 * @throws ComposerException
	 */
	private void setPageShift() throws ComposerException {	
		// if folding edge is not defined or set to zero, center the cover
		if(mImpositionJob.getFoldingEdgeXpos() == 0){
			float fEdge = (float) (mImpositionJob.getSheetWidth()/2 + mImpositionJob.getCalculatedThickness()/2);
			mImpositionJob.setFoldingEdgeXpos(fEdge);
		}

		xShift = Format.mm2points(mImpositionJob.getFoldingEdgeXpos()).floatValue()
				- origBleedBox.getWidth()/2 - Format.mm2points((float)mImpositionJob.getCalculatedThickness()).floatValue()/2;
		float coverBleed	= Float.parseFloat(System.getProperty(Constants.COVER_BLEED_VALUE));
		float ypos = Format.mm2points( + mImpositionJob.getyPosition() - coverBleed).floatValue();
		yShift = ypos ;


	}


	/**
	 * 
	 * @return
	 * @throws ComposerException
	 */
	private Float[] getPageTransformationMatrix() throws ComposerException {

		Float[] tm = new Float[6];
		tm[0] = 1.0f;		//a
		tm[1] = 0.0f;		//b
		tm[2] = 0.0f;		//c
		tm[3] = 1.0f;		//d
		tm[4] = xShift;		//e
		tm[5] = yShift; 	//f	

		return tm;
	}

	/**
	 * 
	 * @throws ComposerException
	 */
	protected void getBoxes() throws ComposerException {
		origCropBox = mReader.getBoxSize(1, "crop"); 
		origTrimBox = mReader.getBoxSize(1, "trim"); 
		origBleedBox = mReader.getBoxSize(1, "bleed");

		if (origBleedBox == null){
			if (origTrimBox == null){
				if (origCropBox == null){
					System.out.println("File can not be imposed, neither bleed or trim box or crop box are set.");
					throw new ComposerException("neither bleed or trim box or crop box are set " + mImpositionJob.getCover().getFilePath());
				}
				
			}
			
		}
		
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
	private void drawDatamatrix (PdfContentByte cb, Datamatrix datamatrix, float rotation) throws Exception{

		float xShift =  Format.mm2points(datamatrix.getxPos()).floatValue();
		float yShift = Format.mm2points(datamatrix.getyPos()).floatValue();

		/** iText **/				
		BarcodeDatamatrix barcode = getDatamatrixCode(datamatrix.getContent());
		if (barcode != null){

			Image image = barcode.createImage();
			image.setRotationDegrees(rotation);
			image.scaleAbsolute(Format.mm2points(datamatrix.getWidth()).floatValue(), Format.mm2points(datamatrix.getHeight()).floatValue());

			image.setBorder(Format.mm2points(2.5f).intValue());

			image.setAbsolutePosition(xShift, yShift); 

			cb.saveState();
			cb.setColorFill(BaseColor.WHITE);
			cb.rectangle(xShift - Format.mm2points(2.0f).floatValue(),
					yShift - Format.mm2points(2.0f).floatValue(),
					image.getWidth() + Format.mm2points(6.0f).floatValue(),
					image.getHeight() + Format.mm2points(6.0f).floatValue());
			cb.fill();
			cb.restoreState();

			//Add Barcode to PDF document
			cb.addImage(image);
		}
		else
			throw new Exception("datamatrix code is null...");
	}

	/**
	 * Oumaima Kridene (generate datamatrix from text)
	 * @param text
	 * @return
	 * @throws Exception
	 */
	private static BarcodeDatamatrix getDatamatrixCode (String text) throws Exception{
		// supported square barcode dimensions
		//int[] barcodeDimensions = {10, 12, 14, 16, 18, 20, 22, 24, 26, 32, 36, 40, 44, 48, 52, 64, 72, 80, 88, 96, 104, 120, 132, 144};

		BarcodeDatamatrix barcode = new BarcodeDatamatrix();
		barcode.setOptions(BarcodeDatamatrix.DM_AUTO);
		barcode.setWidth(36);
		barcode.setHeight(12);
		//barcode.setWs(3);

		int returnResult = barcode.generate(text);
		if (returnResult == BarcodeDatamatrix.DM_NO_ERROR) {
			return barcode;
		}
		return null;
	}


	/**
	 * 
	 * @param jobID
	 * @param thikness
	 * @param spine
	 * @return
	 * @throws ComposerException
	 */
	private String getBarcodeText(String jobID, float height, double spine, float margin) throws ComposerException {
		StringBuffer sb = new StringBuffer(33); 

		for(int i=jobID.length() ; i<14; i++) {
			sb.append("0");
		}
		sb.append(jobID);

		String sHeight = getFloatAsString(height);
		for(int i=sHeight.length() ; i<4; i++) {
			sb.append("0");
		}
		sb.append(sHeight);

		String sSpine = getFloatAsString(spine);
		for(int i=sSpine.length() ; i<3; i++) {
			sb.append("0");
		}
		sb.append(sSpine);

		String sMargin = getFloatAsString(margin);
		for(int i=sMargin.length() ; i<3; i++) {
			sb.append("0");
		}
		sb.append(sMargin);

		System.out.println(sb.toString());
		return sb.toString();
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	private String getFloatAsString(double value) {
		BigDecimal cutLength = BigDecimal.valueOf(value*10);
		String result = cutLength.setScale(0, RoundingMode.HALF_UP).toString(); 
		return result;
	}

	/** Converts values of rectangle to points and returns new rectangle **/
	protected Rectangle getBoxSizeInPoints(Rectangle box) {
		System.out.println("getBoxSizeInPoints: "+box.getLeft()+"-"+box.getBottom()+"-"+box.getRight()+"-"+box.getTop());
		Rectangle result = new Rectangle(Format.mm2points(box.getLeft()).floatValue(), 
				Format.mm2points(box.getBottom()).floatValue(), 
				Format.mm2points(box.getRight()).floatValue(), 
				Format.mm2points(box.getTop()).floatValue());
		return result; 
	}

	/**
	 * 
	 * @param msg
	 */
	protected void addEvent(String msg) {
		System.out.println(msg);
	}
}
