package com.eventure.calendar_app.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Users {
    
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	private Long id;

	@Column(unique = true)
	private String username;

	@Column(unique = true)
	private String email;

	private String name;
	private String password;	// not used for oauth but keep for local login flows
	private String provider;	// e.g. "google", "github"
	private String providerId;	// provider's user id
	private String roles; // comma separated roles "ROLE_USER,ROLE_ADMIN"
}
