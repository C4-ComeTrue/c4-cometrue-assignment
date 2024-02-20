package org.c4marathon.assignment.domain.entity;

import static org.assertj.core.api.Assertions.*;

import org.c4marathon.assignment.common.utils.ChargeLimitUtils;
import org.junit.jupiter.api.Test;

class AccountTest {

	@Test
	void 충전에_성공한다() {
		// given
		var chargeAmount = 1000;
		var totalAmount = 500000L;
		var accumulatedChargeAmount = 100000;
		Member member = new Member("email", "password");
		Account account = new Account(1L, member, "name", "number", totalAmount, accumulatedChargeAmount, ChargeLimitUtils.BASIC_LIMIT);

		// when
		account.charge(chargeAmount);

		// then
		assertThat(account.getAmount()).isEqualTo(501000);
		assertThat(account.getAccumulatedChargeAmount()).isEqualTo(101000);
	}

	@Test
	void 인출에_성공한다() {
		// given
		var withDrawAmount = 10000;
		var totalAmount = 50000L;
		var accumulatedChargeAmount = 100000;
		Member member = new Member("email", "password");
		Account account = new Account(1L, member, "name", "number", totalAmount, accumulatedChargeAmount, ChargeLimitUtils.BASIC_LIMIT);

		// when
		account.withdraw(withDrawAmount);

		// then
		assertThat(account.getAmount()).isEqualTo(40000);
	}
}
