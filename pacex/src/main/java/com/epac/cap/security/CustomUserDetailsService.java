package com.epac.cap.security;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.epac.cap.common.PersistenceException;
import com.epac.cap.handler.UserHandler;
import com.epac.cap.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	
	@Autowired
	private UserHandler userHandler;
	
	private Logger logger = Logger.getLogger(this.getClass());



	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		
		try {
			com.epac.cap.model.User user = userHandler.loadUserByUserName(userName);
			
			if(user == null) {
				throw new UsernameNotFoundException(userName);
			}
			
			return new UserRepositoryUserDetails(user);
		} catch (PersistenceException e) {
			throw new UsernameNotFoundException(userName);
		}
		
		
	}

	

}