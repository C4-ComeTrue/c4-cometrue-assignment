package org.c4marathon.assignment.global.event.transactional;

import java.time.LocalDateTime;

import org.c4marathon.assignment.transactional.domain.TransactionalStatus;
import org.c4marathon.assignment.transactional.domain.TransactionalType;

public record TransactionalCreateEvent(
	Long senderAccountId,
	Long receiverAccountId,
	long amount,
	TransactionalType type,
	TransactionalStatus status,
	LocalDateTime sendTime
) {

}
