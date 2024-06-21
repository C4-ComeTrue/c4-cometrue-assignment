package org.c4marathon.assignment.domain.event.service;

import static org.c4marathon.assignment.global.error.ErrorCode.*;

import org.c4marathon.assignment.domain.event.entity.Event;
import org.c4marathon.assignment.domain.event.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventReadService {

	private final EventRepository eventRepository;

	@Transactional(readOnly = true)
	public boolean existsByName(String name) {
		return eventRepository.existsByName(name);
	}

	@Transactional(readOnly = true)
	public boolean existsById(Long id) {
		return eventRepository.existsById(id);
	}

	@Transactional(readOnly = true)
	public Event findById(Long id) {
		return eventRepository.findById(id)
			.orElseThrow(EVENT_NOT_FOUND::baseException);
	}
}
