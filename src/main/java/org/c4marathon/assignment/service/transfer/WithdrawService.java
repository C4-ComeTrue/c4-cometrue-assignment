package org.c4marathon.assignment.service.transfer;

import java.time.LocalDateTime;

import org.c4marathon.assignment.api.dto.TransferAccountDto;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.domain.TransferStatus;
import org.c4marathon.assignment.domain.entity.Account;
import org.c4marathon.assignment.domain.entity.TransferLog;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.TransferLogRepository;
import org.c4marathon.assignment.service.ChargeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 출금 로직
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WithdrawService {

	private final ChargeService chargeService;

	private final AccountRepository accountRepository;

	private final TransferLogRepository transferLogRepository;


	@Transactional
	public TransferAccountDto.Res withdraw(
		long accountId, String transferAccountNumber, long transferAmount
	) {

		log.info("A 차감 트랜잭션 로직 수행, 스레드 : {} 현재 시각 : {}", Thread.currentThread().getId(), LocalDateTime.now());

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

		// 2. A 차감이 끝난다면 pending 상태로 DB에 이채 내역을 기록한다.
		TransferLog transferLog = TransferLog.builder()
			.sendAccountId(accountId)
			.receiveAccountNumber(transferAccountNumber)
			.amount(transferAmount)
			.transferStatus(TransferStatus.PENDING)
			.build();

		transferLogRepository.save(transferLog);

		long resultAmount = accountRepository.findAmount(accountId);
		return new TransferAccountDto.Res(resultAmount);
	}

	private void minusMyAccount(long accountId, long transferAmount) {
		int effectedRowCnt = accountRepository.withdraw(accountId, transferAmount);
		if (effectedRowCnt == 0) {
			throw ErrorCode.ACCOUNT_LACK_OF_AMOUNT.businessException();
		}
	}
}
