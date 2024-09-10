package org.c4marathon.assignment.config;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.AccountType;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Random;

@Configuration
@RequiredArgsConstructor
public class JoinEventHandler {
    private final AccountRepository accountRepository;

    Long random = new Random().nextLong();

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void craeteAccount(JoinEventDto joinEventDto){
        Account account = Account.builder()
                .accountNum(random)
                .type(AccountType.MAIN_ACCOUNT)
                .accountPw(1234)
                .limitaccount(3000000)
                .amount(0)
                .user(joinEventDto.getUser())
                .build();

        accountRepository.save(account);
    }
}
