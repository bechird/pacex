package com.epac.cap.config;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.epac.cap.utils.LogUtils;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = { "com.epac.cap", "com.epac.oc" })
@EnableJpaRepositories(basePackages = "com.epac.cap.repository")
@EnableWebSecurity
@EnableWebMvc
@PropertySource("classpath:config.properties")
public class JpaConfig {

	/*

	@Bean
	@Autowired
	public DataSource dataSource() {

		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(System.getProperty(ConfigurationConstants.HIBERNATE_DRIVER_CLASS));
		dataSource.setUrl(System.getProperty(ConfigurationConstants.HIBERNATE_CONNECTION_URL));
		dataSource.setUsername(System.getProperty(ConfigurationConstants.HIBERNATE_CONNECTION_USERNAME));
		dataSource.setPassword(System.getProperty(ConfigurationConstants.HIBERNATE_CONNECTION_PASSWORD));
		dataSource.setMaxWaitMillis(5000);
		dataSource.setMaxIdle(20);
		dataSource.setInitialSize(30);
		dataSource.setValidationQuery("SELECT 1");

		LogUtils.debug("Initializing dataSource: URL = "+System.getProperty(ConfigurationConstants.HIBERNATE_CONNECTION_URL));
		/*
		JndiDataSourceLookup jndi = new JndiDataSourceLookup();
		try {
			dataSource = (DataSource) jndi.getDataSource("jdbc/paceDataSource");
		} catch (Exception e) {
			System.err.println("NamingException for jdbc/paceDataSource");
			e.printStackTrace(System.err);
		}
		return dataSource;
	}*/

	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
		return new JpaTransactionManager(emf);
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LogUtils.debug("Initializing entityManagerFactory: dataSource = "+(dataSource() == null?" is null": " successfully injected"));
		Properties properties = new Properties();

		properties.setProperty("hibernate.archive.autodetection", "class,hbm");
		properties.setProperty("hibernate.cache.use_second_level_cache", "true");
		properties.setProperty("hibernate.cache.use_query_cache", "true");
		properties.setProperty("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory");
		properties.setProperty("net.sf.ehcache.configurationResourceName", "/ehcache.xml");
		properties.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.EhCacheProvider");
		properties.setProperty("hibernate.generate_statistics", "false");
		properties.setProperty("hibernate.hbm2ddl.auto", "update");

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();

		vendorAdapter.setGenerateDdl(true);
		vendorAdapter.setShowSql(false);
		vendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQLDialect");
		vendorAdapter.setDatabase(Database.MYSQL);

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();

		factory.setJpaVendorAdapter(vendorAdapter);
		factory.setPackagesToScan("com.epac.cap.model");
		factory.setPersistenceUnitName("capPU");
		factory.setDataSource(dataSource());

		factory.setJpaProperties(properties);
		factory.afterPropertiesSet();

		return factory;
	}



	@Bean
	@Autowired
	public DataSource dataSource(){
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(System.getProperty(ConfigurationConstants.HIBERNATE_DRIVER_CLASS));
		dataSource.setUrl(System.getProperty(ConfigurationConstants.HIBERNATE_CONNECTION_URL));
		dataSource.setUsername(System.getProperty(ConfigurationConstants.HIBERNATE_CONNECTION_USERNAME));
		dataSource.setPassword(System.getProperty(ConfigurationConstants.HIBERNATE_CONNECTION_PASSWORD));
		
		return dataSource;
	}

	/*
	@Bean
	public DataSource dataSource() {
		DataSource dataSource = null;
		JndiDataSourceLookup jndi = new JndiDataSourceLookup();
		try {
			dataSource = (DataSource) jndi.getDataSource("jdbc/paceDataSource");
		} catch (Exception e) {
			LogUtils.error("Could not find DataSource jdbc/paceDataSource:", e);
		}
		return dataSource;
	}*/

	@Bean 
	PersistenceAnnotationBeanPostProcessor persistenceAnnotationBeanPostProcessor(){
		return new PersistenceAnnotationBeanPostProcessor();
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
		return new PropertySourcesPlaceholderConfigurer();
	}

}
