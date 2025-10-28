package com.eventure.calendar_app.user.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.eventure.calendar_app.auth.service.JWTService;
import com.eventure.calendar_app.user.model.Users;
import com.eventure.calendar_app.user.repo.UserRepo;

/**
 * CustomOAuth2UserService
 * - Called by Spring Security when an OAuth2 user is authenticated by the provider.
 * - Purpose: map provider attributes -> our application's Users entity (create or update),
 *            then return an OAuth2User that Spring Security will put in the SecurityContext.
 */

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final UserRepo userRepo;

    private final JWTService jwtService;

    public CustomOAuth2UserService(UserRepo userRepo, JWTService jwtService) {
        this.userRepo = userRepo;
        this.jwtService = jwtService;
    }

    /**
     * loadUser:
     * - Called after OAuth2 provider has returned user info.
     * - We use the DefaultOAuth2UserService to fetch user attributes then map them.
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Fetch attributes from provider (default behavior)
        OAuth2User oauth2User = fetchOAuth2User(userRequest);

        // provider id: "google", "github", etc.
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // provider attributes map (these keys vary by provider)
        Map<String, Object> attributes = oauth2User.getAttributes();

        // Resolve an email (Google returns "email", GitHub may require extra calls / check "email" or "login")
        String email = resolveEmail(registrationId, attributes);

        // Resolve provider-specific id (sub for Google, id for GitHub)
        String providerId = resolveProviderId(registrationId, attributes);

        // Build a unique username in the system (e.g., provider + providerId)
        String username = registrationId + "_" + providerId;

        // Try to find existing user in DB by username or email
        Users user = userRepo.findByUsername(username);

        // Try by email if username not found (user may have registered with email previously)
        if(user == null && email != null) {
            user = userRepo.findByEmail(email);
        }

        if(user == null) {
            // Create new user
            user = new Users();
            user.setUsername(username);
            user.setEmail(email);
            user.setProvider(registrationId);
            user.setProviderId(providerId);
            user.setName(resolveName(registrationId, attributes));
            // Generate a random password or mark as oauth-only
            user.setPassword("OAUTH2_USER"); // not used for oauth logins
            user.setRoles("ROLE_USER");
            userRepo.save(user);
        } else {
            // Update existing user info (e.g., name changed at provider)
            boolean changed = false;
            String name = resolveName(registrationId, attributes);

            if(name != null && !name.equals(user.getName())) {
                user.setName(name);
                changed = true;
            }

            if(email != null && !email.equals(user.getEmail())) {
                user.setEmail(email);
                changed = true;
            }

            if(changed) {
                userRepo.save(user);
            }
        }

        // Create authorities for Spring Security
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");

        // Add the DB id and username to attributes so the app can read them from OAuth2User
        // (You can add any extra fields the frontend will need)
        // Create a map combining provider attributes plus our own
        // NOTE: DefaultOAuth2User will expose attributes via oauth2User.getAttributes()
        // Keep provider attributes but include our user id and username
        Map<String, Object> mappedAttributes = new HashMap<>(attributes);
        mappedAttributes.put("appUserId", user.getId());
        mappedAttributes.put("username", user.getUsername());
        mappedAttributes.put("name", user.getName());
        mappedAttributes.put("email", user.getEmail());

        return new DefaultOAuth2User(Collections.singleton(authority), mappedAttributes, "email");
    }

    protected OAuth2User fetchOAuth2User(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        return super.loadUser(userRequest);
    }

    // Helper to find an email in provider-specific attributes
    private String resolveEmail(String registrationId, Map<String, Object> attributes) {
        
        // Google: "email"
        if("google".equalsIgnoreCase(registrationId)) {
            Object email = attributes.get("email");
            return email != null ? email.toString() : null;
        }

        // GitHub: primary email may not be present in attributes; sometimes "email" exists
        if("github".equalsIgnoreCase(registrationId)) {
            Object email = attributes.get("email");
            if(email != null) {
                return email.toString();
            }

            // If email not present, use login as fallback
            Object login = attributes.get("login");
            return login != null ? login.toString() + "@github" : null;
        }

        // Generic fallback
        Object email = attributes.get("email");
        return email != null ? email.toString() : null;
    }

    private String resolveProviderId(String registrationId, Map<String, Object> attributes) {
        
        // Google uses "sub
        if("google".equalsIgnoreCase(registrationId)) {
            Object sub = attributes.get("sub");
            return sub != null ? sub.toString() : null;
        }

        // GitHub uses "id"
        if("github".equalsIgnoreCase(registrationId)) {
            Object id = attributes.get("id");
            return id != null ? id.toString() : null;
        }

        // Generic fallback
        Object id = attributes.get("id");
        return id != null ? id.toString() : null;
    }

    private String resolveName(String registrationId, Map<String, Object> attributes) {
        if("google".equalsIgnoreCase(registrationId)) {
            Object name = attributes.get("name");
            return name != null ? name.toString() : null;
        }

        if("github".equalsIgnoreCase(registrationId)) {
            Object name = attributes.get("name");
            if(name != null) {
                return name.toString();
            }

            // GitHub fallback
            Object login = attributes.get("login");
            return login != null ? login.toString() : null;
        }

        Object name = attributes.get("name");
        return name != null ? name.toString() : null;
    }
}
