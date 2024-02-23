package org.c4marathon.assignment.member.interceptor;

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
			return false;
		}

		return true;
	}
}
