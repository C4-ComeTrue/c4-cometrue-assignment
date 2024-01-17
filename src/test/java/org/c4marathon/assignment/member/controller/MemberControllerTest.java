package org.c4marathon.assignment.member.controller;

import java.nio.charset.StandardCharsets;

import org.c4marathon.assignment.member.dto.request.SignUpRequestDto;
import org.c4marathon.assignment.member.exception.MemberErrorCode;
import org.c4marathon.assignment.member.exception.MemberException;
import org.c4marathon.assignment.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 모든 빈을 로드하지 않고 컨트롤러 계층만 테스트하기 위해 @WebMvcTest를 사용했습니다.
 */
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
			doNothing().when(memberService)
				.signUp(createRequestDto("seungh1024", "testPass", "seungh", "01012345678"));

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
}
