package org.c4marathon.assignment.domain.entity;

import static org.assertj.core.api.Assertions.*;

import java.math.BigInteger;

import org.c4marathon.assignment.domain.ChargeLimit;
import org.junit.jupiter.api.Test;

class AccountTest {

	@Test
	void 충전_테스트() {
		// given
		var chargeAmount = 1000;
		var totalAmount = BigInteger.valueOf(500000);
		var accumulatedChargeAmount = 100000;
		Member member = new Member("email", "password");
		Account account = new Account(1L, member, "name", "number", totalAmount, accumulatedChargeAmount, ChargeLimit.DAY_BASIC_LIMIT);

		// when
		account.charge(chargeAmount);

		// then
		assertThat(account.getAmount()).isEqualTo(501000);
		assertThat(account.getAccumulatedChargeAmount()).isEqualTo(101000);
	}
}
