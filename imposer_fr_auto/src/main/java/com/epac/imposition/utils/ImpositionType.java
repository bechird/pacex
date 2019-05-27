package com.epac.imposition.utils;

public enum ImpositionType {
	
	TEXT("Text"), COVER("Cover"), ALL("All");

	private String name;
	private ImpositionType(String name) {
		this.name = name;
	}
	public String getName() { return name; }

}
