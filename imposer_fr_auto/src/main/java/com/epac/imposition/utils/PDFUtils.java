package com.epac.imposition.utils;

import java.io.FileOutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFUtils {
	
	private static float REGULAR_PAGE_PERCENT = 0.7f;
	
	public static void createSampleTextFile(float width, float height,float bleed,  int number, String file) {
		Document doc = null;
		PdfWriter writer = null;

		try {
			FontFactory.defaultEmbedding = true;

			BaseFont helvetica = BaseFont.createFont("Helvetica.otf", BaseFont.CP1252, BaseFont.EMBEDDED);
			Font font = new Font(helvetica, 120, Font.BOLD);
			


			width += 2*bleed;
			height+= 2*bleed;
			
			Rectangle box = new Rectangle(width, height);
			Rectangle size = new Rectangle(Format.mm2points(box.getLeft()).floatValue(),
					Format.mm2points(box.getBottom()).floatValue(), Format.mm2points(box.getRight()).floatValue(),
					Format.mm2points(box.getTop()).floatValue());
			doc = new Document(size);
			FileOutputStream stream = new FileOutputStream(file);
			writer = PdfWriter.getInstance(doc, stream);
			doc.open();

			for (int i = 1; i <= number; i++) {
				doc.newPage();
				writer.setPageEmpty(false);
				PdfContentByte cb = writer.getDirectContent();
				
				// set background for bleed
				Rectangle rect = new Rectangle(0,0, Format.mm2points(width).floatValue(), Format.mm2points(height).floatValue() );
				rect.setBackgroundColor(new CMYKColor(0, 0, 0, 0.15f));
				cb.rectangle(rect);
				
				// set background for big page
				rect = new Rectangle(Format.mm2points(bleed).floatValue(),Format.mm2points(bleed).floatValue(), Format.mm2points(width - bleed).floatValue(), Format.mm2points(height - bleed).floatValue() );
				rect.setBackgroundColor(new CMYKColor(0, 0, 0, 0.05f));
				cb.rectangle(rect);
				
			
				float sWidth = width * REGULAR_PAGE_PERCENT;
				float sHeight= height* REGULAR_PAGE_PERCENT;
				
				// set background small page 80% of big page
				
				float sX = bleed;
				float sY = bleed;
				if(i % 2 == 0)
					sX = width - sWidth;
	
				
				rect = new Rectangle(Format.mm2points(sX).floatValue(),Format.mm2points(sY).floatValue(), Format.mm2points(sWidth + sX - bleed).floatValue(), Format.mm2points(sHeight - bleed).floatValue() );
				rect.setBackgroundColor(new CMYKColor(0.66f, 0, 0.74f, 0));
				cb.rectangle(rect);				
				
				// write page number
				String text = String.valueOf(i);
				
				float w = helvetica.getWidthPoint(text, font.getCalculatedSize()) / 2;
				float h = (helvetica.getAscentPoint(text, font.getCalculatedSize()) -
						helvetica.getDescentPoint(text, font.getCalculatedSize())) /2;
				
				float y = sHeight / 2;
				float x = sWidth  / 2 + sX;
				
				ColumnText.showTextAligned(cb, Element.ALIGN_JUSTIFIED, new Phrase(text, font),
						Format.mm2points(x).floatValue() - w, Format.mm2points(y).floatValue() - h, 0.0f);
				
				// set page boxes
				
				PdfDictionary pdfDictionary = writer.getPageDictEntries();
				PdfArray bleedArray = new PdfArray();
				bleedArray.add(new PdfNumber(0));
				bleedArray.add(new PdfNumber(0));
				bleedArray.add(new PdfNumber(Format.mm2points(width).floatValue()));
				bleedArray.add(new PdfNumber(Format.mm2points(height).floatValue()));
				
				PdfArray trimArray = new PdfArray();
				trimArray.add(new PdfNumber(Format.mm2points(bleed).floatValue()));
				trimArray.add(new PdfNumber(Format.mm2points(bleed).floatValue()));
				trimArray.add(new PdfNumber(Format.mm2points(width - bleed).floatValue()));
				trimArray.add(new PdfNumber(Format.mm2points(height - bleed).floatValue()));
				
				pdfDictionary.put(PdfName.CROPBOX, bleedArray);
				pdfDictionary.put(PdfName.MEDIABOX, bleedArray);
				pdfDictionary.put(PdfName.TRIMBOX, trimArray);
				pdfDictionary.put(PdfName.BLEEDBOX, bleedArray);
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			doc.close();
			writer.close();

		}
	}
	
	public static void createSampleCoverFile(float width, float height,float bleed, float spine, String file) {
		Document doc = null;
		PdfWriter writer = null;

		try {
			FontFactory.defaultEmbedding = true;

			BaseFont helvetica = BaseFont.createFont("Helvetica.otf", BaseFont.CP1252, BaseFont.EMBEDDED);
			Font font = new Font(helvetica, 90, Font.BOLD);
			
			Rectangle box = new Rectangle(width*2+spine + 2*bleed, height+ 2*bleed);
			Rectangle size = new Rectangle(Format.mm2points(box.getLeft()).floatValue(),
					Format.mm2points(box.getBottom()).floatValue(), Format.mm2points(box.getRight()).floatValue(),
					Format.mm2points(box.getTop()).floatValue());
			doc = new Document(size);
			FileOutputStream stream = new FileOutputStream(file);
			writer = PdfWriter.getInstance(doc, stream);
			doc.open();
			doc.newPage();
			writer.setPageEmpty(false);
			PdfContentByte cb = writer.getDirectContent();
			
			// set background for bleed
			Rectangle rect = new Rectangle(size);
			rect.setBackgroundColor(new CMYKColor(0, 0, 0, 0.15f));
			cb.rectangle(rect);
			
			
			// set background for two big pages
			rect = new Rectangle(Format.mm2points(bleed).floatValue(),Format.mm2points(bleed).floatValue(), Format.mm2points(width+ bleed).floatValue(), Format.mm2points(height + bleed).floatValue() );
			rect.setBackgroundColor(new CMYKColor(0, 0, 0, 0.05f));
			cb.rectangle(rect);
			
			rect = new Rectangle(Format.mm2points(bleed + width+spine).floatValue(),Format.mm2points(bleed).floatValue(), size.getRight() -  Format.mm2points(bleed).floatValue(), size.getTop() -  Format.mm2points(bleed).floatValue() );
			rect.setBackgroundColor(new CMYKColor(0, 0, 0, 0.05f));
			cb.rectangle(rect);
			
			
			// set background for two small pages
			float sWidth = width * REGULAR_PAGE_PERCENT;
			float sHeight= height* REGULAR_PAGE_PERCENT;
			
			float sX1 = width - sWidth;
			float sX2 = bleed + width+spine;
			
			rect = new Rectangle(Format.mm2points(sX1).floatValue(),Format.mm2points(bleed).floatValue(), Format.mm2points(width+ bleed).floatValue(), Format.mm2points(sHeight + bleed).floatValue() );
			rect.setBackgroundColor(new CMYKColor(0.66f, 0, 0.74f, 0));
			cb.rectangle(rect);
			
			
			rect = new Rectangle(Format.mm2points(sX2).floatValue(),Format.mm2points(bleed).floatValue(), Format.mm2points(sX2+sWidth).floatValue(), Format.mm2points(sHeight + bleed).floatValue() );
			rect.setBackgroundColor(new CMYKColor(0.66f, 0, 0.74f, 0));
			cb.rectangle(rect);
			
			
			// Add text
			String text = "TEXT";
			float w = helvetica.getWidthPoint(text, font.getCalculatedSize()) / 2;
			float h = (helvetica.getAscentPoint(text, font.getCalculatedSize()) -
					helvetica.getDescentPoint(text, font.getCalculatedSize())) /2;
			
			float y = sHeight / 2;
			float x = sWidth  / 2 + sX1;
			ColumnText.showTextAligned(cb, Element.ALIGN_JUSTIFIED, new Phrase(text, font),
					Format.mm2points(x).floatValue() - w, Format.mm2points(y).floatValue() - h, 0.0f);
			
			x = sWidth  / 2 + sX2;
			ColumnText.showTextAligned(cb, Element.ALIGN_JUSTIFIED, new Phrase(text, font),
					Format.mm2points(x).floatValue() - w, Format.mm2points(y).floatValue() - h, 0.0f);
			
			
			// set boxes
			
			PdfDictionary pdfDictionary = writer.getPageDictEntries();
			PdfArray bleedArray = new PdfArray();
			bleedArray.add(new PdfNumber(0));
			bleedArray.add(new PdfNumber(0));
			bleedArray.add(new PdfNumber(size.getRight()));
			bleedArray.add(new PdfNumber(size.getTop()));
			
			PdfArray trimArray = new PdfArray();
			trimArray.add(new PdfNumber(Format.mm2points(bleed).floatValue()));
			trimArray.add(new PdfNumber(Format.mm2points(bleed).floatValue()));
			trimArray.add(new PdfNumber(size.getRight() -  Format.mm2points(bleed).floatValue()));
			trimArray.add(new PdfNumber(size.getTop() -  Format.mm2points(bleed).floatValue()));
			
			pdfDictionary.put(PdfName.CROPBOX, bleedArray);
			pdfDictionary.put(PdfName.MEDIABOX, bleedArray);
			pdfDictionary.put(PdfName.TRIMBOX, trimArray);
			pdfDictionary.put(PdfName.BLEEDBOX, bleedArray);
			
			

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			doc.close();
			writer.close();

		}
	}
	
	public static void main(String[] args) {
		float width = 108;
		float height = 177;
		float thickness= 26.88f;
		
		createSampleTextFile(width,height,3f, 16,  "/Users/elamine/9782264065964E1.text.pdf");
		createSampleCoverFile(width,height,3f, thickness,  "/Users/elamine/9782264065964E1.cover.pdf");
	}
}
