package org.c4marathon.assignment.common.exception;

import org.c4marathon.assignment.common.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

import static org.c4marathon.assignment.common.utils.ExceptionLogHelper.*;

@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

	@ExceptionHandler(CommonException.class)
	public ResponseEntity<ErrorResponse> handleCommonException(CommonException error) {
		ErrorResponse errorResponse = ErrorResponse.of(error.getHttpStatus(), error.getErrorMessage());
		makeExceptionLog(log, error, error.getErrorName(), error.getDebugMessage());

		return ResponseEntity.status(error.getHttpStatus()).body(errorResponse);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException error) {
		ErrorCode errorCode = CommonErrorCode.INVALID_ARGUMENT_ERROR;
		ErrorResponse errorResponse = ErrorResponse.of(errorCode.getHttpStatus(), errorCode.getMessage(),
			error.getBindingResult());
		makeExceptionLog(log, error, errorCode.name());

		return ResponseEntity.status(errorCode.getHttpStatus()).body(errorResponse);
	}
}
