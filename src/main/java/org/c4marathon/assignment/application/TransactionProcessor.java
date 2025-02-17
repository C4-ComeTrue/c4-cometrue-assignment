package org.c4marathon.assignment.application;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountRepository;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.domain.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionProcessor {
	private final AccountRepository accountRepository;
	private final UserRepository userRepository;
	private final WireTransferStrategyContext wireTransferStrategyContext;

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void updateBalance(String accountNumber, long money) {
		if (money > 0) {
			updateUser(accountNumber, money);
		}

		updateAccount(accountNumber, money);
	}

	public void wireTransfer(String senderAccountNumber, String receiverAccountNumber, long money) {
		Account senderAccount = accountRepository.findByAccountNumber(senderAccountNumber)
			.orElseThrow(() -> new RuntimeException("Account Not Found."));
		User sender = userRepository.findById(senderAccount.getUserId())
			.orElseThrow(() -> new RuntimeException("User Not Found."));

		WireTransferStrategy wireTransferStrategy = wireTransferStrategyContext.getWireTransferStrategy(
			sender.getSendingType());

		wireTransferStrategy.wireTransfer(senderAccountNumber, receiverAccountNumber, money);
	}

	private void updateUser(String accountNumber, long money) {
		Account account = accountRepository.findByAccountNumber(accountNumber)
			.orElseThrow(() -> new RuntimeException("Account not found"));

		int updatedRow = userRepository.charge(account.getUserId(), money);

		if (updatedRow == 0)
			throw new RuntimeException("Account not charged");
	}

	private void updateAccount(String accountNumber, long money) {
		int updatedRow = accountRepository.updateBalance(accountNumber, money);

		if (updatedRow == 0)
			throw new RuntimeException("Failed to update balance.");
	}
}
