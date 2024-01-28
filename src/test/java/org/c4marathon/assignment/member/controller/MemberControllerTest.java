package org.c4marathon.assignment.member.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.c4marathon.assignment.member.dto.RequestDto;
import org.c4marathon.assignment.member.dto.ResponseDto;
import org.c4marathon.assignment.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Test
    @DisplayName("회원 정보 입력 후 요청 시 회원 가입에 성공한다.")
    public void joinTest() throws Exception {
        // 필요한 필드를 설정하세요.
        RequestDto.JoinDto joinDto = new RequestDto.JoinDto("test@naver.com", "test", "test");

        doNothing().when(memberService).join(ArgumentMatchers.any(RequestDto.JoinDto.class));

        mockMvc.perform(post("/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(joinDto)))
            .andExpect(status().isNoContent());

        verify(memberService, times(1)).join(ArgumentMatchers.any(RequestDto.JoinDto.class));
    }

    @Test
    @DisplayName("회원 정보 입력 후 요청 시 로그인에 성공한다.")
    public void loginTest() throws Exception {
        RequestDto.LoginDto loginDto = new RequestDto.LoginDto("test@naver.com", "test");

        when(memberService.login(ArgumentMatchers.any(RequestDto.LoginDto.class))).thenReturn(ArgumentMatchers.any(
            ResponseDto.LoginDto.class));

        mockMvc.perform(post("/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(loginDto)))
            .andExpect(status().isOk());

        verify(memberService, times(1)).login(ArgumentMatchers.any(RequestDto.LoginDto.class));
    }
}
