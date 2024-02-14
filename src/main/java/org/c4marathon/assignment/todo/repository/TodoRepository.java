package org.c4marathon.assignment.todo.repository;

import java.util.List;

import org.c4marathon.assignment.calendar.entity.CalendarEntity;
import org.c4marathon.assignment.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {
	List<Todo> findAllByCalendarEntity(CalendarEntity calendarEntity);
}
