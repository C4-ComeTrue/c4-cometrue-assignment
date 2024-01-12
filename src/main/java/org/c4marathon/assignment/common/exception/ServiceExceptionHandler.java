package org.c4marathon.assignment.common.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import ch.qos.logback.classic.Logger;

@RestControllerAdvice
public class ServiceExceptionHandler {
	private final Logger logger = (Logger)LoggerFactory.getLogger(this.getClass().getSimpleName());

	@ExceptionHandler(value = {ServiceException.class})
	public ResponseEntity<ErrorResponse> handleServiceException(ServiceException exception) {
		if (exception.getDebugMessage() != null) {
			logger.debug(exception.getDebugMessage());
		}

		var errorResponse = new ErrorResponse(
			exception.getErrorCode(),
			exception.getErrorMessage()
		);

		var httpStatus = ErrorCode.valueOf(exception.getErrorCode()).getHttpStatus();

		return new ResponseEntity<>(errorResponse, httpStatus);
	}
}