package com.epac.cap.sse.pacer;

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
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.handler.LookupHandler;
import com.epac.cap.model.Preference;
import com.epac.cap.sse.interfaces.TokenGetter;
import com.epac.om.api.utils.LogUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PacerTokenGetter implements TokenGetter{
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public String getToken(CloseableHttpClient httpClient, LookupHandler lookupHandler) {
		
		String token = null;
		Preference pref = null;
		
		
		String pacerServerUrl = null;		
		try {pref = lookupHandler.read(ConfigurationConstants.PACER_SERVER_URL, Preference.class);
			pacerServerUrl = pref.getName();} catch (Exception e2) {}		
		if (pacerServerUrl == null) {
			//LogUtils.info(ConfigurationConstants.PACER_SERVER_URL + " is not defined, exiting...");
			return "-1";
		}
		
		
		
		String pacerOauthUri = null;
		try {pref = lookupHandler.read(ConfigurationConstants.PACER_OAUTH_URI, Preference.class);
		pacerOauthUri = pref.getName();} catch (Exception e2) {}
		if (pacerOauthUri == null) {
			//LogUtils.info(ConfigurationConstants.PACER_OAUTH_URI + " is not defined, exiting...");
			return "-1";
		}
		String pacerOauthUrl = pacerServerUrl + pacerOauthUri;

		
		String pacerOauthLogin = null;
		try {pref = lookupHandler.read(ConfigurationConstants.PACER_OAUTH_LOGIN, Preference.class);
		pacerOauthLogin = pref.getName();} catch (Exception e2) {}
		if (pacerOauthLogin == null) {
			//LogUtils.info(ConfigurationConstants.PACER_OAUTH_LOGIN + " is not defined, exiting...");
			return "-1";
		}

		String pacerOauthPassword = null;
		try {pref = lookupHandler.read(ConfigurationConstants.PACER_OAUTH_PASSWORD, Preference.class);
		pacerOauthPassword = pref.getName();} catch (Exception e2) {}
		if (pacerOauthPassword == null) {
			//LogUtils.info(ConfigurationConstants.PACER_OAUTH_PASSWORD + " is not defined, exiting...");
			return "-1";
		}
		
		String pacerOauthUserName = null;
		try {pref = lookupHandler.read(ConfigurationConstants.PACER_USER_USERNAME, Preference.class);
		pacerOauthUserName = pref.getName();} catch (Exception e2) {}
		if (pacerOauthUserName == null) {
			//LogUtils.info("com.epac.cap.pacer.user.username is not defined, exiting...");
			return "-1";
		}

		String pacerOauthUserPassword = null;
		try {pref = lookupHandler.read(ConfigurationConstants.PACER_USER_PASSWORD, Preference.class);
		pacerOauthUserPassword = pref.getName();} catch (Exception e2) {}
		if (pacerOauthUserPassword == null) {
			//LogUtils.info("com.epac.cap.pacer.user.password is not defined, exiting...");
			return "-1";
		}
		
		
		
		
		Encoder encoder = Base64.getEncoder();
		String encodedAuth = new String(encoder.encode(pacerOauthLogin.concat(":").concat(pacerOauthPassword).getBytes()));

		List<NameValuePair> params = new ArrayList<NameValuePair>();
	    params.add(new BasicNameValuePair("grant_type", "password"));
	    params.add(new BasicNameValuePair("username", pacerOauthUserName));
	    params.add(new BasicNameValuePair("password", pacerOauthUserPassword));

	    

	    try {		    	

				
					HttpPost httpPost = new HttpPost(pacerOauthUrl);
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
