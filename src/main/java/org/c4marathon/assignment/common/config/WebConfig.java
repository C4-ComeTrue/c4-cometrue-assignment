package org.c4marathon.assignment.common.config;

import java.util.List;

import org.c4marathon.assignment.common.argumentresolver.LoginMemberArgumentResolver;
import org.c4marathon.assignment.member.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new LoginMemberArgumentResolver());
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new LoginInterceptor())
			.order(1)
			.addPathPatterns("/**")
			.excludePathPatterns("/api/members/signup", "/api/members/signin");
	}
}
