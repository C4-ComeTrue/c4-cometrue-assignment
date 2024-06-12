package org.c4marathon.assignment.domain.event.controller;

import org.c4marathon.assignment.domain.event.dto.request.PublishEventRequest;
import org.c4marathon.assignment.domain.event.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

	private final EventService eventService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void publishEvent(@Valid @RequestBody PublishEventRequest request) {
		eventService.publishEvent(request);
	}
}
