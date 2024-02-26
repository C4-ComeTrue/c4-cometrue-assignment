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
import org.springframework.transaction.annotation.Isolation;
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
	 * 드물게 발생하지 않는 상황을 위해 매번 쓰기 락을 거는 것은 비효율적
	 * REPEATABLE READ 설정 시 메인 계좌 잔액 차감 로직에서 데드락 발생
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void transferForRegularSavings(long memberId) {
		// 1. 사용자의 메인 계좌와 저축 계좌를 불러온다.
		Account account = accountRepository.findByMemberId(memberId)
			.orElseThrow(ErrorCode.INVALID_ACCOUNT::businessException);

		SavingsAccount savingsAccount = savingsAccountRepository.findByMemberId(memberId)
			.orElseThrow(ErrorCode.INVALID_ACCOUNT::businessException);

		// 2. 잔고가 요청된 인출 금액 이상 남아있는지 확인한다. (불필요한 쿼리 방지)
		long withdrawAmount = savingsAccount.getWithdrawAmount();
		if (isAmountEnoughToWithdraw(account.getAmount(), withdrawAmount)) {
			doSavingsFailProcess();
		}

		// 4. 메인 계좌의 잔액을 차감시킨다.
		int effectedRowCnt = accountRepository.withdraw(account.getId(), withdrawAmount);

		// 5. 만약 해당 로직 사이에 송금이 일어나서 잔액이 부족해지면 예외를 던진다. (UPDATE 시에만 락을 건다)
		if (isAmountLackToWithdraw(effectedRowCnt)) {
			doSavingsFailProcess();
		}

		// 6. 저축 계좌의 잔액을 증가시킨다.
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

		// 2. 계좌에 돈을 충전한다.
		savingsAccount.charge(amount);
		return new ChargeSavingsAccountDto.Res(savingsAccount.getAmount());
	}

	private boolean isAmountEnoughToWithdraw(long totalAmount, long withDrawAmount) {
		return totalAmount < withDrawAmount;
	}

	private boolean isAmountLackToWithdraw(int effectedRowCnt) {
		return effectedRowCnt == 0;
	}

	private void doSavingsFailProcess() {
		// TODO : 정기 적금 실패 로그 파일에 기록, 오전 8시 전에 불러와서 재수행
		throw ErrorCode.ACCOUNT_LACK_OF_AMOUNT.businessException();
	}

	private boolean isNotFreeSavingsAccount(SavingsType savingsType) {
		return !savingsType.equals(SavingsType.FREE);
	}
}
