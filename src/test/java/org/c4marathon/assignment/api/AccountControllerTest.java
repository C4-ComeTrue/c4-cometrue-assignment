package org.c4marathon.assignment.api;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.c4marathon.assignment.api.dto.ChargeAccountDto;
import org.c4marathon.assignment.api.dto.CreateAccountDto;
import org.c4marathon.assignment.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	AccountService accountService;

	ObjectMapper mapper = new ObjectMapper();

	@Test
	void 계좌를_생성한다() throws Exception {
		// given
		var accountId = 1L;
		var result = new CreateAccountDto.Res(accountId);
		var request = new CreateAccountDto.Req("name", "accountNumber", 1L);

		given(accountService.createAccount(anyLong(), anyString(), anyString())).willReturn(result);

		// when
		mockMvc.perform(
				post("/v1/accounts")
					.content(mapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
		).andExpectAll(
			status().isCreated(),
			jsonPath("$.id").value(accountId)
		);
	}

	@Test
	void 계좌에_충전한다() throws Exception {
		// given
		var accountId = 1L;
		var amount = 1000;
		var totalAmount = 10000;
		var result = new ChargeAccountDto.Res(totalAmount);
		var request = new ChargeAccountDto.Req(accountId, amount);

		given(accountService.charge(anyLong(), anyLong())).willReturn(result);

		// when
		mockMvc.perform(
			post("/v1/accounts/charge")
				.content(mapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON)
		).andExpectAll(
			status().isOk(),
			jsonPath("$.totalAmount").value(totalAmount)
		);
	}

	@Test
	void 계좌_번호가_null_이면_충전에_실패한다() throws Exception {
		var request = new ChargeAccountDto.Req(null, 1000);
		mockMvc.perform(
				post("/v1/accounts/charge")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
		)
			.andExpectAll(status().is4xxClientError());
	}

	@Test
	void 계좌_번호가_이면_충전에_실패한다() throws Exception {
		var request = new ChargeAccountDto.Req(null, 1000);
		mockMvc.perform(
				post("/v1/accounts/charge")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
		)
			.andExpectAll(status().is4xxClientError());
	}

	@Test
	void 충전_금액이_null_이면_충전에_실패한다() throws Exception {
		var request = new ChargeAccountDto.Req(1L, null);
		mockMvc.perform(
				post("/v1/accounts/charge")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
		)
			.andExpectAll(status().is4xxClientError());
	}

	@Test
	void 충전_금액이_음수이면_충전에_실패한다() throws Exception {
		var request = new ChargeAccountDto.Req(1L, -1000);
		mockMvc.perform(
				post("/v1/accounts/charge")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
		)
			.andExpectAll(status().is4xxClientError());
	}

	@Test
	void 멤버_아이디가_null_이면_계좌_생성에_실패한다() throws Exception {
		var request = new CreateAccountDto.Req("name", "1199-2245", null);
		mockMvc.perform(
				post("/v1/accounts")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
		)
			.andExpectAll(status().is4xxClientError());
	}

	@Test
	void 계좌_번호가_비어있다면_계좌_생성에_실패한다() throws Exception {
		var request = new CreateAccountDto.Req("name", null, 1L);
		mockMvc.perform(
				post("/v1/accounts")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
		)
			.andExpectAll(status().is4xxClientError());
	}
}
