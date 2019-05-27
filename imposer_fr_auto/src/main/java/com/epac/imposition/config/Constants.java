package com.epac.imposition.config;

public class Constants {
	
	
	public static final String	MATERIAL_TYPE_BOOKBLOCK				= "0";
	public static final String	MATERIAL_TYPE_COVER					= "1";
	public static final String	MATERIAL_TYPE_BOUNDBOOK				= "2";
	
	public static final float 	CROPMARK_DEFAULT_SIZE 				= 2.0f;
	
	public static final float 	PB_DEFAULT_MILLING 				    = 1.5875f; // 0.125inch (.0625inch on each side)
	
	public static final String  TEXT_BLEED_VALUE					= "com.epac.composer.text.bleed"; //value in mm
	public static final String  COVER_BLEED_VALUE					= "com.epac.composer.cover.bleed"; //value in mm
	
	
	public static final String  COVER_PRINTER_MARGIN				= "com.epac.composer.cover.printer.margins";
	public static final String  COVER_TOTAL_MARGIN					= "com.epac.composer.cover.margins";
	
	public static final byte  	CUSTOMER_SOGEDIF					= 0;
	
	public static final byte  	CUSTOMER_SEJER						= 1;
	
	
	public static final String  BOOK_RTL							= "RTL";
	public static final String  BOOK_LTR							= "LTR";
	
	public static final String 	BLEED 								= "bleed";
	public static final String 	TRIM  								= "trim";
	
	public static final String IMPOSITION_OUTPUT_FOLDER 			= "com.epac.composer.imposed";
	public static final String IMPOSITION_REFRESHBAR_PATH 			= "com.epac.composer.refreshbar";
	public static final String IMPOSITION_JDF_TEMPLATE_PATH 		= "com.epac.composer.jdf.template";
	
	public static final String IMPOSITION_ROLL_WIDTH 				= "com.epac.composer.rollwidth";
	public static final String IMPOSITION_GLUE 						= "com.epac.composer.glue";
	public static final String IMPOSITION_MILLING 					= "com.epac.composer.milling";
	public static final String IMPOSITION_BLEED 					= "com.epac.composer.text.bleed";
	public static final String IMPOSITION_INNER_MILLING 			= "com.epac.composer.innerMilling";
	public static final String IMPOSITION_TRIMING 					= "com.epac.composer.triming";
	public static final String IMPOSITION_TOP_MARGIN 				= "com.epac.composer.topmargin";
	public static final String IMPOSITION_BOTTOM_MARGIN 			= "com.epac.composer.bottommargin";
	
	public static final String IMPOSITION_PB_ENABLED				= "com.epac.composer.PB.enabled";
	public static final String IMPOSITION_PL_ENABLED				= "com.epac.composer.PL.enabled";
	public static final String IMPOSITION_PF_ENABLED				= "com.epac.composer.PF.enabled";
	public static final String IMPOSITION_FF_ENABLED				= "com.epac.composer.FF.enabled";
	
	public static final String IMPOSITION_MM_DATAMATRIX_ENABLED		= "com.epac.imposer.mm.datamatrix.enabled";
	
	public static final String IMPOSITION_INNER_CUEMARK_ENABLED		= "com.epac.imposer.inner.cuemark.enabled";
	
	public static final String IMPOSITION_HUNKELER_DATAMATRIX_XPOS  = "com.epac.composer.datamatrix.xpos";
	public static final String IMPOSITION_HUNKELER_DATAMATRIX_YPOS 	= "com.epac.composer.datamatrix.ypos";
	public static final String IMPOSITION_DATAMATRIX_WIDTH 			= "com.epac.composer.datamatrix.width";
	public static final String IMPOSITION_DATAMATRIX_HEIGHT 		= "com.epac.composer.datamatrix.height";
	
	public static final String IMPOSITION_HUNKELER_CUEMARK_WIDTH 	= "com.epac.composer.cuemark.width";
	public static final String IMPOSITION_HUNKELER_CUEMARK_HEIGHT 	= "com.epac.composer.cuemark.height";
	public static final String IMPOSITION_HUNKELER_CUEMARK_YPOS 	= "com.epac.composer.cuemark.ypos";
	
	public static final String IMPOSITION_HUNKELER_POP_CUEMARK_WIDTH 	= "com.epac.composer.pop.cuemark.width";
	public static final String IMPOSITION_HUNKELER_POP_CUEMARK_HEIGHT 	= "com.epac.composer.pop.cuemark.height";
	public static final String IMPOSITION_HUNKELER_POP_CUEMARK_YPOS 	= "com.epac.composer.pop.cuemark.ypos";
	
	public static final String IMPOSITION_HUNKELER_LINE				= "com.epac.composer.hunkeler.line";
	
	public static final String IMPOSITION_POP_PERFORATION			= "com.epac.composer.pop.perforation";
	
