package org.c4marathon.assignment.account.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.entity.Type;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.c4marathon.assignment.util.exceptions.BaseException;
import org.c4marathon.assignment.util.exceptions.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
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

    @DisplayName("특정 사용자의 생성된 모든 계좌를 불러온다.")
    @Test
    void findAccountTest() {

        // given
        Member member = createMember("test1@naver.com", "test", "test");
        memberRepository.save(member);
        Account account = createAccount(Type.REGULAR_ACCOUNT, member);
        accountRepository.save(account);

        // when
        List<Account> accountList = accountRepository.findByMember(member);

        // then
        assertThat(accountList).isNotNull();
        assertThat(accountList).hasSize(1);
        assertThat(accountList.get(0).getMember().getId()).isEqualTo(member.getId());
    }

    @DisplayName("사용자의 특정 계좌를 불러온다.")
    @Test
    @Transactional
    void findMemberAccountTest() {

        // given
        Member member1 = createMember("test1@naver.com", "test", "test");
        memberRepository.save(member1);
        Account account1 = createAccount(Type.REGULAR_ACCOUNT, member1);
        accountRepository.save(account1);

        // when
        Account account = accountRepository.findByAccount(member1.getId(), account1.getId());

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
        Long afterBalance = 10000L;

        // when
        Account account = accountRepository.findByAccount(member1.getId(), account1.getId());
        Integer dailyLimit = account.getDailyLimit();
        Long balance = account.getBalance();
        // 하루 충전 금액이 300만원 보다 적어야 함.
        if (dailyLimit + afterBalance <= 3000000) {
            account.resetDailyLimit(dailyLimit + afterBalance.intValue());
            account.transferBalance(balance + afterBalance);
        }
        accountRepository.save(account);

        Account resultAccount = accountRepository.findByAccount(member1.getId(), account.getId());

        // then
        assertThat(resultAccount.getBalance()).isEqualTo(afterBalance);
        assertThat(resultAccount.getDailyLimit()).isEqualTo(dailyLimit + afterBalance);
    }

    @DisplayName("메인 계좌에서 적금 계좌로 돈을 이체한다.")
    @Test
    @Transactional
    void transferFromRegularAccountTest() {

        // given
        // 적금 계좌로 이체하려는 금액
        Long balance = 10000L;

        Member member1 = createMember("test1@naver.com", "test", "test");
        memberRepository.save(member1);
        // 메인 계좌
        Account regularAccount = createAccount(Type.REGULAR_ACCOUNT, member1);
        // 금액 추가
        regularAccount.transferBalance(balance);
        regularAccount.resetDailyLimit(balance.intValue());
        // 적금 계좌
        Account savingAccount = createAccount(Type.INSTALLMENT_SAVINGS_ACCOUNT, member1);
        accountRepository.saveAll(List.of(regularAccount, savingAccount));

        // when
        // 비관적 락을 걸어두어 행단위 잠금이 되었고, 해당 트랜잭션 안에서만 조회가 가능.

        Account afterSavingAccount = accountRepository.findByAccount(member1.getId(), savingAccount.getId());
        Account afterRegularAccount = accountRepository.findByRegularAccount(member1.getId());
        if (afterRegularAccount.getBalance() < balance) {
            throw new BaseException(ErrorCode.INSUFFICIENT_BALANCE.toString(), HttpStatus.FORBIDDEN.toString());
        }
        afterRegularAccount.transferBalance(afterRegularAccount.getBalance() - balance);
        afterSavingAccount.transferBalance(afterSavingAccount.getBalance() + balance);
        accountRepository.saveAll(List.of(afterRegularAccount, afterSavingAccount));

        Account resultSavingAccount = accountRepository.findByAccount(member1.getId(), afterSavingAccount.getId());
        Account resultRegularAccount = accountRepository.findByRegularAccount(member1.getId());

        // then
        assertThat(resultRegularAccount.getBalance()).isEqualTo(0);
        assertThat(resultSavingAccount.getBalance()).isEqualTo(10000);
    }
}