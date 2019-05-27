package com.epac.cap.common;


public class BCryptPasswordEncoderCustom extends BCryptPasswordEncoderImpl {
	public BCryptPasswordEncoderCustom(){
		super();
	}
	
	public BCryptPasswordEncoderCustom(int strength){
		super(strength);
	}
}
