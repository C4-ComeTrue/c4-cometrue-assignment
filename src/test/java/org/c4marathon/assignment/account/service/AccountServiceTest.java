package org.c4marathon.assignment.account.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

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
public class AccountServiceTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void tearDown() {

        accountRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    // 계좌 객체 생성
    private Account createAccount(Type type, Member member) {

        return Account.builder()
            .type(type)
            .member(member)
            .build();
    }

    private Member createMember(String email, String password, String name) {

        return Member.builder()
            .email(email)
            .password(password)
            .name(name)
            .build();
    }

    @DisplayName("계좌 생성 요청에 의해 회원의 계좌를 생성한다.")
    @Test
    @Transactional
    void createAccountTest() {

        // given
        Member member = createMember("test@naver.com", "test", "test");
        memberRepository.save(member);
        Account account = createAccount(Type.REGULAR_ACCOUNT, member);

        // when
        accountRepository.save(account);

        // then
        Optional<Account> findAccount = accountRepository.findById(account.getId());
        assertThat(findAccount).isPresent();
        assertThat(findAccount.get().getId()).isEqualTo(account.getId());
    }

    @DisplayName("생성된 모든 메인 계좌를 불러온다.")
    @Test
    void findRegularAccountTest() {

        // given
        Member member1 = createMember("test1@naver.com", "test", "test");
        Member member2 = createMember("test2@naver.com", "test", "test");
        Member member3 = createMember("test3@naver.com", "test", "test");
        memberRepository.saveAll(List.of(member1, member2, member3));
        Account account1 = createAccount(Type.REGULAR_ACCOUNT, member1);
        Account account2 = createAccount(Type.REGULAR_ACCOUNT, member2);
        Account account3 = createAccount(Type.REGULAR_ACCOUNT, member3);
        accountRepository.saveAll(List.of(account1, account2, account3));

        // when
        List<Account> accountList = accountRepository.findByType(Type.REGULAR_ACCOUNT);

        // then
        assertThat(accountList).isNotNull();
        assertTrue(accountList.stream().allMatch(account -> account.getType() == Type.REGULAR_ACCOUNT));
     }

     @DisplayName("사용자의 메인 계좌를 불러온다.")
     @Test
     @Transactional
     void findAccountTest() {

         // given
         Member member1 = createMember("test1@naver.com", "test", "test");
         memberRepository.save(member1);
         Account account1 = createAccount(Type.REGULAR_ACCOUNT, member1);
         accountRepository.save(account1);

         // when
         Account account = accountRepository.findByAccount(member1.getId(), Type.REGULAR_ACCOUNT);

         // then
         assertThat(account1.getId()).isEqualTo(account.getId());
      }

     @DisplayName("사용자의 외부 계좌에서 메인 계좌로 10,000원을 이체한다.")
     @Test
     @Transactional
     void transferToRegularAccountTest() {

         // given
         Member member1 = createMember("test1@naver.com", "test", "test");
         memberRepository.save(member1);
         Account account1 = createAccount(Type.REGULAR_ACCOUNT, member1);
         accountRepository.save(account1);

         // 계좌 잔액
         Integer afterBalance = 10000;

         // when
         Account account = accountRepository.findByAccount(member1.getId(), Type.REGULAR_ACCOUNT);
         Integer dailyLimit = account.getDailyLimit();
         Integer balance = account.getBalance();
         // 하루 충전 금액이 300만원 보다 적어야 함.
         if (dailyLimit+afterBalance <= 3000000){
             account.resetDailyLimit(dailyLimit+afterBalance);
             account.transferBalance(balance+afterBalance);
         }
         accountRepository.save(account);

         Account resultAccount = accountRepository.findByAccount(member1.getId(), Type.REGULAR_ACCOUNT);

         // then
         assertThat(resultAccount.getBalance()).isEqualTo(afterBalance);
         assertThat(resultAccount.getDailyLimit()).isEqualTo(dailyLimit + afterBalance);
      }
}