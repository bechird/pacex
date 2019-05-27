/*
 * This file was derived from AbstractTransactionalJUnit4SpringContextTests.java since a class may not extend from two
 * classes.
 * 
 * Copyright 2002-2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.epac.cap.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

/**
 * Base class for unit test classes requiring an entity manager/datasource/transaction. If you need to load additional
 * contexts then add override the ContextConfiguration annotation in your subclass.
 * 
 */
@ContextConfiguration(locations = {"/WEB-INF/testContext.xml"},
                      loader = WebAppLoader.class,
                      inheritLocations = true)
@TransactionConfiguration(transactionManager = "transactionManager")
@TestExecutionListeners(value = {TransactionalTestExecutionListener.class,
                                 DependencyInjectionTestExecutionListener.class,
                                 DirtiesContextTestExecutionListener.class})
@Transactional
public abstract class SpringTransactionalBaseTest extends SpringBaseTest {
  protected DataSource dataSource;
  /**
   * The SimpleJdbcTemplate that this base class manages, available to subclasses if needed.
   */
  protected JdbcTemplate jdbcTemplate;

  /**
   * Sets the dataSource (through spring autowiring) and also instantiates the jdbcTemplate.
   * 
   * @param dataSource
   */
  @Autowired
  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
    jdbcTemplate = new JdbcTemplate(dataSource);
  }

  /**
   * Count the rows in the given table.
   * 
   * @param tableName table name to count rows in
   * @return the number of rows in the table
   
  protected int countRowsInTable(String tableName) {
    return jdbcTemplate.queryForRowSet("SELECT * FROM " + tableName).;
  }*/

  /**
   * Convenience method for deleting all rows from the specified tables. Use with caution outside of a transaction!
   * 
   * @param names the names of the tables from which to delete
   * @see SimpleJdbcTestUtils#deleteFromTables(SimpleJdbcTemplate, String...)
   * @return the total number of rows deleted from all specified tables
   */
  protected int deleteFromTables(String... names) {
    int totalRowCount = 0;
    for (String tableName : names) {
      int rowCount = jdbcTemplate.update("DELETE FROM " + tableName);
      totalRowCount += rowCount;
      if (logger.isInfoEnabled()) {
        logger.info("Deleted " + rowCount + " rows from table " + tableName);
      }
    }
    return totalRowCount;
  }

}
