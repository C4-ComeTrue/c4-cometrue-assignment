package org.c4marathon.assignment.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageDto {
	long transferTransactionId;

	long senderMainAccount;

	long receiverMainAccount;
	long amount;

	@Builder
	public MessageDto(long transferTransactionId, long senderMainAccount, long receiverMainAccount, long amount) {
		this.transferTransactionId = transferTransactionId;
		this.senderMainAccount = senderMainAccount;
		this.receiverMainAccount = receiverMainAccount;
		this.amount = amount;
	}
}
