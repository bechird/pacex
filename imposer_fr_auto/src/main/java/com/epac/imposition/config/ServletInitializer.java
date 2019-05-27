package com.epac.imposition.config;

import javax.servlet.Filter;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class ServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class[] { SpringMvcConfig.class };
	}

	@Override
	protected String getServletName() {
		return "imposer";
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/*" };
	}

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return null;
	}

	@Override
	protected Filter[] getServletFilters() {
		return new Filter[] {};
	}

	@Override
	protected WebApplicationContext createRootApplicationContext() {
		AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
		return applicationContext;
	}
}
