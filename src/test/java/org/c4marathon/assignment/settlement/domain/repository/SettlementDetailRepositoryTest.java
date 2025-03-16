package org.c4marathon.assignment.settlement.domain.repository;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.settlement.domain.SettlementType.*;

import java.util.List;

import org.c4marathon.assignment.IntegrationTestSupport;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.global.util.AccountNumberUtil;
import org.c4marathon.assignment.settlement.domain.Settlement;
import org.c4marathon.assignment.settlement.domain.SettlementDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class SettlementDetailRepositoryTest extends IntegrationTestSupport {
	@Autowired
	private SettlementRepository settlementRepository;

	@Autowired
	private SettlementDetailRepository settlementDetailRepository;

	@Autowired
	private AccountRepository accountRepository;

	@Transactional
	@DisplayName("accountId를 통해 정산 요청 받은 데이터를 조회한다.")
	@Test
	void findByAccountId() {
	    // given
		String accountNumber1 = generateAccountNumber();
		String accountNumber2 = generateAccountNumber();
		String accountNumber3 = generateAccountNumber();
		Account account1 = createAccount(accountNumber1, 10000L); //정산 요청한 사람
		Account account2 = createAccount(accountNumber2, 20000L); //정산 요청 받은 사람 1
		Account account3 = createAccount(accountNumber3, 30000L); //정산 요청 받은 사람 2

		Settlement settlement = Settlement.create(account1.getAccountNumber(), 30000, EQUAL);
		settlementRepository.save(settlement);

		SettlementDetail settlementDetail1 = SettlementDetail.create(settlement, account2.getAccountNumber(), 10000);
		SettlementDetail settlementDetail2 = SettlementDetail.create(settlement, account3.getAccountNumber(), 10000);
		settlementDetailRepository.saveAll(List.of(settlementDetail1, settlementDetail2));

		// when
		List<SettlementDetail> findSettlementDetail = settlementDetailRepository.findByAccountNumber(account2.getAccountNumber());

		// then
		assertThat(findSettlementDetail)
			.hasSize(1)
			.extracting("accountNumber", "amount")
			.containsExactly(tuple(account2.getAccountNumber(), 10000));

		assertThat(findSettlementDetail.get(0).getSettlement())
			.isNotNull()
			.extracting("id", "requestAccountNumber", "totalAmount", "type")
			.containsExactly(settlement.getId(), settlement.getRequestAccountNumber(), settlement.getTotalAmount(), settlement.getType());
	}
	private Account createAccount(String accountNumber, long money) {
		Account account = Account.create(accountNumber, money);
		accountRepository.save(account);
		return account;
	}

	private String generateAccountNumber() {
		return AccountNumberUtil.generateAccountNumber("3333");
	}

}