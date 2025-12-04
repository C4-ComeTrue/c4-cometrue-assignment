package org.c4marathon.assignment.service;

import java.time.LocalDateTime;

import org.c4marathon.assignment.api.dto.CreateAccountDto;
import org.c4marathon.assignment.api.dto.TransferAccountDto;
import org.c4marathon.assignment.common.event.TransferEvent;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.domain.TransferStatus;
import org.c4marathon.assignment.domain.entity.Account;
import org.c4marathon.assignment.domain.entity.Member;
import org.c4marathon.assignment.domain.entity.TransferLog;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.MemberRepository;
import org.c4marathon.assignment.repository.TransferLogRepository;
import org.c4marathon.assignment.service.transfer.DepositService;
import org.c4marathon.assignment.service.transfer.WithdrawService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

	private final ChargeService chargeService;

	private final AccountRepository accountRepository;

	private final MemberRepository memberRepository;

	private final TransferLogRepository transferLogRepository;

	private final ApplicationEventPublisher eventPublisher;

	private final DepositService depositService;

	private final WithdrawService withdrawService;

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
	 * 메인 계좌 송금 API V1
	 * 한 트랜잭션 내부에서 A 출금과 B 입금 로직이 수행된다.
	 */
	@Transactional
	public TransferAccountDto.Res transfer(
		long accountId, String transferAccountNumber, long transferAmount
	) {
		// A는 바로 데이터 가져오기
		Account account = accountRepository.findById(accountId)
			.orElseThrow(ErrorCode.INVALID_ACCOUNT::businessException);

		if (!accountRepository.existsByAccountNumber(transferAccountNumber)) {
			throw ErrorCode.INVALID_ACCOUNT.businessException();
		}

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
	 * 메인 계좌 송금 API V2 (같은 은행 기준, 타행 송금은 고려 X)
	 * A 계좌 출금 로직을 수행하고 B 입금 로직 이벤트를 발행한다.
	 */
	@Transactional
	public TransferAccountDto.Res transferAsync(
		long accountId, String transferAccountNumber, long transferAmount
	) {
		log.info("A 출금 트랜잭션 로직 수행 : 스레드 {}", Thread.currentThread().getId());
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

		// 4. A 차감이 끝난다면 pending 상태로 DB에 이채 내역을 기록한다.
		TransferLog transferLog = TransferLog.builder()
			.sendAccountId(accountId)
			.receiveAccountNumber(transferAccountNumber)
			.amount(transferAmount)
			.transferStatus(TransferStatus.PENDING)
			.build();

		transferLogRepository.save(transferLog);

		// 5. B 차감 로직을 수행하기 위해 메시지를 발행한다. 커밋이 완료되면 이벤트 리스너가 수행된다.
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
	@Retryable(
		retryFor = TransientDataAccessException.class, // 복구가 가능한 일시적 예외의 경우에 Retry 설정
		maxAttempts = 5,
		backoff = @Backoff(delay = 2000),
		recover = "recoverMethod"
	)  // Ordered.LOWEST_PRECEDENCE - 1(@transactional 보다 먼저 적용 필요)
	@Transactional(propagation = Propagation.REQUIRES_NEW) // Ordered.LOWEST_PRECEDENCE
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void transferPostProcess(TransferEvent transferEvent) {
		log.info("B 입금 트랜잭션 로직 수행, 스레드 : {} 현재 시각 : {}", Thread.currentThread().getId(), LocalDateTime.now());
		Long amount = transferEvent.getAmount();
		String transferAccountNumber = transferEvent.getReceiveAccountNumber();

		try {
			// 1. B 계좌의 유효성을 검사하고 DB 원자적 연산을 통해(get + set) 입금 로직을 처리한다.
			Account transferAccount = accountRepository.findByAccountNumber(transferAccountNumber)
				.orElseThrow(ErrorCode.INVALID_ACCOUNT::businessException);

			accountRepository.deposit(transferAccount.getId(), amount);

			// 2. B 입금까지 정상적으로 끝났다면 이체 기록을 pending -> completed 상태로 변환한다.
			TransferLog transferLog = transferLogRepository.findBySendAccountIdAndReceiveAccountNumberAndAmount(
				transferEvent.getSendAccountId(), transferAccountNumber, amount
			).orElseThrow(ErrorCode.INVALID_TRANSFER_LOG::businessException);

			transferLog.changeCompleted();
		} catch (Exception exception) {
			// 일시적 예외가 아닌 경우는 재시도 진행 X
			if (!(exception instanceof TransientDataAccessException)) {
		     	log.error("재시도가 불가능한 예외 발생, 재시도 진행하지 않고 실패 통지", exception);
				plusMyAccount(transferEvent.getSendAccountId(), transferEvent.getAmount());
				changeTransferLogStatusFailedAndAlertFail(transferEvent);
			}

			// 일시적 예외라면 재시도 진행 이후 복구
			throw exception;
		}
	}

	@Recover
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void recoverMethod(TransientDataAccessException e, TransferEvent transferEvent) {
		// 재시도 끝난 후에도 실패했을 때 해당 메서드에서 보상 트랜잭션 수행 & 송금 실패 예외 반환
		log.error("재시도 전체 실패 후 A 입금 보상 트랜잭션 수행", e);
		plusMyAccount(transferEvent.getSendAccountId(), transferEvent.getAmount());
		changeTransferLogStatusFailedAndAlertFail(transferEvent);
	}

	private void changeTransferLogStatusFailedAndAlertFail(TransferEvent transferEvent) {
		// 송금 내역의 상태를 실패로 변경
		TransferLog failedTransferLog = transferLogRepository.findBySendAccountIdAndReceiveAccountNumberAndAmount(
			transferEvent.getSendAccountId(), transferEvent.getReceiveAccountNumber(), transferEvent.getAmount()
		).orElseThrow(ErrorCode.INVALID_TRANSFER_LOG::businessException);

		failedTransferLog.changeFailed();

		// TODO: 앱 -> FCM 알림 전송으로 실패 상태 통지
		throw ErrorCode.FAILED_TO_TRANSFER.businessException();
	}

	/**
	 * 메인 계좌 송금 API V3 (같은 은행 기준, 타행 송금은 고려 X)
	 * A 계좌 출금 로직을 수행하고 B 입금 로직을 별도 트랜잭션을 열어서 수행하지만, 같은 스레드 내에서 진행한다.
	 */
	public void transferSync(
		long accountId, String transferAccountNumber, long transferAmount
	) {
		withdrawService.withdraw(accountId, transferAccountNumber, transferAmount);
		depositService.deposit(accountId, transferAccountNumber, transferAmount);
	}

	public void plusMyAccount(long accountId, long transferAmount) {
		accountRepository.deposit(accountId, transferAmount);
	}

	private void minusMyAccount(long accountId, long transferAmount) {
		int effectedRowCnt = accountRepository.withdraw(accountId, transferAmount);
		if (effectedRowCnt == 0) {
			throw ErrorCode.ACCOUNT_LACK_OF_AMOUNT.businessException();
		}
	}

	private void plusTargetAccount(String accountNumber, long transferAmount) {
		Account transferAccount = accountRepository.findByAccountNumber(accountNumber)
			.orElseThrow(ErrorCode.INVALID_ACCOUNT::businessException);
		accountRepository.deposit(transferAccount.getId(), transferAmount);
	}

}
