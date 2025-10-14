package com.eventure.calendar_app.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventure.calendar_app.model.Events;
import com.eventure.calendar_app.service.EventService;

import java.security.Principal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
public class EventController {
    
    private EventService service;

    // Constructor injection
    public EventController(EventService service) {
        this.service = service;
    }
    
    // Create new event after log in
    // Use Principal to fetch the username of the logged in user
    @PostMapping("/calendar")
    public void createEvent(@RequestBody Events event, Principal principal) {
        String username = principal != null ? principal.getName() : null;
        service.createEvent(event, username);
    }
}

