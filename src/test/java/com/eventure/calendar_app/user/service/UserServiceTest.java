package com.eventure.calendar_app.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.eventure.calendar_app.auth.service.JWTService;
import com.eventure.calendar_app.user.model.Users;
import com.eventure.calendar_app.user.repo.UserRepo;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private JWTService jwtService;

    @Mock
    private AuthenticationManager authManager;

    @InjectMocks
    private UserService userService;

    private Users testUser;
    private final String rawPassword = "testPassword123";
    private final String encodedPassword = "$2a$12$somehashedpassword";
    private final String authToken = "test.jwt.token";

    @BeforeEach
    void setUp() {
        testUser = new Users();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword(rawPassword);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
    }

    @Test
    void register_ShouldEncodePasswordAndSaveUser() {
        // Arrange
        when(userRepo.save(any(Users.class))).thenAnswer(invocation -> {
            Users user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // Act
        Users savedUser = userService.register(testUser);

        // Assert
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getPassword())
            .isNotBlank()
            .isNotEqualTo(rawPassword)
            .matches(encoded -> new BCryptPasswordEncoder().matches(rawPassword, encoded));
        verify(userRepo, times(1)).save(any(Users.class));
    }

    @Test
    void verify_WithValidCredentials_ShouldReturnToken() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(anyString())).thenReturn(authToken);

        // Act
        String result = userService.verify(testUser);

        // Assert
        assertThat(result).isEqualTo(authToken);
        verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(testUser.getUsername());
    }

    @Test
    void verify_WithInvalidCredentials_ShouldReturnFail() {
        // Arrange
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act
        String result = userService.verify(testUser);

        // Assert
        assertThat(result).isEqualTo("fail");
        verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void verify_WhenAuthenticationFails_ShouldReturnFail() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        // Act
        String result = userService.verify(testUser);

        // Assert
        assertThat(result).isEqualTo("fail");
        verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(anyString());
    }
}
