package org.c4marathon.assignment.domain.event.service;

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
}
