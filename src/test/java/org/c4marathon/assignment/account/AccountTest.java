package org.c4marathon.assignment.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.c4marathon.assignment.account.dto.ChargeDto;
import org.c4marathon.assignment.account.dto.SendDto;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Random;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountTest {
    @MockBean
    private AccountRepository accountRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    @BeforeEach
    void start() {
        accountRepository.deleteAll();
    }

    @AfterEach
    void end() {
        accountRepository.deleteAll();
    }

    @DisplayName("[메인 계좌 충전 테스트]")
    @Test
    void chargeTest() throws Exception {
        Long random = new Random().nextLong();
        // given
        ChargeDto chargeDto = new ChargeDto(random, 10000);

        // when, then
        mockMvc.perform(post("/account/charge")
                        .content(toJson(chargeDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @DisplayName("[적금 계좌 생성 테스트]")
    @Test
    void savingTest() throws Exception {

        // given
        Long userId = 1L;

        // when, then
        mockMvc.perform(post("/account/create/{userId}")
                        .content(toJson(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @DisplayName("[송금 테스트]")
    @Test
    void sendTest() throws Exception {

        // given
        Long userId = 1L;
        SendDto loginDto = new SendDto(11111111L, 5000, 1234);

        // when, then
        mockMvc.perform(post("/account/send/{userId}")
                        .content(toJson(userId))
                        .content(toJson(loginDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
