package org.c4marathon.assignment.dto;

import org.c4marathon.assignment.entity.TransactionType;

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

	TransactionType type;

	@Builder
	public MessageDto(long transferTransactionId, long account, long amount, TransactionType type) {
		this.transferTransactionId = transferTransactionId;
		this.account = account;
		this.amount = amount;
		this.type = type;
	}
}
