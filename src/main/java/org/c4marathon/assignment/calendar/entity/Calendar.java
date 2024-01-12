package org.c4marathon.assignment.calendar.entity;

import java.util.ArrayList;
import java.util.List;

import org.c4marathon.assignment.todo.entity.Todo;
import org.c4marathon.assignment.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Getter
@RequiredArgsConstructor
public class Calendar {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "calendar_id", nullable = false)
	private Long id;

	// 일정 이름
	@Column(name = "calendar_name", nullable = false)
	private String calendarName;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@OneToMany(mappedBy = "calendar")
	private List<Todo> todos = new ArrayList<>();

	@Builder
	public Calendar (String calendarName, User user) {
		this.calendarName = calendarName;
		this.user = user;
	}
}
