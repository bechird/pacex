package com.epac.imposition.web;

import java.nio.charset.Charset;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.epac.om.api.internal.xml.XmlHttpMessageConverter;

@Configuration
public class ImpositionWebConfigure extends WebMvcConfigurerAdapter {

	public ImpositionWebConfigure() {
	}

	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(converter());
        converters.add(new MappingJackson2HttpMessageConverter(new com.fasterxml.jackson.databind.ObjectMapper()));
        converters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
	}

    @Bean
    public HttpMessageConverter<?> converter() {
        XmlHttpMessageConverter converter = new XmlHttpMessageConverter();
        return converter;
    }
}
  