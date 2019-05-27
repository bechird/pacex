package com.epac.cap.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.model.CoverSection;
import com.epac.cap.model.Job;
import com.epac.cap.model.Part;
import com.epac.cap.model.WFSDataSupport;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;
import com.itextpdf.text.pdf.PdfStamper;

public class BatchFactory {

	private static Logger logger = Logger.getLogger(BatchFactory.class);
	private static final String DTFORMAT = "yyyyMMddHHmmss";  

	/**
	 * Manipulates a PDF cover file to create cover batch Job
	 * @param index 
	 * @throws Exception 
	 */

	public synchronized static File createCoverBatchJob(CoverSection section, Job coverJob, Part pr, Map<String, Integer> map, int qty) throws Exception {
		int quantity = map.get("quantity");
		int index	 = map.get("index");
		PdfReader inputReader = null;
		File fsCombined = null;
		File fsComposed = null;
		File fsFinal = null;
		boolean singlePage = false;
		String coverImposedPath = null;
		String coverSize = "NA";
		try {
			logger.info("start create Cover Batch Job...");
			Set<WFSDataSupport> onProdDataSupports = pr.getDataSupportsOnProd();
			for (WFSDataSupport ds : onProdDataSupports) {
				if (ds.getName().equals(WFSDataSupport.NAME_IMPOSE)
						&& ds.getDsType().equalsIgnoreCase(WFSDataSupport.TYPE_COVER)) {
					coverImposedPath = ds.getLocationdByType("Destination").getPath();
					break;
				}
			}
			
			if (coverImposedPath != null) {
				File imposedFile = new File(coverImposedPath);
				PdfReader firstInputReader = new PdfReader(imposedFile.getAbsolutePath());
				Rectangle mediabox = firstInputReader.getPageSize(1);
				float sheetWidth = mediabox.getWidth();
				
				float mSheetWidth = Float.parseFloat(System.getProperty(ConfigurationConstants.MEDIABOX_M_SHEET_WIDTH));
				float lSheetWidth = Float.parseFloat(System.getProperty(ConfigurationConstants.MEDIABOX_L_SHEET_WIDTH));
				float xlSheetWidth = Float
						.parseFloat(System.getProperty(ConfigurationConstants.MEDIABOX_XL_SHEET_WIDTH));
				 sheetWidth = Format.points2mm(sheetWidth).floatValue();
				//Elamine: a document size is never accurent, give it a tolerence
				if (sheetWidth <= mSheetWidth + 5 && sheetWidth >= mSheetWidth -5)
					coverSize = "M";
				if (sheetWidth <= lSheetWidth + 5 && sheetWidth >= lSheetWidth -5)
					coverSize = "L";
				if (sheetWidth <= xlSheetWidth + 5 && sheetWidth >= xlSheetWidth -5)
					coverSize = "XL";

				// add blank page to the pdf in case of single page
				Document firstDoc = new Document();
				if (firstInputReader.getNumberOfPages() == 1) {
					fsComposed = File.createTempFile(new SimpleDateFormat(DTFORMAT).format(new Date()),
							"composed.pdf");
					PdfSmartCopy copyt = new PdfSmartCopy(firstDoc, new FileOutputStream(fsComposed));
					firstDoc.open();
					copyt.addPage(copyt.getImportedPage(firstInputReader, 1));
					copyt.addPage(firstInputReader.getPageSize(1), firstInputReader.getPageRotation(1));
					firstDoc.close();
					firstInputReader.close();
					inputReader = new PdfReader(fsComposed.getAbsolutePath());
					singlePage = true;

				} else {
					inputReader = new PdfReader(coverImposedPath);
				}
				// combine cover files for qty times to create batch
				Document document = new Document();
				fsCombined = File.createTempFile(new SimpleDateFormat(DTFORMAT).format(new Date()),
						"combined.pdf");
				FileOutputStream foutStreamCombined = new FileOutputStream(fsCombined);
				PdfSmartCopy copy = new PdfSmartCopy(document, foutStreamCombined);
				document.open();
				if (inputReader.getNumberOfPages() == 1) {

					for (int i = 1; i <= qty; i++)
						// if 1 page cover pdf
						copy.addPage(copy.getImportedPage(inputReader, 1));
					copy.addPage(inputReader.getPageSize(0), inputReader.getPageRotation(0));

				} else {
					for (int i = 1; i <= qty; i++) {
						// if 2 pages cover pdf
						copy.addPage(copy.getImportedPage(inputReader, 1));
						copy.addPage(copy.getImportedPage(inputReader, 2));
					}
				}
				logger.info("Composed Cover Section created successfully");
				document.close();
				inputReader.close();
				foutStreamCombined.flush();
				foutStreamCombined.close();
				foutStreamCombined = null;
	            System.gc();
				// Add Overlay on batch
				PdfReader ovReader = new PdfReader(fsCombined.getAbsolutePath());
				String[] bits = coverImposedPath.split(System.getProperty("file.separator"));
				String lastOne = bits[bits.length - 1];
				fsFinal = File.createTempFile(new SimpleDateFormat(DTFORMAT).format(new Date()), lastOne);
				BaseFont helvetica;
				helvetica = BaseFont.createFont("Helvetica.otf", BaseFont.CP1252, BaseFont.EMBEDDED);
				Font font = new Font(helvetica, 8, Font.NORMAL);
				FileOutputStream foutStreamfsFinal = new FileOutputStream(fsFinal);
				PdfStamper stamper = new PdfStamper(ovReader, foutStreamfsFinal);
				Rectangle page = ovReader.getPageSize(1);
				Integer originalNoOfPages = ovReader.getNumberOfPages();
				PdfContentByte over = null;
				// add sequence number and add batch name as overlay
				if (ovReader.getNumberOfPages() == qty) {
					for (int i = 1; i <= originalNoOfPages; i++) {
						over = stamper.getOverContent(i);
						ColumnText.showTextAligned(over, Element.ALIGN_LEFT,
								new Phrase(section.getCoverSectionName() + " " + index + "/" + section.getQuantity() + " " + coverSize+ "    File: "+imposedFile.getName()),
								page.getWidth() - 90, page.getHeight() - 15, 0);
						index ++;
					}
				} else {
					for (int i = 1, j = 1; i <= originalNoOfPages; i++) {
						if ((i % 2) != 0) {
							over = stamper.getOverContent(i);
							ColumnText.showTextAligned(over, Element.ALIGN_LEFT,
									new Phrase(
											section.getCoverSectionName() + " " + index + "/" + section.getQuantity() + " " + coverSize + "    File: "+imposedFile.getName(),
											font),
									70, 16, 0);
							j++;
							index ++;
						}

					}
				}
				stamper.close();
				ovReader.close();
				foutStreamfsFinal.flush();
				foutStreamfsFinal.close();
				foutStreamfsFinal = null;
	            System.gc();
				if (fsComposed != null && fsComposed.delete()) {
					logger.info(fsComposed + " is deleted!");
				} else {
					logger.info("Delete operation is failed for fsComposed with [" + section.getCoverSectionName()
					+ "] while creating Cover pacex Job ["+coverJob.getJobId()+"]");				}

				if (fsCombined != null && fsCombined.delete()) {
					logger.info(fsCombined + " is deleted!");
				} else {
					logger.info("Delete operation is failed for fsCombined with [" + section.getCoverSectionName()
					+ "] while creating Cover pacex Job ["+coverJob.getJobId()+"]");
				}
			} else {
				logger.error("cover Imposed File not found for JobId [" + coverJob.getJobId() + "]");
				throw new Exception();
			}
			map.put("index", index);
		} catch (Exception e) {
			logger.error("error while creating cover batch Job ["+coverJob.getJobId()+"] of section ["+section.getCoverSectionId()+"]:  ", e);
		}
		return fsFinal;
	}

