package org.c4marathon.assignment.member.service;

import org.c4marathon.assignment.IntegrationTestSupport;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.member.domain.Member;
import org.c4marathon.assignment.member.domain.repository.MemberRepository;
import org.c4marathon.assignment.member.dto.MemberLoginRequest;
import org.c4marathon.assignment.member.dto.MemberRegisterRequest;
import org.c4marathon.assignment.member.dto.MemberRegisterResponse;
import org.c4marathon.assignment.member.exception.DuplicateEmailException;
import org.c4marathon.assignment.member.exception.InvalidPasswordException;
import org.c4marathon.assignment.member.exception.NotFoundMemberException;
import org.c4marathon.assignment.global.session.SessionMemberInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class MemberServiceTest extends IntegrationTestSupport {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
    }

    @Transactional
    @DisplayName("회원가입 및 계좌 생성 통합 테스트")
    @Test
    void register() {
        // given
        MemberRegisterRequest registerRequest = new MemberRegisterRequest(
                "test@test.com",
                "테스트",
                "test"
        );

        // when
        MemberRegisterResponse registerMember = memberService.register(registerRequest);

        // then
        Member member = memberRepository.findById(registerMember.memberId())
                .orElseThrow(NotFoundMemberException::new);
        assertThat(member.getEmail()).isEqualTo("test@test.com");

        // 메인 계좌 생성 여부 검증
        Optional<Account> account = accountRepository.findByAccountNumberWithLock(member.getAccountNumber());
        assertThat(account).isPresent();
    }

    @DisplayName("이미 가입한 이메일로 회원가입 시도 시 DuplicateEmailException 가 발생한다.")
    @Test
    void registerByDuplicateEmail() throws Exception {
        Member member = Member.create("test@test.com", "테스트", "test");
        memberRepository.save(member);

        MemberRegisterRequest registerRequest = new MemberRegisterRequest(
                "test@test.com",
                "테스트",
                "test"
        );

        // when // then
        assertThatThrownBy(() -> memberService.register(registerRequest))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("이미 가입한 이메일입니다.");
    }

    @DisplayName("로그인 테스트")
    @Test
    void login() {
        // given
        Member member = Member.create("test@test.com", "테스트", passwordEncoder.encode("test"));
        member.setMainAccountNumber("3333");
        memberRepository.save(member);
        MemberLoginRequest loginRequest = new MemberLoginRequest("test@test.com", "test");

        // when
        SessionMemberInfo loginMember = memberService.login(loginRequest);

        // then
        assertThat(loginMember)
                .extracting("memberId", "email", "accountNumber")
                .contains(member.getId(), "test@test.com", "3333");
    }

    @Test
    @DisplayName("가입한적 없는 이메일로 로그인 시도를 할 경우 NotFoundMemberException 예외가 발생한다.")
    void loginWithNonExistentEmail() {
        // given
        MemberLoginRequest loginRequest = new MemberLoginRequest("test@test.com", "test");

        // when // then
        assertThatThrownBy(() -> memberService.login(loginRequest))
                .isInstanceOf(NotFoundMemberException.class)
                .hasMessage("조회된 멤버가 없습니다.");
    }

    @DisplayName("틀린 비밀번호로 로그인 시도할 경우 InvalidPasswordException 예외가 발생한다.")
    @Test
    void test() {
        // given
        Member member = Member.create("test@test.com", "테스트", passwordEncoder.encode("test"));
        memberRepository.save(member);
        MemberLoginRequest loginRequest = new MemberLoginRequest("test@test.com", "wrongPassword");

        // when // then
        assertThatThrownBy(() -> memberService.login(loginRequest))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("잘못된 비밀번호 입니다.");
    }




}