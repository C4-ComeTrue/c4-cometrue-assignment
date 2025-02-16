package org.c4marathon.assignment.dto;

import org.c4marathon.assignment.entity.TransactionType;

import lombok.Builder;

public class DelayedTransferTransactionEvent extends TransferTransactionEvent {
	@Builder
	public DelayedTransferTransactionEvent(String userName, long senderMainAccount, long receiverMainAccount,
		Long receiverId, long amount) {
		super(userName, senderMainAccount, receiverMainAccount, amount, receiverId, TransactionType.PENDING);
	}
}
