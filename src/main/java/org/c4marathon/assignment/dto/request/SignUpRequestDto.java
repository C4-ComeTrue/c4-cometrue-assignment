package org.c4marathon.assignment.dto.request;

import jakarta.validation.constraints.Size;

public record SignUpRequestDto(
	@Size(max = 30)
	String username
) {
}
