package org.c4marathon.assignment.bankaccount.exception;

import org.c4marathon.assignment.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountErrorCode implements ErrorCode {
	CHARGE_LIMIT_EXCESS(HttpStatus.BAD_REQUEST, "일일 충전 한도를 초과했습니다."),
	ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 계좌입니다."),
	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 적금 상품입니다.");
	private final HttpStatus httpStatus;
	private final String message;

	public AccountException accountException() {
		return new AccountException(getHttpStatus(), name(), getMessage());
	}

	public AccountException accountException(String debugMessage) {
		return new AccountException(getHttpStatus(), name(), getMessage(), debugMessage);
	}
}
