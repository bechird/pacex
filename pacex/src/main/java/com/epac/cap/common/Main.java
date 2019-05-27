package com.epac.cap.common;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.epac.cap.utils.OrderXMLParser;

public class Main {
	
	public static void main1(String[] args) {
		//SecureRandom random = new SecureRandom();
		
		BCryptPasswordEncoder encoder = new  BCryptPasswordEncoder(11);
		
		
		String pwd = "$2a$11$gxpnezmYfNJRYnw/EpIK5Oe08TlwZDmcmUeKkrGcSGGHXvWaxUwQ2"; //encoder.encode("password");
		
		System.out.println(encoder.matches("password", pwd));
		
		System.out.println(encoder.encode("password"));
		System.out.println(encoder.encode("password"));
		System.out.println(encoder.encode("password"));
	}
	
	public static void main(String[] args) {

		String url = "/Users/islem/Downloads/OdersXmlExample.xml";
		
		OrderXMLParser orderXMLParser = new OrderXMLParser();

		//System.out.println(orderXMLParser.parseXML(url));

	}
}
