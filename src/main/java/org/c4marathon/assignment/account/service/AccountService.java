package org.c4marathon.assignment.account.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.repository.AccountRepository;
import org.c4marathon.assignment.account.exception.DailyChargeLimitExceededException;
import org.c4marathon.assignment.account.exception.NotFoundAccountException;
import org.c4marathon.assignment.member.domain.Member;
import org.c4marathon.assignment.member.domain.repository.MemberRepository;
import org.c4marathon.assignment.member.exception.NotFoundMemberException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;

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
     * @param accountId
     * @param money
     */
    //기본값 -> Repeatable Read
    @Transactional
    public void chargeMoney(Long accountId, long money) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(NotFoundAccountException::new);

        if (!account.isCharge(money)) {
            throw new DailyChargeLimitExceededException();
        }

        account.chargeAccount(money);
        accountRepository.save(account);
    }

}
