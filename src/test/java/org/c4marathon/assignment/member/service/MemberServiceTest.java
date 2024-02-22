package org.c4marathon.assignment.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.auth.jwt.JwtTokenUtil;
import org.c4marathon.assignment.member.dto.request.JoinRequestDto;
import org.c4marathon.assignment.member.dto.request.LoginRequestDto;
import org.c4marathon.assignment.member.dto.response.LoginResponseDto;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.c4marathon.assignment.util.event.MemberJoinedEvent;
import org.c4marathon.assignment.util.exceptions.BaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Nested
    @DisplayName("회원 가입 테스트")
    class signUp {
        @DisplayName("회원 가입 정보를 받아 새로운 회원을 생성한뒤 계좌 생성을 위해 이벤트 리스너에 이벤트를 등록한다.")
        @Test
        void signUpTest() {

            // given
            String email = "test@naver.com";
            String password = "test";
            String name = "test";

            JoinRequestDto joinRequestDto = new JoinRequestDto(email, password, name);

            Member member = Member.builder()
                .email(joinRequestDto.email())
                .password(passwordEncoder.encode(joinRequestDto.password()))
                .name(joinRequestDto.name())
                .build();

            given(memberService.checkEmailExist(joinRequestDto.email())).willReturn(false);
            given(passwordEncoder.encode(joinRequestDto.password())).willReturn(password);
            given(memberRepository.save(any(Member.class))).willReturn(member);

            // when
            memberService.join(joinRequestDto);

            // then
            verify(passwordEncoder, times(2)).encode(password);
            verify(memberRepository).save(any(Member.class));
            verify(eventPublisher).publishEvent(any(MemberJoinedEvent.class));

            assertThat(member.getEmail()).isEqualTo(joinRequestDto.email());
        }

        @DisplayName("이미 존재하는 이메일로 회원 가입을 시도하면 실패한다.")
        @Test
        void signUpWithExistingEmailTest() {

            // given
            String email = "test@naver.com";
            String password = "test";
            String name = "test";

            JoinRequestDto joinRequestDto = new JoinRequestDto(email, password, name);

            Member member = Member.builder()
                .email(joinRequestDto.email())
                .password(passwordEncoder.encode(joinRequestDto.password()))
                .name(joinRequestDto.name())
                .build();

            given(memberService.checkEmailExist(joinRequestDto.email())).willReturn(true);

            // when
            Exception exception = assertThrows(BaseException.class, () -> {
                memberService.join(joinRequestDto);
            });

            // then
            String expectedMessage = HttpStatus.CONFLICT.toString();
            String actualMessage = exception.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class Login {

        @DisplayName("유효한 회원 정보 입력 시 로그인에 성공한다")
        @Test
        void loginTest() {

            // given
            String email = "test@naver.com";
            String password = "test";
            String jwtToken = "test.jwt.token";
            Long expireTimeMs = 3600000L;  // 1 hour in milliseconds
            String secretKey = "testSecretKey";
            MockedStatic<JwtTokenUtil> mockedStatic = Mockito.mockStatic(JwtTokenUtil.class);

            ReflectionTestUtils.setField(memberService, "expireTimeMs", expireTimeMs);
            ReflectionTestUtils.setField(memberService, "secretKey", secretKey);

            LoginRequestDto loginRequestDto = new LoginRequestDto(email, password);

            Member member = mock(Member.class);

            given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));
            given(passwordEncoder.matches(password, member.getPassword())).willReturn(true);
            mockedStatic.when(() -> JwtTokenUtil.createToken(anyLong(), anyString(), anyLong())).thenReturn(jwtToken);

            // when
            LoginResponseDto loginResponseDto = memberService.login(loginRequestDto);

            // then
            assertEquals(jwtToken, loginResponseDto.token());
        }

        @DisplayName("유효하지 않은 아이디 입력 시 로그인에 실패한다.")
        @Test
        void loginIdFailedTest() {

            // given
            String email = "test@naver.com";
            String password = "test";

            LoginRequestDto loginRequestDto = new LoginRequestDto(email, password);

            given(memberRepository.findByEmail(email)).willReturn(Optional.empty());

            // when
            Exception exception = assertThrows(BaseException.class, () -> {
                memberService.login(loginRequestDto);
            });

            // then
            String expectedMessage = HttpStatus.NOT_FOUND.toString();
            String actualMessage = exception.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }

        @DisplayName("유효하지 않은 비밀번호 입력 시 로그인에 실패한다.")
        @Test
        void loginPasswordFailedTest() {

            // given
            String email = "test@naver.com";
            String password = "test";

            LoginRequestDto loginRequestDto = new LoginRequestDto(email, password);
            Member member = mock(Member.class);

            given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));
            given(passwordEncoder.matches(password, member.getPassword())).willReturn(false);

            // when
            Exception exception = assertThrows(BaseException.class, () -> {
                memberService.login(loginRequestDto);
            });

            // then
            String expectedMessage = HttpStatus.UNAUTHORIZED.toString();
            String actualMessage = exception.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Nested
    @DisplayName("회원 정보 조회 테스트")
    class findMember {

        @DisplayName("PK로 회원 정보 찾는다")
        @Test
        void getMemberByIdTest() {
            // given
            Long memberId = 0L;
            Member member = mock(Member.class);
            given(memberRepository.findById(memberId)).willReturn(Optional.ofNullable(member));

            // when
            Member member1 = memberService.getMemberById(memberId);

            // then
            assertEquals(member, member1);
        }

        @DisplayName("회원의 아이디가 존재하는지 확인한다.")
        @Test
        void checkEmailExistTest() {
            // given
            String email = "test@naver.com";
            given(memberRepository.existsByEmail(email)).willReturn(true);

            // when
            boolean result = memberService.checkEmailExist(email);

            // then
            assertTrue(result);
        }
    }
}
