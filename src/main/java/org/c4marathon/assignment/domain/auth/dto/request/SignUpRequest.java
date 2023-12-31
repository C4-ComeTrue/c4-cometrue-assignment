package org.c4marathon.assignment.domain.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SignUpRequest {

	@Size(max = 50)
	@NotNull
	private String email;
	@Size(max = 100)
	private String address;
}
