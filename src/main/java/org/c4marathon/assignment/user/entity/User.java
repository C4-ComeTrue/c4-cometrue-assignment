package org.c4marathon.assignment.user.entity;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import org.c4marathon.assignment.calendar.entity.Calendar;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Entity
@RequiredArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_name", nullable = false)
	private String userName;

	@Column(name = "user_email", nullable = false)
	private String userEmail;

	@Column(name = "password", nullable = false)
	private String password;

	@OneToMany(mappedBy = "user")
	private List<Calendar> calendars = new ArrayList<>();

	@Builder
	public User(String userName, String userEmail, String password) {
		this.userName = userName;
		this.userEmail = userEmail;
		this.password = password;
	}
}
