package com.eventure.calendar_app.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.eventure.calendar_app.user.model.UserPrincipal;
import com.eventure.calendar_app.user.model.Users;
import com.eventure.calendar_app.user.repo.UserRepo;

@ExtendWith(MockitoExtension.class)
class MyUserDetailsServiceTest {

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private MyUserDetailsService myUserDetailsService;

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserPrincipal() {
        Users existingUser = new Users();
        existingUser.setUsername("jane.doe");

        when(userRepo.findByUsername("jane.doe")).thenReturn(existingUser);

        UserDetails result = myUserDetailsService.loadUserByUsername("jane.doe");

        assertThat(result)
            .isInstanceOf(UserPrincipal.class)
            .extracting(UserDetails::getUsername)
            .isEqualTo("jane.doe");
        verify(userRepo).findByUsername("jane.doe");
    }

    @Test
    void loadUserByUsername_WhenUserMissing_ShouldThrowException() {
        when(userRepo.findByUsername("missing"))
            .thenReturn(null);

        assertThatThrownBy(() -> myUserDetailsService.loadUserByUsername("missing"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessage("User not found");
        verify(userRepo).findByUsername("missing");
    }
}
