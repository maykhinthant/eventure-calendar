package com.eventure.calendar_app.event.model;

import java.time.LocalDateTime;

import com.eventure.calendar_app.calendar.model.Calendars;
import com.eventure.calendar_app.user.model.Users;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Events {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)     // Auto-generate the IDs in the database so that you don't need to hard-code
    private Integer id;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean completed;

    @ManyToOne(fetch = FetchType.LAZY)  // Many events -> one user
    @JoinColumn(name = "users_id", nullable = false)
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private Users createdBy;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "calendar_id", nullable = true)
    @JsonIgnoreProperties({"owner", "hibernateLazyInitializer", "handler"})
    private Calendars calendar;

    // Quick boolean to know if the event repeats at all.
    private Boolean isRecurring;

    // iCalendar RRULE string
    @Column(columnDefinition = "TEXT")
    private String recurrenceRule;
    
    // End date for recurring events
    private LocalDateTime recurrenceEndDate;
}