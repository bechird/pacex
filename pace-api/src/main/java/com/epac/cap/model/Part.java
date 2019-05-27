package com.epac.cap.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SortComparator;

import com.epac.cap.common.AuditableBean;
import com.epac.cap.common.WFSDataSupportComparableDeserializer;
import com.epac.cap.handler.SubPartsByIdDescendingOrderingComparator;
import com.epac.cap.handler.WFSDataSupportsByIdOrderingComparator;
import com.epac.cap.model.WFSStatus.ProgressStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Part class representing a part in the system that needs to be produced
 */
@Entity
@Table(name = "part")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="com.epac.cap")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class Part extends AuditableBean implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4790877288020874107L;
	private String partNum;
	private String isbn;
	private String title;
	private Integer version;
	private String author;
	private Date publishDate;
	private Date lastPrinted;
	
	private String filePath;
	private String fileName;
	private String displayFileName;
	private Integer dataSupportId;
	
	private Boolean softDelete;
	private Boolean activeFlag;
	private BindingType bindingType;
	private Integer pagesCount;
	private Float thickness;
	private PartCategory category;
	private String colors = new String("");
	private String coverColor = new String("");
	private Float width;
	private Float length;
	private String size;
	private String spineType;
	private String headTailBands;
	private String wireColor;
	
	private PaperType paperType;
	private Lamination lamination;
	private String notes = new String("");
	private Boolean readyToProduce;
	private String publisher;
	private Set<WFSPartWorkflow> workflows = new HashSet<WFSPartWorkflow>();
	private WFSPartWorkflow prodWorkflow;
	
	@JsonDeserialize(using = WFSDataSupportComparableDeserializer.class)
	private SortedSet<WFSDataSupport> dataSupports = new TreeSet<WFSDataSupport>(new WFSDataSupportsByIdOrderingComparator());
	
	@JsonDeserialize(using = WFSDataSupportComparableDeserializer.class)
	private SortedSet<WFSDataSupport> dataSupportsOnProd = new TreeSet<WFSDataSupport>(new WFSDataSupportsByIdOrderingComparator());
	
	@JsonIgnore
	private Set<PartCritiria> partCritirias = new HashSet<PartCritiria>(0);

	private Set<String> critirias = new HashSet<String>(0);
	

	private SortedSet<SubPart> subParts = new TreeSet<SubPart>(new SubPartsByIdDescendingOrderingComparator());
	private Set<String> children = new HashSet<String>(0);
	
	private Set<SubPart> topParts = new HashSet<SubPart>(0);
	
	private Set<Preference> pnlPreferences = new HashSet<Preference>();
	private String pnlNotNeeded;
	private String pnlTemplateId;
	private String pnlLocation;
	private String pnlPageNumber;
	private String pnlPrintingNumber;
	private String pnlHmargin;
	private String pnlVmargin;
	private String pnlFontType;
	private String pnlFontSize;
	
	private Boolean selfcover;
	private Boolean dustJacket;
	private Boolean printedEndSheet;
	private String djColor;
	private String djPaper;
	private String esColor;
	private String esPaper;
	
	private Boolean spotVarnish;
	
	private String bestSheet;
	private Float bestSheetWaste;
	
	/**
	 * Default constructor
	 */
	public Part() {
	}

	/**
	 * Constructor which sets the identity property
	 */
	public Part(String partNum, String isbn) {
		this.partNum = partNum;
		this.isbn = isbn;
	}

	/**
	 * Constructor which sets all of the properties
	 */
	public Part(String partNum, String isbn, String title, String filePath, String fileName, Boolean softDelete,
			Integer pagesCount, Float thickness, String colors,
			Float width, Float length, String notes, Set<Job> jobs, Set<OrderPart> orderParts,
			Set<PartCritiria> partCritirias) {
		this.partNum = partNum;
		this.isbn = isbn;
		this.title = title;
		this.filePath = filePath;
		//this.fileName = fileName;
		this.softDelete = softDelete;
		this.pagesCount = pagesCount;
		this.thickness = thickness;
		this.colors = colors;
		this.width = width;
		this.length = length;
		this.notes = notes;
		//this.jobs = jobs;
		//this.orderParts = orderParts;
		this.partCritirias = partCritirias;
	}

	/**
	 * Accessor methods for partNum
	 *
	 * @return partNum  
	 */
	@Id
	@Column(name = "Part_Num", unique = true, nullable = false, length = 25)
	public String getPartNum() {
		return this.partNum;
	}

	/**
	 * @param partNum the partNum to set
	 */
	public void setPartNum(String partNum) {
		this.partNum = partNum;
	}
	
	public enum BindingTypes { 
		DEFAULT("DEFAULT"),
		LOOSELEAF("LOOSELEAF"),
		CARDSS("CARDSS"),
		CASEBOUND("CASEBOUND");

		private String name;
		private BindingTypes(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}
	
	public enum PartsCategory { 
		BOOK("BOOK"),
		TEXT("TEXT"),
		COVER("COVER"),
		DUSTJACKET("DUSTJACKET"),
		ENDSHEET("ENDSHEET");

		private String name;
		private PartsCategory(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}
	
	public enum PartColors { 
		_4C("4C"),
		_1C("1C"),
		_ALL("All");

		private String name;
		private PartColors(String name) {
			this.name = name;
		}
		public String getName() { return name; }
	}
	
	/**
	 * @return the subParts
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "id.topPartNum", cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE}, orphanRemoval = true)
	@SortComparator(SubPartsByIdDescendingOrderingComparator.class)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="com.epac.cap")
	public SortedSet<SubPart> getSubParts() {
		return subParts;
	}

	/**
	 * @param subParts the subParts to set
	 */
	public void setSubParts(SortedSet<SubPart> subParts) {
		this.subParts = subParts;
	}
	
	/**
	 * @return the children parts
	 */
	@Transient
	public Set<String> getChildren(){
		for(SubPart sb : this.getSubParts()){
			children.add(sb.getId().getSubPartNum());
		}
		return children;
	}
	
	/**
	 * @return the sub part bean by id
	 */
	@Transient
	public SubPart getSubPartById(String subPartNum){
		SubPart result = null;
		for(SubPart sb : this.getSubParts()){
			if(subPartNum.equals(sb.getId().getSubPartNum())){
				result = sb;
				break;
			}
		}
		return result;
	}
	
	/**
	 * @return the topParts
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "id.subPartNum")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="com.epac.cap")
	public Set<SubPart> getTopParts() {
		return topParts;
	}

	/**
	 * @param topParts the topParts to set
	 */
	public void setTopParts(Set<SubPart> topParts) {
		this.topParts = topParts;
	}
	
	/**
	 * Accessor methods for isbn
	 *
	 * @return isbn  
	 */

	@Column(name = "ISBN", nullable = false, length = 25)
	public String getIsbn() {
		return this.isbn;
	}

	/**
	 * @param isbn the isbn to set
	 */
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	/**
	 * Accessor methods for title
	 *
	 * @return title  
	 */

	@Column(name = "Title", length = 255)
	public String getTitle() {
		return this.title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the version
	 */
	@Column(name = "Version")
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
	@Column(name = "Author", length = 55)
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
	 * @return the publishDate
	 */
	@Temporal(TemporalType.DATE)
	@Column(name = "Publish_Date")
	public Date getPublishDate() {
		return publishDate;
	}

	/**
	 * @param publishDate the publishDate to set
	 */
	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}

	/**
	 * @return the lastPrinted
	 */
	@Temporal(TemporalType.DATE)
	@Column(name = "Last_Printed")
	public Date getLastPrinted() {
		return lastPrinted;
	}

	/**
	 * @param lastPrinted the lastPrinted to set
	 */
	public void setLastPrinted(Date lastPrinted) {
		this.lastPrinted = lastPrinted;
	}

	/**
	 * Accessor methods for filePath
	 *
	 * @return filePath  
	 

	@Column(name = "File_Path", length = 500)
	public String getFilePath() {
		return this.filePath;
	}*/
	
	/**
	 * Accessor methods for filePath
	 *
	 * @return filePath  
	 */

	@Transient
	public String getFilePath() {
		WFSDataSupport ds = this.getDataSupportOnProdByName("Download");
		if(ds != null){
			WFSLocation l = ds.getLocationdByType("Destination");
			if(l != null){
				this.filePath = l.getPath();
			}
		}
		return this.filePath;
	}

	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Accessor methods for fileName
	 *
	 * @return fileName  
	 
	@Column(name = "File_Name", length = 100)
	public String getFileName() {
		return this.fileName;
	}*/
	
	/**
	 * Accessor methods for fileName
	 *
	 * @return displayFileName  
	 */
	@Transient
	public String getDisplayFileName() {
		/*String fp = this.getFilePath();
		if(fp != null && ! fp.isEmpty()){
			String fn = FilenameUtils.removeExtension(fp);
			this.fileName = fn.substring(0, fn.lastIndexOf("_")) + ".pdf";
		}*/
		WFSDataSupport ds = this.getDataSupportOnProdByName("Download");
		if(ds != null){
			WFSLocation l = ds.getLocationdByType("Destination");
			if(l != null){
				this.displayFileName = l.getFileName();
			}
		}
		return this.displayFileName;
	}
	
	/**
	 * @param displayFileName the displayFileName to set
	 */
	public void setDisplayFileName(String fileName) {
		this.displayFileName = fileName;
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the dataSupportId
	 */
	@Transient
	public Integer getDataSupportId() {
		WFSDataSupport ds = this.getDataSupportOnProdByName("Download");
		if(ds != null){
			WFSLocation l = ds.getLocationdByType("Destination");
			if(l != null){
				this.dataSupportId = l.getDataSupportId();
			}
		}
		return this.dataSupportId;
	}

	/**
	 * @param dataSupportId the dataSupportId to set
	 */
	public void setDataSupportId(Integer dataSupportId) {
		this.dataSupportId = dataSupportId;
	}

	/**
	 * Accessor methods for softDelete
	 *
	 * @return softDelete  
	 */

	@Column(name = "Soft_Delete")
	public Boolean getSoftDelete() {
		return this.softDelete;
	}

	/**
	 * @param softDelete the softDelete to set
	 */
	public void setSoftDelete(Boolean softDelete) {
		this.softDelete = softDelete;
	}
	
	/**
	 * Accessor methods for activeFlag
	 *
	 * @return activeFlag  
	 */

	@Column(name = "Active_Flag")
	public Boolean getActiveFlag() {
		return this.activeFlag;
	}

	/**
	 * @param activeFlag the activeFlag to set
	 */
	public void setActiveFlag(Boolean activeFlag) {
		this.activeFlag = activeFlag;
	}

	/**
	 * Accessor methods for bindingType
	 *
	 * @return bindingType
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Binding_Type_Id")
	public BindingType getBindingType() {
		return this.bindingType;
	}

	/**
	 * @param bindingType the bindingType to set
	 */
	public void setBindingType(BindingType bindingType) {
		this.bindingType = bindingType;
	}

	/**
	 * Accessor methods for pagesCount
	 *
	 * @return pagesCount  
	 */

	@Column(name = "Pages_Count")
	public Integer getPagesCount() {
		return this.pagesCount;
	}

	/**
	 * @param pagesCount the pagesCount to set
	 */
	public void setPagesCount(Integer pagesCount) {
		this.pagesCount = pagesCount;
	}

	/**
	 * Accessor methods for thickness
	 *
	 * @return thickness  
	 */

	@Column(name = "Thickness", length = 25)
	public Float getThickness() {
		return this.thickness;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	/**
	 * @param thickness the thickness to set
	 */
	public void setThickness(Float thickness) {
		this.thickness = thickness;
	}

	/**
	 * Accessor methods for category
	 *
	 * @return categoryI 
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Category_Id")
	public PartCategory getCategory() {
		return this.category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(PartCategory category) {
		this.category = category;
	}

	/**
	 * Accessor methods for colors
	 *
	 * @return colors  
	 */

	@Column(name = "Colors", nullable = false, length = 15)
	public String getColors() {
		return this.colors;
	}

	/**
	 * @param colors the colors to set
	 */
	public void setColors(String colors) {
		this.colors = colors;
	}

	/**
	 * @return the coverColor
	 */
	@Column(name = "CoverColor", nullable = false, length = 15)
	public String getCoverColor() {
		return coverColor;
	}

	/**
	 * @param coverColor the coverColor to set
	 */
	public void setCoverColor(String coverColor) {
		this.coverColor = coverColor;
	}

	/**
	 * Accessor methods for width
	 *
	 * @return width  
	 */

	@Column(name = "Width", precision = 12, scale = 5)
	public Float getWidth() {
		return this.width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(Float width) {
		this.width = width;
	}

	/**
	 * Accessor methods for length
	 *
	 * @return length  
	 */

	@Column(name = "Length", precision = 12, scale = 5)
	public Float getLength() {
		return this.length;
	}

	/**
	 * @param length the length to set
	 */
	public void setLength(Float length) {
		this.length = length;
	}

	/**
	 * @return the size
	 */
	@Transient
	public String getSize() {
		this.size = Float.toString(this.width == null ? 0: this.width) + " x " +  
				Float.toString(this.length == null ? 0: this.length);
		return this.size;
	}

	/**
	 * Accessor methods for paperType
	 *
	 * @return paperType  
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Paper_Type")
	public PaperType getPaperType() {
		return this.paperType;
	}

	/**
	 * @param paperType the paperType to set
	 */
	public void setPaperType(PaperType paperType) {
		this.paperType = paperType;
	}

	/**
	 * Accessor methods for lamination
	 *
	 * @return lamination  
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Lamination")
	public Lamination getLamination() {
		return this.lamination;
	}

	/**
	 * @param lamination the lamination to set
	 */
	public void setLamination(Lamination lamination) {
		this.lamination = lamination;
	}

	/**
	 * Accessor methods for notes
	 *
	 * @return notes  
	 */

	@Column(name = "Notes", length = 200)
	public String getNotes() {
		return this.notes;
	}

	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	@Column(name = "ReadyToProduce")
	public Boolean getReadyToProduce() {
		return readyToProduce;
	}

	public void setReadyToProduce(Boolean readyToProduce) {
		this.readyToProduce = readyToProduce;
	}

	public void setSize(String size) {
		this.size = size;
	}
	
	/**
	 * @return the dataSupports
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "partNumb", cascade ={ CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH}, orphanRemoval = true)
	@SortComparator(WFSDataSupportsByIdOrderingComparator.class)
	public SortedSet<WFSDataSupport> getDataSupports() {
		return dataSupports;
	}

	/**
	 * @param dataSupports the dataSupports to set
	 */
	public void setDataSupports(SortedSet<WFSDataSupport> dataSupports) {
		this.dataSupports = dataSupports;
	}
	
	public void addDataSupports(WFSDataSupport dataSupport) {
		if(this.dataSupports == null)
			this.dataSupports = new TreeSet<>(new WFSDataSupportsByIdOrderingComparator());
		
		this.dataSupports.add(dataSupport);
		
		if(dataSupport.getProductionStatus().getId().equals(WFSProductionStatus.statuses.ONPROD.getName())) {
			this.dataSupportsOnProd.add(dataSupport);
		}
	}
	
	/**
	 * @return the dataSupports on prod

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "partNumb", orphanRemoval=true)
	@Where(clause = "productionstatusid = '1'")
	@SortComparator(WFSDataSupportsByIdOrderingComparator.class)*/
	@Transient
	public SortedSet<WFSDataSupport> getDataSupportsOnProd() {
		for(WFSDataSupport wds : this.getDataSupports()){
			if(WFSProductionStatus.statuses.ONPROD.getName().equals(wds.getProductionStatus().getId())){
				dataSupportsOnProd.add(wds);
			}
		}
		return dataSupportsOnProd;
	}

	/**
	 * @param dataSupports the dataSupports to set
	 */
	public void setDataSupportsOnProd(SortedSet<WFSDataSupport> dataSupports) {
		this.dataSupportsOnProd = dataSupports;
	}
	
	/**
	 * @return the workflows
	 */
	@OneToMany(fetch = FetchType.EAGER, cascade ={ CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH}, mappedBy = "partNum", orphanRemoval = true)
	public Set<WFSPartWorkflow> getWorkflows() {
		return workflows;
	}

	/**
	 * @param workflows the workflows to set
	 */
	public void setWorkflows(Set<WFSPartWorkflow> workflows) {
		this.workflows = workflows;
	}
	
	public void addWorkflow(WFSPartWorkflow workflow) {
		if(workflows == null)
			workflows = new HashSet<WFSPartWorkflow>();
		workflows.add(workflow);
	}

	/**
	 * @return the prodWorkflow
	 
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "partNum")
	@Where(clause = "wf_status = 'ONPROD'")*/
	@Transient
	public WFSPartWorkflow getProdWorkflow() {
		return prodWorkflow;
	}

	/**
	 * @param prodWorkflow the prodWorkflow to set
	 */
	public void setProdWorkflow(WFSPartWorkflow prodWorkflow) {
		this.prodWorkflow = prodWorkflow;
	}

	/**
	 * Accessor methods for partCritirias
	 *
	 * @return partCritirias  
	 */

	@JsonIgnore
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "id.partNum", cascade = CascadeType.ALL, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="com.epac.cap")
	public Set<PartCritiria> getPartCritirias() {
		return this.partCritirias;
	}

	/**
	 * @param partCritirias the partCritirias to set
	 */
	public void setPartCritirias(Set<PartCritiria> partCritirias) {
		this.partCritirias = partCritirias;
	}

	/**
	 * @return the spineType
	 */
	@Column(name = "spineType", length = 25)
	public String getSpineType() {
		return spineType;
	}

	/**
	 * @param spineType the spineType to set
	 */
	public void setSpineType(String spineType) {
		this.spineType = spineType;
	}

	/**
	 * @return the headTailBands
	 */
	@Column(name = "headTailBands", length = 35)
	public String getHeadTailBands() {
		return headTailBands;
	}

	/**
	 * @param headTailBands the headTailBands to set
	 */
	public void setHeadTailBands(String headTailBands) {
		this.headTailBands = headTailBands;
	}

	@Column(name = "wireColor", length = 35)
	public String getWireColor() {
		return wireColor;
	}

	public void setWireColor(String wireColor) {
		this.wireColor = wireColor;
	}

	/**
	  * Accessor methods for critirias
	  */ 
	  @Transient
	  public Set<String> getCritirias(){
		  //this.critirias = new HashSet<String>();
		  for(PartCritiria pc : this.getPartCritirias()){
			  this.critirias.add(pc.getId().getCritiriaId());
		  }
		  return this.critirias;
	  }
	  
	  public void setCritirias(Set<String> critirias){
		  this.critirias = critirias;
	  }
	
   /**
	 * @return the dustJacket
	 */
	@Transient
	public Boolean getDustJacket() {
		dustJacket = false;
		for(String s : getCritirias()){
			if(s.equals("DUSTJACKET")){
				dustJacket = true;
				break;
			}
		}
		return dustJacket;
	}

	/**
	 * @param dustJacket the dustJacket to set
	 */
	public void setDustJacket(Boolean dustJacket) {
		this.dustJacket = dustJacket;
	}
	
	/**
	 * @return the selfcover
	 */
	@Transient
	public Boolean getSelfcover() {
		selfcover = false;
		for(String s : getCritirias()){
			if(s.equals("SELFCOVER")){
				selfcover = true;
				break;
			}
		}
		return selfcover;
	}

	/**
	 * @param selfcover the selfcover to set
	 */
	public void setSelfcover(Boolean selfcover) {
		this.selfcover = selfcover;
	}
	
	/**
	 * @return the endSheet
	 */
	@Transient
	public Boolean getPrintedEndSheet() {
		printedEndSheet = false;
		for(String s : getCritirias()){
			if(s.equals("PRINTEDENDSHEET")){
				printedEndSheet = true;
				break;
			}
		}
		return printedEndSheet;
	}
	
	/**
	 * @param printedEndSheet the printedEndSheet to set
	 */
	public void setPrintedEndSheet(Boolean printedEndSheet) {
		this.printedEndSheet = printedEndSheet;
	}

	/**
	 * @return the spotVarnish
	 */
	@Transient
	public Boolean getSpotVarnish() {
		spotVarnish = false;
		for(String s : getCritirias()){
			if(s.equals("SPOTVARNISH")){
				spotVarnish = true;
				break;
			}
		}
		return spotVarnish;
	}

	/**
	 * @param spotVarnish the spotVarnish to set
	 */
	public void setSpotVarnish(Boolean spotVarnish) {
		this.spotVarnish = spotVarnish;
	}

	@Transient
	  public Set<String> getCritiriasOrigin(){
		  return this.critirias;
	  }
	
	public void setCritiriasOrigin(Set<String> cr){
		  this.critirias = cr;
	  }
	  
	  /**
	  * Accessor methods for critirias string
	  */ 
	  @Transient
	  @JsonIgnore
	  public String getCritiriasString(){
		  StringBuilder result = new StringBuilder();
		  for(String ur : this.getCritirias()){
			  if(result.length() > 0){
				  result.append(", ");
			  }
			  result.append(ur);
		  }
		  return result.toString();
	  }
	  
	/**
	 * 
	 */
	@Transient
	public Boolean partCritiriaFor(String critiriaId){
		Boolean result = false;
		if(critiriaId != null){
			for(PartCritiria pc : getPartCritirias()){
				if(critiriaId.equals(pc.getId().getCritiriaId())){
					result = true;
				}
			}
		}
		return result;
	}
	
	/**
	 * returns the ds that is on prod for that specific type 
	 */
	@Transient
	@JsonIgnore
	public WFSDataSupport getDataSupportOnProdByName(String name){
		WFSDataSupport result = null;
		if(!this.getDataSupportsOnProd().isEmpty()){
			for(WFSDataSupport ds : this.getDataSupportsOnProd()){
				if(ds.getName().equals(name)){
					result = ds;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * returns the wf that is on prod
	 */
	@Transient
	public WFSPartWorkflow getPartWorkFlowOnProd(){
		WFSPartWorkflow result = null;
		if(!this.getWorkflows().isEmpty()){
			for(WFSPartWorkflow ds : this.getWorkflows()){
				if(WFSProductionStatus.statuses.ONPROD.getName().equals(ds.getWfStatus())){
					result = ds;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * returns if the wf that is on prod ready or not
	 */
	@Transient
	public Boolean isPartWorkFlowOnProdReady(){
		WFSPartWorkflow wfp = getPartWorkFlowOnProd();
		Boolean result = true;
		if(wfp != null){
			for(WFSProgress pIter : wfp.getProgresses()){
				if(!ProgressStatus.DONE.getName().equals(pIter.getStatus())){
					result = false;
					break;
				}
			}
		}else{
			result = false;
		}
		return result;
	}

	/**
	 * @return the pnlPreferences
	 */
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "partNum", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<Preference> getPnlPreferences() {
		return pnlPreferences;
	}

	/**
	 * @param pnlPreferences the pnlPreferences to set
	 */
	public void setPnlPreferences(Set<Preference> pnlPreferences) {
		this.pnlPreferences = pnlPreferences;
	}
	
	/**
	 * Returns the PNL preference related to the specified id
	 */
	@Transient
	public Preference getPnlPreferenceById(String pnlId){
		Preference result = null;
		if(pnlId != null && !pnlId.isEmpty()){
			for(Preference p : this.getPnlPreferences()){
				if(p.getId().startsWith(pnlId)){
					result = p;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * @return the pnlTemplateId
	 */
	@Transient
	public String getPnlTemplateId() {
		return pnlTemplateId;
	}

	/**
	 * @param pnlTemplateId the pnlTemplateId to set
	 */
	public void setPnlTemplateId(String pnlTemplateId) {
		this.pnlTemplateId = pnlTemplateId;
	}

	/**
	 * @return the pnlPageNumber
	 */
	@Transient
	public String getPnlPageNumber() {
		return pnlPageNumber;
	}

	/**
	 * @param pnlPageNumber the pnlPageNumber to set
	 */
	public void setPnlPageNumber(String pnlPageNumber) {
		this.pnlPageNumber = pnlPageNumber;
	}

	/**
	 * @return the pnlPrintingNumber
	 */
	@Transient
	public String getPnlPrintingNumber() {
		return pnlPrintingNumber;
	}

	/**
	 * @param pnlPrintingNumber the pnlPrintingNumber to set
	 */
	public void setPnlPrintingNumber(String pnlPrintingNumber) {
		this.pnlPrintingNumber = pnlPrintingNumber;
	}

	/**
	 * @return the pnlNotNeeded
	 */
	@Transient
	public String getPnlNotNeeded() {
		return pnlNotNeeded;
	}

	/**
	 * @param pnlNotNeeded the pnlNotNeeded to set
	 */
	public void setPnlNotNeeded(String pnlNotNeeded) {
		this.pnlNotNeeded = pnlNotNeeded;
	}

	/**
	 * @return the pnlLocation
	 */
	@Transient
	public String getPnlLocation() {
		return pnlLocation;
	}

	/**
	 * @param pnlLocation the pnlLocation to set
	 */
	public void setPnlLocation(String pnlLocation) {
		this.pnlLocation = pnlLocation;
	}

	/**
	 * @return the pnlHmargin
	 */
	@Transient
	public String getPnlHmargin() {
		return pnlHmargin;
	}

	/**
	 * @param pnlHmargin the pnlHmargin to set
	 */
	public void setPnlHmargin(String pnlHmargin) {
		this.pnlHmargin = pnlHmargin;
	}

	/**
	 * @return the pnlVmargin
	 */
	@Transient
	public String getPnlVmargin() {
		return pnlVmargin;
	}

	/**
	 * @param pnlVmargin the pnlVmargin to set
	 */
	public void setPnlVmargin(String pnlVmargin) {
		this.pnlVmargin = pnlVmargin;
	}

	/**
	 * @return the pnlFontType
	 */
	@Transient
	public String getPnlFontType() {
		return pnlFontType;
	}

	/**
	 * @param pnlFontType the pnlFontType to set
	 */
	public void setPnlFontType(String pnlFontType) {
		this.pnlFontType = pnlFontType;
	}

	/**
	 * @return the pnlFontSize
	 */
	@Transient
	public String getPnlFontSize() {
		return pnlFontSize;
	}

	/**
	 * @param pnlFontSize the pnlFontSize to set
	 */
	public void setPnlFontSize(String pnlFontSize) {
		this.pnlFontSize = pnlFontSize;
	}

	/**
	 * @return the djColor
	 */
	@Column(name = "djColor", length = 35)
	public String getDjColor() {
		return djColor;
	}

	/**
	 * @param djColor the djColor to set
	 */
	public void setDjColor(String djColor) {
		this.djColor = djColor;
	}

	/**
	 * @return the djPaper
	 */
	@Column(name = "djPaper", length = 55)
	public String getDjPaper() {
		return djPaper;
	}

	/**
	 * @param djPaper the djPaper to set
	 */
	public void setDjPaper(String djPaper) {
		this.djPaper = djPaper;
	}

	/**
	 * @return the esColor
	 */
	@Column(name = "esColor", length = 35)
	public String getEsColor() {
		return esColor;
	}

	/**
	 * @param esColor the esColor to set
	 */
	public void setEsColor(String esColor) {
		this.esColor = esColor;
	}

	/**
	 * @return the esPaper
	 */
	@Column(name = "esPaper", length = 55)
	public String getEsPaper() {
		return esPaper;
	}

	/**
	 * @param esPaper the esPaper to set
	 */
	public void setEsPaper(String esPaper) {
		this.esPaper = esPaper;
	}

	/**
	 * @return the bestSheet
	 */
	@Column(name = "bestSeet", length = 15)
	public String getBestSheet() {
		return bestSheet;
	}

	/**
	 * @param bestSheet the bestSheet to set
	 */
	public void setBestSheet(String bestSheet) {
		this.bestSheet = bestSheet;
	}

	/**
	 * @return the bestSheetWaste
	 */
	@Column(name = "bestSeetWaste", precision = 12, scale = 5)
	public Float getBestSheetWaste() {
		return bestSheetWaste;
	}

	/**
	 * @param bestSheetWaste the bestSheetWaste to set
	 */
	public void setBestSheetWaste(Float bestSheetWaste) {
		this.bestSheetWaste = bestSheetWaste;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof Part)) {
			return false;
		}
		Part other = (Part) obj;
		if (getPartNum() == null) {
			if (other.getPartNum() != null) {
				return false;
			}
		} else if (!getPartNum().equals(other.getPartNum())) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getPartNum() == null) ? 0 : getPartNum().hashCode());
		/*result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + ((bindingType == null) ? 0 : bindingType.hashCode());
		result = prime * result + ((critirias == null) ? 0 : critirias.hashCode());
		result = prime * result + ((isbn == null) ? 0 : isbn.hashCode());
		result = prime * result + ((lamination == null) ? 0 : lamination.hashCode());
		result = prime * result + ((pagesCount == null) ? 0 : pagesCount.hashCode());
		result = prime * result + ((paperType == null) ? 0 : paperType.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + Float.FloatToIntBits(width);*/
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equalsSpecs(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Part)) {
			return false;
		}
		Part other = (Part) obj;
		
		if (author == null) {
			if (other.author != null) {
				return false;
			}
		} else if (!author.equals(other.author)) {
			return false;
		}
		if (bindingType == null) {
			if (other.bindingType != null) {
				return false;
			}
		} else if (!bindingType.equals(other.bindingType)) {
			return false;
		}
		
		if (colors == null) {
			if (other.colors != null) {
				return false;
			}
		} else if (!colors.equals(other.colors)) {
			return false;
		}
		if (critirias == null) {
			if (other.getCritirias() != null) {
				return false;
			}
		} else if (!critirias.equals(other.getCritirias())) {
			return false;
		}
		
		if (isbn == null) {
			if (other.isbn != null) {
				return false;
			}
		} else if (!isbn.equals(other.isbn)) {
			return false;
		}
		if (lamination == null) {
			if (other.lamination != null) {
				return false;
			}
		} else if (!lamination.equals(other.lamination)) {
			return false;
		}
		if (lastPrinted == null) {
			if (other.lastPrinted != null) {
				return false;
			}
		} else if (!lastPrinted.equals(other.lastPrinted)) {
			return false;
		}
		if (Float.floatToIntBits(length) != Float.floatToIntBits(other.length)) {
			return false;
		}
		
		if (pagesCount == null) {
			if (other.pagesCount != null) {
				return false;
			}
		} else if (!pagesCount.equals(other.pagesCount)) {
			return false;
		}
		if (paperType == null) {
			if (other.paperType != null) {
				return false;
			}
		} else if (!paperType.equals(other.paperType)) {
			return false;
		}
		
		if (publishDate == null) {
			if (other.publishDate != null) {
				return false;
			}
		} else if (!publishDate.equals(other.publishDate)) {
			return false;
		}
		
		if (softDelete != other.softDelete) {
			return false;
		}
		if (!(thickness == other.thickness)) {
				return false;
		}
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		if (Float.floatToIntBits(width) != Float.floatToIntBits(other.width)) {
			return false;
		}
		return true;
	}
}
