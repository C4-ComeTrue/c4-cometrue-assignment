package org.c4marathon.assignment.transactional.domain;

public enum TransactionalStatus {
	WITHDRAW,
	PENDING_DEPOSIT,
	SUCCESS_DEPOSIT,
	FAILED_DEPOSIT,
	CANCEL
}
