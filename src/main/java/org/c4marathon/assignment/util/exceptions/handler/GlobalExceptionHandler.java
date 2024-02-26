package org.c4marathon.assignment.util.exceptions.handler;

import org.c4marathon.assignment.util.exceptions.BaseException;
import org.c4marathon.assignment.util.exceptions.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.log4j.Log4j2;

@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ExceptionResponse> handleCustomException(BaseException baseException) {
        ErrorCode errorCode = ErrorCode.valueOf(baseException.getErrorCode());
        log.debug("DebugMessage: " + baseException.getDebugMessage(), baseException);
        return new ResponseEntity<>(new ExceptionResponse(errorCode.name(), baseException.getMessage()),
            errorCode.getStatus());
    }

    private record ExceptionResponse(
        String errorCode,
        String message
    ) {
    }
}