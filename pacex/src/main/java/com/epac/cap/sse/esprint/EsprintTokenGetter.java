package com.epac.cap.sse.esprint;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.handler.LookupHandler;
import com.epac.cap.sse.interfaces.TokenGetter;
import com.epac.om.api.utils.LogUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EsprintTokenGetter implements TokenGetter{
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public String getToken(CloseableHttpClient httpClient, LookupHandler lookupHandler) {	
		
		String token = null;
		
		String url = System.getProperty(ConfigurationConstants.SSO_SERVICE);
		String username = System.getProperty(ConfigurationConstants.SSO_USERNAME);
		String password = System.getProperty(ConfigurationConstants.SSO_PASSWORD);
		
		Encoder encoder = Base64.getEncoder();
		String encodedAuth = new String(encoder.encode(username.concat(":").concat(password).getBytes()));

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("grant_type", "client_credentials"));
	    //params.add(new BasicNameValuePair("grant_type", "password"));
	    //params.add(new BasicNameValuePair("username", username));
	    //params.add(new BasicNameValuePair("password", password));

	    

	    try {		    	


				
					HttpPost httpPost = new HttpPost(url);
					//httpPost.setHeader("Authorization", "Basic ".concat("dGVzdDp0ZXN0"));
					httpPost.setHeader("Authorization", "Basic ".concat(encodedAuth));
					httpPost.setHeader("Accept", "application/json");

				    httpPost.setEntity(new UrlEncodedFormEntity(params));		    

					CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
					

					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						
						HttpEntity httpEntity = httpResponse.getEntity();					   
						String jsonResponse = EntityUtils.toString(httpEntity);					
						
						httpResponse.close();
						
						JsonNode jsonNode = objectMapper.readTree(jsonResponse);					
						token = jsonNode.get("access_token").asText();						

					} else {
						token = "-1";
						httpResponse.close();
					}
					
					
					
					
			} catch (Exception e) {}				
				
		
		return token;
	}

		

}
