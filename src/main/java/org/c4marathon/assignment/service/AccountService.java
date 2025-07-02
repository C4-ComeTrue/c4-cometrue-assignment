package org.c4marathon.assignment.service;

import org.c4marathon.assignment.api.dto.CreateAccountDto;
import org.c4marathon.assignment.api.dto.TransferAccountDto;
import org.c4marathon.assignment.common.event.TransferEvent;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.domain.entity.Account;
import org.c4marathon.assignment.domain.entity.Member;
import org.c4marathon.assignment.domain.entity.TransferLog;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.MemberRepository;
import org.c4marathon.assignment.repository.TransferLogRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final ChargeService chargeService;

	private final AccountRepository accountRepository;

	private final MemberRepository memberRepository;

	private final TransferLogRepository transferLogRepository;

	private final ApplicationEventPublisher eventPublisher;

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
	 */
	@Transactional
	public TransferAccountDto.Res transfer(
		long accountId, String transferAccountNumber, long transferAmount
	) {
		// A는 바로 데이터 가져오기
		Account account = accountRepository.findById(accountId)
			.orElseThrow(ErrorCode.INVALID_ACCOUNT::businessException);

		// 1. 잔액이 부족할 경우 10000원 단위로 자동 충전한다.
		if (account.isAmountLackToWithDraw(transferAmount)) {
			chargeService.autoChargeByUnit(accountId, transferAmount);
		}

		// 2. 잔액이 여유로워졌다면, 내 계좌의 잔액을 차감시키고 친구의 메인 계좌로 송금한다.
		minusMyAccount(accountId, transferAmount);
		plusTargetAccount(transferAccountNumber, transferAmount);

		long resultAmount = accountRepository.findAmount(accountId);
		return new TransferAccountDto.Res(resultAmount);
	}

	/**
	 * A -> B 메인 계좌 송금 API
	 */
	@Transactional
	public TransferAccountDto.Res transferAsync(
		long accountId, String transferAccountNumber, long transferAmount
	) {
		// 1. 유효성 검사를 수행한다. B 계좌도 미리 앞단에서 수행해서 불필요한 동작을 방지한다.
		Account account = accountRepository.findById(accountId)
			.orElseThrow(ErrorCode.INVALID_ACCOUNT::businessException);

		if (!accountRepository.existsByAccountNumber(transferAccountNumber)) {
			throw ErrorCode.INVALID_ACCOUNT.businessException();
		}

		// 2. A계좌 잔액이 부족할 경우 10000원 단위로 자동 충전한다.
		if (account.isAmountLackToWithDraw(transferAmount)) {
			chargeService.autoChargeByUnit(accountId, transferAmount);
		}

		// 3. 잔액이 여유로워졌다면, A 계좌의 잔액을 차감시킨다.
		minusMyAccount(accountId, transferAmount);

		// 3. B 차감 로직을 수행하기 위해 메시지를 발행한다. 커밋이 완료되면 이벤트 리스너가 수행된다.
		TransferEvent transferEvent = new TransferEvent(this, accountId, transferAccountNumber, transferAmount);
		eventPublisher.publishEvent(transferEvent);

		long resultAmount = accountRepository.findAmount(accountId);
		return new TransferAccountDto.Res(resultAmount);
	}

	/**
	 * B 입금 로직 수행
	 * 새로운 트랜잭션을 열고, A 차감 로직이 롤백 없이 커밋된 경우에 B 입금 이벤트가 비동기로 수행되도록 한다.
	 */
	@Async("customTaskExecutor")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void transferPostProcess(TransferEvent transferEvent) {
		// 1. B 계좌를 비관적 락을 통해 조회해서 금액을 업데이트한다.
		Long amount = transferEvent.getAmount();
		String transferAccountNumber = transferEvent.getReceiveAccountNumber();

		Account transferAccount = accountRepository.findByAccountNumberWithWriteLock(transferAccountNumber)
			.orElseThrow(ErrorCode.INVALID_ACCOUNT::businessException);

		accountRepository.deposit(transferAccount.getId(), amount);

		// 2. A 차감과 B 입금이 정상적으로 끝났다면 이체 기록을 데이터베이스에 저장한다.
		TransferLog transferLog = TransferLog.builder()
			.sendAccountId(transferEvent.getSendAccountId())
			.receiveAccountNumber(transferAccountNumber)
			.amount(amount)
			.build();

		transferLogRepository.save(transferLog);
	}

	private void minusMyAccount(long accountId, long transferAmount) {
		int effectedRowCnt = accountRepository.withdraw(accountId, transferAmount);
		if (effectedRowCnt == 0) {
			throw ErrorCode.ACCOUNT_LACK_OF_AMOUNT.businessException();
		}
	}

	private void plusTargetAccount(String accountNumber, long transferAmount) {
		Account transferAccount = accountRepository.findByAccountNumberWithWriteLock(accountNumber)
			.orElseThrow(ErrorCode.INVALID_ACCOUNT::businessException);
		accountRepository.deposit(transferAccount.getId(), transferAmount);
	}
}
