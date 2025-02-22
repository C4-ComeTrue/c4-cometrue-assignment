package org.c4marathon.assignment.domain.dto;

import java.time.LocalDateTime;

public interface TransactionRemindInfo {
	long getId();
	String getReceiverAccountNumber();
	LocalDateTime getDeadline();
	long getBalance();
}
