package org.c4marathon.assignment.member.controller;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Objects;

import org.c4marathon.assignment.auth.config.SecurityConfig;
import org.c4marathon.assignment.member.dto.request.JoinRequestDto;
import org.c4marathon.assignment.member.dto.request.LoginRequestDto;
import org.c4marathon.assignment.member.dto.response.LoginResponseDto;
import org.c4marathon.assignment.member.service.MemberService;
import org.c4marathon.assignment.util.exceptions.BaseException;
import org.c4marathon.assignment.util.exceptions.ErrorCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(MemberController.class)
@Import(SecurityConfig.class)
@TestInstance(value = PER_CLASS)
@ActiveProfiles("test")
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String REQUEST_URL = "/members";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeAll
    public void setUp() {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();
    }

    @Nested
    @DisplayName("회원 가입 테스트")
    class Join {

        private JoinRequestDto createJoinRequestDto() {
            return new JoinRequestDto("test@naver.com", "test", "test");
        }

        @DisplayName("회원 정보 입력 후 요청 시 회원 가입에 성공한다.")
        @Test
        void joinTest() throws Exception {

            // given
            JoinRequestDto joinRequestDto = createJoinRequestDto();

            // when
            doNothing().when(memberService).join(joinRequestDto);
            ResultActions resultActions = mockMvc.perform(post(REQUEST_URL + "/join")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(joinRequestDto)));

            // then
            resultActions.andExpect(status().isNoContent());
        }

        @DisplayName("회원 가입 요청 시 중복되는 이메일이 존재한다.")
        @Test
        void duplicateEmailTest() throws Exception {

            // given
            JoinRequestDto joinRequestDto = createJoinRequestDto();
            BaseException baseException = ErrorCode.DUPLICATED_EMAIL.baseException();

            // when
            doThrow(baseException).when(memberService).join(joinRequestDto);
            MvcResult mvcResult = mockMvc.perform(post(REQUEST_URL + "/join")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf())
                    .content(objectMapper.writeValueAsString(joinRequestDto)))
                .andExpect(status().isConflict())
                .andReturn();

            // then
            String message = Objects.requireNonNull(mvcResult.getResolvedException()).getMessage();
            int status = mvcResult.getResponse().getStatus();
            assertEquals(HttpStatus.CONFLICT.value(), status);
            assertThat(message).contains(ErrorCode.DUPLICATED_EMAIL.getMessage());
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class Login {

        private LoginRequestDto createLoginRequestDto() {
            return new LoginRequestDto("test@naver.com", "test");
        }

        @Test
        @DisplayName("유효한 회원 정보 입력 시 로그인에 성공한다.")
        void loginTest() throws Exception {

            // given
            LoginRequestDto loginRequestDto = createLoginRequestDto();
            given(memberService.login(loginRequestDto)).willReturn(ArgumentMatchers.any(LoginResponseDto.class));

            // when then
            mockMvc.perform(post(REQUEST_URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf())
                    .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andReturn();
        }

        @DisplayName("유효하지 않은 아이디 입력 시 로그인에 실패한다.")
        @Test
        void loginIdFailedTest() throws Exception {

            // given
            LoginRequestDto loginRequestDto = createLoginRequestDto();
            BaseException baseException = ErrorCode.COMMON_NOT_FOUND.baseException();
            given(memberService.login(loginRequestDto)).willThrow(baseException);

            // when
            MvcResult mvcResult = mockMvc.perform(post(REQUEST_URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf())
                    .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isNotFound())
                .andReturn();

            // then
            String message = Objects.requireNonNull(mvcResult.getResolvedException()).getMessage();
            assertThat(message).contains(ErrorCode.COMMON_NOT_FOUND.getMessage());
        }

        @DisplayName("유효하지 않은 비밀번호 입력 시 로그인에 실패한다.")
        @Test
        void loginPasswordFailedTest() throws Exception {

            // given
            LoginRequestDto loginRequestDto = createLoginRequestDto();
            BaseException baseException = ErrorCode.LOGIN_FAILED.baseException();
            given(memberService.login(loginRequestDto)).willThrow(baseException);

            // when
            MvcResult mvcResult = mockMvc.perform(post(REQUEST_URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(csrf())
                    .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isUnauthorized())
                .andReturn();

            // then
            String message = Objects.requireNonNull(mvcResult.getResolvedException()).getMessage();
            assertThat(message).contains(ErrorCode.LOGIN_FAILED.getMessage());
        }
    }
}
