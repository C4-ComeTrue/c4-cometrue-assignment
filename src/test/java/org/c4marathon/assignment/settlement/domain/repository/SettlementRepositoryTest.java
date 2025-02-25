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

class SettlementRepositoryTest extends IntegrationTestSupport {

	@Autowired
	private SettlementRepository settlementRepository;

	@Autowired
	private SettlementDetailRepository settlementDetailRepository;

	@Autowired
	private AccountRepository accountRepository;

	@DisplayName("정산을 요청한 AccountId로 정산 데이터를 조회한다.")
	@Transactional
	@Test
	void findByRequestAccountId() {
	    // given
		String accountNumber1 = generateAccountNumber();
		String accountNumber2 = generateAccountNumber();
		String accountNumber3 = generateAccountNumber();
		Account account1 = createAccount(accountNumber1, 10000L);
		Account account2 = createAccount(accountNumber2, 20000L);
		Account account3 = createAccount(accountNumber3, 30000L);

		Settlement settlement = Settlement.create(account1.getAccountNumber(), 30000, EQUAL);
		settlementRepository.save(settlement);

		SettlementDetail settlementDetail1 = SettlementDetail.create(settlement, account2.getAccountNumber(), 10000);
		SettlementDetail settlementDetail2 = SettlementDetail.create(settlement, account3.getAccountNumber(), 10000);
		settlementDetailRepository.saveAll(List.of(settlementDetail1, settlementDetail2));

	    // when
		List<Settlement> findSettlement = settlementRepository.findByRequestAccountNumber(account1.getAccountNumber());

		// then
		assertThat(findSettlement.get(0))
			.extracting("requestAccountNumber", "totalAmount")
			.containsExactly(account1.getAccountNumber(), 30000);

		assertThat(findSettlement.get(0).getSettlementDetails())
			.extracting("accountNumber", "amount")
			.containsExactlyInAnyOrder(
				tuple(account2.getAccountNumber(), 10000),
				tuple(account3.getAccountNumber(), 10000)
			);
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
