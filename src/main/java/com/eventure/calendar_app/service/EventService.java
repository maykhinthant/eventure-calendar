package com.eventure.calendar_app.service;

// import java.util.List;

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
    // Find the user object using the username
    // Set the createdBy for the event and save the event
    public void createEvent(Events event, String username) {
        Users user = userRepo.findByUsername(username);

        if(user == null) {
            throw new IllegalArgumentException("user not found: " + username);
        }

        event.setCreatedBy(user);
        eventRepo.save(event);
    }
}
