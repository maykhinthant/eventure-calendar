package com.eventure.calendar_app.event.repo;

import com.eventure.calendar_app.calendar.model.Calendars;
import com.eventure.calendar_app.event.model.Events;
import com.eventure.calendar_app.user.model.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class EventRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventRepo eventRepo;

    private Users testUser;
    private Users anotherUser;
    private Calendars testCalendar;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser = new Users();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser = entityManager.persist(testUser);

        anotherUser = new Users();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setName("Another User");
        anotherUser = entityManager.persist(anotherUser);

        // Create a test calendar with required fields
        testCalendar = new Calendars();
        testCalendar.setName("Test Calendar");
        testCalendar.setColor("#FFFFFF");
        testCalendar.setOwner(testUser);  // Set the owner to testUser
        testCalendar = entityManager.persist(testCalendar);
        
        // Flush to ensure all entities are persisted
        entityManager.flush();
    }

    @Test
    void findByCreatedBy_Username_WhenUserHasEvents_ShouldReturnEvents() {
        // Given
        Events event1 = createTestEvent("Test Event 1", testUser, testCalendar, false);
        Events event2 = createTestEvent("Test Event 2", testUser, testCalendar, true);
        entityManager.persist(event1);
        entityManager.persist(event2);
        entityManager.flush();

        // When
        List<Events> foundEvents = eventRepo.findByCreatedBy_Username(testUser.getUsername());

        // Then
        assertThat(foundEvents).hasSize(2);
        assertThat(foundEvents).extracting(Events::getTitle)
                .containsExactlyInAnyOrder("Test Event 1", "Test Event 2");
    }

    @Test
    void findByCreatedBy_Username_WhenUserHasNoEvents_ShouldReturnEmptyList() {
        // Given - No events for testUser
        Events otherUserEvent = createTestEvent("Other User Event", anotherUser, testCalendar, false);
        entityManager.persist(otherUserEvent);
        entityManager.flush();

        // When
        List<Events> foundEvents = eventRepo.findByCreatedBy_Username(testUser.getUsername());

        // Then
        assertThat(foundEvents).isEmpty();
    }

    @Test
    void findByCreatedBy_Username_WhenUsernameDoesNotExist_ShouldReturnEmptyList() {
        // Given - No users or events in the database
        entityManager.clear();

        // When
        List<Events> foundEvents = eventRepo.findByCreatedBy_Username("nonexistent");

        // Then
        assertThat(foundEvents).isEmpty();
    }

    @Test
    void findByCreatedBy_Username_WhenMultipleUsersHaveEvents_ShouldReturnOnlyRequestedUsersEvents() {
        // Given
        Events event1 = createTestEvent("User1 Event", testUser, testCalendar, false);
        Events event2 = createTestEvent("User2 Event", anotherUser, testCalendar, true);
        entityManager.persist(event1);
        entityManager.persist(event2);
        entityManager.flush();

        // When
        List<Events> foundEvents = eventRepo.findByCreatedBy_Username(testUser.getUsername());

        // Then
        assertThat(foundEvents).hasSize(1);
        assertThat(foundEvents.get(0).getTitle()).isEqualTo("User1 Event");
    }

    @Test
    void findByCreatedBy_Username_WhenEventsHaveDifferentProperties_ShouldReturnAllMatchingUsername() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        Events recurringEvent = createTestEvent("Recurring Event", testUser, testCalendar, true);
        recurringEvent.setRecurrenceRule("FREQ=WEEKLY;COUNT=5");
        recurringEvent.setRecurrenceEndDate(now.plusWeeks(5));
        
        Events completedEvent = createTestEvent("Completed Event", testUser, testCalendar, false);
        completedEvent.setCompleted(true);
        
        entityManager.persist(recurringEvent);
        entityManager.persist(completedEvent);
        entityManager.flush();

        // When
        List<Events> foundEvents = eventRepo.findByCreatedBy_Username(testUser.getUsername());

        // Then
        assertThat(foundEvents).hasSize(2);
        assertThat(foundEvents).extracting(Events::getTitle)
                .containsExactlyInAnyOrder("Recurring Event", "Completed Event");
    }

    private Events createTestEvent(String title, Users user, Calendars calendar, boolean isRecurring) {
        LocalDateTime now = LocalDateTime.now();
        Events event = new Events();
        event.setTitle(title);
        event.setStartTime(now);
        event.setEndTime(now.plusHours(1));
        event.setCompleted(false);
        event.setCreatedBy(user);
        event.setCalendar(calendar);
        event.setIsRecurring(isRecurring);
        return event;
    }
}
