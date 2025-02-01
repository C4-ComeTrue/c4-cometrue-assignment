package org.c4marathon.assignment.account.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.SavingAccount;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.domain.repository.SavingAccountRepository;
import org.c4marathon.assignment.account.dto.WithdrawRequest;
import org.c4marathon.assignment.account.exception.DailyChargeLimitExceededException;
import org.c4marathon.assignment.account.exception.NotFoundAccountException;
import org.c4marathon.assignment.global.util.StringUtil;
import org.c4marathon.assignment.member.domain.Member;
import org.c4marathon.assignment.member.domain.repository.MemberRepository;
import org.c4marathon.assignment.member.exception.NotFoundMemberException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.c4marathon.assignment.global.util.Const.CHARGE_AMOUNT;
import static org.c4marathon.assignment.global.util.Const.DEFAULT_BALANCE;


@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final SavingAccountRepository savingAccountRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public void createAccount(Long memberId) {
        Account account = Account.create(DEFAULT_BALANCE);
        accountRepository.save(account);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
        member.setMainAccountId(account.getId());

        memberRepository.save(member);
    }

    /**
     * 메인 계좌에 돈을 충전하다.
     * 한 번에 메인 계좌에다가 충전을 여러 번 할 수도 있다. 어떻게 관리해야하나?
     * @param accountId
     * @param money
     */
    //기본값 -> Repeatable Read
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void chargeMoney(Long accountId, long money) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(NotFoundAccountException::new);

        if (!account.isChargeWithinDailyLimit(money)) {
            throw new DailyChargeLimitExceededException();
        }

        account.deposit(money);
        accountRepository.save(account);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void sendToSavingAccount(Long accountId, Long savingAccountId, long money) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(NotFoundAccountException::new);

        if (!account.isSend(money)) {
            autoCharge(money, account);
        }

        SavingAccount savingAccount = savingAccountRepository.findById(savingAccountId)
                .orElseThrow(NotFoundAccountException::new);

        account.withdraw(money);
        accountRepository.save(account);

        savingAccount.deposit(money);
        savingAccountRepository.save(savingAccount);
    }

    /**
     * 송금 시 출금하는 로직
     * 잔액 확인 후 잔액 부족시 10,000 단위로 충전 후 Redis Hash 에다가 금액을 누적
     * @param senderAccountId
     * @param request
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void withdraw(Long senderAccountId, WithdrawRequest request) {
        Account senderAccount = accountRepository.findByIdWithLock(senderAccountId)
                .orElseThrow(NotFoundAccountException::new);

        if (!senderAccount.isSend(request.money())) {
            autoCharge(request.money(), senderAccount);
        }

        senderAccount.withdraw(request.money());
        accountRepository.save(senderAccount);


        redisTemplate.opsForHash().increment(
                "pending-deposits",
                request.receiverAccountId(),
                request.money()
        );
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void withdraw2(Long senderAccountId, WithdrawRequest request) {
        Account senderAccount = accountRepository.findByIdWithLock(senderAccountId)
                .orElseThrow(NotFoundAccountException::new);

        if (!senderAccount.isSend(request.money())) {
            autoCharge(request.money(), senderAccount);
        }

        senderAccount.withdraw(request.money());
        accountRepository.save(senderAccount);

        String transactionId = UUID.randomUUID().toString();

        redisTemplate.opsForList().rightPush(
                "pending-deposits",
                StringUtil.format("{} : {} : {} : {}", transactionId, senderAccountId, request.receiverAccountId(), request.money())
        );
    }

    /**
     * 송금할 때 메인 계좌에 잔액이 부족할 때 10,000원 단위로 충전하는 로직
     * @param money
     * @param senderAccount
     */
    private void autoCharge(long money, Account senderAccount) {

        long needMoney = money - senderAccount.getMoney();
        long chargeMoney = ((needMoney + CHARGE_AMOUNT - 1) / CHARGE_AMOUNT) * CHARGE_AMOUNT;

        if (!senderAccount.isChargeWithinDailyLimit(chargeMoney)) {
            throw new DailyChargeLimitExceededException();
        }

        senderAccount.deposit(chargeMoney);
    }

}
