package org.c4marathon.assignment.domain.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.c4marathon.assignment.domain.account.dto.AccountResponseDto;
import org.c4marathon.assignment.domain.account.dto.DepositDto;
import org.c4marathon.assignment.domain.account.dto.TransferDto;
import org.c4marathon.assignment.domain.account.service.AccountService;
import org.c4marathon.assignment.global.exception.ErrorCode;
import org.c4marathon.assignment.global.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import java.util.ArrayList;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(value = PER_CLASS)
class AccountControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean AccountService accountService;

    @Autowired JwtTokenProvider tokenProvider;

    String token;
    String email;

    @BeforeEach
    void setUp() {
        email = "test@naver.com";
        token = tokenProvider.createAccessToken(email, "USER_ROLE");
    }

    @Test
    @DisplayName("올바른 jwt로 접근 시 일반 계좌 생성")
    void createAccountSuccess() throws Exception {
        // given
        willDoNothing().given(accountService).createAccount(anyString());

        // when + then
        mockMvc.perform(
                post("/accounts/checking")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated());
        verify(accountService).createAccount(email);
    }

    @Test
    @DisplayName("올바르지 않은 jwt로 접근 시 계좌 생성 실패")
    void creatAccountInvalidJwt() throws Exception{
        // given
        willDoNothing().given(accountService).createAccount(anyString());

        // when + then
        mockMvc.perform(
                post("/accounts/checking")
                        .header("Authorization", "Bearer invalidToken")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().is4xxClientError()
        );
    }

    @Test
    @DisplayName("올바른 jwt로 접근 시 적금 계좌 생성")
    void createSavingAccountSuccess() throws Exception {
        // given
        willDoNothing().given(accountService).createSavingAccount(anyString());

        // when + then
        mockMvc.perform(
                post("/accounts/saving")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated());
        verify(accountService).createSavingAccount(email);
    }

    @Test
    @DisplayName("적금 계좌 조회 성공")
    void getAllSavingAccountSuccess() throws Exception {
        // given
        ArrayList<AccountResponseDto> list = new ArrayList<>();
        list.add(new AccountResponseDto(1L, 1000L, 0.1));
        given(accountService.getSavingAccounts(anyString())).willReturn(list);

        // when + then
        mockMvc.perform(
                get("/accounts/saving")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId").value(1L))
                .andExpect(jsonPath("$[0].balance").value(1000L))
                .andExpect(jsonPath("$[0].interestRate").value(0.1));
    }

    @Test
    @DisplayName("메인 계좌에 충전")
    void depositToMainAccountSuccess() throws Exception{
        // given
        DepositDto.Req req = new DepositDto.Req(1000L);
        DepositDto.Res res = new DepositDto.Res(2000L);
        given(accountService.depositToMainAccount(anyString(), eq(req.amount()))).willReturn(res);

        // when + then
        mockMvc.perform(
                post("/accounts/deposit")
                        .header("Authorization", "Bearer " + token)
                        .content(new ObjectMapper().writeValueAsString(req))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(2000L));
    }

    @Test
    @DisplayName("음수 값을 보냈을 때 메인 계좌에 충전 실패")
    void depositToMainAccountNegativeValue() throws Exception{
        // given
        DepositDto.Req req = new DepositDto.Req(-1000L);
        DepositDto.Res res = new DepositDto.Res(2000L);
        given(accountService.depositToMainAccount(anyString(), anyLong())).willReturn(res);

        // when + then
        mockMvc.perform(
                        post("/accounts/deposit")
                                .header("Authorization", "Bearer " + token)
                                .content(new ObjectMapper().writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REQUEST_CONTENT.name()));
    }

    @Test
    @DisplayName("적금 계좌에 이체 성공")
    void transferToSavingsSuccess() throws Exception{
        // given
        TransferDto.Req req = new TransferDto.Req(1L, 1000L);
        TransferDto.Res res = new TransferDto.Res(1000L);
        given(accountService.transferToSavings(anyString(), anyLong(), anyLong())).willReturn(res);

        // when + then
        mockMvc.perform(
                post("/accounts/transfer")
                        .header("Authorization", "Bearer " + token)
                        .content(new ObjectMapper().writeValueAsString(req))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000L));
    }

    @Test
    @DisplayName("이체할 값으로 0을 보냈을 때 이체 실패")
    void transferToSavingsZeroValue() throws Exception{
        TransferDto.Req req = new TransferDto.Req(1L, 0L);
        TransferDto.Res res = new TransferDto.Res(1000L);
        given(accountService.transferToSavings(anyString(), anyLong(), anyLong())).willReturn(res);

        // when + then
        mockMvc.perform(
                        post("/accounts/transfer")
                                .header("Authorization", "Bearer " + token)
                                .content(new ObjectMapper().writeValueAsString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REQUEST_CONTENT.name()));
    }
}