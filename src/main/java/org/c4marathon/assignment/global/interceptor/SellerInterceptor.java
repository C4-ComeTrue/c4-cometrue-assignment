package org.c4marathon.assignment.global.interceptor;

import java.util.Optional;

import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.c4marathon.assignment.domain.seller.repository.SellerRepository;
import org.c4marathon.assignment.global.auth.SellerThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class SellerInterceptor implements HandlerInterceptor {

	private final SellerRepository sellerRepository;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		Optional<String> email = Optional.ofNullable(request.getHeader("Authorization"));
		if (email.isEmpty()) {
			return false;
		}
		Optional<Seller> seller = sellerRepository.findByEmail(email.get());
		if (seller.isEmpty()) {
			return false;
		}
		SellerThreadLocal.set(seller.get());

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
		ModelAndView modelAndView) throws Exception {
		Seller seller = SellerThreadLocal.get();

		if (seller == null) {
			return;
		}

		SellerThreadLocal.remove();
	}
}
