package org.c4marathon.assignment.account.service;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.exception.NotFoundAccountException;
import org.c4marathon.assignment.global.event.deposit.DepositCompletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
public class DepositService {
	private final AccountRepository accountRepository;
	private final ApplicationEventPublisher eventPublisher;

	/**
	 * 입금을 시도하는 로직.
	 * 입금이 정상적으로 커밋 완료 시 -> 이벤트 발행을 하며 Redis에 저장된 송금 기록을 삭제
	 * 입금 중 예외가 발생 시 -> AOP를 통해 예외를 감지하여 Redis에 실패 송금 기록을 저장
	 * @param deposit
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void successDeposit(String deposit) {
		processDeposit(deposit);
	}
	/**
	 * 실패한 입금을 재시도하는 로직
	 * 재입금이 정상적으로 커밋 완료 시 -> 이벤트 발행을 하며 Redis에 저장된 송금 기록을 삭제
	 * 재입금 중 예외(실패) 시 -> AOP를 통해 예외를 감지하여 송금 롤백 시도
	 * @param failedDeposit
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void failedDeposit(String failedDeposit) {
		processDeposit(failedDeposit);
	}

	private void processDeposit(String deposit) {
		String[] parts = deposit.split(":");
		String transactionId = parts[0];
		Long senderAccountId = Long.valueOf(parts[1]);
		Long receiverAccountId = Long.valueOf(parts[2]);
		long money = Long.parseLong(parts[3]);

		Account receiverAccount = accountRepository.findByIdWithLock(receiverAccountId)
			.orElseThrow(NotFoundAccountException::new);

		receiverAccount.deposit(money);
		accountRepository.save(receiverAccount);

		eventPublisher.publishEvent(new DepositCompletedEvent(deposit));
	}
}
