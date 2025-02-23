package org.c4marathon.assignment.domain.dto;

import java.time.LocalDateTime;

public interface TransactionInfo {
	long getId();
	String getSenderAccountNumber();
	String getReceiverAccountNumber();
	LocalDateTime getDeadline();
	long getBalance();
}
