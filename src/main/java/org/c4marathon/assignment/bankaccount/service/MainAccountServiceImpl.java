package org.c4marathon.assignment.bankaccount.service;

import org.c4marathon.assignment.bankaccount.entity.MainAccount;
import org.c4marathon.assignment.bankaccount.exception.AccountErrorCode;
import org.c4marathon.assignment.bankaccount.limit.ChargeLimitManager;
import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MainAccountServiceImpl implements MainAccountService {

	private final MainAccountRepository mainAccountRepository;
	private final ChargeLimitManager chargeLimitManager;

	/**
	 *
	 * @param mainAccountPk 메인 계좌 pk
	 * @param money 충전할 금액
	 * @return 충전 후 계좌 잔고
	 *
	 * ChargeLimitManager를 통해 충전이 가능한지 확인하고 money만큼 충전 후 계좌 잔고를 리턴합니다.
	 */
	@Override
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public int chargeMoney(long mainAccountPk, int money) {
		if (!chargeLimitManager.charge(mainAccountPk, money)) {
			throw AccountErrorCode.CHARGE_LIMIT_EXCESS.accountException(
				"MainAccountServiceImpl에서 메인 계좌 충전 중 일일 충전 한도 초과 예외 발생");
		}

		MainAccount mainAccount = mainAccountRepository.findByIdForUpdate(mainAccountPk)
			.orElseThrow(() -> AccountErrorCode.ACCOUNT_NOT_FOUND.accountException(
				"MainAccountServiceImpl에서 메인 계좌 충전 중 ACCOUNT_NOT_FOUND 예외 발생"));

		mainAccount.chargeMoney(money);
		mainAccountRepository.save(mainAccount);

		return mainAccount.getMoney();
	}
}
