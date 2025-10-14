package com.eventure.calendar_app.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eventure.calendar_app.model.Events;

@Repository
public interface EventRepo extends JpaRepository<Events, Integer>{

}