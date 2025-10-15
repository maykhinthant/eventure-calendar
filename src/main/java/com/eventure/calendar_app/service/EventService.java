package com.eventure.calendar_app.service;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.eventure.calendar_app.model.Events;
import com.eventure.calendar_app.model.Users;
import com.eventure.calendar_app.repo.EventRepo;
import com.eventure.calendar_app.repo.UserRepo;

@Service
public class EventService {

    private EventRepo eventRepo;
    private UserRepo userRepo;

    // Constructor injection
    public EventService(EventRepo eventRepo, UserRepo userRepo) {
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
    }

    // ---------- CREATE NEW EVENT ----------
    // Find the users object using the username
    // Set the createdBy for the event and save the event
    public void createEvent(Events event, String username) {
        Users user = userRepo.findByUsername(username);

        if(user == null) {
            throw new IllegalArgumentException("user not found: " + username);
        }

        event.setCreatedBy(user);
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

        // Check ownership
        Users owner = existing.getCreatedBy();

        if(owner == null || username == null || !username.equals(owner.getUsername())) {
            throw new AccessDeniedException("Not allowed to update this event");
        }

        // Copy fileds that are allowed to change
        existing.setTitle(updated.getTitle());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setCalendarId(updated.getCalendarId());
        existing.setCompleted(updated.getCompleted());

        eventRepo.save(existing);
    }
}
