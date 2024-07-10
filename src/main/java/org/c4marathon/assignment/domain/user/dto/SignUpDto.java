package org.c4marathon.assignment.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class SignUpDto {

    public record Req(
            @Email(message = "이메일 형식이 아닙니다.") @NotNull String email,
            @NotNull String password
    ) {
    }

    public record Res(
            String accessToken
    ) {
    }
}
