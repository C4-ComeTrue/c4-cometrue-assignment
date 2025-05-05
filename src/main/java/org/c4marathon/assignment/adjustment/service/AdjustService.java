package org.c4marathon.assignment.adjustment.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.c4marathon.assignment.adjustment.dto.req.AdjustRequestDto;
import org.c4marathon.assignment.adjustment.dto.req.AdjustTargetRequestDto;
import org.c4marathon.assignment.adjustment.dto.res.AdjustResponseDto;
import org.c4marathon.assignment.adjustment.entity.Adjust;
import org.c4marathon.assignment.adjustment.entity.AdjustTarget;
import org.c4marathon.assignment.adjustment.repository.AdjustRepository;
import org.c4marathon.assignment.adjustment.repository.AdjustTargetRepository;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.util.common.AdjustmentStatus;
import org.c4marathon.assignment.util.common.Status;
import org.c4marathon.assignment.util.exceptions.BaseException;
import org.c4marathon.assignment.util.exceptions.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void postAdjust(
        AdjustRequestDto adjustRequestDto
    ) {
        try {
            // 총 정산 금액
            final Long totalAmount = adjustRequestDto.adjustTotalAmount();
            final Integer peopleCount = adjustRequestDto.adjustTargetRequestDtoList().size();

            if (totalAmount < peopleCount) {
                log.error("요청하신 금액보다 사람의 수가 작을 수 없습니다.");
                throw new BaseException(ErrorCode.INSUFFICIENT_ADJUST_AMOUNT.name(), ErrorCode.INSUFFICIENT_ADJUST_AMOUNT.getMessage());
            }

            // 정산 테이블 데이터 생성
            Adjust adjust = Adjust.builder()
                .adjustTotalAmount(totalAmount)
                .adjustmentStatus(adjustRequestDto.adjustmentStatus())
                .build();

            // 정산 인원에 따른 데이터 생성
            List<AdjustTarget> adjustTargetList = new ArrayList<>();

            // 정산 금액 산정 방식에 따른 개인별 정산 금액 산출
            if (adjustRequestDto.adjustmentStatus().equals(AdjustmentStatus.EQUAL)) {
                adjustTargetList = splitEqually(totalAmount, adjustRequestDto.adjustTargetRequestDtoList(), adjust);
            } else if (adjustRequestDto.adjustmentStatus().equals(AdjustmentStatus.RANDOM)) {
                adjustTargetList = splitRandomly(totalAmount, adjustRequestDto.adjustTargetRequestDtoList(), adjust);
            }

            adjustRepository.save(adjust);
            adjustTargetRepository.saveAll(adjustTargetList);
        } catch (BaseException e) {
            log.error("업무 예외 발생: {}", e.getMessage(), e);
            throw e; // 그대로 던짐
        } catch (Exception e) {
            log.error("예상치 못한 에러가 발생했습니다: {}", e.getMessage(), e);
            throw new BaseException(ErrorCode.UNEXPECTED_ERROR.name(), ErrorCode.UNEXPECTED_ERROR.getMessage());
        } finally {
            // 정산 요청 메시징 기능 추가 시 해당 위치에 추가
        }
    }

    // 1/n 정산하기 요청
    private List<AdjustTarget> splitEqually(Long totalAmount, List<AdjustTargetRequestDto> adjustTargetRequestDtoList, Adjust adjust) {

        final Integer peopleCount = adjustTargetRequestDtoList.size();

        // 개인별 정산 금액
        final Long individualAmount = totalAmount / peopleCount;
        // 1/n으로 정산 금액을 산출할 때 남은 금액을 순서대로 분배하기 위한 변수 선언
        Long remainingAmount = totalAmount % peopleCount;

        // 정산 인원에 따른 데이터 생성
        List<AdjustTarget> adjustTargetList = new ArrayList<>();


        for (AdjustTargetRequestDto adjustTargetRequestDto : adjustTargetRequestDtoList) {

            // 1/n을 하고 남는 금액의 경우 처리를 위한 분배
            adjustTargetList.add(
                createAdjustTarget(
                    remainingAmount > 0 ? individualAmount + 1 : individualAmount
                    , adjust
                    , adjustTargetRequestDto.memberId()
                )
            );

            if (remainingAmount > 0) {
                remainingAmount -= 1;
            }
        }

        return adjustTargetList;
    }

    // 랜덤으로 정산하기 요청
    private List<AdjustTarget> splitRandomly(Long totalAmount, List<AdjustTargetRequestDto> adjustTargetRequestDtoList, Adjust adjust) {

        final Integer peopleCount = adjustTargetRequestDtoList.size();

        SecureRandom random = new SecureRandom();

        // 정산 인원에 따른 데이터 생성
        List<AdjustTarget> adjustTargetList = new ArrayList<>();
        long remaining = totalAmount;

        for (int i = 0; i < peopleCount - 1; i++) {

            // 최소 1원은 남겨야 하므로 1 ~ (remainingAmount - (남은 사람 수)) 사이로 생성
            long max = remaining - (peopleCount - i - 1);
            long randomAmount;

            if (max <= 1) {
                randomAmount = 1L;
            } else {
                randomAmount = 1 + random.nextInt((int) max); // 1 ~ max 사이의 랜덤값
            }

            remaining -= randomAmount;

            // 1/n을 하고 남는 금액의 경우 처리를 위한 분배
            adjustTargetList.add(
                createAdjustTarget(
                    randomAmount
                    , adjust
                    , adjustTargetRequestDtoList.get(i).memberId()
                )
            );
        }

        // 마지막 사람은 남은 금액 전부
        adjustTargetList.add(
            createAdjustTarget(
                remaining
                , adjust
                , adjustTargetRequestDtoList.get(peopleCount-1).memberId()
            )
        );

        return adjustTargetList;
    }

    private AdjustTarget createAdjustTarget(Long amount, Adjust adjust, Long memberId) {
        return AdjustTarget.builder()
            .adjust(adjust)
            .adjustAmount(amount)
            .member(Member.builder().id(memberId).build())
            .build();
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
