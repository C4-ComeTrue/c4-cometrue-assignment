package org.c4marathon.assignment.account.service.scheduler;

import org.c4marathon.assignment.account.service.SavingAccountService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SavingAccountScheduler {
	private final SavingAccountService savingAccountService;

	@Scheduled(cron = "0 0 8 * * ?")
	public void depositFixedSavingAccount() {
		savingAccountService.depositFixedSavingAccount();
	}
}
