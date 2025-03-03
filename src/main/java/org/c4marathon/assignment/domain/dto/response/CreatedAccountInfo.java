package org.c4marathon.assignment.domain.dto.response;

import java.time.Instant;

import org.c4marathon.assignment.domain.type.AccountType;

public record CreatedAccountInfo(String accountNumber,
								 Instant createdAt,
								 AccountType accountType,
								 long balance,
								 boolean isMain) {
}
