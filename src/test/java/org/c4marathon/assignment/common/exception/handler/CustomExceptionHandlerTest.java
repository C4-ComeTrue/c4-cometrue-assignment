package org.c4marathon.assignment.common.exception.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Objects;

import org.c4marathon.assignment.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class CustomExceptionHandlerTest {

	@InjectMocks
	CustomExceptionHandler exceptionHandler;

	@Test
	void 비즈니스_예외_테스트(){
		// given
		var message = "존재하지 않는 계좌입니다.";
		var mockEx = mock(BusinessException.class);
		given(mockEx.getErrorCode()).willReturn("INVALID_ACCOUNT");
		given(mockEx.getErrorMessage()).willReturn(message);

		// when
		var response = exceptionHandler.handle(mockEx);

		// then
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertEquals(message, Objects.requireNonNull(response.getBody()).getMessage());
	}

	@Test
	void 비즈니스_예외_디버그_메시지_테스트() {
		// given
		var message = "존재하지 않는 계좌입니다.";
		var mockEx = mock(BusinessException.class);
		given(mockEx.getErrorCode()).willReturn("INVALID_ACCOUNT");
		given(mockEx.getErrorMessage()).willReturn(message);
		given(mockEx.getDebugMessage()).willReturn("debug message");

		// when
		var response = exceptionHandler.handle(mockEx);

		// then
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertEquals(message, Objects.requireNonNull(response.getBody()).getMessage());
	}

	@Test
	void 나머지_모든_예외_테스트() {
		// given
		var mockEx = mock(Exception.class);

		// when
		var response = exceptionHandler.handle(mockEx);

		//then
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
	}
}
