package org.c4marathon.assignment.member.interceptor;

import org.c4marathon.assignment.common.exception.CommonErrorCode;
import org.c4marathon.assignment.common.response.ErrorResponse;
import org.c4marathon.assignment.member.session.SessionConst;
import org.c4marathon.assignment.member.session.SessionMemberInfo;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LoginInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
		Exception {

		HttpSession session = request.getSession();
		SessionMemberInfo memberInfo = (SessionMemberInfo)session.getAttribute(SessionConst.MEMBER_INFO);

		if (memberInfo == null) {
			HttpStatus httpStatus = CommonErrorCode.UNAUTHORIZED_USER.getHttpStatus();
			String message = CommonErrorCode.UNAUTHORIZED_USER.getMessage();
			ErrorResponse errorResponse = ErrorResponse.of(httpStatus, message);
			ObjectMapper objectMapper = new ObjectMapper();
			String errorResponseBody = objectMapper.writeValueAsString(errorResponse);
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.setContentType("application/json;charset=utf-8");
			response.getWriter().write(errorResponseBody);

			return false;
		}

		return true;
	}
}
