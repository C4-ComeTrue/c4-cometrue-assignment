package org.c4marathon.assignment.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @InjectMocks
    private SecurityService securityService;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @DisplayName("SecurityContextHolder에 등록되어 있는 회원의 정보를 찾아온다.")
    @Test
    void findMemberTest() {
        // given
        long memberId = 0L;
        given(authentication.getPrincipal()).willReturn(memberId);
        // when
        when(securityService.findMember()).thenReturn(memberId);
        long resultMemberId = securityService.findMember();
        // then
        assertEquals(memberId, resultMemberId);
    }
}
