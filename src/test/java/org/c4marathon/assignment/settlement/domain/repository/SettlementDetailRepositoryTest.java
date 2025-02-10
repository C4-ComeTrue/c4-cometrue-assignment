package org.c4marathon.assignment.settlement.domain.repository;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.settlement.domain.SettlementType.*;

import java.util.List;

import org.c4marathon.assignment.IntegrationTestSupport;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.settlement.domain.Settlement;
import org.c4marathon.assignment.settlement.domain.SettlementDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SettlementDetailRepositoryTest extends IntegrationTestSupport {
	@Autowired
	private SettlementRepository settlementRepository;

	@Autowired
	private SettlementDetailRepository settlementDetailRepository;

	@Autowired
	private AccountRepository accountRepository;

	@DisplayName("accountId를 통해 정산 요청 받은 데이터를 조회한다.")
	@Test
	void findByAccountId() throws Exception {
	    // given
		Account account1 = createAccount(10000L); //정산 요청한 사람
		Account account2 = createAccount(20000L); //정산 요청 받은 사람 1
		Account account3 = createAccount(30000L); //정산 요청 받은 사람 2

		Settlement settlement = Settlement.create(account1.getId(), 30000, EQUAL);
		settlementRepository.save(settlement);

		SettlementDetail settlementDetail1 = SettlementDetail.create(settlement, account2.getId(), 10000);
		SettlementDetail settlementDetail2 = SettlementDetail.create(settlement, account3.getId(), 10000);
		settlementDetailRepository.saveAll(List.of(settlementDetail1, settlementDetail2));

		// when
		List<SettlementDetail> findSettlementDetail = settlementDetailRepository.findByAccountId(account2.getId());

		// then
		assertThat(findSettlementDetail)
			.hasSize(1)
			.extracting("accountId", "amount")
			.containsExactly(tuple(account2.getId(), 10000));

		assertThat(findSettlementDetail.get(0).getSettlement())
			.isNotNull()
			.extracting("id", "requestAccountId", "totalAmount", "type")
			.containsExactly(settlement.getId(), settlement.getRequestAccountId(), settlement.getTotalAmount(), settlement.getType());
	}
	private Account createAccount(long money) {
		Account account = Account.create(money);
		accountRepository.save(account);
		return account;
	}

}