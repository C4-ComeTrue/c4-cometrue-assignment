package org.c4marathon.assignment.application;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountRepository;
import org.c4marathon.assignment.domain.TransactionLog;
import org.c4marathon.assignment.domain.TransactionLogRepository;
import org.c4marathon.assignment.domain.TransactionRepository;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.domain.UserRepository;
import org.c4marathon.assignment.domain.dto.TransactionInfo;
import org.c4marathon.assignment.domain.type.TransactionState;
import org.c4marathon.assignment.global.EntityConverter;
import org.c4marathon.assignment.global.QueryTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionLogScheduledService {
	private static final int BATCH_SIZE = 1000;

	private final TransactionLogRepository transactionLogRepository;
	private final TransactionRepository transactionRepository;
	private final MongoTemplate mongoTemplate;
	private final AccountRepository accountRepository;
	private final UserRepository userRepository;

	@Scheduled(cron = "${transaction.scheduled.interval.log-saving}")
	public void save() {
		Instant now = Instant.now();
		TransactionLog firstCursor = transactionLogRepository.findFirstBy()
			.orElseGet(() -> TransactionLog.builder().transactionId(0L).sendTime(Instant.EPOCH).build());

		QueryTemplate.<TransactionInfo>selectAndExecuteWithCursor(
			transactionInfo -> transactionRepository.findAllInfoBy(transactionInfo == null ? firstCursor.getTransactionId() : transactionInfo.getId(),
				transactionInfo == null ? firstCursor.getSendTime() : transactionInfo.getDeadline(),
				now,
				TransactionState.FINISHED.name(), BATCH_SIZE),
			transactionInfos -> {
				List<String> senderAccountNumbers = transactionInfos.stream().map(TransactionInfo::getSenderAccountNumber).toList();
				List<String> receiverAccountNumbers = transactionInfos.stream().map(TransactionInfo::getReceiverAccountNumber).toList();

				List<TransactionLog> transactionLogs = getTransactionLogs(transactionInfos,
					senderAccountNumbers, receiverAccountNumbers);

				mongoTemplate.insert(transactionLogs, TransactionLog.class);
			},
			BATCH_SIZE
		);
	}

	private List<TransactionLog> getTransactionLogs(List<TransactionInfo> transactionInfos,
		List<String> senderAccountNumbers, List<String> receiverAccountNumbers) {
		List<String> allAccountNumbers = ListUtils.union(senderAccountNumbers, receiverAccountNumbers);

		Map<String, Account> accountMap = accountRepository.findAllByAccountNumber(allAccountNumbers)
			.stream()
			.collect(Collectors.toMap(Account::getAccountNumber, account -> account));

		List<Long> userIds = accountMap.values().stream().map(Account::getUserId).toList();
		Map<Long, User> userMap = userRepository.findAllById(userIds)
			.stream()
			.collect(Collectors.toMap(User::getId, user -> user));

		return transactionInfos.stream()
			.map(info -> EntityConverter.toTransactionLog(info, accountMap, userMap))
			.toList();
	}
}
