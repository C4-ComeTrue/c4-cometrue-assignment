package org.c4marathon.assignment.application;

import java.time.LocalDateTime;
import java.util.List;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountRepository;
import org.c4marathon.assignment.domain.Transaction;
import org.c4marathon.assignment.domain.TransactionRepository;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.domain.UserRepository;
import org.c4marathon.assignment.domain.dto.TransactionInfo;
import org.c4marathon.assignment.domain.dto.response.TransferResult;
import org.c4marathon.assignment.domain.type.TransactionState;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionProcessor {
	private final AccountRepository accountRepository;
	private final UserRepository userRepository;
	private final TransactionRepository transactionRepository;
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

	@Transactional
	public TransferResult receive(long transactionId) {
		Transaction transaction = transactionRepository.findById(transactionId)
			.orElseThrow(() -> new RuntimeException("Transaction Not Found."));
		updateState(transactionId, TransactionState.PENDING, TransactionState.FINISHED);
		transactionCommonProcessor.updateBalance(transaction.getReceiverAccountNumber(), transaction.getBalance());

		return new TransferResult(transaction.getSenderAccountNumber(),
			transaction.getReceiverAccountNumber(),
			transaction.getBalance());
	}

	@Transactional
	public void cancel(long transactionId) {
		Transaction transaction = transactionRepository.findById(transactionId)
			.orElseThrow(() -> new RuntimeException("Transaction Not Found."));
		updateState(transactionId, TransactionState.PENDING, TransactionState.CANCELLED);
		transactionCommonProcessor.updateAccount(transaction.getSenderAccountNumber(), transaction.getBalance());
	}

	public void updateState(long transactionId, TransactionState preState, TransactionState updateState) {
		int updatedRow = transactionRepository.updateState(transactionId, preState, updateState);

		if (updatedRow == 0)
			throw new RuntimeException("상태를 업데이트 하지 못했습니다.");
	}

	public void updateState(List<Long> transactionIds, TransactionState preState, TransactionState updateState) {
		transactionRepository.updateState(transactionIds, preState, updateState);
	}

	public List<TransactionInfo> findAllAutoCancelInfo(Long id, LocalDateTime end, TransactionState state, int limit) {
		if (id == null) {
			return transactionRepository.findAllAutoCancelInfoWithXLockBy(end, state.name(), limit);
		}

		return transactionRepository.findAllAutoCancelInfoWithXLockBy(id, end, state.name(), limit);
	}
}
