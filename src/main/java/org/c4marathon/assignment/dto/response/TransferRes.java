package org.c4marathon.assignment.dto.response;

import java.time.Instant;

public record TransferRes(
	Instant transferDate,
	Long mainAccountBalance
) {
	public TransferRes(long mainAccountBalance) {
		this(
			Instant.now(),
			mainAccountBalance
		);
	}
}
