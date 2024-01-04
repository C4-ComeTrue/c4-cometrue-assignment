package org.c4marathon.assignment.util.exceptions.handler;

import org.c4marathon.assignment.util.exceptions.BaseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<String> handleCustomException(BaseException e) {
        return new ResponseEntity<>(e.getMessage(), e.getHttpStatus());
    }
}