package org.c4marathon.assignment.global.error;

import org.c4marathon.assignment.global.response.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ResponseDto<Void>> handleRuntimeException(BaseException e) {

		ErrorCode errorCode = e.getErrorCode();
		String message = e.getMessage();
		return ResponseEntity.status(errorCode.getStatus()).body(ResponseDto.message(message));
	}
}
