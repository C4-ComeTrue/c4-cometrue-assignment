package org.c4marathon.assignment.user.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDto(
        @NotBlank(message = "아이디는 필수 입력 항목입니다.")
        String userId,

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        String userPw
) {
}
