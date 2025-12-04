package org.c4marathon.assignment.service.transfer;

import java.time.LocalDateTime;

import org.c4marathon.assignment.common.event.TransferEvent;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.domain.entity.Account;
import org.c4marathon.assignment.domain.entity.TransferLog;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.TransferLogRepository;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 입금 로직
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DepositService {

	private final AccountRepository accountRepository;
	private final TransferLogRepository transferLogRepository;

	@Retryable(
		retryFor = TransientDataAccessException.class, // 복구가 가능한 일시적 예외의 경우에 Retry 설정
		maxAttempts = 5,
		backoff = @Backoff(delay = 2000),
		recover = "recoverMethod"
	)
	@Transactional(propagation = Propagation.REQUIRES_NEW) // Ordered.LOWEST_PRECEDENCE
	public void deposit(
		long sendAccountId, String transferAccountNumber, long transferAmount
	) {
		log.info("B 입금 트랜잭션 로직 수행, 스레드 : {} 현재 시각 : {}", Thread.currentThread().getId(), LocalDateTime.now());

		try {
			// 1. B 계좌의 유효성을 검사하고 DB 원자적 연산을 통해(get + set) 입금 로직을 처리한다.
			Account transferAccount = accountRepository.findByAccountNumber(transferAccountNumber)
				.orElseThrow(ErrorCode.INVALID_ACCOUNT::businessException);

			accountRepository.deposit(transferAccount.getId(), transferAmount);

			// 2. B 입금까지 정상적으로 끝났다면 이체 기록을 pending -> completed 상태로 변환한다.
			TransferLog transferLog = transferLogRepository.findBySendAccountIdAndReceiveAccountNumberAndAmount(
				sendAccountId, transferAccountNumber, transferAmount
			).orElseThrow(ErrorCode.INVALID_TRANSFER_LOG::businessException);

			transferLog.changeCompleted();
		} catch (Exception exception) {
			// 일시적 예외가 아닌 경우는 재시도 진행 X
			if (!(exception instanceof TransientDataAccessException)) {
				log.error("재시도가 불가능한 예외 발생", exception);
				plusMyAccount(sendAccountId, transferAmount);
				changeTransferLogStatusFailed(sendAccountId, transferAccountNumber, transferAmount);
				throw ErrorCode.FAILED_TO_TRANSFER.businessException();
			}

			// 일시적 예외라면 재시도 진행 이후 복구
			throw exception;
		}
	}

	@Recover
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void recoverMethod(TransientDataAccessException e, long sendAccountId, String transferAccountNumber, long transferAmount) {
		// 재시도 끝난 후에도 실패했을 때 해당 메서드에서 보상 트랜잭션 수행 & 송금 실패 예외 반환
		log.error("재시도 전체 실패 후 A 입금 보상 트랜잭션 수행", e);
		plusMyAccount(sendAccountId, transferAmount);
		changeTransferLogStatusFailed(sendAccountId, transferAccountNumber, transferAmount);
		throw ErrorCode.FAILED_TO_TRANSFER.businessException();
	}

	public void plusMyAccount(long accountId, long transferAmount) {
		accountRepository.deposit(accountId, transferAmount);
	}

	private void changeTransferLogStatusFailed(long sendAccountId, String receiveAccountNumber, long amount) {
		// 송금 내역의 상태를 실패로 변경
		TransferLog failedTransferLog = transferLogRepository.findBySendAccountIdAndReceiveAccountNumberAndAmount(
			sendAccountId, receiveAccountNumber, amount
		).orElseThrow(ErrorCode.INVALID_TRANSFER_LOG::businessException);

		failedTransferLog.changeFailed();
	}
}
