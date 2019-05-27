package com.epac.imposition.cover;

public enum ScoringAlignment {
  FRONT("FRONT"),  BACK("BACK"),  CENTER("CENTER");
  
	private String name;
  private ScoringAlignment(String name) {
	  this.name = name;
  }
  
  public String getName() { return name; }
  
  public String getLetter() { return name().charAt(0) + ""; }
}
