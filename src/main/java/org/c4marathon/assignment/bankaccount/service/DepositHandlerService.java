package org.c4marathon.assignment.bankaccount.service;

import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.c4marathon.assignment.bankaccount.repository.SendRecordRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepositHandlerService {
	private final MainAccountRepository mainAccountRepository;
	private final SendRecordRepository sendRecordRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
	@Async("depositExecutor")
	public void doDeposit(long accountPk, long money, long recordPk) {
		throw new RuntimeException();
		// mainAccountRepository.deposit(accountPk, money);
		// sendRecordRepository.checkRecord(recordPk);
	}
}
