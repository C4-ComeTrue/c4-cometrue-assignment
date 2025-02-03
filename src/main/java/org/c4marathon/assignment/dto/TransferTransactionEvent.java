package org.c4marathon.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TransferTransactionEvent {
	private static final int INIT_TRANSFER_TRANSACTION_ID = -1;
	@Size(max = 100)
	@NotBlank
	String userName;

	long transferTransactionId;

	@Positive(message = "이체 계좌는 양수가 되어야 합니다.")
	long senderMainAccount;

	@Positive(message = "이체 받는 계좌는 양수가 되어야 합니다.")
	long receiverMainAccount;

	@Positive(message = "이체 금액은 양수가 되어야 합니다.")
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
