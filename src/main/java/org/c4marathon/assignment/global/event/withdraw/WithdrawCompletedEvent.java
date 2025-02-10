package org.c4marathon.assignment.global.event.withdraw;

public record WithdrawCompletedEvent(
	String transactionId,
	Long senderAccountId,
	Long receiverAccountId,
	long money
) {

}
