package org.c4marathon.assignment.account.presentation;

import org.c4marathon.assignment.ControllerTestSupport;
import org.c4marathon.assignment.account.domain.SavingProductType;
import org.c4marathon.assignment.account.dto.SavingAccountCreateRequest;
import org.c4marathon.assignment.global.session.SessionConst;
import org.c4marathon.assignment.global.session.SessionMemberInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SavingAccountControllerTest extends ControllerTestSupport {
    @BeforeEach
    void initSession() {
        session = new MockHttpSession();
        SessionMemberInfo memberInfo = new SessionMemberInfo(1L, "test@test.com", "3333");
        session.setAttribute(SessionConst.LOGIN_MEMBER, memberInfo);
    }

    @DisplayName("적금 계좌를 생성한다.")
    @Test
    void createSavingAccount() throws Exception {
        // given
        SavingAccountCreateRequest request = new SavingAccountCreateRequest(SavingProductType.FREE, 1L, 20000L);

        // when // then
        mockMvc.perform(
                        post("/api/saving-account")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .session(session)
                )
                .andExpect(status().isCreated());
    }
}