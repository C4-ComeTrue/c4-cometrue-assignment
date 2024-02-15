package org.c4marathon.assignment.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.*;

import java.util.Optional;

import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.c4marathon.assignment.util.entity.Status;
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
import org.springframework.http.HttpStatus;
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

    private Member createMember() {

        return Member.builder()
            .email("test@naver.com")
            .password(passwordEncoder.encode("test"))
            .name("test")
            .build();
    }

    private boolean existsByEmail(String email) {

        return memberRepository.existsByEmail(email);
    }

    @Nested
    @DisplayName("회원 가입 테스트")
    class signUp {

        @AfterEach
        void tearDown() {

            memberRepository.deleteAllInBatch();
        }

        @DisplayName("회원 가입 정보를 받아 새로운 회원을 생성한다.")
        @Test
        @Transactional
        void signUpTest() {

            // given
            Member member = createMember();
            member.setStatus(Status.ACTIVE);

            // when
            if (existsByEmail(member.getEmail())) {
                throw new BaseException(ErrorCode.DUPLICATED_EMAIL.toString(), HttpStatus.CONFLICT.toString());
            }
            memberRepository.save(member);

            // then
            Optional<Member> findMember = memberRepository.findByEmail(member.getEmail());
            assertThat(findMember).isPresent();
            assertThat(findMember.get().getEmail()).isEqualTo(member.getEmail());
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
                if (existsByEmail(member1.getEmail())) {
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

            // when
            Member member1 = memberRepository.findByEmail(member.getEmail()).orElseThrow(
                () -> new BaseException(ErrorCode.COMMON_NOT_FOUND.toString(),
                    HttpStatus.NOT_FOUND.toString()));

            // then
            assertThat(member.getEmail()).isEqualTo(member1.getEmail());
            assertThat(member.getPassword()).isEqualTo(member1.getPassword());
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
                        HttpStatus.EXPECTATION_FAILED.toString());
                }
            });

            // then
            assertEquals(HttpStatus.EXPECTATION_FAILED.toString(), exception.getMessage());
        }
    }
}
