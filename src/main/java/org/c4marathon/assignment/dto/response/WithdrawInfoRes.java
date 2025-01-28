package org.c4marathon.assignment.dto.response;

import java.time.Instant;

import org.c4marathon.assignment.entity.Account;

public record WithdrawInfoRes(
	Instant withdrawDate,
	Long mainAccountBalance,
	Long savingsAccountBalance
) {
	public WithdrawInfoRes(long mainAccountBalance, long savingsAccountBalance) {
		this(
			Instant.now(),
			mainAccountBalance,
			savingsAccountBalance
		);
	}
}
