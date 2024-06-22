package org.c4marathon.assignment.global.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CouponIssueLock {

	String key() default "";

	long waitTime() default 5;

	long leaseTime() default 1;
}
