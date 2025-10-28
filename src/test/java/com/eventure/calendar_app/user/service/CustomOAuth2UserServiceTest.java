package com.eventure.calendar_app.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.Builder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.eventure.calendar_app.auth.service.JWTService;
import com.eventure.calendar_app.user.model.Users;
import com.eventure.calendar_app.user.repo.UserRepo;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private JWTService jwtService;

    private CustomOAuth2UserService service;

    @BeforeEach
    void setUp() {
        service = org.mockito.Mockito.spy(new CustomOAuth2UserService(userRepo, jwtService));
    }

    @Test
    void loadUser_createsNewUserWhenNotFound() throws OAuth2AuthenticationException {
        OAuth2UserRequest userRequest = buildUserRequest("google");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "new.user@example.com");
        attributes.put("sub", "provider-id");
        attributes.put("name", "New User");

        OAuth2User oauth2User = org.mockito.Mockito.mock(OAuth2User.class);
        doReturn(attributes).when(oauth2User).getAttributes();

        doReturn(null).when(userRepo).findByUsername("google_provider-id");
        doReturn(null).when(userRepo).findByEmail("new.user@example.com");
        doReturn(oauth2User).when(service).fetchOAuth2User(userRequest);

        doAnswer(invocation -> {
            Users toSave = invocation.getArgument(0);
            toSave.setId(42L);
            return toSave;
        }).when(userRepo).save(any(Users.class));

        var result = service.loadUser(userRequest);

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(userRepo).save(captor.capture());

        Users saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("google_provider-id");
        assertThat(saved.getEmail()).isEqualTo("new.user@example.com");
        assertThat(saved.getName()).isEqualTo("New User");
        assertThat(saved.getProvider()).isEqualTo("google");
        assertThat(saved.getProviderId()).isEqualTo("provider-id");

        assertThat(result.getAttributes())
                .containsEntry("appUserId", 42L)
                .containsEntry("username", "google_provider-id")
                .containsEntry("email", "new.user@example.com")
                .containsEntry("name", "New User");
    }

    @Test
    void loadUser_updatesExistingUserWhenAttributesChange() throws OAuth2AuthenticationException {
        OAuth2UserRequest userRequest = buildUserRequest("google");

        Users existing = new Users();
        existing.setId(10L);
        existing.setUsername("google_provider-id");
        existing.setEmail("old.email@example.com");
        existing.setName("Old Name");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "updated.email@example.com");
        attributes.put("sub", "provider-id");
        attributes.put("name", "Updated Name");

        OAuth2User oauth2User = org.mockito.Mockito.mock(OAuth2User.class);
        doReturn(attributes).when(oauth2User).getAttributes();

        doReturn(existing).when(userRepo).findByUsername("google_provider-id");
        doReturn(oauth2User).when(service).fetchOAuth2User(userRequest);
        doReturn(existing).when(userRepo).save(existing);

        var result = service.loadUser(userRequest);

        verify(userRepo).save(existing);
        assertThat(existing.getEmail()).isEqualTo("updated.email@example.com");
        assertThat(existing.getName()).isEqualTo("Updated Name");

        assertThat(result.getAttributes())
                .containsEntry("appUserId", 10L)
                .containsEntry("username", "google_provider-id")
                .containsEntry("email", "updated.email@example.com")
                .containsEntry("name", "Updated Name");
    }

    private OAuth2UserRequest buildUserRequest(String registrationId) {
        Builder builder = ClientRegistration.withRegistrationId(registrationId)
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://example.com/login/oauth2/code")
                .scope("openid", "profile", "email")
                .authorizationUri("https://example.com/oauth2/authorize")
                .tokenUri("https://example.com/oauth2/token")
                .userInfoUri("https://example.com/userinfo")
                .userNameAttributeName("sub")
                .clientName("Example");

        ClientRegistration registration = builder.build();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(TokenType.BEARER, "token-value", Instant.now(), Instant.now().plusSeconds(60));
        return new OAuth2UserRequest(registration, accessToken);
    }
}
