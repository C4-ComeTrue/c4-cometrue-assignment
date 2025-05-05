package org.c4marathon.assignment.adjustment.dto.res;

import java.util.ArrayList;
import java.util.List;

import org.c4marathon.assignment.adjustment.entity.Adjust;
import org.c4marathon.assignment.adjustment.entity.AdjustTarget;
import org.c4marathon.assignment.util.common.AdjustmentStatus;
import org.c4marathon.assignment.util.common.Status;

public record AdjustResponseDto(
    Long id,
    Long adjustTotalAmount,
    Status status,
    AdjustmentStatus adjustmentStatus,
    List<AdjustTarget> activeTargets,
    List<AdjustTarget> pendingTargets,
    List<AdjustTarget> completedTargets
) {
    public static AdjustResponseDto entityToDto(
        Adjust adjust
        , List<AdjustTarget> activeTargets
        , List<AdjustTarget> pendingTargets
        , List<AdjustTarget> completedTargets
    ) {
        return new AdjustResponseDto(
            adjust.getId()
            , adjust.getAdjustTotalAmount()
            , adjust.getStatus()
            , adjust.getAdjustmentStatus()
            , activeTargets
            , pendingTargets
            , completedTargets
        );
    }
}
