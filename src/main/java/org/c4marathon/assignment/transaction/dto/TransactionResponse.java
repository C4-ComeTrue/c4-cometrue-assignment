package org.c4marathon.assignment.transaction.dto;

import java.time.LocalDateTime;

import org.c4marathon.assignment.transaction.domain.Transaction;
import org.c4marathon.assignment.transaction.domain.TransactionStatus;
import org.c4marathon.assignment.transaction.domain.TransactionType;

import lombok.Getter;

@Getter
public record TransactionResponse(
	Long transactionId,
	String senderAccountNumber,
	String receiverAccountNumber,
	long amount,
	TransactionType type,
	TransactionStatus status,
	LocalDateTime sendTime,
	LocalDateTime receiverTime
) {
	public static TransactionResponse from(Transaction transaction) {
		return new TransactionResponse(
			transaction.getId(),
			transaction.getSenderAccountNumber(),
			transaction.getReceiverAccountNumber(),
			transaction.getAmount(),
			transaction.getType(),
			transaction.getStatus(),
			transaction.getSendTime(),
			transaction.getReceiverTime()
		);
	}

}
