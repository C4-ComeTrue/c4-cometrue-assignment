package org.c4marathon.assignment.event;

import org.c4marathon.assignment.dto.TransferTransactionEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class TransferTransactionEventPublisher {
	private final ApplicationEventPublisher applicationEventPublisher;

	public void publishTransferTransactionEvent(TransferTransactionEvent transferTransactionEvent) {
		applicationEventPublisher.publishEvent(transferTransactionEvent);
	}
}
