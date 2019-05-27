package com.epac.imposition.model;

import com.epac.om.api.book.Book;

public class ImpositionBook {

	private 	Book		book;
	
	private 	String 		docId;
	
	private 	float  		trimboxHeight;
	
	private 	float 		trimboxWidth;
	
	private 	float  		bookHeight;
	
	private 	float 		bookWidth;
	
	private 	int 		bookNbrPages;
	
	private 	int 		finalPageCount;
	
	private 	String 		filePath;

	private 	String 		outputFilePath;
	
	private 	String 		tempOutputFilePath;
	
	
	/**
	 * 
	 */
	public ImpositionBook() {
	}

	
	/**
	 * 
	 * @param itemId
	 * @param trimboxHeight
	 * @param trimboxWidth
	 * @param bookHeight
	 * @param bookWidth
	 * @param bookNbrPages
	 * @param filePath
	 */
	public ImpositionBook(String itemId, float trimboxHeight, float trimboxWidth, float bookHeight, float bookWidth,
			int bookNbrPages, String filePath) {
		super();
		this.docId = itemId;
		this.trimboxHeight = trimboxHeight;
		this.trimboxWidth = trimboxWidth;
		this.bookHeight = bookHeight;
		this.bookWidth = bookWidth;
		this.bookNbrPages = bookNbrPages;
		this.filePath = filePath;
	}
	
	
	public double getBookThickness (){
		if (book != null)
			return book.getMetadata().getThickness();
		
		return 0;
	}
	
	public String getTempOutputFilePath() {
		return tempOutputFilePath;
	}

	public void setTempOutputFilePath(String tempOutputFilePath) {
		this.tempOutputFilePath = tempOutputFilePath;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public int getFinalPageCount() {
		return finalPageCount;
	}

	public void setFinalPageCount(int finalPageCount) {
		this.finalPageCount = finalPageCount;
	}

	public String getDocId() {
		return docId;
	}

	public void setDocId(String itemId) {
		this.docId = itemId;
	}

	public float getTrimboxHeight() {
		return trimboxHeight;
	}

	public void setTrimboxHeight(float trimboxHeight) {
		this.trimboxHeight = trimboxHeight;
	}

	public float getTrimboxWidth() {
		return trimboxWidth;
	}

	public void setTrimboxWidth(float trimboxWidth) {
		this.trimboxWidth = trimboxWidth;
	}

	public float getBookHeight() {
		return bookHeight;
	}

	public void setBookHeight(float bookHeight) {
		this.bookHeight = bookHeight;
	}

	public float getBookWidth() {
		return bookWidth;
	}

	public void setBookWidth(float bookWidth) {
		this.bookWidth = bookWidth;
	}

	public int getBookNbrPages() {
		return bookNbrPages;
	}

	public void setBookNbrPages(int bookNbrPages) {
		this.bookNbrPages = bookNbrPages;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getOutputFilePath() {
		return outputFilePath;
	}

	public void setOutputFilePath(String outputFilePath) {
		this.outputFilePath = outputFilePath;
	}

	/**
	 * 
	 * @return
	 */
	public float getMetadataWidth (){
		if (this.book == null)
			return 0;
		
		return (float) this.book.getMetadata().getWidth();
	}
	
	/**
	 * 
	 * @return
	 */
	public float getMetadataHeight (){
		if (this.book == null)
			return 0;
		
		return (float) this.book.getMetadata().getHeight();
	}

	/**
	 * 
	 * @return
	 */
	public String getBarcodeValue (){
		if (this.book == null)
			return "";
		
		return this.book.getMetadata().getBarcode();
	}
	
	
	/**
	 * 
	 * @param barcode
	 * @return
	 */
	public static String generateBookId (String barcode){
		StringBuffer sb = new StringBuffer();
		if (barcode.isEmpty())
			return "";
		
		String sFirst = barcode.substring(0, 3);
		String bookid = sFirst.substring(0, sFirst.length() - 2);
		sb.append(bookid);
		
		if (bookid.length() < 14){
			for (int i = bookid.length(); i < 14; i++) {
				sb.append("0");
			}
		}
		
		sb.append("01");
		
		return sb.toString();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String generatePrintNbr (){
		StringBuffer sb = new StringBuffer();
		
		if (getBook() != null && getBook().getMetadata() != null){
			String printnbr = String.valueOf(getBook().getMetadata().getPrintNumber());
			for(int i=printnbr.length() ; i<4; i++) {
				sb.append('0');
			}
			sb.append(printnbr);
			
			return sb.toString();
		}
		
		return "0001";
	}
	
	/**
	 * 
	 * @param book
	 */
	public static ImpositionBook generateImpositionBook (Book book){
		if (book == null)
			return null;
		
		ImpositionBook impositionbook = new ImpositionBook();
		impositionbook.setBook(book);
		//impositionbook.setDocId(generateBookId(book.getMetadata().getBarcode()));		
		
		return impositionbook;
	}
	
	
}

