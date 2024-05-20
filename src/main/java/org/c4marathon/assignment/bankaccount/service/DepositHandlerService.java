package org.c4marathon.assignment.bankaccount.service;

import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepositHandlerService {
	private final MainAccountRepository mainAccountRepository;

	/**
	 *
	 * 입금 로직을 처리하는 메소드.
	 *
	 * @param sendPk 보내는 사람의 계좌
	 * @param depositPk 받는 사람의 계좌
	 * @param money 이체할 금액
	 */
	@Async("depositExecutor")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void doDeposit(long sendPk, long depositPk, long money) {
		int updateResult = mainAccountRepository.deposit(depositPk, money);
		// 상대 계좌에 업데이트가 되지 않은 경우 롤백해야 한다.
		if (updateResult == 0) {
			mainAccountRepository.deposit(sendPk, money);
		}
	}

}
