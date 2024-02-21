package org.c4marathon.assignment.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.*;
import static org.mockito.BDDMockito.*;

import org.c4marathon.assignment.account.service.AccountService;
import org.c4marathon.assignment.member.dto.request.JoinRequestDto;
import org.c4marathon.assignment.member.dto.request.LoginRequestDto;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.c4marathon.assignment.util.entity.Status;
import org.c4marathon.assignment.util.event.MemberJoinedEvent;
import org.c4marathon.assignment.util.exceptions.BaseException;
import org.c4marathon.assignment.util.exceptions.ErrorCode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
public class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private AccountService accountService;

    private Member createMember() {

        return Member.builder()
            .email("test@naver.com")
            .password(passwordEncoder.encode("test"))
            .name("test")
            .build();
    }

    private boolean checkEmailExist(String email) {

        return memberRepository.existsByEmail(email);
    }

    @Nested
    @DisplayName("회원 가입 테스트")
    class signUp {

        @AfterEach
        void tearDown() {

            memberRepository.deleteAllInBatch();
        }

        @DisplayName("회원 가입을 위해 요청한 데이터를 Entity 객체로 올바르게 변경한다.")
        @Test
        void dtoToEntityTest() {
            // given
            JoinRequestDto joinRequestDto = new JoinRequestDto("test@naver.com", "test", "test");

            // when
            Member member = Member.builder()
                .email(joinRequestDto.email())
                .password(joinRequestDto.password())
                .name(joinRequestDto.name())
                .build();

            // then
            assertEquals(member.getEmail(), joinRequestDto.email());
            assertEquals(member.getPassword(), joinRequestDto.password());
            assertEquals(member.getName(), joinRequestDto.name());
        }

        @DisplayName("회원 가입 정보를 받아 새로운 회원을 생성한다.")
        @Test
        @Transactional
        void signUpTest() {

            // given
            Member member = createMember();
            member.setStatus(Status.ACTIVE);

            // when
            if (checkEmailExist(member.getEmail())) {
                throw new BaseException(ErrorCode.DUPLICATED_EMAIL.toString(), HttpStatus.CONFLICT.toString());
            }
            memberRepository.save(member);
            Member findMember = memberRepository.findByEmail(member.getEmail()).orElseThrow(
                () -> new BaseException(ErrorCode.COMMON_NOT_FOUND.toString(), HttpStatus.NOT_FOUND.toString()));

            // then
            assertThat(findMember.getEmail()).isEqualTo(member.getEmail());
        }

        @DisplayName("회원 가입 정보를 받아 새로운 회원을 생성한뒤 계좌 생성을 위해 이벤트 리스너에 이벤트를 등록한다.")
        @Test
        void signUpWithEventListenerTest() {
            // given
            Member member = createMember();
            member.setStatus(Status.ACTIVE);

            // when
            if (checkEmailExist(member.getEmail())) {
                throw new BaseException(ErrorCode.DUPLICATED_EMAIL.toString(), HttpStatus.CONFLICT.toString());
            }
            memberRepository.save(member);
            Long memberId = member.getId();
            doNothing().when(accountService).saveMainAccount(memberId);

            eventPublisher.publishEvent(new MemberJoinedEvent(this, memberId));

            // then
            verify(accountService, times(1)).saveMainAccount(memberId);
        }

        @DisplayName("이미 존재하는 이메일로 회원 가입을 시도하면 실패한다.")
        @Test
        @Transactional
        void signUpWithExistingEmailTest() {

            // given
            Member member = createMember();
            memberRepository.save(member);

            // when
            Member member1 = createMember();

            // then
            assertThatThrownBy(() -> {
                if (checkEmailExist(member1.getEmail())) {
                    throw new BaseException(ErrorCode.DUPLICATED_EMAIL.toString(), HttpStatus.CONFLICT.toString());
                }
                memberRepository.save(member1);
            }).isInstanceOf(BaseException.class)
                .hasMessageContaining(HttpStatus.CONFLICT.toString());
        }
    }

    @Nested
    @TestInstance(value = PER_CLASS)
    @DisplayName("로그인 테스트")
    class Login {

        private Member member;

        @BeforeAll
        void setUp() {
            // given
            member = createMember();
            memberRepository.save(member);
        }

        @AfterAll
        void tearDown() {
            memberRepository.deleteAllInBatch();
        }

        @DisplayName("유효한 회원 정보 입력 시 로그인에 성공한다")
        @Test
        void loginTest() {

            // given
            LoginRequestDto loginRequestDto = new LoginRequestDto("test@naver.com", "test");

            // when
            Member member = memberRepository.findByEmail(loginRequestDto.email()).orElseThrow(
                () -> new BaseException(ErrorCode.COMMON_NOT_FOUND.toString(),
                    HttpStatus.NOT_FOUND.toString()));

            if (!passwordEncoder.matches(loginRequestDto.password(), member.getPassword())) {
                throw new BaseException(ErrorCode.LOGIN_FAILED.toString(), HttpStatus.UNAUTHORIZED.toString());
            }

            // then
            assertThat(loginRequestDto.email()).isEqualTo(member.getEmail());
            assertTrue(passwordEncoder.matches(loginRequestDto.password(), member.getPassword()));
        }

        @DisplayName("유효하지 않은 아이디 입력 시 로그인에 실패한다.")
        @Test
        void loginIdFailedTest() {

            // given
            String email = "test1@naver.com";

            // when then
            assertThrows(BaseException.class, () -> {
                memberRepository.findByEmail(email)
                    .orElseThrow(
                        () -> new BaseException(ErrorCode.COMMON_NOT_FOUND.toString(),
                            HttpStatus.NOT_FOUND.toString()));
            });
        }

        @DisplayName("유효하지 않은 비밀번호 입력 시 로그인에 실패한다.")
        @Test
        void loginPasswordFailedTest() {

            // given
            String email = "test@naver.com";
            String password = "test1";

            // when
            Member member = memberRepository.findByEmail(email)
                .orElseThrow(
                    () -> new BaseException(ErrorCode.COMMON_NOT_FOUND.toString(), HttpStatus.NOT_FOUND.toString()));

            Exception exception = assertThrows(BaseException.class, () -> {
                if (!passwordEncoder.matches(password, member.getPassword())) {
                    throw new BaseException(ErrorCode.LOGIN_FAILED.toString(),
                        HttpStatus.UNAUTHORIZED.toString());
                }
            });

            // then
            assertEquals(HttpStatus.UNAUTHORIZED.toString(), exception.getMessage());
        }
    }

    @Nested
    @TestInstance(value = PER_CLASS)
    @DisplayName("회원 정보 조회 테스트")
    class findMember {

        private Member member;

        @BeforeAll
        void setUp() {
            // given
            member = createMember();
            memberRepository.save(member);
        }

        @AfterAll
        void tearDown() {
            memberRepository.deleteAllInBatch();
        }

        @DisplayName("PK로 회원 정보 찾는다")
        @Test
        void getMemberByIdTest() {

            // when
            Member member1 = memberRepository.findById(member.getId())
                .orElseThrow(
                    () -> new BaseException(ErrorCode.COMMON_NOT_FOUND.toString(), HttpStatus.NOT_FOUND.toString()));

            // then
            assertEquals(member.getId(), member1.getId());
            assertEquals(member.getEmail(), member1.getEmail());
            assertEquals(member.getPassword(), member1.getPassword());
            assertEquals(member.getName(), member1.getName());

        }

        @DisplayName("회원의 아이디가 존재하는지 확인한다.")
        @Test
        void checkEmailExistTest() {

            // when
            boolean result = memberRepository.existsByEmail(member.getEmail());

            // then
            assertTrue(result);
        }

        private void setSecurityContext() {
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(new TestingAuthenticationToken(member.getId(), "test", "ROLE_USER"));
            SecurityContextHolder.setContext(securityContext);
        }

        @DisplayName("SecurityContextHolder에서 등록된 회원의 PK를 찾아온다.")
        @Test
        void findPK() {
            // given
            setSecurityContext();
            // when
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long memberId = (Long)authentication.getPrincipal();

            // then
            assertEquals(member.getId(), memberId);
         }
    }
}
