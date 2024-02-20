package org.c4marathon.assignment.api;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.c4marathon.assignment.api.dto.ChargeSavingsAccountDto;
import org.c4marathon.assignment.api.dto.CreateSavingsAccountDto;
import org.c4marathon.assignment.domain.SavingsType;
import org.c4marathon.assignment.service.SavingsAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(SavingsAccountController.class)
class SavingsAccountControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	SavingsAccountService savingsAccountService;

	ObjectMapper mapper = new ObjectMapper();

	@Test
	void 적금_계좌를_생성한다() throws Exception {
		// given
		var accountId = 1L;
		var result = new CreateSavingsAccountDto.Res(accountId);
		var request = new CreateSavingsAccountDto.Req("name", 1L, 1000, SavingsType.REGULAR);

		given(savingsAccountService.createSavingsAccount(anyLong(), anyString(), anyLong(), any())).willReturn(result);

		// when
		mockMvc.perform(
			post("/v1/accounts/savings")
				.content(mapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON)
		).andExpectAll(
			status().isCreated(),
			jsonPath("$.id").value(accountId)
		);
	}

	@Test
	void 자유_적금_계좌에_충전한다() throws Exception {
		// given
		var accountId = 1L;
		var amount = 1000;
		var request = new ChargeSavingsAccountDto.Req(accountId, amount);

		// when
		mockMvc.perform(
			post("/v1/accounts/savings/charge")
				.content(mapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON)
		).andExpectAll(
			status().isOk()
		);
	}

	@Test
	void 멤버_ID가_null_이면_생성에_실패한다() throws Exception {
		var request = new CreateSavingsAccountDto.Req("name", null, 1000, SavingsType.REGULAR);
		mockMvc.perform(
				post("/v1/accounts/savings")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
			)
			.andExpectAll(status().is4xxClientError());
	}

	@Test
	void 적금_자동_이체액이_1_미만이면_생성에_실패한다() throws Exception {
		var request = new CreateSavingsAccountDto.Req("name", 1L, 0, SavingsType.REGULAR);
		mockMvc.perform(
				post("/v1/accounts/savings")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
			)
			.andExpectAll(status().is4xxClientError());
	}

	@Test
	void 적금_타입이_null_이면_생성에_실패한다() throws Exception {
		var request = new CreateSavingsAccountDto.Req("name", 1L, 1000, null);
		mockMvc.perform(
				post("/v1/accounts/savings")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
			)
			.andExpectAll(status().is4xxClientError());
	}

	@Test
	void 계좌_ID가_null_이면_충전에_실패한다() throws Exception {
		var request = new ChargeSavingsAccountDto.Req(null, 10000);
		mockMvc.perform(
				post("/v1/accounts/savings/charge")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
			)
			.andExpectAll(status().is4xxClientError());
	}

	@Test
	void 적금_충전액이_1_미만이면_충전에_실패한다() throws Exception {
		var request = new ChargeSavingsAccountDto.Req(1L, -100);
		mockMvc.perform(
				post("/v1/accounts/savings/charge")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
			)
			.andExpectAll(status().is4xxClientError());
	}
}
