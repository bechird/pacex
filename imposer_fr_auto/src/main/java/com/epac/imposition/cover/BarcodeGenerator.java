package com.epac.imposition.cover;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BarcodeGenerator {
	
	
	
	public static String getMullerMartiniBarcode (String jobID, float width, float height, float headtrim){
		StringBuffer sb = new StringBuffer(32); 
		
		for(int i=jobID.length() ; i<14; i++) {
			sb.append('0');
		}
		sb.append(jobID); 
		
		String cHeight = getValueinMM(height);
		for(int i=cHeight.length(); i<4; i++) {
			sb.append('0');
		}
		sb.append(cHeight);
		
		String cWidth = getValueinMM(width);
		for(int i=cWidth.length(); i<4; i++) {
			sb.append('0');
		}
		sb.append(cWidth);
		
		String cHeadTrim = getValueinMM(headtrim);
		for(int i=cHeadTrim.length(); i<4; i++) {
			sb.append('0');
		}
		sb.append(cHeadTrim);
	

		System.out.println("trimmer datamatrix "+sb.toString());
		return sb.toString();
	}
	
	/**
	 * 
	 * @param valueInInch
	 * @return
	 */
	private static String getValueinMM(float valueInInch) {
		BigDecimal cutLength = BigDecimal.valueOf(valueInInch*10);
		String result = cutLength.setScale(0, RoundingMode.HALF_UP).toString(); 
		return result;
	}
}
