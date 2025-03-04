package org.c4marathon.assignment.domain.dto;

import java.time.Instant;

public interface TransactionInfo {
	long getId();
	String getSendingName();
	String getSenderAccountNumber();
	String getReceiverAccountNumber();
	Instant getCreatedAt();
	Instant getDeadline();
	long getBalance();
}
