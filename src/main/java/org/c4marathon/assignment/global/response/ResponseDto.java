package org.c4marathon.assignment.global.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResponseDto<T> {

	private String message;
	private T data;

	public static ResponseDto<Void> message(String message) {
		return new ResponseDto<>(message, null);
	}

	public static <T> ResponseDto<T> data(T data) {
		return new ResponseDto<>(null, data);
	}

}
