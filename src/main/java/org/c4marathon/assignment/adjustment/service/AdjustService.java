package org.c4marathon.assignment.adjustment.service;

import java.util.ArrayList;
import java.util.List;

import org.c4marathon.assignment.adjustment.dto.res.AdjustResponseDto;
import org.c4marathon.assignment.adjustment.entity.Adjust;
import org.c4marathon.assignment.adjustment.entity.AdjustTarget;
import org.c4marathon.assignment.adjustment.repository.AdjustRepository;
import org.c4marathon.assignment.adjustment.repository.AdjustTargetRepository;
import org.c4marathon.assignment.util.common.Status;
import org.c4marathon.assignment.util.exceptions.BaseException;
import org.c4marathon.assignment.util.exceptions.ErrorCode;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdjustService {

    private final AdjustRepository adjustRepository;

    private final AdjustTargetRepository adjustTargetRepository;

    // 정산 조회
    public AdjustResponseDto getAdjust(Long id) throws BaseException {

        // 정산
        Adjust adjust = adjustRepository.findById(id).orElseThrow(() ->
            new BaseException(
                ErrorCode.COMMON_NOT_FOUND.name()
                , ErrorCode.COMMON_NOT_FOUND.getMessage()
                , "요청하신 정산 요청을 찾을 수 없습니다."
            )
        );

        // 정산 대상자
        List<AdjustTarget> adjustTarget = adjustTargetRepository.findByAdjustId(id);

        if (adjustTarget.isEmpty()) {
            throw new BaseException(ErrorCode.NO_ADJUST_TARGET_REMAINING.name(), ErrorCode.NO_ADJUST_TARGET_REMAINING.getMessage());
        }

        List<AdjustTarget> activeTargets = new ArrayList<>();
        List<AdjustTarget> pendingTargets = new ArrayList<>();
        List<AdjustTarget> completedTargets = new ArrayList<>();

        // 정산 대상자 상태 구분
        for (AdjustTarget target : adjustTarget) {
            
            // 정산 대상자가 정산하지 않은 상태.
            if (target.getStatus().equals(Status.ACTIVE)) {
                activeTargets.add(target);
            // 정산 대상자는 정산했지만, 정산 요청자가 받지 않은 상태.
            } else if (target.getStatus().equals(Status.PENDING)) {
                pendingTargets.add(target);
            // 정산 대상자에 대한 정산이 완료된 상태
            } else if (target.getStatus().equals(Status.COMPLETED)) {
                completedTargets.add(target);
            }
        }

        return AdjustResponseDto.entityToDto(
            adjust
            , activeTargets
            , pendingTargets
            , completedTargets
        );
    }

    // 정산 하기 생성 요청
    public void postAdjust() {

    }

    // 정산 완료 요청
    public void patchAdjust() {

    }

    // 정산 삭제 요청
    public void deleteAdjust() {

    }

    // 정산 받기 요청
    public void postAdjustAndAdjustTarget() {

    }
}
