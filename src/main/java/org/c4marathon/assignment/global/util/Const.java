package org.c4marathon.assignment.global.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Const {
    public static final long CHARGE_LIMIT = 3_000_000L;
    public static final long DEFAULT_BALANCE = 0L;
    public static final long CHARGE_AMOUNT = 10_000L;
    public static final long MAX_RETRIES = 3;
    public static final String PENDING_DEPOSIT = "pending-deposit";
    public static final String FAILED_DEPOSIT = "failed-deposit";


}
