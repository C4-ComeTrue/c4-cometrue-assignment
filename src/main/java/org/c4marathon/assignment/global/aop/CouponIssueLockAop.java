package org.c4marathon.assignment.global.aop;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.c4marathon.assignment.global.aop.annotation.CouponIssueLock;
import org.c4marathon.assignment.global.aop.annotation.TransactionAop;
import org.c4marathon.assignment.global.common.CouponElParser;
import org.c4marathon.assignment.global.error.ErrorCode;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class CouponIssueLockAop {

	private static final String COUPON_ISSUE_LOCK_PREFIX = "EVENT_LOCK:";
	private final RedissonClient redissonClient;
	private final TransactionAop transactionAop;

	/**
	 * redis distributed lock, unlock을 진행하는 AOP
	 * 파라미터를 통해서 lock key를 정하고 redisson client의 tryLock 이후에 unlock
	 */
	@Around("@annotation(org.c4marathon.assignment.global.aop.annotation.CouponIssueLock)")
	public Object couponIssueLock(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature)joinPoint.getSignature();
		Method method = signature.getMethod();
		CouponIssueLock couponIssueLock = method.getAnnotation(CouponIssueLock.class);

		String key = COUPON_ISSUE_LOCK_PREFIX + CouponElParser.getDynamicValue(
			signature.getParameterNames(),
			joinPoint.getArgs(),
			couponIssueLock.key());
		RLock lock = redissonClient.getLock(key);

		try {
			if (!lock.tryLock(couponIssueLock.waitTime(), couponIssueLock.leaseTime(), TimeUnit.SECONDS)) {
				return false;
			}
			return transactionAop.proceed(joinPoint);
		} catch (InterruptedException e) {
			throw ErrorCode.SERVER_ERROR.baseException();
		} finally {
			if (lock.isLocked() && lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}
}
