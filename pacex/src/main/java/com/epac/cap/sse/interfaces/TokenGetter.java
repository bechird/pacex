package com.epac.cap.sse.interfaces;

import org.apache.http.impl.client.CloseableHttpClient;

import com.epac.cap.handler.LookupHandler;

public interface TokenGetter {			
	String getToken(CloseableHttpClient httpClient, LookupHandler lookupHandler);

}
