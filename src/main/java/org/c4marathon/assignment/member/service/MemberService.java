package org.c4marathon.assignment.member.service;

import org.c4marathon.assignment.bankaccount.entity.MainAccount;
import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.c4marathon.assignment.member.dto.request.SignInRequestDto;
import org.c4marathon.assignment.member.dto.request.SignUpRequestDto;
import org.c4marathon.assignment.member.dto.response.MemberInfoResponseDto;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.exception.MemberErrorCode;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.c4marathon.assignment.member.session.SessionMemberInfo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final MainAccountRepository mainAccountRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public void signUp(SignUpRequestDto requestDto) {
		if (!isValidId(requestDto.memberId())) {
			throw MemberErrorCode.USER_ALREADY_EXIST.memberException("회원가입 도중 중복되는 사용자 에러 발생");
		}
		MainAccount mainAccount = new MainAccount();
		mainAccountRepository.save(mainAccount);

		String encodedPassword = passwordEncoder.encode(requestDto.password());
		Member member = requestDto.toEntity(mainAccount.getAccountPk(), encodedPassword);
		memberRepository.save(member);

	}

	public SessionMemberInfo signIn(SignInRequestDto requestDto) {
		Member member = memberRepository.findMemberByMemberId(requestDto.memberId());
		if (member == null) {
			throw MemberErrorCode.USER_NOT_FOUND.memberException("존재하지 않는 사용자, memberId = " + requestDto.memberId());
		}
		if (!isSamePassword(requestDto.password(), member.getPassword())) {
			throw MemberErrorCode.INVALID_PASSWORD.memberException("비밀번호 불일치");
		}

		return new SessionMemberInfo(member.getMemberPk(), member.getMemberId(), member.getMemberName(),
			member.getMainAccountPk());
	}

	public MemberInfoResponseDto getMemberInfo(long memberPk) {
		Member member = memberRepository.findById(memberPk)
			.orElseThrow(() -> MemberErrorCode.USER_NOT_FOUND.memberException(
				"service 계층 getMemberInfo 메소드 실행 중 존재하지 않는 사용자 요청 발생"));

		return new MemberInfoResponseDto(member.getMemberPk(), member.getMemberId(), member.getMemberName());
	}

	private boolean isValidId(String memberId) {
		return memberRepository.findMemberByMemberId(memberId) == null;
	}

	private boolean isSamePassword(String inputPassword, String findPassword) {
		return passwordEncoder.matches(inputPassword, findPassword);
	}
}
