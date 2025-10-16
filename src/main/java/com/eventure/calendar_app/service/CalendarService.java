package com.eventure.calendar_app.service;

import org.springframework.stereotype.Service;

import com.eventure.calendar_app.model.Calendars;
import com.eventure.calendar_app.model.Users;
import com.eventure.calendar_app.repo.CalendarRepo;
import com.eventure.calendar_app.repo.UserRepo;

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
    
}
