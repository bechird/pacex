

package com.epac.cap.config;

import com.mycila.event.api.Dispatcher;

import com.mycila.event.spi.Dispatchers;

public  class NDispatcher {

	static Dispatcher dispatcher;
	public static Dispatcher getDispatcher(){
		if(dispatcher == null)
		dispatcher = Dispatchers.synchronousSafe();
		
		return dispatcher;
	}
	
	
	
}
