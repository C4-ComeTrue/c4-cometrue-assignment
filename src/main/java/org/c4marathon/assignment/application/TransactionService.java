package org.c4marathon.assignment.application;

import static org.c4marathon.assignment.global.CommonUtils.*;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountRepository;
import org.c4marathon.assignment.domain.dto.response.TransferResult;
import org.c4marathon.assignment.domain.dto.response.WithdrawResult;
import org.c4marathon.assignment.domain.type.AccountType;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {
	private static final long CEILING_POINT = 10_000L;

	private final TransactionProcessor transactionProcessor;
	private final AccountRepository accountRepository;

	// 단순 출금
	public WithdrawResult withdraw(String accountNumber, long money) {
		transactionProcessor.updateBalance(accountNumber, -money);

		return new WithdrawResult(money);
	}

	/**
	 * 송금 메서드. 송금 유효 판단 후 계좌에 송금 금액만큼 충전. 마지막으로 송금.
	 * @param senderAccountNumber
	 * @param receiverAccountNumber
	 * @param money
	 * @return
	 */
	public TransferResult wireTransfer(String senderAccountNumber, String receiverAccountNumber, long money) {
		validateSender(senderAccountNumber, receiverAccountNumber);
		long diff = computeBalanceDiff(senderAccountNumber, money);
		if (diff < 0) {
			transactionProcessor.updateBalance(senderAccountNumber, getCeil(-diff, CEILING_POINT));
		}
		transactionProcessor.wireTransfer(senderAccountNumber, receiverAccountNumber, money);

		return new TransferResult(senderAccountNumber, receiverAccountNumber, money);
	}

	public TransferResult receive(long transactionId) {
		return transactionProcessor.receive(transactionId);
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
