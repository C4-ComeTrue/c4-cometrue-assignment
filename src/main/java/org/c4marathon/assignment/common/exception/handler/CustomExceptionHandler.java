package org.c4marathon.assignment.common.exception.handler;

import java.util.List;
import java.util.Objects;

import org.c4marathon.assignment.common.exception.BusinessException;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.common.exception.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handle(BusinessException ex) {
		log.error(ex.getMessage(), ex);

		if (!Objects.isNull(ex.getDebugMessage())) {
			log.debug(ex.getDebugMessage());
		}

		ErrorCode errorCode = ErrorCode.valueOf(ex.getErrorCode());
		ErrorResponse errorResponse = new ErrorResponse(ex.getErrorMessage());
		return ResponseEntity.status(errorCode.getStatus()).body(errorResponse);
	}

	@ExceptionHandler
	public ResponseEntity<Object> handle(BindException ex) {
		log.error(ex.getMessage(), ex);
		List<ErrorResponse.ErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
			.map(ErrorResponse.ErrorDetail::from)
			.toList();

		ErrorResponse errorResponse = new ErrorResponse(ErrorCode.BAD_REQUEST_ERROR.getMessage(), errors);
		return ResponseEntity.badRequest().body(errorResponse);
	}

	@ExceptionHandler
	public ResponseEntity<Object> handle(Exception ex) {
		log.error(ex.getMessage(), ex);
		return ResponseEntity.internalServerError().body(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
	}
}
