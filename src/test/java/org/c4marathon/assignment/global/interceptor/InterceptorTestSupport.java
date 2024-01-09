package org.c4marathon.assignment.global.interceptor;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public abstract class InterceptorTestSupport {

	@Mock
	protected HttpServletRequest request;

	@Mock
	protected HttpServletResponse response;

	@Mock
	protected Object handler;

	@Mock
	protected ModelAndView modelAndView;
}
