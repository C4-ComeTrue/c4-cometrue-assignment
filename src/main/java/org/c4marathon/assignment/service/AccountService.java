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
	public TransferAccountDto.Res transferV2(
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
	 * 새로운 트랜잭션을 열고, A 차감 로직이 롤백 없이 커밋된 경우에 B 입금 이벤트가 수행되도록 한다.
	 * @Async 비동기 방식은 별도 멀티 스레드에서 처리가 되는거고, 유저에게는 해당 이벤트 처리 로직과 상관 없이 바로 반환이 된다. (같은 스레드를 계속 점유 X)
	 * 문제는 B 입금 로직이 실패 --> 몇번 텀 두고 재시도해도 실패하면 A 차감 로직까지 롤백이 필요하지 않나 싶은데,
	 * 비동기를 도입하면 사용자에게는 A 차감만 성공해도 송금이 성공했다고 알려지게 되니까,
	 * 그냥 동기 방식으로 수행하고 재시도 해도 B 입금 실패 시 예외 발생 → 사용자에게 송금 실패로 응답하는게 맞는 것 같다.
	 * 사용자 관점에서 일관된 성공/실패 처리 가능하지만, 대신 이러면 API 응답 시간이 느려진다는 단점은 존재한다.
	 */
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
