package org.c4marathon.assignment.domain.user.service;

import org.assertj.core.api.Assertions;
import org.c4marathon.assignment.domain.account.entity.Account;
import org.c4marathon.assignment.domain.account.repository.AccountRepository;
import org.c4marathon.assignment.domain.user.dto.SignUpDto;
import org.c4marathon.assignment.domain.user.entity.User;
import org.c4marathon.assignment.domain.user.repository.UserRepository;
import org.c4marathon.assignment.global.exception.CustomException;
import org.c4marathon.assignment.global.exception.ErrorCode;
import org.c4marathon.assignment.global.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @InjectMocks private UserService userService;

    @Test
    @DisplayName("회원 가입에 성공")
    void createUser(){
        // given
        String email = "test@naver.com";
        String password = "password";
        User user = new User(email, BCrypt.hashpw(password, BCrypt.gensalt()));
        given(userRepository.save(any(User.class))).willReturn(user);

        Account account = spy(new Account(user));
        doReturn(1L).when(account).getId();
        given(accountRepository.save(any(Account.class))).willReturn(account);

        given(jwtTokenProvider.createAccessToken(email, "ROLE_USER")).willReturn("testToken");

        // when
        SignUpDto.Res res = userService.signUp(email, password);

        // then
        Assertions.assertThat(res.accessToken()).isEqualTo("testToken");
    }

    @Test
    @DisplayName("중복 회원 생성시 예외 발생")
    void createUserDuplicatedEmail() {
        // given
        String email = "test@naver.com";
        String password = "password";
        User user = new User(email, BCrypt.hashpw(password, BCrypt.gensalt()));

        // when
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // then
        CustomException exception = assertThrows(CustomException.class, () -> {
            userService.signUp(email, password);
        });

        assertEquals(ErrorCode.DUPLICATED_EMAIL, exception.getErrorCode());
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인에 성공")
    void signInTest(){
        // given
        String email = "test@naver.com";
        String password = "password";
        User user = new User(email, BCrypt.hashpw(password, BCrypt.gensalt()));
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        given(jwtTokenProvider.createAccessToken(email, "ROLE_USER")).willReturn("testToken");

        // when
        SignUpDto.Res res = userService.signIn(email, password);

        // then
        Assertions.assertThat(res.accessToken()).isEqualTo("testToken");
    }

}