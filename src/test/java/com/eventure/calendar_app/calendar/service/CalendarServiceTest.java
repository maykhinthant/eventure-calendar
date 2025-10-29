package com.eventure.calendar_app.calendar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.AccessDeniedException;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eventure.calendar_app.calendar.model.Calendars;
import com.eventure.calendar_app.calendar.repo.CalendarRepo;
import com.eventure.calendar_app.user.model.Users;
import com.eventure.calendar_app.user.repo.UserRepo;

@ExtendWith(MockitoExtension.class)
public class CalendarServiceTest {

    @Mock
    private CalendarRepo calendarRepo;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private CalendarService calendarService;

    @Test
    void createCalendar_WhenUserExists_ShouldSetOwnerAndPersist() {
        Calendars calendar = new Calendars();
        Users existingUser = new Users();
        existingUser.setUsername("jane.doe");

        when(userRepo.findByUsername("jane.doe")).thenReturn(existingUser);

        calendarService.createCalendar(calendar, "jane.doe");

        ArgumentCaptor<Calendars> calendarCaptor = ArgumentCaptor.forClass(Calendars.class);
        verify(calendarRepo).save(calendarCaptor.capture());

        Calendars savedCalendar = calendarCaptor.getValue();
        assertThat(savedCalendar).isSameAs(calendar);
        assertThat(savedCalendar.getOwner()).isEqualTo(existingUser);
    }

