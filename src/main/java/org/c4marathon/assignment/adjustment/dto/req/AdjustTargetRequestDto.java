package org.c4marathon.assignment.adjustment.dto.req;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AdjustTargetRequestDto(
    @NotNull
    Long id,
    @NotEmpty
    Long memberId
) {
}
