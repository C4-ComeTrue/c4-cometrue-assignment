package org.c4marathon.assignment.account.service;

import static org.c4marathon.assignment.global.util.Const.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.exception.NotFoundAccountException;
import org.c4marathon.assignment.global.core.MiniPayThreadPoolExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositService {
	private final AccountRepository accountRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final MiniPayThreadPoolExecutor threadPoolExecutor = new MiniPayThreadPoolExecutor(8, 32);

	@Scheduled(fixedRate = 10000)
	public void deposits() {
		threadPoolExecutor.init();

		List<String> deposits = redisTemplate.opsForList().range(PENDING_DEPOSIT, 0, -1);
		if (deposits == null || deposits.isEmpty()) {
			return;
		}

		for (String deposit : deposits) {
			threadPoolExecutor.execute(() -> processDeposit(deposit));
		}

		try {
			threadPoolExecutor.waitToEnd();
		} catch (Exception e) {
			log.error("스레드 풀 실행 중 예외 발생 : {}", e.getMessage(), e);
		}
	}

	/**
	 * 입금 실패한 경우가 많이 없을 것이라고 생각하여 멀티 스레드 X
	 *  나중에 멀티 스레드 성능 테스트 후 결정
	 */
	@Scheduled(fixedRate = 12000)
	public void rollbackDeposits() {
		List<String> failedDeposits = redisTemplate.opsForList().range(FAILED_DEPOSIT, 0, -1);
		if (failedDeposits == null || failedDeposits.isEmpty()) {
			return;
		}

		for (String depositRequest : failedDeposits) {
			processFailedDeposit(depositRequest);
		}
	}

	private void processDeposit(String deposit) {
		String[] parts = deposit.split(":");
		String transactionId = parts[0];
		Long senderAccountId = Long.valueOf(parts[1]);
		Long receiverAccountId = Long.valueOf(parts[2]);
		long money = Long.parseLong(parts[3]);

		try {
			redisTemplate.opsForList().remove(PENDING_DEPOSIT, 1, deposit);

			Account receiverAccount = accountRepository.findByIdWithLock(receiverAccountId)
				.orElseThrow(NotFoundAccountException::new);

			receiverAccount.deposit(money);
			accountRepository.save(receiverAccount);

			log.debug("입금 성공: transactionId={}, senderAccountId={}, receiverAccountId={}, money={}",
				transactionId, senderAccountId, receiverAccountId, money);
		} catch (Exception e) {
			redisTemplate.opsForList().rightPush(FAILED_DEPOSIT, deposit);
			log.debug("입금 실패: transactionId={}, senderAccountId={}, receiverAccountId={}, money={}",
				transactionId, senderAccountId, receiverAccountId, money);

		}
	}

	private void processFailedDeposit(String failedDeposit) {
		String[] parts = failedDeposit.split(":");
		String transactionId = parts[0];
		Long senderAccountId = Long.valueOf(parts[1]);
		Long receiverAccountId = Long.valueOf(parts[2]);
		long money = Long.parseLong(parts[3]);

		try {
			Account receiverAccount = accountRepository.findByIdWithLock(receiverAccountId)
				.orElseThrow(NotFoundAccountException::new);

			receiverAccount.deposit(money);
			accountRepository.save(receiverAccount);
			redisTemplate.opsForList().remove(FAILED_DEPOSIT, 1, failedDeposit);
			log.debug("입금 재시도 성공: transactionId={}, senderAccountId={}, receiverAccountId={}, money={}",
				transactionId, senderAccountId, receiverAccountId, money);
		} catch (Exception e) {
			log.debug("입금 재시도 실패: transactionId={}, senderAccountId={}, receiverAccountId={}, money={}",
				transactionId, senderAccountId, receiverAccountId, money);

			int retryCount = redisTemplate.opsForValue().increment("deposit-failures:" + transactionId).intValue();

			if (retryCount > MAX_RETRIES) {
				log.error("입금 실패 횟수 초과, 출금 롤백 진행: transactionId={}, senderId={}, money={}", transactionId,
					senderAccountId, money);

				redisTemplate.opsForList().remove(FAILED_DEPOSIT, 1, failedDeposit);
				rollbackWithdraw(senderAccountId, money);

				redisTemplate.delete("deposit-failures:" + transactionId);
			}
		}
	}

	private void rollbackWithdraw(Long senderAccountId, long money) {
		Account senderAccount = accountRepository.findByIdWithLock(senderAccountId)
			.orElseThrow(NotFoundAccountException::new);

		senderAccount.deposit(money);
		accountRepository.save(senderAccount);
	}
}
