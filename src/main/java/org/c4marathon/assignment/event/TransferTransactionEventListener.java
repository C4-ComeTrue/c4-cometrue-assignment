package org.c4marathon.assignment.event;

import static org.c4marathon.assignment.config.AsyncConfig.*;

import org.c4marathon.assignment.dto.MessageDto;
import org.c4marathon.assignment.dto.ImmediateTransferTransactionEvent;
import org.c4marathon.assignment.dto.TransferTransactionEvent;
import org.c4marathon.assignment.entity.TransferTransaction;
import org.c4marathon.assignment.service.MessageService;
import org.c4marathon.assignment.service.TransactionService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferTransactionEventListener {
	private final TransactionService transactionService;
	private final MessageService messageService;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void handleBeforeCommitTransferTransactionEvent(TransferTransactionEvent transferTransactionEvent) {
		TransferTransaction transferTransaction = transactionService.saveTransferTransaction(
			TransferTransaction.builder()
				.sender(transferTransactionEvent.getUserName())
				.senderMainAccount(transferTransactionEvent.getSenderMainAccount())
				.receiverMainAccount(transferTransactionEvent.getReceiverMainAccount())
				.amount(transferTransactionEvent.getAmount())
				.type(transferTransactionEvent.getType())
				.receiverId(transferTransactionEvent.getReceiverId())
				.build());

		transferTransactionEvent.updateTransferTransactionId(transferTransaction.getId());
	}

	@Async(ASYNC_LISTENER_TASK_EXECUTOR_NAME)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleAfterCommitTransferTransactionEvent(ImmediateTransferTransactionEvent immediateTransferEvent) {
		log.debug("{} handle transfer transaction event after commit", Thread.currentThread().getName());

		messageService.sendTransaction(MessageDto.builder()
			.transferTransactionId(immediateTransferEvent.getTransferTransactionId())
			.senderMainAccount(immediateTransferEvent.getSenderMainAccount())
			.receiverMainAccount(immediateTransferEvent.getReceiverMainAccount())
			.amount(immediateTransferEvent.getAmount())
			.build());
	}
}
