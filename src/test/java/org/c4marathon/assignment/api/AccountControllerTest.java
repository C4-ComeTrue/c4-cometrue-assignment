package org.c4marathon.assignment.api;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.c4marathon.assignment.api.dto.CreateAccountDto;
import org.c4marathon.assignment.api.dto.TransferAccountDto;
import org.c4marathon.assignment.service.AccountService;
import org.c4marathon.assignment.service.ChargeService;
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

	@MockBean
	ChargeService chargeService;

	ObjectMapper mapper = new ObjectMapper();

	@Test
	void 계좌를_생성한다() throws Exception {
		// given
		var accountId = 1L;
		var response = new CreateAccountDto.Res(accountId);
		var request = new CreateAccountDto.Req("name", "accountNumber", 1L);

		given(accountService.createAccount(anyLong(), anyString(), anyString())).willReturn(response);

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

	@Test
	void 상대방_계좌로_송금한다() throws Exception {
		var accountId = 1L;
		var accountNumber = "111-222";
		var transferAmount = 1000L;
		var totalAmount = 9000L;
		var request = new TransferAccountDto.Req(accountId, accountNumber, transferAmount);
		var response = new TransferAccountDto.Res(totalAmount);

		given(accountService.transfer(anyLong(), anyString(), anyLong())).willReturn(response);

		// when + then
		mockMvc.perform(
			post("/v1/accounts/transfer")
				.content(mapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON)
		).andExpectAll(
			status().isOk(),
			jsonPath("$.totalAmount").value(totalAmount)
		);

	}

	@Test
	void 송금할_계좌_번호가_비어있다면_송금에_실패한다() throws Exception {
		// given
		var accountId = 1L;
		var accountNumber = " ";
		var transferAmount = 1000L;
		var request = new TransferAccountDto.Req(accountId, accountNumber, transferAmount);

		// when + then
		mockMvc.perform(
			post("/v1/accounts/transfer")
				.content(mapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().is4xxClientError());
	}

	@Test
	void 계좌_ID가_null_이라면_송금에_실패한다() throws Exception {
		Long accountId = null;
		var accountNumber = "111-222";
		var transferAmount = 1000L;
		var request = new TransferAccountDto.Req(accountId, accountNumber, transferAmount);

		// when + then
		mockMvc.perform(
				post("/v1/accounts/transfer")
					.content(mapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().is4xxClientError());
	}

	@Test
	void 송금_금액이_0원_이하라면_송금에_실패한다() throws Exception {
		var accountId = 1L;
		var accountNumber = "111-222";
		var transferAmount = 0L;
		var request = new TransferAccountDto.Req(accountId, accountNumber, transferAmount);

		// when + then
		mockMvc.perform(
				post("/v1/accounts/transfer")
					.content(mapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().is4xxClientError());
	}
}
