package com.eventure.calendar_app.user.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eventure.calendar_app.user.model.Users;

public interface UserRepo extends JpaRepository<Users, Long> {
    Users findByUsername(String username);
    Users findByEmail(String email);
}
