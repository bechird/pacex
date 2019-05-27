package com.epac.cap.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtils {
	
	private static TimeZone timeZone = TimeZone.getTimeZone("UTC");
	
	static{
		TimeZone.setDefault(timeZone);
	}

	public static Date nowInUTC(){
		
		Calendar calendar = Calendar.getInstance(timeZone);
		Date date = calendar.getTime();
		return date;
	}
	
	
}
