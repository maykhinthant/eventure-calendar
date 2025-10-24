package com.eventure.calendar_app.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eventure.calendar_app.model.Users;

public interface UserRepo extends JpaRepository<Users, Long> {
    Users findByUsername(String username);
    Users findByEmail(String email);
}
