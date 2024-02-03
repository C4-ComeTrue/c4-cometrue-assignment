package org.c4marathon.assignment.common.argumentresolver;

import org.c4marathon.assignment.common.annotation.Login;
import org.c4marathon.assignment.common.exception.CommonErrorCode;
import org.c4marathon.assignment.member.session.SessionConst;
import org.c4marathon.assignment.member.session.SessionMemberInfo;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * @Login을 사용하면 세션에서 로그인한 사용자 정보를 찾아 반환해주는 클래스
 */
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {
	/**
	 *
	 * @param parameter the method parameter to check
	 * @return @Login 애노테이션과 SessionMemberInfo 파라미터가 있으면 true를 반환하여 ArgumentResolver가 사용된다.
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {

		boolean hasLoginAnnotation = parameter.hasParameterAnnotation(Login.class);
		boolean hasMemberType = SessionMemberInfo.class.isAssignableFrom(parameter.getParameterType());

		return hasLoginAnnotation && hasMemberType;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

		HttpServletRequest request = (HttpServletRequest)webRequest.getNativeRequest();
		HttpSession session = request.getSession();

		if (session == null) {
			throw CommonErrorCode.UNAUTHORIZED_USER.commonException("인증 단계 중 resolveArgument 과정에서 세션이 없는 사용자의 접근 발생");
		}

		return session.getAttribute(SessionConst.MEMBER_INFO);
	}
}
