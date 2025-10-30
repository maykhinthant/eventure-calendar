package com.eventure.calendar_app.calendar.repo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.eventure.calendar_app.calendar.model.Calendars;
import com.eventure.calendar_app.user.model.Users;

@DataJpaTest
public class CalendarRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
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
        testUser1.setUsername("user1");
        testUser1.setEmail("user1@example.com");
        testUser1.setPassword("password1");
        entityManager.persist(testUser1);

        testUser2 = new Users();
        testUser2.setUsername("user2");
        testUser2.setEmail("user2@example.com");
        testUser2.setPassword("password2");
        entityManager.persist(testUser2);

        // Setup test calendars
        calendar1 = new Calendars();
        calendar1.setName("User1 Calendar 1");
        calendar1.setColor("#FF0000");
        calendar1.setOwner(testUser1);
        entityManager.persist(calendar1);

        calendar2 = new Calendars();
        calendar2.setName("User1 Calendar 2");
        calendar2.setColor("#00FF00");
        calendar2.setOwner(testUser1);
        entityManager.persist(calendar2);

        calendar3 = new Calendars();
        calendar3.setName("User2 Calendar 1");
        calendar3.setColor("#0000FF");
        calendar3.setOwner(testUser2);
        entityManager.persist(calendar3);

        entityManager.flush();
    }

    @Test
    void findByOwner_Username_WhenUserHasCalendars_ShouldReturnAllCalendarsForUser() {
        // Act
        List<Calendars> result = calendarRepo.findByOwner_Username("user1");

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result)
            .extracting(Calendars::getName)
            .containsExactlyInAnyOrder("User1 Calendar 1", "User1 Calendar 2");
        assertThat(result)
            .extracting(c -> c.getOwner().getUsername())
            .containsOnly("user1");
    }

    @Test
    void findByOwner_Username_WhenUserHasNoCalendars_ShouldReturnEmptyList() {
        // Act
        List<Calendars> result = calendarRepo.findByOwner_Username("nonexistent");

        // Assert
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void findByOwner_Username_WhenUsernameIsNull_ShouldReturnEmptyList() {
        // Act
        List<Calendars> result = calendarRepo.findByOwner_Username(null);

        // Assert
        assertThat(result).isNotNull().isEmpty();
    }
}
