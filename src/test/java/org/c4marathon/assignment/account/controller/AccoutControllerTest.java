package org.c4marathon.assignment.account.controller;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Objects;

import org.c4marathon.assignment.account.dto.request.AccountRequestDto;
import org.c4marathon.assignment.account.dto.request.RechargeAccountRequestDto;
import org.c4marathon.assignment.account.dto.request.SavingAccountRequestDto;
import org.c4marathon.assignment.account.dto.response.AccountResponseDto;
import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.entity.Type;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.account.service.AccountService;
import org.c4marathon.assignment.auth.config.SecurityConfig;
import org.c4marathon.assignment.auth.jwt.JwtTokenUtil;
import org.c4marathon.assignment.auth.service.SecurityService;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.service.MemberService;
import org.c4marathon.assignment.util.exceptions.BaseException;
import org.c4marathon.assignment.util.exceptions.ErrorCode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
@TestInstance(value = PER_CLASS)
@ActiveProfiles("test")
public class AccoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private MemberService memberService;

    @MockBean
    private SecurityService securityService;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @Value("${jwt.key}")
    private String secretKey;

    @Value("${jwt.max-age}")
    private Long expireTimeMs;

    private final String REQUEST_URL = "/accounts";

    @Autowired
    private WebApplicationContext webApplicationContext;

    // static 메서드 테스트
    private static MockedStatic<JwtTokenUtil> mockedStatic;

    // 토큰 생성
    private String createToken() {
        return JwtTokenUtil.createToken(0L, secretKey, expireTimeMs);
    }

    @BeforeAll
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();

        token = createToken();
        mockedStatic = mockStatic(JwtTokenUtil.class);
    }

    @AfterAll
    void afterAll() {
        mockedStatic.close();
    }

    @Nested
    @DisplayName("계좌 생성 테스트")
    class Create {

        @DisplayName("계좌 생성 요청이 들어오면 요청에 따른 타입의 계좌를 생성한다.")
        @Test
        void createAccountTest() throws Exception {

            // given
            Member member = Member.builder()
                .email("test@naver.com")
                .password("test")
                .name("test")
                .build();
            Account account = Account.builder()
                .type(Type.ADDITIONAL_ACCOUNT)
                .member(member)
                .build();
            AccountRequestDto accountRequestDto = new AccountRequestDto(Type.ADDITIONAL_ACCOUNT);
            willDoNothing().given(accountService).saveAccount(accountRequestDto);
            willReturn(member).given(memberService).getMemberById(member.getId());
            willReturn(account).given(accountService).createAccount(Type.ADDITIONAL_ACCOUNT, member);
            willReturn(true).given(accountService).isMainAccount(0L);

            // when
            ResultActions resultActions = mockMvc.perform(post(REQUEST_URL).header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(accountRequestDto)));

            // then
            resultActions.andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("계좌 조회 테스트")
    class Read {

        @DisplayName("Authorization 헤더로 전달 받은 토큰을 통해 회원의 계좌를 조회한다.")
        @Test
        void findAccountTest() throws Exception {
            // given
            Member member = Member.builder()
                .email("test@naver.com")
                .password("test")
                .name("test")
                .build();
            Account account = Account.builder()
                .type(Type.REGULAR_ACCOUNT)
                .member(member)
                .build();
            List<Account> accountList = List.of(account);
            List<AccountResponseDto> accountResponseDtoList = accountList.stream()
                .map(AccountResponseDto::entityToDto)
                .toList();
            willReturn(0L).given(securityService).findMember();
            willReturn(accountResponseDtoList).given(accountService).findAccount();
            willReturn(accountList).given(accountRepository).findByMemberId(0L);

            // when then
            mockMvc.perform(get(REQUEST_URL).header("Authorization", token)).andExpect(status().isOk()).andReturn();
        }
    }

    @Nested
    @DisplayName("계좌 충전 테스트")
    class Recharge {

        @DisplayName("메인 계좌에 잔액 충전 요청 시 설정한 금액만큼 잔액이 충전된다.")
        @Test
        void rechargeAccountTest() throws Exception {

            // given
            RechargeAccountRequestDto rechargeAccountRequestDto = new RechargeAccountRequestDto(1L, 10000L);
            willDoNothing().given(accountService).rechargeAccount(rechargeAccountRequestDto);

            // when then
            mockMvc.perform(post(REQUEST_URL + "/recharge").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rechargeAccountRequestDto))).andExpect(status().isNoContent());
        }

        @DisplayName("메인 계좌에 잔액 충전 요청 시 충전 한도가 넘어 오류가 발생한다.")
        @Test
        void rechargeAccountErrorTest() throws Exception {

            // given
            RechargeAccountRequestDto rechargeAccountRequestDto = new RechargeAccountRequestDto(1L, 10000L);
            BaseException baseException = ErrorCode.EXCEEDED_DAILY_LIMIT.baseException("하루 충전 한도를 초과하였습니다.");
            willThrow(baseException).given(accountService).rechargeAccount(rechargeAccountRequestDto);

            // when
            MvcResult mvcResult = mockMvc.perform(post(REQUEST_URL + "/recharge").header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(rechargeAccountRequestDto)))
                .andExpect(status().isBadRequest())
                .andReturn();

            // then
            String message = Objects.requireNonNull(mvcResult.getResolvedException()).getMessage();
            assertThat(message).contains("하루 충전 한도를 초과하였습니다.");
        }

        @DisplayName("메인 계좌에서 적금 계좌로 이체 요청 시 입력한 금액만큼 충전된다.")
        @Test
        void transferFromRegularAccountTest() throws Exception {

            // given
            SavingAccountRequestDto savingAccountRequestDto = new SavingAccountRequestDto(10000L, 2L);
            willDoNothing().given(accountService).transferFromRegularAccount(savingAccountRequestDto);

            // when then
            mockMvc.perform(post(REQUEST_URL + "/saving").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(savingAccountRequestDto))).andExpect(status().isNoContent());
        }

        @DisplayName("메인 계좌에서 적금 계좌로 입금 시 잔액 부족하다면 오류가 발생한다.")
        @Test
        void transferFromRegularAccountErrorTest() throws Exception {

            // given
            SavingAccountRequestDto savingAccountRequestDto = new SavingAccountRequestDto(50000L, 2L);
            BaseException baseException = ErrorCode.INSUFFICIENT_BALANCE.baseException("잔액이 부족합니다.");
            willThrow(baseException).given(accountService).transferFromRegularAccount(savingAccountRequestDto);

            // when
            MvcResult mvcResult = mockMvc.perform(post(REQUEST_URL + "/saving").header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(savingAccountRequestDto)))
                .andExpect(status().isForbidden())
                .andReturn();

            // then
            String message = Objects.requireNonNull(mvcResult.getResolvedException()).getMessage();
            assertThat(message).contains("잔액이 부족합니다.");
        }
    }
}
