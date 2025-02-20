package org.c4marathon.assignment.application;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EagerWireTransferStrategy implements WireTransferStrategy {
	private final TransactionCommonProcessor transactionCommonProcessor;

	@Override
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void wireTransfer(String senderAccountNumber, String receiverAccountNumber, long money) {
		if (senderAccountNumber.compareTo(receiverAccountNumber) < 0) {
			transactionCommonProcessor.updateBalance(senderAccountNumber, -money);
			transactionCommonProcessor.updateBalance(receiverAccountNumber, money);
		}
		else {
			transactionCommonProcessor.updateBalance(receiverAccountNumber, money);
			transactionCommonProcessor.updateBalance(senderAccountNumber, -money);
		}
	}
}
