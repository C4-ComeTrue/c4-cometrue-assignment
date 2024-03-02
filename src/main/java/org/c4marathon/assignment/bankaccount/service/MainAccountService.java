package org.c4marathon.assignment.bankaccount.service;

import org.c4marathon.assignment.bankaccount.dto.response.MainAccountResponseDto;
import org.c4marathon.assignment.bankaccount.entity.MainAccount;
import org.c4marathon.assignment.bankaccount.entity.SavingAccount;
import org.c4marathon.assignment.bankaccount.exception.AccountErrorCode;
import org.c4marathon.assignment.bankaccount.limit.ChargeLimitManager;
import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.c4marathon.assignment.bankaccount.repository.SavingAccountRepository;
import org.c4marathon.assignment.common.utils.ConstValue;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
	private final DepositHandlerService depositHandlerService;
	private final ThreadPoolTaskExecutor executor;

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
			throw AccountErrorCode.CHARGE_LIMIT_EXCESS.accountException("충전 한도 초과, money = " + money);
		}

		MainAccount mainAccount = mainAccountRepository.findByPkForUpdate(mainAccountPk)
			.orElseThrow(() -> AccountErrorCode.ACCOUNT_NOT_FOUND.accountException());

		mainAccount.chargeMoney(money);
		mainAccountRepository.save(mainAccount);

		return mainAccount.getMoney();
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void sendToSavingAccount(long mainAccountPk, long savingAccountPk, long money) {
		MainAccount mainAccount = mainAccountRepository.findByPkForUpdate(mainAccountPk)
			.orElseThrow(AccountErrorCode.ACCOUNT_NOT_FOUND::accountException);

		autoMoneyChange(mainAccount, money);

		SavingAccount savingAccount = savingAccountRepository.findByPkForUpdate(savingAccountPk)
			.orElseThrow(AccountErrorCode.ACCOUNT_NOT_FOUND::accountException);
		savingAccount.addMoney(money);
		savingAccountRepository.save(savingAccount);

		mainAccountRepository.save(mainAccount);
	}

	public MainAccountResponseDto getMainAccountInfo(long mainAccountPk) {
		MainAccount mainAccount = mainAccountRepository.findById(mainAccountPk)
			.orElseThrow(() -> AccountErrorCode.ACCOUNT_NOT_FOUND.accountException(
				"존재하지 않는 계좌, mainAccountPk = " + mainAccountPk));

		return new MainAccountResponseDto(mainAccount);
	}

	/**
	 *
	 * 이체 로그는 현재 내가 이체 했다는 기록을 남깁니다.
	 * 해당 로그는 DB에 기록되며 상대방의 금액을 변경 후 상태를 입금 완료로 바꿉니다.
	 * 혹시 시스템 문제로 입금이 되지 않았을 경우를 위해 사용하는 로그로 주기적으로 해당 테이블을 체크하여 입금 처리 스레드 풀에 작업을 넣어줍니다.
	 *
	 * 입금 로직은 별도의 스레드 풀에서 처리합니다.
	 * 이는 락을 효율적으로 활용하기 위함입니다. 한 트랜잭션에서 두 개의 레코드에 락을 걸면 필요 이상으로 락을 소유하게 됩니다.
	 * 락을 짧게 가져가기 위해 출금과 입금 로직을 분리하였습니다. 입금 로직은 백그라운드에서 자동으로 실행됩니다.
	 * */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void sendToOtherAccount(long myAccountPk, long otherAccountPk, long money) {
		// 1. 나의 계좌에서 이체할 금액을 빼준다.
		MainAccount myAccount = mainAccountRepository.findByPkForUpdate(myAccountPk)
			.orElseThrow(AccountErrorCode.ACCOUNT_NOT_FOUND::accountException);

		autoMoneyChange(myAccount, money);
		mainAccountRepository.save(myAccount);
		// 2. 이체 로그를 남겨준다.

		// 3. 입금 로직은 다른 스레드 풀에게 넘기고 트랜잭션을 종료한다.
		depositHandlerService.doDeposit(otherAccountPk, 1000);
	}

	public boolean isSendValid(long myMoney, long sendMoney) {
		return myMoney >= sendMoney;
	}

	/**
	 *
	 * 메인 계좌의 돈을 자동으로 차감 또는 충전 후 차감 해주는 메소드
	 */
	public void autoMoneyChange(MainAccount mainAccount, long money) {
		// 잔고가 부족한 경우 자동 충전 시작
		if (!isSendValid(mainAccount.getMoney(), money)) {
			long minusMoney =
				money - mainAccount.getMoney(); // chargeMoney 계산 편의를 위해(양수로 만들기 위해) money - mainAccount.getMoney()
			long chargeMoney = (minusMoney / ConstValue.LimitConst.CHARGE_AMOUNT + 1)
				* ConstValue.LimitConst.CHARGE_AMOUNT; // 만 원 단위로 충전해야 할 금액
			if (!chargeLimitManager.charge(mainAccount.getAccountPk(), chargeMoney)) {
				throw AccountErrorCode.CHARGE_LIMIT_EXCESS.accountException("충전 한도 초과, money = " + chargeMoney);
			}
			chargeMoney = chargeMoney - money; // 실제로 계좌에 충전해야 하는 금액
			mainAccount.chargeMoney(chargeMoney);
		} else {
			mainAccount.minusMoney(money);
		}
	}
}
