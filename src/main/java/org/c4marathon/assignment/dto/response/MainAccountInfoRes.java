package org.c4marathon.assignment.dto.response;

import java.time.Instant;

import org.c4marathon.assignment.entity.Account;

public record MainAccountInfoRes(
	Instant depositDate,
	Long balance
) {
	public MainAccountInfoRes(Account account) {
		this(
			account.getUpdatedDate(),
			account.getBalance()
		);
	}
}
