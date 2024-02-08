package org.c4marathon.assignment.service;

import org.c4marathon.assignment.api.dto.MemberSignUpDto;
import org.c4marathon.assignment.common.utils.EncryptUtils;
import org.c4marathon.assignment.domain.Member;
import org.c4marathon.assignment.repository.MemberRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;

	//TODO: 인증 및 인가
	public MemberSignUpDto.Response register(String email, String password) {
		// 1. 유저 회원 가입
		Member member = new Member(email, EncryptUtils.encrypt(password));  // 단방향 해싱 알고리즘
		Member memberEntity = memberRepository.save(member);
		return new MemberSignUpDto.Response(memberEntity.getId());
	}
}
