package com.epac.imposition.utils;

import java.util.HashMap;
import java.util.Map;

import com.epac.imposition.bookblock.ImpositionJob;
import com.epac.imposition.bookblock.PdfComposer;
import com.epac.imposition.bookblock.TextComposer;
import com.epac.imposition.rip.RipJobCreator;
import com.epac.om.api.utils.LogUtils;

public class ImpositionHandler {
	
	
	private static ProgressBars mProgress;
	
	
	public static void setupProgressBar (ProgressBars progress) {
		mProgress = progress;
	}
	
	private static void updateProgressStatus(String msg) {
		if (mProgress != null)
			mProgress.setLabel(msg);
	}
	
	
	/**
	 * 
	 * @param bookId
	 * @param imposedFilePath
	 * @param job
	 * @throws Exception
	 */
	public static void imposeTextFile (String bookId, String imposedFilePath, ImpositionJob job) throws Exception{
		if (job.isBigSheet()) {
			String outputfilePath = job.getBook().getOutputFilePath();
			
			Map<String, Float> heights = new HashMap<String, Float>();
			heights.put(ImpositionJob.S0, job.getStandardBigSheetHeight());
			heights.put(ImpositionJob.S1, job.getBigSheetHeightS1());
			heights.put(ImpositionJob.S2, job.getBigSheetHeightS2());
			heights.put(ImpositionJob.S3, job.getBigSheetHeightS3());
			
			for (int i=0; i<4; i++) {
				float height = (float) (heights.values().toArray())[i];
				if (height == 0)
					continue;
				
				resetConfig(job, height, String.valueOf((heights.keySet().toArray())[i]), outputfilePath);
				if(job.isPopLine() && String.valueOf((heights.keySet().toArray())[i]) != "S0")
					continue;
				doImposition(bookId, imposedFilePath, job, String.valueOf((heights.keySet().toArray())[i]));
				if(job.isPopLine())
				break;	
			}		
		}else {
			doImposition (bookId, imposedFilePath, job, "");
		}		
	}
	
	
	/**
	 * 
	 * @param job
	 * @param bigSheetHeight
	 * @param prefix
	 * @param outputPath
	 */
	private static void resetConfig (ImpositionJob job, float bigSheetHeight, String prefix, String outputPath) {
		job.setPrintSheetHeight(bigSheetHeight);
		job.getBook().setOutputFilePath(outputPath);
		job.getBook().setBookHeight(0);
		job.getBook().setBookWidth(0);
	}
	
	
	/**
	 * 
	 * @param bookId
	 * @param imposedFilePath
	 * @param job
	 * @param prefix
	 * @throws Exception
	 */
	private static void doImposition (String bookId, String imposedFilePath, ImpositionJob job, String prefix) throws Exception{		
		TextComposer composer = new TextComposer(job);
		composer.setPrefixHeight(prefix);
		job.setPrefixHeight(prefix);
		composer.compose();
		LogUtils.debug("Writing imposed file to "+imposedFilePath);
		updateProgressStatus("Writing imposed file to "+imposedFilePath);
		PdfComposer tool = new PdfComposer(job);
		tool.createFinalFile();
		LogUtils.debug("Text imposition finished successfully for "+bookId);
		updateProgressStatus("Text imposition finished successfully for "+bookId);
		
		//if (job.isBigSheet()){
			RipJobCreator.createRipJob(job, prefix);
			LogUtils.debug("JDF file created successfully for "+bookId);
			updateProgressStatus("JDF file created finished successfully for "+bookId);
		//}
	}
	

}
