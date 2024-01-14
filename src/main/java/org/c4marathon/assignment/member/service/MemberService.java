package org.c4marathon.assignment.member.service;

import org.c4marathon.assignment.auth.jwt.JwtTokenUtil;
import org.c4marathon.assignment.member.dto.RequestDto;
import org.c4marathon.assignment.member.dto.ResponseDto;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.c4marathon.assignment.util.exceptions.BaseException;
import org.c4marathon.assignment.util.exceptions.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

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
    public void join(RequestDto.JoinDto joinDto) {

        if (checkEmailExist(joinDto.email())) {
            throw new BaseException(ErrorCode.DUPLICATED_EMAIL.toString(), HttpStatus.CONFLICT.toString());
        }

        Member member = Member.builder()
            .email(joinDto.email())
            .password(passwordEncoder.encode(joinDto.password()))
            .name(joinDto.name())
            .build();

        memberRepository.save(member);
    }

    public boolean checkEmailExist(String email) {
        return memberRepository.existsByEmail(email);
    }

    public ResponseDto.LoginDto login(RequestDto.LoginDto loginDto) {
        Member member = memberRepository.findByEmail(loginDto.email())
            .orElseThrow(
                () -> new BaseException(ErrorCode.COMMON_NOT_FOUND.toString(), HttpStatus.NOT_FOUND.toString()));

        if (!passwordEncoder.matches(loginDto.password(), member.getPassword())) {
            throw new BaseException(ErrorCode.LOGIN_FAILED.toString(), HttpStatus.EXPECTATION_FAILED.toString());
        }

        String jwtToken = JwtTokenUtil.createToken(member.getEmail(), secretKey, expireTimeMs);

        return new ResponseDto.LoginDto(
            jwtToken
        );
    }
}
