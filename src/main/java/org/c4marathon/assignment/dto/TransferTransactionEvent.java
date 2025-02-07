package org.c4marathon.assignment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TransferTransactionEvent {
	private static final int INIT_TRANSFER_TRANSACTION_ID = -1;
	String userName;
	long transferTransactionId;
	long senderMainAccount;
	long receiverMainAccount;
	long amount;

	@Builder
	public TransferTransactionEvent(String userName, long senderMainAccount, long receiverMainAccount, long amount) {
		this.userName = userName;
		this.transferTransactionId = INIT_TRANSFER_TRANSACTION_ID;
		this.senderMainAccount = senderMainAccount;
		this.receiverMainAccount = receiverMainAccount;
		this.amount = amount;
	}

	public void updateTransferTransactionId(long transferTransactionId) {
		this.transferTransactionId = transferTransactionId;
	}
}
