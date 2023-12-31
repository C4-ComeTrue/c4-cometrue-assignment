package org.c4marathon.assignment.global.configuration;

import org.c4marathon.assignment.global.interceptor.ConsumerInterceptor;
import org.c4marathon.assignment.global.interceptor.SellerInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfiguration implements WebMvcConfigurer {

	private final ConsumerInterceptor consumerInterceptor;
	private final SellerInterceptor sellerInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry
			.addInterceptor(consumerInterceptor)
			.addPathPatterns("/consumers/**");

		registry
			.addInterceptor(sellerInterceptor)
			.addPathPatterns("/sellers/**");
	}
}
