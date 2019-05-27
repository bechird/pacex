package com.epac.cap.config;

import javax.servlet.Filter;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;


public class ServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
	
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] {};
    }

    
    @Override
    protected String getServletName() {
    	return "pacex";
    }
    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return null;
    }

    @Override
    protected Filter[] getServletFilters() {
        return new Filter[]{ };
    }

}
