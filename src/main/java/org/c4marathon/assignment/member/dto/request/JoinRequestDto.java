package org.c4marathon.assignment.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record JoinRequestDto(
    @NotBlank
    @Email
    String email,
    @NotBlank
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,16}$",
        message = "비밀번호 입력값이 형식에 맞지 않습니다."
    )
    String password,
    @NotBlank
    String name
) {
}
