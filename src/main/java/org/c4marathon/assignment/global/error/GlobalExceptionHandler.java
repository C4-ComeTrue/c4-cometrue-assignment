package org.c4marathon.assignment.global.error;

import static org.c4marathon.assignment.global.error.ErrorCode.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ExceptionResponse> handleRuntimeException(BaseException baseException) {
		ErrorCode errorCode = valueOf(baseException.getErrorCode());
		String message = baseException.getMessage();
		log.debug(baseException.getDebugMessage());
		return new ResponseEntity<>(new ExceptionResponse(errorCode.name(), message), errorCode.getStatus());
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<BindExceptionResponse> handleBindException(BindingResult result) {
		FieldError fieldError = result.getFieldErrors().get(0);
		log.debug("field: {}, value: {}, message: {}",
			fieldError.getField(), fieldError.getRejectedValue(), fieldError.getDefaultMessage());
		return new ResponseEntity<>(
			new BindExceptionResponse(BIND_ERROR.name(), BIND_ERROR.getMessage(), fieldError.getField()),
			HttpStatus.BAD_REQUEST
		);
	}

	@ExceptionHandler(value = ConstraintViolationException.class)
	public ResponseEntity<ExceptionResponse> constraintViolationException(ConstraintViolationException e) {
		return new ResponseEntity<>(
			new ExceptionResponse(BIND_ERROR.name(), BIND_ERROR.getMessage()),
			HttpStatus.BAD_REQUEST
		);
	}

	private record ExceptionResponse(
		String errorCode,
		String message
	) {
	}

	private record BindExceptionResponse(
		String errorCode,
		String message,
		String field
	) {
	}
}
