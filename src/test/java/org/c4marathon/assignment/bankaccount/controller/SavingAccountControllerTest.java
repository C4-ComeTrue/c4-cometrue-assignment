package org.c4marathon.assignment.bankaccount.controller;

import java.util.HashMap;
import java.util.Map;

import org.c4marathon.assignment.bankaccount.product.ProductManager;
import org.c4marathon.assignment.bankaccount.service.SavingAccountService;
import org.c4marathon.assignment.common.session.SessionConst;
import org.c4marathon.assignment.common.session.SessionMemberInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SavingAccountController.class)
class SavingAccountControllerTest {

	private String REQUEST_URL = "/api/accounts/saving";
	@Autowired
	MockMvc mockMvc;

	@MockBean
	private SavingAccountService savingAccountService;

	@MockBean
	private ProductManager productManager;

	private MockHttpSession session;

	@BeforeEach
	public void setSession() {
		session = new MockHttpSession();
		SessionMemberInfo sessionMemberInfo = SessionMemberInfo.builder()
			.memberPk(1L)
			.memberId("testId")
			.mainAccountPk(1L)
			.build();
		session.setAttribute(SessionConst.MEMBER_INFO, sessionMemberInfo);
	}

	@Nested
	@DisplayName("적금 상품 조회 테스트")
	class GetProductInfo {
		@Test
		@DisplayName("로그인한 사용자라면 상품 정보를 반환받는다.")
		void request_with_a_valid_member() throws Exception {
			// Given
			Map<String, Integer> map = new HashMap<>();
			map.put("free", 500);
			given(productManager.getProductInfo()).willReturn(map);

			// When
			ResultActions resultActions = mockMvc.perform(get(REQUEST_URL + "/products")
				.session(session));

			// Then
			resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.free").value(500));
		}
	}

	@Nested
	@DisplayName("적금 계좌 생성 테스트")
	class Create {
		private String productName = "free";

		@Test
		@DisplayName("로그인한 사용자면 적금 계좌 생성에 성공한다.")
		void request_with_login_member() throws Exception {
			// When
			ResultActions resultActions = mockMvc.perform(get(REQUEST_URL + "/{productName}", productName)
				.session(session));

			// Then
			resultActions
				.andExpect(status().isOk());
		}
	}
}
