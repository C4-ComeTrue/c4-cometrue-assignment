package org.c4marathon.assignment.account.service;

import static org.c4marathon.assignment.account.domain.SavingProductType.*;

import java.util.List;

import org.c4marathon.assignment.account.domain.SavingAccount;
import org.c4marathon.assignment.account.domain.repository.SavingAccountRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavingAccountQueryService {
	private final SavingAccountRepository savingAccountRepository;

	public List<SavingAccount> findSavingAccountByFixedWithLastId(Long lastId, int size) {
		if (lastId == null) {
			return savingAccountRepository.findSavingAccountByFixed(FIXED, size);
		} else {
			return savingAccountRepository.findSavingAccountByFixedWithLastId(FIXED, lastId, size);
		}
	}

}
