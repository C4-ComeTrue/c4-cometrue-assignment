package org.c4marathon.assignment.member.service;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.c4marathon.assignment.util.entity.Status;
import org.c4marathon.assignment.util.exceptions.BaseException;
import org.c4marathon.assignment.util.exceptions.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
public class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void tearDown() {

        memberRepository.deleteAllInBatch();
    }

    private Member createMember(String email, String password, String name) {

        return Member.builder()
            .email(email)
            .password(password)
            .name(name)
            .build();
    }

    private boolean existsByEmail(String email) {

        return memberRepository.existsByEmail(email);
    }

    @DisplayName("회원 가입 정보를 받아 새로운 회원을 생성한다.")
    @Test
    @Transactional
    void signUpTest() {

        // given
        Member member = createMember("test@email.com", "testpassword", "testuesr");
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
        Member member = createMember("test@email.com", "testpassword", "testuesr");
        memberRepository.save(member);

        // when
        Member member1 = createMember("test@email.com", "testpassword", "testuesr");

        // then
        assertThatThrownBy(() -> {
            if (existsByEmail(member1.getEmail())) {
                throw new BaseException(ErrorCode.DUPLICATED_EMAIL.toString(), HttpStatus.CONFLICT.toString());
            }
            memberRepository.save(member1);
        }).isInstanceOf(BaseException.class)
            .hasMessageContaining(HttpStatus.CONFLICT.toString());
    }

    @DisplayName("회원 정보를 통한 로그인이 성공한다.")
    @Test
    void signInTest() {

        // given
        Member member = createMember("test@email.com", "testpassword", "testuesr");
        memberRepository.save(member);

        // when
        Member member1 = memberRepository.findByEmail("test@email.com").get();

        // then
        assertThat(member.getEmail()).isEqualTo(member1.getEmail());
        assertThat(member.getPassword()).isEqualTo(member1.getPassword());
    }
}
