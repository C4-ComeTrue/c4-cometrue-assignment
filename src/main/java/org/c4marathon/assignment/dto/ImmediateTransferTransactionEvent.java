package org.c4marathon.assignment.dto;

import org.c4marathon.assignment.entity.TransactionType;

import lombok.Builder;

public class ImmediateTransferTransactionEvent extends TransferTransactionEvent {
	@Builder
	public ImmediateTransferTransactionEvent(String userName, long senderMainAccount, long receiverMainAccount,
		Long receiverId, long amount) {
		super(userName, senderMainAccount, receiverMainAccount, amount, receiverId, TransactionType.IMMEDIATE);
	}
}
