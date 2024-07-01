package org.c4marathon.assignment.global.aop.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionAop {

	/**
	 * distributed lock 이후에 새로운 트랜잭션으로 감싸는 AOP
	 * REQUIRES_NEW가 아닌 REQUIRES(default)로 할 경우, 커밋 되지 않은 시점에서 다른 트랜잭션이 락을 얻을 수 있기 때문에
	 * 데이터 정합성에서 문제가 발생할 수 있음
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Object proceed(final ProceedingJoinPoint joinPoint) throws Throwable {
		return joinPoint.proceed();
	}
}
