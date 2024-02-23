package org.c4marathon.assignment.bankaccount.service;

import org.c4marathon.assignment.bankaccount.dto.response.MainAccountResponseDto;
import org.c4marathon.assignment.bankaccount.entity.MainAccount;
import org.c4marathon.assignment.bankaccount.entity.SavingAccount;
import org.c4marathon.assignment.bankaccount.exception.AccountErrorCode;
import org.c4marathon.assignment.bankaccount.limit.ChargeLimitManager;
import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.c4marathon.assignment.bankaccount.repository.SavingAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MainAccountService {

	private final MainAccountRepository mainAccountRepository;
	private final ChargeLimitManager chargeLimitManager;
	private final SavingAccountRepository savingAccountRepository;

	/**
	 *
	 * @param mainAccountPk 메인 계좌 pk
	 * @param money 충전할 금액
	 * @return 충전 후 계좌 잔고
	 *
	 * ChargeLimitManager를 통해 충전이 가능한지 확인하고 money만큼 충전 후 계좌 잔고를 리턴합니다.
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public long chargeMoney(long mainAccountPk, long money) {
		if (!chargeLimitManager.charge(mainAccountPk, money)) {
			throw AccountErrorCode.CHARGE_LIMIT_EXCESS.accountException(
				"충전 한도 초과, money = " + money);
		}

		MainAccount mainAccount = mainAccountRepository.findByPkForUpdate(mainAccountPk)
			.orElseThrow(() -> AccountErrorCode.ACCOUNT_NOT_FOUND.accountException(
				"존재하지 않는 계좌, mainAccountPk = " + mainAccountPk));

		mainAccount.chargeMoney(money);
		mainAccountRepository.save(mainAccount);

		return mainAccount.getMoney();
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void sendToSavingAccount(long mainAccountPk, long savingAccountPk, long money) {
		MainAccount mainAccount = mainAccountRepository.findByPkForUpdate(mainAccountPk)
			.orElseThrow(() -> AccountErrorCode.ACCOUNT_NOT_FOUND.accountException(
				"존재하지 않는 계좌, mainAccountPk = " + mainAccountPk));

		if (!isSendValid(mainAccount.getMoney(), money)) {
			throw AccountErrorCode.INVALID_MONEY_SEND.accountException(
				"현재 잔고 부족, mainAccount.getMoney() = " + mainAccount.getMoney() + ", send money = " + money);
		}

		SavingAccount savingAccount = savingAccountRepository.findByPkForUpdate(savingAccountPk)
			.orElseThrow(() -> AccountErrorCode.ACCOUNT_NOT_FOUND.accountException(
				"존재하지 않는 계좌, savingAccountPk = " + savingAccountPk));
		savingAccount.addMoney(money);
		savingAccountRepository.save(savingAccount);

		mainAccount.minusMoney(money);
		mainAccountRepository.save(mainAccount);
	}

	public MainAccountResponseDto getMainAccountInfo(long mainAccountPk) {
		MainAccount mainAccount = mainAccountRepository.findById(mainAccountPk)
			.orElseThrow(() -> AccountErrorCode.ACCOUNT_NOT_FOUND.accountException(
				"존재하지 않는 계좌, mainAccountPk = " + mainAccountPk));

		return new MainAccountResponseDto(mainAccount);
	}

	public boolean isSendValid(long myMoney, long sendMoney) {
		return myMoney >= sendMoney;
	}
}
