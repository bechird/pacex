package com.epac.cap.test;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Base class for all tests which need spring (without transactional support). Configures Spring so sub classes don't
 * have to.
 * 
 * If you need to load additional contexts then add override the ContextConfiguration annotation in your subclass.
 * 
 */
@ContextConfiguration(locations = {"/WEB-INF/testContext.xml"}, loader = WebAppLoader.class)
public abstract class SpringBaseTest extends AbstractJUnit4SpringContextTests {

  /**
	 * 
	 */
  public SpringBaseTest() {}
  
  /**
   * Returns the user id used during the invocation of the request. Subclasses can override if necessary. By default
   * this returns testUserId
   * 
   * @return "testUserId"
   */
  protected String getUserId() {
    return "testUserId";
  }
  
  
  /**
   * 
   */
  protected void setUp() throws Exception{
   
  }

}
