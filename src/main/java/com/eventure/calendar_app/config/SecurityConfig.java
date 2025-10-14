package com.eventure.calendar_app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	// CONFIGURE OUR OWN AUTHENTICATION PROVIDER.

	// Create user details (username, password, roles etc) and store in Postgresql
	// Pass our own userDetailsService instead of using the default one (using a service that implements UserDetailsService)

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private jwtFilter jwtFilter;
	
	@Bean
	public AuthenticationProvider authProvider() {
		DaoAuthenticationProvider provider=new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManager (AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
					.csrf(customizer -> customizer.disable())
					.authorizeHttpRequests(request -> request
							.requestMatchers("/api/users/register", "/api/users/login")	// Don't authorize the register and login page
							.permitAll()							// But permit authorization for any other requests
							.anyRequest().authenticated())
					.httpBasic(Customizer.withDefaults())
					.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

					// when we use Jwt, we have 2 filters in the chain (jwtfilter, UsernamePasswordAuthenticationfilter)
					// We are saying "hey use jwtfilter before UsernamePasswordAuthenticationfilter"
					.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
					.build();
	}

}
