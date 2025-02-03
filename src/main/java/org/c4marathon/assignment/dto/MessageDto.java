package org.c4marathon.assignment.dto;

import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageDto {
	@Positive(message = "송금 트랜잭션 번호는 양수가 되어야 합니다.")
	long transferTransactionId;
	@Positive(message = "이체 계좌는 양수가 되어야 합니다.")
	long senderMainAccount;
	@Positive(message = "이체 받는 계좌는 양수가 되어야 합니다.")
	long receiverMainAccount;
	@Positive(message = "이체 금액은 양수가 되어야 합니다.")
	long amount;

	@Builder
	public MessageDto(long transferTransactionId, long senderMainAccount, long receiverMainAccount, long amount) {
		this.transferTransactionId = transferTransactionId;
		this.senderMainAccount = senderMainAccount;
		this.receiverMainAccount = receiverMainAccount;
		this.amount = amount;
	}
}
