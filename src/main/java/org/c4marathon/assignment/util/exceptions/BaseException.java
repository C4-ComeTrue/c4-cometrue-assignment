package org.c4marathon.assignment.util.exceptions;

import org.springframework.http.HttpStatus;

public class BaseException extends RuntimeException {

    private final BaseResponseStatus responseStatus;
    private final HttpStatus httpStatus;

    public BaseException(BaseResponseStatus responseStatus, HttpStatus httpStatus) {
        super(responseStatus.getMessage());
        this.responseStatus = responseStatus;
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public BaseResponseStatus getResponseStatus() {
        return responseStatus;
    }
}