    @Test
    void createCalendar_WhenUserMissing_ShouldThrowIllegalArgumentException() {
        when(userRepo.findByUsername("missing")).thenReturn(null);

        assertThatThrownBy(() -> calendarService.createCalendar(new Calendars(), "missing"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("user not found: missing");

        verify(calendarRepo, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getAllCalendars_WhenUserExists_ShouldReturnCalendars() {
        // Arrange
        String username = "testuser";
        Calendars userCalendar1 = new Calendars();
        userCalendar1.setName("User Calendar 1");
        Calendars userCalendar2 = new Calendars();
        userCalendar2.setName("User Calendar 2");
        
        when(calendarRepo.findByOwner_Username(username)).thenReturn(Arrays.asList(userCalendar1, userCalendar2));

        // Act
        List<Calendars> result = calendarService.getAllCalendars(username);

        // Assert
        assertThat(result) 
            .hasSize(2)
            .extracting(Calendars::getName)
            .containsExactlyInAnyOrder("User Calendar 1", "User Calendar 2");
        
        verify(calendarRepo).findByOwner_Username(username);
        verify(calendarRepo, never()).findAll();
    }

    @Test
    void getAllCalendars_WhenUserMissing_ShouldReturnAllCalendars() {
        // Arrange
        Calendars calendar1 = new Calendars();
        calendar1.setName("Calendar 1");
        Calendars calendar2 = new Calendars();
        calendar2.setName("Calendar 2");
        
        when(calendarRepo.findAll()).thenReturn(Arrays.asList(calendar1, calendar2));

        // Act
        List<Calendars> result = calendarService.getAllCalendars(null);

        // Assert
        assertThat(result)
            .hasSize(2)
            .extracting(Calendars::getName)
            .containsExactlyInAnyOrder("Calendar 1", "Calendar 2");
        
        verify(calendarRepo, never()).findByOwner_Username(org.mockito.ArgumentMatchers.any());
        verify(calendarRepo).findAll();
    }

    @Test
    void updateCalendar_WhenCalendarExistsAndUserIsOwner_ShouldUpdateCalendar() throws Exception {
        // Arrange
        Integer calendarId = 1;
        String username = "testuser";
        
        Users owner = new Users();
        owner.setUsername(username);
        
        Calendars existingCalendar = new Calendars();
        existingCalendar.setId(calendarId);
        existingCalendar.setName("Old Name");
        existingCalendar.setColor("#FF0000");
        existingCalendar.setOwner(owner);
        
        Calendars updatedCalendar = new Calendars();
        updatedCalendar.setId(calendarId); 
        updatedCalendar.setName("New Name");
        updatedCalendar.setColor("#0000FF");
        
        when(calendarRepo.findById(calendarId)).thenReturn(java.util.Optional.of(existingCalendar));
        when(calendarRepo.save(any(Calendars.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // Act
        calendarService.updateCalendar(calendarId, updatedCalendar, username);
        
        // Assert
        verify(calendarRepo).save(existingCalendar);
        assertThat(existingCalendar.getName()).isEqualTo("New Name");
        assertThat(existingCalendar.getColor()).isEqualTo("#0000FF");
        assertThat(existingCalendar.getId()).isEqualTo(calendarId);
    }
    
    @Test
    void updateCalendar_WhenCalendarDoesNotExist_ShouldThrowIllegalArgumentException() {
        // Arrange
        Integer nonExistentId = 999;
        when(calendarRepo.findById(nonExistentId)).thenReturn(java.util.Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> calendarService.updateCalendar(nonExistentId, new Calendars(), "user1"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("calendar not found: " + nonExistentId);
            
        verify(calendarRepo, never()).save(any(Calendars.class));
    }
    
    @Test
    void updateCalendar_WhenUserIsNotOwner_ShouldThrowAccessDeniedException() {
        // Arrange
        Integer calendarId = 1;
        String ownerUsername = "owner";
        String otherUser = "otheruser";
        
        Users owner = new Users();
        owner.setUsername(ownerUsername);
        
        Calendars existingCalendar = new Calendars();
        existingCalendar.setId(calendarId);
        existingCalendar.setOwner(owner);
        
        when(calendarRepo.findById(calendarId)).thenReturn(java.util.Optional.of(existingCalendar));
        
        // Act & Assert
        assertThatThrownBy(() -> calendarService.updateCalendar(calendarId, new Calendars(), otherUser))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("Not allowed to update this calendar");
            
        verify(calendarRepo, never()).save(any(Calendars.class));
    }
    
    @Test
    void updateCalendar_WhenUsernameIsNull_ShouldThrowAccessDeniedException() {
        // Arrange
        Integer calendarId = 1;
        
        Users owner = new Users();
        owner.setUsername("owner");
        
        Calendars existingCalendar = new Calendars();
        existingCalendar.setId(calendarId);
        existingCalendar.setOwner(owner);
        
        when(calendarRepo.findById(calendarId)).thenReturn(java.util.Optional.of(existingCalendar));
        
        // Act & Assert
        assertThatThrownBy(() -> calendarService.updateCalendar(calendarId, new Calendars(), null))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("Not allowed to update this calendar");
            
        verify(calendarRepo, never()).save(any(Calendars.class));
    }
    
    @Test
    void deleteCalendar_WhenUserIsOwner_ShouldDeleteCalendar() throws Exception {
        // Arrange
        Integer calendarId = 1;
        String username = "testuser";
        
        Users owner = new Users();
        owner.setUsername(username);
        
        Calendars calendar = new Calendars();
        calendar.setId(calendarId);
        calendar.setOwner(owner);
        
        when(calendarRepo.findById(calendarId)).thenReturn(java.util.Optional.of(calendar));
        
        // Act
        calendarService.deleteCalendar(calendarId, username);
        
        // Assert
        verify(calendarRepo).deleteById(calendarId);
    }
    
    @Test
    void deleteCalendar_WhenCalendarDoesNotExist_ShouldThrowIllegalArgumentException() {
        // Arrange
        Integer nonExistentId = 999;
        when(calendarRepo.findById(nonExistentId)).thenReturn(java.util.Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> calendarService.deleteCalendar(nonExistentId, "user1"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("calendar not found: " + nonExistentId);
            
        verify(calendarRepo, never()).deleteById(any());
    }
    
    @Test
    void deleteCalendar_WhenUserIsNotOwner_ShouldThrowAccessDeniedException() {
        // Arrange
        Integer calendarId = 1;
        String ownerUsername = "owner";
        String otherUser = "otheruser";
        
        Users owner = new Users();
        owner.setUsername(ownerUsername);
        
        Calendars calendar = new Calendars();
        calendar.setId(calendarId);
        calendar.setOwner(owner);
        
        when(calendarRepo.findById(calendarId)).thenReturn(java.util.Optional.of(calendar));
        
        // Act & Assert
        assertThatThrownBy(() -> calendarService.deleteCalendar(calendarId, otherUser))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("Not allowed to delete this calendar");
            
        verify(calendarRepo, never()).deleteById(any());
    }
    
    @Test
    void deleteCalendar_WhenUsernameIsNull_ShouldThrowAccessDeniedException() {
        // Arrange
        Integer calendarId = 1;
        
        Users owner = new Users();
        owner.setUsername("owner");
        
        Calendars calendar = new Calendars();
        calendar.setId(calendarId);
        calendar.setOwner(owner);
        
        when(calendarRepo.findById(calendarId)).thenReturn(java.util.Optional.of(calendar));
        
        // Act & Assert
        assertThatThrownBy(() -> calendarService.deleteCalendar(calendarId, null))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("Not allowed to delete this calendar");
            
        verify(calendarRepo, never()).deleteById(any());
    }
    
    @Test
    void deleteCalendar_WhenOwnerIsNull_ShouldThrowAccessDeniedException() {
        // Arrange
        Integer calendarId = 1;
        
        Calendars calendar = new Calendars();
        calendar.setId(calendarId);
        calendar.setOwner(null); // Owner is null
        
        when(calendarRepo.findById(calendarId)).thenReturn(java.util.Optional.of(calendar));
        
        // Act & Assert
        assertThatThrownBy(() -> calendarService.deleteCalendar(calendarId, "anyuser"))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("Not allowed to delete this calendar");
            
        verify(calendarRepo, never()).deleteById(any());
    }
}
