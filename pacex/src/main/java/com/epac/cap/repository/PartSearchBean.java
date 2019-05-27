package com.epac.cap.repository;

import com.epac.cap.common.AuditableSearchBean;

/**
 * Container for Part search criteria.
 * 
 * @author walid
 * 
 */
@SuppressWarnings("serial")
public class PartSearchBean extends AuditableSearchBean{
	private String partNum;
	private String partNumStart;
	private String partNumLike;
	private String isbn;
	private String isbnExact;
	private String title;
	private Integer version;
	private String author;
	private String filePath;
	private String fileName;
	private Boolean softDelete;
	private Boolean activeFlag;
	private Boolean nonNullIsbns;
	private String bindingTypeId;
	private Integer pagesCount;
	private Float thickness;
	private String categoryId;
	private String colors;
	private String paperType;
	private String lamination;
	private String notes;
	private String critiriaId;
	private Boolean hasNoParent;
	private String bestSheet;
	
	private Integer resultOffset;
	private boolean listing = false;
	
	/**
	 * Default constructor.  Sets the default ordering to fileName ascending 
	 *
	 **/
	public PartSearchBean(){
		setOrderBy("partNum", "desc");
	}
	
	/**   
	 * Accessor for the partNum property
	 *
	 * @return partNum
	 */
	public String getPartNum(){
		return this.partNum;
	}

	/**       
	 * Mutator for the partNum property
	 *
	 * @param inPartNum the new value for the partNum property
	 */	
	public void setPartNum(String inPartNum){
		this.partNum = inPartNum;
	}

	/**
	 * @return the partNumStart
	 */
	public String getPartNumStart() {
		return partNumStart;
	}

	/**
	 * @param partNumStart the partNumStart to set
	 */
	public void setPartNumStart(String partNumStart) {
		this.partNumStart = partNumStart;
	}

	/**
	 * @return the partNumLike
	 */
	public String getPartNumLike() {
		return partNumLike;
	}

	/**
	 * @param partNumLike the partNumLike to set
	 */
	public void setPartNumLike(String partNumLike) {
		this.partNumLike = partNumLike;
	}

	/**
	 * @return the nonNullIsbns
	 */
	public Boolean getNonNullIsbns() {
		return nonNullIsbns;
	}

	/**
	 * @param nonNullIsbns the nonNullIsbns to set
	 */
	public void setNonNullIsbns(Boolean nonNullIsbns) {
		this.nonNullIsbns = nonNullIsbns;
	}

	/**   
	 * Accessor for the isbn property
	 *
	 * @return isbn
	 */
	public String getIsbn(){
		return this.isbn;
	}

	/**       
	 * Mutator for the isbn property
	 *
	 * @param inIsbn the new value for the isbn property
	 */	
	public void setIsbn(String inIsbn){
		this.isbn = inIsbn;
	}

	/**
	 * @return the isbnExact
	 */
	public String getIsbnExact() {
		return isbnExact;
	}

	/**
	 * @param isbnExact the isbnExact to set
	 */
	public void setIsbnExact(String isbnExact) {
		this.isbnExact = isbnExact;
	}

	/**   
	 * Accessor for the title property
	 *
	 * @return title
	 */
	public String getTitle(){
		return this.title;
	}

	/**       
	 * Mutator for the title property
	 *
	 * @param inTitle the new value for the title property
	 */	
	public void setTitle(String inTitle){
		this.title = inTitle;
	}

	/**
	 * @return the version
	 */
	public Integer getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**   
	 * Accessor for the filePath property
	 *
	 * @return filePath
	 */
	public String getFilePath(){
		return this.filePath;
	}

	/**       
	 * Mutator for the filePath property
	 *
	 * @param inFilePath the new value for the filePath property
	 */	
	public void setFilePath(String inFilePath){
		this.filePath = inFilePath;
	}

	/**   
	 * Accessor for the fileName property
	 *
	 * @return fileName
	 */
	public String getFileName(){
		return this.fileName;
	}

	/**       
	 * Mutator for the fileName property
	 *
	 * @param inFileName the new value for the fileName property
	 */	
	public void setFileName(String inFileName){
		this.fileName = inFileName;
	}

	/**   
	 * Accessor for the softDelete property
	 *
	 * @return softDelete
	 */
	public Boolean getSoftDelete(){
		return this.softDelete;
	}

	/**       
	 * Mutator for the softDelete property
	 *
	 * @param inSoftDelete the new value for the softDelete property
	 */	
	public void setSoftDelete(Boolean inSoftDelete){
		this.softDelete = inSoftDelete;
	}

