package org.c4marathon.assignment.account.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.SavingAccount;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.domain.repository.SavingAccountRepository;
import org.c4marathon.assignment.account.exception.DailyChargeLimitExceededException;
import org.c4marathon.assignment.account.exception.InsufficientBalanceException;
import org.c4marathon.assignment.account.exception.NotFoundAccountException;
import org.c4marathon.assignment.member.domain.Member;
import org.c4marathon.assignment.member.domain.repository.MemberRepository;
import org.c4marathon.assignment.member.exception.NotFoundMemberException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.c4marathon.assignment.global.util.Const.CHARGE_LIMIT;


@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final SavingAccountRepository savingAccountRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void createAccount(Long memberId) {
        Account account = Account.create();
        accountRepository.save(account);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        member.setMainAccountId(account.getId());

        memberRepository.save(member);
    }

    /**
     * 메인 계좌에 돈을 충전하다.
     * 한 번에 메인 계좌에다가 충전을 여러 번 할 수도 있다. 어떻게 관리해야하나?
     * 나의 외부계좌에서 내 메인 계좌로 충전하는 것이 충돌 가능성이 그렇게 높지는 않을 것 같다.
     * 굳이 비관적 락을 사용할 필요는 없을 것 같다. 낙관적 락을 사용
     * @param accountId
     * @param money
     */
    //기본값 -> Repeatable Read
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void chargeMoney(Long accountId, long money) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(NotFoundAccountException::new);

        if (!account.isCharge(money)) {
            throw new DailyChargeLimitExceededException();
        }

        //OptimisticLockException 예외 발생시 globalHandlerException 에서 잡자
        account.chargeAccount(money);
        accountRepository.save(account);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void sendToSavingAccount(Long accountId, Long savingAccountId, long money) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(NotFoundAccountException::new);

        if (!account.isSend(money)) {
            throw new InsufficientBalanceException();
        }

        SavingAccount savingAccount = savingAccountRepository.findById(savingAccountId)
                .orElseThrow(NotFoundAccountException::new);

        account.minusMoney(money);
        accountRepository.save(account);

        savingAccount.addMoney(money);
        savingAccountRepository.save(savingAccount);
    }

    /**
     * ChargeLimit 가 3_000_000이 아닌 Account 를 모두 조회해서 일일 한도를 매일 0시에 초기화한다.
     * 배치 업데이트를 위해 JdbcTemplate 를 사용했다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetChargeLimit() {
        int batchSize = 1000;
        Long lastCursorId = 0L;

        while (true) {
            List<Long> accountIds = jdbcTemplate.query(
                    "SELECT account_id FROM account WHERE charge_limit < ? AND account_id > ? ORDER BY account_id ASC LIMIT ?",
                    new Object[] { CHARGE_LIMIT, lastCursorId, batchSize },
                    (rs, rowNum) -> rs.getLong("account_id")
            );

            if (accountIds.isEmpty()) {
                break;
            }
            jdbcTemplate.batchUpdate(
                    "UPDATE account SET charge_limit = ? WHERE account_id = ?",
                    accountIds,
                    batchSize,
                    (ps, accountId) -> {
                        ps.setLong(1, CHARGE_LIMIT);
                        ps.setLong(2, accountId);
                    }
            );

            lastCursorId = accountIds.get(accountIds.size() - 1);
        }
    }
}
