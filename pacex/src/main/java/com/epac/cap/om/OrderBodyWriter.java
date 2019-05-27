package com.epac.cap.om;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import com.epac.cap.model.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OrderBodyWriter implements MessageBodyWriter<Order>{

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public long getSize(Order t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		// TODO Auto-generated method stub
		 ObjectMapper mapper = new ObjectMapper();
		String orderString = "";
		try {
			orderString = mapper.writeValueAsString(t);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return orderString.length();
	}

	@Override
	public void writeTo(Order t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException {
         ObjectMapper mapper = new ObjectMapper();
         ObjectMapper objectMapper = new ObjectMapper();
         objectMapper.configure(
             DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
         mapper.writeValue(entityStream, t);
	}

}
