package org.c4marathon.assignment.common.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
	String name();

	HttpStatus getHttpStatus();

	String getMessage();
}
