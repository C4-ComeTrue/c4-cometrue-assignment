package org.c4marathon.assignment.member.service;

import org.c4marathon.assignment.member.dto.request.SignUpRequestDto;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.exception.MemberErrorCode;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

	private final MemberRepository memberRepository;

	@Override
	public void signUp(SignUpRequestDto requestDto) {
		if (!isValidMember(requestDto.memberId())) {
			throw MemberErrorCode.USER_ALREADY_EXIST.memberException("회원가입 도중 중복되는 사용자 에러 발생");
		}
		Member member = requestDto.toEntity();
		memberRepository.save(member);
	}

	private boolean isValidMember(String memberId) {
		Member member = memberRepository.findMemberByMemberId(memberId);
		if (member == null) {
			return true;
		}
		return false;
	}
}
