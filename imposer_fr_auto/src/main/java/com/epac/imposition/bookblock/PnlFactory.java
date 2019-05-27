package com.epac.imposition.bookblock;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.epac.imposition.config.Constants;
import com.epac.imposition.config.LogUtils;
import com.epac.imposition.model.PNLInfo;
import com.epac.imposition.model.PNLTemplateLine;
import com.epac.imposition.utils.Format;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

public class PnlFactory {
	private static final String DTFORMAT = "yyyyMMddHHmmss";
	private static final String HELVETICA = BaseFont.HELVETICA;
	private static final String COURIER = BaseFont.COURIER;
	private static final String CALIBRI = "Calibri";
	private static final String CAMBRIA = "Cambria";

			
	public synchronized File createPnl(File originalText, PNLInfo pnlInfos) throws Exception {
		// Prepare pnl content
		LogUtils.info("Start composing pnl on text file ...");
		Document doc = null;
		PdfWriter writer = null;
		float aboveBaseline = 0f;
		float underBaseline = 0f;
		float height = 0f;
		int style = 0;
		float interline = 2f;
		float xshift = 0f;
		float yshift = 0f;
		File fsComposed = null;
		PdfStamper stamper = null;
		PdfReader pdfReader = null;
		FileOutputStream out = null;
		ArrayList pnlLines = new ArrayList<>();
		try {
			pdfReader = new PdfReader(originalText.getAbsolutePath());
			File tempDir = new File(System.getProperty(Constants.PNL_BASE_TEMP)+File.separator+"pnlTemp");
			if (!tempDir.exists())
			tempDir.mkdirs();
			String fname = originalText.getName().split("\\.pdf")[0];

			Rectangle origMediaBox = pdfReader.getBoxSize(2, "media");
			Rectangle origTrimBox = pdfReader.getBoxSize(2, "trim"); 
			xshift = (origMediaBox.getWidth() - origTrimBox.getWidth())/2;
			yshift = (origMediaBox.getHeight() - origTrimBox.getHeight())/2;
			fsComposed = new File(tempDir.getAbsolutePath() +File.separator+ fname + ".pdf");


			// stamper object is configured to writes content to the new PDF
			out = new FileOutputStream(fsComposed.getAbsolutePath());
			stamper = new PdfStamper(pdfReader, out);
			// Retrieve an instance of the ContentByte concerned by pnl
			PdfContentByte canvas = stamper.getOverContent(pnlInfos.getPageNumber());

			// Interline algorithm: take in consideration above and under baseline for each
			// font used by template lines
			pnlLines = pnlInfos.getPnlLines();
			// pnls margins
			xshift += Format.mm2points(pnlInfos.gethMargin()).floatValue();
			yshift += Format.mm2points(pnlInfos.getvMargin()).floatValue();
			// && pnlInfos.getLineSpacing()!=0)
			interline = Format.mm2points(pnlInfos.getLineSpacing()).floatValue();
			for (int i = 0; i < pnlLines.size(); i++) {
				if (((PNLTemplateLine) pnlLines.get(i)).getFontBold()) {
					if (((PNLTemplateLine) pnlLines.get(i)).getFontItalic()) {
						style = Font.BOLDITALIC;
					} else {
						style = Font.BOLD;
					}
				} else {

					if (((PNLTemplateLine) pnlLines.get(i)).getFontItalic()) {
						style = Font.ITALIC;
					} else {
						style = Font.NORMAL;
					}

				}

				switch (((PNLTemplateLine) pnlLines.get(i)).getFontType()) {
				case CAMBRIA: {

					BaseFont cambria;
					Font font;
					if (((PNLTemplateLine) pnlLines.get(i)).getFontBold()) {
						if (((PNLTemplateLine) pnlLines.get(i)).getFontItalic()) {
							cambria = BaseFont.createFont("CAMBRIAZ.TTF", BaseFont.WINANSI, BaseFont.EMBEDDED);
							font = new Font(cambria, ((PNLTemplateLine) pnlLines.get(i)).getFontSize(), style);
						} else {
							cambria = BaseFont.createFont("cambriab.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
							font = new Font(cambria, ((PNLTemplateLine) pnlLines.get(i)).getFontSize(), style);
						}
					} else {

						if (((PNLTemplateLine) pnlLines.get(i)).getFontItalic()) {
							cambria = BaseFont.createFont("CAMBRIAI.TTF", BaseFont.WINANSI, BaseFont.EMBEDDED);
							font = new Font(cambria, ((PNLTemplateLine) pnlLines.get(i)).getFontSize(), style);
						} else {
							cambria = BaseFont.createFont("Cambria.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
							font = new Font(cambria, ((PNLTemplateLine) pnlLines.get(i)).getFontSize(), style);

						}

					}

					ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
							new Paragraph(((PNLTemplateLine) pnlLines.get(i)).getLineText(), font), xshift,
							yshift + interline * i + height, 0.0f);
					System.out.println("info line " + ((PNLTemplateLine) pnlLines.get(i)).getId() + "   __> "
							+ ((PNLTemplateLine) pnlLines.get(i)).getLineText());
					aboveBaseline = font.getBaseFont().getAscentPoint(((PNLTemplateLine) pnlLines.get(i)).getLineText(),
							font.getSize());
					System.out.println(CALIBRI + " font aboveBaseline =" + aboveBaseline);
					underBaseline = font.getBaseFont()
							.getDescentPoint(((PNLTemplateLine) pnlLines.get(i)).getLineText(), font.getSize());
					System.out.println(CALIBRI + " font underBaseline =" + underBaseline);
					height += aboveBaseline - underBaseline;
					System.out.println(CALIBRI + " height =" + height);

					break;
				}
				case CALIBRI: {

					BaseFont calibri;
					Font font;
					if (((PNLTemplateLine) pnlLines.get(i)).getFontBold()) {
						if (((PNLTemplateLine) pnlLines.get(i)).getFontItalic()) {
							calibri = BaseFont.createFont("CALIBRIZ.TTF", BaseFont.WINANSI, BaseFont.EMBEDDED);
							font = new Font(calibri, ((PNLTemplateLine) pnlLines.get(i)).getFontSize(), style);
						} else {
							calibri = BaseFont.createFont("CALIBRIB.TTF", BaseFont.WINANSI, BaseFont.EMBEDDED);
							font = new Font(calibri, ((PNLTemplateLine) pnlLines.get(i)).getFontSize(), style);
						}
					} else {

						if (((PNLTemplateLine) pnlLines.get(i)).getFontItalic()) {
							calibri = BaseFont.createFont("CALIBRII.TTF", BaseFont.WINANSI, BaseFont.EMBEDDED);
							font = new Font(calibri, ((PNLTemplateLine) pnlLines.get(i)).getFontSize(), style);
						} else {
							calibri = BaseFont.createFont("Calibri.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
							font = new Font(calibri, ((PNLTemplateLine) pnlLines.get(i)).getFontSize(), style);

						}

					}

					ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
							new Paragraph(((PNLTemplateLine) pnlLines.get(i)).getLineText(), font), xshift,
							yshift + interline * i + height, 0.0f);
					System.out.println("info line " + ((PNLTemplateLine) pnlLines.get(i)).getId() + "   __> "
							+ ((PNLTemplateLine) pnlLines.get(i)).getLineText());
					aboveBaseline = font.getBaseFont().getAscentPoint(((PNLTemplateLine) pnlLines.get(i)).getLineText(),
							font.getSize());
					System.out.println(CALIBRI + " font aboveBaseline =" + aboveBaseline);
					underBaseline = font.getBaseFont()
							.getDescentPoint(((PNLTemplateLine) pnlLines.get(i)).getLineText(), font.getSize());
					System.out.println(CALIBRI + " font underBaseline =" + underBaseline);
					height += aboveBaseline - underBaseline;
					System.out.println(CALIBRI + " height =" + height);

					break;
				}
				case HELVETICA: {

					BaseFont bfbaseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
					Font font = new Font(bfbaseFont, ((PNLTemplateLine) pnlLines.get(i)).getFontSize(), style);
					ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
							new Paragraph(((PNLTemplateLine) pnlLines.get(i)).getLineText(), font), xshift,
							yshift + interline * i + height, 0.0f);
					System.out.println("info line " + ((PNLTemplateLine) pnlLines.get(i)).getId() + "   __> "
							+ ((PNLTemplateLine) pnlLines.get(i)).getLineText());
					aboveBaseline = font.getBaseFont().getAscentPoint(((PNLTemplateLine) pnlLines.get(i)).getLineText(),
							font.getSize());
					System.out.println(HELVETICA + " font aboveBaseline =" + aboveBaseline);
					underBaseline = font.getBaseFont()
							.getDescentPoint(((PNLTemplateLine) pnlLines.get(i)).getLineText(), font.getSize());
					System.out.println(HELVETICA + " font underBaseline =" + underBaseline);
					height += aboveBaseline - underBaseline;
					System.out.println(HELVETICA + " height" + height);

					break;
				}
				case COURIER: {
					BaseFont bfbaseFont = BaseFont.createFont(BaseFont.COURIER, BaseFont.WINANSI, BaseFont.EMBEDDED);
					Font font = new Font(bfbaseFont, ((PNLTemplateLine) pnlLines.get(i)).getFontSize());
					ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
							new Paragraph(((PNLTemplateLine) pnlLines.get(i)).getLineText(), font), xshift,
							yshift + interline * i + height, 0.0f);
					aboveBaseline = font.getBaseFont().getAscentPoint(((PNLTemplateLine) pnlLines.get(i)).getLineText(),
							font.getSize());
					underBaseline = font.getBaseFont()
							.getDescentPoint(((PNLTemplateLine) pnlLines.get(i)).getLineText(), font.getSize());
					height += aboveBaseline - underBaseline;

					break;
				}
				default: {
					BaseFont bfbaseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
					Font font = new Font(bfbaseFont, ((PNLTemplateLine) pnlLines.get(i)).getFontSize(), style);

					ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
							new Paragraph(((PNLTemplateLine) pnlLines.get(i)).getLineText(), font), xshift,
							yshift + interline * i + height, 0.0f);
					aboveBaseline = font.getBaseFont().getAscentPoint(((PNLTemplateLine) pnlLines.get(i)).getLineText(),
							font.getSize());
					underBaseline = font.getBaseFont()
							.getDescentPoint(((PNLTemplateLine) pnlLines.get(i)).getLineText(), font.getSize());
					height += aboveBaseline - underBaseline;

					break;

				}
				}

			}
		} catch (Exception e) {
			LogUtils.error("---- original File not found /or invalid for pnl generation----");
			e.printStackTrace();
		} finally {
			stamper.close();
			pdfReader.close();
			out.flush();
			out.close();
			out = null;
			System.gc();
		}
		return fsComposed;

	}
	/** Creates parent directories if necessary. Then returns file */
	private static File fileWithDirectoryAssurance(String directory, String filename) {
	    File dir = new File(directory);
	    if (!dir.exists()) dir.mkdirs();
	    return new File(directory + "/" + filename);
	}
}
