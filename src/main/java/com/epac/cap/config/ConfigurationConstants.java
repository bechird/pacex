package com.epac.cap.config;

public class ConfigurationConstants {
	
	public static final String PACEX_ENV							= "com.epac.cap.env";
	public static final String HIBERNATE_DRIVER_CLASS 				= "hibernate.connection.driver_class";
	public static final String HIBERNATE_CONNECTION_URL 			= "hibernate.connection.url";
	public static final String HIBERNATE_CONNECTION_USERNAME 		= "hibernate.connection.username";
	public static final String HIBERNATE_CONNECTION_PASSWORD 		= "hibernate.connection.password";

	public static final String ESPRINT_CONNECT						= "com.epac.cap.esprint.connect";

	public static final String ESPRINT_RASTER_DIR                   = "com.epac.esprint.directories.raster";

	public static final String DIR_DOWNLOAD							= "com.epac.cap.directories.download";
	public static final String DIR_PREFLIGHT						= "com.epac.cap.directories.preflight";
	public static final String DIR_ARCHIVE 							= "com.epac.cap.directories.archive";
	public static final String DIR_RIPPING_INPUT					= "com.epac.cap.directories.ripping.input";
	public static final String DIR_RIPPING_OUTPUT					= "com.epac.cap.directories.raster";
	public static final String DIR_FUJI_INPUT						= "com.epac.cap.directories.fuji.input";

	public static final String DIR_RICOH_INPUT						= "com.epac.cap.directories.ricoh.input";
	public static final String DIR_PDF_RIPPING						= "com.epac.cap.directories.ripping.pdf";
	public static final String DIR_REPORTS							= "com.epac.cap.directories.reports";
	public static final String DIR_PROOFING							= "com.epac.cap.directories.proofing";
	public static final String DIR_TEMPORARY						= "com.epac.cap.directories.temporary";
	public static final String IMPOSED_PDF_DIR 						= "com.epac.cap.directories.imposing";
	public static final String DIR_VIEWER                           = "com.epac.cap.directories.viewer";
	public static final String DIR_UPLOAD_REPO                      = "com.epac.cap.directories.upload";
	public static final String TIME_LAPSE							= "com.epac.cap.timelapse";

	public static final String TEXT_PREFLIGHT_INPUT_DIR 			= "com.epac.cap.preflight.text.input";
	public static final String COVER_PREFLIGHT_INPUT_DIR 			= "com.epac.cap.preflight.cover.input";

	public static final String PREFLIGHT_OUTPUT_SUCCESS_DIR 		= "com.epac.cap.preflight.success";
	public static final String PREFLIGHT_OUTPUT_FIALURE_DIR 		= "com.epac.cap.preflight.failure";

	public static final String PREFLIGHT_TIMEOUT 					= "com.epac.cap.preflight.timeout";

	public static final String EMAIL_USERNAME						= "com.epac.cap.email.username";
	public static final String EMAIL_PASSWORD						= "com.epac.cap.email.password";
	public static final String EMAIL_SERVER							= "com.epac.cap.email.server";
	public static final String EMAIL_PORT							= "com.epac.cap.email.port";
	public static final String EMAIL_SECURITY						= "com.epac.cap.email.security";
	public static final String EMAIL_PREPRESS						= "com.epac.cap.email.prepress";
	public static final String EMAIL_SENDER 						= "com.epac.cap.email.sender";
	public static final String EMAIL_NAME 							= "com.epac.cap.email.name";

	public static final String EMAIL_ESPRINT						= "com.epac.cap.email.esprint";

	public static final String SFTP_HOSTNAME						= "com.epac.cap.sftp.hostname";
	public static final String SFTP_USERNAME 						= "com.epac.cap.sftp.username";
	public static final String SFTP_PASSWORD 						= "com.epac.cap.sftp.password";

	public static final String SSO_SERVICE								= "com.epac.cap.om.security";
	public static final String SSO_USERNAME								= "com.epac.cap.om.username";
	public static final String SSO_PASSWORD								= "com.epac.cap.om.password";

	public static final String OM_SERVICE								= "com.epac.cap.om.service";
	public static final String OM_RETRY_COUNT							= "com.epac.cap.om.retry.count";
	public static final String OM_RETRY_WAIT 							= "com.epac.cap.om.retry.wait";
	public static final String EPS_SERVICE                              = "com.epac.cap.eps.service";

	public static final String SFTP_RETRY 							= "com.epac.cap.sftp.retry";
	public static final String SFTP_TEMP_DIR						= "com.epac.cap.sftp.temp.dir";

	public static final String SMB_DOMAIN							= "com.epac.cap.smb.domain";
	public static final String SMB_USERNAME							= "com.epac.cap.smb.username";
	public static final String SMB_PASSWORD							= "com.epac.cap.smb.password";

	public static final String RIP_MAX_HOURS						= "com.epac.cap.rip.max.hours";
	public static final String RIP_INTERVAL							= "com.epac.cap.rip.interval";

