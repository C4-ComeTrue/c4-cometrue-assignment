package org.c4marathon.assignment;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.c4marathon.assignment.domain.SavingsType;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.ChargeLinkedAccountRepository;
import org.c4marathon.assignment.repository.SavingsAccountRepository;
import org.c4marathon.assignment.service.AccountService;
import org.c4marathon.assignment.service.ChargeService;
import org.c4marathon.assignment.service.MemberService;
import org.c4marathon.assignment.service.SavingsAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ConcurrencyTest {

	@Autowired SavingsAccountService savingsAccountService;

	@Autowired AccountService accountService;

	@Autowired MemberService memberService;

	@Autowired ChargeService chargeService;

	@Autowired AccountRepository accountRepository;

	@Autowired SavingsAccountRepository savingsAccountRepository;

	@Autowired ChargeLinkedAccountRepository chargeLinkedAccountRepository;

	@Test
	void 메인_계좌_충전_및_적금_자동_이체_동시성_테스트() throws InterruptedException {
		// given
		// 1. 회원 가입 -> 메인 계좌 자동 생성
		var response = memberService.register("email", "password");
		var memberId = response.memberId();
		var accountId = response.accountId();
		var concurrentUser = 2;
		var withdrawAmount = 5000;
		var chargeAmount = 10000;

		// 2. 적금 계좌 생성
		var res = savingsAccountService.createSavingsAccount(memberId, "name", withdrawAmount, SavingsType.REGULAR);
		var savingAccountId = res.id();

		var executorService = Executors.newFixedThreadPool(1000);
		var countDownLatch = new CountDownLatch(concurrentUser);

		// when
		for (int i = 0; i < concurrentUser; i++) {
			executorService.execute(() -> {
				chargeService.charge(accountId, chargeAmount);               // 메인 계좌 2번 10000원 충전 = 20000
				savingsAccountService.transferForRegularSavings(memberId);   // 메인 계좌 2번 5000원이 감소, 적금 계좌 2번 5000원 증가
				countDownLatch.countDown();
			});
		}

		countDownLatch.await();
		executorService.shutdown();

		// then
		var accountEntity = accountRepository.findById(accountId).orElseThrow();
		var savingsAccountEntity = savingsAccountRepository.findById(savingAccountId).orElseThrow();
		assertThat(accountEntity.getAmount()).isEqualTo(chargeAmount * concurrentUser - withdrawAmount * concurrentUser);
		assertThat(savingsAccountEntity.getAmount()).isEqualTo(withdrawAmount * concurrentUser);
	}

	@Test
	void 동시에_같은_계좌에_송금이_발생한다() throws InterruptedException {
		// given
		// 1. 회원 가입 -> 메인 계좌 자동 생성
		var userA = memberService.register("email1", "password1");
		var userB = memberService.register("email2", "password2");

		var userAAccountId = userA.accountId();
		var userBAccountId = userB.accountId();   // userB 에게 동시에 전송
		var transferAmount = 5000;
		var chargeAmount = 1000000;

		// 2. 각 회원이 10000원씩 충전한다.
		var userBAccountNumber = accountRepository.findById(userBAccountId).orElseThrow().getAccountNumber();
		chargeService.charge(userAAccountId, chargeAmount);

		var concurrentUser = 100;
		var executorService = Executors.newFixedThreadPool(1000);
		var countDownLatch = new CountDownLatch(concurrentUser);

		// when
		for (int i = 0; i < concurrentUser; i++) {
			executorService.execute(() -> {
				accountService.transfer(userAAccountId, userBAccountNumber, transferAmount);
				countDownLatch.countDown();
			});
		}

		countDownLatch.await();
		executorService.shutdown();

		// then
		var accountEntity = accountRepository.findById(userAAccountId).orElseThrow();
		assertThat(accountEntity.getAmount()).isEqualTo(chargeAmount - transferAmount * concurrentUser);
	}
}
