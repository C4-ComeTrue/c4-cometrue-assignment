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
public class MainAccountServiceImpl implements MainAccountService {

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

	@Override
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void sendToSavingAccount(long mainAccountPk, long savingAccountPk, int money) {
		SavingAccount savingAccount = savingAccountRepository.findByPkForUpdate(savingAccountPk)
			.orElseThrow(() -> AccountErrorCode.ACCOUNT_NOT_FOUND.accountException(
				"MainAccountServiceImpl에서 sendToSavingAccount 메소드 실행 중 [Main Account NOT_FOUND] 예외 발생."));
		savingAccount.addMoney(money);
		savingAccountRepository.save(savingAccount);

		MainAccount mainAccount = mainAccountRepository.findByPkForUpdate(mainAccountPk)
			.orElseThrow(() -> AccountErrorCode.ACCOUNT_NOT_FOUND.accountException(
				"MainAccountServiceImpl에서 sendToSavingAccount 메소드 실행 중 [Main Account NOT_FOUND] 예외 발생."));

		if (!isSendValid(mainAccount.money, money)) {
			throw AccountErrorCode.INVALID_MONEY_SEND.accountException(
				"MainAccountServiceImpl에서 sendToSavingAccount 메소드 실행 중 잔고 부족 예외 발생.");
		}

		mainAccount.minusMoney(money);
		mainAccountRepository.save(mainAccount);
	}

	@Override
	public MainAccountResponseDto getMainAccountInfo(long mainAccountPk) {
		MainAccount mainAccount = mainAccountRepository.findById(mainAccountPk)
			.orElseThrow(() -> AccountErrorCode.ACCOUNT_NOT_FOUND.accountException(
				"MainAccountServiceImpl에서 getMainACcountInfo 메소드 실행 중 [Main Account NOT_FOUND] 예외 발생."));

		return MainAccountResponseDto.builder()
			.accountPk(mainAccount.getAccountPk())
			.chargeLimit(mainAccount.getChargeLimit())
			.money(mainAccount.getMoney())
			.build();
	}

	public boolean isSendValid(int myMoney, int sendMoney) {
		if (myMoney < sendMoney) {
			return false;
		}
		return true;
	}
}
