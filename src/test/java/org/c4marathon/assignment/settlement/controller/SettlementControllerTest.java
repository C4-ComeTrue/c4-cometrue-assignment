package org.c4marathon.assignment.settlement.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.c4marathon.assignment.member.session.SessionConst;
import org.c4marathon.assignment.member.session.SessionMemberInfo;
import org.c4marathon.assignment.settlement.document.MemberInfoDocument;
import org.c4marathon.assignment.settlement.dto.request.DivideMoneyRequestDto;
import org.c4marathon.assignment.settlement.dto.request.MemberInfo;
import org.c4marathon.assignment.settlement.dto.response.SettlementInfoResponseDto;
import org.c4marathon.assignment.settlement.service.SettlementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SettlementController.class)
class SettlementControllerTest {

	private final String REQUEST_URL = "/api/settle";

	@Autowired
	MockMvc mockMvc;

	@MockBean
	SettlementService settlementService;

	@Autowired
	ObjectMapper objectMapper;

	MockHttpSession session;

	int totalNumber;
	long totalMoney;
	List<MemberInfo> memberInfoList;

	@BeforeEach
	void initSession() {
		session = new MockHttpSession();
		SessionMemberInfo memberInfo = new SessionMemberInfo(1L, "testId", "testName", 1L);
		session.setAttribute(SessionConst.MEMBER_INFO, memberInfo);

		totalNumber = 3;
		totalMoney = 100000;
		memberInfoList = new ArrayList<>(
			List.of(new MemberInfo(1, "user1"), new MemberInfo(2, "user2"), new MemberInfo(3, "user3")));
	}

	@Nested
	@DisplayName("정산 요청 테스트")
	class MakeSettlement {

		@Test
		@DisplayName("정산 인원이 1명 이상 50명 이하가 아니면 정산 요청이 실패한다.")
		void request_with_not_valid_totalNumber() throws Exception {
			// Given
			DivideMoneyRequestDto requestDto = new DivideMoneyRequestDto(0, totalMoney, true, memberInfoList);

			// When
			ResultActions resultActions = mockMvc.perform(post(REQUEST_URL + "/divide").session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(requestDto)));

			// Then
			resultActions.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("정산 금액이 양수가 아니면 정산 요청이 실패한다..")
		void request_with_not_valid_totalMoney() throws Exception {
			// Given
			DivideMoneyRequestDto requestDto = new DivideMoneyRequestDto(totalNumber, 0, true, memberInfoList);

			// When
			ResultActions resultActions = mockMvc.perform(post(REQUEST_URL + "/divide").session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(requestDto)));

			// Then
			resultActions.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("정산 방식을 입력하지 않으면 정산 요청이 실패한다.")
		void request_with_null_IsRandom() throws Exception {
			// Given
			DivideMoneyRequestDto requestDto = new DivideMoneyRequestDto(totalNumber, totalMoney, null, memberInfoList);

			// When
			ResultActions resultActions = mockMvc.perform(post(REQUEST_URL + "/divide").session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(requestDto)));

			// Then
			resultActions.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("정산할 사용자들 정보를 입력하지 않으면 정산 요청이 실패한다.")
		void request_with_null_memberInfoList() throws Exception {
			// Given
			DivideMoneyRequestDto requestDto = new DivideMoneyRequestDto(totalNumber, totalMoney, true, null);

			// When
			ResultActions resultActions = mockMvc.perform(post(REQUEST_URL + "/divide").session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(requestDto)));

			// Then
			resultActions.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("정산할 사용자들의 정보가 올바르지 않으면 정산 요청이 실패한다.")
		void request_with_not_valid_memberInfoList() throws Exception {
			// Given
			memberInfoList.add(new MemberInfo(0, "user0"));
			DivideMoneyRequestDto requestDto = new DivideMoneyRequestDto(totalNumber, totalMoney, true, memberInfoList);

			// When
			ResultActions resultActions = mockMvc.perform(post(REQUEST_URL + "/divide").session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(requestDto)));

			// Then
			resultActions.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("유효한 요청이면 정산 요청이 성공한다.")
		void request_with_valid_form() throws Exception {
			// Given
			DivideMoneyRequestDto requestDto = new DivideMoneyRequestDto(totalNumber, totalMoney, true, memberInfoList);

			// When
			ResultActions resultActions = mockMvc.perform(post(REQUEST_URL + "/divide").session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(requestDto)));

			// Then
			resultActions.andExpect(status().isOk());
		}
	}

	@Nested
	@DisplayName("정산 조회 테스트")
	class GetSettlementInfo {

		@Test
		@DisplayName("전체 정산 정보 조회 테스트")
		void get_settlement_info_list() throws Exception {
			List<SettlementInfoResponseDto> responseDto = new ArrayList<>(
				List.of(new SettlementInfoResponseDto(new ObjectId(), 1, "user1", 2, 10000,
					List.of(new MemberInfoDocument(2, "user2", 3000), new MemberInfoDocument(3, "user3", 7000)),
					LocalDateTime.now())));
			// Given
			given(settlementService.getSettlementInfoList(anyLong())).willReturn(responseDto);

			// When
			ResultActions resultActions = mockMvc.perform(get(REQUEST_URL + "/info/list").session(session));

			// Then
			String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
			resultActions.andExpectAll(status().isOk(), jsonPath("$[0].memberInfoList").isNotEmpty());
		}
	}

}
