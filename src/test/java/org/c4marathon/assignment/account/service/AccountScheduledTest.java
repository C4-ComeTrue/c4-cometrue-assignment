package org.c4marathon.assignment.account.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.entity.Type;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
public class AccountScheduledTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void tearDown() {

        accountRepository.deleteAllInBatch();
    }

    // 계좌 객체 생성
    private Account createAccount(Type type, Member member) {

        return Account.builder().type(type).member(member).build();
    }

    private Member createMember(String email, String password, String name) {

        return Member.builder().email(email).password(password).name(name).build();
    }

    @DisplayName("생성된 모든 메인 계좌를 불러와서 1일 한도를 1000으로 초기화한다.")
    @Test
    @Transactional
    void resetDailyLimitTest() {

        // given
        Member member1 = createMember("test1@naver.com", "test", "test");
        Member member2 = createMember("test2@naver.com", "test", "test");
        Member member3 = createMember("test3@naver.com", "test", "test");
        memberRepository.saveAll(List.of(member1, member2, member3));
        Account account1 = createAccount(Type.REGULAR_ACCOUNT, member1);
        Account account2 = createAccount(Type.REGULAR_ACCOUNT, member2);
        Account account3 = createAccount(Type.REGULAR_ACCOUNT, member3);
        accountRepository.saveAll(List.of(account1, account2, account3));
        List<Account> accountList = accountRepository.findAll();

        // when
        for (int i = 0; i < accountList.size(); i++) {
            accountList.get(i).resetDailyLimit(1000);
        }
        accountRepository.saveAll(accountList);
        List<Account> afterAccountList = accountRepository.findAll();

        // then
        assertTrue(afterAccountList.stream().allMatch(account -> account.getDailyLimit() == 1000));
    }
}
