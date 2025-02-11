package org.c4marathon.assignment.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.c4marathon.assignment.domain.Settlement;
import org.c4marathon.assignment.domain.SettlementMember;
import org.c4marathon.assignment.domain.enums.SettlementStatus;
import org.c4marathon.assignment.domain.enums.SettlementType;
import org.c4marathon.assignment.dto.request.RemittanceRequestDto;
import org.c4marathon.assignment.dto.request.SettlementRequestDto;
import org.c4marathon.assignment.repository.SettlementRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettlementService {

	private final SettlementRepository settlementRepository;

	/**
	 * [Step3] 정산
	 * @param requestDto - requestAccountId, totalAmount, type
	 * */
	public void divideMoney(SettlementRequestDto requestDto) {

		if (requestDto.type() == SettlementType.EQUAL) {
			divideEqual(requestDto);
		} else {
			divideRandom(requestDto);
		}
	}

	/**
	 * [Step3] 1/n정산
	 * 딱 나누어 떨어지지 않을 경우 정해진 순서 없이 아무나에게 1씩 추가 정산 요청
	 * */
	@Transactional
	protected void divideEqual(SettlementRequestDto requestDto) {
		List<Long> settlementMemberIds = requestDto.settlementMemberIds();
		int totalPeople = settlementMemberIds.size();
		int totalAmount = requestDto.totalAmount();

		long baseAmount = totalAmount / totalPeople;
		long remainAmount = totalAmount % totalPeople;

		Settlement settlement = new Settlement(requestDto.requestAccountId(), totalAmount, totalAmount, totalPeople,
			SettlementType.EQUAL, null);

		//기본 금액 전체 할당
		List<SettlementMember> updatedMembers = settlementMemberIds.stream().map(memberId -> new SettlementMember(
			memberId , baseAmount, SettlementStatus.PENDING, settlement
		)).collect(Collectors.toList());

		//남은 금액 1원씩 할당
		if(remainAmount > 0) {
			Collections.shuffle(updatedMembers);
			for (int i = 0; i < remainAmount; i++) {
				SettlementMember member = updatedMembers.get(i);
				member.updateAmount(1);
			}
		}

		settlement.addSettlementMembers(updatedMembers);
		settlementRepository.save(settlement);
	}

	/**
	 * [Step3] Random 정산 (0원 가능)
	 * 카카오페이의 돈 뿌리기 처럼 돈을 못받는? 정산하지 않아도 되는 사람이 생길 수도 있다.
	 * */
	@Transactional
	protected void divideRandom(SettlementRequestDto requestDto) {
		List<Long> settlementMemberIds = requestDto.settlementMemberIds();
		int totalPeople = settlementMemberIds.size();
		int totalAmount = requestDto.totalAmount();
		Random random = new Random();

		//Settlement
		Settlement settlement = new Settlement(requestDto.requestAccountId(), totalAmount, totalAmount, totalPeople, SettlementType.RANDOM, null);

		//랜덤으로 돈 배정
		Collections.shuffle(settlementMemberIds);
		int remainAmount = totalAmount;
		List<SettlementMember> updatedMembers = new ArrayList<>();

		for(int i = 0; i < totalPeople - 1; i++) {
			int randomAmount = random.nextInt(remainAmount + 1);
			remainAmount -= randomAmount;
			if (randomAmount > 0) { // 0원을 받는 사람은 저장X
				updatedMembers.add(new SettlementMember(
					settlementMemberIds.get(i),
					randomAmount,
					SettlementStatus.PENDING,
					settlement
				));
			}
		}

		//남은 돈이 있으면 마지막 사람에게 할당
		if(remainAmount > 0) {
			updatedMembers.add(new SettlementMember(
				settlementMemberIds.get(totalPeople - 1),
				remainAmount,
				SettlementStatus.PENDING,
				settlement
			));
		}

		settlement.addSettlementMembers(updatedMembers);
		settlementRepository.save(settlement);
	}

	@Transactional
	public void remittanceMoney(RemittanceRequestDto requestDto) {
		// 돈 내면 pending, success
		//돈을 내면 Settlement
	}
}
