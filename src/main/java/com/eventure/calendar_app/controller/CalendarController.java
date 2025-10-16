package com.eventure.calendar_app.controller;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;

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

    // Fetch all the calendars
    @GetMapping("/calendars")
    public List<Calendars> getAllCalendars(Principal principal) {
        String username = principal != null ? principal.getName() : null;
        return service.getAllCalendars(username);
    }

    // Update an existing calendar
    @PutMapping("/calendars/{id}")
    public void updateCalendar(@PathVariable Integer id, @RequestBody Calendars updated, Principal principal) throws AccessDeniedException {
        String username = principal != null ? principal.getName() : null;
        service.updateCalendar(id, updated, username);
    }

    // Delete a calendar
    @DeleteMapping("/calendars/{id}")
    public void deleteCalendar(@PathVariable Integer id, Principal principal) throws AccessDeniedException {
        String username = principal != null ? principal.getName() : null;
        service.deleteCalendar(id, username);
    }
}
