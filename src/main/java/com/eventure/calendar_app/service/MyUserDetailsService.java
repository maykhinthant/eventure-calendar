package com.eventure.calendar_app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.eventure.calendar_app.model.UserPrincipal;
import com.eventure.calendar_app.model.Users;
import com.eventure.calendar_app.repo.UserRepo;

@Service
public class MyUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepo repo; 

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Users user = repo.findByUsername(username);

		if(user == null) {
			System.out.println("USer not found");
			throw new UsernameNotFoundException("User not found");
		}

		return new UserPrincipal(user);
	}
	
}