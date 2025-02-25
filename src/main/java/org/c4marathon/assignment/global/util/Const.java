package org.c4marathon.assignment.global.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Const {
    public static final long CHARGE_LIMIT = 3_000_000L;
    public static final long DEFAULT_BALANCE = 0L;
    public static final long CHARGE_AMOUNT = 10_000L;
    public static final int ACCOUNT_PREFIX_START = 0;
    public static final int ACCOUNT_PREFIX_END = 4;
    public static final String ACCOUNT_PREFIX = "3333";
    public static final String SAVING_ACCOUNT_PREFIX = "2222";
    public static final int PAGE_SIZE = 100;
    public static final String INTEREST_ACCOUNT = "44441234567812";

}
