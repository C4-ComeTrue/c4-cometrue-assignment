package org.c4marathon.assignment.settlement.service;

import static org.c4marathon.assignment.global.util.SettlementUtil.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.c4marathon.assignment.settlement.domain.Settlement;
import org.c4marathon.assignment.settlement.domain.SettlementDetail;
import org.c4marathon.assignment.settlement.domain.SettlementType;
import org.c4marathon.assignment.settlement.domain.repository.SettlementDetailRepository;
import org.c4marathon.assignment.settlement.domain.repository.SettlementRepository;
import org.c4marathon.assignment.settlement.dto.ReceivedSettlementResponse;
import org.c4marathon.assignment.settlement.dto.SettlementRequest;
import org.c4marathon.assignment.settlement.dto.SettlementResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettlementService {
	private final SettlementRepository settlementRepository;
	private final SettlementDetailRepository settlementDetailRepository;

	/**
	 * 정산 요청하기
	 * @param requestAccountNumber
	 * @param request
	 */
	@Transactional
	public void createSettlement(String requestAccountNumber, SettlementRequest request) {

		Settlement settlement = Settlement.create(requestAccountNumber, request.totalAmount(), request.type());

		List<Integer> amounts = calculateSettlementAmounts(request.totalAmount(), request.totalNumber(), request.type());

		settlement.setAmount(amounts.get(0));
		settlementRepository.save(settlement);

		List<SettlementDetail> settlementDetails = IntStream.range(0, request.accountNumbers().size())
			.mapToObj(i -> SettlementDetail.create(settlement, request.accountNumbers().get(i), amounts.get(i + 1)))
			.toList();

		settlementDetailRepository.saveAll(settlementDetails);
	}

	/**
	 * 정산 요청한 리스트 조회(받을 돈을 조회)
	 * @param requestAccountNumber
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<SettlementResponse> getRequestedSettlements(String requestAccountNumber) {
		List<Settlement> settlements = settlementRepository.findByRequestAccountNumber(requestAccountNumber);

		return SettlementResponse.fromEntities(settlements);
	}

	/**
	 * 요청받은 정산 리스트 조회(보내야 할 돈 조회)
	 * @param accountNumber
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<ReceivedSettlementResponse> getReceivedSettlements(String accountNumber) {
		List<SettlementDetail> settlementDetails = settlementDetailRepository.findByAccountNumber(accountNumber);

		return ReceivedSettlementResponse.fromEntities(settlementDetails);
	}

	private List<Integer> calculateSettlementAmounts(int totalAmount, int totalNumber, SettlementType type) {
		if (type == SettlementType.EQUAL) {
			return getEquallySettlement(totalAmount, totalNumber);
		} else {
			return getRandomSettlement(totalAmount, totalNumber);
		}
	}

	/**
	 * 10원 단위로 랜덤한 금액을 N - 1명에게 정산하고 남은 금액을 남은 1명에게 정산하는 방법
	 * @param totalAmount
	 * @param totalNumber
	 * @return
	 */
	private List<Integer> getRandomSettlement(int totalAmount, int totalNumber) {
		// 총 금액을 10원 단위로 변환
		int totalAmountDivided = totalAmount / 10;

		// 총 인원 - 1명을 대상으로 10원 단위로 랜덤한 금액을 배정
		List<Integer> amounts = IntStream.range(0, totalNumber - 1)
			.mapToObj(i -> getRandomInt(totalAmountDivided - (totalNumber - i - 1)) + 1)
			.sorted()
			.toList();

		// 첫 번째 사람은 cutPoints의 첫 번째 값, 나머지는 차이를 통해 분배
		List<Integer> result = IntStream.range(0, totalNumber - 1)
			.mapToObj(i -> (i == 0 ? amounts.get(i) * 10 : (amounts.get(i) - amounts.get(i - 1)) * 10))
			.collect(Collectors.toList());

		// 마지막 사람에게 남은 금액 배분
		int lastAmount = totalAmount - result.stream().mapToInt(Integer::intValue).sum();
		result.add(lastAmount);

		return result;
	}

	/**
	 * 1 / N 으로 정산을 하고 정확히 나누어 떨어지지 않아 남는 금액은 앞에서부터 +1원씩 더해준다.
	 * @param totalAmount
	 * @param totalNumber
	 * @return
	 */
	private List<Integer> getEquallySettlement(int totalAmount, int totalNumber) {
		int baseAmount = totalAmount / totalNumber;
		int remainder = totalAmount % totalNumber;

		List<Integer> result = IntStream.range(0, totalNumber)
			.mapToObj(i -> baseAmount + (i < remainder ? 1 : 0))  // 앞에서부터 remainder 개수만큼 +1 배분
			.collect(Collectors.toList());
		Collections.shuffle(result);

		return result;

	}
}
