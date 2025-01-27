package org.c4marathon.assignment.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<Object> handleCustomException(CustomException customException) {
		ErrorCode errorCode = customException.getErrorCode();
		return handleExceptionInternal(errorCode);
	}

	private ResponseEntity<Object> handleExceptionInternal(ErrorCode errorCode) {
		return ResponseEntity.status(errorCode.getHttpStatus()).body(makeErrorResponse(errorCode));
	}

	private ErrorResponse makeErrorResponse(ErrorCode errorCode) {
		return ErrorResponse.builder().code(errorCode.name()).message(errorCode.getMessage()).build();
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
		MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

		List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(ErrorResponse.ValidationError::of)
			.collect(Collectors.toList());

		ErrorResponse errorResponse = ErrorResponse.builder()
			.code(ErrorCode.INVALID_PARAMETER.name())
			.message(ErrorCode.INVALID_PARAMETER.getMessage())
			.errors(validationErrors)
			.build();

		return new ResponseEntity<>(errorResponse, ErrorCode.INVALID_PARAMETER.getHttpStatus());
	}
}
