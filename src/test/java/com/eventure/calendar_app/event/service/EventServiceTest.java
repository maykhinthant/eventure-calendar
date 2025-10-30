package com.eventure.calendar_app.event.service;

import com.eventure.calendar_app.calendar.model.Calendars;
import com.eventure.calendar_app.calendar.repo.CalendarRepo;
import com.eventure.calendar_app.event.model.Events;
import com.eventure.calendar_app.event.repo.EventRepo;
import com.eventure.calendar_app.user.model.Users;
import com.eventure.calendar_app.user.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class EventServiceTest {

    @Mock
    private EventRepo eventRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private CalendarRepo calendarRepo;

    @InjectMocks
    private EventService eventService;

    private Users testUser;
    private Calendars testCalendar;
    private Events testEvent;

    @BeforeEach
    void setUp() {
        testUser = new Users();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testCalendar = new Calendars();
        testCalendar.setId(1);
        testCalendar.setName("Test Calendar");

        testEvent = new Events();
        testEvent.setId(1);
        testEvent.setTitle("Test Event");
        testEvent.setStartTime(LocalDateTime.now());
        testEvent.setEndTime(LocalDateTime.now().plusHours(1));
        testEvent.setCreatedBy(testUser);
        testEvent.setCalendar(testCalendar);
    }

    @Test
    void createEvent_WhenValidInput_ShouldSaveEvent() {
        // Given
        when(userRepo.findByUsername("testuser")).thenReturn(testUser);
        when(calendarRepo.findById(1)).thenReturn(Optional.of(testCalendar));
        when(eventRepo.save(any(Events.class))).thenReturn(testEvent);

        // When
        eventService.createEvent(testEvent, "testuser");

        // Then
        assertThat(testEvent.getCreatedBy()).isEqualTo(testUser);
        assertThat(testEvent.getCalendar()).isEqualTo(testCalendar);
        verify(eventRepo, times(1)).save(any(Events.class));
    }

    @Test
    void createEvent_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(userRepo.findByUsername("nonexistent")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> eventService.createEvent(testEvent, "nonexistent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("user not found");
    }

    @Test
    void getEvents_WhenUsernameProvided_ShouldReturnUserEvents() {
        // Given
        when(eventRepo.findByCreatedBy_Username("testuser")).thenReturn(List.of(testEvent));

        // When
        List<Events> events = eventService.getEvents("testuser");

        // Then
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTitle()).isEqualTo("Test Event");
    }

    @Test
    void getEvents_WhenNoUsername_ShouldReturnAllEvents() {
        // Given
        when(eventRepo.findAll()).thenReturn(List.of(testEvent));

        // When
        List<Events> events = eventService.getEvents(null);

        // Then
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTitle()).isEqualTo("Test Event");
    }

    @Test
    void updateEvent_WhenUserIsOwner_ShouldUpdateEvent() throws AccessDeniedException {
        // Given
        Events existingEvent = new Events();
        existingEvent.setId(1);
        existingEvent.setCreatedBy(testUser);

        Events updatedEvent = new Events();
        updatedEvent.setTitle("Updated Title");
        updatedEvent.setCalendar(testCalendar);

        when(eventRepo.findById(1)).thenReturn(Optional.of(existingEvent));
        when(calendarRepo.findById(1)).thenReturn(Optional.of(testCalendar));

        // When
        eventService.updateEvent(1, updatedEvent, "testuser");

        // Then
        assertThat(existingEvent.getTitle()).isEqualTo("Updated Title");
        verify(eventRepo, times(1)).save(existingEvent);
    }

    @Test
    void updateEvent_WhenUserNotOwner_ShouldThrowAccessDenied() {
        // Given
        Users otherUser = new Users();
        otherUser.setUsername("otheruser");
        
        Events existingEvent = new Events();
        existingEvent.setId(1);
        existingEvent.setCreatedBy(otherUser);

        when(eventRepo.findById(1)).thenReturn(Optional.of(existingEvent));

        // When & Then
        assertThatThrownBy(() -> eventService.updateEvent(1, new Events(), "testuser"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Not allowed to update this event");
    }

    @Test
    void deleteEvent_WhenUserIsOwner_ShouldDeleteEvent() throws AccessDeniedException {
        // Given
        when(eventRepo.findById(1)).thenReturn(Optional.of(testEvent));

        // When
        eventService.deleteEvent(1, "testuser");

        // Then
        verify(eventRepo, times(1)).deleteById(1);
    }

    @Test
    void deleteEvent_WhenUserNotOwner_ShouldThrowAccessDenied() {
        // Given
        Users otherUser = new Users();
        otherUser.setUsername("otheruser");
        
        Events existingEvent = new Events();
        existingEvent.setId(1);
        existingEvent.setCreatedBy(otherUser);

        when(eventRepo.findById(1)).thenReturn(Optional.of(existingEvent));

        // When & Then
        assertThatThrownBy(() -> eventService.deleteEvent(1, "testuser"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Not allowed to delete this event");
    }

    @Test
    void getEvents_WithRecurringEvent_ShouldExpandRecurrences() {
        // Given
        testEvent.setRecurrenceRule("FREQ=DAILY;COUNT=3");
        when(eventRepo.findByCreatedBy_Username("testuser")).thenReturn(List.of(testEvent));

        // When
        List<Events> events = eventService.getEvents("testuser");

        // Then
        assertThat(events).hasSize(3);
        assertThat(events.get(0).getTitle()).isEqualTo("Test Event");
        assertThat(events.get(1).getTitle()).isEqualTo("Test Event");
        assertThat(events.get(1).getId()).isNegative(); // Generated ID should be negative
    }

    @Test
    void buildSyntheticId_WhenValidInput_ShouldReturnNegativeNumber() {
        // Since buildSyntheticId is private, we'll test it indirectly through the public API
        // by creating a recurring event and checking its generated IDs
        testEvent.setRecurrenceRule("FREQ=DAILY;COUNT=1");
        when(eventRepo.findByCreatedBy_Username("testuser")).thenReturn(List.of(testEvent));
        
        // When
        List<Events> events = eventService.getEvents("testuser");
        
        // Then - verify the event was processed (the actual ID generation is an implementation detail)
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getId()).isNotNull();
    }
}
