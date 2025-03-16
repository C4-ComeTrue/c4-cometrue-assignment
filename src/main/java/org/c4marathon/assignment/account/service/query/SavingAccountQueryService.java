package org.c4marathon.assignment.account.service.query;

import static org.c4marathon.assignment.account.domain.SavingProductType.*;

import java.util.List;

import org.c4marathon.assignment.account.domain.SavingAccount;
import org.c4marathon.assignment.account.domain.repository.SavingAccountRepository;
import org.c4marathon.assignment.account.exception.NotFoundAccountException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavingAccountQueryService {
	private final SavingAccountRepository savingAccountRepository;


	public SavingAccount findSavingAccountWithLock(String savingAccountNumber) {
		return savingAccountRepository.findBySavingAccountNumberWithLock(savingAccountNumber)
			.orElseThrow(NotFoundAccountException::new);
	}

	public SavingAccount findFreeSavingAccountWithLock(String freeSavingAccountNumber) {
		return savingAccountRepository.findFreeSavingAccountWithLock(freeSavingAccountNumber, FREE)
			.orElseThrow(NotFoundAccountException::new);
	}

	public List<SavingAccount> findSavingAccountByFixedWithLastId(Long lastId, int size) {
		if (lastId == null) {
			return savingAccountRepository.findSavingAccountByFixed(FIXED, size);
		} else {
			return savingAccountRepository.findSavingAccountByFixedWithLastId(FIXED, lastId, size);
		}
	}

	public List<SavingAccount> findAllSavingAccountByLastId(Long lastId, int size) {
		if (lastId == null) {
			return savingAccountRepository.findAllSavingAccount(size);
		} else {
			return savingAccountRepository.findAllSavingAccountByLastId(lastId, size);
		}
	}
}
