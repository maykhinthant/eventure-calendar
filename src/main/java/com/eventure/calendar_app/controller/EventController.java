// package com.eventure.calendar_app.controller;

// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.eventure.calendar_app.model.Event;
// import com.eventure.calendar_app.service.EventService;

// import java.security.Principal;
// import java.util.List;

// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;

// @RestController
// @RequestMapping("/api")
// public class EventController {
    
//     private EventService service;

//     // Constructor injection
//     public EventController(EventService service) {
//         this.service = service;
//     }
    
//     @PostMapping("/calendar")
//     public void createEvent(@RequestBody Event event, Principal principal) {
//         service.createEvent(event);
//     }

//     @GetMapping("/calendar/{username}")
//     public List<Event> getEventsForUser(@PathVariable String username) {
//         return service.getEventsForUser(username);
//     } 
// }

