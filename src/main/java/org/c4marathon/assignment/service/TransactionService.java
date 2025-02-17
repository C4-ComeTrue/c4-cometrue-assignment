package org.c4marathon.assignment.service;

import static org.c4marathon.assignment.config.AsyncConfig.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.c4marathon.assignment.dto.MessageDto;
import org.c4marathon.assignment.dto.request.TransferAcceptReq;
import org.c4marathon.assignment.dto.response.TransferRes;
import org.c4marathon.assignment.entity.TransactionStatus;
import org.c4marathon.assignment.entity.TransactionType;
import org.c4marathon.assignment.entity.TransferTransaction;
import org.c4marathon.assignment.exception.CustomException;
import org.c4marathon.assignment.exception.ErrorCode;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.TransferTransactionRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
	private final TransferTransactionRepository transferTransactionRepository;
	private final AccountRepository accountRepository;
	private final MessageService messageService;

	public TransferTransaction saveTransferTransaction(TransferTransaction transferTransaction) {
		return transferTransactionRepository.save(transferTransaction);
	}

	/**
	 * 비정상 송금 내역 처리 스케줄러
	 * 1. 정상 처리가 안된 송금 내역 조회
	 * 2. 1에서 찾은 송금 내역을 바탕으로 메세지 큐에 이벤트 발행
	 */
	@Async(ASYNC_SCHEDULER_TASK_EXECUTOR_NAME)
	@Scheduled(cron = "0 * * * * *")
	public void findAndPostMessage() {
		log.debug("{} post message start", Thread.currentThread().getName());

		List<TransferTransaction> transferTransactions = transferTransactionRepository.getTransferTransactionsByStatusAndType(
			TransactionStatus.PENDING, TransactionType.IMMEDIATE);

		if (transferTransactions.isEmpty()) {
			return;
		}

		transferTransactions.forEach((transferTransaction -> {
			messageService.sendTransaction(MessageDto.builder()
				.transferTransactionId(transferTransaction.getId())
				.account(transferTransaction.getReceiverMainAccount())
				.amount(transferTransaction.getAmount())
				.type(transferTransaction.getType())
				.build());
		}));
	}

	/**
	 * 24시간 남은 송금 알림 스케줄러
	 * 1. 24시간 남은 송금 내역 조회
	 * 2. 알림 발송
	 */
	@Async(ASYNC_SCHEDULER_TASK_EXECUTOR_NAME)
	@Scheduled(cron = "0 * * * * *")
	public void ReminderForPendingTransactions() {
		Instant targetTime = Instant.now().minus(48, ChronoUnit.HOURS);
		List<TransferTransaction> transferTransactions = transferTransactionRepository.findPendingTransactions(
			TransactionStatus.PENDING, TransactionType.PENDING, targetTime);

		if (transferTransactions.isEmpty()) {
			return;
		}

		transferTransactions.forEach((transferTransaction -> {
			log.debug("{} 알림 발송", Thread.currentThread().getName());
		}));
	}

	/**
	 * 72시간 지난 송금 취소 스케줄러
	 * 1. 72시간 지난 송금 송금 내역 조회
	 * 2. 송금 취소
	 */
	@Async(ASYNC_SCHEDULER_TASK_EXECUTOR_NAME)
	@Scheduled(cron = "0 * * * * *")
	public void cancelTransfer() {
		Instant targetTime = Instant.now().minus(72, ChronoUnit.HOURS);
		List<TransferTransaction> transferTransactions = transferTransactionRepository.findExpiredTransferTransactions(
			TransactionStatus.PENDING, TransactionType.PENDING, targetTime);

		if (transferTransactions.isEmpty()) {
			return;
		}

		transferTransactions.forEach((transferTransaction -> {
			messageService.sendTransaction(MessageDto.builder()
				.transferTransactionId(transferTransaction.getId())
				.account(transferTransaction.getSenderMainAccount())
				.amount(transferTransaction.getAmount())
				.type(transferTransaction.getType())
				.build());
		}));
	}

	@Transactional
	public void acceptTransfer(Long transferTransactionId, TransferAcceptReq transferAcceptReq) {
		TransferTransaction transferTransaction = transferTransactionRepository.findByIdAndStatus(transferTransactionId,
			TransactionStatus.PENDING).orElseThrow(() -> new CustomException(ErrorCode.TRANSFER_TRANSACTION_NOT_FOUND));

		if (transferTransaction.getReceiverId() != transferAcceptReq.requester()) {
			throw new CustomException(ErrorCode.INVALID_TRANSFER_REQUEST);
		}

		updateTransferTransactionStatus(transferTransactionId, TransactionStatus.SUCCESS);

		updateBalance(transferTransaction.getReceiverMainAccount(), transferTransaction.getAmount());
	}

	private void updateTransferTransactionStatus(Long transferTransactionId, TransactionStatus status) {
		int transferTransactionResult = transferTransactionRepository.updateStatus(transferTransactionId,
			TransactionStatus.PENDING, status);

		if (transferTransactionResult == 0) {
			throw new CustomException(ErrorCode.TRANSFER_TRANSACTION_NOT_FOUND);
		}
	}

	private void updateBalance(Long accountId, Long amount) {
		int accountResult = accountRepository.updateBalance(accountId, amount);

		if (accountResult == 0) {
			throw new CustomException(ErrorCode.ACCOUNT_NOT_FOUND);
		}
	}
}
