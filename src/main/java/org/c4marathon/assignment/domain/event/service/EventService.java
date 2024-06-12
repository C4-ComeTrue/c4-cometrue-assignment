package org.c4marathon.assignment.domain.event.service;

import static org.c4marathon.assignment.domain.event.entity.EventFactory.*;

import org.c4marathon.assignment.domain.event.dto.request.PublishEventRequest;
import org.c4marathon.assignment.domain.event.entity.Event;
import org.c4marathon.assignment.domain.event.repository.EventRepository;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class EventService {

	private final EventRepository eventRepository;
	private final EventReadService eventReadService;

	@Transactional
	public void publishEvent(PublishEventRequest request) {
		if (eventReadService.existsByName(request.name())) {
			throw ErrorCode.ALREADY_EVENT_EXISTS.baseException();
		}
		Event event = buildEvent(request);
		eventRepository.save(event);
	}
}
