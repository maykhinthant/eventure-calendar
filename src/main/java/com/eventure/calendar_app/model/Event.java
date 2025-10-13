// package com.eventure.calendar_app.model;

// import java.time.LocalDateTime;

// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.NoArgsConstructor;

// @Entity
// @Data
// @AllArgsConstructor
// @NoArgsConstructor
// public class Event {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)     // Auto-generate the IDs in the database so that you don't need to hard-code
//     private Integer id;
//     private String title;
//     private LocalDateTime startTime;
//     private LocalDateTime endTime;
//     private String calendarId;
//     private Boolean completed;
//     private String createdBy;
// }