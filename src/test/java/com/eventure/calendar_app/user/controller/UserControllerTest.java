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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.eventure.calendar_app.user.model.Users;
import com.eventure.calendar_app.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void login_WhenServiceReturnsToken_ShouldRespondWithToken() throws Exception {
        Users requestUser = new Users();
        requestUser.setUsername("jane.doe");
        requestUser.setPassword("super-secret");

        when(userService.verify(any(Users.class))).thenReturn("jwt-token");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestUser)))
            .andExpect(status().isOk())
            .andExpect(content().string("jwt-token"));

        verify(userService).verify(argThat(user ->
                "jane.doe".equals(user.getUsername()) && "super-secret".equals(user.getPassword())));
    }

    @Test
    void login_WhenServiceReturnsFail_ShouldRespondWithFail() throws Exception {
        Users requestUser = new Users();
        requestUser.setUsername("john.doe");
        requestUser.setPassword("bad-password");

        when(userService.verify(any(Users.class))).thenReturn("fail");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestUser)))
            .andExpect(status().isOk())
            .andExpect(content().string("fail"));

        verify(userService).verify(argThat(user ->
                "john.doe".equals(user.getUsername()) && "bad-password".equals(user.getPassword())));
    }

    @Test
    void register_ShouldDelegateToServiceAndReturnPersistedUser() throws Exception {
        Users requestUser = new Users();
        requestUser.setUsername("alice");
        requestUser.setPassword("password123");
        requestUser.setEmail("alice@example.com");
        requestUser.setName("Alice Wonderland");

        Users persistedUser = new Users();
        persistedUser.setId(1L);
        persistedUser.setUsername("alice");
        persistedUser.setPassword("password123");
        persistedUser.setEmail("alice@example.com");
        persistedUser.setName("Alice Wonderland");

        when(userService.register(any(Users.class))).thenReturn(persistedUser);

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
}
