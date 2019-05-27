package com.epac.owd.service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.epac.cap.model.BindingType;
import com.epac.cap.model.Client;
import com.epac.cap.model.Lamination;
import com.epac.cap.model.LookupItem;
import com.epac.cap.model.PaperType;
import com.epac.owd.config.ConfigurationConstants;
import com.epac.owd.utils.LogUtils;

@Component
public class PacexClient {

	private String clientId;
	private String clientPass;

	private String token;
	private boolean shutdown;

	public PacexClient() {

		this.clientId = System.getProperty(ConfigurationConstants.SSO_CLIENTID);
		this.clientPass = System.getProperty(ConfigurationConstants.SSO_PASSWORD);
	}

	public void authenticate() {
		doAuthenticate();
	}

	@SuppressWarnings("rawtypes")
	private void doAuthenticate() {

		/*
		 * add to the header: Authorization : Basic base64(login:password) and
		 * put in the x-www-form-urlencoded : grant_type : client_credentials
		 */
		if (clientId == null || clientId.isEmpty() || clientPass == null || clientPass.isEmpty())
			throw new PacexClientException("ClientId/ClientPass must not be either null or empty");

		String securityService = System.getProperty(ConfigurationConstants.SSO_SERVICE);
		LogUtils.debug("Authentication: " + securityService);
		Encoder encoder = Base64.getEncoder();
		String encodedAuth = new String(encoder.encode(clientId.concat(":").concat(clientPass).getBytes()));

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("grant_type", "client_credentials");

		HttpHeaders headers;
		headers = new HttpHeaders();
		headers.add("Authorization", "Basic ".concat(encodedAuth));
		RestTemplate rest = new RestTemplate();

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(params,
				headers);
		ResponseEntity<Map> response = rest.exchange(securityService, HttpMethod.POST, request, Map.class);

		if (response.getStatusCode().is2xxSuccessful()) {
			token = (String) response.getBody().get("access_token");
			LogUtils.debug("Authentication success, token: " + token);
		} else {
			throw new PacexClientException(
					"Authentication failed with error code (" + response.getStatusCode() + "): " + response.getBody());
		}
	}

	@SuppressWarnings("unchecked")
	public List get(String url) throws AuthenticationException {
		if (token == null) {
			doAuthenticate();
		}

		// ResponseEntity<Map> response = null;
		boolean unauthorized = false;
		ResponseEntity<?> response = null;

		do {
			RestTemplate rest = new RestTemplate();
			List<HttpMessageConverter<?>> converters = new ArrayList<>();
			converters.add(new MappingJackson2HttpMessageConverter());
			converters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
			rest.setMessageConverters(converters);
			HttpHeaders headers = new HttpHeaders();

			headers.add("Authorization", "Bearer ".concat(token));
			headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE);
			headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
			HttpEntity<Object> request = new HttpEntity<Object>(headers);

			try {
				String type = url.substring(url.lastIndexOf('/') + 1);
				switch (type) {
				case "PaperType":
					response = rest.exchange(url, HttpMethod.GET, request,
							new ParameterizedTypeReference<List<PaperType>>() {
							});
					break;
				case "Client":
					response = rest.exchange(url, HttpMethod.GET, request,
							new ParameterizedTypeReference<List<Client>>() {
							});
					break;
				case "Lamination":
					response = rest.exchange(url, HttpMethod.GET, request,
							new ParameterizedTypeReference<List<Lamination>>() {
							});
					break;
				case "BindingType":
					response = rest.exchange(url, HttpMethod.GET, request,
							new ParameterizedTypeReference<List<BindingType>>() {
							});
					break;
				default:
					break;
				}

				unauthorized = false;
				if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
					unauthorized = true;
				}
			} catch (Exception e) {
				throw e;
			}

			if (unauthorized)
				doAuthenticate();

		} while (unauthorized);

		System.out.println(response.getBody());
		return (List<?>) response.getBody(); // response != null?
												// response.getBody() : null;

	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<String> post(Object entity, String url) throws AuthenticationException {
		if (token == null) {
			doAuthenticate();
		}

		ResponseEntity<String> response = null;
		boolean unauthorized = false;
		do {
			RestTemplate rest = new RestTemplate();
			List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
			messageConverters.add(new FormHttpMessageConverter());
			messageConverters.add(new StringHttpMessageConverter());
			messageConverters.add(new MappingJackson2HttpMessageConverter());
			rest.setMessageConverters(messageConverters);
			HttpHeaders headers = new HttpHeaders();

			headers.add("Authorization", "Bearer ".concat(token));
			headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
			headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
			HttpEntity<Object> request = new HttpEntity<Object>(entity, headers);
			try {
				response = rest.exchange(url, HttpMethod.POST, request, String.class);
				unauthorized = false;
				if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
					unauthorized = true;
				}
			} catch (Exception e) {
				throw e;
			}

			if (unauthorized)
				doAuthenticate();

		} while (unauthorized);

		return response != null ? response : null;

	}

	public String getToken() {
		return token;
	}

	public static void main(String[] args) throws AuthenticationException {

		System.setProperty(ConfigurationConstants.SSO_CLIENTID, "walidb@epac.com");
		System.setProperty(ConfigurationConstants.SSO_PASSWORD, "secret");
		System.setProperty(ConfigurationConstants.SSO_SERVICE, "http://localhost:8080/pacex/oauth/token");

		PacexClient client = new PacexClient();
		client.authenticate();
		List<LookupItem> e = new ArrayList<LookupItem>();
		e = client.get("http://localhost:8080/pacex/rest/lookups/BindingType");
		System.out.println(e);

	}
}
