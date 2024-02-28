package org.c4marathon.assignment.api;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.c4marathon.assignment.api.dto.ChargeAccountDto;
import org.c4marathon.assignment.api.dto.CreateChargeLinkedAccountDto;
import org.c4marathon.assignment.service.ChargeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ChargeController.class)
class ChargeControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	ChargeService chargeService;

	ObjectMapper mapper = new ObjectMapper();

	@Test
	void 충전_연동_계좌를_등록한다() throws Exception {
		// given
		var accountId = 1L;
		var bank = "우리은행";
		var accountNumber = "111-2222";
		var isMain = true;
		var request = new CreateChargeLinkedAccountDto.Req(accountId, bank, accountNumber, isMain);

		// when then
		mockMvc.perform(
			post("/v1/charge-accounts")
				.content(mapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON)
		).andExpectAll(
			status().isCreated()
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

		given(chargeService.charge(anyLong(), anyLong())).willReturn(result);

		// when + then
		mockMvc.perform(
			post("/v1/charge-accounts/charge")
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
				post("/v1/charge-accounts/charge")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
			)
			.andExpectAll(status().is4xxClientError());
	}

	@Test
	void 계좌_번호가_이면_충전에_실패한다() throws Exception {
		var request = new ChargeAccountDto.Req(null, 1000);
		mockMvc.perform(
				post("/v1/charge-accounts/charge")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
			)
			.andExpectAll(status().is4xxClientError());
	}

	@Test
	void 충전_금액이_null_이면_충전에_실패한다() throws Exception {
		var request = new ChargeAccountDto.Req(1L, null);
		mockMvc.perform(
				post("/v1/charge-accounts/charge")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
			)
			.andExpectAll(status().is4xxClientError());
	}

	@Test
	void 충전_금액이_음수이면_충전에_실패한다() throws Exception {
		var request = new ChargeAccountDto.Req(1L, -1000);
		mockMvc.perform(
				post("/v1/charge-accounts/charge")
					.contentType(MediaType.APPLICATION_JSON)
					.content(mapper.writeValueAsString(request))
			)
			.andExpectAll(status().is4xxClientError());
	}
}
