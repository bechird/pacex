package com.epac.cap.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AbstractContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.util.Arrays;

/**
 * A Spring application context loader which also sets up a MockServletContext for use by test classes which need a
 * servletcontext. 
 */
public class WebAppLoader extends AbstractContextLoader {
  /**
   * a MockServletContext setup to look for resources using the defaultresourceloader
   */
  public static final MockServletContext SERVLET_CONTEXT = new MockServletContext();

  protected static final Logger logger = LoggerFactory.getLogger(WebAppLoader.class);

  // had trouble loading everything so using defaultloader instead "/test-resources", new FileSystemResourceLoader());

  protected BeanDefinitionReader createBeanDefinitionReader(final GenericApplicationContext context) {
    return new XmlBeanDefinitionReader(context);
  }

  @Override
  public final ConfigurableApplicationContext loadContext(MergedContextConfiguration mergedConfig) throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug(String.format("Loading ApplicationContext for merged context configuration [%s].", mergedConfig));
    }

    final GenericWebApplicationContext webContext = new GenericWebApplicationContext();
    webContext.getEnvironment().setActiveProfiles(mergedConfig.getActiveProfiles());
    SERVLET_CONTEXT.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webContext);
    //SERVLET_CONTEXT.initFromWebXml();
    webContext.setServletContext(SERVLET_CONTEXT);

    createBeanDefinitionReader(webContext).loadBeanDefinitions(mergedConfig.getLocations());
    AnnotationConfigUtils.registerAnnotationConfigProcessors(webContext);
    webContext.refresh();
    webContext.registerShutdownHook();
    return webContext;
  }

  @Override
  public final ConfigurableApplicationContext loadContext(final String... locations) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("Loading ApplicationContext for locations [" + Arrays.toString(locations) + "].");
    }

    final GenericWebApplicationContext webContext = new GenericWebApplicationContext();
    SERVLET_CONTEXT.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webContext);
    //SERVLET_CONTEXT.initFromWebXml();
    webContext.setServletContext(SERVLET_CONTEXT);

    createBeanDefinitionReader(webContext).loadBeanDefinitions(locations);
    AnnotationConfigUtils.registerAnnotationConfigProcessors(webContext);
    webContext.refresh();
    webContext.registerShutdownHook();
    return webContext;
  }

  @Override
  protected String getResourceSuffix() {
    return "Context.xml";
  }

}
