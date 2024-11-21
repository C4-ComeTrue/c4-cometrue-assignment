package org.c4marathon.assignment.bankaccount.exception.async;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountAsyncErrorCode {
	SEND_ROLLBACK_FAILED("이체 롤백에 실패했습니다");
	private final String message;

	public AccountAsyncException accountAsyncException() {
		return new AccountAsyncException(name(), getMessage());
	}
}



