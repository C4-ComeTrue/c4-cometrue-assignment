package org.c4marathon.assignment.account.service;

import static org.c4marathon.assignment.transactional.domain.TransactionalStatus.*;

import java.time.LocalDateTime;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.exception.NotFoundAccountException;
import org.c4marathon.assignment.transactional.domain.TransferTransactional;
import org.c4marathon.assignment.transactional.domain.repository.TransactionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositService {
	private final AccountRepository accountRepository;
	private final TransactionalRepository transactionalRepository;

	/**
	 * 송금 내역 데이터를 조회해서 출금 로직 실행
	 * status = WITHDRAW인 송금 내역을 조회해서 입금 처리를 함
	 * 입금 성공 : 송금 내역 status를 SUCCESS_DEPOSIT, 입금 한 시간을 업데이트 한다.
	 * 입금 실패 : AOP를 통해 송금 내역 status를 FAILED_DEPOSIT로 변경한다.
	 * @param transactional
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void successDeposit(TransferTransactional transactional) {
		processDeposit(transactional);
	}

	/**
	 * 송금 내역 데이터를 조회해서 출금 로직 실행
	 * status = FAILED_DEPOSIT인 송금 내역을 조회해서 입금 재시도를 함
	 * 입금 성공 : 송금 내역 status를 SUCCESS_DEPOSIT, 입금 한 시간을 업데이트 한다.
	 * 입금 재시도 실패 : AOP를 통해 송금 내역 status를 CANCEL로 변경 후 송금 취소한다.
	 * @param transactional
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void failedDeposit(TransferTransactional transactional) {
		processDeposit(transactional);
	}

	private void processDeposit(TransferTransactional transactional) {
		Long receiverAccountId = transactional.getReceiverAccountId();
		long amount = transactional.getAmount();

		Account receiverAccount = accountRepository.findByIdWithLock(receiverAccountId)
			.orElseThrow(NotFoundAccountException::new);

		receiverAccount.deposit(amount);
		accountRepository.save(receiverAccount);

		transactional.setReceiverTime(LocalDateTime.now());
		transactional.updateStatus(SUCCESS_DEPOSIT);
		transactionalRepository.save(transactional);

	}
}
