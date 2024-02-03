package org.c4marathon.assignment.member.interceptor;

import org.c4marathon.assignment.common.exception.CommonErrorCode;
import org.c4marathon.assignment.member.session.SessionConst;
import org.c4marathon.assignment.member.session.SessionMemberInfo;
import org.springframework.web.servlet.HandlerInterceptor;

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
			throw CommonErrorCode.UNAUTHORIZED_USER.commonException(
				"LoginInterceptor 사용자 세션 처리 중 로그인을 하지 않은 사용자의 요청 발생.");
		}

		return true;
	}
}