	public static final String IMPOSITION_NON_PRINTING_AREA 		= "com.epac.composer.nonprinting";
	public static final String IMPOSITION_OUTPUTCONTROLLER_MARGIN 	= "com.epac.composer.oc.margin";
	public static final String IMPOSITION_PRINTER_SHEET_HEIGHT 		= "com.epac.composer.printsheet.height";		
	
	public static final String IMPOSITION_PRINTER_SHEET_TYPE 		= "com.epac.composer.printsheet.type";
	
	public static final String IMPOSITION_CUSTOMER_CODE				= "com.epac.composer.customer.code";
	
	public static final String IMPOSITION_CUSTOMER_LOGO_WIDTH		= "com.epac.composer.customer.logo.width";
	public static final String IMPOSITION_CUSTOMER_LOGO_HEIGHT		= "com.epac.composer.customer.logo.height";
	
	public static final String IMPOSITION_PRINTER_SHEET_STANDARD_HEIGHT 	= "com.epac.composer.bigsheet.standard.height";	
	
	public static final String IMPOSITION_PRINTER_S1_HEIGHT 		= "com.epac.imposer.s1.height";
	public static final String IMPOSITION_PRINTER_S2_HEIGHT 		= "com.epac.imposer.s2.height";
	public static final String IMPOSITION_PRINTER_S3_HEIGHT 		= "com.epac.imposer.s3.height";
	
	
	public static final String COMPOSER_OUTPUT						=	"com.epac.composer.output";
	
	public static final String ESPRINT_ENDPOINT						=	"com.epac.composer.esprint.endpoint";
	public static final String ESPRINT_USERNAME						=	"com.epac.composer.esprint.username";
	public static final String ESPRINT_PASSWORD						=	"com.epac.composer.esprint.password";
	public static final String ESPRINT_SECURITY 					= 	"com.epac.composer.esprint.security";
	public static final String ESPRINT_REPOSITORY 					= 	"com.epac.composer.esprint.repository";
	
	public static final String IMPOSITION_TYPE 						=   "com.epac.composer.imposition.type";
	
	public static final String IMPOSITION_RIGHT_TO_LEFT 			=   "com.epac.imposer.rtl";
	
	
	public static final String IMPOSER_COVER_INPUT_FOLDER 					= "com.epac.composer.cover.input";
	public static final String IMPOSER_COVER_OUTPUT_FOLDER					= "com.epac.composer.output";
	
	//public static final String IMPOSITION_COVER_SHEET_WIDTH 				= "com.epac.composer.cover.sheet.width";
	//public static final String IMPOSITION_COVER_SHEET_HEIGHT 				= "com.epac.composer.cover.sheet.height";
	
	public static final String IMPOSITION_M_SHEET_WIDTH						= "com.epac.composer.sheet.M.width";
	public static final String IMPOSITION_M_SHEET_HEIGHT					= "com.epac.composer.sheet.M.height";
	public static final String IMPOSITION_L_SHEET_WIDTH						= "com.epac.composer.sheet.L.width";
	public static final String IMPOSITION_L_SHEET_HEIGHT					= "com.epac.composer.sheet.L.height";
	public static final String IMPOSITION_XL_SHEET_WIDTH					= "com.epac.composer.sheet.XL.width";
	public static final String IMPOSITION_XL_SHEET_HEIGHT					= "com.epac.composer.sheet.XL.height";
	
	public static final String IMPOSITION_COVER_YPOSITION					= "com.epac.composer.cover.sheet.y";

	public static final String IMPOSITION_COVER_FOLDINGEDGE_XPOS 			= "com.epac.composer.cover.foldingedge.xpos";
	
	public static final String IMPOSITION_COVER_DATAMATRIX_BINDER_XPOS 		= "com.epac.composer.cover.datamatrix.binder.x";
	public static final String IMPOSITION_COVER_DATAMATRIX_BINDER_YPOS 		= "com.epac.composer.cover.datamatrix.binder.y";

	public static final String IMPOSITION_COVER_DATAMATRIX_TRIMMER_XPOS 	= "com.epac.composer.cover.datamatrix.trimmer.x";
	public static final String IMPOSITION_COVER_DATAMATRIX_TRIMMER_YPOS 	= "com.epac.composer.cover.datamatrix.trimmer.y";
	public static final String IMPOSITION_COVER_DATAMATRIX_TRIMMER_OFFSET 	= "com.epac.composer.cover.datamatrix.trimmer.offset";
	
	public static final String IMPOSITION_COVER_MEASURELINE_XPOS 			= "com.epac.composer.cover.precut.measureLine.x";
	public static final String COVERS_BATCH_NAME							= "com.epac.composer.cover.batch";
	public static final String COMBINE_COVERS 								= "com.epac.composer.cover.combine";
	public static final String IMPOSITION_ADD_DEPOT_LEGAL 					= "com.epac.composer.text.depotLegal";
	public static final String END_OF_DOCUMENT_CODE				= "com.epac.composer.hunkeler.endOfDocumentCode";
	public static final String PNL_BASE_TEMP					= "com.epac.composer.directories.pnlBaseTemp";
}
