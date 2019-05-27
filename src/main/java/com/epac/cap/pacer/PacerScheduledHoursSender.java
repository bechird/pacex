package com.epac.cap.pacer;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.handler.JobHandler;
import com.epac.cap.handler.LookupHandler;
import com.epac.cap.model.BindingType;
import com.epac.cap.model.Lamination;
import com.epac.cap.model.PaperType;
import com.epac.cap.model.PartCategory;
import com.epac.cap.model.Preference;
import com.epac.cap.model.WFSProductionStatus;
import com.epac.om.api.utils.LogUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class PacerScheduledHoursSender {
	
	@Autowired
	private JobHandler jobHandler;
	
	@Autowired
	private LookupHandler lookupHandler;
	
	private ObjectMapper objectMapper = new ObjectMapper();	
	
	@Autowired
	private CloseableHttpClient sseHttpClient;

	@Scheduled(fixedDelay = 7200000, initialDelay = 30000)
	public void run() {
		
		Preference pref = null;
		
		
		String pacerServerUrl = null;		
		try {pref = lookupHandler.read(ConfigurationConstants.PACER_SERVER_URL, Preference.class);
			pacerServerUrl = pref.getName();} catch (Exception e2) {}		
		if (pacerServerUrl == null) {
			LogUtils.info(ConfigurationConstants.PACER_SERVER_URL + " is not defined, exiting...");
			return;
		}
		
		
		String pacerOauthUri = null;
		try {pref = lookupHandler.read(ConfigurationConstants.PACER_OAUTH_URI, Preference.class);
		pacerOauthUri = pref.getName();} catch (Exception e2) {}
		if (pacerOauthUri == null) {
			LogUtils.info(ConfigurationConstants.PACER_OAUTH_URI + " is not defined, exiting...");
			return;
		}
		String pacerOauthUrl = pacerServerUrl + pacerOauthUri;

		
		String pacerOauthLogin = null;
		try {pref = lookupHandler.read(ConfigurationConstants.PACER_OAUTH_LOGIN, Preference.class);
		pacerOauthLogin = pref.getName();} catch (Exception e2) {}
		if (pacerOauthLogin == null) {
			LogUtils.info(ConfigurationConstants.PACER_OAUTH_LOGIN + " is not defined, exiting...");
			return;
		}

		String pacerOauthPassword = null;
		try {pref = lookupHandler.read(ConfigurationConstants.PACER_OAUTH_PASSWORD, Preference.class);
		pacerOauthPassword = pref.getName();} catch (Exception e2) {}
		if (pacerOauthPassword == null) {
			LogUtils.info(ConfigurationConstants.PACER_OAUTH_PASSWORD + " is not defined, exiting...");
			return;
		}
		
		String pacerOauthUserName = null;
		try {pref = lookupHandler.read(ConfigurationConstants.PACER_SCHEDULED_HOURS_USERNAME, Preference.class);
		pacerOauthUserName = pref.getName();} catch (Exception e2) {}
		if (pacerOauthUserName == null) {
			LogUtils.info(ConfigurationConstants.PACER_SCHEDULED_HOURS_USERNAME + " is not defined, exiting...");
			return;
		}

		
		String pacerOauthUserPassword = null;
		try {pref = lookupHandler.read(ConfigurationConstants.PACER_SCHEDULED_HOURS_PASSWORD, Preference.class);
		pacerOauthUserPassword = pref.getName();} catch (Exception e2) {}
		if (pacerOauthUserPassword == null) {
			LogUtils.info(ConfigurationConstants.PACER_SCHEDULED_HOURS_PASSWORD + " is not defined, exiting...");
			return;
		}

		String site = null;
		try {pref = lookupHandler.read(ConfigurationConstants.PACER_SSE_SITE, Preference.class);
		site = pref.getName();} catch (Exception e2) {}
		if (site == null) {
			LogUtils.info(ConfigurationConstants.PACER_SSE_SITE + " is not defined, exiting...");
			return;
		}
		

		String pacerScheduledHoursUri = null;
		try {pref = lookupHandler.read(ConfigurationConstants.PACER_SCHEDULED_HOURS_URI, Preference.class);
		pacerScheduledHoursUri = pref.getName();} catch (Exception e2) {}
		if (pacerScheduledHoursUri == null) {
			LogUtils.info(ConfigurationConstants.PACER_SCHEDULED_HOURS_URI + " is not defined, exiting...");
			return;
		}
		

		String token = null;
		
		Float scheduledHours = 0f;

		LogUtils.info("Authentication url: " + pacerOauthUrl);
		Encoder encoder = Base64.getEncoder();
		String encodedAuth = new String(
				encoder.encode(pacerOauthLogin.concat(":").concat(pacerOauthPassword).getBytes()));

		List<NameValuePair> params = new ArrayList<NameValuePair>();
	    params.add(new BasicNameValuePair("grant_type", "password"));
	    params.add(new BasicNameValuePair("username", pacerOauthUserName));
	    params.add(new BasicNameValuePair("password", pacerOauthUserPassword));
	
			
			try {
				
				
				HttpPost httpPost = new HttpPost(pacerOauthUrl);
				httpPost.setHeader("Authorization", "Basic ".concat(encodedAuth));
				
				httpPost.setHeader("Accept", ContentType.APPLICATION_JSON.toString());
				httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

			    httpPost.setEntity(new UrlEncodedFormEntity(params));		    

				CloseableHttpResponse httpResponse = sseHttpClient.execute(httpPost);			
				
								
				
	
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					
										   
					HttpEntity httpEntity = httpResponse.getEntity();
					String jsonResponse = EntityUtils.toString(httpEntity);				
					//LogUtils.info("jsonResponse: " + jsonResponse);	
					
					
					JsonNode jsonNode = objectMapper.readTree(jsonResponse);					
					token = jsonNode.get("access_token").asText();
					LogUtils.info("Authentication success, token: " + token);
	
					LogUtils.debug("calculatePressHours");
					Float c4ScheduledHhours = 0f;
					try {
						c4ScheduledHhours = jobHandler.calculatePressHours("4C", "A", "S");
					} catch (Exception e) {}
					
					Float c1ScheduledHhours = 0f;
					try {
						c1ScheduledHhours = jobHandler.calculatePressHours("1C", "A", "S");
					} catch (Exception e) {}
					
				
					scheduledHours = c4ScheduledHhours + c1ScheduledHhours * 2;
					LogUtils.debug("scheduledHours:" + scheduledHours);
	
				} else {
					LogUtils.info("Authentication to pacer failed with error code (" + httpResponse.getStatusLine().getStatusCode() + ")");
					LogUtils.info("Please check the login and password in the configuration file");
					
				}				
				
				httpResponse.close();
				
				
				
				//main request -----------------------------------------------------------------------------
				String pacerScheduledHoursUrl = pacerServerUrl + pacerScheduledHoursUri;
				
				LogUtils.info("pacerScheduledHoursUrl: " + pacerScheduledHoursUrl);
				
				HttpPost mainHttpPost = new HttpPost(pacerScheduledHoursUrl);
				mainHttpPost.setHeader("Authorization", "Bearer ".concat(token));				
				mainHttpPost.setHeader("Accept", ContentType.APPLICATION_JSON.toString());
				
				
				ObjectMapper mapper = new ObjectMapper();
				ObjectNode node = mapper.createObjectNode();
				node.put("site", site);
				node.put("scheduledhours", String.valueOf(scheduledHours));
				
				ObjectNode lookups = mapper.createObjectNode();
				//get lookups infos
				//binding type
				try{
					List<BindingType> list = lookupHandler.readAll(BindingType.class);
					JsonNode jsonNode = mapper.valueToTree(list);
					lookups.set("BindingType", jsonNode);					
				}catch (Exception c){}
				
				//Lamination
				try{
					List<Lamination> list = lookupHandler.readAll(Lamination.class);
					JsonNode jsonNode = mapper.valueToTree(list);
					lookups.set("Lamination", jsonNode);					
				}catch (Exception c){}

				//PaperType
				try{
					List<PaperType> list = lookupHandler.readAll(PaperType.class);
					JsonNode jsonNode = mapper.valueToTree(list);
					lookups.set("PaperType", jsonNode);					
				}catch (Exception c){}

				
				//PaperType
				try{
					List<PaperType> list = lookupHandler.readAll(PaperType.class);
					JsonNode jsonNode = mapper.valueToTree(list);
					lookups.set("PaperType", jsonNode);					
				}catch (Exception c){}
				
				//WFSProductionStatus
				try{
					List<WFSProductionStatus> list = lookupHandler.readAll(WFSProductionStatus.class);
					JsonNode jsonNode = mapper.valueToTree(list);
					lookups.set("WFSProductionStatus", jsonNode);					
				}catch (Exception c){}

				//PartCategory
				try{
					List<PartCategory> list = lookupHandler.readAll(PartCategory.class);
					JsonNode jsonNode = mapper.valueToTree(list);
					lookups.set("PartCategory", jsonNode);					
				}catch (Exception c){}
				
				node.set("lookups", lookups);
				String jsonBody = node.toString();
				
				mainHttpPost.setEntity(new StringEntity(jsonBody, ContentType.TEXT_PLAIN));
				CloseableHttpResponse mainHttpResponse = sseHttpClient.execute(mainHttpPost);
				
				HttpEntity mainHttpEntity = mainHttpResponse.getEntity();
				String jsonResponse = EntityUtils.toString(mainHttpEntity);				
				LogUtils.info("jsonResponse: " + jsonResponse);
				
				mainHttpResponse.close();
				
				
				//------------------------------------------------------------------------------------------
				
			} catch (Exception e) {
				LogUtils.info("Can't connect to pacer url:" + pacerOauthUrl + " cause :" + e.getMessage());
			}
		
		
			
			
			
			
		
		
		
		
		
		

	}

}
