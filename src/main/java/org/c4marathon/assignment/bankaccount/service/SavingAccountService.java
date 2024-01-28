package org.c4marathon.assignment.bankaccount.service;

import java.util.List;

import org.c4marathon.assignment.bankaccount.dto.response.SavingAccountResponseDto;

public interface SavingAccountService {
	void create(long memberPk, String productName);

	List<SavingAccountResponseDto> getSavingAccountInfo(long memberPk);
}
