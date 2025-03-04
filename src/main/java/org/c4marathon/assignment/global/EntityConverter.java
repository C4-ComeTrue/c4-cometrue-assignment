package org.c4marathon.assignment.global;

import java.util.Map;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.TransactionLog;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.domain.dto.TransactionInfo;

public abstract class EntityConverter {
	private EntityConverter() {}

	public static TransactionLog toTransactionLog(TransactionInfo transactionInfo,
		Map<String, Account> accountMap, Map<Long, User> userMap) {
		Account senderAccount = accountMap.getOrDefault(transactionInfo.getSenderAccountNumber(), null);
		Account receiverAccount = accountMap.getOrDefault(transactionInfo.getReceiverAccountNumber(), null);
		User sender = (senderAccount == null) ? null : userMap.getOrDefault(senderAccount.getUserId(), null);
		User receiver = (receiverAccount == null) ? null : userMap.getOrDefault(receiverAccount.getUserId(), null);

		return TransactionLog.builder()
			.transactionId(transactionInfo.getId())
			.sendingName(transactionInfo.getSendingName())
			.senderAccountNumber(transactionInfo.getSenderAccountNumber())
			.senderEmail(sender == null ? null : sender.getEmail())
			.senderId(sender == null ? null : sender.getId())
			.senderName(sender == null ? null : sender.getName())
			.receiverId(receiver == null ? null : receiver.getId())
			.receiverName(receiver == null ? null : receiver.getName())
			.receiverEmail(receiver == null ? null : receiver.getEmail())
			.receiverAccountNumber(transactionInfo.getReceiverAccountNumber())
			.sendTime(transactionInfo.getCreatedAt())
			.money(transactionInfo.getBalance())
			.build();
	}
}
