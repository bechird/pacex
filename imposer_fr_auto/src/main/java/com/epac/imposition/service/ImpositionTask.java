package com.epac.imposition.service;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.epac.imposition.bookblock.ImpositionJob;
import com.epac.imposition.bookblock.PdfComposer;
import com.epac.imposition.bookblock.PnlFactory;
import com.epac.imposition.bookblock.TextComposer;
import com.epac.imposition.config.Configuration;
import com.epac.imposition.config.Constants;
import com.epac.imposition.cover.Cover;
import com.epac.imposition.cover.CoverImposer;
import com.epac.imposition.model.PNLInfo;
import com.epac.imposition.rip.RipJobCreator;
import com.epac.imposition.utils.ImpositionHandler;
import com.epac.om.api.book.Book;
import com.epac.om.api.utils.LogUtils;

public class ImpositionTask implements Runnable {

	private float width;
	
	private Book book;
	private String textPath;
	private String coverPath;
	private String textOutput;
	private String coverOutput;
	private String perforation;
	
	private String [] bestSheets ;
	private String [] hunkeler ;
	private PNLInfo pnlInfos;
	
	public ImpositionTask(float width, Book impositionBook) {		
		super();
		this.width = width;
		this.book = impositionBook;
		
	}

	public Book getImpositionBook() {
		return book;
	}

	public ImpositionTask(Book book, float rollWidth, String textPath, String coverPath, String textOutput, String coverOutput, String perforation,PNLInfo pnlInfos, String[] bestSheets, String ...hunkelerLines) {
		this.width = rollWidth;
		this.book = book;
		this.textPath = textPath;
		this.coverPath = coverPath;
		this.textOutput = textOutput;
		this.coverOutput = coverOutput;
		this.pnlInfos = pnlInfos;
		this.hunkeler = hunkelerLines;
		this.bestSheets = bestSheets;
		this.perforation = perforation;
	}

	public void setImpositionBook(Book impositionBook) {
		this.book = impositionBook;
	}
	
	public void setTextOutput(String output) {
		this.textOutput = output;
	}
	
	public String getTextOutput() {
		return textOutput;
	}
	
	public void setTextPath(String textPath) {
		this.textPath = textPath;
	}
	public String getTextPath() {
		return textPath;
	}
	
	public void setCoverPath(String coverPath) {
		this.coverPath = coverPath;
	}
	
	public String getCoverPath() {
		return coverPath;
	}
	
	
	public String[] getHunkeler() {
		return hunkeler;
	}

	public void setHunkeler(String[] hunkeler) {
		this.hunkeler = hunkeler;
	}
	
	/**
	 * @return the bestSheets
	 */
	public String[] getBestSheets() {
		return bestSheets;
	}

	/**
	 * @param bestSheets the bestSheets to set
	 */
	public void setBestSheets(String[] bestSheets) {
		this.bestSheets = bestSheets;
	}

	public String getPerforation() {
		return perforation;
	}

	public void setPerforation(String perforation) {
		this.perforation = perforation;
	}
	public PNLInfo getPnlInfos() {
		return pnlInfos;
	}

	public void setPnlInfos(PNLInfo pnlInfos) {
		this.pnlInfos = pnlInfos;
	}
	
