package org.c4marathon.assignment.application;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountRepository;
import org.c4marathon.assignment.domain.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EagerWireTransferStrategy implements WireTransferStrategy {
	private final AccountRepository accountRepository;
	private final UserRepository userRepository;

	@Override
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void wireTransfer(String senderAccountNumber, String receiverAccountNumber, long balance) {
		if (senderAccountNumber.compareTo(receiverAccountNumber) < 0) {
			updateBalance(senderAccountNumber, -balance);
			updateBalance(receiverAccountNumber, balance);
		}
		else {
			updateBalance(receiverAccountNumber, balance);
			updateBalance(senderAccountNumber, -balance);
		}
	}

	private void updateBalance(String accountNumber, long balance) {
		if (balance > 0) {
			updateUser(accountNumber, balance);
		}

		updateAccount(accountNumber, balance);
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
