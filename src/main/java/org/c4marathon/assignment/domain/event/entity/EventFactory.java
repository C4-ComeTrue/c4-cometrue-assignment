package org.c4marathon.assignment.domain.event.entity;

import org.c4marathon.assignment.domain.event.dto.request.PublishEventRequest;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventFactory {

	public static Event buildEvent(PublishEventRequest request) {
		return new Event(null, request.name(), request.endDate());
	}
}
