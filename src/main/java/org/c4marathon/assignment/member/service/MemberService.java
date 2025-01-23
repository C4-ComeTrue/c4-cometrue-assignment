package org.c4marathon.assignment.member.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.member.domain.Member;
import org.c4marathon.assignment.member.dto.MemberLoginRequest;
import org.c4marathon.assignment.member.dto.MemberRegisterRequest;
import org.c4marathon.assignment.member.domain.repository.MemberRepository;
import org.c4marathon.assignment.member.dto.MemberRegisterResponse;
import org.c4marathon.assignment.member.exception.DuplicateEmailException;
import org.c4marathon.assignment.member.exception.InvalidPasswordException;
import org.c4marathon.assignment.member.exception.NotFoundMemberException;
import org.c4marathon.global.session.SessionMemberInfo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberRegisterResponse register(MemberRegisterRequest request) {

        if (!validateEmailDuplicate(request.email())) {
            throw new DuplicateEmailException();
        }
        String encodedPassword = passwordEncoder.encode(request.password());

        Member member = Member.create(
                request.email(),
                request.name(),
                encodedPassword
        );

        memberRepository.save(member);
        return new MemberRegisterResponse(member.getId(), member.getEmail());

    }

    public SessionMemberInfo login(MemberLoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(NotFoundMemberException::new);

        if (!passwordMatches(request.password(), member.getPassword())) {
            throw new InvalidPasswordException();
        }

        return new SessionMemberInfo(member.getId(), member.getEmail());

    }

    private boolean passwordMatches(String loginPassword, String matchPassword) {
        return passwordEncoder.matches(loginPassword, matchPassword);
    }

    private boolean validateEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);

    }
}