	public static File generateSectionTag(File fsFinal, CoverSection section) {
		File fsOut = null;
		BaseFont helvetica;

		// Add empty Header tag 3 pages
		try {
			helvetica = BaseFont.createFont("Helvetica.otf", BaseFont.CP1252, BaseFont.EMBEDDED);
			Document doc = new Document();
			PdfReader fnReader = new PdfReader(fsFinal.getAbsolutePath());
			File fsCompo = File.createTempFile(new SimpleDateFormat(DTFORMAT).format(new Date()), "head.pdf");

			PdfSmartCopy copyPages = new PdfSmartCopy(doc, new FileOutputStream(fsCompo));
			doc.open();
			copyPages.addPage(fnReader.getPageSize(1), fnReader.getPageRotation(1));
			copyPages.addPage(fnReader.getPageSize(1), fnReader.getPageRotation(1));
			copyPages.addPage(fnReader.getPageSize(1), fnReader.getPageRotation(1));
			copyPages.addDocument(fnReader);

			copyPages.close();
			doc.close();
			fnReader.close();

			fsOut = File.createTempFile(new SimpleDateFormat(DTFORMAT).format(new Date()), "final.pdf");

			// create info header
			PdfReader headReader = new PdfReader(fsCompo.getAbsolutePath());
			PdfStamper stampHeader = new PdfStamper(headReader, new FileOutputStream(fsOut));
			Rectangle page = headReader.getPageSize(1);
			PdfContentByte ovctn = null;
			Font fonthead1 = new Font(helvetica, 50, Font.NORMAL);
			Font fonthead2 = new Font(helvetica, 62, Font.BOLD);

			// add 3 pages tag: sequence number and batch name
			for (int i = 1; i <= 3; i++) {
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
			if (fsFinal != null && fsFinal.delete()) {
				logger.info(fsFinal.getName() + " is deleted!");
			} else {
				logger.info("Delete operation is failed for fsFinal with [" + section.getCoverSectionName()
						+ "] while generating Section Tag");
			}
			if (fsCompo != null && fsCompo.delete()) {
				logger.info(fsCompo.getName() + " is deleted!");
			} else {
				logger.info("Delete operation is failed for fsCompo with [" + section.getCoverSectionName()
						+ "] while generating Section Tag");
			}

		} catch (Exception e) {
			logger.error("error while generating tag for section ["+section.getCoverSectionName()+"]  :", e);
		}
		return fsOut;
	}
	
	/**
	 * 
	 * Main method.
	 * 
	 * @param args
	 *            no arguments needed
	 * 
	 * @throws DocumentException
	 * 
	 * @throws IOException
	 * 
	 */

	public static void main(String[] args) {
		// System.out.println("hello");
		// System.out.println(BatchFactory.createCoverBatchJob("/Users/mbk/Desktop/booklist/M_9782266267830E1.cover.pdf",
		// 10,
		// "A230", "1of2", "XL", "MATT").getAbsolutePath());

	}

}
