package org.c4marathon.assignment.service;

import java.time.LocalDate;

import org.c4marathon.assignment.api.dto.ChargeAccountDto;
import org.c4marathon.assignment.common.exception.ErrorCode;
import org.c4marathon.assignment.common.utils.ChargeLimitUtils;
import org.c4marathon.assignment.domain.entity.Account;
import org.c4marathon.assignment.domain.entity.ChargeLinkedAccount;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.ChargeLinkedAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ChargeService {

	private static final long BASE_CHARGE_UNIT = 10000;

	private final ChargeLinkedAccountRepository linkedAccountRepository;

	private final AccountRepository accountRepository;

	/**
	 * 충전 연동 계좌 등록 API
	 */
	@Transactional
	public void registerChargeAccount(
		long accountId, String bank, String accountNumber, boolean main
	) {
		Account account = accountRepository.findById(accountId)
			.orElseThrow(ErrorCode.INVALID_ACCOUNT::businessException);

		ChargeLinkedAccount linkedAccount = ChargeLinkedAccount.builder()
			.account(account)
			.bank(bank)
			.accountNumber(accountNumber)
			.main(main)
			.build();

		linkedAccountRepository.save(linkedAccount);
	}

	/**
	 * 메인 계좌 자동 충전 API
	 */
	@Transactional
	public void autoChargeByUnit(long accountId, long amount) {
		// 1. 해당 계좌와 연동된 주 충전 계좌를 가져온다.
		ChargeLinkedAccount linkedAccount = linkedAccountRepository.findById(accountId)
			.orElseThrow(ErrorCode.INVALID_CHARGE_LINKED_ACCOUNT::businessException);

		// 2. 만원 단위로 충전할 금액을 계산한다.
		long chargeAmount = getChargeAmountByUnit(amount);

		// 3. 주 충전 연동 계좌를 확인하고, 충전이 가능한지 확인한다.
		if (linkedAccount.getAmount() > chargeAmount) {
			throw ErrorCode.ACCOUNT_LACK_OF_AMOUNT.businessException();
		}

		// 4. 금액을 충전하고, 충전 연동 계좌에서 충전 금액만큼 차감한다.
		charge(accountId, chargeAmount);
		linkedAccount.withdraw(chargeAmount);
	}

	/**
	 * 메인 계좌 수동 충전 API
	 */
	@Transactional
	public ChargeAccountDto.Res charge(long accountId, long chargeAmount) {
		// 1. 현재 충전 한도와 잔고가 얼마인지 확인한다.
		Account account = accountRepository.findByIdWithWriteLock(accountId)
			.orElseThrow(ErrorCode.INVALID_ACCOUNT::businessException);

		// 2. 다음날이 되어 1일 충전 한도를 초기화 시켜야 하는지 확인한다.
		if (isChargeDateChanged(account.getChargeUpdatedAt())) {
			account.initializeChargeAmount();
		}

		// 3. 1일 충전 한도를 넘지 않는지 확인한다.
		long chargeLimit = account.getChargeLimit();
		if (ChargeLimitUtils.doesExceedLimit(chargeLimit, account.getAccumulatedChargeAmount(), chargeAmount)) {
			throw ErrorCode.EXCEED_CHARGE_LIMIT.businessException();
		}

		// 4. 메인 계좌의 잔액을 증가시킨다.
		account.charge(chargeAmount);
		return new ChargeAccountDto.Res(account.getAmount());
	}

	private long getChargeAmountByUnit(long amount) {
		if (amount % BASE_CHARGE_UNIT != 0) {
			return BASE_CHARGE_UNIT * (amount / BASE_CHARGE_UNIT) + 1;
		}
		return amount;
	}

	private boolean isChargeDateChanged(LocalDate chargeRequestDate) {
		return !LocalDate.now().equals(chargeRequestDate);
	}
}
