package com.eventure.calendar_app.service;

import java.nio.file.AccessDeniedException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.eventure.calendar_app.model.Calendars;
import com.eventure.calendar_app.model.Events;
import com.eventure.calendar_app.model.Users;
import com.eventure.calendar_app.repo.CalendarRepo;
import com.eventure.calendar_app.repo.EventRepo;
import com.eventure.calendar_app.repo.UserRepo;

import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.Period;

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
        List<Events> allEvents = username == null
                ? eventRepo.findAll()
                : eventRepo.findByCreatedBy_Username(username);
        // This new list will hold both normal and expanded recurring events
        List<Events> expandedEvents = new ArrayList<>();

        for(Events event: allEvents) {
            expandedEvents.add(event);

            // Check if this event has a recurrence rule (RRULE string like "FREQ=DAILY;INTERVAL=2")
            if(event.getRecurrenceRule() != null && !event.getRecurrenceRule().isEmpty()) {
                if(event.getStartTime() == null || event.getEndTime() == null) {
                    continue;
                }
                try{
                    Recur<ZonedDateTime> recur = new Recur<>(event.getRecurrenceRule());

                    // Convert our startTime (LocalDateTime) to ZonedDateTime because ical4j 4.x uses Temporal types.
                    ZonedDateTime startDateTime = event.getStartTime().atZone(ZoneId.systemDefault());

                    // Use the recurrenceEndDate if provided, otherwise default to 6 months from start
                    ZonedDateTime periodEnd = event.getRecurrenceEndDate() != null ? 
                        event.getRecurrenceEndDate().atZone(ZoneId.systemDefault()) : 
                        startDateTime.plusMonths(6);

                    // Create a Period between start and end time
                    Period<ZonedDateTime> recurrencePeriod = new Period<>(
                            startDateTime,
                            java.time.Duration.between(startDateTime, periodEnd)
                    );

                    // Generate the recurrence set of dates
                    List<ZonedDateTime> recurrenceDates = recur.getDates(startDateTime, recurrencePeriod);

                    final int[] occurrenceIndex = {0};
                    recurrenceDates.forEach(recurrenceDate -> {
                        if(recurrenceDate.isEqual(startDateTime)) {
                            return;
                        }

                        // Generate repeated events for each date in the recurrence set
                        Events repeatedEvent = new Events();

                        repeatedEvent.setId(buildSyntheticId(event.getId(), occurrenceIndex[0]++));
                        repeatedEvent.setTitle(event.getTitle());
                        repeatedEvent.setStartTime(recurrenceDate.toLocalDateTime());

                          long durationMinutes = java.time.Duration.between(
                                event.getStartTime(), event.getEndTime()
                        ).toMinutes();

                        repeatedEvent.setEndTime(recurrenceDate.toLocalDateTime().plusMinutes(durationMinutes));
                        repeatedEvent.setRecurrenceRule(event.getRecurrenceRule());
                        repeatedEvent.setCalendar(event.getCalendar());
                        repeatedEvent.setCreatedBy(event.getCreatedBy());
                        repeatedEvent.setCompleted(event.getCompleted());
                        repeatedEvent.setIsRecurring(true);
                        repeatedEvent.setRecurrenceEndDate(event.getRecurrenceEndDate());

                        expandedEvents.add(repeatedEvent);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return expandedEvents;
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
        existing.setIsRecurring(updated.getIsRecurring());
        existing.setRecurrenceRule(updated.getRecurrenceRule());
        existing.setRecurrenceEndDate(updated.getRecurrenceEndDate());

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

    // Generate Id for each repeated events 
    private Integer buildSyntheticId(Integer baseId, int occurrenceIndex) {
        int base = baseId != null ? Math.abs(baseId) : 0;
        return -((base + 1) * 1000 + occurrenceIndex + 1);
    }
}
