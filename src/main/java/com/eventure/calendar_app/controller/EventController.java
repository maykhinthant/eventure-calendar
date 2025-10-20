package com.eventure.calendar_app.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventure.calendar_app.model.Events;
import com.eventure.calendar_app.service.EventService;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class EventController {
    
    private EventService service;

    // Constructor injection
    public EventController(EventService service) {
        this.service = service;
    }
    
    // Create new event after log in
    // Use Principal to fetch the username of the logged in user
    @PostMapping("/events")
    public ResponseEntity<?> createEvent(@RequestBody Events event, Principal principal) {
        try {
            String username = principal != null ? principal.getName() : null;
            service.createEvent(event, username);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    // Fetch all the events for the logged in user
    @GetMapping("/events")
    public ResponseEntity<?> getEvents(Principal principal) {
        try {
            String username = principal != null ? principal.getName() : null;
            List<Events> events = service.getEvents(username);
            return ResponseEntity.ok(events);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    // Update an existing event
    @PutMapping("/events/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Integer id, @RequestBody Events event, Principal principal) {
        try {
            String username = principal != null ? principal.getName() : null;
            service.updateEvent(id, event, username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    // Delete the event by the id
    @DeleteMapping("/events/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Integer id, Principal principal) {
        try {
            String username = principal != null ? principal.getName() : null;
            service.deleteEvent(id, username);
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