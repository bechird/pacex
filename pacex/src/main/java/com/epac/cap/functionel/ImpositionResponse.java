package com.epac.cap.functionel;

import java.util.HashMap;

public class ImpositionResponse extends HashMap<String, String>{

	private static final long serialVersionUID = 1L;
	
	public static final String ERROR = "error";
	public static final String MESSAGE = "message";

	public static final String FFSM_PDF = "FFSM.PDF";
	public static final String FFEM_PDF = "FFEM.PDF";
	public static final String PFSM_PDF = "PFSM.PDF";
	public static final String PFEM_PDF = "PFEM.PDF";
	public static final String PLSM_PDF = "PLSM.PDF";
	public static final String PLEM_PDF = "PLEM.PDF";
	public static final String PBSM_PDF = "PBSM.PDF";
	public static final String PBEM_PDF = "PBEM.PDF";
	
	public static final String FFSM_JDF = "FFSM.JDF";
	public static final String FFEM_JDF = "FFEM.JDF";
	public static final String PFSM_JDF = "PFSM.JDF";
	public static final String PFEM_JDF = "PFEM.JDF";
	public static final String PLSM_JDF = "PLSM.JDF";
	public static final String PLEM_JDF = "PLEM.JDF";
	public static final String PBSM_JDF = "PBSM.JDF";
	public static final String PBEM_JDF = "PBEM.JDF";
	
	public static final String COVER	= "cover";
	
	
	public ImpositionResponse() {
		
	}


	public boolean hasError() {
		String error = get(ERROR);
		return Boolean.valueOf(error != null ? error : "false");
	}
	
	
	
}