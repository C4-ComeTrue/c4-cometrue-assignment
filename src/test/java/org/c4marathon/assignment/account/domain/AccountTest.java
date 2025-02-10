package org.c4marathon.assignment.account.domain;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.global.util.Const.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccountTest {

	@DisplayName("입금이 성공한다")
	@Test
	void deposit() {
	    // given
		Account account = Account.create(10000L);

		// when
		account.deposit(10000L);

		// then
		assertThat(account.getMoney()).isEqualTo(20000L);
	}
	@DisplayName("출금이 성공한다.")
	@Test
	void withdraw() {
		// given
		Account account = Account.create(10000L);

		// when
		account.withdraw(10000L);

		// then
		assertThat(account.getMoney()).isZero();
	}

	@DisplayName("일일 충전 한도를 초과하면 false를 반환한다.")
	@Test
	void chargeLimitExceeded() {
		// given
		Account account = Account.create(10000L);
		long exceedAmount = CHARGE_LIMIT + 1;

		// when
		boolean result = account.isChargeWithinDailyLimit(exceedAmount);

		// then
		assertThat(result).isFalse();
		assertThat(account.getChargeLimit()).isEqualTo(CHARGE_LIMIT); // 차감되지 않음
	}

	@DisplayName("일일 충전 한도 내에서 충전하면 true를 반환하고 한도가 차감된다.")
	@Test
	void chargeWithinLimit() {
		// given
		Account account = Account.create(10000L);
		long chargeAmount = 5_000L;

		// when
		boolean result = account.isChargeWithinDailyLimit(chargeAmount);

		// then
		assertThat(result).isTrue();
		assertThat(account.getChargeLimit()).isEqualTo(CHARGE_LIMIT - chargeAmount);
	}

	@DisplayName("잔액이 부족하면 송금할 수 없다.")
	@Test
	void insufficientBalanceForSend() {
		// given
		Account account = Account.create(10000L);
		long sendAmount = 20_000L; // 잔액보다 큰 금액

		// when
		boolean result = account.isSend(sendAmount);

		// then
		assertThat(result).isFalse();
	}

	@DisplayName("잔액이 충분하면 송금할 수 있다.")
	@Test
	void sufficientBalanceForSend() {
		// given
		Account account = Account.create(10000L);
		long sendAmount = 5_000L;

		// when
		boolean result = account.isSend(sendAmount);

		// then
		assertThat(result).isTrue();
	}

}