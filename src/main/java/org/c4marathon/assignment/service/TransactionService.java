package org.c4marathon.assignment.service;

import static org.c4marathon.assignment.config.AsyncConfig.*;

import java.util.List;

import org.c4marathon.assignment.dto.MessageDto;
import org.c4marathon.assignment.entity.TransactionStatus;
import org.c4marathon.assignment.entity.TransferTransaction;
import org.c4marathon.assignment.repository.TransferTransactionRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
	private final TransferTransactionRepository transferTransactionRepository;
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

		List<TransferTransaction> transferTransactions = transferTransactionRepository.getTransferTransactionsByStatus(
			TransactionStatus.PENDING);

		if (transferTransactions.isEmpty()) {
			return;
		}

		transferTransactions.forEach((transferTransaction -> {
			messageService.sendTransaction(MessageDto.builder()
				.transferTransactionId(transferTransaction.getId())
				.senderMainAccount(transferTransaction.getSenderMainAccount())
				.receiverMainAccount(transferTransaction.getReceiverMainAccount())
				.amount(transferTransaction.getAmount())
				.build());
		}));
	}
}
