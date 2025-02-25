package org.c4marathon.assignment.settlement.service;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.settlement.domain.SettlementType.*;

import java.util.List;
import java.util.stream.Collectors;

import org.c4marathon.assignment.IntegrationTestSupport;
import org.c4marathon.assignment.global.util.AccountNumberUtil;
import org.c4marathon.assignment.settlement.domain.Settlement;
import org.c4marathon.assignment.settlement.domain.SettlementDetail;
import org.c4marathon.assignment.settlement.domain.SettlementType;
import org.c4marathon.assignment.settlement.domain.repository.SettlementDetailRepository;
import org.c4marathon.assignment.settlement.domain.repository.SettlementRepository;
import org.c4marathon.assignment.settlement.dto.ReceivedSettlementResponse;
import org.c4marathon.assignment.settlement.dto.SettlementDetailInfo;
import org.c4marathon.assignment.settlement.dto.SettlementRequest;
import org.c4marathon.assignment.settlement.dto.SettlementResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class SettlementServiceTest extends IntegrationTestSupport {

	@Autowired
	private SettlementRepository settlementRepository;

	@Autowired
	private SettlementDetailRepository settlementDetailRepository;

	@Autowired
	private SettlementService settlementService;

	private static final String REQUEST_ACCOUNT_NUMBER = "33331234567811";
	private static final List<String> PARTICIPANT_ACCOUNT_NUMBERS = List.of("33331234123456", "33331234123457", "33331234123458");

	private static final int TOTAL_NUMBER = 4; // 요청자 포함

	@DisplayName("정산 요청 시 정산 요청 데이터(Settlement, SettlementDetail)를 생성한다.")
	@Transactional
	@Test
	void createSettlement() {
	    // given
		int totalAmount = 30000;
		SettlementRequest request = new SettlementRequest(TOTAL_NUMBER, totalAmount, PARTICIPANT_ACCOUNT_NUMBERS, EQUAL);

	    // when
		settlementService.createSettlement(REQUEST_ACCOUNT_NUMBER, request);

	    // then
		List<Settlement> settlements = settlementRepository.findByRequestAccountNumber(REQUEST_ACCOUNT_NUMBER);

		assertThat(settlements).hasSize(1);
		assertThat(settlements.get(0))
			.extracting("requestAccountNumber", "totalAmount", "amount", "type")
			.containsExactly(REQUEST_ACCOUNT_NUMBER, totalAmount, 7500, EQUAL);

		assertThat(settlements.get(0).getSettlementDetails())
			.hasSize(3)
			.extracting("accountNumber", "amount")
			.containsExactlyInAnyOrder(
				tuple("33331234123456", 7500),
				tuple("33331234123457", 7500),
				tuple("33331234123458", 7500)
			);
	}

	@DisplayName("정산 요청(1/N) 시 정확하게 나누어 떨어지지 않는 금액은 랜덤으로 1원씩 더한다")
	@Transactional
	@Test
	void divideEquallyWithRemainder() {
	    // given
		int totalAmount = 30006;
		SettlementRequest request = new SettlementRequest(TOTAL_NUMBER, totalAmount, PARTICIPANT_ACCOUNT_NUMBERS, EQUAL);

	    // when
		settlementService.createSettlement(REQUEST_ACCOUNT_NUMBER, request);

	    // then
		List<Settlement> settlements = settlementRepository.findByRequestAccountNumber(REQUEST_ACCOUNT_NUMBER);

		Settlement settlement = settlements.get(0);
		List<SettlementDetail> settlementDetails = settlement.getSettlementDetails();

		List<Integer> actualAmounts = settlementDetails.stream()
			.map(SettlementDetail::getAmount)
			.collect(Collectors.toList());

		actualAmounts.add(settlement.getAmount());

		int totalCalculatedAmount = actualAmounts.stream().mapToInt(Integer::intValue).sum();
		assertThat(totalCalculatedAmount).isEqualTo(totalAmount);

		assertThat(settlement)
			.extracting("requestAccountNumber", "totalAmount", "type")
			.containsExactly(REQUEST_ACCOUNT_NUMBER, totalAmount, SettlementType.EQUAL);
		assertThat(settlement.getTotalAmount()).isEqualTo(totalCalculatedAmount);

		assertThat(actualAmounts)
			.containsExactlyInAnyOrder(7501, 7502, 7502, 7501);
	}

	@DisplayName("정산 요청 시(랜덤) N-1명에게 10원 단위로 배분되고, 남은 금액을 남은 한 명에게 배분한다. ")
	@Transactional
	@Test
	void divideRandom() {
	    // given
		int totalAmount = 50000;

		SettlementRequest request = new SettlementRequest(TOTAL_NUMBER, totalAmount, PARTICIPANT_ACCOUNT_NUMBERS, SettlementType.RANDOM);

		// when
		settlementService.createSettlement(REQUEST_ACCOUNT_NUMBER, request);

	    // then
		List<Settlement> settlements = settlementRepository.findByRequestAccountNumber(REQUEST_ACCOUNT_NUMBER);

		Settlement settlement = settlements.get(0);
		List<SettlementDetail> settlementDetails = settlement.getSettlementDetails();

		List<Integer> actualAmounts = settlementDetails.stream()
			.map(SettlementDetail::getAmount)
			.collect(Collectors.toList());

		actualAmounts.add(settlement.getAmount());

		assertThat(settlement)
			.extracting("requestAccountNumber", "totalAmount", "type")
			.containsExactly(REQUEST_ACCOUNT_NUMBER, totalAmount, SettlementType.RANDOM);

		int totalCalculatedAmount = actualAmounts.stream().mapToInt(Integer::intValue).sum();
		assertThat(totalCalculatedAmount).isEqualTo(totalAmount);

		assertThat(actualAmounts).allMatch(amount -> amount % 10 == 0);
	}


	@DisplayName("정산 요청 리스트를 조회한다.(받을 돈을 조회)")
	@Transactional
	@Test
	void getRequestedSettlements() {
	    // given
		int totalAmount = 30000;
		Settlement settlement = Settlement.create(REQUEST_ACCOUNT_NUMBER, totalAmount, SettlementType.EQUAL);
		settlement.setAmount(7500);
		settlementRepository.save(settlement);

		List<SettlementDetail> settlementDetails = PARTICIPANT_ACCOUNT_NUMBERS.stream()
			.map(participantId -> SettlementDetail.create(settlement, participantId, 7500))
			.toList();
		settlementDetailRepository.saveAll(settlementDetails);

	    // when
		List<SettlementResponse> response = settlementService.getRequestedSettlements(REQUEST_ACCOUNT_NUMBER);

		// then
		assertThat(response).hasSize(1);
		assertThat(response.get(0))
			.extracting("requestAccountNumber", "totalAmount")
			.containsExactly(REQUEST_ACCOUNT_NUMBER, totalAmount);

		assertThat(response.get(0).members())
			.extracting(SettlementDetailInfo::accountNumber, SettlementDetailInfo::amount)
			.containsExactlyInAnyOrder(
				tuple("33331234123456", 7500),
				tuple("33331234123457", 7500),
				tuple("33331234123458", 7500)
			);
	}

	@DisplayName("정산 요청 받은 리스트 조회한다.(보내야 할 돈 조회)")
	@Transactional
	@Test
	void getReceivedSettlements() {
		// given
		int totalAmount = 30000;
		Settlement settlement = Settlement.create(REQUEST_ACCOUNT_NUMBER, totalAmount, SettlementType.EQUAL);
		settlement.setAmount(7500); // 요청자의 금액 설정
		settlementRepository.save(settlement);

		List<SettlementDetail> settlementDetails = PARTICIPANT_ACCOUNT_NUMBERS.stream()
			.map(participantId -> SettlementDetail.create(settlement, participantId, 7500))
			.toList();
		settlementDetailRepository.saveAll(settlementDetails);

		// when
		List<ReceivedSettlementResponse> responses = settlementService.getReceivedSettlements("33331234123457");

		// then
		assertThat(responses).hasSize(1);

		ReceivedSettlementResponse response = responses.get(0);
		assertThat(response)
			.extracting("settlementId", "requestAccountNumber", "totalAmount", "myAccountNumber", "mySettlementAmount")
			.containsExactly(settlement.getId(), REQUEST_ACCOUNT_NUMBER, totalAmount, "33331234123457", 7500);
	}
}