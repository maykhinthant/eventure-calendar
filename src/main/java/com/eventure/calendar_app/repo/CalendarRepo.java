package com.eventure.calendar_app.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eventure.calendar_app.model.Calendars;

@Repository
public interface CalendarRepo extends JpaRepository<Calendars, Integer> {
    List<Calendars> findByOwner_Username(String username);
}
