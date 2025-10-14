package com.eventure.calendar_app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventure.calendar_app.model.Users;
import com.eventure.calendar_app.service.UserService;

@RestController
@RequestMapping("/api")
public class UserController {

	@Autowired
	private UserService service;
	
	// Register a new user in the database: id, username and password
	@PostMapping("/users/register")
	public Users register (@RequestBody Users user) {
		return service.register(user);
	}

	@PostMapping("/users/login")
	public String login(@RequestBody Users user) {
		System.out.println(user);
		return service.verify(user);
	}
}
