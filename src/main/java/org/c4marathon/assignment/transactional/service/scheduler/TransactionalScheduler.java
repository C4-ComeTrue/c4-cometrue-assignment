package org.c4marathon.assignment.transactional.service.scheduler;

import org.c4marathon.assignment.transactional.domain.repository.TransactionalRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionalScheduler {
	private final TransactionalRepository transactionalRepository;

	@Scheduled(fixedRate = 10000)
	public void cancelExpiredTransactions() {
	}
}
