package org.c4marathon.assignment.service;

import java.util.Random;

import org.c4marathon.assignment.api.dto.MemberSignUpDto;
import org.c4marathon.assignment.common.utils.EncryptUtils;
import org.c4marathon.assignment.domain.entity.Account;
import org.c4marathon.assignment.domain.entity.Member;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.qos.logback.core.testUtil.RandomUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final AccountRepository accountRepository;

	//TODO: 인증 및 인가
	@Transactional
	public MemberSignUpDto.Res register(String email, String password) {
		// 1. 유저 회원 가입
		Member member = new Member(email, EncryptUtils.encrypt(password));  // 단방향 해싱 알고리즘
		Member memberEntity = memberRepository.save(member);

		// 2. 메인 계좌 생성
		Account account = Account.builder()
			.member(member)
			.accountNumber(generateAccountNumber())
			.build();

		Account accountEntity = accountRepository.save(account);
		return new MemberSignUpDto.Res(memberEntity.getId(), accountEntity.getId());
	}

	private String generateAccountNumber() {
		return String.valueOf(new Random().nextInt());  // 임식 계좌 번호 자동 생성
	}
}
