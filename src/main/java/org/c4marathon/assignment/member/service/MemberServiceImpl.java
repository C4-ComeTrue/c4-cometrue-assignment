package org.c4marathon.assignment.member.service;

import org.c4marathon.assignment.bankaccount.entity.MainAccount;
import org.c4marathon.assignment.bankaccount.limit.ChargeLimitManager;
import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.c4marathon.assignment.member.dto.request.SignInRequestDto;
import org.c4marathon.assignment.member.dto.request.SignUpRequestDto;
import org.c4marathon.assignment.member.dto.response.MemberInfoResponseDto;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.exception.MemberErrorCode;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.c4marathon.assignment.member.session.SessionMemberInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

	private final MemberRepository memberRepository;
	private final MainAccountRepository mainAccountRepository;
	private final ChargeLimitManager chargeLimitManager;

	@Override
	@Transactional
	public void signUp(SignUpRequestDto requestDto) {
		if (!isValidMember(requestDto.memberId())) {
			throw MemberErrorCode.USER_ALREADY_EXIST.memberException("회원가입 도중 중복되는 사용자 에러 발생");
		}
		MainAccount mainAccount = new MainAccount();
		mainAccount.init();
		mainAccountRepository.save(mainAccount);

		chargeLimitManager.init(mainAccount.getAccountPk());

		Member member = requestDto.toEntity(mainAccount.getAccountPk());
		memberRepository.save(member);

	}

	@Override
	public SessionMemberInfo signIn(SignInRequestDto requestDto) {
		Member member = memberRepository.findMemberByMemberId(requestDto.memberId());
		if (member == null) {
			throw MemberErrorCode.USER_NOT_FOUND.memberException("service 계층 signin 메소드 실행 중 존재하지 않는 사용자 아이디 입력 발생");
		}
		if (!isValidMember(requestDto.password(), member.getPassword())) {
			throw MemberErrorCode.INVALID_PASSWORD.memberException("service 계층 signin 메소드 실행 중 비밀번호 불일치 발생");
		}

		return new SessionMemberInfo(member.getMemberPk(), member.getMemberId(), member.getMainAccountPk());
	}

	@Override
	public MemberInfoResponseDto getMemberInfo(long memberPk) {
		Member member = memberRepository.findById(memberPk)
			.orElseThrow(() -> MemberErrorCode.USER_NOT_FOUND.memberException(
				"service 계층 getMemberInfo 메소드 실행 중 존재하지 않는 사용자 요청 발생"));

		return new MemberInfoResponseDto(member.getMemberPk(), member.getMemberId(), member.getMemberName());
	}

	private boolean isValidMember(String memberId) {
		return memberRepository.findMemberByMemberId(memberId) == null;
	}

	private boolean isValidMember(String inputPassword, String findPassword) {
		return inputPassword.equals(findPassword);
	}
}
