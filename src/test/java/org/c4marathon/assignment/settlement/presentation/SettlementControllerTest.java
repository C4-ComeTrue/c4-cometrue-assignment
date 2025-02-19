package org.c4marathon.assignment.settlement.presentation;

import static org.c4marathon.assignment.settlement.domain.SettlementType.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.c4marathon.assignment.ControllerTestSupport;
import org.c4marathon.assignment.global.session.SessionConst;
import org.c4marathon.assignment.global.session.SessionMemberInfo;
import org.c4marathon.assignment.global.util.AccountNumberUtil;
import org.c4marathon.assignment.settlement.dto.ReceivedSettlementResponse;
import org.c4marathon.assignment.settlement.dto.SettlementDetailInfo;
import org.c4marathon.assignment.settlement.dto.SettlementRequest;
import org.c4marathon.assignment.settlement.dto.SettlementResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;


class SettlementControllerTest extends ControllerTestSupport {

	@BeforeEach
	void initSession() {
		session = new MockHttpSession();
		SessionMemberInfo memberInfo = new SessionMemberInfo(1L, "test@test.com", "3333");
		session.setAttribute(SessionConst.LOGIN_MEMBER, memberInfo);
	}

	@Test
	@DisplayName("정산 요청 API가 정상적으로 호출된다")
	void settle() throws Exception {
		// given
		String accountNumber1 = generateAccountNumber();
		String accountNumber2 = generateAccountNumber();
		String accountNumber3 = generateAccountNumber();
		SettlementRequest request = new SettlementRequest(3, 30000,
			List.of(accountNumber1, accountNumber2, accountNumber3), EQUAL);

		// when & then
		mockMvc.perform(post("/api/settle")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.session(session)
			)
			.andExpect(status().isOk());
	}

	@DisplayName("내가 요청한 정산 리스트를 정상적으로 조회한다")
	@Test
	void getRequestedSettlements() throws Exception {
		// given
		String accountNumber1 = generateAccountNumber();
		String accountNumber2 = generateAccountNumber();
		String accountNumber3 = generateAccountNumber();
		List<SettlementResponse> responses = List.of(
			new SettlementResponse(1L, "3333", 30000, List.of(
				new SettlementDetailInfo(10L, accountNumber1, 10000),
				new SettlementDetailInfo(11L, accountNumber2, 10000),
				new SettlementDetailInfo(12L, accountNumber3, 10000)
			))
		);

		when(settlementService.getRequestedSettlements("3333")).thenReturn(responses);

		// when & then
		mockMvc.perform(
				get("/api/settlements/requested")
					.session(session)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.size()").value(1))
			.andExpect(jsonPath("$[0].settlementId").value(1L))
			.andExpect(jsonPath("$[0].requestAccountNumber").value("3333"))
			.andExpect(jsonPath("$[0].totalAmount").value(30000));
	}

	@DisplayName("내가 요청받은 정산 리스트를 정상적으로 조회한다.")
	@Test
	void getReceivedSettlements() throws Exception {
	    // given
		String requestAccountNumber = generateAccountNumber();
		String myAccountNumber = "3333";
		List<ReceivedSettlementResponse> responses = List.of(
			new ReceivedSettlementResponse(1L, requestAccountNumber, 30000, myAccountNumber, 10000)
		);

		when(settlementService.getReceivedSettlements(myAccountNumber)).thenReturn(responses);
	    // when // then
		mockMvc.perform(
				get("/api/settlements/received")
					.session(session)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.size()").value(1))
			.andExpect(jsonPath("$[0].settlementId").value(1L))
			.andExpect(jsonPath("$[0].requestAccountNumber").value(requestAccountNumber))
			.andExpect(jsonPath("$[0].totalAmount").value(30000))
			.andExpect(jsonPath("$[0].myAccountNumber").value(myAccountNumber))
			.andExpect(jsonPath("$[0].mySettlementAmount").value(10000));
	}

	private String generateAccountNumber() {
		return AccountNumberUtil.generateAccountNumber("3333");
	}
}