package com.epac.imposition.utils;

public enum CompositionScheme {
	ONE_UP("1UP", 1), TWO_UP("2UP", 2), THREE_UP("3UP", 3), FOUR_UP("4UP", 4);

	private String name;
	private int value;
	private CompositionScheme(String name, int value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() { return name; }
	public int getPagesPerSignature() {return value;}
	public int getPagesPerSheet() {return value *2;}
	
}
