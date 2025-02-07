package org.c4marathon.assignment.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUserReq(
        @Size(max = 100)
        @NotBlank
        String username,

        @Email
        @NotBlank
        String email,

        @Size(max = 100)
        @NotBlank
        String nickname
) {
}
