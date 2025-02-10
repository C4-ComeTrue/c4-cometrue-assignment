package org.c4marathon.assignment.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import org.c4marathon.assignment.dto.request.PostSettlementReq;
import org.c4marathon.assignment.dto.response.SettlementDetailInfoRes;
import org.c4marathon.assignment.dto.response.SettlementInfoRes;
import org.c4marathon.assignment.entity.Settlement;
import org.c4marathon.assignment.entity.SettlementDetail;
import org.c4marathon.assignment.entity.SettlementType;
import org.c4marathon.assignment.exception.CustomException;
import org.c4marathon.assignment.exception.ErrorCode;
import org.c4marathon.assignment.repository.SettlementDetailRepository;
import org.c4marathon.assignment.repository.SettlementRepository;
import org.c4marathon.assignment.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettlementService {
	private final SettlementRepository settlementRepository;
	private final SettlementDetailRepository settlementDetailRepository;
	private final UserRepository userRepository;

	/**
	 * 정산 요청 기능
	 */
	@Transactional
	public SettlementInfoRes requestSettlement(PostSettlementReq settlementReq) {
		List<Long> targetUsers = makeTargetUsers(settlementReq);

		validateUsers(targetUsers);

		Settlement settlement = Settlement.builder()
			.requester(settlementReq.requester())
			.totalAmount(settlementReq.totalAmount())
			.type(settlementReq.type())
			.build();

		settlementRepository.save(settlement);

		List<Long> amounts = generateAmounts(settlementReq, targetUsers.size());

		List<SettlementDetail> settlementDetails = createSettlementDetails(targetUsers, settlement, amounts);
		settlementDetailRepository.saveAll(settlementDetails);

		return new SettlementInfoRes(settlementDetails.stream().map(SettlementDetailInfoRes::new).toList());
	}

	public List<Long> makeTargetUsers(PostSettlementReq settlementReq) {
		List<Long> targetUsers = new ArrayList<>(settlementReq.userIds());
		targetUsers.add(settlementReq.requester());
		Collections.shuffle(targetUsers);
		return targetUsers;
	}

	public void validateUsers(List<Long> targetUsers) {
		int userCnt = userRepository.countByIds(targetUsers);
		if (userCnt != targetUsers.size()) {
			throw new CustomException(ErrorCode.INVALID_USER_ID);
		}
	}

	public List<Long> generateAmounts(PostSettlementReq settlementReq, int userCount) {
		return (settlementReq.type() == SettlementType.EQUAL)
			? generateEqualAmount(userCount, settlementReq.totalAmount())
			: generateRandomAmount(userCount, settlementReq.totalAmount());
	}

	private List<SettlementDetail> createSettlementDetails(List<Long> targetUsers, Settlement settlement,
		List<Long> amounts) {
		return IntStream.range(0, targetUsers.size())
			.filter(i -> !targetUsers.get(i).equals(settlement.getRequester()))
			.mapToObj(i -> SettlementDetail.builder()
				.userId(targetUsers.get(i))
				.settlement(settlement)
				.amount(amounts.get(i))
				.build())
			.toList();
	}

	/**
	 * 1/N 정산 금액 계산
	 */
	public List<Long> generateEqualAmount(int count, long totalAmount) {
		long equalAmount = totalAmount / count;
		long remainder = totalAmount % count;

		return IntStream.range(0, count)
			.mapToLong(i -> i < remainder ? equalAmount + 1 : equalAmount)
			.boxed()
			.toList();
	}

	/**
	 * 랜덤 정산 금액 계산
	 */
	public List<Long> generateRandomAmount(int count, long totalAmount) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		List<Long> amounts = new ArrayList<>();
		long sum = 0;

		for (int i = 0; i < count - 1; i++) {
			long amount = (long)(random.nextDouble() * (totalAmount - sum) / (count - i));
			amounts.add(amount);
			sum += amount;
		}
		amounts.add(totalAmount - sum);

		return amounts;
	}
}
