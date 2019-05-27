package com.epac.cap.om;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.utils.LogUtils;
import com.epac.om.api.book.Book;
import com.epac.om.api.book.BookStatus;
import com.epac.om.api.book.Metadata;
import com.epac.om.api.common.Criteria;
import com.epac.om.api.production.ProductionOrder;
import com.epac.om.client.OMClientException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Component
public class ProductionClient {

	protected String bookService;
	protected String service;
	
	public ProductionClient() {
		this.service = System.getProperty(ConfigurationConstants.OM_SERVICE);
		this.bookService = System.getProperty(ConfigurationConstants.BOOK_SERVICE);
	}
	
	private String getToken() throws OMClientException{
		
		String token = null;
		
		String username = System.getProperty(ConfigurationConstants.SSO_USERNAME);
		String password = System.getProperty(ConfigurationConstants.SSO_PASSWORD);
		
		if (username == null || username.isEmpty() || password == null || password.isEmpty())
			throw new OMClientException("Username/Password must not be either null or empty");

		String securityService = System.getProperty(ConfigurationConstants.SSO_SERVICE);
		LogUtils.info("Authentication: " + securityService);
		Encoder encoder = Base64.getEncoder();
		String encodedAuth = new String(encoder.encode(username.concat(":").concat(password).getBytes()));

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("grant_type", "client_credentials");
		
		//params.add("grant_type", "password");
		//params.add("username", username);
		//params.add("password", password);

		HttpHeaders headers;
		headers = new HttpHeaders();
		headers.add("Authorization", "Basic ".concat(encodedAuth));
		//headers.add("Authorization", "Basic ".concat("dGVzdDp0ZXN0"));
		headers.add("Accept", org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
		RestTemplate rest = new RestTemplate();
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(params,
				headers);
		ResponseEntity<Map> response = rest.exchange(securityService, HttpMethod.POST, request, Map.class);

		if (response.getStatusCode().is2xxSuccessful()) {
			token = (String) response.getBody().get("access_token");
			LogUtils.info("Authentication success, token: " + token);
		} else {
			throw new OMClientException(
					"Authentication failed with error code (" + response.getStatusCode() + "): " + response.getBody());
		}
		
		return token;
	}
	
	public Book fetchBook( String resource, String token) throws Exception {
		
		if(token == null) {
			try {
				token = getToken();
			} catch (Exception e) {
				return null;
			}
		}
		
		String url = "https://library.esprint.com.mt/".concat(resource);		
		
		LogUtils.info("Start Http request: GET "+url);
		LogUtils.info("Using token: "+token);
		ResponseEntity<Book> response = null;

		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();

		headers.add("Authorization", "Bearer ".concat(token));
		headers.add("Accept", javax.ws.rs.core.MediaType.APPLICATION_JSON);
		HttpEntity<Object> request = new HttpEntity<Object>(headers);
		try {
			
			response = rest.exchange(url, HttpMethod.GET, request, Book.class);

		} catch (Exception e) {
			throw e;
		}		
		Book book = (Book) response.getBody();
	

		return book;// response != null? response.getBody() : null;

	}
	
	
	public Map<String, Map<String, Object>> getBook(String bookId, String token) {
		
		if(token == null) {
			try {
				token = getToken();
			} catch (Exception e) {
				return null;
			}
		}

		Map<String, Map<String, Object>> response = null;
		try {
			LogUtils.info("Get the book with bookId : [" + bookId + "]");
			String resource = "/".concat(bookId);
			response = get(bookId, resource, token);
			if (response == null) {
				LogUtils.debug("Get the book with bookId [" + bookId + "] failed: EPP response was null");
			}
			if (response == null) {
				LogUtils.debug("Get the book with bookId  [" + bookId + "] failed ");
			} else {
				LogUtils.debug("Get the book with bookId  [" + bookId + "] successfully got ");
			}

		} catch (Exception e) {
			LogUtils.error("Get the book with bookId  [" + bookId + "] failed: ", e);
		}

		return response;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Map<String, Object>> get(Object entity, String resource, String token) {
		
		if(token == null) {
			try {
				token = getToken();
			} catch (Exception e) {
				return null;
			}
		}
		
		String url = bookService.concat(resource);
		
		
		LogUtils.info("Start Http request: GET "+url);
		LogUtils.info("Using token: "+token);
		ResponseEntity<Map> response = null;


		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();

		headers.add("Authorization", "Bearer ".concat(token));
		headers.add("Accept", javax.ws.rs.core.MediaType.APPLICATION_JSON);
		HttpEntity<Object> request = new HttpEntity<Object>(headers);
		try {
			
			response = rest.exchange(url, HttpMethod.GET, request, Map.class);
		} catch (Exception e) {
			LogUtils.info("Exception: " + e.getMessage());
			return null;
		}


		
		Map<String, Map<String, Object>> files = (Map<String, Map<String, Object>>) response.getBody();

		return files;// response != null? response.getBody() : null;

	}
	
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> post(Object entity, String resource, String token) throws Exception {
		
		if(token == null) {
			try {
				token = getToken();
			} catch (Exception e) {
				return null;
			}
		}

		ResponseEntity<Map> response = null;

		RestTemplate rest = new RestTemplate();
		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(new MappingJackson2HttpMessageConverter());
		converters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
		rest.setMessageConverters(converters);
		HttpHeaders headers = new HttpHeaders();

		headers.add("Authorization", "Bearer ".concat(token));
		headers.add("Content-Type", "text/plain");
		headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<Object> request = new HttpEntity<Object>(entity, headers);
		try {
			String url = service.concat(resource);
			response = rest.exchange(url, HttpMethod.POST, request, Map.class);
		} catch (Exception e) {
			throw e;
		}


		return response != null ? response.getBody() : null;

	}
	
	
	public Set<ProductionOrder> getOrders(Criteria criteria, String token) {
		
		
		if(token == null) {
			try {
				token = getToken();
			} catch (Exception e) {
				return null;
			}
		}

		ResponseEntity<ProductionPage> response = null;

		RestTemplate rest = new RestTemplate();
		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		
		SimpleModule module = new SimpleModule();
		
		module.addDeserializer(Book.class, new JsonDeserializer<Book>() {
			  @Override
			  public Book deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			    ObjectCodec oc = jp.getCodec();
			    JsonNode node = oc.readTree(jp);
			    final String bookId = node.asText();
			    Book book = new Book();
			    book.setBookId(bookId);
			    book.setMetadata(new Metadata());
			    book.setStatus(BookStatus.READY);
			    book.getMetadata().setBarcode(bookId.substring(0, 13));
			    return book;
			  }
			});
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(module);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		converters.add(new MappingJackson2HttpMessageConverter(mapper));
		
		rest.setMessageConverters(converters);
		HttpHeaders headers = new HttpHeaders();

		headers.add("Authorization", "Bearer ".concat(token));
		headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<Criteria> request = new HttpEntity<Criteria>(criteria, headers);
		try {
			String url = service.concat("search/online");
			response = rest.exchange(url, HttpMethod.POST, request, ProductionPage.class);

		} catch (Exception e) {
				throw e;
		}



		ProductionPage result = response.getBody();
		
		Set<ProductionOrder> resultSet = new HashSet<ProductionOrder>(result.getContent());
		return resultSet;
	}
	
}
	class ProductionPage{
		private List<ProductionOrder> content;
		
		public ProductionPage() {}
		public List<ProductionOrder> getContent() {
			return content;
		}
		
		public void setContent(List<ProductionOrder> content) {
			this.content = content;
		}




}
