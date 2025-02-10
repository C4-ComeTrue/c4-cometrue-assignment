package org.c4marathon.assignment.global.aop;

import static org.c4marathon.assignment.global.util.Const.*;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.c4marathon.assignment.account.service.AccountService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class DepositFailureHandler {
	private final RedisTemplate<String, String> redisTemplate;
	private final AccountService accountService;

	@AfterThrowing(pointcut = "execution(* org.c4marathon.assignment.account.service.DepositService.successDeposit(..))", throwing = "ex")
	public void handleDepositFailure(JoinPoint joinPoint, Exception ex) {

		Object[] args = joinPoint.getArgs();
		String deposit = (String) args[0];

		redisTemplate.opsForList().remove(PENDING_DEPOSIT, 1, deposit);
		redisTemplate.opsForList().rightPush(FAILED_DEPOSIT, deposit);
	}

	@AfterThrowing(pointcut = "execution(* org.c4marathon.assignment.account.service.DepositService.failedDeposit(..))", throwing = "ex")
	public void handleFailedDepositFailure(JoinPoint joinPoint, Exception ex) {
		Object[] args = joinPoint.getArgs();
		String failedDeposit = (String) args[0];

		String[] parts = failedDeposit.split(":");
		Long senderAccountId = Long.valueOf(parts[1]);
		long money = Long.parseLong(parts[3]);

		accountService.rollbackWithdraw(senderAccountId, money);
		redisTemplate.opsForList().remove(FAILED_DEPOSIT, 1, failedDeposit);
	}
}