	public static final String IMPOSITION_COVER_FRONTMARGIN			= "com.epac.composer.cover.frontmargin";
	public static final String DEFAULT_PAPER_WIDTH					= "com.epac.cap.default.paper.width";

	public static final String PRINTING_TEST						= "com.epac.oc.printing.test";
	public static final String PDFSLIP						        = "com.epac.cap.pdfSlip";
	public static final String PDFSLIPFR						    = "com.epac.cap.pdfSlipFR";
	public static final String BLIVRAISON						    = "com.epac.cap.bLivraison";
	public static final String BLIVRAISONINT						= "com.epac.cap.bLivraisonInter";

	public static final String DONOTCOPYRASTER_TEST					= "com.epac.oc.printing.donotCopyRaster.test";
	public static final String SINGLE_INSTANCE_PORT 				= "com.epac.cap.singleton.port";
	public static final String ESPRINT_DOWNLOAD_DIR                     = "com.epac.esprint.directories.download";
	public static final String ESPRINT_PROOFING_DIR                     = "com.epac.esprint.directories.proofing";
	public static final String ESPRINT_IMPOSED_DIR                      = "com.epac.esprint.directories.imposed";
	public static final String CAP_ADDRESS							 = "com.epac.cap.ip.address";
	public static final String BOOK_SERVICE							 = "com.epac.cap.book.service";

	public static final String MEDIABOX_M_SHEET_WIDTH					= "com.epac.cap.sheet.M.width";
	public static final String MEDIABOX_L_SHEET_WIDTH					= "com.epac.cap.sheet.L.width";
	public static final String MEDIABOX_XL_SHEET_WIDTH					= "com.epac.cap.sheet.XL.width";
	public static final String COVER_M_SHEET_HEIGHT						= "com.epac.cap.sheet.M.height";
	public static final String COVER_L_SHEET_HEIGHT						= "com.epac.cap.sheet.L.height";
	public static final String COVER_XL_SHEET_HEIGHT					= "com.epac.cap.sheet.XL.height";
	
	public static final String TRIM_CUT_SECUR_RETREAT					= "com.epac.cap.trimCut.secure.retreat";
	
	public static final String SHEET_S1_HEIGHT						= "com.epac.cap.sheet.S1.height";
	public static final String SHEET_S2_HEIGHT						= "com.epac.cap.sheet.S2.height";
	public static final String SHEET_S3_HEIGHT						= "com.epac.cap.sheet.S3.height";
	public static final String SHEET_MAX_HEIGHT						= "com.epac.cap.sheet.max.height";

	public static final String RIP_MAX_QUANTITY 					= "com.epac.cap.rip.max.quantity";
	public static final String Order_Type                            = "com.epac.cap.order.type";
	
	public static final String RasterFilesTransferPhpScriptAddress                            = "com.epac.cap.php.rasterFiles.transfer.address";
	
	
	public static final String PACER_SERVER_URL                     = "com.epac.cap.pacer.server.url";
	public static final String PACER_OAUTH_URI                      = "com.epac.cap.pacer.oauth.uri";
	public static final String PACER_SSE_URI                        = "com.epac.cap.pacer.sse.uri";
	public static final String PACER_SSE_SITE                       = "com.epac.cap.pacer.sse.site";	
	public static final String PACER_OAUTH_LOGIN                    = "com.epac.cap.pacer.oauth.login";
	public static final String PACER_OAUTH_PASSWORD                 = "com.epac.cap.pacer.oauth.password";	
	public static final String PACER_USER_USERNAME                  = "com.epac.cap.pacer.user.username";
	public static final String PACER_USER_PASSWORD                  = "com.epac.cap.pacer.user.password";	
	public static final String PACER_RECONNECTION_DELAY             = "com.epac.cap.pacer.reconnection.retry.delay.seconds";
	
	public static final String PACER_SCHEDULED_HOURS_USERNAME            = "com.epac.cap.pacer.scheduled.hours.username";
	public static final String PACER_SCHEDULED_HOURS_PASSWORD            = "com.epac.cap.pacer.scheduled.hours.password";	
	public static final String PACER_SCHEDULED_HOURS_URI            = "com.epac.cap.pacer.scheduled.hours.uri";
	public static final String PACER_SCHEDULED_HOURS_PERIOD         = "com.epac.cap.pacer.scheduled.hours.period.hours";
	
	public static final String NasAddress                            = "com.epac.cap.nas.address";
	public static final String NasLogin                            = "com.epac.cap.nas.login";
	public static final String NasPwd                            = "com.epac.cap.nas.pwd";

	// Used for PNL template naming conventions
	public static final String PNL_LOCATION							  = "com.epac.cap.pnl.template.location";
	public static final String PNL_DATE							      = "com.epac.cap.pnl.template.date";
	public static final String PNL_MONTH							  = "com.epac.cap.pnl.template.month";
	public static final String PNL_YEAR							      = "com.epac.cap.pnl.template.year";
	
}
