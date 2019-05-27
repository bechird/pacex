package com.epac.cap.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.epac.cap.common.AuditableBean;
import com.epac.cap.common.PersistenceException;
import com.epac.cap.handler.UserHandler;
import com.epac.cap.model.User;
import com.epac.cap.repository.UserSearchBean;
import com.epac.cap.security.UserRepositoryUserDetails;

//@Secured({ "ROLE_PM", "ROLE_ADMIN", "ROLE_LOP", "ROLE_OP", "ROLE_UESR"})
public abstract class AbstractService {
	
	@Autowired
	private UserHandler userHandler;
	
	private User user;
	
	public void doAddLogging(AuditableBean bean){
		if(bean instanceof AuditableBean){
			bean.setCreatedDate(new Date());
			bean.setCreatorId(getExecutingUserId());
		}	
	}
	
	public void doEditLogging(AuditableBean bean){
		if(bean instanceof AuditableBean){
			bean.setLastUpdateDate(new Date());
			bean.setLastUpdateId(getExecutingUserId());
		}
	}
	
	public String getExecutingUserId(){	
		
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = ((UserRepositoryUserDetails) principal).getUser();		
		return  user.getLoginName();
	}

	
	@GET
    @Path( "/currentUser" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response currentUser(@Context HttpServletRequest request) throws PersistenceException{
			
			User user=null;
			try {
				Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				user = ((UserRepositoryUserDetails) principal).getUser();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
            return Response.ok(user).build();
    }
	
	private boolean hasRole(String role) {
	  Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>)
	  SecurityContextHolder.getContext().getAuthentication().getAuthorities();
	  boolean hasRole = false;
	  for (GrantedAuthority authority : authorities) {
	     hasRole = authority.getAuthority().equals(role);
	     if (hasRole) {
	      break;
	     }
	  }
	  return hasRole;
	}

	/**
	 * @return the userHandler
	 */
	public UserHandler getUserHandler() {
		return userHandler;
	}

	/**
	 * @param userHandler the userHandler to set
	 */
	public void setUserHandler(UserHandler userHandler) {
		this.userHandler = userHandler;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
}
