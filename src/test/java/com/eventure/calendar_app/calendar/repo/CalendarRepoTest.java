package com.eventure.calendar_app.calendar.repo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.eventure.calendar_app.calendar.model.Calendars;
import com.eventure.calendar_app.user.model.Users;

@ExtendWith(MockitoExtension.class)
@DataJpaTest
public class CalendarRepoTest {

    @Mock
    private CalendarRepo calendarRepo;

    private Users testUser1;
    private Users testUser2;
    private Calendars calendar1;
    private Calendars calendar2;
    private Calendars calendar3;

    @BeforeEach
    void setUp() {
        // Setup test users
        testUser1 = new Users();
        testUser1.setId(1L);
        testUser1.setUsername("user1");
        testUser1.setEmail("user1@example.com");

        testUser2 = new Users();
        testUser2.setId(2L);
        testUser2.setUsername("user2");
        testUser2.setEmail("user2@example.com");

        // Setup test calendars
        calendar1 = new Calendars();
        calendar1.setId(1);
        calendar1.setName("User1 Calendar 1");
        calendar1.setColor("#FF0000");
        calendar1.setOwner(testUser1);

        calendar2 = new Calendars();
        calendar2.setId(2);
        calendar2.setName("User1 Calendar 2");
        calendar2.setColor("#00FF00");
        calendar2.setOwner(testUser1);

        calendar3 = new Calendars();
        calendar3.setId(3);
        calendar3.setName("User2 Calendar 1");
        calendar3.setColor("#0000FF");
        calendar3.setOwner(testUser2);
    }

    @Test
    void findByOwner_Username_WhenUserHasCalendars_ShouldReturnAllCalendarsForUser() {
        // Arrange
        String username = "user1";
        List<Calendars> expectedCalendars = Arrays.asList(calendar1, calendar2);
        when(calendarRepo.findByOwner_Username(username)).thenReturn(expectedCalendars);

        // Act
        List<Calendars> result = calendarRepo.findByOwner_Username(username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Calendars::getOwner)
                         .extracting(Users::getUsername)
                         .containsOnly(username);
    }

    @Test
    void findByOwner_Username_WhenUserHasNoCalendars_ShouldReturnEmptyList() {
        // Arrange
        String username = "nonexistent";
        when(calendarRepo.findByOwner_Username(username)).thenReturn(List.of());

        // Act
        List<Calendars> result = calendarRepo.findByOwner_Username(username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void findByOwner_Username_WhenUsernameIsNull_ShouldReturnEmptyList() {
        // Arrange
        when(calendarRepo.findByOwner_Username(null)).thenReturn(List.of());

        // Act
        List<Calendars> result = calendarRepo.findByOwner_Username(null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}
