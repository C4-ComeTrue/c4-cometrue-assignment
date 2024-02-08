package org.c4marathon.assignment.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public class MemberSignUpDto {

	public record Request(
		@Valid @Email(message = "잘못된 이메일 형식입니다.") String email,
		@Valid @NotEmpty(message = "비밀번호는 비어있을 수 없습니다.") String password
	) {
	}

	public record Response(
		Long id
	) {
	}
}
