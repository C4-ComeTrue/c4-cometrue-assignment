package org.c4marathon.assignment.calendar.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.NotNull;
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
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CalendarEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "calendar_id")
	private Long id;

	@Size(max=50)
	@Column(name = "calendar_name")
	private String calendarName;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@OneToMany(mappedBy = "calendarEntity", fetch= FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Todo> todos = new ArrayList<>();

	public CalendarEntity(String calendarName, User user, List<Todo> todos) {
		this.calendarName = calendarName;
		this.user = user;
		this.todos = todos;
	}

	public void addTodo(Todo todo) {
		this.todos.add(todo);
	}
}
