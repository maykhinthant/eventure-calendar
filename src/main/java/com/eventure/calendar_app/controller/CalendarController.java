package com.eventure.calendar_app.controller;

import java.security.Principal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventure.calendar_app.model.Calendars;
import com.eventure.calendar_app.service.CalendarService;

@RestController
@RequestMapping("/api")
public class CalendarController {
    
    private CalendarService service;

    public CalendarController (CalendarService service) {
        this.service = service;
    }

    // Create new calendar for the logged in user
    @PostMapping("/calendars")
    public void createCalendar(@RequestBody Calendars calendar, Principal principal) {
        String username = principal != null ? principal.getName() : null;
        service.createCalendar(calendar, username);
    }
}
