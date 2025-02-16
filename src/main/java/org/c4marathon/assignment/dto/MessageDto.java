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

	long account;

	long amount;

	@Builder
	public MessageDto(long transferTransactionId, long account, long amount) {
		this.transferTransactionId = transferTransactionId;
		this.account = account;
		this.amount = amount;
	}
}
