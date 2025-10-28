package com.eventure.calendar_app.calendar.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eventure.calendar_app.calendar.model.Calendars;

@Repository
public interface CalendarRepo extends JpaRepository<Calendars, Integer> {
    List<Calendars> findByOwner_Username(String username);
}
