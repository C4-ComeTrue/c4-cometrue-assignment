package org.c4marathon.assignment.domain.event.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PublishEventRequest(
	@NotEmpty
	@Size(max = 30)
	String name,
	@NotNull
	@Future
	LocalDateTime endDate
) {
}
