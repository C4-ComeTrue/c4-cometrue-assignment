package org.c4marathon.assignment.global.aop;

import static org.c4marathon.assignment.transactional.domain.TransactionalStatus.*;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.c4marathon.assignment.account.service.AccountService;
import org.c4marathon.assignment.transactional.domain.TransferTransactional;
import org.c4marathon.assignment.transactional.domain.repository.TransactionalRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class DepositFailureHandler {
	private final AccountService accountService;
	private final TransactionalRepository transactionalRepository;

	@AfterThrowing(pointcut = "execution(* org.c4marathon.assignment.account.service.DepositService.successDeposit(..))", throwing = "ex")
	// @Retryable()
	public void handleDepositFailure(JoinPoint joinPoint, Exception ex) {

		Object[] args = joinPoint.getArgs();
		TransferTransactional transactional = (TransferTransactional) args[0];

		transactional.updateStatus(FAILED_DEPOSIT);
		transactionalRepository.save(transactional);
	}

	@AfterThrowing(pointcut = "execution(* org.c4marathon.assignment.account.service.DepositService.failedDeposit(..))", throwing = "ex")
	// @Retryable()
	public void handleFailedDepositFailure(JoinPoint joinPoint, Exception ex) {
		Object[] args = joinPoint.getArgs();
		TransferTransactional transactional = (TransferTransactional)args[0];

		transactional.updateStatus(CANCEL);
		transactionalRepository.save(transactional);

		accountService.rollbackWithdraw(transactional.getSenderAccountId(), transactional.getAmount());
	}
}
