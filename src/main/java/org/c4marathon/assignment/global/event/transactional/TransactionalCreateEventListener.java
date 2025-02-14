package org.c4marathon.assignment.global.event.transactional;

import org.c4marathon.assignment.transactional.service.TransactionalService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionalCreateEventListener {

	private final TransactionalService transactionalService;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void handleTransactionalCreate(TransactionalCreateEvent event) {
		transactionalService.createTransactional(event);
	}
}
