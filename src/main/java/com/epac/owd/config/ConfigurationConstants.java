package com.epac.owd.config;

import java.util.List;

import com.epac.cap.model.BindingType;
import com.epac.cap.model.Lamination;
import com.epac.cap.model.PaperType;

public class ConfigurationConstants{
	
	public static final String LOCAL_ORDER_OLD_DIR 					= "com.epac.owd.local.order.old";
	public static final String LOCAL_ORDER_NEW_DIR 					= "com.epac.owd.local.order.new";
	
	public static final String SSO_SERVICE							= "com.epac.owd.pacex.security";
	public static final String SSO_USERNAME							= "com.epac.owd.pacex.username";
	public static final String SSO_PASSWORD							= "com.epac.owd.pacex.password";
	
	
	public static final String CAP_ADDRESS							= "com.epac.owd.cap.ip.address";


	public static final String FILEREPOSITORY						= "com.epac.owd.file.repository";
	
	public static final String REPOSITORY_PREFIX					= "com.epac.owd.repository.prefix";
	public static final String REPOSITORY_TRANSFER					= "com.epac.owd.repository.transfer";
	public static final String REPOSITORY_FINAL						= "com.epac.owd.repository.final";
	public static final String SSO_CLIENTID 						= "com.epac.owd.pacex.clientId";
	
	
	public static List<BindingType>	bindingTypes;
	public static List<Lamination>	laminations;
	public static List<PaperType>	paperTypes;

}
