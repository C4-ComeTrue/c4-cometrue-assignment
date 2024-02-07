package org.c4marathon.assignment.account.dto.request;

import org.c4marathon.assignment.account.entity.Type;

import jakarta.validation.constraints.NotBlank;

public record AccountRequestDto(
    @NotBlank
    Type type
) {
}
