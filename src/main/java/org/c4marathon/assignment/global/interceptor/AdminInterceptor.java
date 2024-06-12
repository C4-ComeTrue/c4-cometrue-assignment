package org.c4marathon.assignment.global.interceptor;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AdminInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		Optional<String> email = Optional.ofNullable(request.getHeader("Authorization"));
		return email.isPresent() && StringUtils.equals(email.get(), "admin");
	}
}
