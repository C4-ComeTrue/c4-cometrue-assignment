package org.c4marathon.assignment.bankaccount.service;

import org.c4marathon.assignment.bankaccount.exception.async.AccountAsyncErrorCode;
import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SendRollbackHandlerService {
	private final MainAccountRepository mainAccountRepository;

	@Async("rollbackExecutor")
	@Transactional
	public void rollBackDeposit(long sendPk, long depositPk, long money) {
		int updateResult = mainAccountRepository.deposit(sendPk, money);

		// 상대 계좌에 업데이트가 되지 않은 경우 롤백 실패 예외가 발생한다.
		if (updateResult == 0) {
			throw AccountAsyncErrorCode.SEND_ROLLBACK_FAILED.accountAsyncException();
		}

	}
}
