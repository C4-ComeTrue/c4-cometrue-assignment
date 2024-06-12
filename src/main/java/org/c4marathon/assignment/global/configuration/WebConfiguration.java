package org.c4marathon.assignment.global.configuration;

import org.c4marathon.assignment.global.interceptor.AdminInterceptor;
import org.c4marathon.assignment.global.interceptor.ConsumerInterceptor;
import org.c4marathon.assignment.global.interceptor.DeliveryCompanyInterceptor;
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
	private final DeliveryCompanyInterceptor deliveryCompanyInterceptor;
	private final AdminInterceptor adminInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry
			.addInterceptor(consumerInterceptor)
			.addPathPatterns("/consumers/**", "/reviews/**");

		registry
			.addInterceptor(sellerInterceptor)
			.addPathPatterns("/sellers/**");

		registry
			.addInterceptor(deliveryCompanyInterceptor)
			.addPathPatterns("/orders/deliveries/**");
		registry
			.addInterceptor(adminInterceptor)
			.addPathPatterns("/event/**", "/discount-policy/**");
	}
}
