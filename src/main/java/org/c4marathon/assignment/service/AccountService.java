package org.c4marathon.assignment.service;

import org.c4marathon.assignment.api.dto.ChargeAccountDto;
import org.c4marathon.assignment.api.dto.CreateAccountDto;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.domain.ChargeLimit;
import org.c4marathon.assignment.domain.entity.Account;
import org.c4marathon.assignment.domain.entity.Member;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final AccountRepository accountRepository;
	private final MemberRepository memberRepository;

	/**
	 * 메인 계좌 생성 API
	 */
	@Transactional
	public CreateAccountDto.Res createAccount(long memberId, String name, String accountNumber) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(ErrorCode.INVALID_MEMBER::businessException);

		Account account = Account.builder()  // 기본 한도 자동 설정
			.member(member)
			.name(name)
			.accountNumber(accountNumber)
			.build();

		Account accountEntity = accountRepository.save(account);
		return new CreateAccountDto.Res(accountEntity.getId());
	}

	/**
	 * 메인 계좌 충전 API
	 */
	@Transactional
	public ChargeAccountDto.Res charge(long accountId, int amount) {
		// 1. 현재 충전 한도와 잔고가 얼마인지 확인한다.
		Account account = accountRepository.findByIdWithWriteLock(accountId)
			.orElseThrow(ErrorCode.INVALID_ACCOUNT::businessException);

		// 2. 1일 충전 한도를 넘지 않는지 확인한다.
		ChargeLimit chargeLimit = account.getChargeLimit();
		if (chargeLimit.doesExceed(account.getAccumulatedChargeAmount(), amount)) {
			throw ErrorCode.EXCEED_CHARGE_LIMIT.businessException();
		}

		// 3. 메인 계좌의 잔액을 증가시킨다.
		account.charge(amount);
		return new ChargeAccountDto.Res(account.getAmount());
	}
}
