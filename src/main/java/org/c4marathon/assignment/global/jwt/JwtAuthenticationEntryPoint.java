package org.c4marathon.assignment.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.c4marathon.assignment.global.exception.CustomException;
import org.c4marathon.assignment.global.exception.ErrorCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.Map;

// 필터에서 인증을 처리하는 과정 중 발생하는 예외를 처리해주기 위한 커스텀 핸들러
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // AuthenticationException이 발생할 경우 commence() 가 실행
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        CustomException e = new CustomException(ErrorCode.TOKEN_VALIDATION_EXCEPTION);

        ObjectMapper objectMapper = new ObjectMapper();

        response.setStatus(e.getErrorCode().getHttpStatusCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE+ ";charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("message", e.getErrorCode().getMessage(), "code", e.getErrorCode())));
    }

}
