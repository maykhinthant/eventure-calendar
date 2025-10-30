package com.eventure.calendar_app.calendar.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

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

import com.eventure.calendar_app.calendar.model.Calendars;
import com.eventure.calendar_app.calendar.service.CalendarService;
import com.eventure.calendar_app.config.jwtFilter;
import com.eventure.testconfig.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(value = CalendarController.class, 
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = jwtFilter.class))
@Import(TestSecurityConfig.class)
public class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalendarService calendarService;

    @Autowired
    private ObjectMapper objectMapper;

    private Calendars testCalendar;
    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        testCalendar = new Calendars();
        testCalendar.setId(1);
        testCalendar.setName("Test Calendar");
        testCalendar.setColor("#FF0000");
        
        mockPrincipal = () -> "testuser";
    }

    @Test
    void createCalendar_WhenValidCalendarProvided_ShouldReturnsCreatedStatus() throws Exception {
        // Arrange
        doNothing().when(calendarService).createCalendar(any(Calendars.class), any());

        // Act & Assert
        mockMvc.perform(post("/api/calendars")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCalendar)))
                .andExpect(status().isCreated());
    }

    @Test
    void createCalendar_WhenUserNotFound_ShouldReturnNotFoundStatus() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("user not found: testuser"))
            .when(calendarService).createCalendar(any(Calendars.class), any());

        // Act & Assert
        mockMvc.perform(post("/api/calendars")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCalendar)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("user not found: testuser"));
    }

    @Test
    void createCalendar_WhenUnexpectedErrorOccurs_ShouldReturnInternalServerErrorStatus() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Unexpected error"))
            .when(calendarService).createCalendar(any(Calendars.class), any());

        // Act & Assert
        mockMvc.perform(post("/api/calendars")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCalendar)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Unexpected error"));
    }

    @Test
    void getAllCalendars_WhenCalendarsExist_ShouldReturnCalendarsList() throws Exception {
        // Arrange
        List<Calendars> calendarsList = Arrays.asList(testCalendar);
        when(calendarService.getAllCalendars(any())).thenReturn(calendarsList);

        // Act & Assert
        mockMvc.perform(get("/api/calendars")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getAllCalendars_WhenUnexpectedErrorOccurs_ShouldReturnInternalServerErrorStatus() throws Exception {
        // Arrange
        when(calendarService.getAllCalendars(any()))
            .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(get("/api/calendars")
                .principal(mockPrincipal))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Unexpected error"));
    }

    @Test
    void updateCalendar_WhenValidCalendarProvided_ShouldReturnNoContentStatus() throws Exception {
        // Arrange
        doNothing().when(calendarService).updateCalendar(eq(1), any(Calendars.class), any());

        // Act & Assert
        mockMvc.perform(put("/api/calendars/1")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCalendar)))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateCalendar_WhenCalendarNotFound_ShouldReturnNotFoundStatus() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("calendar not found: 1"))
            .when(calendarService).updateCalendar(eq(1), any(Calendars.class), any());

        // Act & Assert
        mockMvc.perform(put("/api/calendars/1")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCalendar)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("calendar not found: 1"));
    }

    @Test
    void updateCalendar_WhenAccessDenied_ShouldReturnForbiddenStatus() throws Exception {
        // Arrange
        doThrow(new AccessDeniedException("Not allowed to update this calendar"))
            .when(calendarService).updateCalendar(eq(1), any(Calendars.class), any());

        // Act & Assert
        mockMvc.perform(put("/api/calendars/1")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCalendar)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Not allowed to update this calendar"));
    }

    @Test
    void updateCalendar_WhenUnexpectedErrorOccurs_ShouldReturnInternalServerErrorStatus() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Unexpected error"))
            .when(calendarService).updateCalendar(eq(1), any(Calendars.class), any());

        // Act & Assert
        mockMvc.perform(put("/api/calendars/1")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCalendar)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Unexpected error"));
    }

    @Test
    void deleteCalendar_WhenValidCalendarIdProvided_ShouldReturnNoContentStatus() throws Exception {
        // Arrange
        doNothing().when(calendarService).deleteCalendar(eq(1), any());

        // Act & Assert
        mockMvc.perform(delete("/api/calendars/1")
                .principal(mockPrincipal))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCalendar_WhenCalendarNotFound_ShouldReturnNotFoundStatus() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("calendar not found: 1"))
            .when(calendarService).deleteCalendar(eq(1), any());

        // Act & Assert
        mockMvc.perform(delete("/api/calendars/1")
                .principal(mockPrincipal))
                .andExpect(status().isNotFound())
                .andExpect(content().string("calendar not found: 1"));
    }

    @Test
    void deleteCalendar_WhenAccessDenied_ShouldReturnForbiddenStatus() throws Exception {
        // Arrange
        doThrow(new AccessDeniedException("Not allowed to delete this calendar"))
            .when(calendarService).deleteCalendar(eq(1), any());

        // Act & Assert
        mockMvc.perform(delete("/api/calendars/1")
                .principal(mockPrincipal))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Not allowed to delete this calendar"));
    }

    @Test
    void deleteCalendar_WhenUnexpectedErrorOccurs_ShouldReturnInternalServerErrorStatus() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Unexpected error"))
            .when(calendarService).deleteCalendar(eq(1), any());

        // Act & Assert
        mockMvc.perform(delete("/api/calendars/1")
                .principal(mockPrincipal))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Unexpected error"));
    }
}
