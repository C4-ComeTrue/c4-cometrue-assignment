package org.c4marathon.assignment.domain.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import org.c4marathon.assignment.domain.auth.controller.AuthController;
import org.c4marathon.assignment.domain.auth.service.AuthService;
import org.c4marathon.assignment.domain.consumer.controller.ConsumerController;
import org.c4marathon.assignment.domain.consumer.service.ConsumerService;
import org.c4marathon.assignment.domain.deliverycompany.controller.DeliveryCompanyController;
import org.c4marathon.assignment.domain.deliverycompany.service.DeliveryCompanyService;
import org.c4marathon.assignment.domain.pay.controller.PayController;
import org.c4marathon.assignment.domain.pay.service.PayService;
import org.c4marathon.assignment.domain.seller.controller.SellerController;
import org.c4marathon.assignment.domain.seller.service.SellerService;
import org.c4marathon.assignment.global.interceptor.ConsumerInterceptor;
import org.c4marathon.assignment.global.interceptor.DeliveryCompanyInterceptor;
import org.c4marathon.assignment.global.interceptor.SellerInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebMvcTest(controllers = {AuthController.class, ConsumerController.class, DeliveryCompanyController.class,
	PayController.class, SellerController.class})
@MockBean(JpaMetamodelMappingContext.class)
public abstract class ControllerTestSupport {

	@BeforeEach
	void setUp(WebApplicationContext webApplicationContext) {
		given(sellerInterceptor.preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any()))
			.willReturn(true);
		given(consumerInterceptor.preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any()))
			.willReturn(true);
		given(
			deliveryCompanyInterceptor.preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any()))
			.willReturn(true);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		this.om = new ObjectMapper();
	}

	protected MockMvc mockMvc;
	protected ObjectMapper om;
	@MockBean
	protected SellerInterceptor sellerInterceptor;
	@MockBean
	protected ConsumerInterceptor consumerInterceptor;
	@MockBean
	protected DeliveryCompanyInterceptor deliveryCompanyInterceptor;
	@MockBean
	protected ConsumerService consumerService;
	@MockBean
	protected SellerService sellerService;
	@MockBean
	protected DeliveryCompanyService deliveryCompanyService;
	@MockBean
	protected PayService payService;
	@MockBean
	protected AuthService authService;
}