package org.c4marathon.assignment.bankaccount.service;

import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepositHandlerService {
	private final MainAccountRepository mainAccountRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Async("depositExecutor")
	public void doDeposit(long accountPk, long money) {
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			System.out.println("error");
		}
		mainAccountRepository.deposit(accountPk, money);

	}
}
