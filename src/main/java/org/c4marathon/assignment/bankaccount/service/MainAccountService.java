package org.c4marathon.assignment.bankaccount.service;

import org.c4marathon.assignment.bankaccount.dto.response.MainAccountResponseDto;
import org.c4marathon.assignment.bankaccount.entity.MainAccount;
import org.c4marathon.assignment.bankaccount.entity.SavingAccount;
import org.c4marathon.assignment.bankaccount.exception.AccountErrorCode;
import org.c4marathon.assignment.bankaccount.message.util.RedisOperator;
import org.c4marathon.assignment.bankaccount.repository.MainAccountRepository;
import org.c4marathon.assignment.bankaccount.repository.SavingAccountRepository;
import org.c4marathon.assignment.common.utils.ConstValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MainAccountService {

	private final MainAccountRepository mainAccountRepository;
	private final SavingAccountRepository savingAccountRepository;
	private final RedisOperator redisOperator;

	@Value("${redis-stream.stream-key}")
	private String streamKey;

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
		MainAccount mainAccount = mainAccountRepository.findByPkForUpdate(mainAccountPk)
			.orElseThrow(() -> AccountErrorCode.ACCOUNT_NOT_FOUND.accountException());
		checkAndCharge(money, mainAccount);
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
	 * 나의 계좌의 금액만 차감하고 나머지 작업은 별도의 스레드 풀에 넘겨주는 메소드
	 * 정합성을 위해 별도의 SendRecord를 남겼습니다.
	 * */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void sendToOtherAccount(long senderPk, long depositPk, long money) {
		// 1. 나의 계좌에서 이체할 금액을 빼준다.
		MainAccount myAccount = mainAccountRepository.findByPkForUpdate(senderPk)
			.orElseThrow(AccountErrorCode.ACCOUNT_NOT_FOUND::accountException);

		autoMoneyChange(myAccount, money);
		mainAccountRepository.save(myAccount);

		// 2. 입금 로직을 위한 이체 메세지를 Redis에 넘겨주고 트랜잭션을 종료한다.
		redisOperator.addStream(streamKey, senderPk, depositPk, money);
	}

	/**
	 *
	 * 메인 계좌의 돈을 자동으로 차감 또는 충전 후 차감 해주는 메소드
	 */
	public void autoMoneyChange(MainAccount mainAccount, long money) {
		// 잔고가 부족한 경우 자동 충전 시작
		if (!mainAccount.canSend(money)) {
			// chargeMoney 계산 편의를 위해(양수로 만들기 위해) money - mainAccount.getMoney()
			long minusMoney = money - mainAccount.getMoney();
			// chargeMoney 계산
			long chargeMoney =
				((minusMoney + ConstValue.LimitConst.CHARGE_AMOUNT - 1) / ConstValue.LimitConst.CHARGE_AMOUNT)
					* ConstValue.LimitConst.CHARGE_AMOUNT; //
			checkAndCharge(chargeMoney, mainAccount); // 충전 한도 확인 및 변화
			mainAccount.minusMoney(money);
		} else {
			mainAccount.minusMoney(money);
		}
	}

	/**
	 *
	 * 충전 한도 테이블에서 충전 한도를 확인하고 가능하면 충전해주는 메소드
	 */
	public void checkAndCharge(long money, MainAccount mainAccount) {
		mainAccount.chargeCheck();
		if (!mainAccount.canCharge(money)) {
			throw AccountErrorCode.CHARGE_LIMIT_EXCESS.accountException("충전 한도 초과, money = " + money);
		}
		mainAccount.charge(money);
	}
}
