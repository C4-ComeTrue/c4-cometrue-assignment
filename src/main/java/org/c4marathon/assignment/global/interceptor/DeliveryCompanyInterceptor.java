package org.c4marathon.assignment.global.interceptor;

import java.util.Optional;

import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;
import org.c4marathon.assignment.domain.deliverycompany.repository.DeliveryCompanyRepository;
import org.c4marathon.assignment.global.auth.DeliveryCompanyThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class DeliveryCompanyInterceptor implements HandlerInterceptor {

	private final DeliveryCompanyRepository deliveryCompanyRepository;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		Optional<String> email = Optional.ofNullable(request.getHeader("Authorization"));
		if (email.isEmpty()) {
			return false;
		}
		Optional<DeliveryCompany> deliveryCompany = deliveryCompanyRepository.findByEmail(email.get());
		if (deliveryCompany.isEmpty()) {
			return false;
		}
		DeliveryCompanyThreadLocal.set(deliveryCompany.get());

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
		ModelAndView modelAndView) throws Exception {
		DeliveryCompany deliveryCompany = DeliveryCompanyThreadLocal.get();

		if (deliveryCompany == null) {
			return;
		}

		DeliveryCompanyThreadLocal.remove();
	}
}
