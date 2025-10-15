package com.eventure.calendar_app.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventure.calendar_app.model.Events;
import com.eventure.calendar_app.service.EventService;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

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
    @PostMapping("/events")
    public void createEvent(@RequestBody Events event, Principal principal) {
        String username = principal != null ? principal.getName() : null;
        service.createEvent(event, username);
    }

    // Fetch all the events for the logged in user
    @GetMapping("/events")
    public List<Events> getEvents(Principal principal) {
        String username = principal != null ? principal.getName() : null;
        return service.getEvents(username);
    }

}