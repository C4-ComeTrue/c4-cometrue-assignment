package org.c4marathon.assignment.global.error;

import org.c4marathon.assignment.global.response.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ResponseDto<Void>> handleRuntimeException(BaseException baseException) {

		ErrorCode errorCode = baseException.getErrorCode();
		String message = baseException.getMessage();
		return ResponseEntity.status(errorCode.getStatus()).body(ResponseDto.message(message));
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ResponseDto<Object>> bindExceptionHandler(BindingResult result) {
		FieldError fieldError = result.getFieldErrors().get(0);
		return new ResponseEntity<>(
			ResponseDto.builder()
				.message(fieldError.getDefaultMessage())
				.build(),
			HttpStatus.BAD_REQUEST);
	}
}
