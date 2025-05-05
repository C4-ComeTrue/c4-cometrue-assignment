package org.c4marathon.assignment.adjustment.dto.req;

import java.util.List;

import org.c4marathon.assignment.util.common.AdjustmentStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AdjustRequestDto(
    @NotNull
    Long id,
    @NotNull
    Long adjustTotalAmount,
    @NotNull
    AdjustmentStatus adjustmentStatus,
    @NotEmpty
    List<@Valid @NotNull AdjustTargetRequestDto> adjustTargetRequestDtoList
) {
}
