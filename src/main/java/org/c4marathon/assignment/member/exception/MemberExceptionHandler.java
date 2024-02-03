package org.c4marathon.assignment.member.exception;

import org.c4marathon.assignment.common.response.ErrorResponse;
import org.c4marathon.assignment.common.utils.ExceptionLogHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class MemberExceptionHandler {

	@ExceptionHandler(MemberException.class)
	public ResponseEntity<ErrorResponse> handleMemberException(MemberException error) {
		ExceptionLogHelper.makeExceptionLog(log, error, error.getErrorName(), error.getDebugMessage());
		ErrorResponse errorResponse = ErrorResponse.of(error.getHttpStatus(), error.getErrorMessage());
		return ResponseEntity.status(error.getHttpStatus()).body(errorResponse);
	}
}
