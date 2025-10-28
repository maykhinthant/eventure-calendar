package com.eventure.calendar_app.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JWTServiceTest {

    private final JWTService jwtService = new JWTService();

    @Test
    void testGenerateToken() {
        String token = jwtService.generateToken("alice@example.com");

        assertThat(token).isNotBlank();
        assertThat(token).contains(".");
    }

    @Test
    void testExtractUserName() {
        String username = "alice@example.com";
        String token = jwtService.generateToken(username);

        String extractedUserName = jwtService.extractUserName(token);

        assertThat(extractedUserName).isEqualTo(username);
    }

    @Test
    void testValidateToken() {
        String username = "alice@example.com";
        String token = jwtService.generateToken(username);
        UserDetails matchingUser = User.withUsername(username)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        UserDetails otherUser = User.withUsername("bob@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        assertThat(jwtService.validateToken(token, matchingUser)).isTrue();
        assertThat(jwtService.validateToken(token, otherUser)).isFalse();
    }
}
