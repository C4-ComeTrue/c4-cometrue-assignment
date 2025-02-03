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
}
