package org.c4marathon.assignment.bankaccount.service;

import org.c4marathon.assignment.bankaccount.dto.response.MainAccountResponseDto;

public interface MainAccountService {
	int chargeMoney(long mainAccountPk, int money);

	void sendToSavingAccount(long mainAccountPk, long savingAccountPk, int money);

	MainAccountResponseDto getMainAccountInfo(long mainAccountPk);
}