	/**   
	 * Accessor for the bindingTypeId property
	 *
	 * @return bindingTypeId
	 */
	public String getBindingTypeId(){
		return this.bindingTypeId;
	}

	/**       
	 * Mutator for the bindingTypeId property
	 *
	 * @param inBindingTypeId the new value for the bindingTypeId property
	 */	
	public void setBindingTypeId(String inBindingTypeId){
		this.bindingTypeId = inBindingTypeId;
	}

	/**   
	 * Accessor for the pagesCount property
	 *
	 * @return pagesCount
	 */
	public Integer getPagesCount(){
		return this.pagesCount;
	}

	/**       
	 * Mutator for the pagesCount property
	 *
	 * @param inPagesCount the new value for the pagesCount property
	 */	
	public void setPagesCount(Integer inPagesCount){
		this.pagesCount = inPagesCount;
	}

	/**   
	 * Accessor for the thickness property
	 *
	 * @return thickness
	 */
	public Float getThickness(){
		return this.thickness;
	}

	/**       
	 * Mutator for the thickness property
	 *
	 * @param inThickness the new value for the thickness property
	 */	
	public void setThickness(Float inThickness){
		this.thickness = inThickness;
	}

	/**   
	 * Accessor for the categoryId property
	 *
	 * @return categoryId
	 */
	public String getCategoryId(){
		return this.categoryId;
	}

	/**       
	 * Mutator for the categoryId property
	 *
	 * @param inCategoryId the new value for the categoryId property
	 */	
	public void setCategoryId(String inCategoryId){
		this.categoryId = inCategoryId;
	}

	/**   
	 * Accessor for the colors property
	 *
	 * @return colors
	 */
	public String getColors(){
		return this.colors;
	}

	/**       
	 * Mutator for the colors property
	 *
	 * @param inColors the new value for the colors property
	 */	
	public void setColors(String inColors){
		this.colors = inColors;
	}

	/**   
	 * Accessor for the paperType property
	 *
	 * @return paperType
	 */
	public String getPaperType(){
		return this.paperType;
	}

	/**       
	 * Mutator for the paperType property
	 *
	 * @param inPaperType the new value for the paperType property
	 */	
	public void setPaperType(String inPaperType){
		this.paperType = inPaperType;
	}

	/**   
	 * Accessor for the lamination property
	 *
	 * @return lamination
	 */
	public String getLamination(){
		return this.lamination;
	}

	/**       
	 * Mutator for the lamination property
	 *
	 * @param inLamination the new value for the lamination property
	 */	
	public void setLamination(String inLamination){
		this.lamination = inLamination;
	}

	/**   
	 * Accessor for the notes property
	 *
	 * @return notes
	 */
	public String getNotes(){
		return this.notes;
	}

	/**       
	 * Mutator for the notes property
	 *
	 * @param inNotes the new value for the notes property
	 */	
	public void setNotes(String inNotes){
		this.notes = inNotes;
	}

	/**
	 * @return the activeFlag
	 */
	public Boolean getActiveFlag() {
		return activeFlag;
	}

	/**
	 * @param activeFlag the activeFlag to set
	 */
	public void setActiveFlag(Boolean activeFlag) {
		this.activeFlag = activeFlag;
	}

	/**
	 * @return the critiriaId
	 */
	public String getCritiriaId() {
		return critiriaId;
	}

	/**
	 * @param critiriaId the critiriaId to set
	 */
	public void setCritiriaId(String critiriaId) {
		this.critiriaId = critiriaId;
	}

	/**
	 * @return the hasNoParent
	 */
	public Boolean getHasNoParent() {
		return hasNoParent;
	}

	/**
	 * @param hasNoParent the hasNoParent to set
	 */
	public void setHasNoParent(Boolean hasNoParent) {
		this.hasNoParent = hasNoParent;
	}

	public Integer getResultOffset() {
		return resultOffset;
	}

	public void setResultOffset(Integer resultOffset) {
		this.resultOffset = resultOffset;
	}

	public boolean isListing() {
		return listing;
	}

	public void setListing(boolean listing) {
		this.listing = listing;
	}

	/**
	 * @return the bestSheet
	 */
	public String getBestSheet() {
		return bestSheet;
	}

	/**
	 * @param bestSheet the bestSheet to set
	 */
	public void setBestSheet(String bestSheet) {
		this.bestSheet = bestSheet;
	}

	
}