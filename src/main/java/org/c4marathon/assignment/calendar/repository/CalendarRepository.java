package org.c4marathon.assignment.calendar.repository;

import java.util.Optional;

import org.c4marathon.assignment.calendar.entity.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarRepository extends JpaRepository<Calendar, Long> {

	Optional<Calendar> findByUserIdAndId(Long userId, Long calendarId);

}
