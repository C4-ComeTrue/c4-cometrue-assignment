package org.c4marathon.assignment.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.c4marathon.assignment.domain.user.dto.SignUpDto;
import org.c4marathon.assignment.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean UserService userService;

    @Test
    @DisplayName("회원가입에 성공")
    void signUpSuccess() throws Exception {
        // given
        String email = "test@naver.com";
        String password = "password";

        SignUpDto.Req req = new SignUpDto.Req(email, password);
        SignUpDto.Res res = new SignUpDto.Res("accessToken");

        given(userService.signUp(email, password)).willReturn(res);

        // when + then
        mockMvc.perform(
                MockMvcRequestBuilders.post("/sign-up")
                        .content(new ObjectMapper().writeValueAsString(req))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isCreated(),
                jsonPath("$.accessToken").value("accessToken")
        );
    }

    @Test
    @DisplayName("이메일이 형식이 잘못된 경우 회원 가입 실패")
    void signUpInvalidEmail() throws Exception {
        // given
        String invalidEmail = "testnavercom";
        String password = "password";

        SignUpDto.Req req = new SignUpDto.Req(invalidEmail,password);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/sign-up")
                        .content(new ObjectMapper().writeValueAsString(req))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().is4xxClientError()
        );
    }

    @Test
    @DisplayName("로그인에 성공")
    void signInSuccess() throws Exception {
        // given
        String email = "test@naver.com";
        String password = "password";

        SignUpDto.Req req = new SignUpDto.Req(email, password);
        SignUpDto.Res res = new SignUpDto.Res("accessToken");

        given(userService.signIn(email, password)).willReturn(res);

        // when + then
        mockMvc.perform(
                MockMvcRequestBuilders.post("/sign-in")
                        .content(new ObjectMapper().writeValueAsString(req))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.accessToken").value("accessToken")
        );
    }

}