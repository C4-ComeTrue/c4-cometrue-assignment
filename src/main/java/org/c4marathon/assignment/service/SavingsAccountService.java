package org.c4marathon.assignment.service;

import org.c4marathon.assignment.api.dto.ChargeSavingsAccountDto;
import org.c4marathon.assignment.api.dto.CreateSavingsAccountDto;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.domain.SavingsType;
import org.c4marathon.assignment.domain.entity.Account;
import org.c4marathon.assignment.domain.entity.Member;
import org.c4marathon.assignment.domain.entity.SavingsAccount;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.MemberRepository;
import org.c4marathon.assignment.repository.SavingsAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavingsAccountService {

	private final MemberRepository memberRepository;
	private final SavingsAccountRepository savingsAccountRepository;
	private final AccountRepository accountRepository;

	/**
	 * 적금 계좌 생성 API
	 */
	@Transactional
	public CreateSavingsAccountDto.Res createSavingsAccount(
		long memberId, String name, long withdrawAmount, SavingsType savingsType
	) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(ErrorCode.INVALID_MEMBER::businessException);

		SavingsAccount account = SavingsAccount.builder()
			.member(member)
			.name(name)
			.withdrawAmount(withdrawAmount)
			.savingsType(savingsType)
			.build();

		SavingsAccount accountEntity = savingsAccountRepository.save(account);
		return new CreateSavingsAccountDto.Res(accountEntity.getId());
	}

	/**
	 * 정기 적금 API -> 매일 오전 8시에 자동 출금
	 * 범위 좁히기 : 충전 <-> 송금 트랜잭션을 분리?
	 */
	@Transactional
	public void transferForRegularSavings(long memberId) {
		// 1. 사용자의 메인 계좌와 저축 계좌를 불러온다.
		Account account = accountRepository.findByMemberId(memberId)
			.orElseThrow(ErrorCode.INVALID_ACCOUNT::businessException);

		SavingsAccount savingsAccount = savingsAccountRepository.findByMemberId(memberId)
			.orElseThrow(ErrorCode.INVALID_ACCOUNT::businessException);

		// 2. 잔고가 요청된 인출 금액 이상 남아있는지 확인한다.
		long withdrawAmount = savingsAccount.getWithdrawAmount();
		if (account.isAmountLackToWithDraw(withdrawAmount)) {
			// TODO : 정기 적금 실패 로그 파일에 기록, 오전 8시 전에 불러와서 재수행
			throw ErrorCode.ACCOUNT_LACK_OF_AMOUNT.businessException();
		}

		// 3. 메인 계좌의 잔액을 차감시키고, 저축 계좌의 잔액을 증가시킨다.
		minusMyAccount(account.getId(), withdrawAmount);
		savingsAccountRepository.charge(savingsAccount.getId(), withdrawAmount);
	}

	/**
	 * 자유 적금 API -> 자유롭게 적금
	 */
	@Transactional
	public ChargeSavingsAccountDto.Res transferForFreeSavings(long accountId, long amount) {
		// 1. 자유 적금 계좌를 불러온다.
		SavingsAccount savingsAccount = savingsAccountRepository.findById(accountId)
			.orElseThrow(ErrorCode.INVALID_ACCOUNT::businessException);

		// 2. 적금 타입을 확인한다.
		if (isNotFreeSavingsAccount(savingsAccount.getSavingsType())) {
			throw ErrorCode.INVALID_SAVINGS_TRANSFER.businessException();
		}

		// 3. 계좌에 돈을 충전한다.
		savingsAccount.charge(amount);
		return new ChargeSavingsAccountDto.Res(savingsAccount.getAmount());
	}

	private void minusMyAccount(long accountId, long withDrawAmount) {
		int effectedRowCnt = accountRepository.withdraw(accountId, withDrawAmount);
		// 만약 해당 로직 사이에 송금이 일어나서 잔액이 부족해지면 예외를 던진다. (UPDATE 시에만 락을 건다)
		if (effectedRowCnt == 0) {
			throw ErrorCode.ACCOUNT_LACK_OF_AMOUNT.businessException();
		}
	}

	private boolean isNotFreeSavingsAccount(SavingsType savingsType) {
		return !savingsType.equals(SavingsType.FREE);
	}
}
