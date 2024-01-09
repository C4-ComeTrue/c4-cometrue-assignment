package org.c4marathon.assignment.global.interceptor;

import java.util.Optional;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.consumer.repository.ConsumerRepository;
import org.c4marathon.assignment.global.auth.ConsumerThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ConsumerInterceptor implements HandlerInterceptor {

	private final ConsumerRepository consumerRepository;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		Optional<String> email = Optional.ofNullable(request.getHeader("Authorization"));
		if (email.isEmpty()) {
			return false;
		}
		Optional<Consumer> consumer = consumerRepository.findByEmail(email.get());
		if (consumer.isEmpty()) {
			return false;
		}
		ConsumerThreadLocal.set(consumer.get());

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
		ModelAndView modelAndView) throws Exception {
		Consumer consumer = ConsumerThreadLocal.get();

		if (consumer == null) {
			return;
		}

		ConsumerThreadLocal.remove();
	}
}
