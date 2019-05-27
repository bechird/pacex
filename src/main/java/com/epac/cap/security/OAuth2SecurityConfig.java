package com.epac.cap.security;

import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.TokenApprovalStore;
import org.springframework.security.oauth2.provider.approval.TokenStoreUserApprovalHandler;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

import com.epac.cap.config.ConfigurationConstants;
import com.epac.cap.utils.LogUtils;

@Order(99)
public class OAuth2SecurityConfig extends WebSecurityConfigurerAdapter {
	Logger logger = Logger.getLogger(getClass().getName());

	@Autowired
	private ClientDetailsService clientService;

	@Autowired
	private CustomUserDetailsService userDetailsService;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		 auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
	}


	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http
		.csrf().disable()
		.anonymous().disable()
		.authorizeRequests()
			.antMatchers("/oauth/token").authenticated();
		/*
		.and()
		.httpBasic();
		*/

	}


	@Override
	@Bean
	public AuthenticationManager authenticationManager() throws Exception {
		return super.authenticationManager();
	}
	

	
	@Bean
	@Primary
	public DefaultTokenServices tokenServices() {
		DefaultTokenServices tokenServices = new DefaultTokenServices();
		tokenServices.setSupportRefreshToken(true);
		tokenServices.setClientDetailsService(new JdbcClientDetailsService(dataSource()));
		tokenServices.setTokenStore(new JdbcTokenStore(dataSource()));
		tokenServices.setAccessTokenValiditySeconds(60);
		return tokenServices;
	}

	@Bean
	@Primary
	public ResourceBundleMessageSource messageSource(){
		
		ResourceBundleMessageSource messages = new ResourceBundleMessageSource();
		messages.addBasenames("messages");
		messages.setDefaultEncoding("UTF-8");
		//messages.setFileEncodings();
		return messages;
	}
	
	public FixedLocaleResolver localeResolver(){
		FixedLocaleResolver resolver = new FixedLocaleResolver();
		return resolver;
	}
	
	
	@Bean
	@Autowired
	public TokenStoreUserApprovalHandler userApprovalHandler(TokenStore tokenStore) {
		TokenStoreUserApprovalHandler handler = new TokenStoreUserApprovalHandler();
		handler.setTokenStore(tokenStore);
		handler.setRequestFactory(new DefaultOAuth2RequestFactory(clientService));
		handler.setClientDetailsService(clientService);
		return handler;
	}

	@Bean
	@Autowired
	public ApprovalStore approvalStore(TokenStore tokenStore) throws Exception {
		TokenApprovalStore store = new TokenApprovalStore();
		store.setTokenStore(tokenStore);
		return store;
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
	
	@Value("classpath:oauth_tables_20171018.sql")
	private Resource oauthTablesScript;
	 
	@Bean
	public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
	    DataSourceInitializer initializer = new DataSourceInitializer();
	    initializer.setDataSource(dataSource);
	    initializer.setDatabasePopulator(databasePopulator());
	    return initializer;
	}
	 
	private DatabasePopulator databasePopulator() {
	    ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
	    populator.addScript(oauthTablesScript);
	    return populator;
	}

	
}
