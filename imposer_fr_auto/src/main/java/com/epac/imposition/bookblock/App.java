package com.epac.imposition.bookblock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.SwingUtilities;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.jackson.JacksonFeature;

import com.epac.imposition.config.Configuration;
import com.epac.imposition.config.Constants;
import com.epac.imposition.cover.Cover;
import com.epac.imposition.cover.CoverImposer;
import com.epac.imposition.cover.ScoringAlignment;
import com.epac.imposition.utils.Format;
import com.epac.imposition.utils.ImpositionHandler;
import com.epac.imposition.utils.ImpositionType;
import com.epac.imposition.utils.ProgressBars;
import com.epac.om.api.book.Book;
import com.epac.om.api.utils.LogUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

public class App {
	
	public static final String IMPOSITION_VERSION = "3.0.3";
	
	
	private static com.epac.imposition.cover.ImpositionJob coverImpositionJob = null;
	
	
	
	private static Client client = ClientBuilder.newBuilder()
			.register(JacksonFeature.class)
			.build();
	
	private static String token;
	
	
	private static void authenticate(){
		
		String security = System.getProperty(Constants.ESPRINT_SECURITY);
		String username = System.getProperty(Constants.ESPRINT_USERNAME);
		String password = System.getProperty(Constants.ESPRINT_PASSWORD);
		
		WebTarget target = client.target(security);
		LogUtils.debug("Authentication: "+target.getUri());
		Encoder encoder = Base64.getEncoder();
		String encodedAuth = new String(encoder.encode(username.concat(":")
				.concat(password).getBytes()));

		Invocation.Builder invocationBuilder = target.request().header(
				"Authorization", "Basic ".concat(encodedAuth));

		Form form = new Form();
		form.param("grant_type", "client_credentials");

		Response response = invocationBuilder.acceptLanguage(Locale.getDefault()).post(javax.ws.rs.client.Entity.entity(
				form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		
		if(response.getStatus() != 200){
			LogUtils.error("Esprint authentication failed");
			progress.setError("Esprint authentication failed");
			return;
		}
		LogUtils.debug("Authentication successfully completed.");
		progress.setLabel("Authentication successfully completed.");
		try {
			token = (String) response.readEntity(Map.class).get("access_token");
		} catch (Exception e) {
			LogUtils.error("Esprint authentication failed", e);
			progress.setError("Esprint authentication failed");
		}
		
	}
	
	private static Book getBook(String bookId) {
		
		
		String endpoint = System.getProperty(Constants.ESPRINT_ENDPOINT);
		
		
		if(token == null)
			authenticate();
		
		if(token == null)
			return null;
		
		
		
		WebTarget finalTarget = client.target(endpoint).path(bookId);

		Invocation.Builder invocationBuilder = finalTarget.request().header("Authorization", "Bearer "+token);
		
		Response response = invocationBuilder
				.acceptLanguage(Locale.getDefault())
				.accept(MediaType.APPLICATION_JSON)
				.get();
		if(response.getStatus() != 200){
			LogUtils.error("Fetching book "+bookId+" from Esprint failed!");
			progress.setError("Fetching book "+bookId+" from Esprint failed!");
			LogUtils.error(response.readEntity(String.class));
			return null;
		}
		
		String strBook = response.readEntity(String.class);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Book book = null;
		try {
			book = mapper.readValue(strBook, Book.class);
			LogUtils.debug("Metadata of book "+bookId+" successfully fetched from Esprint");
			progress.setLabel("Metadata of book "+bookId+" successfully fetched from Esprint");
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch book metadata "+bookId, e);
		}
		
		return book;
	}
	
	private static List<Object> getBooks() throws Exception{
		File file = new File("books.txt");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		 ObjectMapper mapper = new ObjectMapper();
         List<Object> books;
         try{
                 Class<?> clazz = Class.forName(Book.class.getName());
                 CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
                 books = mapper.readValue(file, type);
                 reader.close();
                 
                 LogUtils.debug("books.txt is a valid JSON file, no need to contact Esprint.");
                 return books;
         }catch (Exception e) {
                 LogUtils.error("books.txt is not a valid JSON file, trying considering it as bookId list", e);
         }
         
         
		books = new ArrayList<Object>();
		
		String id;
		try {
			while((id = reader.readLine()) != null){
				if(id.trim().contains(" ") || id.trim().contains(",") || id.trim().contains(";"))
					throw new RuntimeException("Books file format not valid");
				else if (id.trim().isEmpty())
					continue;
				
				books.add(id.trim());
			}
		} catch (Exception e) {
			throw e;
		}finally{
			reader.close();
		}
		return books;
		
	}
	
	private static File downloadText(Book book, File originalPdf) throws Exception{
		String repository = System.getProperty(Constants.ESPRINT_REPOSITORY);
		if(!repository.endsWith("/"))
			repository = repository.concat("/");
		
		repository = repository.concat("text/proofed/").concat(book.getBookId()).concat("/").concat(book.getBookId()).concat(".text.pdf");
		InputStream stream = null;
		FileOutputStream writer = null;
		try{
			URL url = new URL(repository);
			stream = url.openStream();
			
			writer = new FileOutputStream(originalPdf);
			byte[] buffer = new byte[1024 * 1024 * 10];
			int length = 0;
			LogUtils.debug("downlaoding book "+ book.getBookId() + " ("+getSize(book.getMetadata().getTextPDFFileSize())+")");
			progress.setLabel("downlaoding book "+ book.getBookId() + " ("+getSize(book.getMetadata().getTextPDFFileSize())+")");
			while((length = stream.read(buffer)) > 0){
				writer.write(buffer, 0, length);
			}
			LogUtils.debug("File downloaded "+originalPdf.getAbsolutePath());
			progress.setLabel("File downloaded "+originalPdf.getAbsolutePath());
		}catch(Exception e ){
			throw e;
		}finally{
			try{writer.close();}catch(Exception e){}
			try{stream.close();}catch(Exception e){}
		}
		return originalPdf;
	}
	
	private static File downloadCover(Book book, File originalPdf) throws Exception{
		String repository = System.getProperty(Constants.ESPRINT_REPOSITORY);
		if(!repository.endsWith("/"))
			repository = repository.concat("/");
		
		repository = repository.concat("cover/proofed/").concat(book.getBookId()).concat("/").concat(book.getBookId()).concat(".cover.pdf");
		URL url = new URL(repository);
		InputStream stream = url.openStream();

		
		FileOutputStream writer = new FileOutputStream(originalPdf);
		byte[] buffer = new byte[1024 * 1024 * 10];
		int length = 0;
		LogUtils.debug("downlaoding book "+ book.getBookId() + " ("+getSize(book.getMetadata().getCoverPDFFileSize())+")");
		progress.setLabel("downlaoding book "+ book.getBookId() + " ("+getSize(book.getMetadata().getCoverPDFFileSize())+")");
		while((length = stream.read(buffer)) > 0){
			writer.write(buffer, 0, length);
		}
		writer.close();
		stream.close();
		LogUtils.debug("File downloaded "+originalPdf.getAbsolutePath());
		progress.setLabel("File downloaded "+originalPdf.getAbsolutePath());
		return originalPdf;
	}
	
	private static String getSize(double size){
		int i = 0;
		String[] units = {"Kb", "Mb", "Gb"};
		while(size > 1024){
			size = size / 1024;
			i++;
		}
		return new DecimalFormat("#.## ").format(Math.max(size, 0.1)) + units[i];
	}
	
	
	/**
	 * @throws IOException 
	 * @throws ComposerException 
	 * 
	 */
	private static void execute () {
		Configuration.load("imposition.properties");
		
		try {
			String filter = System.getProperty("com.epac.composer.cover.filter", "");
			String baseDir = System.getProperty("com.epac.composer.cover.baseDir", "");
	
			if (!filter.isEmpty() && !baseDir.isEmpty())
				runImpositionMM ();
			else
				runImposition ();	
		}catch (Exception e) {
			LogUtils.error("Error occured while running the imposer", e);
		}
	}
	
	/**
	 * 
	 * @throws ComposerException
	 * @throws IOException
	 */
	private static void runImpositionMM () throws ComposerException, IOException{
		
		String combine = System.getProperty("com.epac.composer.cover.combine", "false");
		String batch = System.getProperty("com.epac.composer.cover.batch");
		final String filter = System.getProperty("com.epac.composer.cover.filter", "");
		String baseDir = System.getProperty("com.epac.composer.cover.baseDir");
		File coversPath = new File(baseDir, "original");
		File imposedPath = new File(baseDir, "imposed");
		File croppedPath = new File(baseDir, "cropped");

		if (!coversPath.exists()) {
			progress.setError("Original files path does not exist" + System.lineSeparator() + coversPath);
			return;
		}
		if (!croppedPath.exists()) {
			croppedPath.mkdirs();
		}
		if (!imposedPath.exists()) {
			imposedPath.mkdirs();
		}

		ScriptEngineManager sem = new ScriptEngineManager();
		final ScriptEngine se = sem.getEngineByName("JavaScript");

		File[] covers = coversPath.listFiles(new java.io.FilenameFilter()
		{
			public boolean accept(File dir, String name) {
				boolean accepted = (name.endsWith("pdf"));
				if (accepted) {
					se.put("name", name);
					Object val = Boolean.FALSE;
					try {
						val = se.eval(filter);
					} catch (ScriptException e) {
						e.printStackTrace();
					}
					System.out.println(name + " evaluated to " + val);
					boolean filtered = ((Boolean)val).booleanValue();
					accepted = (accepted) && (filtered);
				}
				
				return accepted;
			}
		});

		ImpositionJob.totalBatch = covers.length;
		coverImpositionJob = com.epac.imposition.cover.ImpositionJob.create();
		
		for (File cover : covers) {
			String coverName = cover.getName();
			String bookId = coverName.substring(0, coverName.indexOf('_'));
			
			ScoringAlignment scoring = ScoringAlignment.CENTER;
			if (coverName.matches(".*_([fF])_.*"))
			scoring = ScoringAlignment.FRONT;
			if (coverName.matches(".*_([bB])_.*")) {
				scoring = ScoringAlignment.BACK;
			}
			
			Book book = getBook(bookId);
			ImpositionJob job = ImpositionJob.create(book, null, null);
			job.getBook().getBook().getMetadata().setCoverAlignment(scoring.getLetter());
			
			File croppedFileCover = new File(croppedPath, coverName);
			File imposedFileCover = new File(imposedPath, coverName);
			FileUtils.copyFile(cover, croppedFileCover, false);
			imposeCoverFile(bookId, croppedFileCover.getAbsolutePath(), imposedFileCover.getAbsolutePath(), job);
		}

		float sheetWidth    = coverImpositionJob.getSheetWidth();
		float mSheetWidth   = Float.parseFloat(System.getProperty(Constants.IMPOSITION_M_SHEET_WIDTH));
		float lSheetWidth   = Float.parseFloat(System.getProperty(Constants.IMPOSITION_L_SHEET_WIDTH));
		float xlSheetWidth  = Float.parseFloat(System.getProperty(Constants.IMPOSITION_XL_SHEET_WIDTH));

		String sheetType = "M";
		if(sheetWidth == mSheetWidth)
		sheetType = "M";
		else if(sheetWidth == lSheetWidth)
		sheetType = "L";
		else if(sheetWidth == xlSheetWidth)
		sheetType = "XL";

		if(batch == null || !"true".equalsIgnoreCase(combine))
		return;

		File batchOutputFile  = new File(imposedPath, batch.concat(".pdf"));

		Document doc = null;
		PdfWriter writer = null;
		PdfReader reader = null;
		try {
			Rectangle box = new Rectangle(coverImpositionJob.getSheetWidth(), coverImpositionJob.getSheetHeight());
			
			Rectangle rectangle = new Rectangle(Format.mm2points(box.getLeft()).floatValue(),
			Format.mm2points(box.getBottom()).floatValue(), Format.mm2points(box.getRight()).floatValue(),
			Format.mm2points(box.getTop()).floatValue());
			
			doc = new Document(rectangle);
			FileOutputStream file = new FileOutputStream(batchOutputFile);
			writer = PdfWriter.getInstance(doc, file);
			doc.open();
		}catch (Exception e) {
			LogUtils.error("Error occured while creating combined covers file", e);
			progress.setError("Error occured while creating combined covers files, See log file.");
			return;
		}


		PdfContentByte cb = writer.getDirectContent();

		List<PdfReader> readers = new ArrayList<>();


		for(int j = covers.length -1; j >= 0; j--){
			Object b = covers[j];
			String bookId;
			if(b instanceof Book)
			bookId = ((Book)b).getBookId();
			else
			bookId = String.valueOf(b);
			
			try {
				String imposedBookPath = "imposed/" + bookId + "/";
				File imposedFolder  = new File(imposedPath, imposedBookPath);
				File imposedFileCover = new File(imposedFolder, sheetType.concat("_").concat(bookId).concat(".cover.pdf"));
				if(!imposedFileCover.exists()){
					imposedFileCover = new File(imposedFolder, bookId.concat(".cover.pdf"));
				}
				reader = new PdfReader(imposedFileCover.getAbsolutePath());
				readers.add(reader);
				
				for(int i=1; i <= reader.getNumberOfPages(); i++){
					doc.newPage();
					writer.setPageEmpty(false);
					PdfImportedPage page = writer.getImportedPage(reader, i);
					cb.addTemplate(page,0,0);
				}
				
				// add a blank page for verso
				if(reader.getNumberOfPages() == 1){
					doc.newPage();
					writer.setPageEmpty(false);
					
				}
				
			}catch (Exception e) {
				LogUtils.error("Error occured while creating combined covers file", e);
				progress.setError("Error occured while creating combined covers files, See log file.");
				return;
			}
		}

		try {
			doc.close();
			writer.close();
			for (PdfReader pdfReader : readers) 
			pdfReader.close();
		} catch (Exception e2) {}
		
	}

	
	/**
	 * 
	 */
	private static void runImposition (){

		String output = System.getProperty(Constants.COMPOSER_OUTPUT);
		
		if(!output.endsWith("/"))
			output = output.concat("/");
		
		 List<Object> books;
         try {
                 books = getBooks();
         } catch (Exception e) {
                 LogUtils.error(e.getMessage());
                 progress.setError("Reading books list failed, See log file.");
                 return;
         }
         
		
		ImpositionJob.totalBatch = books.size();
		
		
		int index = 0;
		String combine = System.getProperty(Constants.COMBINE_COVERS, "false");
		String batch = System.getProperty(Constants.COVERS_BATCH_NAME);
		
		
		for(Object b: books){
			Book book = null;
			try {
				 if(b instanceof Book)
                     book = (Book)b;
             else
                     book = getBook(String.valueOf(b));
             
				
				if(book == null){
					progress.stop();
					return;
				}
				String suffix = new String("");
				if(Boolean.valueOf(combine))
					suffix = String.valueOf(index).concat(".");
				String bookId = book.getBookId();
				String imposedBookPath = "imposed/" + bookId + "/";
				String originalBookPath = "original/" + bookId + "/";
				
				File imposedFolder  = new File(output, imposedBookPath);
				File originalFolder = new File(output, originalBookPath);
	
				if(!imposedFolder.exists())
					imposedFolder.mkdirs();
				
				if(!originalFolder.exists())
					originalFolder.mkdirs();

				
				File originalPdfText = new File(output, originalBookPath.concat(book.getBookId().concat(".text.pdf")));
				File imposedFileText = new File(imposedFolder, book.getBookId().concat(".text.pdf"));
				File croppedFileText = File.createTempFile(bookId, ".temp.text.pdf");
				
				File originalPdfCover = new File(output, originalBookPath.concat(book.getBookId().concat(".cover.pdf")));
				File imposedFileCover = new File(imposedFolder, book.getBookId().concat(".cover.").concat(suffix).concat("pdf"));
				File croppedFileCover = File.createTempFile(bookId, ".temp.cover.pdf");
				
				String  hunkelerLine 		= System.getProperty(Constants.IMPOSITION_HUNKELER_LINE);
				
				ImpositionJob job = ImpositionJob.create(book, croppedFileText.getAbsolutePath(), imposedFileText.getAbsolutePath());
				
				LogUtils.debug("Imposition started for "+book.getBookId());
				progress.setLabel("Imposition started for "+book.getBookId());
				
				if (job.getImpositionType() == ImpositionType.TEXT || job.getImpositionType() == ImpositionType.ALL ){
					
					LogUtils.debug("Text Imposition");
					progress.setLabel("Text Imposition");
					
					//download text file if does not exist
					if(!originalPdfText.exists())
						downloadText(book, originalPdfText);
					else{
						LogUtils.debug("Text file already exists, no need to redownload: "+originalPdfText.getAbsolutePath());
						progress.setLabel("Text file already exists, no need to redownload: "+originalPdfText.getAbsolutePath());
					}
					
					FileUtils.copyFile(originalPdfText, croppedFileText, false);

					String [] hunkelerLines = {"PF", "PL", "FF", "PB"};
		
					//impose text file
					if (job.isAllLines()){
						for (String line: hunkelerLines){
							boolean enabled = Boolean.parseBoolean(System.getProperty("com.epac.composer." + line + ".enabled", "false"));
							if(!enabled)
								continue;
							
							ImpositionJob newJob = ImpositionJob.setUpImpositionBook(line, book, croppedFileText.getAbsolutePath(),  imposedFileText.getAbsolutePath());
							//imposeTextFile(bookId, imposedFileText.getAbsolutePath(), newJob);
							
							ImpositionHandler.setupProgressBar(progress);
							ImpositionHandler.imposeTextFile(bookId, imposedFileText.getAbsolutePath(), newJob);
						}
					}else{
						//imposeTextFile(bookId, imposedFileText.getAbsolutePath(), job);
						
						ImpositionHandler.setupProgressBar(progress);
						ImpositionHandler.imposeTextFile(bookId, imposedFileText.getAbsolutePath(), job);
					}
			
				}
				
				if (job.getImpositionType() == ImpositionType.COVER || job.getImpositionType() == ImpositionType.ALL){
					
					LogUtils.debug("Cover Imposition");
					progress.setLabel("Cover Imposition");

					//download cover file if does not exist
					if(!originalPdfCover.exists())
						downloadCover(book, originalPdfCover);
					else{
						LogUtils.debug("Cover file already exists, no need to redownload: "+originalPdfCover.getAbsolutePath());
						progress.setLabel("Cover file already exists, no need to redownload: "+originalPdfCover.getAbsolutePath());
					}
					
					FileUtils.copyFile(originalPdfCover, croppedFileCover, false);
					
					//impose cover file
					imposeCoverFile(bookId, croppedFileCover.getAbsolutePath(), imposedFileCover.getAbsolutePath(), job);
					index++;
					
				}
				/*
				else if (job.getImpositionType() == ImpositionType.ALL){
					
					LogUtils.debug("Text&Cover Imposition");
					progress.setLabel("Text&Cover Imposition");
					
					//download text file if does not exist
					if(!originalPdfText.exists())
						downloadText(book, originalPdfText);
					else{
						LogUtils.debug("Text file already exists, no need to redownload: "+originalPdfText.getAbsolutePath());
						progress.setLabel("Text file already exists, no need to redownload: "+originalPdfText.getAbsolutePath());
					}
					
					//impose text file
					if (job.isAllLines()){
						System.out.println("impose all types " + job.getHunkelerlines());
						for (String s:job.getHunkelerlines()){
							ImpositionJob newJob = ImpositionJob.setUpImpositionBook(s, book, originalPdfText.getAbsolutePath(),  imposedFileText.getAbsolutePath());
							
							imposeTextFile(bookId, imposedFileText.getAbsolutePath(), newJob);
						}
					}else{
						imposeTextFile(bookId, imposedFileText.getAbsolutePath(), job);
					}
					
					//download cover file if does not exist
					if(!originalPdfCover.exists())
						downloadCover(book, originalPdfCover);
					else{
						LogUtils.debug("Cover file already exists, no need to redownload: "+originalPdfCover.getAbsolutePath());
						progress.setLabel("Cover file already exists, no need to redownload: "+originalPdfCover.getAbsolutePath());
					}
					
					//impose cover file
					imposeCoverFile(bookId, originalPdfCover.getAbsolutePath(), imposedFileCover.getAbsolutePath(), job);
				}
				*/
				
				FileUtils.forceDelete(croppedFileText);				
				FileUtils.forceDelete(croppedFileCover);
				
			} catch (Exception e) {
				Throwable t = e;
				if(t.getCause() != null)
					t = t.getCause();
				LogUtils.error(e.getLocalizedMessage(), t);
				progress.setError("Imposition failed. See log file.");
			}		
		}
		
		ImpositionType	impositionType		= ImpositionJob.getImpositionTypeFromConfig(Byte.parseByte(System.getProperty(Constants.IMPOSITION_TYPE)));
		
		if(impositionType == ImpositionType.TEXT)
			return;

		
		float sheetWidth    = coverImpositionJob.getSheetWidth();
		float mSheetWidth   = Float.parseFloat(System.getProperty(Constants.IMPOSITION_M_SHEET_WIDTH));
		float lSheetWidth   = Float.parseFloat(System.getProperty(Constants.IMPOSITION_L_SHEET_WIDTH));
		float xlSheetWidth  = Float.parseFloat(System.getProperty(Constants.IMPOSITION_XL_SHEET_WIDTH));
		
		String sheetType = "M";
		if(sheetWidth == mSheetWidth)
			sheetType = "M";
		else if(sheetWidth == lSheetWidth)
			sheetType = "L";
		else if(sheetWidth == xlSheetWidth)
			sheetType = "XL";
		
		if(batch == null || !"true".equalsIgnoreCase(combine))
			return;
		
		File batchOutputFile  = new File(output, batch.concat(".pdf"));
		
		Document doc = null;
		PdfWriter writer = null;
		PdfReader reader = null;
		try {
			Rectangle box = new Rectangle(coverImpositionJob.getSheetWidth(), coverImpositionJob.getSheetHeight());
			
			Rectangle rectangle = new Rectangle(Format.mm2points(box.getLeft()).floatValue(),
					Format.mm2points(box.getBottom()).floatValue(), Format.mm2points(box.getRight()).floatValue(),
					Format.mm2points(box.getTop()).floatValue());
			
			doc = new Document(rectangle);
			FileOutputStream file = new FileOutputStream(batchOutputFile);
			writer = PdfWriter.getInstance(doc, file);
			doc.open();
		}catch (Exception e) {
			LogUtils.error("Error occured while creating combined covers file", e);
			progress.setError("Error occured while creating combined covers files, See log file.");
            return;
		}
		
		
		PdfContentByte cb = writer.getDirectContent();

		List<PdfReader> readers = new ArrayList<>();
			
		for(int j = books.size() -1; j >= 0; j--){
			Object b = books.get(j);
			String bookId;
			if(b instanceof Book)
				bookId = ((Book)b).getBookId();
			else
				bookId = String.valueOf(b);
			
			try {
				String imposedBookPath = "imposed/" + bookId + "/";
				File imposedFolder  = new File(output, imposedBookPath);
				File imposedFileCover = new File(imposedFolder, sheetType.concat("_").concat(bookId).concat(".cover.pdf"));
				if(!imposedFileCover.exists()){
					imposedFileCover = new File(imposedFolder, bookId.concat(".cover.pdf"));
				}
				
				reader = new PdfReader(imposedFileCover.getAbsolutePath());
				readers.add(reader);
				
				for(int i=1; i <= reader.getNumberOfPages(); i++){
					doc.newPage();
					writer.setPageEmpty(false);
					PdfImportedPage page = writer.getImportedPage(reader, i);
					cb.addTemplate(page,0,0);
				}
				
				// add a blank page for verso
				if(reader.getNumberOfPages() == 1){
					doc.newPage();
					writer.setPageEmpty(false);

				}

			}catch (Exception e) {
				LogUtils.error("Error occured while creating combined covers file", e);
				progress.setError("Error occured while creating combined covers files, See log file.");
	            return;
			}
		}
		
		try {
			doc.close();
			writer.close();
			for (PdfReader pdfReader : readers) 
				pdfReader.close();
		} catch (Exception e2) {}
		
	}
		
	
	private static void imposeCoverFile (String bookId, String originalFilePath, String imposedFilePath, ImpositionJob job) throws ComposerException{
		float textBleed	= Float.parseFloat(System.getProperty(Constants.TEXT_BLEED_VALUE));
		
		LogUtils.debug("Start imposing cover file to "+imposedFilePath);
		progress.setLabel("Start imposing cover file to "+imposedFilePath);
		
		coverImpositionJob = com.epac.imposition.cover.ImpositionJob.create();
		
		float topmargin = job.getTopMargin() + job.getOutputControllerMargin() + textBleed;
		
		float finalwidth = (float) job.getBook().getMetadataWidth();
		float finalheight = (float) job.getBook().getMetadataHeight();
		double thickness = job.getBook().getBookThickness();
	
		float rawheight = topmargin + job.getBottomMargin() + finalheight;
		float frontmargin = coverImpositionJob.getyPosition();

		Cover cover = new Cover(job.getBook(), rawheight, frontmargin, finalheight, finalwidth, thickness, originalFilePath);	
		coverImpositionJob.setCover(cover);

		CoverImposer imposer = new CoverImposer(coverImpositionJob, imposedFilePath);
		imposer.compose();
		
		LogUtils.debug("Cover imposition finished successfully for "+bookId);
		progress.setLabel("Cover imposition finished successfully for "+bookId);
	}
	
	
	private static ProgressBars progress;
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
                  progress = new ProgressBars();
            }
		});
		try {
			Thread.sleep(4000);
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			execute ();	
		} catch (Exception e) {
			LogUtils.error(e.getLocalizedMessage(), e);
			progress.setError("Imposition failed. See log file.");
		}
		progress.stop();
	}
}
