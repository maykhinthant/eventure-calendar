package com.eventure.calendar_app.config;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.eventure.calendar_app.auth.service.JWTService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    
    @Autowired
    private JWTService jwtService;

    // The final frontend redirect URI where you want the browser to land with the token.
    // Keep in application.properties / application.yml and inject it here.
    @Value("${app.oauth2.authorized-redirect-uri:http://localhost:5173/oauth2/redirect}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        // The authenticated principal will be an OAuth2User (from our CustomOAuth2UserService)
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        // We prefer to use our 'username' attribute (we put it in CustomOAuth2UserService)
        String username = Optional.ofNullable((String) oauthUser.getAttributes().get("username"))
                        .orElse((String) oauthUser.getName());

        String token = jwtService.generateToken(username);

        // Build a redirect URL and append token as query parameter (app.oauth2.authorized-redirect-uri + ?token=...)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build().toUriString();

        // Redirect with 302 Found
        response.setStatus(HttpStatus.FOUND.value());
        response.sendRedirect(targetUrl);
    }
}
