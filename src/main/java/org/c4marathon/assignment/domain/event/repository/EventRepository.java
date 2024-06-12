package org.c4marathon.assignment.domain.event.repository;

import org.c4marathon.assignment.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

	boolean existsByName(String name);
}
