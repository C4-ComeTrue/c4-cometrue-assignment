package org.c4marathon.assignment.account.controller;

import static org.junit.jupiter.api.TestInstance.Lifecycle.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.c4marathon.assignment.account.dto.request.AccountRequestDto;
import org.c4marathon.assignment.account.dto.response.SavingAccountResponseDto;
import org.c4marathon.assignment.account.service.SavingAccountService;
import org.c4marathon.assignment.auth.config.SecurityConfig;
import org.c4marathon.assignment.auth.jwt.JwtTokenUtil;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(SavingAccountController.class)
@Import(SecurityConfig.class)
@TestInstance(value = PER_CLASS)
@ActiveProfiles("test")
public class SavingAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SavingAccountService savingAccountService;

    private final String REQUEST_URL = "/accounts/savings";

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @Value("${jwt.key}")
    private String secretKey;

    @Value("${jwt.max-age}")
    private Long expireTimeMs;

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

        @DisplayName("적금 계좌 생성 요청에 따라 적금 계좌를 생성한다.")
        @Test
        void createSavingAccountTest() throws Exception {
            // given
            AccountRequestDto accountRequestDto = mock(AccountRequestDto.class);
            willDoNothing().given(savingAccountService).saveSavingAccount(accountRequestDto);

            // when
            savingAccountService.saveSavingAccount(accountRequestDto);

            // then
            mockMvc.perform(post(REQUEST_URL).header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountRequestDto))).andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("적금 계좌 조회 테스트")
    class Read {

        @DisplayName("Authorization 헤더로 전달 받은 토큰을 통해 회원의 계좌를 조회한다.")
        @Test
        void findAccountTest() throws Exception {
            // given
            SavingAccountResponseDto savingAccountResponseDto = mock(SavingAccountResponseDto.class);
            List<SavingAccountResponseDto> savingAccountResponseDtoList = List.of(savingAccountResponseDto);

            willReturn(savingAccountResponseDtoList).given(savingAccountService).findSavingAccount();

            // when then
            mockMvc.perform(get(REQUEST_URL).header("Authorization", token)).andExpect(status().isOk()).andReturn();
        }
    }
}
