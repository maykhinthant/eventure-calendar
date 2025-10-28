package com.eventure.calendar_app.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import com.eventure.calendar_app.auth.service.JWTService;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

    private static final String REDIRECT_URI = "http://example.com/callback";

    private OAuth2LoginSuccessHandler successHandler;

    @Mock
    private JWTService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oauth2User;

    @BeforeEach
    void setUp() {
        successHandler = new OAuth2LoginSuccessHandler();
        ReflectionTestUtils.setField(successHandler, "jwtService", jwtService);
        ReflectionTestUtils.setField(successHandler, "redirectUri", REDIRECT_URI);
    }

    @Test
    void shouldRedirectWithTokenUsingCustomUsernameAttribute() throws ServletException, IOException {
        String username = "alice";
        String token = "jwt-token";

        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttributes()).thenReturn(Map.of("username", username));
        when(oauth2User.getName()).thenReturn("principal-name");
        when(jwtService.generateToken(username)).thenReturn(token);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        verify(jwtService).generateToken(usernameCaptor.capture());
        assertThat(usernameCaptor.getValue()).isEqualTo(username);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue()).isEqualTo(REDIRECT_URI + "?token=" + token);
        verify(response).setStatus(HttpStatus.FOUND.value());
    }

    @Test
    void shouldFallbackToPrincipalNameWhenUsernameAttributeMissing() throws ServletException, IOException {
        String principalName = "bob";
        String token = "jwt-token-bob";

        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttributes()).thenReturn(Map.of());
        when(oauth2User.getName()).thenReturn(principalName);
        when(jwtService.generateToken(principalName)).thenReturn(token);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        verify(jwtService).generateToken(usernameCaptor.capture());
        assertThat(usernameCaptor.getValue()).isEqualTo(principalName);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue()).isEqualTo(REDIRECT_URI + "?token=" + token);
        verify(response).setStatus(HttpStatus.FOUND.value());
    }
}
