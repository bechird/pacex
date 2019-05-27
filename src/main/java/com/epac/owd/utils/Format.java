package com.epac.owd.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Format {
	
	public static final int DEFAULT_DECIMAL_PLACES = 4;
	
	public static final BigDecimal conv_fact_72;
	public static final BigDecimal conv_fact_25_4; 
	private static final MathContext mc; 
	
	private static final BigDecimal conv_factor = new BigDecimal("25.4");
	
	//private static final MathContext mc = 
	//	new MathContext(DEFAULT_DECIMAL_PLACES, RoundingMode.HALF_UP);
	
	
	static {
		conv_fact_72 = new BigDecimal("72.0"); 
		conv_fact_25_4 = new BigDecimal("25.4"); 
		mc = new MathContext(DEFAULT_DECIMAL_PLACES, RoundingMode.HALF_UP); 
	}
	
	
	public enum Unit {
		INCH("inch"),
		MM("mm"),
		POINTS("points");
		
		private String name; 
		
		private Unit(String name) {
			this.name = name;
		}
		
		public String getName() { return name; }
	}
	
	
	public static String convert2Unit(Format.Unit unit, BigDecimal mm) {
		if(mm == null) 
			return "";
		if(unit.equals(Unit.INCH)) 
			return mm2inch(mm).toString();
		return mm.round(mc).toString();
	}
	
	/* Converts String into BigDecimal, if the unit equals inch, the
	 * 	value is also converted to mm. */
	public static BigDecimal convert(Format.Unit unit, String val) 
		throws NumberFormatException {
		if(val == null) return null;
		if(val.equals("")) return null;
		if(unit.equals(Unit.INCH))
			return new BigDecimal(val.trim()).multiply(conv_factor, mc);
		else
			return new BigDecimal(val.trim()).round(mc);
	}
	
	

	public static String inch2mm(BigDecimal val) {
		if(val == null) return "";
		return val.multiply(conv_factor, mc).toString();
	}
	
	
	public static BigDecimal points2mm(Float value) {
		BigDecimal points = new BigDecimal(Float.toString(value));
		return points.divide(conv_fact_72, mc).multiply(conv_fact_25_4, mc); 
	}
	
	protected static BigDecimal points2mm(BigDecimal val) {
		return points2mm(val.floatValue());
	}

	
	
	public static BigDecimal mm2points(Float value) {
		return mm2points(new BigDecimal(Float.toString(value))); 
		
	}
	
	public static BigDecimal mm2points(BigDecimal mm) {
		return mm.divide(conv_fact_25_4, mc).multiply(conv_fact_72, mc);
	}
	


	public static BigDecimal mm2inch(Float value) {
		BigDecimal mm = new BigDecimal(Float.toString(value)); 
		return mm.divide(conv_fact_25_4, mc); 
	}
	
	public static BigDecimal mm2inch(BigDecimal val) {
		if(val == null) return null;
		return val.divide(conv_fact_25_4, mc);
	}
	
	
	protected static BigDecimal inch2mm(Float value, int precision) {
		BigDecimal mm = new BigDecimal(Float.toString(value));
		return mm.multiply(conv_fact_25_4, mc);
	}
	
} 
