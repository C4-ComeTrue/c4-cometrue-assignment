package org.c4marathon.assignment.bankaccount.service;

public interface MainAccountService {
	int chargeMoney(long mainAccountPk, int money);

	void sendToSavingAccount(long mainAccountPk, long savingAccountPk, int money);
}
