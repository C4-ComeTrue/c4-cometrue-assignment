package org.c4marathon.assignment.common.exception;

import org.c4marathon.assignment.common.response.ErrorResponse;
import org.c4marathon.assignment.common.utils.ExceptionLogHelper;
import org.c4marathon.assignment.member.exception.MemberException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 정의 예외에 없는 예외를 처리하는 Handler 클래스
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@RestControllerAdvice
public class ServerExceptionHandler {

	@ExceptionHandler(MemberException.class)
	public ResponseEntity<ErrorResponse> handleMemberException(MemberException error) {
		ExceptionLogHelper.makeExceptionLog(log, error, error.getErrorName(), error.getDebugMessage());
		ErrorResponse errorResponse = ErrorResponse.of(error.getHttpStatus(), error.getErrorMessage());
		return ResponseEntity.status(error.getHttpStatus()).body(errorResponse);
	}
}
