package org.c4marathon.assignment.bankaccount.exception.async;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccountAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

	@Override
	public void handleUncaughtException(Throwable ex, Method method, Object... params) {
		StringBuilder sb = new StringBuilder();

		Parameter[] parameters = method.getParameters();
		int parameterIndex = 0;
		for (Object param : params) {
			sb.append(parameters[parameterIndex++]).append(" = ").append(param).append(" , ");
		}
		AccountAsyncException exception = (AccountAsyncException)ex;

		// 실제 환경에서는 메일 같은 것으로 에러 발생을 알려줘야 할 것 같다.
		log.error(
			"error name : {} | error message: {} | error method: {} | error parameters: ",
			exception.getErrorName(), exception.getMessage(), method.getName(),
			sb);
	}
}
