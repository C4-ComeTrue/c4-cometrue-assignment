package org.c4marathon.assignment.global.interceptor;

import org.c4marathon.assignment.domain.consumer.entity.Consumer;
import org.c4marathon.assignment.domain.deliverycompany.entity.DeliveryCompany;
import org.c4marathon.assignment.domain.seller.entity.Seller;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public abstract class InterceptorTestSupport {

	@Mock
	protected HttpServletRequest request;
	@Mock
	protected HttpServletResponse response;
	@Mock
	protected Object handler;
	@Mock
	protected ModelAndView modelAndView;
	@Mock
	protected Consumer consumer;
	@Mock
	protected Seller seller;
	@Mock
	protected DeliveryCompany deliveryCompany;
}
