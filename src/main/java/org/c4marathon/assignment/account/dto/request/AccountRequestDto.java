package org.c4marathon.assignment.account.dto.request;

import org.c4marathon.assignment.account.entity.Type;
import org.c4marathon.assignment.account.validation.ValidType;

import jakarta.validation.constraints.NotNull;

public record AccountRequestDto(
    @NotNull
    @ValidType
    Type type
) {
}
