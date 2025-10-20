package com.eventure.calendar_app.controller;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventure.calendar_app.model.Calendars;
import com.eventure.calendar_app.service.CalendarService;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class CalendarController {
    
    private CalendarService service;

    public CalendarController (CalendarService service) {
        this.service = service;
    }

    // Create new calendar for the logged in user
    @PostMapping("/calendars")
    public ResponseEntity<?> createCalendar(@RequestBody Calendars calendar, Principal principal) {
        try {
            String username = principal != null ? principal.getName() : null;
            service.createCalendar(calendar, username);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    // Fetch all the calendars
    @GetMapping("/calendars")
    public ResponseEntity<?> getAllCalendars(Principal principal) {
        try {
            String username = principal != null ? principal.getName() : null;
            List<Calendars> calendars = service.getAllCalendars(username);
            return ResponseEntity.ok(calendars);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    // Update an existing calendar
    @PutMapping("/calendars/{id}")
    public ResponseEntity<?> updateCalendar(@PathVariable Integer id, @RequestBody Calendars updated, Principal principal) {
        try {
            String username = principal != null ? principal.getName() : null;
            service.updateCalendar(id, updated, username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    // Delete a calendar
    @DeleteMapping("/calendars/{id}")
    public ResponseEntity<?> deleteCalendar(@PathVariable Integer id, Principal principal) {
        try {
            String username = principal != null ? principal.getName() : null;
            service.deleteCalendar(id, username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }
}
