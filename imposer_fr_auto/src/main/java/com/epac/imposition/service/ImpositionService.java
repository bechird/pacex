package com.epac.imposition.service;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Produces;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.epac.imposition.bookblock.App;
import com.epac.imposition.bookblock.PnlFactory;
import com.epac.imposition.model.PNLInfo;
import com.epac.imposition.model.PNLTemplateLine;
import com.epac.om.api.book.Book;
import com.epac.om.api.book.Metadata;
import com.epac.om.api.book.PaperType;
import com.epac.om.api.utils.LogUtils;
import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

import org.springframework.core.io.Resource;

@RestController
public class ImpositionService {

	private static final String STANDARD_MODE_RASTER_FILTER = "MB2";

	private static final String ROLL_WIDTH = "rollWidth";
	private static final String COVER_PATH = "cover";
	private static final String TEXT_PATH = "text";
	private static final String TEXT_OUTPUT = "textOutput";
	private static final String COVER_OUTPUT = "coverOutput";
	private static final String BOOK_WIDTH = "bookWidth";
	private static final String BOOK_HEIGHT = "bookHeight";
	private static final String BOOK_THICKNESS = "bookThickness";
	private static final String BOOK_ID = "bookId";
	private static final String BOOK_ISBN = "barcode";
	private static final String PAPER_THICKNESS = "paperThickness";
	private static final String HUNKELER_LINES = "hunkelerLines";
	private static final String BEST_SHEETS = "bestSheets";
	private static final String PERFORATION = "perforation";
	private static final String PNLINFORMATIONS = "pnlInformation";

	public ImpositionService() {
		LogUtils.debug("ImpositionService instance created");
	}

