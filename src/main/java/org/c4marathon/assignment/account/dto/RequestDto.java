package org.c4marathon.assignment.account.dto;

import org.c4marathon.assignment.account.entity.Type;

import jakarta.validation.constraints.NotBlank;

public class RequestDto {

    public record AccountDto(

        @NotBlank
        Type type,
        @NotBlank
        String token
    ) {
    }
}
