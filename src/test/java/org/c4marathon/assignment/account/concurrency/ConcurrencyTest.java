package org.c4marathon.assignment.account.concurrency;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.c4marathon.assignment.account.dto.request.RechargeAccountRequestDto;
import org.c4marathon.assignment.account.dto.request.TransferToSavingAccountRequestDto;
import org.c4marathon.assignment.account.entity.Account;
import org.c4marathon.assignment.account.entity.Type;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.account.service.AccountService;
import org.c4marathon.assignment.member.entity.Member;
import org.c4marathon.assignment.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@TestInstance(value = PER_CLASS)
@ActiveProfiles("test")
public class ConcurrencyTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AccountService accountService;

    private Member member;
    private Account mainAccount;
    private Account savingAccount;

    // 회원가입과 기본 계좌 생성
    @BeforeAll
    void setUp() {
        member = createMember();
        memberRepository.save(member);
        savingAccount = createAccount(Type.INSTALLMENT_SAVINGS_ACCOUNT, member);
        mainAccount = createAccount(Type.REGULAR_ACCOUNT, member);
        accountRepository.saveAll(List.of(mainAccount, savingAccount));
    }

    @AfterAll
    void tearDown() {
        accountRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    // 계좌 객체 생성
    private Account createAccount(Type type, Member member) {

        return Account.builder().type(type).member(member).build();
    }

    private Member createMember() {

        return Member.builder().email("test@naver.com").password("test").name("test").build();
    }

    private void setSecurityContext() {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new TestingAuthenticationToken(member.getId(), null, "ROLE_USER"));
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("메인 계좌, 적금 계좌 송금 동시성 테스트")
    class AccountConcurrency {

        @DisplayName("적금 계좌 입금과 메인 계좌 입금이 동시에 발생할 때 메인 계좌 잔액이 정확하게 갱신되어야 한다.")
        @Test
        void test_concurrent_transfer() throws InterruptedException {

            // given
            final int threadCount = 50;
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch countDownLatch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger();
            AtomicInteger failCount = new AtomicInteger();

            // when
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    try {
                        setSecurityContext();
                        // 메인 계좌 입금
                        accountService.rechargeAccount(new RechargeAccountRequestDto(mainAccount.getId(), 10000L));
                        // 적금 계좌로 출금
                        accountService.transferFromRegularAccount(
                            new TransferToSavingAccountRequestDto(10000L, savingAccount.getId()));
                        successCount.getAndIncrement();
                    } catch (Exception e) {
                        failCount.getAndIncrement();
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }

            countDownLatch.await();
            executorService.shutdown();

            Account resultMainAccount = accountRepository.findById(mainAccount.getId()).orElseThrow();
            Account resultSavingAccount = accountRepository.findById(savingAccount.getId()).orElseThrow();

            // then
            assertEquals(threadCount, successCount.get());
            assertEquals(0, failCount.get());
            assertEquals(0L, resultMainAccount.getBalance());
            assertEquals(10000L * threadCount, resultSavingAccount.getBalance());
        }
    }
}
