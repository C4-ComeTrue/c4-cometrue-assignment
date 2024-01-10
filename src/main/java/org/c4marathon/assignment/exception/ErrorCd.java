package org.c4marathon.assignment.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCd {
	INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "Invalid Argument"),
	NOT_FOUND(HttpStatus.NOT_FOUND, "HTTP not found"),
	NO_PERMISSION(HttpStatus.FORBIDDEN, "No Permission"),
	NOT_EXIST_USER(HttpStatus.NOT_FOUND, "User Not Found"),
	NO_SUCH_ITEM(HttpStatus.NOT_FOUND, "Item Not Found"),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");

	private final HttpStatus httpStatus;
	private final String message;

	public ServiceException serviceException() {
		return new ServiceException(this.name(), message);
	}

	public ServiceException serviceException(String debugMessage, Object...debugMessageArgs) {
		return new ServiceException(this.name(), message, String.format(debugMessage, debugMessageArgs));
	}
}
