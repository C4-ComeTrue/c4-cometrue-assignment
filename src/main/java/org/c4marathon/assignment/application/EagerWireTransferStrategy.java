package org.c4marathon.assignment.application;

import org.c4marathon.assignment.domain.Transaction;
import org.c4marathon.assignment.domain.TransactionRepository;
import org.c4marathon.assignment.domain.type.TransactionState;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EagerWireTransferStrategy implements WireTransferStrategy {
	private final TransactionCommonProcessor transactionCommonProcessor;
	private final TransactionRepository transactionRepository;

	/**
	 * 송금 시 금액을 바로 업데이트합니다. 송금 계좌는 money만큼 감소, 수금 계좌는 money만큼 증가합니다.
	 * 계좌 내역에는 FINISHED 상태로 바로 저장됩니다.
	 * @param senderAccountNumber
	 * @param receiverAccountNumber
	 * @param money
	 */
	@Override
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void wireTransfer(String sendingName, String senderAccountNumber, String receiverAccountNumber, long money) {
		if (senderAccountNumber.compareTo(receiverAccountNumber) < 0) {
			transactionCommonProcessor.updateBalance(senderAccountNumber, -money);
			transactionCommonProcessor.updateBalance(receiverAccountNumber, money);
		}
		else {
			transactionCommonProcessor.updateBalance(receiverAccountNumber, money);
			transactionCommonProcessor.updateBalance(senderAccountNumber, -money);
		}

		Transaction transaction = Transaction.builder()
			.senderAccountNumber(senderAccountNumber)
			.sendingName(sendingName)
			.receiverAccountNumber(receiverAccountNumber)
			.state(TransactionState.FINISHED)
			.balance(money)
			.build();

		transactionRepository.save(transaction);
	}
}
