package org.c4marathon.assignment.application;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountRepository;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.domain.UserRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionProcessor {
	private final AccountRepository accountRepository;
	private final UserRepository userRepository;
	private final WireTransferStrategyContext wireTransferStrategyContext;
	private final TransactionCommonProcessor transactionCommonProcessor;

	public void wireTransfer(String senderAccountNumber, String receiverAccountNumber, long money) {
		Account senderAccount = accountRepository.findByAccountNumber(senderAccountNumber)
			.orElseThrow(() -> new RuntimeException("Account Not Found."));
		User sender = userRepository.findById(senderAccount.getUserId())
			.orElseThrow(() -> new RuntimeException("User Not Found."));

		WireTransferStrategy wireTransferStrategy = wireTransferStrategyContext.getWireTransferStrategy(
			sender.getSendingType());

		wireTransferStrategy.wireTransfer(senderAccountNumber, receiverAccountNumber, money);
	}

	public void updateBalance(String accountNumber, long money) {
		transactionCommonProcessor.updateBalance(accountNumber, money);
	}
}
