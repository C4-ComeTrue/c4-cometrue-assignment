package org.c4marathon.assignment.bankaccount.dto.response;

import org.c4marathon.assignment.bankaccount.entity.MainAccount;

public record MainAccountResponseDto(
	long accountPk,
	long chargeLimit,
	long spareMoney,
	long money
) {
	public MainAccountResponseDto(MainAccount mainAccount) {
		this(mainAccount.getAccountPk(), mainAccount.getChargeLimit(), mainAccount.getSpareMoney(),
			mainAccount.getMoney());
	}
}
