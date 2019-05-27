package com.epac.imposition.model;

import java.math.BigDecimal;

import com.epac.imposition.bookblock.ComposerException;
import com.epac.imposition.bookblock.ImpositionJob;
import com.epac.imposition.utils.Format;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.exceptions.InvalidPdfException;
import com.itextpdf.text.pdf.Barcode;
import com.itextpdf.text.pdf.BarcodeDatamatrix;
import com.itextpdf.text.pdf.PdfReader;

/** The Composer and its derived classes rely on the external itext library, that
internally works with points (1 point => 1/72 inch). */

public abstract class Composer {

	protected static final int JOBTICKET_OCE_2_UP = 1;
	protected static final int JOBTICKET_OCE_3_UP = 2;
	protected static final int JOBTICKET_NEXPRESS = 3;

	protected ImpositionJob job;
	
	protected String srcFilename; 
	protected String composedFilename; 

	protected Rectangle origMediaBox;  
	protected Rectangle origTrimBox; 
	protected Rectangle origBleedBox;
	protected Rectangle origCropBox;

	protected Rectangle mediaBox;
	protected Rectangle trimBox;
	protected Rectangle bleedBox; 
	protected Rectangle cropBox;

	protected Rectangle referenceBox;		// either crop or trim box of original PDF

	protected Float paperWidth; 
	protected Float paperHeight; 

	protected Rectangle pretrimSize; 
	protected Rectangle precutSize; 
	protected Float bleed;

	protected Integer originalNoOfPages; 
	protected Integer finalNoOfPages; 

	protected Float rotation;

	public static float max3UpWidth;
	public static float max4UpWidth;
	protected PdfReader reader;

	public enum BarcodeType {
		CODE128(Barcode.CODE128),
		CODE_EAN13(Barcode.EAN13);

		private int type; 

		private BarcodeType(int type) {
			this.type = type; 
		}

		public int getType() { return type; }
	}

	public Composer(ImpositionJob job) throws ComposerException {
		this.job = job;
		this.srcFilename = job.getBook().getFilePath();
		this.composedFilename = job.getBook().getTempOutputFilePath();
	}

	protected Boolean isTowUp() throws NullPointerException, ComposerException {
		return job.getBook().getBookWidth() > max3UpWidth ;
	}

	public void setSrcFilename(String file) { srcFilename = file; }
	public String getSrcFilename() { return srcFilename; }

	public void setComposedFilename(String file) { composedFilename = file; }
	public String getComposedFilename() { return composedFilename; }

	/** Returns the trim box of the composed PDF. */
	public Rectangle getTrimBox() { return trimBox; }

	/** Returns the media box of the composed PDF . */
	public Rectangle getMediaBox() { return mediaBox; }

	/** Returns the bleed box of the composed PDF . */
	public Rectangle getBleedBox() { return bleedBox; }



	/** Returns the trim box of the original PDF. */
	public Rectangle getOrigTrimBox() { return origTrimBox; }

	/** Returns the crop box of the original PDF . */
	public Rectangle getOrigCropBox() { return origCropBox; }

	/** Returns the media box of the original PDF . */
	public Rectangle getOrigMediaBox() { return origMediaBox; }

	/** Returns the bleed box of the original PDF . */
	public Rectangle getOrigBleedBox() { return origBleedBox; }

	/** Sets the default margin around the trim box. The cover image
		is printed beyond the border of the trim box to prohibit
		white verges at the trimmed edge. */
	public void setBleed(Float bleed) { this.bleed = bleed; }
	public Float getBleed() { return bleed; }

	/*
	public Item getItem() { return item; }
	public void setItem(Item item) { this.item = item; }
	 */
	/*
	public void setPaperType(PaperType type) { paperType = type; }
	public PaperType getPaperType() { return paperType; }
	 */

	public void setPaperWidth(Float width) { paperWidth = width; }
	public Float getPaperWidth() { return paperWidth; }

	public void setPaperHeight(Float height) { paperHeight = height; }
	public Float getPaperHeight() { return paperHeight; }


	/** Implements all the necessary steps to compose a PDF, which 
	    are different for loose leaf stack, book block or cover.
	    The composed file will be saved under the given file path. */
	public abstract void compose() throws ComposerException;


	/** Returns the results of the composing as a XML string */
	public abstract String getReport();


	/** Creates a PdfReader object for the original PDF file. */
	protected void openReader(String srcfile) throws ComposerException {
		System.out.println("Opening PDF ...");	
		//job.addEvent("Opening PDF file " + srcFilename + " ...");
		try {
			reader = new PdfReader(srcfile);

		} catch(InvalidPdfException e) {
			String msg = "Corrupted PDF file. e: " + e.getMessage();
			//job.setState(JobStage.ABORTED, JobStatus.COMPOSITION_ERROR, msg);
			throw new ComposerException(msg);
		} catch(Exception e) { 
			String msg = "Exception while opening " + getSrcFilename() + ". " + e.getMessage();
			//job.setState(ChildJob.JobStage.ABORTED, ChildJob.JobStatus.PDF_ERROR, msg);
			throw new ComposerException(msg); 
		}
	}


