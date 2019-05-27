package com.epac.cap.sse.printers;

import org.apache.http.impl.client.CloseableHttpClient;

import com.epac.cap.handler.LookupHandler;
import com.epac.cap.sse.interfaces.TokenGetter;

public class PrinterTokenGetter implements TokenGetter{
	
	@Override
	public String getToken(CloseableHttpClient httpClient, LookupHandler lookupHandler) {
		return "void";
	}

		

}
