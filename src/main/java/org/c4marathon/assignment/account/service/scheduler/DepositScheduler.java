package org.c4marathon.assignment.account.service.scheduler;

import static org.c4marathon.assignment.transaction.domain.TransactionStatus.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.c4marathon.assignment.account.service.DepositService;
import org.c4marathon.assignment.global.core.MiniPayThreadPoolExecutor;
import org.c4marathon.assignment.transaction.domain.Transaction;
import org.c4marathon.assignment.transaction.service.TransactionQueryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositScheduler {
	private final DepositService depositService;

	@Scheduled(fixedRate = 10000)
	public void deposits() {
		depositService.successDeposit();
	}

	@Scheduled(fixedRate = 12000)
	public void retryDeposit() {
		depositService.retryDeposit();
	}
}
