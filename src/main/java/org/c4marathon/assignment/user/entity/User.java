package org.c4marathon.assignment.user.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import org.c4marathon.assignment.calendar.entity.CalendarEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "user_email", unique = true)
	private String userEmail;

	@Column(name = "password")
	private String password;

	@OneToMany(mappedBy = "user", fetch= FetchType.LAZY, cascade = CascadeType.ALL)
	private List<CalendarEntity> calendarEntity = new ArrayList<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<Authority> roles = new ArrayList<>();

	@Builder
	public User(String userName, String userEmail, String password) {
		this.userName = userName;
		this.userEmail = userEmail;
		this.password = password;
	}

	public void setRoles(List<Authority> role) {
		this.roles = role;
		role.forEach(o -> o.setUser(this));
	}
}
