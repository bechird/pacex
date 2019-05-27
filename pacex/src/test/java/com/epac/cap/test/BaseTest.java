package com.epac.cap.test;

import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.epac.cap.common.SimplePrincipal;


/**
 * Base class for all PACEX tests. Configures Spring so sub classes don't have to.
 */
@ContextConfiguration(locations = {"/WEB-INF/testContext.xml"},
                      loader = WebAppLoader.class,
                      inheritLocations = true)
@TransactionConfiguration(transactionManager = "transactionManager")
public abstract class BaseTest extends SpringTransactionalBaseTest {
	protected SimplePrincipal testPrincipal = null;
	
	@Override
	  @Before
	  public void setUp() throws Exception {
	    super.setUp();
	    testPrincipal = new SimplePrincipal(getUserId(), new String[] {"ADMIN"});
	  }

	  @Override
	  public String getUserId() {
	    return "walidb";
	  }
}
