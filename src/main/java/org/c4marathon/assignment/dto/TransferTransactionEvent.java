package org.c4marathon.assignment.dto;

import org.c4marathon.assignment.entity.TransactionType;
import org.c4marathon.assignment.entity.TransferTransaction;

import lombok.Getter;

@Getter
public abstract class TransferTransactionEvent {
	private static final int INIT_TRANSFER_TRANSACTION_ID = -1;
	protected String userName;
	protected long transferTransactionId;
	protected long senderMainAccount;
	protected long receiverMainAccount;
	protected long amount;
	protected Long receiverId;
	protected TransactionType type;

	protected TransferTransactionEvent(String userName, long senderMainAccount, long receiverMainAccount,
		long amount, Long receiverId, TransactionType type) {
		this.userName = userName;
		this.transferTransactionId = INIT_TRANSFER_TRANSACTION_ID;
		this.senderMainAccount = senderMainAccount;
		this.receiverMainAccount = receiverMainAccount;
		this.amount = amount;
		this.receiverId = receiverId;
		this.type = type;
	}

	public void updateTransferTransactionId(long transferTransactionId) {
		this.transferTransactionId = transferTransactionId;
	}
}