	protected void getItemSettingsFromPdf() throws ComposerException {
		System.out.println("Retrieving item settings from PDF ....");
		//job.addEvent("Retrieving settings from PDF ...");
		try {	
			//job.addEvent("Retrieving " + job.getItem().getItemType().getName() + " settings from PDF ...");
			getBoxes();
			updateProductSize();
			getOriginalNoOfPages();

		} catch(Exception e) {
			String msg = "Cannot retrieve item settings from " + srcFilename + ". e:" + e.getMessage();
			//job.setState(ChildJob.JobStage.ABORTED, ChildJob.JobStatus.COMPOSITION_ERROR, msg);
			throw new ComposerException(msg);
		}
	} 

	protected void getOriginalNoOfPages() throws ComposerException {
		try {
			//Item item = job.getItem();
			originalNoOfPages = reader.getNumberOfPages();
			if(originalNoOfPages == null) {
				//job.setState(JobStage.ABORTED, JobStatus.COMPOSITION_ERROR, "Cannot retrieve page count from PDF.");
				throw new ComposerException("Cannot retrieve number of pages from PDF."); 
			}

			if(job.getBook().getBookNbrPages() == 0) {
				job.getBook().setBookNbrPages(originalNoOfPages);
			}
			
			System.out.println("Final number of pages = "+originalNoOfPages);
			
		} catch(ComposerException e) {
			throw new ComposerException("Cannot retrieve original number of pages for item " + job.getBook().getDocId() + ". e: " + e.getMessage());
		}
		//job.addEvent("Found " + originalNoOfPages + " pages in PDF ...");
	}

	protected void getBoxes() throws ComposerException {
		//System.out.println(job.getJobId() + ": Retrieving box information from PDF ... ");
		origMediaBox = reader.getBoxSize(2, "media");
		origCropBox = reader.getBoxSize(2, "crop"); 
		origTrimBox = reader.getBoxSize(2, "trim"); 
		origBleedBox = reader.getBoxSize(2, "bleed");

		if (Float.compare(origBleedBox.getWidth(), origTrimBox.getWidth()) != 0
				&& Float.compare(origBleedBox.getHeight(), origTrimBox.getHeight()) != 0)
			job.setTextWithBleed(true);
		referenceBox = origBleedBox;
		if(referenceBox == null)
			referenceBox = origTrimBox;
		if(referenceBox == null) {
			System.out.println("Neither corp or trim box are set.");
			//job.setState(ChildJob.JobStage.ABORTED, ChildJob.JobStatus.COMPOSITION_ERROR, "Missing trim or crop box."); 
			throw new ComposerException("Neither crop or trim box are set for " + getSrcFilename()); 
		}
		//job.addEvent("Trim/Crop box " + Format.points2mm(referenceBox.getWidth()) + " x " + Format.points2mm(referenceBox.getHeight()) + " mm (width x height)");

	}

	protected String getBoxDetails(String name, Rectangle r) {
		return "Found " + name + " " + r.toString() + "- " + r.getLeft() + "," + r.getBottom() + ")";
	}

	private void updateProductSize() throws ComposerException {
		if(job.getBook().getBookWidth() == 0) { 
			Rectangle page = reader.getPageSize(1);
			job.getBook().setBookWidth(Format.points2mm(page.getWidth()).floatValue());
			//job.setBookWidth(Format.points2mm(referenceBox.getWidth()).doubleValue());
		} else {
			BigDecimal widthFromPdf = Format.points2mm(referenceBox.getWidth());
		}
		if(job.getBook().getBookHeight() == 0) {
			Rectangle page = reader.getPageSize(1);
			job.getBook().setBookHeight(Format.points2mm(page.getHeight()).floatValue());
			//job.setBookHeight(Format.points2mm(referenceBox.getHeight()).doubleValue());
		} else {
			BigDecimal heightFromPdf = Format.points2mm(referenceBox.getHeight());
		}
	}

	protected abstract void updateProducibleItem() throws ComposerException; 

	/** Converts values of rectangle to points and returns new rectangle **/
	protected Rectangle getBoxSizeInPoints(Rectangle box) {
		System.out.println("getBoxSizeInPoints: "+box.getLeft()+"-"+box.getBottom()+"-"+box.getRight()+"-"+box.getTop());
		Rectangle result = new Rectangle(Format.mm2points(box.getLeft()).floatValue(), 
				Format.mm2points(box.getBottom()).floatValue(), 
				Format.mm2points(box.getRight()).floatValue(), 
				Format.mm2points(box.getTop()).floatValue());
		return result; 
	}

	protected void addEvent(String msg) {
		//job.addEvent(msg);
		System.out.println(msg);
	}


	/**
	 * Oumaima Kridene (generate datamatrix from text)
	 * @param text
	 * @return
	 * @throws Exception
	 */

	public static BarcodeDatamatrix getDatamatrixCode (String text) throws Exception{
		// supported square barcode dimensions
		int[] barcodeDimensions = {10, 12, 14, 16, 18, 20, 22, 24, 26, 32, 36, 40, 44, 48, 52, 64, 72, 80, 88, 96, 104, 120, 132, 144};

		BarcodeDatamatrix barcode = new BarcodeDatamatrix();
		barcode.setOptions(BarcodeDatamatrix.DM_AUTO);
		barcode.setWidth(36);
		barcode.setHeight(12);
		//barcode.setWs(2);

		int returnResult = barcode.generate(text);
		if (returnResult == BarcodeDatamatrix.DM_NO_ERROR) {
			return barcode;
		}
		return null;
	}
}
