package org.c4marathon.assignment.service;

import org.c4marathon.assignment.api.dto.CreateAccountDto;
import org.c4marathon.assignment.api.dto.TransferAccountDto;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.domain.entity.Account;
import org.c4marathon.assignment.domain.entity.Member;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final ChargeService chargeService;
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
	 * 메인 계좌 송금 API
	 * 내 계좌 충전 -> 내 계좌 감소 -> 상대방 계좌 증가가 하나의 트랜잭션 안에 묶여야 할 것 같은데, 범위를 어떻게 좁힐 수 있을 지 고민
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public TransferAccountDto.Res transfer(
		long accountId, String transferAccountNumber, long transferAmount
	) {
		Account account = accountRepository.findById(accountId)
			.orElseThrow(ErrorCode.INVALID_ACCOUNT::businessException);

		// 1. 잔액이 부족할 경우 10000원 단위로 자동 충전한다.
		if (account.isAmountLackToWithDraw(transferAmount)) {
			chargeService.autoChargeByUnit(accountId, transferAmount);
		}

		// 2. 잔액이 여유로워졌다면, 내 메인 계좌의 잔액을 차감시킨다.
		int effectedRowCnt = accountRepository.withdraw(accountId, transferAmount);
		if (effectedRowCnt == 0) {
			throw ErrorCode.ACCOUNT_LACK_OF_AMOUNT.businessException();
		}

		// 3. 친구의 메인 계좌로 송금한다.
		transferToTargetAccount(transferAccountNumber, transferAmount);

		long resultAmount = accountRepository.findAmount(accountId);
		return new TransferAccountDto.Res(resultAmount);
	}

	@Transactional
	public void transferToTargetAccount(String transferAccountNumber, long transferAmount) {
		Account transferAccount = accountRepository.findByAccountNumber(transferAccountNumber)
			.orElseThrow(ErrorCode.INVALID_ACCOUNT::businessException);

		accountRepository.deposit(transferAccount.getId(), transferAmount);
	}
}
