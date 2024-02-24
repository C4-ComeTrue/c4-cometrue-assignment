package org.c4marathon.assignment.bankaccount.dto.response;

import org.c4marathon.assignment.bankaccount.entity.SavingAccount;

public record SavingAccountResponseDto(
	long accountPk,
	long savingMoney,
	long rate,
	String productName
) {
	public SavingAccountResponseDto(SavingAccount savingAccount) {
		this(savingAccount.getAccountPk(), savingAccount.getSavingMoney(), savingAccount.getRate(),
			savingAccount.getProductName());
	}
}