	public void run() {
		
		Configuration.load("imposition.properties");
		LogUtils.start();
		
		try
		{	
			File tempFolder = null;
			File originalCoverFile = null;
			File imposedCoverFile = null;
			File originalTextFile = new File(textPath);
			if (coverPath != null && !coverPath.isEmpty() && !coverPath.equals("null"))
				originalCoverFile = new File(coverPath);
			// String originalTextFileName = originalTextFile.getName();
			// File originalTextFile2 = new File(textPath);

			if (getPnlInfos() != null) {
				// add pnl to original content
				File baseFolder = new File(System.getProperty(Constants.PNL_BASE_TEMP));
				tempFolder = new File(baseFolder.getAbsolutePath() + File.separator + "pnlTemp");
				originalTextFile = new PnlFactory().createPnl(originalTextFile, getPnlInfos());
				System.out.println("generated file path:" + originalTextFile.getAbsolutePath());
				System.out.println("generated file name:" + originalTextFile.getName());

				/*
				 * if(originalTextFile.renameTo(originalTextFile2)){
				 * System.out.println("File renamed"); }else{
				 * System.out.println("Sorry! the file can't be renamed"); }
				 */
			} else {
				LogUtils.error("invalid  data for pnl generation ---> no pnl generated");
			}
			File modifiedTextFile = File.createTempFile(book.getBookId(), ".text.pdf");
			File modifiedCoverFile = File.createTempFile(book.getBookId(), ".cover.pdf");

			FileUtils.copyFile(originalTextFile, modifiedTextFile, false);
			if (coverPath != null && !coverPath.isEmpty() && !coverPath.equals("null")) {
				FileUtils.copyFile(originalCoverFile, modifiedCoverFile, false);
				imposedCoverFile = new File(coverOutput, originalCoverFile.getName());
				if (!imposedCoverFile.getParentFile().exists())
					imposedCoverFile.getParentFile().mkdirs();
			}
			File imposedTextFile = new File(textOutput, originalTextFile.getName());

			if (!imposedTextFile.getParentFile().exists())
				imposedTextFile.getParentFile().mkdirs();
//to be removed later
			// String[] hunkeler = { "FF", "PF", "PL", "PB" };
			String[] sheets = { "1", "0" };
			String[] modes = { "EM", "SM" };
			ImpositionJob job = null;

			for (int i = 0; i < hunkeler.length; i++) {
				
				boolean hunkelrEnabled = Boolean.parseBoolean(System.getProperty("com.epac.composer."+hunkeler[i]+".enabled", "false"));
				if(!hunkelrEnabled)
					continue;
				
				for (int j = 0; j < sheets.length; j++) {

					boolean modeEnabled = Boolean.parseBoolean(System.getProperty("com.epac.composer."+modes[j]+".enabled", "true"));
					if(!modeEnabled)
						continue;
					
					job = ImpositionJob.create(book, modifiedTextFile.getAbsolutePath(), imposedTextFile.getAbsolutePath(),
							this.width, sheets[j], hunkeler[i], perforation, bestSheets);

					LogUtils.debug("start imposition job => originalFile: " + modifiedTextFile);
					LogUtils.debug("start imposition job => imposedFile: " + imposedTextFile);

//					TextComposer composer = new TextComposer(job);
//					composer.compose();
//
//					PdfComposer tool = new PdfComposer(job);
//					tool.createFinalFile();
//					try {
//						RipJobCreator.createRipJob(job);
//					} catch (Exception e) {
//						LogUtils.error("Cannot create JDF for imposition "+hunkeler[i]+" in "+(sheets[j].equals("0")? "standrad":"Epac")+" mode", e);
//					}

					
					//Replace the old imposition by the best sheet height
					ImpositionHandler.imposeTextFile(book.getBookId(), imposedTextFile.getAbsolutePath(), job);
					
				}
			}
			
			// cover may not exist, so do not impose it
			if(coverPath != null && !coverPath.isEmpty() && !coverPath.equals("null")){
				com.epac.imposition.cover.ImpositionJob coverJob = com.epac.imposition.cover.ImpositionJob.create();
				float textBleed	= Float.parseFloat(System.getProperty(Constants.TEXT_BLEED_VALUE));
				float topmargin = job.getTopMargin() + job.getOutputControllerMargin() + textBleed;
				float finalwidth = (float) job.getBook().getMetadataWidth();
				float finalheight = (float) job.getBook().getMetadataHeight();
				double metaThickness = job.getBook().getBookThickness();
					
				float rawheight = topmargin + job.getBottomMargin() + finalheight;
				float frontmargin = coverJob.getyPosition() - topmargin;
	
				Cover cover = new Cover(job.getBook(), rawheight, frontmargin, finalheight, finalwidth, metaThickness, modifiedCoverFile.getAbsolutePath());	
				coverJob.setCover(cover);
	
				CoverImposer imposer = new CoverImposer(coverJob, imposedCoverFile.getAbsolutePath());
				imposer.compose();
				FileUtils.forceDelete(modifiedCoverFile);			
			}
			
			FileUtils.forceDelete(modifiedTextFile);
			if(tempFolder != null){
			  for (File f : tempFolder.listFiles()) {
		            f.delete();
		        }
			  tempFolder.delete();
			}
		} catch (Exception e) {
			LogUtils.error("Imposition failed: ", e);
		}

		LogUtils.end();
	}


}