	@RequestMapping(value = "/pnlPreview", method = RequestMethod.POST)
	public void download(@RequestBody Map<String, Object> pnlOrg, HttpServletResponse response) throws IOException {
		OutputStream out = null;
		FileInputStream in = null;
		File pnlfPreview = null;
		File fsOriginal = null;
		File tempFolder = null;
		//PNLInfo(Integer pageNumber, Float hMargin, Float vMargin, ArrayList<PNLTemplateLine> pnlLines,
		//		Float pageWidth, Float pageHeight, Float lineSpacing) {
		PNLInfo pnl = null;
				//new PNLInfo((Integer)pnlOrg.get(""), pnlOrg.get(""), pnlOrg.get(""), pnlOrg.get(""), pnlOrg.get(""), pnlOrg.get(""), pnlOrg.get(""));
		try {
			if (pnl != null && pnl.getPnlLines() != null) {
				Document document = new Document();
				fsOriginal = File.createTempFile(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()),
						"original.pdf");
				PdfWriter.getInstance(document, new FileOutputStream(fsOriginal));
				Rectangle rect = new Rectangle(pnl.getPageWidth(), pnl.getPageHeight());
				document.setPageSize(rect);
				document.open();
				document.newPage();
				document.close();
				
				tempFolder = new File(fsOriginal.getParent()+File.separator+"pnlTemp");
				ArrayList<PNLTemplateLine> PNLTemplateLines = pnl.getPnlLines();
				PNLTemplateLines.sort((pnl1, pnl2) -> pnl1.getOrdering().compareTo(pnl2.getOrdering()));
				pnl.setPnlLines(PNLTemplateLines);
				pnlfPreview = new PnlFactory().createPnl(fsOriginal, pnl);

				response.setContentType("application/pdf");
				response.setHeader("Content-disposition", "attachment; filename=" + pnlfPreview.getName());
				out = response.getOutputStream();
				in = new FileInputStream(pnlfPreview);
				// copy from in to out
				IOUtils.copy(in, out);
				LogUtils.info("content for preview is generated successfully");
			} else {
				LogUtils.error("content for preview cannot be generated due to null pnl infos values");
				//response.sendError(sc, msg);
			}
		} catch (Exception e) {
			LogUtils.error("content for preview cannot be generated due to an error[" + e + "]");
		} finally {
			out.close();
			in.close();
			pnlfPreview.delete();
			fsOriginal.delete();
			deleteFile(tempFolder);
		}
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/submit", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> submit(@RequestBody Map<String, Object> parameter) {
		PNLInfo pnlInfos =null;
		//Float pageWidth = 0f;
		//Float pageHeight = 0f;
		Map<String, Object> response = new HashMap<>();
		response.put("error", false);
		try {

			String coverPath = String.valueOf(parameter.get(COVER_PATH));
			String textPath = String.valueOf(parameter.get(TEXT_PATH));
			String textOutput = String.valueOf(parameter.get(TEXT_OUTPUT));
			String coverOutput = String.valueOf(parameter.get(COVER_OUTPUT));
			String barcode = String.valueOf(parameter.get(BOOK_ISBN));
			String bookId = String.valueOf(parameter.get(BOOK_ID));
			float bookWidth = Float.parseFloat(String.valueOf(parameter.get(BOOK_WIDTH)));
			float bookHeight = Float.parseFloat(String.valueOf(parameter.get(BOOK_HEIGHT)));
			float bookSpine = Float.parseFloat(String.valueOf(parameter.get(BOOK_THICKNESS)));
			float paperThickness = Float.parseFloat(String.valueOf(parameter.get(PAPER_THICKNESS)));
			float width = Float.parseFloat(String.valueOf(parameter.get(ROLL_WIDTH)));
			String perforation = String.valueOf(parameter.getOrDefault(PERFORATION, "0"));
			List<String> bestSheetsParam = (List<String>) parameter.get(BEST_SHEETS);
			List<String> hunkelerLinesParam = (List<String>) parameter.get(HUNKELER_LINES);
			Map<String, Object> rcvPnl = (Map<String, Object>) parameter.get(PNLINFORMATIONS);
			if (rcvPnl != null ) {
			Integer pageNumber = (Integer) rcvPnl.get("pageNumber");
			Float hMargin = ((Double) rcvPnl.get("hMargin")).floatValue();
			Float vMargin = ((Double) rcvPnl.get("vMargin")).floatValue();
			ArrayList<java.util.LinkedHashMap> pnlLines = (ArrayList<java.util.LinkedHashMap>) rcvPnl.get("pnlLines");
			//Float lineSpacing = ((Double) rcvPnl.get("lineSpacing")).floatValue();
			Float lineSpacing = (rcvPnl.get("lineSpacing") != null && ((Double) rcvPnl.get("lineSpacing")).floatValue() != 0.0)?((Double) rcvPnl.get("lineSpacing")).floatValue():2;
			/*if (rcvPnl.get("pageWidth") != null && rcvPnl.get("pageHeight") != null) {
				 pageWidth = ((Double) rcvPnl.get("pageWidth")).floatValue();
				 pageHeight = ((Double) rcvPnl.get("pageHeight")).floatValue();
			}*/
			ArrayList<PNLTemplateLine> templateList = new ArrayList<>();
			
			for(java.util.LinkedHashMap line :(ArrayList<java.util.LinkedHashMap>) pnlLines)
			{PNLTemplateLine tpl = new PNLTemplateLine();
			tpl.setFontBold((Boolean)line.get("fontBold"));
			tpl.setFontItalic((Boolean)line.get("fontItalic"));
			//if(rcvPnl.get("fontSize")!=null)
			Float fontsize = (line.get("fontSize") != null)?((Double) line.get("fontSize")).floatValue():6;
			tpl.setFontSize(fontsize);
			tpl.setFontType((String)line.get("fontType"));
			tpl.setId((Integer)line.get("id"));
			tpl.setLineText((String)line.get("lineText"));
			tpl.setOrdering((Integer)line.get("ordering"));
			tpl.setTemplateId((String)line.get("templateId"));
			templateList.add(tpl);
			}	
			
				//pnlInfos = new PNLInfo(pageNumber, hMargin, vMargin, pnlLines, pageWidth, pageHeight,lineSpacing);
				pnlInfos = new PNLInfo(pageNumber, hMargin, vMargin, templateList, 0f, 0f,lineSpacing);
				ArrayList<PNLTemplateLine> PNLTemplateLines = pnlInfos.getPnlLines();
				PNLTemplateLines.sort((pnl1, pnl2) -> pnl2.getOrdering().compareTo(pnl1.getOrdering()));
				pnlInfos.setPnlLines(PNLTemplateLines);
			}
			String[] hunkelerLines = hunkelerLinesParam.toArray(new String[hunkelerLinesParam.size()]);
			String[] bestSheets = bestSheetsParam.toArray(new String[bestSheetsParam.size()]);
			Book book = new Book();
			Metadata metadata = new Metadata();
			book.setBookId(bookId);
			book.setMetadata(metadata);
			PaperType textPaperType = new PaperType();
			textPaperType.setPaperThickness(paperThickness);

			metadata.setBarcode(barcode);
			metadata.setTextPaperType(textPaperType);
			metadata.setThickness(bookSpine);
			metadata.setWidth(bookWidth);
			metadata.setHeight(bookHeight);

			//ImpositionTask task = new ImpositionTask(book, width, textPath, coverPath, textOutput, coverOutput,
				//	perforation, buildPNL((Map<String, Object>) parameter.get(PNLINFORMATIONS)), hunkelerLines);
			ImpositionTask task = new ImpositionTask(book, width, textPath, coverPath, textOutput, coverOutput,
					perforation, pnlInfos, bestSheets, hunkelerLines);
			
			task.run();

			// Look for generated files

			String[] lines = { "FFSM", "FFEM", "PFSM", "PFEM", "PLSM", "PLEM", "PBSM", "PBEM" };
			String[] types = { ".PDF", ".JDF" };
			File textDirectory = new File(textOutput);
			File coverDirectory = new File(coverOutput);

			for (int i = 0; i < types.length; i++) {
				for (int j = 0; j < lines.length; j++) {
					File[] file = findFiles(textDirectory, types[i], lines[j],
							FilenameUtils.getBaseName(textPath).toUpperCase());

					if (file != null && file.length > 0){
						StringBuilder sb = new StringBuilder();
						for(int k = 0; k < file.length; k++){
							sb.append(file[k].getAbsolutePath());
							if(k < file.length-1){
								sb.append(";");
							}
						}
						response.put(lines[j].concat(types[i]), sb.toString());
					}
				}
			}

			File[] cover = findFiles(coverDirectory, "PDF", FilenameUtils.getBaseName(coverPath).toUpperCase());
			if (cover != null)
				response.put("cover", cover[0].getAbsolutePath());

		} catch (Exception e) {
			response.put("error", true);
			response.put("message", "Imposition failed to start: " + e.getMessage());
			LogUtils.error("Imposition error:", e);
		}

