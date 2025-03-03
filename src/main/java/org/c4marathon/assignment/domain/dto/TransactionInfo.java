package org.c4marathon.assignment.domain.dto;

import java.time.Instant;

public interface TransactionInfo {
	long getId();
	String getSenderAccountNumber();
	String getReceiverAccountNumber();
	Instant getDeadline();
	long getBalance();
}
