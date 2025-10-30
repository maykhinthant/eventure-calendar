package com.eventure.calendar_app.event.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.eventure.calendar_app.event.model.Events;
import com.eventure.calendar_app.event.service.EventService;
import com.eventure.calendar_app.testconfig.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(EventController.class)
@Import(TestSecurityConfig.class)
@ContextConfiguration(classes = {EventController.class, TestSecurityConfig.class})
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createEvent_WhenValidEvent_ShouldReturnCreated() throws Exception {
        // Arrange
        Events event = new Events();
        event.setTitle("Test Event");
        event.setStartTime(LocalDateTime.now());
        event.setEndTime(LocalDateTime.now().plusHours(1));
        
        Principal principal = () -> "testuser";
        
        doNothing().when(eventService).createEvent(any(Events.class), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/events")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated());
    }

    @Test
    void getEvents_WhenEventsExist_ShouldReturnEventsList() throws Exception {
        // Arrange
        Events event1 = new Events();
        event1.setId(1);
        event1.setTitle("Event 1");
        
        Principal principal = () -> "testuser";
        
        when(eventService.getEvents(anyString())).thenReturn(List.of(event1));

        // Act & Assert
        mockMvc.perform(get("/api/events")
                .principal(principal))
                .andExpect(status().isOk());
    }

    @Test
    void updateEvent_WhenValidEvent_ShouldReturnNoContent() throws Exception {
        // Arrange
        Integer eventId = 1;
        Events updatedEvent = new Events();
        updatedEvent.setTitle("Updated Event");
        updatedEvent.setStartTime(LocalDateTime.now());
        updatedEvent.setEndTime(LocalDateTime.now().plusHours(1));
        
        Principal principal = () -> "testuser";
        
        doNothing().when(eventService).updateEvent(eq(eventId), any(Events.class), anyString());

        // Act & Assert
        mockMvc.perform(put("/api/events/{id}", eventId)
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedEvent)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEvent_WhenValidEvent_ShouldReturnNoContent() throws Exception {
        // Arrange
        Integer eventId = 1;
        Principal principal = () -> "testuser";
        
        doNothing().when(eventService).deleteEvent(eq(eventId), anyString());

        // Act & Assert
        mockMvc.perform(delete("/api/events/{id}", eventId)
                .principal(principal))
                .andExpect(status().isNoContent());
    }
}
