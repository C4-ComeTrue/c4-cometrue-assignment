package org.c4marathon.assignment.domain.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SignUpRequest {

	@Size(max = 50, message = "email size exceed 50")
	@NotNull(message = "email is null")
	private String email;
	@Size(max = 100, message = "address size exceed 100")
	private String address;
}
