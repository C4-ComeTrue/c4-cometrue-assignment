package org.c4marathon.assignment.account.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TypeValidator.class)
public @interface ValidType {
    String message() default "올바르지 못한 입력입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
