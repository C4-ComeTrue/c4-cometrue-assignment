package org.c4marathon.assignment.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinDto (
        @NotBlank(message = "아이디는 필수 입력 항목입니다.")
        String userId,

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        String userPw,

        @NotBlank(message = "이름은 필수 입력 항목입니다.")
        String name
){
}
