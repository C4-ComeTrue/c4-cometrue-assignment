package org.c4marathon.assignment.application;

import static org.c4marathon.assignment.global.CommonUtils.*;

import java.time.Instant;
import java.util.List;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountRepository;
import org.c4marathon.assignment.domain.Transaction;
import org.c4marathon.assignment.domain.TransactionRepository;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.domain.UserRepository;
import org.c4marathon.assignment.domain.dto.TransactionInfo;
import org.c4marathon.assignment.domain.dto.response.TransferResult;
import org.c4marathon.assignment.domain.dto.response.WithdrawResult;
import org.c4marathon.assignment.domain.type.AccountType;
import org.c4marathon.assignment.domain.type.TransactionState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {
	private static final long CEILING_POINT = 10_000L;

	private final AccountRepository accountRepository;
	private final UserRepository userRepository;
	private final TransactionRepository transactionRepository;
	private final WireTransferStrategyContext wireTransferStrategyContext;
	private final TransactionCommonProcessor transactionCommonProcessor;

	// 단순 출금
	public WithdrawResult withdraw(String accountNumber, long money) {
		transactionCommonProcessor.updateBalance(accountNumber, -money);

		return new WithdrawResult(money);
	}

	/**
	 * 송금 메서드. 송금 유효 판단 후 계좌에 송금 금액만큼 충전. 마지막으로 송금.
	 * @param senderAccountNumber
	 * @param receiverAccountNumber
	 * @param money
	 * @return
	 */
	@Transactional
	public TransferResult wireTransfer(String sendingName, String senderAccountNumber, String receiverAccountNumber, long money) {
		validateSender(senderAccountNumber, receiverAccountNumber);
		long diff = computeBalanceDiff(senderAccountNumber, money);
		if (diff < 0) {
			transactionCommonProcessor.updateBalance(senderAccountNumber, getCeil(-diff, CEILING_POINT));
		}
		wireTransferByStrategy(sendingName, senderAccountNumber, receiverAccountNumber, money);

		return new TransferResult(senderAccountNumber, receiverAccountNumber, money);
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

	public List<TransactionInfo> findAllAutoCancelInfo(Long id, Instant end, TransactionState state, int limit) {
		if (id == null) {
			return transactionRepository.findAllAutoCancelInfoWithXLockBy(end, state.name(), limit);
		}

		return transactionRepository.findAllAutoCancelInfoWithXLockBy(id, end, state.name(), limit);
	}

	private void wireTransferByStrategy(String sendingName, String senderAccountNumber, String receiverAccountNumber, long money) {
		Account senderAccount = accountRepository.findByAccountNumber(senderAccountNumber)
			.orElseThrow(() -> new RuntimeException("Account Not Found."));
		User sender = userRepository.findById(senderAccount.getUserId())
			.orElseThrow(() -> new RuntimeException("User Not Found."));

		WireTransferStrategy wireTransferStrategy = wireTransferStrategyContext.getWireTransferStrategy(
			sender.getSendingType());

		sendingName = (sendingName == null) ? sender.getName() : sendingName;
		wireTransferStrategy.wireTransfer(sendingName, senderAccountNumber, receiverAccountNumber, money);
	}

	/**
	 * @param senderAccountNumber
	 * @param receiverAccountNumber
	 *
	 * 적금 계좌는 송신 계좌가 메인 계좌여야 하기 때문에 그 조건을 판단하는 메서드.
	 */
	private void validateSender(String senderAccountNumber, String receiverAccountNumber) {
		Account receiver = accountRepository.findByAccountNumber(receiverAccountNumber)
			.orElseThrow(() -> new RuntimeException("Account not found."));

		if (receiver.getAccountType() != AccountType.INSTALLATION)
			return;

		Account mainAccount = accountRepository.findMainAccount(receiver.getUserId())
			.orElseThrow(() -> new RuntimeException("Account not found."));

		if (!mainAccount.getAccountNumber().equals(senderAccountNumber))
			throw new RuntimeException("적금 계좌는 본인의 메인 계좌에서만 거래 가능합니다.");
	}

	/**
	 * @param senderAccountNumber
	 * @param money
	 * @return
	 *
	 * 송금 금액과 계좌 내 잔액이 얼마나 차이나는지 계산하는 메서드
	 */
	private long computeBalanceDiff(String senderAccountNumber, long money) {
		Account account = accountRepository.findByAccountNumber(senderAccountNumber)
			.orElseThrow(() -> new RuntimeException("Account not found."));

		return account.getBalance() - money;
	}
}
