package org.c4marathon.assignment.account.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.c4marathon.assignment.account.dto.RequestDto;
import org.c4marathon.assignment.account.entity.Type;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.auth.jwt.JwtTokenUtil;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AccoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Value("${jwt.key}")
    private String secretKey;

    @Value("${jwt.max-age}")
    private Long expireTimeMs;

    @BeforeAll
    static void setup(@Autowired DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/sql/CreateData.sql"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void tearDown(@Autowired DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/sql/RemoveData.sql"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // 토큰 생성
    private String createToken() {
        return JwtTokenUtil.createToken("test@naver.com", secretKey, expireTimeMs);
    }

    @DisplayName("계좌 생성 테스트")
    @Test
    public void createAccountTest() throws Exception {

        String token = createToken();
        RequestDto.AccountDto accountDto = new RequestDto.AccountDto(Type.ADDITIONAL_ACCOUNT);

        mockMvc.perform(post("/accounts")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(accountDto)))
            .andExpect(status().isNoContent());
    }

    @DisplayName("계좌 조회 테스트")
    @Test
    public void findAccountTest() throws Exception {
        String token = createToken();

        mockMvc.perform(get("/accounts")
                .header("Authorization", token))
            .andExpect(status().isOk());
    }

    @DisplayName("계좌 충전 테스트")
    @Test
    public void rechargeAccountTest() throws Exception {

        String token = createToken();
        RequestDto.RechargeAccountDto rechargeAccountDto = new RequestDto.RechargeAccountDto(1L, 10000);

        mockMvc.perform(post("/accounts/recharge")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(rechargeAccountDto)))
            .andExpect(status().isNoContent());
    }

    @DisplayName("메인 계좌에서 적금 계좌로 이체 테스트")
    @Test
    public void transferFromRegularAccountTest() throws Exception {
        String token = createToken();
        RequestDto.SavingAccountDto savingAccountDto = new RequestDto.SavingAccountDto(10000, 2L);

        mockMvc.perform(post("/accounts/saving")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(savingAccountDto)))
            .andExpect(status().isNoContent());
    }
}
