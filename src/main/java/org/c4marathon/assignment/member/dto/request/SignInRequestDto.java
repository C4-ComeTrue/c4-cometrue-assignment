package org.c4marathon.assignment.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SignInRequestDto(
	@NotBlank(message = "아이디는 공백일 수 없습니다.")
	String memberId,

	@NotBlank(message = "비밀번호는 공백일 수 없습니다.")
	String password
) {
}