		return ResponseEntity.ok(response);

	}
	
	private PNLInfo buildPNL(Map<String, Object> originalBean){
		PNLInfo result = null;
		if(originalBean != null){
			
		}
		return result;
	}

	public File[] findFiles(File directory, String... tokens) {
		StringBuilder tokensText = new StringBuilder();
		for (int i = 0; i < tokens.length; i++) {
			tokensText.append(tokens[i]).append(" ");
		}
		LogUtils.debug("looking for file in [" + directory + "] having these tokens: " + tokensText.toString());
		File[] files = directory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				for (int i = 0; i < tokens.length; i++) {
					if (!pathname.getName().toUpperCase().contains(tokens[i]))
						return false;
				}
				return true;
			}
		});

		if (files == null || files.length == 0)
			return null;
		return files;
	}

	private static List<File> findFile(String location, String... tokens) {
		File directory = new File(location);

		System.out.println("findFile directory " + directory);
		System.out.println("findFile tokens " + Arrays.asList(tokens));
		if (!directory.exists() || !directory.isDirectory())
			return null;

		File[] files = directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				System.out.println("accept " + dir + " - " + name);
				for (int i = 0; i < tokens.length; i++) {
					if (name.indexOf(tokens[i]) == -1)
						return false;
				}
				return true;
			}
		});

		return Arrays.asList(files);
	}

	@RequestMapping(value = "info", method = RequestMethod.GET)
	public ResponseEntity<?> info() {
		Map<String, String> response = new HashMap<>();

		response.put("name", "Epac imposition service");
		response.put("version", App.IMPOSITION_VERSION);
		return ResponseEntity.ok(response);
	}

	@RequestMapping(value = "/files", method = RequestMethod.POST)
	private Map<String, Map<String, Object>> findBookFiles(@RequestBody Map<String, String> params) {
		Map<String, Map<String, Object>> files = new HashMap<String, Map<String, Object>>();
		files.put(Book.COVER, new HashMap<String, Object>());
		files.put(Book.TEXT, new HashMap<String, Object>());

		String bookId = params.get("id");
		File rootfolder = new File(System.getProperty("com.epac.imposer.repository"), bookId);

		String[] types = { Book.ORIGINAL, Book.PROOFED, Book.IMPOSED, Book.RASTER };

		for (int i = 0; i < types.length; i++) {
			String location = types[i].toLowerCase(); // System.getProperty(locations[i]);

			if (location == null)
				continue;

			location = location.concat(File.separator);

			List<File> found = findFile(location, bookId.concat(".").concat(Book.COVER));
			List<Object> add = new ArrayList<>();
			if (found != null)
				for (File f : found) {
					Map<String, String> file = new HashMap<String, String>();
					file.put(f.getName(), f.getAbsolutePath());
					add.add(file);
					files.get(Book.COVER).put(types[i], file);
				}
			// if(add.size() > 0)
			// files.get(Book.COVER).put(types[i], add);

			System.out.println("COVER " + files);
		}

		for (int i = 0; i < types.length; i++) {

			String location = types[i].toLowerCase();
			if (location == null)
				continue;

			if (Book.IMPOSED.equals(types[i])) {
				Map<String, Object> ff = new HashMap<String, Object>();
				Map<String, Object> pf = new HashMap<String, Object>();
				Map<String, Object> pl = new HashMap<String, Object>();
				Map<String, Object> pb = new HashMap<String, Object>();

				// EPAC mode
				List<File> plEmPdf = findFile(location, Book.PL_HUNKELER.concat(Book.EPAC_MODE),
						bookId.concat(".").concat(Book.TEXT));
				List<Object> plEm = new ArrayList<>();
				if (plEmPdf != null)
					for (File f : plEmPdf) {
						Map<String, String> file = new HashMap<String, String>();
						file.put(f.getName(), f.getAbsolutePath().replace(location, ""));
						plEm.add(file);
					}
				if (plEm.size() > 0)
					pl.put(Book.EPAC_MODE, plEm);

				List<File> pbEmPdf = findFile(location, Book.PB_HUNKELER.concat(Book.EPAC_MODE),
						bookId.concat(".").concat(Book.TEXT));
				List<Object> pbEm = new ArrayList<>();
				if (pbEmPdf != null)
					for (File f : pbEmPdf) {
						Map<String, String> file = new HashMap<String, String>();
						file.put(f.getName(), f.getAbsolutePath().replace(location, ""));
						pbEm.add(file);
					}

				if (pbEm.size() > 0)
					pb.put(Book.EPAC_MODE, pbEm);

				// Normal Mode
				List<File> plSmPdf = findFile(location, Book.PL_HUNKELER.concat(Book.STND_MODE),
						bookId.concat(".").concat(Book.TEXT));
				List<Object> plSm = new ArrayList<>();
				if (plSmPdf != null)
					for (File f : plSmPdf) {
						Map<String, String> file = new HashMap<String, String>();
						file.put(f.getName(), f.getAbsolutePath().replace(location, ""));
						plSm.add(file);
					}

				if (plSm.size() > 0)
					pl.put(Book.STND_MODE, plSm);

				List<File> pbSmPdf = findFile(location, Book.PB_HUNKELER.concat(Book.STND_MODE),
						bookId.concat(".").concat(Book.TEXT));
				List<Object> pbSm = new ArrayList<>();
				if (pbSmPdf != null)
					for (File f : pbSmPdf) {
						Map<String, String> file = new HashMap<String, String>();
						file.put(f.getName(), f.getAbsolutePath().replace(location, ""));
						pbSm.add(file);
					}

				if (pbSm.size() > 0)
					pb.put(Book.STND_MODE, pbSm);

				// EPAC mode
				List<File> ffEmPdf = findFile(location, Book.FF_HUNKELER.concat(Book.EPAC_MODE),
						bookId.concat(".").concat(Book.TEXT));
				List<Object> ffEm = new ArrayList<>();
				if (ffEmPdf != null)
					for (File f : ffEmPdf) {
						Map<String, String> file = new HashMap<String, String>();
						file.put(f.getName(), f.getAbsolutePath().replace(location, ""));
						ffEm.add(file);
					}

				if (ffEm.size() > 0)
					ff.put(Book.EPAC_MODE, ffEm);

				// Normal Mode
				List<File> ffSmPdf = findFile(location, Book.FF_HUNKELER.concat(Book.STND_MODE),
						bookId.concat(".").concat(Book.TEXT));

				List<Object> ffSm = new ArrayList<>();
				if (ffSmPdf != null)
					for (File f : ffSmPdf) {

						Map<String, String> file = new HashMap<String, String>();
						file.put(f.getName(), f.getAbsolutePath().replace(location, ""));
						ffSm.add(file);
					}

				if (ffSm.size() > 0)
					ff.put(Book.STND_MODE, ffSm);

				// EPAC mode
				List<File> pfEmPdf = findFile(location, Book.PF_HUNKELER.concat(Book.EPAC_MODE),
						bookId.concat(".").concat(Book.TEXT));

				List<Object> pfEm = new ArrayList<>();
				if (pfEmPdf != null)
					for (File f : pfEmPdf) {
						Map<String, String> file = new HashMap<String, String>();
						file.put(f.getName(), f.getAbsolutePath().replace(location, ""));
						pfEm.add(file);
					}

				if (pfEm.size() > 0)
					pf.put(Book.EPAC_MODE, pfEm);

				// Normal Mode
				List<File> pfSmPdf = findFile(location, Book.PF_HUNKELER.concat(Book.STND_MODE),
						bookId.concat(".").concat(Book.TEXT));

				List<Object> pfSm = new ArrayList<>();
				if (pfSmPdf != null)
					for (File f : pfSmPdf) {
						Map<String, String> file = new HashMap<String, String>();
						file.put(f.getName(), f.getAbsolutePath().replace(location, ""));
						pfSm.add(file);
					}

				if (pfSm.size() > 0)
					pf.put(Book.STND_MODE, pfSm);

				if (ff.size() > 0 || pf.size() > 0) {
					Map<String, Object> imposed = new HashMap<>();
					files.get(Book.TEXT).put(types[i], imposed);

					if (ff.size() > 0)
						imposed.put(Book.FF_HUNKELER, ff);

					if (pf.size() > 0)
						imposed.put(Book.PF_HUNKELER, pf);

					if (pl.size() > 0)
						imposed.put(Book.PL_HUNKELER, pl);

					if (pb.size() > 0)
						imposed.put(Book.PB_HUNKELER, pb);
				}

			} else if (Book.RASTER.equals(types[i])) {
				Map<String, Object> ff = new HashMap<String, Object>();
				Map<String, Object> pf = new HashMap<String, Object>();
				Map<String, Object> pl = new HashMap<String, Object>();
				Map<String, Object> pb = new HashMap<String, Object>();

				// EPAC mode
				List<File> plEmPdf = findFile(location.concat(Book.EPAC_MODE).concat("/"),
						Book.PL_HUNKELER.concat(Book.EPAC_MODE), bookId);
				List<Object> plEm = new ArrayList<>();
				if (plEmPdf != null)
					for (File f : plEmPdf) {
						Map<String, String> file = new HashMap<String, String>();
						file.put(f.getName(), f.getAbsolutePath().replace(location, ""));
						plEm.add(file);
					}

				if (plEm.size() > 0)
					pl.put(Book.EPAC_MODE, plEm);

				List<File> pbEmPdf = findFile(location.concat(Book.EPAC_MODE).concat("/"),
						Book.PB_HUNKELER.concat(Book.EPAC_MODE), bookId);
				List<Object> pbEm = new ArrayList<>();
				if (pbEmPdf != null)
					for (File f : pbEmPdf) {
						Map<String, String> file = new HashMap<String, String>();
						file.put(f.getName(), f.getAbsolutePath().replace(location, ""));
						pbEm.add(file);
					}

				if (pbEm.size() > 0)
					pb.put(Book.EPAC_MODE, pbEm);

				// Normal Mode
				List<File> plSmPdf = findFile(location.concat(Book.STND_MODE).concat("/"),
						Book.PL_HUNKELER.concat(Book.STND_MODE), bookId, STANDARD_MODE_RASTER_FILTER);
				List<Object> plSm = new ArrayList<>();
				if (plSmPdf != null) {
					for (File f : plSmPdf) {
						Map<String, String> file = new HashMap<String, String>();
						file.put(f.getName(), f.getAbsolutePath().replace(location, ""));
						plSm.add(file);
					}
				}

				if (plSm.size() > 0)
					pl.put(Book.STND_MODE, plSm);

				List<File> pbSmPdf = findFile(location.concat(Book.STND_MODE).concat("/"),
						Book.PB_HUNKELER.concat(Book.STND_MODE), bookId, STANDARD_MODE_RASTER_FILTER);
				List<Object> pbSm = new ArrayList<>();
				if (pbSmPdf != null) {
					for (File f : pbSmPdf) {
						Map<String, String> file = new HashMap<String, String>();
						file.put(f.getName(), f.getAbsolutePath().replace(location, ""));
						pbSm.add(file);
					}
				}

				if (pbSm.size() > 0)
					pb.put(Book.STND_MODE, pbSm);

				//
				// EPAC mode
				List<File> ffEmPdf = findFile(location.concat(Book.EPAC_MODE).concat("/"),
						Book.FF_HUNKELER.concat(Book.EPAC_MODE), bookId);
				List<Object> ffEm = new ArrayList<>();
				if (ffEmPdf != null)
					for (File f : ffEmPdf) {
						Map<String, String> file = new HashMap<String, String>();
						file.put(f.getName(), f.getAbsolutePath().replace(location, ""));
						ffEm.add(file);
					}

				if (ffEm.size() > 0)
					ff.put(Book.EPAC_MODE, ffEm);

				// Normal Mode
				List<File> ffSmPdf = findFile(location.concat(Book.STND_MODE).concat("/"),
						Book.FF_HUNKELER.concat(Book.STND_MODE), bookId, STANDARD_MODE_RASTER_FILTER);
				List<Object> ffSm = new ArrayList<>();
				if (ffSmPdf != null) {
					for (File f : ffSmPdf) {
						Map<String, String> file = new HashMap<String, String>();
						file.put(f.getName(), f.getAbsolutePath().replace(location, ""));
						ffSm.add(file);
					}
				}

				if (ffSm.size() > 0)
					ff.put(Book.STND_MODE, ffSm);

				// EPAC mode
				List<File> pfEmPdf = findFile(location.concat(Book.EPAC_MODE).concat("/"),
						Book.PF_HUNKELER.concat(Book.EPAC_MODE), bookId);

				List<Object> pfEm = new ArrayList<>();
				if (pfEmPdf != null)
					for (File f : pfEmPdf) {
						Map<String, String> file = new HashMap<String, String>();
						file.put(f.getName(), f.getAbsolutePath().replace(location, ""));
						pfEm.add(file);
					}

				if (pfEm.size() > 0)
					pf.put(Book.EPAC_MODE, pfEm);

				// Normal Mode
				List<File> pfSmPdf = findFile(location.concat(Book.STND_MODE).concat("/"),
						Book.PF_HUNKELER.concat(Book.STND_MODE), bookId, STANDARD_MODE_RASTER_FILTER);
				List<Object> pfSm = new ArrayList<>();
				if (pfSmPdf != null)
					for (File f : pfSmPdf) {
						Map<String, String> file = new HashMap<String, String>();
						file.put(f.getName(), f.getAbsolutePath().replace(location, ""));
						pfSm.add(file);
					}

				if (pfSm.size() > 0)
					pf.put(Book.STND_MODE, pfSm);

				if (ff.size() > 0 || pf.size() > 0) {
					Map<String, Object> raster = new HashMap<>();
					files.get(Book.TEXT).put(types[i], raster);

					if (ff.size() > 0)
						raster.put(Book.FF_HUNKELER, ff);

					if (pf.size() > 0)
						raster.put(Book.PF_HUNKELER, pf);

					if (pl.size() > 0)
						raster.put(Book.PL_HUNKELER, pl);

					if (pb.size() > 0)
						raster.put(Book.PB_HUNKELER, pb);
				}
			} else {

				location = location.concat(bookId).concat(File.separator);
				List<File> found = findFile(location, bookId.concat(".").concat(Book.TEXT));
				List<Object> add = new ArrayList<>();
				if (found != null)
					for (File f : found) {
						Map<String, String> file = new HashMap<String, String>();
						file.put(f.getName(), f.getAbsolutePath().replace(location, ""));
						add.add(file);
					}

				if (add.size() > 0)
					files.get(Book.TEXT).put(types[i], add);
			}
		}

		if (files.get(Book.COVER).size() == 0)
			files.remove(Book.COVER);
		if (files.get(Book.TEXT).size() == 0)
			files.remove(Book.TEXT);

		System.out.println("retrun " + files);

		return files;
	}
	/**
	 * Deletes Folder with all of its content
	 *
	 * @param folder path to folder which should be deleted
	 */
	public static void deleteFile(File element) {
	    if (element.isDirectory()) {
	        for (File sub : element.listFiles()) {
	            deleteFile(sub);
	        }
	    }
	    element.delete();
	}
}
