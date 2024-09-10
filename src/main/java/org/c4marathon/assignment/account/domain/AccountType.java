package org.c4marathon.assignment.account.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountType {
    MAIN_ACCOUNT("메인계좌"),
    SAVING_ACCOUNT("적금계좌");

    private final String type;
}
