package com.eventure.calendar_app.calendar.service;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.eventure.calendar_app.calendar.model.Calendars;
import com.eventure.calendar_app.calendar.repo.CalendarRepo;
import com.eventure.calendar_app.user.model.Users;
import com.eventure.calendar_app.user.repo.UserRepo;

@Service
public class CalendarService {

    private CalendarRepo calRepo;
    private UserRepo userRepo;

    public CalendarService (CalendarRepo calRepo, UserRepo userRepo) {
        this.calRepo = calRepo;
        this.userRepo = userRepo;
    }

    // Create new calendar for the logged in user
    public void createCalendar(Calendars calendar, String username) {
        Users user = userRepo.findByUsername(username);

        if(user == null) {
            throw new IllegalArgumentException("user not found: " + username);
        }

        calendar.setOwner(user);
        calRepo.save(calendar);
    }

    // Fetch all the calendars for the logged in user
    public List<Calendars> getAllCalendars(String username) {
        if(username == null) {
            return calRepo.findAll();
        }

        return calRepo.findByOwner_Username(username);
    }

    // Update an existing calendar
    public void updateCalendar(Integer id, Calendars updated, String username) throws AccessDeniedException {
        Calendars existing = calRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("calendar not found: "  + id));

        // Check ownership
        Users owner = existing.getOwner();

        if(owner == null || username == null || !username.equals(owner.getUsername())) {
            throw new AccessDeniedException("Not allowed to update this calendar");
        }

        // Copy fileds that are allowed to change
        existing.setId(updated.getId());
        existing.setName(updated.getName());
        existing.setColor(updated.getColor());

        calRepo.save(existing);
    }

    // Delete a calendar
    public void deleteCalendar(Integer id, String username) throws AccessDeniedException {
        Calendars existing = calRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("calendar not found: "  + id));

        // Check ownership
        Users owner = existing.getOwner();

        if(owner == null || username == null || !username.equals(owner.getUsername())) {
            throw new AccessDeniedException("Not allowed to delete this calendar");
        }

        calRepo.deleteById(id);
    }
}
