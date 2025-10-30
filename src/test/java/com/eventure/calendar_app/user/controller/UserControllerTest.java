package com.eventure.calendar_app.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.eventure.calendar_app.config.jwtFilter;
import com.eventure.calendar_app.testconfig.TestSecurityConfig;
import com.eventure.calendar_app.user.model.Users;
import com.eventure.calendar_app.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(value = UserController.class, 
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = jwtFilter.class))
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
    }

    @Test
    void register_WhenValidUserProvided_ShouldReturnPersistedUser() throws Exception {
        // Arrange
        Users requestUser = new Users();
        requestUser.setUsername("alice");
        requestUser.setPassword("password123");
        requestUser.setEmail("alice@example.com");
        requestUser.setName("Alice Wonderland");

        Users persistedUser = new Users();
        persistedUser.setId(1L);
        persistedUser.setUsername("alice");
        persistedUser.setPassword("encodedPassword");
        persistedUser.setEmail("alice@example.com");
        persistedUser.setName("Alice Wonderland");

        when(userService.register(any(Users.class))).thenReturn(persistedUser);

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestUser)))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(persistedUser)));

        verify(userService).register(argThat(user ->
                "alice".equals(user.getUsername()) &&
                "password123".equals(user.getPassword()) &&
                "alice@example.com".equals(user.getEmail()) &&
                "Alice Wonderland".equals(user.getName())));
    }

    @Test
    void login_WhenValidCredentialsProvided_ShouldReturnJwtToken() throws Exception {
        // Arrange
        Users requestUser = new Users();
        requestUser.setUsername("jane.doe");
        requestUser.setPassword("super-secret");

        when(userService.verify(any(Users.class))).thenReturn("jwt-token");

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestUser)))
            .andExpect(status().isOk())
            .andExpect(content().string("jwt-token"));

        verify(userService).verify(argThat(user ->
                "jane.doe".equals(user.getUsername()) && "super-secret".equals(user.getPassword())));
    }

    @Test
    void login_WhenInvalidCredentialsProvided_ShouldReturnFail() throws Exception {
        // Arrange
        Users requestUser = new Users();
        requestUser.setUsername("john.doe");
        requestUser.setPassword("bad-password");

        when(userService.verify(any(Users.class))).thenReturn("fail");

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestUser)))
            .andExpect(status().isOk())
            .andExpect(content().string("fail"));

        verify(userService).verify(argThat(user ->
                "john.doe".equals(user.getUsername()) && "bad-password".equals(user.getPassword())));
    }
}
