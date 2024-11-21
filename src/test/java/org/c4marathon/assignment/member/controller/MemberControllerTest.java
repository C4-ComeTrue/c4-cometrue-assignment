package org.c4marathon.assignment.member.controller;

import java.nio.charset.StandardCharsets;

import org.c4marathon.assignment.common.exception.CommonErrorCode;
import org.c4marathon.assignment.member.dto.request.SignInRequestDto;
import org.c4marathon.assignment.member.dto.request.SignUpRequestDto;
import org.c4marathon.assignment.member.dto.response.MemberInfoResponseDto;
import org.c4marathon.assignment.member.exception.MemberErrorCode;
import org.c4marathon.assignment.member.exception.MemberException;
import org.c4marathon.assignment.member.service.MemberService;
import org.c4marathon.assignment.member.session.SessionConst;
import org.c4marathon.assignment.member.session.SessionMemberInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

	private final String REQUEST_URL = "/api/members";
	@Autowired
	MockMvc mockMvc;

	@MockBean
	private MemberService memberService;

	@Autowired
	private ObjectMapper objectMapper;

	@Nested
	@DisplayName("회원 가입 테스트")
	class SignUp {

		@Test
		@DisplayName("유효한 사용자 정보가 주어지면 정상적으로 회원 가입을 수행한다.")
		void request_with_a_valid_form() throws Exception {
			doNothing().when(memberService).signUp(createRequestDto("seungh1024", "testPass", "seungh", "01012345678"));

			mockMvc.perform(post(REQUEST_URL + "/signup")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(
						createRequestDto("seungh1024", "testPass", "seungh", "01012345678"))))
				.andExpect(status().isCreated());
		}

		@Test
		@DisplayName("이미 가입한 아이디로 요청을 보내면 회원 가입에 실패한다.")
		void request_with_a_duplicated_memberId() throws Exception {
			// Given
			SignUpRequestDto requestDto = createRequestDto("seungh1024", "testPass", "seungh", "01012345678");
			MemberException memberException = MemberErrorCode.USER_ALREADY_EXIST.memberException(
				"회원가입 도중 중복되는 사용자 에러 발생");
			doThrow(memberException).when(memberService).signUp(requestDto);

			// When
			MvcResult result = mockMvc.perform(post(REQUEST_URL + "/signup")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(
						createRequestDto("seungh1024", "testPass", "seungh", "01012345678"))))
				.andExpect(status().isConflict())
				.andReturn();

			// Then
			String message = result.getResolvedException().getMessage();
			int status = result.getResponse().getStatus();
			assertEquals(HttpStatus.CONFLICT.value(), status);
			assertThat(message).contains("해당 아이디는 이미 사용중입니다.");
		}

		@Test
		@DisplayName("memberId가 빈칸 이면 회원 가입에 실패한다.")
		void request_with_id_is_empty() throws Exception {
			// Given
			SignUpRequestDto requestDto = createRequestDto("", "testPass", "seungh", "01012345678");

			// When
			MvcResult result = mockMvc.perform(post(REQUEST_URL + "/signup")
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8)
					.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isBadRequest())
				.andReturn();

			// Then
			String message = result.getResolvedException().getMessage();
			int status = result.getResponse().getStatus();
			assertEquals(HttpStatus.BAD_REQUEST.value(), status);
			assertThat(message).contains("아이디는 공백일 수 없습니다.");
		}

		@Test
		@DisplayName("password가 빈칸 이면 회원 가입에 실패한다.")
		void request_with_password_is_empty() throws Exception {
			// Given
			SignUpRequestDto requestDto = createRequestDto("seungh1024", "", "seungh", "01012345678");

			// When
			MvcResult result = mockMvc.perform(post(REQUEST_URL + "/signup")
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8)
					.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isBadRequest())
				.andReturn();

			// Then
			String message = result.getResolvedException().getMessage();
			int status = result.getResponse().getStatus();
			assertEquals(HttpStatus.BAD_REQUEST.value(), status);
			assertThat(message).contains("비밀번호는 공백일 수 없습니다.");
		}

		@Test
		@DisplayName("password가 8자 이하면 회원 가입에 실패한다.")
		void request_with_password_length_is_short() throws Exception {
			// Given
			SignUpRequestDto requestDto = createRequestDto("seungh1024", "t", "seungh", "01012345678");

			// When
			MvcResult result = mockMvc.perform(post(REQUEST_URL + "/signup")
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8)
					.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isBadRequest())
				.andReturn();

			// Then
			String message = result.getResolvedException().getMessage();
			int status = result.getResponse().getStatus();
			assertEquals(HttpStatus.BAD_REQUEST.value(), status);
			assertThat(message).contains("비밀번호는 최소 8자 이상 20자 이하로 설정해야 합니다.");
		}

		@Test
		@DisplayName("name이 빈칸 이면 회원 가입에 실패한다.")
		void request_with_name_is_empty() throws Exception {
			// Given
			SignUpRequestDto requestDto = createRequestDto("seungh1024", "password", "", "01012345678");

			// When
			MvcResult result = mockMvc.perform(post(REQUEST_URL + "/signup")
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8)
					.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isBadRequest())
				.andReturn();

			// Then
			String message = result.getResolvedException().getMessage();
			int status = result.getResponse().getStatus();
			assertEquals(HttpStatus.BAD_REQUEST.value(), status);
			assertThat(message).contains("이름을 입력해주세요.");
		}

		@Test
		@DisplayName("전화번호 형식이 아니면 회원가입에 실패한다.")
		void request_with_phone_number_is_wrong() throws Exception {
			// Given
			SignUpRequestDto requestDto = createRequestDto("seungh1024", "password", "seungh1024", "010");

			// When
			MvcResult result = mockMvc.perform(post(REQUEST_URL + "/signup")
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8)
					.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isBadRequest())
				.andReturn();

			// Then
			String message = result.getResolvedException().getMessage();
			int status = result.getResponse().getStatus();
			assertEquals(HttpStatus.BAD_REQUEST.value(), status);
			assertThat(message).contains("올바른 전화번호 형식을 입력해주세요.");
		}

		@Test
		@DisplayName("전화번호가 11자리가 아니면 회원 가입에 실패한다.")
		void request_with_phone_number_length_is_short() throws Exception {
			// Given
			SignUpRequestDto requestDto = createRequestDto("seungh1024", "password", "seungh1024", "010");

			// When
			MvcResult result = mockMvc.perform(post(REQUEST_URL + "/signup")
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8)
					.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isBadRequest())
				.andReturn();

			// Then
			String message = result.getResolvedException().getMessage();
			int status = result.getResponse().getStatus();
			assertEquals(HttpStatus.BAD_REQUEST.value(), status);
			assertThat(message).contains("'-'를 제외한 전화번호 11자리를 입력해 주세요.");
		}

		private SignUpRequestDto createRequestDto(String memberId, String password, String userName,
			String phoneNumber) {
			return new SignUpRequestDto(memberId, password, userName, phoneNumber);
		}
	}

	@Nested
	@DisplayName("로그인 테스트")
	class SignIn {
		@Test
		@DisplayName("유효한 아이디와 비밀번호를 입력하면 로그인에 성공한다.")
		void request_with_valid_id_and_password() throws Exception {
			// Given
			SignInRequestDto requestDto = new SignInRequestDto("testId", "password");
			SessionMemberInfo memberDto = new SessionMemberInfo(1L, "testId", "testName", 0L);
			given(memberService.signIn(requestDto)).willReturn(memberDto);

			// When
			ResultActions resultActions = mockMvc.perform(
				post(REQUEST_URL + "/signin")
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8)
					.content(objectMapper.writeValueAsString(requestDto)));

			// Then
			resultActions
				.andExpect(status().isOk())
				.andExpect(request().sessionAttribute(SessionConst.MEMBER_INFO, memberDto));
		}

		@Test
		@DisplayName("아이디가 공백이면 로그인에 실패한다.")
		void request_with_empty_id() throws Exception {
			// Given
			SignInRequestDto requestDto = new SignInRequestDto("", "password");
			given(memberService.signIn(requestDto)).willReturn(null);

			// When
			ResultActions resultActions = mockMvc.perform(
				post(REQUEST_URL + "/signin")
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8)
					.content(objectMapper.writeValueAsString(requestDto)));

			// Then
			resultActions
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("비밀번호가 공백이면 로그인에 실패한다.")
		void request_with_empty_password() throws Exception {
			// Given
			SignInRequestDto requestDto = new SignInRequestDto("testId", "");
			given(memberService.signIn(requestDto)).willReturn(null);

			// When
			ResultActions resultActions = mockMvc.perform(
				post(REQUEST_URL + "/signin")
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(StandardCharsets.UTF_8)
					.content(objectMapper.writeValueAsString(requestDto)));

			// Then
			resultActions
				.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("사용자 정보 조회 테스트")
	class GetMyInfo {

		@Test
		@DisplayName("로그인한 사용자는 자신의 정보를 반환받는다.")
		void request_with_login_member() throws Exception {
			// Given
			SessionMemberInfo sessionMemberInfo = new SessionMemberInfo(1L, "testId", "testName", 0L);
			MockHttpSession session = new MockHttpSession();
			session.setAttribute(SessionConst.MEMBER_INFO, sessionMemberInfo);

			MemberInfoResponseDto memberInfo = new MemberInfoResponseDto(1L, "testId", "testName");
			given(memberService.getMemberInfo(sessionMemberInfo.memberPk()))
				.willReturn(memberInfo);

			// When
			ResultActions resultActions = mockMvc.perform(get(REQUEST_URL + "/info")
				.session(session));

			// Then
			resultActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.memberPk").value(memberInfo.memberPk()))
				.andExpect(jsonPath("$.memberId").value(memberInfo.memberId()))
				.andExpect(jsonPath("$.memberName").value(memberInfo.memberName()));
		}

		@Test
		@DisplayName("로그인하지 않은 사용자는 CommonException(UNAUTHORIZED_USER) 예외를 반환받는다.")
		void request_with_non_login_member() throws Exception {
			// Given
			MockHttpSession session = new MockHttpSession();
			session.setAttribute(SessionConst.MEMBER_INFO, null);

			// When
			ResultActions resultActions = mockMvc.perform(get(REQUEST_URL + "/info")
				.session(session));

			// Then
			resultActions
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value(CommonErrorCode.UNAUTHORIZED_USER.getMessage()));
		}
	}
}
