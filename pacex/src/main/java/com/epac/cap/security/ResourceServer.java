package com.epac.cap.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.token.TokenStore;



@Configuration
@EnableResourceServer
public class ResourceServer extends ResourceServerConfigurerAdapter {
	private static final String RESOURCE_ID = "pacex";

	@Autowired
	private TokenStore tokenStore;

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
		resources.tokenStore(tokenStore).resourceId(RESOURCE_ID).stateless(true);
	}


	@Override
	public void configure(HttpSecurity http) throws Exception {
		http
			.anonymous().disable()
			.authorizeRequests()
				.anyRequest().authenticated()
			.and()
			.exceptionHandling().accessDeniedHandler(new OAuth2AccessDeniedHandler());			
	}

}
