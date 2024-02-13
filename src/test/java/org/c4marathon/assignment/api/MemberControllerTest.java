package org.c4marathon.assignment.api;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.c4marathon.assignment.api.dto.MemberSignUpDto;
import org.c4marathon.assignment.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	MemberService memberService;

	ObjectMapper mapper = new ObjectMapper();

	@Test
	void 회원가입을_한다() throws Exception {
		// given
		var memberId = 1L;
		var accountId = 1L;
		var email = "email@naver.com";
		var password = "XX@@";
		var result = new MemberSignUpDto.Res(memberId, accountId);
		var request = new MemberSignUpDto.Req(email, password);

		given(memberService.register(anyString(), anyString())).willReturn(result);

		// when + then
		mockMvc.perform(
			post("/v1/members/sign-up")
				.content(mapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON)
		).andExpectAll(
			status().isCreated(),
			jsonPath("$.memberId").value(memberId),
			jsonPath("$.accountId").value(accountId)
		);
	}

	@Test
	void 잘못된_이메일_형식이면_회원가입에_실패한다() throws Exception {
		// given
		var email = "XX@@naver.co";
		var password = "XX@@";
		var request = new MemberSignUpDto.Req(email, password);

		// when + then
		mockMvc.perform(
			post("/v1/members/sign-up")
				.content(mapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON)
		).andExpectAll(
			status().is4xxClientError()
		);
	}

	@Test
	void 비밀번호가_비어있다면_회원가입에_실패한다() throws Exception {
		// given
		var email = "email";
		var password = " ";
		var request = new MemberSignUpDto.Req(email, password);

		// when + then
		mockMvc.perform(
			post("/v1/members/sign-up")
				.content(mapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON)
		).andExpectAll(
			status().is4xxClientError()
		);
	}
}
