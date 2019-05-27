package com.epac.cap.security;

import org.springframework.security.core.authority.AuthorityUtils;

import com.epac.cap.model.User;

public class UserRepositoryUserDetails extends org.springframework.security.core.userdetails.User {

	private static final long serialVersionUID = 1L;
	
	private User user;

	public UserRepositoryUserDetails(User user) {
		super(user.getLoginName(), user.getLoginPassword(), AuthorityUtils.createAuthorityList(user.getRolesString()));
		
		this.user = user;
	}

	public User getUser() {
		return user;
	}	

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return user.getActiveFlag();
	}
	
}