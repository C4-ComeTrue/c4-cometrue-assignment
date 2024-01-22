package org.c4marathon.assignment.bankaccount.exception;

import org.c4marathon.assignment.common.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

import static org.c4marathon.assignment.common.utils.ExceptionLogHelper.*;

@Slf4j
@RestControllerAdvice
public class AccountExceptionHandler {

	@ExceptionHandler(AccountException.class)
	public ResponseEntity<ErrorResponse> handleAccountException(AccountException error) {
		makeExceptionLog(log, error, error.getErrorName(), error.getDebugMessage());
		ErrorResponse errorResponse = ErrorResponse.of(error.getHttpStatus(), error.getErrorMessage());
		return ResponseEntity.status(error.getHttpStatus()).body(errorResponse);
	}
}
