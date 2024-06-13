package org.c4marathon.assignment.settlement.exception;

import org.c4marathon.assignment.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementErrorCode implements ErrorCode {
	WRONG_PARAMETER(HttpStatus.BAD_REQUEST, "입력값이 잘못되었습니다. 입력값을 다시 확인해 주세요.");
	private final HttpStatus httpStatus;
	private final String message;

	public SettlementException settlementException(String debugMessage) {
		return new SettlementException(getHttpStatus(), name(), getMessage(), debugMessage);
	}
}
