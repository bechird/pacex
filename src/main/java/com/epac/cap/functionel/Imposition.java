package com.epac.cap.functionel;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.epac.cap.model.Preference;
import com.epac.cap.repository.LookupDAO;
import com.epac.cap.service.NotificationService;
import com.epac.cap.sse.beans.EventTarget;

@Component
public class Imposition {

	@Autowired
	private LookupDAO lookupDAO;

	@Autowired
	private NotificationService notificationService;
	
	
	public ImpositionRequest createRequest() {
		return new ImpositionRequest();
	}

	public ImpositionResponse submit(ImpositionRequest impositionRequest) {
		synchronized (this) {
			// Url for the imposition platform
			String url = lookupDAO.read("IMPOSITION_URL", Preference.class).getName();
			/* WFSProgressHandler.broadcast("IMPOSING");*/
			String ssePayload ="IMPOSING";			
			com.epac.cap.sse.beans.Event event=new com.epac.cap.sse.beans.Event(EventTarget.WFSProgress, false, null, ssePayload);
			notificationService.broadcast(event);
			
			RestTemplate rest = new RestTemplate();
			List<HttpMessageConverter<?>> converters = new ArrayList<>();
			converters.add(new MappingJackson2HttpMessageConverter());
			converters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
			rest.setMessageConverters(converters);
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
			headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
			HttpEntity<ImpositionRequest> entity = new HttpEntity<ImpositionRequest>(impositionRequest, headers);
			
			ResponseEntity<ImpositionResponse> impositionResponse = rest.exchange(url, HttpMethod.POST, entity,
					ImpositionResponse.class);

			return impositionResponse.getBody();
		}
	}
}
