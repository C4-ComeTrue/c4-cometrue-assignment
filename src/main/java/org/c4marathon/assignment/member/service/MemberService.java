package org.c4marathon.assignment.member.service;

import org.c4marathon.assignment.auth.jwt.JwtTokenUtil;
import org.c4marathon.assignment.member.dto.request.JoinRequestDto;
import org.c4marathon.assignment.member.dto.request.LoginRequestDto;
import org.c4marathon.assignment.member.dto.response.LoginResponseDto;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.c4marathon.assignment.util.event.MemberJoinedEvent;
import org.c4marathon.assignment.util.exceptions.BaseException;
import org.c4marathon.assignment.util.exceptions.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    private final ApplicationEventPublisher eventPublisher;

    @Value("${jwt.key}")
    private String secretKey;

    @Value("${jwt.max-age}")
    private Long expireTimeMs;

    // 이메일로 회원 정보 찾기
    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));
    }

    @Transactional
    public void join(JoinRequestDto joinRequestDto) {

        if (checkEmailExist(joinRequestDto.email())) {
            throw new BaseException(ErrorCode.DUPLICATED_EMAIL.toString(), HttpStatus.CONFLICT.toString());
        }

        Member member = Member.builder()
            .email(joinRequestDto.email())
            .password(passwordEncoder.encode(joinRequestDto.password()))
            .name(joinRequestDto.name())
            .build();

        memberRepository.save(member);

        // 회원 가입 완료 이벤트 발행
        eventPublisher.publishEvent(new MemberJoinedEvent(this, joinRequestDto.email()));
    }

    public boolean checkEmailExist(String email) {
        return memberRepository.existsByEmail(email);
    }

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        Member member = memberRepository.findByEmail(loginRequestDto.email())
            .orElseThrow(
                () -> new BaseException(ErrorCode.COMMON_NOT_FOUND.toString(), HttpStatus.NOT_FOUND.toString()));

        if (!passwordEncoder.matches(loginRequestDto.password(), member.getPassword())) {
            throw new BaseException(ErrorCode.LOGIN_FAILED.toString(), HttpStatus.EXPECTATION_FAILED.toString());
        }

        String jwtToken = JwtTokenUtil.createToken(member.getEmail(), secretKey, expireTimeMs);

        return new LoginResponseDto(
            jwtToken
        );
    }
}
