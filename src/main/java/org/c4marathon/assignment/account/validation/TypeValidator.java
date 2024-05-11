package org.c4marathon.assignment.account.validation;

import java.util.EnumSet;

import org.c4marathon.assignment.account.entity.Type;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TypeValidator implements ConstraintValidator<ValidType, Type> {
    @Override
    public boolean isValid(Type value, ConstraintValidatorContext context) {
        return value == null || EnumSet.of(
            Type.REGULAR_ACCOUNT,
            Type.INSTALLMENT_SAVINGS_ACCOUNT,
            Type.FREEDOM_INSTALLMENT_SAVINGS_ACCOUNT
        ).contains(value);
    }
}
