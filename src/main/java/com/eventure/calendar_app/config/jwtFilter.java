package com.eventure.calendar_app.config;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.eventure.calendar_app.service.JWTService;
import com.eventure.calendar_app.service.MyUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class jwtFilter extends OncePerRequestFilter{	

	@Autowired
	private JWTService jwtService;

	@Autowired
	ApplicationContext context;

	@Override
	protected void doFilterInternal(
			@org.springframework.lang.NonNull HttpServletRequest request,
			@org.springframework.lang.NonNull HttpServletResponse response,
			@org.springframework.lang.NonNull FilterChain filterChain)
			throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");
		String token = null;
		String username = null;

		// Extract the header, the username and the token
		if(authHeader != null && authHeader.startsWith("Bearer ")) {
			token = authHeader.substring(7);
			username = jwtService.extractUserName(token);
		}

		// Authentication must be null because the token isn't validated yet and we want it to be null
		if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = context.getBean(MyUserDetailsService.class).loadUserByUsername(username);

			// Validate jwt token
			if(jwtService.validateToken(token, userDetails)) {

				// After the jwt filter, we wanna pass the token to the UsernamePasswordAuthenticationFilter
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				
				// Make the filter known of the request
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				// Set the authentication using the token
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}

		filterChain.doFilter(request, response);
	}
}
