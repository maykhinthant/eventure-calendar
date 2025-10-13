// package com.eventure.calendar_app.service;

// import java.util.List;

// import org.springframework.stereotype.Service;

// import com.eventure.calendar_app.model.Event;
// import com.eventure.calendar_app.repo.EventRepo;

// @Service
// public class EventService {

//     private EventRepo repo;

//     // Constructor injection
//     public EventService(EventRepo repo) {
//         this.repo = repo;
//     }

//     // Create a new event
//     public void createEvent(Event event) {
//         repo.save(event);
//     }

//     // Fetch all events for the log in user
//     public List<Event> getEventsForUser(String username) {
//         if(username == null) {
//             return repo.findAll();
//         }

//         return repo.findByCreatedBy(username);
//     }
// }
