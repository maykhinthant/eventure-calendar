package com.eventure.calendar_app.service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.eventure.calendar_app.model.Calendars;
import com.eventure.calendar_app.model.Events;
import com.eventure.calendar_app.model.Users;
import com.eventure.calendar_app.repo.CalendarRepo;
import com.eventure.calendar_app.repo.EventRepo;
import com.eventure.calendar_app.repo.UserRepo;

@Service
public class EventService {

    private EventRepo eventRepo;
    private UserRepo userRepo;
    private CalendarRepo calRepo;

    // Constructor injection
    public EventService(EventRepo eventRepo, UserRepo userRepo, CalendarRepo calRepo) {
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
        this.calRepo = calRepo;
    }

    // Create new event
    public void createEvent(Events event, String username) {

        // Find the user object and set createdBy
        Users user = userRepo.findByUsername(username);

        if(user == null) {
            throw new IllegalArgumentException("user not found: " + username);
        }

        event.setCreatedBy(user);

        // If client provides calendar, set the calendar in the events. If not, set calendar to null
        if(event.getCalendar() != null) {
            Integer calId = event.getCalendar().getId();

            if(calId != null) {
                Calendars calendar = calRepo.findById(calId).orElseThrow(() -> new IllegalArgumentException("calendar not found: " + calId));
                event.setCalendar(calendar);
            } else {
                event.setCalendar(null);
            }
        }

        eventRepo.save(event);
    }

    // Fetch all the events for the logged in user
    public List<Events> getEvents(String username) {
        if(username == null) {
            return eventRepo.findAll();
        }

        return eventRepo.findByCreatedBy_Username(username);
    }

    // Update an existing event
    public void updateEvent(Integer id, Events updated, String username) throws AccessDeniedException {
        Events existing = eventRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("event not found: "  +id));
    
        Users owner = existing.getCreatedBy();

        if(owner == null || username == null || !username.equals(owner.getUsername())) {
            throw new AccessDeniedException("Not allowed to update this event");
        }

        // Copy fileds that are allowed to change
        existing.setTitle(updated.getTitle());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setCompleted(updated.getCompleted());

        // If the calendar is not null in the updated event, update the calendar. If not, set null
        if(updated.getCalendar() != null) {
            Integer newCalId = updated.getCalendar().getId();

            if(newCalId != null) {
                Calendars cal = calRepo.findById(newCalId).orElseThrow(() -> new IllegalArgumentException());
                existing.setCalendar(cal);
            } else {
                existing.setCalendar(null);
            }
        }

        eventRepo.save(existing);
    }

    // Delete the event by the id
    public void deleteEvent(Integer id, String username) throws AccessDeniedException {
        Events existing = eventRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("event not found: "  +id));
        
        Users owner = existing.getCreatedBy();

        if(owner == null || username == null || !username.equals(owner.getUsername())) {
            throw new AccessDeniedException("Not allowed to delete this event");
        }

        eventRepo.deleteById(id);
    }
}
