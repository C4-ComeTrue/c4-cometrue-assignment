package org.c4marathon.assignment.calendar.repository;

import java.util.Optional;

import org.c4marathon.assignment.calendar.entity.CalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarRepository extends JpaRepository<CalendarEntity, Long> {
	Optional<CalendarEntity> findByUserIdAndId(Long userId, Long calendarId);

	boolean existsByUserIdAndCalendarName(Long userId, String calendarName);
}
