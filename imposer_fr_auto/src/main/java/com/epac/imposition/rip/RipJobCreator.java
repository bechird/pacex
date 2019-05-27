package com.epac.imposition.rip;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.epac.imposition.bookblock.ImpositionJob;
import com.epac.om.api.utils.LogUtils;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;

public class RipJobCreator{

	public static String createRipJob(ImpositionJob job, String prefix) throws Exception {

		LogUtils.debug("Start cretaing the Rip Jdf Job ....");

		Rectangle rectangle;
		String filename = job.getBook().getOutputFilePath();
		
		String sheetHeight = FilenameUtils.getBaseName(filename);
		sheetHeight = sheetHeight.substring(0, sheetHeight.indexOf('_') +1);
		
		
		
		
		String jobName = job.getHunkelerLine().concat("EM").concat(job.getBook().getBook().getBookId());
		
		if(!job.isBigSheet() && !(job.isPopLine() && job.isEpacMPb())){
			jobName = sheetHeight.concat(job.getHunkelerLine()).concat("SM").concat(job.getBook().getBook().getBookId());
		}
		
		
		String fujiProfile = "JP540_Color";
		String pdfFilePath = job.getBook().getOutputFilePath();
		PdfReader readInputPDF = null;
		try {

			readInputPDF = new PdfReader(pdfFilePath);

			int pageCount = readInputPDF.getNumberOfPages();

			rectangle = readInputPDF.getPageSize(1);

			String frontSurfaceBox = "0 0 " + rectangle.getWidth() + " " + rectangle.getHeight();
			String frontContentObjectMatrix = "0 0";
			String frontContentObjectRectangle = frontSurfaceBox;

			String backSurfaceBox = "0 0 " + rectangle.getWidth() + " " + rectangle.getHeight();
			String backContentObjectMatrix = "0 0";
			String backContentObjectRectangle = "0 0 " + rectangle.getWidth() + " " + rectangle.getHeight();

			String mediaDimension = rectangle.getWidth() + " " + rectangle.getHeight();
			String mediaType = job.getMediaProfile();

			Map<String, Object> input = new HashMap<String, Object>();
			
			String jobID =  jobName;
			
			if (!prefix.isEmpty())
				jobID = prefix.concat(jobName.substring(jobName.lastIndexOf('_') +1));
			
			input.put("jobId", jobID);
			input.put("jobName", jobName);
			input.put("fujiProfile", fujiProfile);
			input.put("jobPriority", "50");

			input.put("isOverlay", "0");

			File pdfFile = new File(job.getBook().getOutputFilePath());

			String relativeFilePath = "./pdf/".concat(pdfFile.getName());

			input.put("pageCount", String.valueOf(pageCount - 1).replace(",", ""));
			input.put("relativePDFFilePath", relativeFilePath);

			List<Sheet> sheets = new ArrayList<Sheet>();
			for (int i = 0; i < (Integer.valueOf(pageCount) / 2); i++) {
				sheets.add(new Sheet(String.valueOf(i + 1), 
						frontSurfaceBox, 
						frontContentObjectMatrix,
						frontContentObjectRectangle, String.valueOf((i * 2)), 
						backSurfaceBox, 
						backContentObjectMatrix,
						backContentObjectRectangle, 
						String.valueOf((i * 2) + 1)));
			}

			input.put("sheets", sheets);
			input.put("isMarkOn", "0");

			input.put("mediaDimension", mediaDimension);
			input.put("mediaType", mediaType);

			String jdfFile = pdfFile.getAbsolutePath().replace(".pdf", ".jdf");

			File jdfJobFile = new File(jdfFile);

			FileUtils.writeStringToFile(jdfJobFile, TemplateUtil.generate(job, input, "jdfJobTemplate"), Charset.defaultCharset());

			LogUtils.debug("Jdf Job created successfully at " + jdfJobFile.getPath());

			return jdfJobFile.getPath();

		} catch (IOException ie) {
			LogUtils.error("Error when opening the files needed for RIP to get Metadata", ie);
		}finally{
			try { readInputPDF.close();} catch (Exception e) {}
		}
		return null;
	}

}
