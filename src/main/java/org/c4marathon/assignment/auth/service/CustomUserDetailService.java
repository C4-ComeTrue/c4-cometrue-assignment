package org.c4marathon.assignment.auth.service;

import java.util.Collections;

import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.c4marathon.assignment.util.exceptions.BaseException;
import org.c4marathon.assignment.util.exceptions.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomUserDetailService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(
                () -> new BaseException(ErrorCode.COMMON_NOT_FOUND.toString(), HttpStatus.NOT_FOUND.toString()));

        return new org.springframework.security.core.userdetails.User(
            member.getEmail(),
            member.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}